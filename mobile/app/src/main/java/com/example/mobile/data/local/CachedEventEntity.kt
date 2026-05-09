package com.example.mobile.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_events")
data class CachedEventEntity(
    @PrimaryKey val id: String,
    val name: String,
    val address: String,
    val participatesCount: Int,
    val status: String,
    val startTime: String?,
    val endTime: String?
)
