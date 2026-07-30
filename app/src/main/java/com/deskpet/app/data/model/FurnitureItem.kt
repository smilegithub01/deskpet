package com.deskpet.app.data.model

/**
 * Furniture categories for the pet home decoration system.
 */
enum class FurnitureCategory(val displayName: String, val slotCount: Int) {
    WALLPAPER("墙纸", 1),
    FLOOR("地板", 1),
    BED("床铺", 1),
    TABLE("桌椅", 1),
    DECORATION("装饰品", 2),
    TOY("玩具", 2)
}

/**
 * A furniture item that can be purchased and placed in the pet's room.
 */
data class FurnitureItem(
    val id: String,
    val category: FurnitureCategory,
    val name: String,
    val emoji: String,
    val price: Int = 0,
    val requiredLevel: Int = 1,
    val comfort: Int = 0,
    val funLevel: Int = 0,
    val beauty: Int = 0
)
