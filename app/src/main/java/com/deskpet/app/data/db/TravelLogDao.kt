package com.deskpet.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.deskpet.app.data.model.TravelLog
import kotlinx.coroutines.flow.Flow

@Dao
interface TravelLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: TravelLog): Long

    @Query("SELECT * FROM travel_logs WHERE completed = 0 ORDER BY departTime DESC LIMIT 1")
    suspend fun getActiveTravel(): TravelLog?

    @Query("SELECT * FROM travel_logs ORDER BY departTime DESC")
    fun getAll(): Flow<List<TravelLog>>

    @Query("SELECT * FROM travel_logs WHERE id = :id")
    suspend fun getById(id: Long): TravelLog?

    @Query("UPDATE travel_logs SET completed = 1, completedAt = :completedAt, postcardsReceived = :postcards, giftsReceived = :gifts WHERE id = :id")
    suspend fun completeTravel(id: Long, completedAt: Long, postcards: Int, gifts: String)

    @Query("SELECT COUNT(*) FROM travel_logs WHERE completed = 1")
    suspend fun getCompletedCount(): Int
}
