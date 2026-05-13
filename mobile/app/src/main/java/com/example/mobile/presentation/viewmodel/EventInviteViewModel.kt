package com.example.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile.data.model.User
import com.example.mobile.data.repository.EventWorkspaceRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Пауза после последнего изменения строки поиска перед запросом к API (debounce), чтобы не вызывать поиск на каждый символ. */
private const val SEARCH_DEBOUNCE_MS = 350L

data class EventInviteUiState(
    val eventTitle: String? = null,
    val searchQuery: String = "",
    val searchResults: List<User> = emptyList(),
    /** Уже участники и адресаты с активным исходящим приглашением — не показываем в поиске. */
    val excludedInviteEmailsLower: Set<String> = emptySet(),
    val inviteRole: String = "PERFORMER",
    val manualEmail: String = "",
    val isSearchLoading: Boolean = false,
    val isInviteLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class EventInviteViewModel(
    private val repository: EventWorkspaceRepository,
    private val eventId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventInviteUiState())
    val uiState: StateFlow<EventInviteUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            val eventRes = repository.loadEvent(eventId)
            val title = eventRes.getOrNull()?.value?.name
            val partRes = repository.loadParticipants(eventId)
            val participantEmails =
                partRes.getOrNull()?.value?.map { it.email.lowercase() }?.toSet() ?: emptySet()
            val pendingEmails = repository.getPendingOutboundInviteEmails(eventId).getOrElse { emptySet() }
            val excluded = participantEmails + pendingEmails
            _uiState.update { it.copy(eventTitle = title, excludedInviteEmailsLower = excluded) }
        }
    }

    /** Обновляет строку поиска; запрос к серверу выполняется только после паузы ввода [SEARCH_DEBOUNCE_MS]. */
    fun onSearchQueryChange(raw: String) {
        _uiState.update {
            it.copy(searchQuery = raw, errorMessage = null, successMessage = null)
        }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            val q = raw.trim()
            if (q.length < 2) {
                _uiState.update { it.copy(searchResults = emptyList(), isSearchLoading = false) }
                return@launch
            }
            _uiState.update { it.copy(isSearchLoading = true) }
            val result = repository.searchUsers(q)
            _uiState.update { s ->
                if (result.isSuccess) {
                    val list = result.getOrNull().orEmpty()
                    val filtered = list.filter { u -> !s.excludedInviteEmailsLower.contains(u.email.lowercase()) }
                    s.copy(searchResults = filtered, isSearchLoading = false)
                } else {
                    s.copy(
                        isSearchLoading = false,
                        errorMessage = result.exceptionOrNull()?.message,
                        searchResults = emptyList()
                    )
                }
            }
        }
    }

    fun setInviteRole(role: String) {
        _uiState.update { it.copy(inviteRole = role) }
    }

    fun onManualEmailChange(value: String) {
        _uiState.update { it.copy(manualEmail = value, errorMessage = null, successMessage = null) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun inviteByEmail(email: String, onDone: (Boolean) -> Unit) {
        val trimmed = email.trim()
        if (trimmed.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Введите email") }
            onDone(false)
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isInviteLoading = true, errorMessage = null, successMessage = null) }
            val role = _uiState.value.inviteRole
            val result = repository.inviteParticipant(eventId, trimmed, role)
            if (result.isSuccess) {
                _uiState.update { s ->
                    val ex = s.excludedInviteEmailsLower + trimmed.lowercase()
                    s.copy(
                        isInviteLoading = false,
                        successMessage = "Приглашение отправлено",
                        excludedInviteEmailsLower = ex,
                        searchResults = s.searchResults.filter { !ex.contains(it.email.lowercase()) }
                    )
                }
                onDone(true)
            } else {
                _uiState.update {
                    it.copy(
                        isInviteLoading = false,
                        errorMessage = result.exceptionOrNull()?.message
                    )
                }
                onDone(false)
            }
        }
    }
}
