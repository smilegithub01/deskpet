// app/src/main/java/com/deskpet/app/service/AchievementEngine.kt
package com.deskpet.app.service

import com.deskpet.app.data.db.AppDatabase
import com.deskpet.app.data.model.Achievement
import com.deskpet.app.data.model.AchievementCategory
import com.deskpet.app.data.model.AchievementRecord
import com.deskpet.app.data.model.AchievementStats
import com.deskpet.app.data.model.InteractionType
import com.deskpet.app.data.repository.PetRepository
import kotlinx.coroutines.flow.Flow

class AchievementEngine(
    private val database: AppDatabase,
    private val repository: PetRepository
) {
    private val achievementDao = database.achievementDao()
    private val interactionLogDao = database.interactionLogDao()
    private val postcardDao = database.postcardDao()

    val ALL_ACHIEVEMENTS = listOf(
        // Interaction achievements
        Achievement("pet_50", "抚摸新手", "累计抚摸50次", AchievementCategory.INTERACTION, "🤚", 20) { _, s -> s.totalPets >= 50 },
        Achievement("pet_100", "抚摸达人", "累计抚摸100次", AchievementCategory.INTERACTION, "👋", 50) { _, s -> s.totalPets >= 100 },
        Achievement("pet_500", "抚摸大师", "累计抚摸500次", AchievementCategory.INTERACTION, "💝", 200) { _, s -> s.totalPets >= 500 },
        Achievement("feed_30", "喂食新手", "累计喂食30次", AchievementCategory.INTERACTION, "🍽️", 20) { _, s -> s.totalFeeds >= 30 },
        Achievement("feed_100", "喂食达人", "累计喂食100次", AchievementCategory.INTERACTION, "🍳", 50) { _, s -> s.totalFeeds >= 100 },
        Achievement("feed_300", "美食家", "累计喂食300次", AchievementCategory.INTERACTION, "🍰", 200) { _, s -> s.totalFeeds >= 300 },
        Achievement("photo_10", "初拍", "累计拍照10张", AchievementCategory.INTERACTION, "📸", 20) { _, s -> s.totalPhotos >= 10 },
        Achievement("photo_50", "摄影爱好者", "累计拍照50张", AchievementCategory.INTERACTION, "📷", 50) { _, s -> s.totalPhotos >= 50 },
        Achievement("photo_100", "摄影大师", "累计拍照100张", AchievementCategory.INTERACTION, "🖼️", 200) { _, s -> s.totalPhotos >= 100 },
        // Growth achievements
        Achievement("level_5", "初出茅庐", "宠物达到Lv.5", AchievementCategory.GROWTH, "⭐", 50) { p, _ -> p.level >= 5 },
        Achievement("level_10", "茁壮成长", "宠物达到Lv.10", AchievementCategory.GROWTH, "🌟", 100) { p, _ -> p.level >= 10 },
        Achievement("level_20", "满级达人", "宠物达到Lv.20", AchievementCategory.GROWTH, "🏆", 500) { p, _ -> p.level >= 20 },
        Achievement("intimacy_80", "亲密无间", "亲密度达到80", AchievementCategory.GROWTH, "💕", 100) { _, s -> s.intimacy >= 80 },
        Achievement("intimacy_100", "心心相印", "亲密度达到100", AchievementCategory.GROWTH, "💖", 300) { _, s -> s.intimacy >= 100 },
        Achievement("login_7", "一周不间断", "连续登录7天", AchievementCategory.GROWTH, "📅", 50) { _, s -> s.consecutiveDays >= 7 },
        Achievement("login_30", "月度坚持", "连续登录30天", AchievementCategory.GROWTH, "📆", 200) { _, s -> s.consecutiveDays >= 30 },
        // Exploration achievements
        Achievement("outfit_10", "初入衣橱", "收集10件服饰", AchievementCategory.EXPLORATION, "👗", 50) { _, s -> s.ownedOutfitCount >= 10 },
        Achievement("outfit_20", "时尚达人", "收集20件服饰", AchievementCategory.EXPLORATION, "👔", 100) { _, s -> s.ownedOutfitCount >= 20 },
        Achievement("outfit_48", "衣橱满载", "收集全部48件服饰", AchievementCategory.EXPLORATION, "🛍️", 500) { _, s -> s.ownedOutfitCount >= 48 },
        Achievement("furniture_5", "初置家当", "收集5件家具", AchievementCategory.EXPLORATION, "🪑", 30) { _, s -> s.ownedFurnitureCount >= 5 },
        Achievement("furniture_15", "家居达人", "收集15件家具", AchievementCategory.EXPLORATION, "🏠", 100) { _, s -> s.ownedFurnitureCount >= 15 },
        Achievement("postcard_10", "初级旅人", "收集10张明信片", AchievementCategory.EXPLORATION, "📮", 50) { _, s -> s.postcardCount >= 10 },
        Achievement("postcard_30", "旅行家", "收集30张明信片", AchievementCategory.EXPLORATION, "🗺️", 200) { _, s -> s.postcardCount >= 30 }
    )

    data class CheckResult(
        val newAchievements: List<Achievement>,
        val totalUnlocked: Int
    )

    /**
     * Checks all achievements and unlocks any newly met ones.
     */
    suspend fun checkAll(): CheckResult {
        val pet = repository.pet.value
        val stats = collectStats()
        val unlockedIds = achievementDao.getAllIds().toSet()
        val newlyUnlocked = mutableListOf<Achievement>()

        for (achievement in ALL_ACHIEVEMENTS) {
            if (achievement.id !in unlockedIds && achievement.check(pet, stats)) {
                achievementDao.insert(AchievementRecord(
                    achievementId = achievement.id,
                    unlockedAt = System.currentTimeMillis()
                ))
                repository.addDiamonds(achievement.rewardDiamonds)
                newlyUnlocked.add(achievement)
            }
        }

        return CheckResult(newlyUnlocked, achievementDao.count())
    }

    private suspend fun collectStats(): AchievementStats {
        val now = System.currentTimeMillis()
        val threeDaysAgo = now - 3L * 24 * 60 * 60 * 1000
        val pet = repository.pet.value

        return AchievementStats(
            totalPets = interactionLogDao.countByTypeAndDateRange(
                InteractionType.PET.name, 0, now
            ),
            totalFeeds = interactionLogDao.countByTypeAndDateRange(
                InteractionType.FEED.name, 0, now
            ),
            totalPhotos = interactionLogDao.countByTypeAndDateRange(
                InteractionType.PHOTO.name, 0, now
            ),
            consecutiveDays = interactionLogDao.getDistinctDaysSince(threeDaysAgo),
            ownedOutfitCount = repository.ownedOutfits.value.size,
            ownedFurnitureCount = repository.ownedFurniture.value.size,
            postcardCount = postcardDao.count(),
            uniqueDestinations = postcardDao.getUniqueDestinations(),
            petLevel = pet.level,
            intimacy = pet.intimacy
        )
    }

    fun getUnlockedAchievements(): Flow<List<AchievementRecord>> = achievementDao.getAll()
}
