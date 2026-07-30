package com.deskpet.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.deskpet.app.data.model.HabitStreak
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitStreakDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(streak: HabitStreak)

    @Query("SELECT * FROM habit_streaks")
    fun getAll(): Flow<List<HabitStreak>>

    @Query("SELECT * FROM habit_streaks WHERE habitType = :type LIMIT 1")
    suspend fun getByType(type: String): HabitStreak?

    @Query("DELETE FROM habit_streaks")
    suspend fun deleteAll()
}
