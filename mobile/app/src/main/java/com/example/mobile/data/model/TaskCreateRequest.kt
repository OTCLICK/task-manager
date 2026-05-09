package com.example.mobile.data.model

data class TaskCreateRequest(
    val title: String,
    val description: String? = null,
    val taskPriority: String? = null,
    val zoneId: String? = null,
    val performers: List<String>? = null,
    val deadline: String? = null,
    val eventId: String
)