package com.example.mobile.data.repository

import com.example.mobile.data.api.ApiClient
import com.example.mobile.data.model.User
import com.example.mobile.utils.TokenManager
import com.example.mobile.utils.toUserFacingHttpError
import kotlinx.coroutines.flow.firstOrNull

class ProfileRepository(
    private val tokenManager: TokenManager
) {
    suspend fun getCurrentUser(): Result<User> {
        val token = tokenManager.tokenFlow.firstOrNull()
            ?: return Result.failure(IllegalStateException("Не найден токен авторизации"))
        return try {
            val api = ApiClient.createAuthorizedApiService(token)
            val response = api.getCurrentUser()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(IllegalStateException(response.toUserFacingHttpError()))
            }
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}
