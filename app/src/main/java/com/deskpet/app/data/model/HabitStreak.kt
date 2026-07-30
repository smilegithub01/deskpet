package com.deskpet.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class HabitType(val displayName: String, val emoji: String) {
    DRINK("喝水", "💧"),
    SIT("久坐起身", "🪑"),
    EYE("护眼", "👁️")
}

@Entity(tableName = "habit_streaks")
data class HabitStreak(
    @PrimaryKey val habitType: String,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastCheckDate: String = ""
)
