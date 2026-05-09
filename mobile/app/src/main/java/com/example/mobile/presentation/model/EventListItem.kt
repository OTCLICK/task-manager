package com.example.mobile.presentation.model

data class EventListItem(
    val id: String,
    val name: String,
    val address: String,
    val participantsCount: Int,
    val status: String,
    val startTime: String?,
    val endTime: String?
)
