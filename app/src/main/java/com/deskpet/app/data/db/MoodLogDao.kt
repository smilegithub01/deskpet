package com.deskpet.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.deskpet.app.data.model.MoodLog
import kotlinx.coroutines.flow.Flow

/**
 * Data access object for [MoodLog] records.
 */
@Dao
interface MoodLogDao {

    @Insert
    suspend fun insert(moodLog: MoodLog): Long

    @Query("SELECT * FROM mood_logs ORDER BY date DESC")
    fun getAll(): Flow<List<MoodLog>>

    @Query("SELECT * FROM mood_logs ORDER BY date DESC LIMIT :limit")
    fun getRecent(limit: Int = 7): Flow<List<MoodLog>>

    @Query("SELECT * FROM mood_logs WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: Long): MoodLog?

    @Query("DELETE FROM mood_logs")
    suspend fun deleteAll()
}
