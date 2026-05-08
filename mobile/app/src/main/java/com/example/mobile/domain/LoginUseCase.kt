package com.example.mobile.domain

import com.example.mobile.data.model.AuthResponse
import com.example.mobile.data.model.FullName
import com.example.mobile.data.repository.AuthRepository
import com.example.mobile.utils.Resource

class LoginUseCase(private val authRepository: AuthRepository) {
    suspend fun login(email: String, password: String): Resource<AuthResponse> {
        return authRepository.login(email, password)
    }

    suspend fun register(
        email: String,
        password: String,
        fullName: FullName,
//        role: String
    ): Resource<AuthResponse> {
        return authRepository.register(email, password, fullName/*, role*/)
    }

    suspend fun logout() {
        authRepository.logout()
    }
}