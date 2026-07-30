package com.deskpet.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.deskpet.app.data.model.PetEntity

@Dao
interface PetDao {

    @Query("SELECT * FROM pet_state WHERE id = 1 LIMIT 1")
    suspend fun getPet(): PetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(pet: PetEntity)

    @Query("SELECT lastInteractionTime FROM pet_state WHERE id = 1 LIMIT 1")
    suspend fun getLastInteractionTime(): Long?
}
