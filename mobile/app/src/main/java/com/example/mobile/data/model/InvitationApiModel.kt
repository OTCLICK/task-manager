package com.example.mobile.data.model

data class InvitationApiModel(
    val invitationId: String,
    val eventId: String,
    val eventName: String,
    val invitedByUserId: String,
    val invitedByEmail: String,
    val role: String,
    val status: String,
    val createdAt: String?
)
