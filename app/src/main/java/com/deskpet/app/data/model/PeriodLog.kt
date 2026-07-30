package com.deskpet.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A persisted period-tracking record.
 */
@Entity(tableName = "period_logs")
data class PeriodLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val isPeriodStart: Boolean = false
)
