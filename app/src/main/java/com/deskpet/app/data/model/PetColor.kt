package com.deskpet.app.data.model

/**
 * Color themes available for the pet.
 */
enum class PetColor(val displayName: String, val hex: String) {
    PINK("暖粉", "#FF8FAB"),
    BLUE("天蓝", "#A8D8FF"),
    MINT("薄荷", "#B8F2D8"),
    LAVENDER("薰衣草", "#C8B6FF"),
    PEACH("蜜桃", "#FFD4A8"),
    WHITE("奶白", "#FFF8F0")
}
