package com.example.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile.data.repository.InvitationRepository
import com.example.mobile.presentation.model.InvitationListItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InvitationUiState(
    val invitations: List<InvitationListItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class InvitationViewModel(
    private val repository: InvitationRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(InvitationUiState())
    val uiState: StateFlow<InvitationUiState> = _uiState.asStateFlow()

    fun loadInvitations(eventId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.getMyInvitationsByEvent(eventId)
            _uiState.update {
                it.copy(
                    invitations = result.getOrNull() ?: emptyList(),
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun acceptInvitation(eventId: String, invitationId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.acceptInvitation(eventId, invitationId)
            if (result.isSuccess) {
                loadInvitations(eventId)
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }

    fun declineInvitation(eventId: String, invitationId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.declineInvitation(eventId, invitationId)
            if (result.isSuccess) {
                loadInvitations(eventId)
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
