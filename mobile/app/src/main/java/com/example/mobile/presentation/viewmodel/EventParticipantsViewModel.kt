package com.example.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile.data.model.ParticipantApiModel
import com.example.mobile.data.repository.EventWorkspaceRepository
import com.example.mobile.presentation.participantDisplayTitle
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val SEARCH_DEBOUNCE_MS = 350L

data class EventParticipantsUiState(
    val eventTitle: String? = null,
    val participants: List<ParticipantApiModel> = emptyList(),
    val searchQuery: String = "",
    val debouncedSearchQuery: String = "",
    val currentUserId: String? = null,
    val isOrganizer: Boolean = false,
    val isLoading: Boolean = false,
    val actionInProgress: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val cacheHint: String? = null
)

class EventParticipantsViewModel(
    private val repository: EventWorkspaceRepository,
    private val eventId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventParticipantsUiState())
    val uiState: StateFlow<EventParticipantsUiState> = _uiState.asStateFlow()

    private var searchDebounceJob: Job? = null

    init {
        refresh()
    }

    /** Строка поиска обновляется сразу; фильтрация списка — после паузы [SEARCH_DEBOUNCE_MS], как на экране приглашений. */
    fun onSearchQueryChange(raw: String) {
        _uiState.update { it.copy(searchQuery = raw) }
        searchDebounceJob?.cancel()
        searchDebounceJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            _uiState.update { it.copy(debouncedSearchQuery = raw.trim()) }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    successMessage = null,
                    cacheHint = null
                )
            }
            val userRes = repository.getCurrentUser()
            val me = userRes.getOrNull()?.id

            val eventResult = repository.loadEvent(eventId)
            val participantsResult = repository.loadParticipants(eventId)
            val title = eventResult.getOrNull()?.value?.name
            val err = buildString {
                userRes.exceptionOrNull()?.message?.let { append(it) }
                eventResult.exceptionOrNull()?.message?.let {
                    if (isNotEmpty()) append("\n")
                    append(it)
                }
                participantsResult.exceptionOrNull()?.message?.let {
                    if (isNotEmpty()) append("\n")
                    append(it)
                }
            }.trim().ifBlank { null }
            val list = participantsResult.getOrNull()?.value ?: emptyList()
            val sorted = sortParticipants(list)
            val organizer = me != null &&
                sorted.any { it.userId == me && it.role.equals("ORGANIZER", ignoreCase = true) }
            val fromCache = eventResult.getOrNull()?.fromCache == true ||
                participantsResult.getOrNull()?.fromCache == true
            _uiState.update {
                it.copy(
                    isLoading = false,
                    eventTitle = title,
                    participants = sorted,
                    currentUserId = me,
                    isOrganizer = organizer,
                    errorMessage = err,
                    cacheHint = if (fromCache) {
                        "Данные из кэша. При необходимости нажмите «Обновить»."
                    } else {
                        null
                    }
                )
            }
        }
    }

    fun changeParticipantRole(participantUserId: String, newRole: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(actionInProgress = true, errorMessage = null, successMessage = null)
            }
            val result = repository.changeParticipantRole(eventId, participantUserId, newRole)
            if (result.isFailure) {
                _uiState.update {
                    it.copy(
                        actionInProgress = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Ошибка смены роли"
                    )
                }
                return@launch
            }
            applyParticipantsAfterMutation("Роль обновлена")
        }
    }

    fun removeParticipant(participantUserId: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(actionInProgress = true, errorMessage = null, successMessage = null)
            }
            val result = repository.removeParticipant(eventId, participantUserId)
            if (result.isFailure) {
                _uiState.update {
                    it.copy(
                        actionInProgress = false,
                        errorMessage = result.exceptionOrNull()?.message
                            ?: "Не удалось исключить участника"
                    )
                }
                return@launch
            }
            applyParticipantsAfterMutation("Участник исключён")
        }
    }

    private suspend fun applyParticipantsAfterMutation(success: String) {
        val reload = repository.loadParticipants(eventId)
        val list = reload.getOrNull()?.value ?: _uiState.value.participants
        val sorted = sortParticipants(list)
        val me = _uiState.value.currentUserId
        val organizer = me != null &&
            sorted.any { it.userId == me && it.role.equals("ORGANIZER", ignoreCase = true) }
        val reloadErr = reload.exceptionOrNull()?.message
        _uiState.update {
            it.copy(
                actionInProgress = false,
                participants = sorted,
                isOrganizer = organizer,
                successMessage = success,
                errorMessage = reloadErr
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun dismissCacheHint() {
        _uiState.update { it.copy(cacheHint = null) }
    }

    private fun sortParticipants(list: List<ParticipantApiModel>): List<ParticipantApiModel> {
        return list.sortedWith(
            compareBy<ParticipantApiModel> {
                when (it.role.uppercase()) {
                    "ORGANIZER" -> 0
                    "COORDINATOR" -> 1
                    else -> 2
                }
            }.thenBy { participantDisplayTitle(it).lowercase() }
        )
    }
}
