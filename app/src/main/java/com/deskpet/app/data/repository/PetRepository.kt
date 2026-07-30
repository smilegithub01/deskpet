package com.deskpet.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.deskpet.app.data.db.AppDatabase
import com.deskpet.app.data.model.FurnitureCategory
import com.deskpet.app.data.model.FurnitureItem
import com.deskpet.app.data.model.HabitStreak
import com.deskpet.app.data.model.HabitType
import com.deskpet.app.data.model.InteractionLog
import com.deskpet.app.data.model.InteractionType
import com.deskpet.app.data.model.OutfitCategory
import com.deskpet.app.data.model.OutfitItem
import com.deskpet.app.data.model.Pet
import com.deskpet.app.data.model.PetColor
import com.deskpet.app.data.model.PetEntity
import com.deskpet.app.data.model.PetSettings
import com.deskpet.app.data.model.RoomLayout
import com.deskpet.app.data.model.toEntity
import com.deskpet.app.data.model.toPet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Single source of truth for the live [Pet] state and persisted [PetSettings].
 *
 * Pet state is held in an in-memory [MutableStateFlow] (initialized with the
 * default pet "小团子"). Settings and owned outfits are persisted through
 * [SharedPreferences].
 */
class PetRepository private constructor(
    context: Context,
    private val database: AppDatabase
) {

    private val petDao = database.petDao()
    private val habitStreakDao = database.habitStreakDao()
    private val interactionLogDao = database.interactionLogDao()
    private val roomLayoutDao = database.roomLayoutDao()

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _petState = MutableStateFlow(loadPersistedPet())
    val petState: StateFlow<Pet> = _petState.asStateFlow()

    /** Alias for [petState] — used by L2 engines and ViewModels. */
    val pet: StateFlow<Pet> = petState

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<PetSettings> = _settings.asStateFlow()

    private val _ownedOutfits = MutableStateFlow(loadOwnedOutfits())
    val ownedOutfits: StateFlow<List<String>> = _ownedOutfits.asStateFlow()

    // --- Furniture System ---
    private val _ownedFurniture = MutableStateFlow(loadOwnedFurniture())
    val ownedFurniture: StateFlow<List<String>> = _ownedFurniture.asStateFlow()

    private val _roomLayout = MutableStateFlow<List<RoomLayout>>(emptyList())
    val roomLayout: StateFlow<List<RoomLayout>> = _roomLayout.asStateFlow()

    // ------------------------------------------------------------------ Pet

    fun getPet(): Pet = _petState.value

    fun feedPet(food: String) {
        _petState.update { pet ->
            pet.copy(
                hunger = (pet.hunger + FOOD_HUNGER_GAIN).coerceIn(0, MAX_STAT),
                mood = (pet.mood + FOOD_MOOD_GAIN).coerceIn(0, MAX_STAT),
                intimacy = (pet.intimacy + FOOD_INTIMACY_GAIN).coerceIn(0, MAX_STAT)
            )
        }
        persistPet()
    }

    fun petPet() {
        _petState.update { pet ->
            pet.copy(
                mood = (pet.mood + PET_MOOD_GAIN).coerceIn(0, MAX_STAT),
                intimacy = (pet.intimacy + PET_INTIMACY_GAIN).coerceIn(0, MAX_STAT)
            )
        }
        persistPet()
    }

    fun updateMood(delta: Int) {
        _petState.update { pet ->
            pet.copy(mood = (pet.mood + delta).coerceIn(0, MAX_STAT))
        }
        persistPet()
    }

    fun updateHunger(delta: Int) {
        _petState.update { pet ->
            pet.copy(hunger = (pet.hunger + delta).coerceIn(0, MAX_STAT))
        }
        persistPet()
    }

    fun addDiamonds(amount: Int) {
        _petState.update { pet ->
            pet.copy(diamonds = (pet.diamonds + amount).coerceAtLeast(0))
        }
        persistPet()
    }

    /**
     * Applies an arbitrary transformation to the live [Pet] state.
     *
     * Used by the onboarding flow to set the pet's name / species / color /
     * personality tags after the user finishes setup.
     */
    fun updatePet(transform: (Pet) -> Pet) {
        _petState.update(transform)
        persistPet()
    }

    // --------------------------------------------------------------- Outfits

    /** Returns the full catalogue of [OutfitItem]s (without ownership state). */
    fun getOutfitItems(): List<OutfitItem> = OUTFIT_CATALOGUE

    /** Returns the catalogue with current ownership state applied. */
    fun getOutfitShop(): List<OutfitItem> =
        OUTFIT_CATALOGUE.map { it.copy(isOwned = _ownedOutfits.value.contains(it.id)) }

    /**
     * Attempts to purchase an item. Returns `true` on success (enough diamonds
     * and the pet level requirement is met).
     */
    fun purchaseItem(item: OutfitItem): Boolean {
        val pet = _petState.value
        if (_ownedOutfits.value.contains(item.id)) return true
        if (pet.level < item.requiredLevel) return false
        if (pet.diamonds < item.price) return false

        _petState.update { it.copy(diamonds = it.diamonds - item.price) }
        val updated = _ownedOutfits.value + item.id
        _ownedOutfits.value = updated
        saveOwnedOutfits(updated)
        persistPet()
        return true
    }

    /** Equips (or swaps) the given owned item in its category slot. */
    fun equipItem(item: OutfitItem): Boolean {
        if (!_ownedOutfits.value.contains(item.id)) return false
        _petState.update { pet ->
            when (item.category) {
                OutfitCategory.HEAD -> pet.copy(equippedHead = item.id)
                OutfitCategory.GLASSES -> pet.copy(equippedGlasses = item.id)
                OutfitCategory.COLLAR -> pet.copy(equippedCollar = item.id)
                OutfitCategory.CLOTHING -> pet.copy(equippedClothing = item.id)
                OutfitCategory.TAIL -> pet.copy(equippedTail = item.id)
                OutfitCategory.ACCESSORY -> pet.copy(equippedAccessory = item.id)
            }
        }
        persistPet()
        return true
    }

    /** Removes whatever is equipped in the given category. */
    fun unequip(category: OutfitCategory) {
        _petState.update { pet ->
            when (category) {
                OutfitCategory.HEAD -> pet.copy(equippedHead = null)
                OutfitCategory.GLASSES -> pet.copy(equippedGlasses = null)
                OutfitCategory.COLLAR -> pet.copy(equippedCollar = null)
                OutfitCategory.CLOTHING -> pet.copy(equippedClothing = null)
                OutfitCategory.TAIL -> pet.copy(equippedTail = null)
                OutfitCategory.ACCESSORY -> pet.copy(equippedAccessory = null)
            }
        }
        persistPet()
    }

    // ------------------------------------------------------------- Settings

    fun getSettings(): PetSettings = _settings.value

    fun updateSettings(transform: (PetSettings) -> PetSettings) {
        val updated = transform(_settings.value)
        _settings.value = updated
        saveSettings(updated)
    }

    // ---------------------------------------------------------- Furniture

    /** Returns the full catalogue of [FurnitureItem]s. */
    fun getFurnitureItems(): List<FurnitureItem> = FURNITURE_CATALOGUE

    /** Loads room layout from DB into the StateFlow. */
    suspend fun loadRoomLayout() {
        _roomLayout.value = roomLayoutDao.getAllOnce()
    }

    /**
     * Attempts to purchase a furniture item. Returns `true` on success.
     */
    fun purchaseFurniture(item: FurnitureItem): Boolean {
        val pet = _petState.value
        if (_ownedFurniture.value.contains(item.id)) return true
        if (pet.level < item.requiredLevel) return false
        if (pet.diamonds < item.price) return false

        _petState.update { it.copy(diamonds = it.diamonds - item.price) }
        val updated = _ownedFurniture.value + item.id
        _ownedFurniture.value = updated
        saveOwnedFurniture(updated)
        persistPet()
        return true
    }

    /** Places a furniture item in the given slot. */
    suspend fun placeFurniture(slotIndex: Int, furnitureId: String) {
        roomLayoutDao.upsert(RoomLayout(slotIndex = slotIndex, furnitureId = furnitureId))
        loadRoomLayout()
    }

    /** Removes whatever is in the given slot. */
    suspend fun removeFurniture(slotIndex: Int) {
        roomLayoutDao.removeSlot(slotIndex)
        loadRoomLayout()
    }

    /** Returns (comfort, fun, beauty) totals from currently placed furniture. */
    fun getRoomStats(): Triple<Int, Int, Int> {
        val layout = _roomLayout.value
        if (layout.isEmpty()) return Triple(0, 0, 0)
        val items = layout.mapNotNull { slot ->
            FURNITURE_CATALOGUE.find { it.id == slot.furnitureId }
        }
        return Triple(
            items.sumOf { it.comfort },
            items.sumOf { it.funLevel },
            items.sumOf { it.beauty }
        )
    }

    // ----------------------------------------------------- Habit Check-in

    suspend fun checkinHabit(habitType: HabitType): HabitCheckinResult {
        val now = System.currentTimeMillis()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(now))

        val lastCheckTime = when (habitType) {
            HabitType.DRINK -> _settings.value.lastDrinkCheckTime
            HabitType.SIT -> _settings.value.lastSitCheckTime
            HabitType.EYE -> _settings.value.lastEyeCheckTime
        }
        val intervalMs = when (habitType) {
            HabitType.DRINK -> _settings.value.waterReminderInterval * 60_000L
            HabitType.SIT -> _settings.value.sitReminderInterval * 60_000L
            HabitType.EYE -> _settings.value.eyeReminderInterval * 60_000L
        }
        if (now - lastCheckTime < intervalMs && lastCheckTime > 0) {
            return HabitCheckinResult(success = false, message = "还没到时间哦，稍后再来~")
        }

        updateSettings { settings ->
            when (habitType) {
                HabitType.DRINK -> settings.copy(lastDrinkCheckTime = now)
                HabitType.SIT -> settings.copy(lastSitCheckTime = now)
                HabitType.EYE -> settings.copy(lastEyeCheckTime = now)
            }
        }

        val (hungerDelta, moodDelta, intimacyDelta) = when (habitType) {
            HabitType.DRINK -> Triple(3, 0, 0)
            HabitType.SIT -> Triple(0, 5, 0)
            HabitType.EYE -> Triple(0, 0, 2)
        }
        val diamondDelta = 1

        _petState.update { pet ->
            pet.copy(
                hunger = (pet.hunger + hungerDelta).coerceIn(0, MAX_STAT),
                mood = (pet.mood + moodDelta).coerceIn(0, MAX_STAT),
                intimacy = (pet.intimacy + intimacyDelta).coerceIn(0, MAX_STAT),
                diamonds = pet.diamonds + diamondDelta
            )
        }

        val streak = habitStreakDao.getByType(habitType.name)
        val yesterdayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Date(now - 24 * 60 * 60 * 1000))

        val newStreak = if (streak == null) {
            1
        } else if (streak.lastCheckDate == todayStr) {
            streak.currentStreak
        } else if (streak.lastCheckDate == yesterdayStr) {
            streak.currentStreak + 1
        } else {
            1
        }

        val bonusDiamonds = when (newStreak) {
            3 -> 5
            7 -> 15
            15 -> 30
            30 -> 50
            else -> 0
        }

        if (bonusDiamonds > 0) {
            _petState.update { it.copy(diamonds = it.diamonds + bonusDiamonds) }
        }

        habitStreakDao.upsert(HabitStreak(
            habitType = habitType.name,
            currentStreak = newStreak,
            longestStreak = maxOf(streak?.longestStreak ?: 0, newStreak),
            lastCheckDate = todayStr
        ))

        persistPet()

        interactionLogDao.insert(InteractionLog(
            type = InteractionType.CHECKIN.name,
            timestamp = now,
            detail = habitType.name
        ))

        val msg = if (bonusDiamonds > 0) {
            "打卡成功！连续${newStreak}天，奖励${bonusDiamonds}钻石~"
        } else {
            "打卡成功！连续${newStreak}天~"
        }

        return HabitCheckinResult(
            success = true,
            message = msg,
            newStreak = newStreak,
            bonusDiamonds = bonusDiamonds
        )
    }

    /** Exposes habit streaks flow for UI. */
    fun getHabitStreaksFlow() = habitStreakDao.getAll()

    // ----------------------------------------------------- Persistence helpers

    private fun loadSettings(): PetSettings {
        if (!prefs.contains(KEY_SETTINGS_INITIALIZED)) {
            // First run: persist defaults.
            saveSettings(PetSettings())
            prefs.edit().putBoolean(KEY_SETTINGS_INITIALIZED, true).apply()
            return PetSettings()
        }
        return PetSettings(
            overlayEnabled = prefs.getBoolean(SettingsKeys.OVERLAY_ENABLED, true),
            autoBehavior = prefs.getBoolean(SettingsKeys.AUTO_BEHAVIOR, true),
            smartAvoidance = prefs.getBoolean(SettingsKeys.SMART_AVOIDANCE, true),
            widgetEnabled = prefs.getBoolean(SettingsKeys.WIDGET_ENABLED, true),
            liveWallpaperEnabled = prefs.getBoolean(SettingsKeys.LIVE_WALLPAPER, false),
            soundEnabled = prefs.getBoolean(SettingsKeys.SOUND_ENABLED, true),
            themeColor = runCatching {
                PetColor.valueOf(prefs.getString(SettingsKeys.THEME_COLOR, PetColor.PINK.name)!!)
            }.getOrDefault(PetColor.PINK),
            periodTrackingEnabled = prefs.getBoolean(SettingsKeys.PERIOD_TRACKING, true),
            waterReminderEnabled = prefs.getBoolean(SettingsKeys.WATER_REMINDER_ENABLED, true),
            waterReminderInterval = prefs.getInt(SettingsKeys.WATER_REMINDER_INTERVAL, 60),
            sitReminderEnabled = prefs.getBoolean(SettingsKeys.SIT_REMINDER_ENABLED, true),
            sitReminderInterval = prefs.getInt(SettingsKeys.SIT_REMINDER_INTERVAL, 120),
            eyeReminderEnabled = prefs.getBoolean(SettingsKeys.EYE_REMINDER_ENABLED, true),
            eyeReminderInterval = prefs.getInt(SettingsKeys.EYE_REMINDER_INTERVAL, 45),
            quietHoursEnabled = prefs.getBoolean(SettingsKeys.QUIET_HOURS_ENABLED, true),
            quietHoursStart = prefs.getInt(SettingsKeys.QUIET_HOURS_START, 23),
            quietHoursEnd = prefs.getInt(SettingsKeys.QUIET_HOURS_END, 7),
            dataEncrypted = prefs.getBoolean(SettingsKeys.DATA_ENCRYPTED, true),
            lastDrinkCheckTime = prefs.getLong(SettingsKeys.LAST_DRINK_CHECK, 0L),
            lastSitCheckTime = prefs.getLong(SettingsKeys.LAST_SIT_CHECK, 0L),
            lastEyeCheckTime = prefs.getLong(SettingsKeys.LAST_EYE_CHECK, 0L),
            periodBehaviorLink = prefs.getBoolean(SettingsKeys.PERIOD_BEHAVIOR_LINK, false),
            envAwarenessEnabled = prefs.getBoolean(SettingsKeys.ENV_AWARENESS_ENABLED, true),
            ttsEnabled = prefs.getBoolean(SettingsKeys.TTS_ENABLED, false)
        )
    }

    private fun saveSettings(settings: PetSettings) {
        prefs.edit().apply {
            putBoolean(SettingsKeys.OVERLAY_ENABLED, settings.overlayEnabled)
            putBoolean(SettingsKeys.AUTO_BEHAVIOR, settings.autoBehavior)
            putBoolean(SettingsKeys.SMART_AVOIDANCE, settings.smartAvoidance)
            putBoolean(SettingsKeys.WIDGET_ENABLED, settings.widgetEnabled)
            putBoolean(SettingsKeys.LIVE_WALLPAPER, settings.liveWallpaperEnabled)
            putBoolean(SettingsKeys.SOUND_ENABLED, settings.soundEnabled)
            putString(SettingsKeys.THEME_COLOR, settings.themeColor.name)
            putBoolean(SettingsKeys.PERIOD_TRACKING, settings.periodTrackingEnabled)
            putBoolean(SettingsKeys.WATER_REMINDER_ENABLED, settings.waterReminderEnabled)
            putInt(SettingsKeys.WATER_REMINDER_INTERVAL, settings.waterReminderInterval)
            putBoolean(SettingsKeys.SIT_REMINDER_ENABLED, settings.sitReminderEnabled)
            putInt(SettingsKeys.SIT_REMINDER_INTERVAL, settings.sitReminderInterval)
            putBoolean(SettingsKeys.EYE_REMINDER_ENABLED, settings.eyeReminderEnabled)
            putInt(SettingsKeys.EYE_REMINDER_INTERVAL, settings.eyeReminderInterval)
            putBoolean(SettingsKeys.QUIET_HOURS_ENABLED, settings.quietHoursEnabled)
            putInt(SettingsKeys.QUIET_HOURS_START, settings.quietHoursStart)
            putInt(SettingsKeys.QUIET_HOURS_END, settings.quietHoursEnd)
            putBoolean(SettingsKeys.DATA_ENCRYPTED, settings.dataEncrypted)
            putLong(SettingsKeys.LAST_DRINK_CHECK, settings.lastDrinkCheckTime)
            putLong(SettingsKeys.LAST_SIT_CHECK, settings.lastSitCheckTime)
            putLong(SettingsKeys.LAST_EYE_CHECK, settings.lastEyeCheckTime)
            putBoolean(SettingsKeys.PERIOD_BEHAVIOR_LINK, settings.periodBehaviorLink)
            putBoolean(SettingsKeys.ENV_AWARENESS_ENABLED, settings.envAwarenessEnabled)
            putBoolean(SettingsKeys.TTS_ENABLED, settings.ttsEnabled)
        }.apply()
    }

    private fun loadOwnedOutfits(): List<String> {
        val raw = prefs.getString(KEY_OWNED_OUTFITS, "") ?: ""
        return if (raw.isBlank()) DEFAULT_OWNED_IDS else raw.split(SEPARATOR)
    }

    private fun saveOwnedOutfits(ids: List<String>) {
        prefs.edit().putString(KEY_OWNED_OUTFITS, ids.joinToString(SEPARATOR)).apply()
    }

    private fun loadOwnedFurniture(): List<String> {
        val raw = prefs.getString(KEY_OWNED_FURNITURE, "") ?: ""
        return if (raw.isBlank()) DEFAULT_OWNED_FURNITURE else raw.split(SEPARATOR)
    }

    private fun saveOwnedFurniture(ids: List<String>) {
        prefs.edit().putString(KEY_OWNED_FURNITURE, ids.joinToString(SEPARATOR)).apply()
    }

    // ----------------------------------------------------------- Defaults

    private fun persistPet() {
        val pet = _petState.value
        runBlocking {
            petDao.upsert(pet.toEntity(lastInteractionTime = System.currentTimeMillis()))
        }
    }

    private fun loadPersistedPet(): Pet {
        val entity = runBlocking { petDao.getPet() }
        return if (entity != null) {
            val decayed = applyOfflineDecay(entity)
            val pet = decayed.toPet()
            runBlocking { petDao.upsert(decayed) }
            pet
        } else {
            val defaultPet = createDefaultPet()
            runBlocking { petDao.upsert(defaultPet.toEntity()) }
            defaultPet
        }
    }

    private fun applyOfflineDecay(entity: PetEntity): PetEntity {
        val now = System.currentTimeMillis()
        val elapsedMs = now - entity.lastInteractionTime
        val elapsedHours = (elapsedMs / (1000 * 60 * 60)).toInt()

        if (elapsedHours < 4) return entity.copy(lastInteractionTime = now)

        var hunger = entity.hunger
        var mood = entity.mood
        var intimacy = entity.intimacy

        if (elapsedHours > 4) {
            val hungerDecay = (elapsedHours - 4) * 2
            hunger = (hunger - hungerDecay).coerceAtLeast(0)
        }
        if (elapsedHours > 8) {
            val moodDecay = (elapsedHours - 8)
            mood = (mood - moodDecay).coerceAtLeast(0)
        }
        if (elapsedHours > 24) {
            val intimacyDecay = (elapsedHours - 24) / 2
            intimacy = (intimacy - intimacyDecay).coerceAtLeast(0)
        }

        return entity.copy(
            hunger = hunger,
            mood = mood,
            intimacy = intimacy,
            lastInteractionTime = now
        )
    }

    private fun createDefaultPet(): Pet = Pet()

    // ----------------------------------------------------------- Companion

    companion object {
        private const val PREFS_NAME = "deskpet_prefs"
        private const val KEY_SETTINGS_INITIALIZED = "settings_initialized"
        private const val KEY_OWNED_OUTFITS = "owned_outfits"
        private const val KEY_OWNED_FURNITURE = "owned_furniture"
        private const val SEPARATOR = ","

        private const val MAX_STAT = 100
        private const val FOOD_HUNGER_GAIN = 15
        private const val FOOD_MOOD_GAIN = 5
        private const val FOOD_INTIMACY_GAIN = 2
        private const val PET_MOOD_GAIN = 8
        private const val PET_INTIMACY_GAIN = 3

        /** Item ids that are owned by default. */
        private val DEFAULT_OWNED_IDS = listOf("head_bow", "collar_bell", "cloth_scarf")

        /** Furniture ids that are owned by default. */
        private val DEFAULT_OWNED_FURNITURE = listOf("wall_pink", "floor_wood", "bed_round", "decor_plant")

        private object SettingsKeys {
            const val OVERLAY_ENABLED = "overlay_enabled"
            const val AUTO_BEHAVIOR = "auto_behavior"
            const val SMART_AVOIDANCE = "smart_avoidance"
            const val WIDGET_ENABLED = "widget_enabled"
            const val LIVE_WALLPAPER = "live_wallpaper"
            const val SOUND_ENABLED = "sound_enabled"
            const val THEME_COLOR = "theme_color"
            const val PERIOD_TRACKING = "period_tracking"
            const val WATER_REMINDER_ENABLED = "water_reminder_enabled"
            const val WATER_REMINDER_INTERVAL = "water_reminder_interval"
            const val SIT_REMINDER_ENABLED = "sit_reminder_enabled"
            const val SIT_REMINDER_INTERVAL = "sit_reminder_interval"
            const val EYE_REMINDER_ENABLED = "eye_reminder_enabled"
            const val EYE_REMINDER_INTERVAL = "eye_reminder_interval"
            const val QUIET_HOURS_ENABLED = "quiet_hours_enabled"
            const val QUIET_HOURS_START = "quiet_hours_start"
            const val QUIET_HOURS_END = "quiet_hours_end"
            const val DATA_ENCRYPTED = "data_encrypted"
            const val LAST_DRINK_CHECK = "last_drink_check"
            const val LAST_SIT_CHECK = "last_sit_check"
            const val LAST_EYE_CHECK = "last_eye_check"
            const val PERIOD_BEHAVIOR_LINK = "period_behavior_link"
            const val ENV_AWARENESS_ENABLED = "env_awareness_enabled"
            const val TTS_ENABLED = "tts_enabled"
        }

        /**
         * The full catalogue of outfit items (~8 per category with various
         * prices, required levels and (default) ownership).
         */
        val OUTFIT_CATALOGUE: List<OutfitItem> = listOf(
            // HEAD
            OutfitItem("head_bow", OutfitCategory.HEAD, "蝴蝶结", "🎀", 100, 1, true),
            OutfitItem("head_flower", OutfitCategory.HEAD, "花朵", "🌸", 150, 2),
            OutfitItem("head_beanie", OutfitCategory.HEAD, "毛线帽", "🧢", 250, 3),
            OutfitItem("head_santa", OutfitCategory.HEAD, "圣诞帽", "🎅", 300, 4),
            OutfitItem("head_hat", OutfitCategory.HEAD, "魔法帽", "🎩", 500, 5),
            OutfitItem("head_crown", OutfitCategory.HEAD, "小皇冠", "👑", 800, 10),
            OutfitItem("head_headphone", OutfitCategory.HEAD, "耳机", "🎧", 600, 7),
            OutfitItem("head_tophat", OutfitCategory.HEAD, "礼帽", "🎩", 900, 12),

            // GLASSES
            OutfitItem("glasses_round", OutfitCategory.GLASSES, "圆框眼镜", "👓", 200, 1),
            OutfitItem("glasses_sun", OutfitCategory.GLASSES, "墨镜", "🕶️", 350, 3),
            OutfitItem("glasses_3d", OutfitCategory.GLASSES, "3D眼镜", "🎞️", 450, 5),
            OutfitItem("glasses_star", OutfitCategory.GLASSES, "星星眼镜", "🤓", 600, 6),
            OutfitItem("glasses_monocle", OutfitCategory.GLASSES, "单片眼镜", "🧐", 700, 8),
            OutfitItem("glasses_party", OutfitCategory.GLASSES, "派对眼镜", "🥳", 400, 4),
            OutfitItem("glasses_neon", OutfitCategory.GLASSES, "霓虹眼镜", "😎", 1000, 14),
            OutfitItem("glasses_heart", OutfitCategory.GLASSES, "爱心眼镜", "😍", 850, 11),

            // COLLAR
            OutfitItem("collar_bell", OutfitCategory.COLLAR, "铃铛项圈", "🔔", 180, 1, true),
            OutfitItem("collar_bow", OutfitCategory.COLLAR, "蝴蝶项圈", "🎀", 220, 2),
            OutfitItem("collar_ribbon", OutfitCategory.COLLAR, "丝带", "🎀", 200, 1),
            OutfitItem("collar_pearl", OutfitCategory.COLLAR, "珍珠项链", "📿", 700, 8),
            OutfitItem("collar_gold", OutfitCategory.COLLAR, "金链", "💰", 1200, 15),
            OutfitItem("collar_bone", OutfitCategory.COLLAR, "骨头吊坠", "🦴", 350, 4),
            OutfitItem("collar_crystal", OutfitCategory.COLLAR, "水晶吊坠", "💎", 950, 13),
            OutfitItem("collar_flower", OutfitCategory.COLLAR, "花朵项圈", "🌺", 320, 3),

            // CLOTHING
            OutfitItem("cloth_scarf", OutfitCategory.CLOTHING, "围巾", "🧣", 150, 1, true),
            OutfitItem("cloth_sweater", OutfitCategory.CLOTHING, "毛衣", "👕", 300, 3),
            OutfitItem("cloth_dress", OutfitCategory.CLOTHING, "小裙子", "👗", 500, 5),
            OutfitItem("cloth_cape", OutfitCategory.CLOTHING, "披风", "🧥", 650, 7),
            OutfitItem("cloth_suit", OutfitCategory.CLOTHING, "西装", "🤵", 900, 12),
            OutfitItem("cloth_kimono", OutfitCategory.CLOTHING, "和服", "👘", 800, 10),
            OutfitItem("cloth_swimsuit", OutfitCategory.CLOTHING, "泳衣", "🩱", 550, 6),
            OutfitItem("cloth_pajama", OutfitCategory.CLOTHING, "睡衣", "🩲", 280, 2),

            // TAIL
            OutfitItem("tail_ribbon", OutfitCategory.TAIL, "丝带", "🎀", 200, 1),
            OutfitItem("tail_star", OutfitCategory.TAIL, "星星", "⭐", 400, 4),
            OutfitItem("tail_flower", OutfitCategory.TAIL, "花朵", "🌸", 280, 2),
            OutfitItem("tail_balloon", OutfitCategory.TAIL, "气球", "🎈", 350, 3),
            OutfitItem("tail_butterfly", OutfitCategory.TAIL, "蝴蝶结", "🦋", 600, 6),
            OutfitItem("tail_rainbow", OutfitCategory.TAIL, "彩虹", "🌈", 1100, 14),
            OutfitItem("tail_cloud", OutfitCategory.TAIL, "云朵", "☁️", 500, 5),
            OutfitItem("tail_heart", OutfitCategory.TAIL, "爱心", "💕", 750, 9),

            // ACCESSORY
            OutfitItem("acc_balloon", OutfitCategory.ACCESSORY, "气球", "🎈", 200, 1),
            OutfitItem("acc_lollipop", OutfitCategory.ACCESSORY, "棒棒糖", "🍭", 150, 1),
            OutfitItem("acc_umbrella", OutfitCategory.ACCESSORY, "小伞", "☂️", 450, 4),
            OutfitItem("acc_wand", OutfitCategory.ACCESSORY, "魔法棒", "🪄", 800, 9),
            OutfitItem("acc_book", OutfitCategory.ACCESSORY, "魔法书", "📖", 650, 7),
            OutfitItem("acc_camera", OutfitCategory.ACCESSORY, "相机", "📷", 700, 8),
            OutfitItem("acc_gift", OutfitCategory.ACCESSORY, "礼物盒", "🎁", 550, 6),
            OutfitItem("acc_star", OutfitCategory.ACCESSORY, "星星权杖", "✨", 1000, 13)
        )

        /**
         * The full catalogue of furniture items for the pet home decoration.
         * Categories: WALLPAPER, FLOOR, BED, TABLE, DECORATION, TOY.
         * Each item has comfort/fun/beauty attributes that affect pet behavior.
         */
        val FURNITURE_CATALOGUE: List<FurnitureItem> = listOf(
            // WALLPAPER (slot 0)
            FurnitureItem("wall_pink", FurnitureCategory.WALLPAPER, "粉色墙纸", "🩷", 0, 1, 2, 0, 5),
            FurnitureItem("wall_mint", FurnitureCategory.WALLPAPER, "薄荷墙纸", "🌿", 80, 3, 3, 0, 8),
            FurnitureItem("wall_star", FurnitureCategory.WALLPAPER, "星空墙纸", "✨", 200, 5, 5, 2, 15),
            FurnitureItem("wall_rainbow", FurnitureCategory.WALLPAPER, "彩虹墙纸", "🌈", 500, 10, 8, 5, 25),

            // FLOOR (slot 1)
            FurnitureItem("floor_wood", FurnitureCategory.FLOOR, "木地板", "🪵", 0, 1, 3, 0, 3),
            FurnitureItem("floor_tile", FurnitureCategory.FLOOR, "瓷砖地板", "⬜", 60, 2, 2, 0, 5),
            FurnitureItem("floor_carpet", FurnitureCategory.FLOOR, "毛绒地毯", "🟥", 150, 4, 8, 3, 10),
            FurnitureItem("floor_cloud", FurnitureCategory.FLOOR, "云朵地板", "☁️", 400, 8, 10, 5, 20),

            // BED (slot 2)
            FurnitureItem("bed_round", FurnitureCategory.BED, "圆垫床", "🛏️", 0, 1, 5, 0, 2),
            FurnitureItem("bed_basket", FurnitureCategory.BED, "编织篮床", "🧺", 100, 3, 8, 2, 5),
            FurnitureItem("bed_castle", FurnitureCategory.BED, "城堡床", "🏰", 300, 6, 12, 5, 15),
            FurnitureItem("bed_cloud", FurnitureCategory.BED, "云朵床", "☁️", 600, 12, 18, 8, 25),

            // TABLE (slot 3)
            FurnitureItem("table_wood", FurnitureCategory.TABLE, "木桌", "🪑", 0, 1, 0, 2, 3),
            FurnitureItem("table_tea", FurnitureCategory.TABLE, "茶几", "🍵", 120, 3, 0, 5, 8),
            FurnitureItem("table_desk", FurnitureCategory.TABLE, "书桌", "📚", 250, 5, 0, 10, 12),
            FurnitureItem("table_cafe", FurnitureCategory.TABLE, "咖啡桌", "☕", 350, 7, 3, 8, 15),

            // DECORATION (slots 4-5)
            FurnitureItem("decor_plant", FurnitureCategory.DECORATION, "小盆栽", "🪴", 0, 1, 2, 1, 8),
            FurnitureItem("decor_flower", FurnitureCategory.DECORATION, "花瓶", "🌸", 80, 2, 3, 2, 10),
            FurnitureItem("decor_lamp", FurnitureCategory.DECORATION, "小夜灯", "💡", 150, 4, 5, 3, 12),
            FurnitureItem("decor_clock", FurnitureCategory.DECORATION, "挂钟", "🕐", 200, 5, 0, 5, 10),
            FurnitureItem("decor_painting", FurnitureCategory.DECORATION, "挂画", "🖼️", 300, 6, 0, 8, 18),
            FurnitureItem("decor_crystal", FurnitureCategory.DECORATION, "水晶球", "🔮", 500, 10, 5, 10, 25),

            // TOY (slots 6-7)
            FurnitureItem("toy_ball", FurnitureCategory.TOY, "毛线球", "🧶", 0, 1, 0, 8, 3),
            FurnitureItem("toy_mouse", FurnitureCategory.TOY, "小老鼠", "🐭", 80, 2, 0, 12, 5),
            FurnitureItem("toy_tower", FurnitureCategory.TOY, "猫爬架", "🏗️", 250, 5, 5, 18, 12),
            FurnitureItem("toy_tunnel", FurnitureCategory.TOY, "隧道", "🌀", 200, 4, 3, 15, 10),
            FurnitureItem("toy_piano", FurnitureCategory.TOY, "小钢琴", "🎹", 400, 8, 8, 20, 18),
            FurnitureItem("toy_robot", FurnitureCategory.TOY, "机器人", "🤖", 600, 12, 10, 25, 20)
        )

        @Volatile
        private var INSTANCE: PetRepository? = null

        fun getInstance(context: Context): PetRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: PetRepository(context, AppDatabase.getInstance(context)).also { INSTANCE = it }
            }
    }
}

data class HabitCheckinResult(
    val success: Boolean,
    val message: String,
    val newStreak: Int = 0,
    val bonusDiamonds: Int = 0
)
