package com.deskpet.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.deskpet.app.data.model.InteractionLog

@Dao
interface InteractionLogDao {
    @Insert
    suspend fun insert(log: InteractionLog): Long

    @Query("SELECT * FROM interaction_logs WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp ASC")
    suspend fun getByDateRange(start: Long, end: Long): List<InteractionLog>

    @Query("SELECT * FROM interaction_logs WHERE timestamp >= :since ORDER BY timestamp DESC")
    suspend fun getRecent(since: Long): List<InteractionLog>

    @Query("SELECT COUNT(*) FROM interaction_logs WHERE type = :type AND timestamp BETWEEN :start AND :end")
    suspend fun countByTypeAndDateRange(type: String, start: Long, end: Long): Int

    @Query("SELECT COUNT(DISTINCT strftime('%Y-%m-%d', timestamp / 1000, 'unixepoch')) FROM interaction_logs WHERE timestamp >= :since")
    suspend fun getDistinctDaysSince(since: Long): Int

    @Query("DELETE FROM interaction_logs WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long)
}
