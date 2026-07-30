package com.deskpet.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.deskpet.app.data.model.PeriodLog
import kotlinx.coroutines.flow.Flow

/**
 * Data access object for [PeriodLog] records.
 */
@Dao
interface PeriodLogDao {

    @Insert
    suspend fun insert(periodLog: PeriodLog): Long

    @Query("SELECT * FROM period_logs ORDER BY date DESC")
    fun getAll(): Flow<List<PeriodLog>>

    @Query("SELECT * FROM period_logs WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: Long): PeriodLog?

    @Query("DELETE FROM period_logs WHERE date = :date")
    suspend fun deleteByDate(date: Long)

    @Query("DELETE FROM period_logs")
    suspend fun deleteAll()
}
