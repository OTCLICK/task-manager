package com.example.mobile.data.repository

import com.example.mobile.data.api.ApiClient
import com.example.mobile.data.model.EventApiModel
import com.example.mobile.data.model.Task
import com.example.mobile.data.model.TaskCreateRequest
import com.example.mobile.data.model.Zone
import com.example.mobile.data.model.ZoneCreateRequest
import com.example.mobile.utils.TokenManager
import com.example.mobile.utils.toUserFacingHttpError
import kotlinx.coroutines.flow.firstOrNull

class EventWorkspaceRepository(
    private val tokenManager: TokenManager
) {

    private suspend fun authorizedApi() = ApiClient.createAuthorizedApiService(
        tokenManager.tokenFlow.firstOrNull()
            ?: throw IllegalStateException("Не найден токен авторизации")
    )

    suspend fun loadEvent(eventId: String): Result<EventApiModel> {
        return try {
            val response = authorizedApi().getEventById(eventId)
            if (!response.isSuccessful || response.body() == null) {
                Result.failure(IllegalStateException(response.toUserFacingHttpError()))
            } else {
                Result.success(response.body()!!)
            }
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun loadZones(eventId: String): Result<List<Zone>> {
        return try {
            val response = authorizedApi().getZones(eventId)
            if (!response.isSuccessful || response.body() == null) {
                Result.failure(IllegalStateException(response.toUserFacingHttpError()))
            } else {
                Result.success(response.body()!!)
            }
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun loadTasks(eventId: String): Result<List<Task>> {
        return try {
            val response = authorizedApi().getTasks(eventId)
            if (!response.isSuccessful || response.body() == null) {
                Result.failure(IllegalStateException(response.toUserFacingHttpError()))
            } else {
                Result.success(response.body()!!)
            }
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun createZone(request: ZoneCreateRequest): Result<Unit> {
        return try {
            val response = authorizedApi().createZone(request)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(IllegalStateException(response.toUserFacingHttpError()))
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun createTask(request: TaskCreateRequest): Result<Unit> {
        return try {
            val response = authorizedApi().createTask(request)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(IllegalStateException(response.toUserFacingHttpError()))
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun deleteTask(taskId: String): Result<Unit> {
        return try {
            val response = authorizedApi().deleteTask(taskId)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(IllegalStateException(response.toUserFacingHttpError()))
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}
