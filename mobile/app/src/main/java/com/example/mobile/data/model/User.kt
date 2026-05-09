package com.example.mobile.data.model

import com.squareup.moshi.Json

data class User(
    @Json(name = "userId")
    val id: String,
    val email: String,
    val fullName: FullName,
//    val role: String
)
