package com.deskpet.app.data.model

/**
 * Mood levels the pet / user can be in.
 */
enum class MoodLevel(val emoji: String, val displayName: String, val colorHex: String) {
    HAPPY("😊", "开心", "#4CAF50"),
    CALM("😌", "平静", "#2196F3"),
    TIRED("😴", "疲惫", "#9C27B0"),
    EXCITED("🤩", "兴奋", "#FF9800"),
    SAD("🥺", "难过", "#E91E63")
}
