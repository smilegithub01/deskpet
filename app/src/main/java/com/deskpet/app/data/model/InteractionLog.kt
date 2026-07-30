package com.deskpet.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class InteractionType {
    PET, FEED, MOOD_SELECTED, PHOTO, OPEN_APP, CLOSE_APP, CHECKIN, SHARE
}

@Entity(tableName = "interaction_logs")
data class InteractionLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val timestamp: Long,
    val detail: String = ""
)
