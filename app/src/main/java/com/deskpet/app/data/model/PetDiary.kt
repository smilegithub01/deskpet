package com.deskpet.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pet_diaries")
data class PetDiary(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val content: String,
    val moodSnapshot: String,
    val petEmoji: String = "🐱"
)
