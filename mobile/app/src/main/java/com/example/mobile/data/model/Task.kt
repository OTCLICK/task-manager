package com.example.mobile.data.model

data class Task(
    val id: String,
    val title: String,
    val priority: String,
    val status: String,
    val zone: Zone,
    val performers: List<User>,
    val coordinator: User,
    val deadline: String,
    val createdAt: String,
    val updatedAt: String,
    val completedAt: String
)