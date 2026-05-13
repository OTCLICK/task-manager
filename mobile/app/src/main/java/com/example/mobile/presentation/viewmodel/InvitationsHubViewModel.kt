package com.example.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile.data.repository.InvitationRepository
import com.example.mobile.presentation.model.InvitationListItem
import com.example.mobile.presentation.model.SentInvitationListItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InvitationsHubUiState(
    val selectedTab: Int = 0,
    val incoming: List<InvitationListItem> = emptyList(),
    val sent: List<SentInvitationListItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class InvitationsHubViewModel(
    private val repository: InvitationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvitationsHubUiState())
    val uiState: StateFlow<InvitationsHubUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index.coerceIn(0, 1)) }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val incomingResult = repository.getIncomingPendingGlobal()
            val sentResult = repository.getSentInvitations()
            val err = buildString {
                incomingResult.exceptionOrNull()?.message?.let { append(it) }
                sentResult.exceptionOrNull()?.message?.let {
                    if (isNotEmpty()) append("\n")
                    append(it)
                }
            }.trim().ifBlank { null }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    incoming = incomingResult.getOrElse { emptyList() },
                    sent = sentResult.getOrElse { emptyList() },
                    errorMessage = err
                )
            }
        }
    }

    fun acceptInvitation(eventId: String, invitationId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.acceptInvitation(eventId, invitationId)
            if (result.isSuccess) refresh()
            else _uiState.update {
                it.copy(isLoading = false, errorMessage = result.exceptionOrNull()?.message)
            }
        }
    }

    fun declineInvitation(eventId: String, invitationId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.declineInvitation(eventId, invitationId)
            if (result.isSuccess) refresh()
            else _uiState.update {
                it.copy(isLoading = false, errorMessage = result.exceptionOrNull()?.message)
            }
        }
    }

    fun withdrawSentInvitation(invitationId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.withdrawSentInvitation(invitationId)
            if (result.isSuccess) refresh()
            else _uiState.update {
                it.copy(isLoading = false, errorMessage = result.exceptionOrNull()?.message)
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
