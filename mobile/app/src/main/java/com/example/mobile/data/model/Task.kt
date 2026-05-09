package com.example.mobile.data.model

import com.squareup.moshi.Json

data class Task(
    @Json(name = "taskId")
    val id: String,
    val title: String,
    val description: String? = null,
    @Json(name = "taskPriority")
    val priority: String,
    @Json(name = "taskStatus")
    val status: String,
    val zone: Zone?,
    val performers: List<User>,
    val coordinator: User,
    val deadline: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val completedAt: String?
)