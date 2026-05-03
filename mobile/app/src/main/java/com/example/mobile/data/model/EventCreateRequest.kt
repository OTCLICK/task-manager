package com.example.mobile.data.model

data class EventCreateRequest(
    val name: String,
    val address: String,
    val participatesCount: Int? = null,
    val status: String? = null,
    val startTime: String? = null,
    val endTime: String? = null
)