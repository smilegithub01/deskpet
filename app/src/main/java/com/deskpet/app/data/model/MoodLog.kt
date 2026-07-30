package com.deskpet.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A persisted mood record (one entry per recorded mood event).
 */
@Entity(tableName = "mood_logs")
data class MoodLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long, // timestamp at start of day
    val mood: MoodLevel,
    val note: String = ""
)
