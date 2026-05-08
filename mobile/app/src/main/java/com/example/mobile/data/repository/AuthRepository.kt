package com.example.mobile.data.repository

import com.example.mobile.data.api.ApiClient
import com.example.mobile.data.model.*
import com.example.mobile.utils.Resource
import com.example.mobile.utils.TokenManager

class AuthRepository(
    private val tokenManager: TokenManager
) {

    private val apiService = ApiClient.createApiService()

    suspend fun login(email: String, password: String): Resource<AuthResponse> {
        return try {
            val request = AuthRequest(email = email, password = password)
            val response = apiService.login(request)

            if (response.isSuccessful && response.body() != null) {
                tokenManager.saveToken(response.body()!!.token)
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Ошибка входа: ${response.message()}")
            }
        } catch (e: Throwable) {
            Resource.Error("Нет подключения к серверу", e)
        }
    }

    suspend fun register(
        email: String,
        password: String,
        fullName: FullName,
//        role: String
    ): Resource<AuthResponse> {
        return try {
            val request = UserCreateRequest(
                email = email,
                password = password,
                fullName = fullName
//                role = role
            )
            val response = apiService.register(request)

            if (response.isSuccessful && response.body() != null) {
                tokenManager.saveToken(response.body()!!.token)
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Ошибка регистрации: ${response.message()}")
            }
        } catch (e: Throwable) {
            Resource.Error("Нет подключения к серверу", e)
        }
    }

    suspend fun logout() {
        tokenManager.clearToken()
    }
}