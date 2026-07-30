package com.deskpet.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Companion pet link for co-raising feature (L3-2).
 *
 * This entity stores the local pairing information. In the full implementation,
 * pairing data will be synced via Huawei Cloud AGC Cloud DB. For now, this
 * serves as a local stub that can be expanded when cloud services are integrated.
 */
@Entity(tableName = "companion_links")
data class CompanionLink(
    @PrimaryKey val pairCode: String,
    val ownerId: String,
    val partnerName: String = "",
    val petName: String,
    val petSpecies: String,
    val petColor: String,
    val petLevel: Int,
    val petMood: Int,
    val lastUpdate: Long = System.currentTimeMillis(),
    val isActive: Boolean = false
)
