package com.example.mobile.data.local

import com.example.mobile.data.model.EventApiModel
import com.example.mobile.data.model.ParticipantApiModel
import com.example.mobile.data.model.Task
import com.example.mobile.data.model.Zone
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

internal object WorkspaceCacheJson {
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val eventAdapter = moshi.adapter(EventApiModel::class.java)
    private val zoneListType = Types.newParameterizedType(List::class.java, Zone::class.java)
    private val zoneListAdapter = moshi.adapter<List<Zone>>(zoneListType)
    private val taskListType = Types.newParameterizedType(List::class.java, Task::class.java)
    private val taskListAdapter = moshi.adapter<List<Task>>(taskListType)
    private val participantListType =
        Types.newParameterizedType(List::class.java, ParticipantApiModel::class.java)
    private val participantListAdapter = moshi.adapter<List<ParticipantApiModel>>(participantListType)

    fun eventToJson(event: EventApiModel): String = eventAdapter.toJson(event)

    fun eventFromJson(json: String): EventApiModel? =
        runCatching { eventAdapter.fromJson(json) }.getOrNull()

    fun zonesToJson(zones: List<Zone>): String = zoneListAdapter.toJson(zones)

    fun zonesFromJson(json: String): List<Zone>? =
        runCatching { zoneListAdapter.fromJson(json) }.getOrNull()

    fun tasksToJson(tasks: List<Task>): String = taskListAdapter.toJson(tasks)

    fun tasksFromJson(json: String): List<Task>? =
        runCatching { taskListAdapter.fromJson(json) }.getOrNull()

    fun participantsToJson(participants: List<ParticipantApiModel>): String =
        participantListAdapter.toJson(participants)

    fun participantsFromJson(json: String): List<ParticipantApiModel>? =
        runCatching { participantListAdapter.fromJson(json) }.getOrNull()
}
