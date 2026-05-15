package com.example.mobile.data.model

data class TaskUpdateRequest(
    val title: String,
    val description: String? = null,
    val taskPriority: String? = null,
    val zoneId: String? = null,
    val performers: List<String> = emptyList(),
    val deadline: String? = null
)
