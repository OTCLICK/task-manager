package com.example.mobile.presentation.model

data class SentInvitationListItem(
    val invitationId: String,
    val eventId: String,
    val eventName: String,
    val invitedUserEmail: String,
    val role: String,
    val status: String
)
