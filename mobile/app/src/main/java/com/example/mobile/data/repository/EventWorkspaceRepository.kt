package com.example.mobile.data.repository

import com.example.mobile.data.api.ApiClient
import com.example.mobile.data.local.CachedWorkspaceEntity
import com.example.mobile.data.local.WorkspaceCacheDao
import com.example.mobile.data.local.WorkspaceCacheJson
import com.example.mobile.data.model.EventApiModel
import com.example.mobile.data.model.InviteParticipantRequest
import com.example.mobile.data.model.ParticipantApiModel
import com.example.mobile.data.model.Task
import com.example.mobile.data.model.TaskCreateRequest
import com.example.mobile.data.model.TaskStatusPatchRequest
import com.example.mobile.data.model.User
import com.example.mobile.data.model.Zone
import com.example.mobile.data.model.ZoneCreateRequest
import com.example.mobile.utils.TokenManager
import com.example.mobile.utils.toUserFacingHttpError
import kotlinx.coroutines.flow.firstOrNull

class EventWorkspaceRepository(
    private val tokenManager: TokenManager,
    private val workspaceCacheDao: WorkspaceCacheDao
) {

    private suspend fun authorizedApi() = ApiClient.createAuthorizedApiService(
        tokenManager.tokenFlow.firstOrNull()
            ?: throw IllegalStateException("Не найден токен авторизации")
    )

    private suspend fun patchRow(
        eventId: String,
        block: (CachedWorkspaceEntity) -> CachedWorkspaceEntity
    ) {
        val prev = workspaceCacheDao.getByEventId(eventId)
        val base = prev ?: CachedWorkspaceEntity(
            eventId = eventId,
            eventJson = null,
            zonesJson = null,
            tasksJson = null,
            participantsJson = null,
            cachedAtMillis = 0L
        )
        workspaceCacheDao.upsert(block(base).copy(cachedAtMillis = System.currentTimeMillis()))
    }

    private suspend fun persistEvent(eventId: String, event: EventApiModel) {
        patchRow(eventId) { it.copy(eventJson = WorkspaceCacheJson.eventToJson(event)) }
    }

    private suspend fun persistZones(eventId: String, zones: List<Zone>) {
        patchRow(eventId) { it.copy(zonesJson = WorkspaceCacheJson.zonesToJson(zones)) }
    }

    private suspend fun persistTasks(eventId: String, tasks: List<Task>) {
        patchRow(eventId) { it.copy(tasksJson = WorkspaceCacheJson.tasksToJson(tasks)) }
    }

    private suspend fun persistParticipants(eventId: String, participants: List<ParticipantApiModel>) {
        patchRow(eventId) {
            it.copy(participantsJson = WorkspaceCacheJson.participantsToJson(participants))
        }
    }

    suspend fun loadEvent(eventId: String): Result<ValueWithCacheFlag<EventApiModel>> {
        return try {
            val response = authorizedApi().getEventById(eventId)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                persistEvent(eventId, body)
                Result.success(ValueWithCacheFlag(body, fromCache = false))
            } else {
                loadEventFromCache(eventId)
                    ?: Result.failure(IllegalStateException(response.toUserFacingHttpError()))
            }
        } catch (e: Throwable) {
            loadEventFromCache(eventId) ?: Result.failure(e)
        }
    }

    private suspend fun loadEventFromCache(eventId: String): Result<ValueWithCacheFlag<EventApiModel>>? {
        val json = workspaceCacheDao.getByEventId(eventId)?.eventJson ?: return null
        val parsed = WorkspaceCacheJson.eventFromJson(json) ?: return null
        return Result.success(ValueWithCacheFlag(parsed, fromCache = true))
    }

    suspend fun loadZones(eventId: String): Result<ValueWithCacheFlag<List<Zone>>> {
        return try {
            val response = authorizedApi().getZones(eventId)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                persistZones(eventId, body)
                Result.success(ValueWithCacheFlag(body, fromCache = false))
            } else {
                loadZonesFromCache(eventId)
                    ?: Result.failure(IllegalStateException(response.toUserFacingHttpError()))
            }
        } catch (e: Throwable) {
            loadZonesFromCache(eventId) ?: Result.failure(e)
        }
    }

    private suspend fun loadZonesFromCache(eventId: String): Result<ValueWithCacheFlag<List<Zone>>>? {
        val json = workspaceCacheDao.getByEventId(eventId)?.zonesJson ?: return null
        val parsed = WorkspaceCacheJson.zonesFromJson(json) ?: return null
        return Result.success(ValueWithCacheFlag(parsed, fromCache = true))
    }

    suspend fun loadTasks(eventId: String): Result<ValueWithCacheFlag<List<Task>>> {
        return try {
            val response = authorizedApi().getTasks(eventId)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                persistTasks(eventId, body)
                Result.success(ValueWithCacheFlag(body, fromCache = false))
            } else {
                loadTasksFromCache(eventId)
                    ?: Result.failure(IllegalStateException(response.toUserFacingHttpError()))
            }
        } catch (e: Throwable) {
            loadTasksFromCache(eventId) ?: Result.failure(e)
        }
    }

    private suspend fun loadTasksFromCache(eventId: String): Result<ValueWithCacheFlag<List<Task>>>? {
        val json = workspaceCacheDao.getByEventId(eventId)?.tasksJson ?: return null
        val parsed = WorkspaceCacheJson.tasksFromJson(json) ?: return null
        return Result.success(ValueWithCacheFlag(parsed, fromCache = true))
    }

    suspend fun changeParticipantRole(
        eventId: String,
        participantUserId: String,
        newRole: String
    ): Result<Unit> {
        return try {
            val response = authorizedApi().changeParticipantRole(eventId, participantUserId, newRole)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(IllegalStateException(response.toUserFacingHttpError()))
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun removeParticipant(eventId: String, participantUserId: String): Result<Unit> {
        return try {
            val response = authorizedApi().removeParticipant(eventId, participantUserId)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(IllegalStateException(response.toUserFacingHttpError()))
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun loadParticipants(eventId: String): Result<ValueWithCacheFlag<List<ParticipantApiModel>>> {
        return try {
            val response = authorizedApi().getParticipants(eventId)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                persistParticipants(eventId, body)
                Result.success(ValueWithCacheFlag(body, fromCache = false))
            } else {
                loadParticipantsFromCache(eventId)
                    ?: Result.failure(IllegalStateException(response.toUserFacingHttpError()))
            }
        } catch (e: Throwable) {
            loadParticipantsFromCache(eventId) ?: Result.failure(e)
        }
    }

    private suspend fun loadParticipantsFromCache(eventId: String): Result<ValueWithCacheFlag<List<ParticipantApiModel>>>? {
        val json = workspaceCacheDao.getByEventId(eventId)?.participantsJson ?: return null
        val parsed = WorkspaceCacheJson.participantsFromJson(json) ?: return null
        return Result.success(ValueWithCacheFlag(parsed, fromCache = true))
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

    suspend fun updateTaskStatus(taskId: String, taskStatus: String): Result<Unit> {
        return try {
            val response = authorizedApi().updateTaskStatus(
                taskId,
                TaskStatusPatchRequest(taskStatus = taskStatus)
            )
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(IllegalStateException(response.toUserFacingHttpError()))
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(): Result<User> {
        return try {
            val response = authorizedApi().getCurrentUser()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(IllegalStateException(response.toUserFacingHttpError()))
            }
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun searchUsers(query: String): Result<List<User>> {
        val q = query.trim()
        if (q.length < 2) return Result.success(emptyList())
        return try {
            val response = authorizedApi().searchUsers(q)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(IllegalStateException(response.toUserFacingHttpError()))
            }
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun inviteParticipant(eventId: String, email: String, role: String): Result<Unit> {
        return try {
            val response = authorizedApi().inviteParticipant(
                eventId,
                InviteParticipantRequest(email = email.trim(), role = role)
            )
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(IllegalStateException(response.toUserFacingHttpError()))
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    /** Email адресатов с исходящими приглашениями в ожидании (для скрытия в поиске на экране приглашения). */
    suspend fun getPendingOutboundInviteEmails(eventId: String): Result<Set<String>> {
        return try {
            val response = authorizedApi().getPendingOutboundInvitations(eventId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.map { it.invitedUserEmail.lowercase() }.toSet())
            } else {
                Result.failure(IllegalStateException(response.toUserFacingHttpError()))
            }
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}
