package com.example.mobile.data.model

data class Zone(
    val id: String,
    val name: String,
    val description: String,
    val participantsCount: Int,
    val event: Event,
    val coordinator: User
)