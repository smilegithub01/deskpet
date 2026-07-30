package com.deskpet.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AchievementCategory(val displayName: String) {
    INTERACTION("互动成就"),
    GROWTH("养成成就"),
    EXPLORATION("探索成就")
}

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val category: AchievementCategory,
    val emoji: String,
    val rewardDiamonds: Int,
    val check: (pet: Pet, stats: AchievementStats) -> Boolean
)

data class AchievementStats(
    val totalPets: Int,
    val totalFeeds: Int,
    val totalPhotos: Int,
    val consecutiveDays: Int,
    val ownedOutfitCount: Int,
    val ownedFurnitureCount: Int,
    val postcardCount: Int,
    val uniqueDestinations: Int,
    val petLevel: Int,
    val intimacy: Int
)

@Entity(tableName = "achievement_records")
data class AchievementRecord(
    @PrimaryKey val achievementId: String,
    val unlockedAt: Long
)
