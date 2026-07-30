package com.deskpet.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.deskpet.app.data.model.AchievementRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(record: AchievementRecord)

    @Query("SELECT * FROM achievement_records")
    fun getAll(): Flow<List<AchievementRecord>>

    @Query("SELECT achievementId FROM achievement_records")
    suspend fun getAllIds(): List<String>

    @Query("SELECT COUNT(*) FROM achievement_records")
    suspend fun count(): Int
}
