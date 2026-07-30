package com.deskpet.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.deskpet.app.data.model.RoomLayout
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomLayoutDao {

    @Query("SELECT * FROM room_layout ORDER BY slotIndex ASC")
    fun getAll(): Flow<List<RoomLayout>>

    @Query("SELECT * FROM room_layout ORDER BY slotIndex ASC")
    suspend fun getAllOnce(): List<RoomLayout>

    @Query("SELECT * FROM room_layout WHERE slotIndex = :slotIndex LIMIT 1")
    suspend fun getBySlot(slotIndex: Int): RoomLayout?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(layout: RoomLayout)

    @Query("DELETE FROM room_layout WHERE slotIndex = :slotIndex")
    suspend fun removeSlot(slotIndex: Int)

    @Query("DELETE FROM room_layout")
    suspend fun clearAll()
}
