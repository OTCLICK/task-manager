package com.example.mobile.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WorkspaceCacheDao {
    @Query("SELECT * FROM cached_workspace WHERE eventId = :eventId LIMIT 1")
    suspend fun getByEventId(eventId: String): CachedWorkspaceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(row: CachedWorkspaceEntity)
}
