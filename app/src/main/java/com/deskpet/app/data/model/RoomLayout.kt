package com.deskpet.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persisted furniture placement in the pet's room.
 * Each slot index maps to a position in the room grid.
 * slotIndex 0 = wallpaper, 1 = floor, 2 = bed, 3 = table,
 * 4-5 = decoration slots, 6-7 = toy slots.
 */
@Entity(tableName = "room_layout")
data class RoomLayout(
    @PrimaryKey val slotIndex: Int,
    val furnitureId: String,
    val placedAt: Long = System.currentTimeMillis()
)
