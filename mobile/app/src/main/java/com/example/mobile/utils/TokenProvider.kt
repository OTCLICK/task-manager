package com.example.mobile.utils

import kotlinx.coroutines.flow.first

class TokenProvider(private val tokenManager: TokenManager) {
    suspend fun getToken(): String? {
        return tokenManager.tokenFlow.first()
    }

    fun provideToken(): (() -> String?) {
        return {
            //TODO: улучшить!!!! еще suspend функция
            null
        }
    }
}