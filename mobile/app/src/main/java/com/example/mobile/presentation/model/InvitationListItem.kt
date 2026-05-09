package com.example.mobile.presentation.model

data class InvitationListItem(
    val invitationId: String,
    val eventId: String,
    val eventName: String,
    val invitedByEmail: String,
    val role: String,
    val status: String
)
