package com.deskpet.app.service

import com.deskpet.app.data.db.AppDatabase
import com.deskpet.app.data.db.InteractionLogDao
import com.deskpet.app.data.db.PetDiaryDao
import com.deskpet.app.data.model.InteractionLog
import com.deskpet.app.data.model.InteractionType
import com.deskpet.app.data.model.PetDiary
import com.deskpet.app.data.model.PetSpecies
import com.deskpet.app.data.repository.PetRepository
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Generates daily pet diaries based on aggregated InteractionLog data.
 *
 * Called on app launch — checks if today's diary exists; if not, generates
 * yesterday's diary (if missing) and today's placeholder.
 */
class PetMemoryEngine(
    private val database: AppDatabase,
    private val repository: PetRepository
) {
    private val logDao: InteractionLogDao = database.interactionLogDao()
    private val diaryDao: PetDiaryDao = database.petDiaryDao()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * Main entry point — call on app launch.
     * Generates diaries for yesterday and the day before if missing.
     */
    suspend fun generateIfNeeded() {
        // Generate yesterday's diary if missing
        val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }
        val yesterdayStr = dateFormat.format(yesterdayCal.time)
        if (diaryDao.getByDate(yesterdayStr) == null) {
            generateDiaryForDate(yesterdayStr, Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) })
        }

        // Also check the day before yesterday in case app was closed for 2+ days
        val twoDaysAgoCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -2) }
        val twoDaysAgoStr = dateFormat.format(twoDaysAgoCal.time)
        if (diaryDao.getByDate(twoDaysAgoStr) == null) {
            generateDiaryForDate(twoDaysAgoStr, Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -2) })
        }
    }

    private suspend fun generateDiaryForDate(dateStr: String, cal: Calendar) {
        val dayStart = cal.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val dayEnd = dayStart + 24 * 60 * 60 * 1000 - 1

        val logs = logDao.getByDateRange(dayStart, dayEnd)
        val content = generateContent(logs)
        val moodSnapshot = getMoodSnapshot(dayStart)
        val pet = repository.getPet()
        val emoji = pet.species.emoji

        diaryDao.insert(PetDiary(
            date = dateStr,
            content = content,
            moodSnapshot = moodSnapshot,
            petEmoji = emoji
        ))
    }

    private suspend fun generateContent(logs: List<InteractionLog>): String {
        if (logs.isEmpty()) {
            return "今天主人好忙呀…我乖乖等了一整天，明天会来看我吗？"
        }

        val feedLogs = logs.filter { it.type == InteractionType.FEED.name }
        val moodLogs = logs.filter { it.type == InteractionType.MOOD_SELECTED.name }
        val photoCount = logs.count { it.type == InteractionType.PHOTO.name }

        // Check consecutive interaction days
        val now = Calendar.getInstance().timeInMillis
        val threeDaysAgo = now - 3L * 24 * 60 * 60 * 1000
        val consecutiveDays = logDao.getDistinctDaysSince(threeDaysAgo)

        // Priority 1: consecutive days >= 3
        if (consecutiveDays >= 3) {
            return "已经连续 $consecutiveDays 天见到主人啦，这成了我最期待的事！"
        }

        // Priority 2: interactions >= 5
        if (logs.size >= 5) {
            return "今天主人来找我玩了 ${logs.size} 次，我是全世界最幸福的小团子！"
        }

        // Priority 3: had feeding
        if (feedLogs.isNotEmpty()) {
            val foods = feedLogs.mapNotNull { log ->
                log.detail.ifBlank { null }
            }.distinct()
            val foodText = if (foods.isNotEmpty()) foods.joinToString("、") else "好吃的"
            return "今天吃了$foodText，主人总是知道我想吃什么～"
        }

        // Priority 4: mood was sad
        val hadSadMood = moodLogs.any { it.detail.contains("SAD") }
        if (hadSadMood) {
            return "主人今天心情不太好，我一直陪着她，希望能让她开心一点"
        }

        // Priority 5: took photos
        if (photoCount > 0) {
            return "今天和主人拍了 $photoCount 张合影，每一张都要好好珍藏～"
        }

        // Default
        return "今天和主人在一起度过了平凡又开心的一天～"
    }

    private suspend fun getMoodSnapshot(dayStart: Long): String {
        val moodLog = database.moodLogDao().getByDate(dayStart)
        return moodLog?.mood?.name ?: "UNKNOWN"
    }

    /**
     * Returns recent diaries for UI display.
     */
    fun getRecentDiaries(): Flow<List<PetDiary>> = diaryDao.getAll()
}
