package com.example.mobile.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_workspace")
data class CachedWorkspaceEntity(
    @PrimaryKey val eventId: String,
    val eventJson: String?,
    val zonesJson: String?,
    val tasksJson: String?,
    val participantsJson: String?,
    val cachedAtMillis: Long
)
