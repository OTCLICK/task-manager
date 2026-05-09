package com.example.mobile.data.model

data class SentInvitationApiModel(
    val invitationId: String,
    val eventId: String,
    val eventName: String,
    val invitedUserId: String,
    val invitedUserEmail: String,
    val role: String,
    val status: String,
    val createdAt: String?
)
