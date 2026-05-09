package com.example.mobile.data.model

import com.squareup.moshi.Json

data class Zone(
    @Json(name = "zoneId")
    val id: String,
    val name: String,
    val description: String? = null,
    val participatesCount: Int,
    val event: Event,
    @Json(name = "user")
    val coordinator: User
)