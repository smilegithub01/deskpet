package com.deskpet.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "postcards")
data class Postcard(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val destinationId: String,
    val destinationName: String,
    val destinationEmoji: String,
    val date: String,
    val message: String,
    val sceneDrawKey: String,
    val petEmoji: String,
    val collected: Boolean = true
)
