package com.example.mobile.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM cached_events ORDER BY startTime ASC")
    fun observeAll(): Flow<List<CachedEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<CachedEventEntity>)

    @Query("DELETE FROM cached_events")
    suspend fun clearAll()

    @Transaction
    suspend fun replaceAll(events: List<CachedEventEntity>) {
        clearAll()
        if (events.isNotEmpty()) {
            insertAll(events)
        }
    }
}
