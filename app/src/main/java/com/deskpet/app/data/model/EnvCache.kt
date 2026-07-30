package com.deskpet.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "env_cache")
data class EnvCache(
    @PrimaryKey val key: String,
    val value: String,
    val updatedAt: Long
)
