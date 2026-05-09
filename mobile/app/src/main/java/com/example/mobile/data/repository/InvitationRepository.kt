package com.example.mobile.data.repository

import com.example.mobile.data.api.ApiClient
import com.example.mobile.presentation.model.InvitationListItem
import com.example.mobile.utils.TokenManager
import kotlinx.coroutines.flow.firstOrNull

class InvitationRepository(
    private val tokenManager: TokenManager
) {
    suspend fun getMyInvitationsByEvent(eventId: String): Result<List<InvitationListItem>> {
        val token = tokenManager.tokenFlow.firstOrNull()
            ?: return Result.failure(IllegalStateException("Не найден токен авторизации"))

        return try {
            val api = ApiClient.createAuthorizedApiService(token)
            val response = api.getMyInvitationsByEvent(eventId)
            if (!response.isSuccessful || response.body() == null) {
                return Result.failure(
                    IllegalStateException("Не удалось загрузить приглашения: ${response.message()}")
                )
            }

            Result.success(
                response.body()!!.map {
                    InvitationListItem(
                        invitationId = it.invitationId,
                        eventId = it.eventId,
                        eventName = it.eventName,
                        invitedByEmail = it.invitedByEmail,
                        role = it.role,
                        status = it.status
                    )
                }
            )
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun acceptInvitation(eventId: String, invitationId: String): Result<Unit> {
        val token = tokenManager.tokenFlow.firstOrNull()
            ?: return Result.failure(IllegalStateException("Не найден токен авторизации"))

        return try {
            val api = ApiClient.createAuthorizedApiService(token)
            val response = api.acceptInvitation(eventId, invitationId)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(IllegalStateException("Не удалось принять приглашение: ${response.message()}"))
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun declineInvitation(eventId: String, invitationId: String): Result<Unit> {
        val token = tokenManager.tokenFlow.firstOrNull()
            ?: return Result.failure(IllegalStateException("Не найден токен авторизации"))

        return try {
            val api = ApiClient.createAuthorizedApiService(token)
            val response = api.declineInvitation(eventId, invitationId)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(IllegalStateException("Не удалось отклонить приглашение: ${response.message()}"))
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}
