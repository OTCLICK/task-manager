package com.example.mobile.data.model

data class ZoneCreateRequest(
    val name: String,
    val description: String? = null,
    val participatesCount: Int? = null,
    val eventId: String
)