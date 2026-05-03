package com.example.mobile.data.model

data class Event(
    val id: String,
    val name: String,
    val address: String,
    val participatesCount: Int,
    val status: String,
    val startTime: String,
    val endTime: String,
    val organizer: User
)