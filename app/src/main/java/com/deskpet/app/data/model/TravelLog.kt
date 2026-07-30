package com.deskpet.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "travel_logs")
data class TravelLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val destinationId: String,
    val destinationName: String,
    val departTime: Long,
    val returnTime: Long,
    val postcardsReceived: Int = 0,
    val giftsReceived: String = "",
    val completed: Boolean = false,
    val completedAt: Long? = null
)
