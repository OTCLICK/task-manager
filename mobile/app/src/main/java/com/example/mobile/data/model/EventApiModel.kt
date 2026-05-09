package com.example.mobile.data.model

import com.squareup.moshi.Json

data class EventApiModel(
    @Json(name = "eventId")
    val eventId: String,
    val name: String,
    val address: String,
    val participatesCount: Int,
    val status: String,
    val startTime: String?,
    val endTime: String?
)
