package com.example.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mobile.data.repository.AuthRepository
import com.example.mobile.domain.LoginUseCase
import com.example.mobile.utils.TokenManager

class AuthViewModelFactory(
    private val tokenManager: TokenManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            val authRepository = AuthRepository(tokenManager)
            val loginUseCase = LoginUseCase(authRepository)
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(loginUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}