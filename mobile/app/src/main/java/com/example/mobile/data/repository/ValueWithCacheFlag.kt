package com.example.mobile.data.repository

data class ValueWithCacheFlag<T>(
    val value: T,
    val fromCache: Boolean
)
