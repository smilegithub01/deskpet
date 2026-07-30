package com.deskpet.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.deskpet.app.data.model.Postcard
import kotlinx.coroutines.flow.Flow

@Dao
interface PostcardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(postcard: Postcard): Long

    @Query("SELECT * FROM postcards ORDER BY date DESC")
    fun getAll(): Flow<List<Postcard>>

    @Query("SELECT * FROM postcards ORDER BY date DESC LIMIT :limit")
    fun getRecent(limit: Int = 30): Flow<List<Postcard>>

    @Query("SELECT COUNT(*) FROM postcards")
    suspend fun count(): Int

    @Query("SELECT COUNT(DISTINCT destinationId) FROM postcards")
    suspend fun getUniqueDestinations(): Int
}
