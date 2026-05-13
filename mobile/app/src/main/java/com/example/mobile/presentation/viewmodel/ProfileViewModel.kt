package com.example.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile.data.model.User
import com.example.mobile.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    /** false — просмотр карточки другого пользователя (без выхода и приглашений). */
    val isOwnProfile: Boolean = true
)

class ProfileViewModel(
    private val repository: ProfileRepository,
    private val viewedUserId: String? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ProfileUiState(isOwnProfile = viewedUserId == null)
    )
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = if (viewedUserId == null) {
                repository.getCurrentUser()
            } else {
                repository.getUserById(viewedUserId)
            }
            _uiState.update {
                if (result.isSuccess) {
                    it.copy(isLoading = false, user = result.getOrNull(), errorMessage = null)
                } else {
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
