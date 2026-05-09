package com.example.mobile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CachedEventEntity::class, CachedWorkspaceEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun workspaceCacheDao(): WorkspaceCacheDao
}
