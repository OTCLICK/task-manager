package com.example.mobile.data.repository

import com.example.mobile.data.api.ApiClient
import com.example.mobile.data.local.EventDao
import com.example.mobile.data.local.CachedEventEntity
import com.example.mobile.data.model.EventCreateRequest
import com.example.mobile.presentation.model.EventListItem
import com.example.mobile.utils.TokenManager
import com.example.mobile.utils.toUserFacingHttpError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull

class EventRepository(
    private val eventDao: EventDao,
    private val tokenManager: TokenManager
) {

    fun observeEvents(): Flow<List<EventListItem>> {
        return eventDao.observeAll().map { entities ->
            entities.map { entity ->
                EventListItem(
                    id = entity.id,
                    name = entity.name,
                    address = entity.address,
                    participantsCount = entity.participatesCount,
                    status = entity.status,
                    startTime = entity.startTime,
                    endTime = entity.endTime
                )
            }
        }
    }

    suspend fun refreshEvents(): Result<Unit> {
        val token = tokenManager.tokenFlow.firstOrNull()
            ?: return Result.failure(IllegalStateException("Не найден токен авторизации"))

        return try {
            val api = ApiClient.createAuthorizedApiService(token)
            val response = api.getAllEvents()

            if (!response.isSuccessful || response.body() == null) {
                return Result.failure(IllegalStateException("Не удалось загрузить мероприятия: ${response.message()}"))
            }

            val entities = response.body()!!.map { event ->
                CachedEventEntity(
                    id = event.eventId,
                    name = event.name,
                    address = event.address,
                    participatesCount = event.participatesCount,
                    status = event.status,
                    startTime = event.startTime,
                    endTime = event.endTime
                )
            }
            eventDao.replaceAll(entities)
            Result.success(Unit)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun createEvent(request: EventCreateRequest): Result<Unit> {
        val token = tokenManager.tokenFlow.firstOrNull()
            ?: return Result.failure(IllegalStateException("Не найден токен авторизации"))

        return try {
            val api = ApiClient.createAuthorizedApiService(token)
            val response = api.createEvent(request)

            if (response.isSuccessful) {
                refreshEvents()
                Result.success(Unit)
            } else {
                Result.failure(IllegalStateException(response.toUserFacingHttpError()))
            }
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}
