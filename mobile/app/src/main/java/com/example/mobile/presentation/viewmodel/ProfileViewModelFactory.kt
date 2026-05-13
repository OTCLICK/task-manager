package com.example.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mobile.data.repository.ProfileRepository

class ProfileViewModelFactory(
    private val repository: ProfileRepository,
    /** Если задан — загружается профиль этого пользователя (чужой), иначе текущий аккаунт. */
    private val viewedUserId: String? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(repository, viewedUserId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
