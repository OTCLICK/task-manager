package com.example.mobile.data.model

data class UserCreateRequest(
    val email: String,
    val password: String,
    val fullName: FullName,
    val role: String
)