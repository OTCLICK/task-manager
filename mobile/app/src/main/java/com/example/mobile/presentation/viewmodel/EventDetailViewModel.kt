package com.example.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile.data.model.EventApiModel
import com.example.mobile.data.model.EventTaskReportResponse
import com.example.mobile.data.model.ParticipantApiModel
import com.example.mobile.data.model.Task
import com.example.mobile.data.model.TaskCreateRequest
import com.example.mobile.data.model.TaskUpdateRequest
import com.example.mobile.data.model.Zone
import com.example.mobile.data.model.ZoneCreateRequest
import com.example.mobile.data.repository.EventWorkspaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EventDetailUiState(
    val event: EventApiModel? = null,
    val zones: List<Zone> = emptyList(),
    val tasks: List<Task> = emptyList(),
    val participants: List<ParticipantApiModel> = emptyList(),
    val currentUserId: String? = null,
    val isEventPerformer: Boolean = false,
    val mutedZoneIds: Set<String> = emptySet(),
    /** Текущий пользователь — организатор этого мероприятия (может приглашать по API). */
    val canInviteParticipants: Boolean = false,
    /** Организатор или координатор — создание, редактирование и удаление задач. */
    val canManageTasks: Boolean = false,
    /** Организатор — смена статуса мероприятия и отчёт. */
    val canPatchEventStatus: Boolean = false,
    val canViewTaskReport: Boolean = false,
    val reportVisible: Boolean = false,
    val reportLoading: Boolean = false,
    val taskReport: EventTaskReportResponse? = null,
    val reportError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val formError: String? = null,
    /** Подсказка, если хотя бы часть экрана взята из локального кэша (сеть недоступна или ошибка сервера). */
    val cacheHint: String? = null
)

class EventDetailViewModel(
    private val repository: EventWorkspaceRepository,
    private val eventId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventDetailUiState())
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, errorMessage = null, formError = null, cacheHint = null)
            }
            val eventResult = repository.loadEvent(eventId)
            if (eventResult.isFailure) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = eventResult.exceptionOrNull()?.message
                    )
                }
                return@launch
            }
            val eventLoad = eventResult.getOrNull()!!
            val meResult = repository.getCurrentUser()
            val meId = meResult.getOrNull()?.id
            val mutedResult = repository.getMutedZoneIds()
            val mutedZones = mutedResult.getOrNull()?.toSet() ?: emptySet()
            val zonesResult = repository.loadZones(eventId)
            val tasksResult = repository.loadTasks(eventId)
            val participantsResult = repository.loadParticipants(eventId)
            val participants = participantsResult.getOrNull()?.value ?: emptyList()
            val canInvite = meId != null &&
                participants.any { it.userId == meId && it.role.equals("ORGANIZER", ignoreCase = true) }
            val canManageTasks = meId != null &&
                participants.any {
                    it.userId == meId &&
                        (it.role.equals("ORGANIZER", ignoreCase = true) ||
                            it.role.equals("COORDINATOR", ignoreCase = true))
                }
            val isEventPerformer = meId != null &&
                participants.any { it.userId == meId && it.role.equals("PERFORMER", ignoreCase = true) }
            val sideErr = buildString {
                zonesResult.exceptionOrNull()?.message?.let { append(it) }
                tasksResult.exceptionOrNull()?.message?.let {
                    if (isNotEmpty()) append("\n")
                    append(it)
                }
                participantsResult.exceptionOrNull()?.message?.let {
                    if (isNotEmpty()) append("\n")
                    append(it)
                }
                mutedResult.exceptionOrNull()?.message?.let {
                    if (isNotEmpty()) append("\n")
                    append(it)
                }
            }.trim().ifBlank { null }

            val anyFromCache = eventLoad.fromCache ||
                zonesResult.getOrNull()?.fromCache == true ||
                tasksResult.getOrNull()?.fromCache == true ||
                participantsResult.getOrNull()?.fromCache == true

            _uiState.update {
                it.copy(
                    isLoading = false,
                    event = eventLoad.value,
                    zones = zonesResult.getOrNull()?.value ?: emptyList(),
                    tasks = tasksResult.getOrNull()?.value ?: emptyList(),
                    participants = participants,
                    currentUserId = meId,
                    isEventPerformer = isEventPerformer,
                    mutedZoneIds = mutedZones,
                    canInviteParticipants = canInvite,
                    canManageTasks = canManageTasks,
                    canPatchEventStatus = canInvite,
                    canViewTaskReport = canManageTasks,
                    errorMessage = sideErr,
                    cacheHint = if (anyFromCache) {
                        "Показаны сохранённые данные. Проверьте сеть и при необходимости нажмите «Обновить»."
                    } else {
                        null
                    }
                )
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null, formError = null) }
    }

    fun dismissCacheHint() {
        _uiState.update { it.copy(cacheHint = null) }
    }

    fun createZone(
        name: String,
        description: String?,
        participatesCount: Int?,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(formError = null) }
            val result = repository.createZone(
                ZoneCreateRequest(
                    name = name,
                    description = description,
                    participatesCount = participatesCount,
                    eventId = eventId
                )
            )
            if (result.isSuccess) {
                refresh()
                onResult(true)
            } else {
                _uiState.update { it.copy(formError = result.exceptionOrNull()?.message) }
                onResult(false)
            }
        }
    }

    fun createTask(
        title: String,
        description: String?,
        zoneId: String?,
        priority: String?,
        deadline: String?,
        performerIds: List<String>,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(formError = null) }
            val result = repository.createTask(
                TaskCreateRequest(
                    title = title,
                    description = description,
                    taskPriority = priority ?: "MEDIUM",
                    zoneId = zoneId,
                    performers = performerIds.takeIf { it.isNotEmpty() },
                    deadline = deadline?.takeIf { it.isNotBlank() },
                    eventId = eventId
                )
            )
            if (result.isSuccess) {
                refresh()
                onResult(true)
            } else {
                _uiState.update { it.copy(formError = result.exceptionOrNull()?.message) }
                onResult(false)
            }
        }
    }

    fun updateTask(
        taskId: String,
        title: String,
        description: String?,
        zoneId: String?,
        priority: String?,
        deadline: String?,
        performerIds: List<String>,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(formError = null) }
            val result = repository.updateTask(
                taskId,
                TaskUpdateRequest(
                    title = title,
                    description = description,
                    taskPriority = priority ?: "MEDIUM",
                    zoneId = zoneId,
                    performers = performerIds,
                    deadline = deadline?.takeIf { it.isNotBlank() }
                )
            )
            if (result.isSuccess) {
                refresh()
                onResult(true)
            } else {
                _uiState.update { it.copy(formError = result.exceptionOrNull()?.message) }
                onResult(false)
            }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(formError = null) }
            val result = repository.deleteTask(taskId)
            if (result.isSuccess) refresh()
            else _uiState.update { it.copy(formError = result.exceptionOrNull()?.message) }
        }
    }

    fun updateTaskStatus(taskId: String, newStatus: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(formError = null) }
            val result = repository.updateTaskStatus(taskId, newStatus)
            if (result.isSuccess) refresh()
            else _uiState.update { it.copy(formError = result.exceptionOrNull()?.message) }
        }
    }

    fun updateEventStatus(status: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(formError = null) }
            val result = repository.patchEventStatus(eventId, status)
            if (result.isSuccess) {
                val ev = result.getOrNull()
                if (ev != null) {
                    _uiState.update { it.copy(event = ev) }
                }
                refresh()
            } else {
                _uiState.update { it.copy(formError = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun openTaskReport() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    reportVisible = true,
                    reportLoading = true,
                    taskReport = null,
                    reportError = null
                )
            }
            val result = repository.loadEventTaskReport(eventId)
            _uiState.update {
                it.copy(
                    reportLoading = false,
                    taskReport = result.getOrNull(),
                    reportError = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun dismissTaskReport() {
        _uiState.update {
            it.copy(
                reportVisible = false,
                reportLoading = false,
                taskReport = null,
                reportError = null
            )
        }
    }

    fun declineSelfFromTask(taskId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(formError = null) }
            val result = repository.declineTaskSelf(taskId)
            if (result.isSuccess) refresh()
            else _uiState.update { it.copy(formError = result.exceptionOrNull()?.message) }
        }
    }

    fun joinTaskAsPerformer(taskId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(formError = null) }
            val result = repository.joinTaskAsPerformer(taskId)
            if (result.isSuccess) refresh()
            else _uiState.update { it.copy(formError = result.exceptionOrNull()?.message) }
        }
    }

    fun setZoneMuted(zoneId: String, mute: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(formError = null) }
            val next = _uiState.value.mutedZoneIds.toMutableSet()
            if (mute) next.add(zoneId) else next.remove(zoneId)
            val result = repository.replaceMutedZoneIds(next.toList())
            if (result.isSuccess) {
                _uiState.update { it.copy(mutedZoneIds = next) }
            } else {
                _uiState.update { it.copy(formError = result.exceptionOrNull()?.message) }
            }
        }
    }
}
