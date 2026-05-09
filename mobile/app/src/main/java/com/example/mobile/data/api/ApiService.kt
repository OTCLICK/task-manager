package com.example.mobile.data.api

import com.example.mobile.data.model.AuthRequest
import com.example.mobile.data.model.AuthResponse
import com.example.mobile.data.model.Event
import com.example.mobile.data.model.EventApiModel
import com.example.mobile.data.model.EventCreateRequest
import com.example.mobile.data.model.InvitationApiModel
import com.example.mobile.data.model.Task
import com.example.mobile.data.model.TaskCreateRequest
import com.example.mobile.data.model.User
import com.example.mobile.data.model.UserCreateRequest
import com.example.mobile.data.model.Zone
import com.example.mobile.data.model.ZoneCreateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: UserCreateRequest): Response<AuthResponse>

    @GET("events")
    suspend fun getAllEvents(): Response<List<EventApiModel>>

    @GET("events/{id}")
    suspend fun getEventById(@Path("id") id: String): Response<EventApiModel>

    @GET("events/{eventId}/participants/invitations/my")
    suspend fun getMyInvitationsByEvent(@Path("eventId") eventId: String): Response<List<InvitationApiModel>>

    @POST("events/{eventId}/participants/invitations/{invitationId}/accept")
    suspend fun acceptInvitation(
        @Path("eventId") eventId: String,
        @Path("invitationId") invitationId: String
    ): Response<Unit>

    @POST("events/{eventId}/participants/invitations/{invitationId}/decline")
    suspend fun declineInvitation(
        @Path("eventId") eventId: String,
        @Path("invitationId") invitationId: String
    ): Response<Unit>

    @POST("events")
    suspend fun createEvent(@Body request: EventCreateRequest): Response<Event>

    @DELETE("events/{id}")
    suspend fun deleteEvent(@Path("id") id: String): Response<Unit>

    @GET("zones")
    suspend fun getZones(@Query("eventId") eventId: String?): Response<List<Zone>>

    @GET("zones/{id}")
    suspend fun getZoneById(@Path("id") id: String): Response<Zone>

    @POST("zones")
    suspend fun createZone(@Body request: ZoneCreateRequest): Response<Zone>

    @DELETE("zones/{id}")
    suspend fun deleteZone(@Path("id") id: String): Response<Unit>

    @GET("tasks")
    suspend fun getTasks(@Query("eventId") eventId: String?): Response<List<Task>>

    @GET("tasks/{id}")
    suspend fun getTaskById(@Path("id") id: String): Response<Task>

    @POST("tasks")
    suspend fun createTask(@Body request: TaskCreateRequest): Response<Task>

    @DELETE("tasks/{id}")
    suspend fun deleteTask(@Path("id") id: String): Response<Unit>

    @GET("users")
    suspend fun getUsers(): Response<List<User>>

    @GET("users/search")
    suspend fun searchUsers(@Query("q") query: String): Response<List<User>>

//    @GET("users/role/{role}")
//    suspend fun getUsersByRole(@Path("role") role: String): Response<List<User>>

}