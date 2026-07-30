package com.deskpet.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.deskpet.app.data.model.PetDiary
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDiaryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(diary: PetDiary): Long

    @Query("SELECT * FROM pet_diaries ORDER BY date DESC")
    fun getAll(): Flow<List<PetDiary>>

    @Query("SELECT * FROM pet_diaries WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): PetDiary?

    @Query("SELECT COUNT(*) FROM pet_diaries")
    suspend fun count(): Int
}
