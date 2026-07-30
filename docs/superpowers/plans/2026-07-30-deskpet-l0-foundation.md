# DeskPet L0 基础修复层 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现 L0 基础修复层的 4 个功能模块：宠物状态持久化、装扮系统升级、拍照功能落地、音效系统完善。

**Architecture:** 在现有 MVVM + Room + Compose 架构上扩展。L0-1 新增 PetEntity Room 实体实现状态持久化与离线衰减；L0-2 将 emoji 装扮改为 Canvas 矢量渲染；L0-3 用 Bitmap 离屏渲染实现真实拍照；L0-4 扩展程序化音效种类并接入触发点。

**Tech Stack:** Kotlin 1.9.22, Jetpack Compose (Material3), Room 2.6.1, KSP, Android SDK 34, JVM 17

**Spec:** `docs/superpowers/specs/2026-07-30-deskpet-feature-roadmap-design.md` — L0 章节

---

## 文件结构

### 新建文件
| 文件 | 职责 |
|------|------|
| `data/model/PetEntity.kt` | Room 实体，持久化宠物全部状态字段 |
| `data/db/PetDao.kt` | Pet 实体的 DAO 接口 |
| `util/PhotoHelper.kt` | Bitmap 离屏渲染 + 文件保存 + 分享 |
| `ui/components/OutfitRenderer.kt` | 服饰矢量渲染映射表（替代 emoji） |
| `res/xml/file_paths.xml` | FileProvider 路径配置 |

### 修改文件
| 文件 | 修改内容 |
|------|---------|
| `data/db/AppDatabase.kt` | 新增 PetEntity，version 1→2，添加 Migration |
| `data/db/Converters.kt` | 新增 PetSpecies / PetColor / PersonalityTag 转换器 |
| `data/repository/PetRepository.kt` | 初始化读取 Room，更新同步写入 Room，离线衰减 |
| `util/SoundHelper.kt` | 新增 8 种音效的频率定义 |
| `ui/components/PetCanvas.kt` | 装扮渲染从 emoji 改为调用 OutfitRenderer |
| `ui/screens/home/PetViewModel.kt` | onPhoto() 实现真实拍照 |
| `ui/screens/home/PetHomeScreen.kt` | PetStage 提取可复用绘制逻辑 |
| `ui/screens/dressup/DressUpScreen.kt` | 预览窗口从 120dp 改为 180dp |
| `DeskPetApplication.kt` | 启动时播放 GREETING 音效 |
| `service/PetBehaviorEngine.kt` | SLEEPY 切换时播放 SLEEP/WAKE |
| `AndroidManifest.xml` | 注册 FileProvider |
| `app/build.gradle.kts` | 无新依赖（L0 仅用现有库） |

---

## Task 1: PetEntity Room 实体 + PetDao + 数据库迁移

**Files:**
- Create: `app/src/main/java/com/deskpet/app/data/model/PetEntity.kt`
- Create: `app/src/main/java/com/deskpet/app/data/db/PetDao.kt`
- Modify: `app/src/main/java/com/deskpet/app/data/db/Converters.kt`
- Modify: `app/src/main/java/com/deskpet/app/data/db/AppDatabase.kt`

- [ ] **Step 1: 创建 PetEntity.kt**

```kotlin
// app/src/main/java/com/deskpet/app/data/model/PetEntity.kt
package com.deskpet.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.deskpet.app.data.db.Converters

/**
 * Persistent representation of the desk pet stored in Room.
 *
 * Mirrors the fields of [Pet] but as a Room entity so state survives
 * app restarts. Only one row exists (id = 1).
 */
@Entity(tableName = "pet_state")
@TypeConverters(Converters::class)
data class PetEntity(
    @PrimaryKey val id: Long = 1,
    val name: String = "小团子",
    val species: PetSpecies = PetSpecies.CAT,
    val color: PetColor = PetColor.PINK,
    val level: Int = 1,
    val hunger: Int = 80,
    val mood: Int = 80,
    val intimacy: Int = 50,
    val diamonds: Int = 500,
    val personalityTags: List<PersonalityTag> = listOf(PersonalityTag.LIVELY, PersonalityTag.CLINGY),
    val equippedHead: String? = null,
    val equippedGlasses: String? = null,
    val equippedCollar: String? = null,
    val equippedClothing: String? = null,
    val equippedTail: String? = null,
    val equippedAccessory: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastInteractionTime: Long = System.currentTimeMillis()
)
```

- [ ] **Step 2: 在 PetEntity 上添加 toPet() 和 fromPet() 转换函数**

在 `PetEntity.kt` 文件末尾添加：

```kotlin
/** Converts the Room entity to the runtime [Pet] data class. */
fun PetEntity.toPet(): Pet = Pet(
    id = id,
    name = name,
    species = species,
    color = color,
    level = level,
    hunger = hunger,
    mood = mood,
    intimacy = intimacy,
    diamonds = diamonds,
    personalityTags = personalityTags,
    equippedHead = equippedHead,
    equippedGlasses = equippedGlasses,
    equippedCollar = equippedCollar,
    equippedClothing = equippedClothing,
    equippedTail = equippedTail,
    equippedAccessory = equippedAccessory,
    createdAt = createdAt
)

/** Converts the runtime [Pet] to a Room entity, preserving lastInteractionTime if provided. */
fun Pet.toEntity(lastInteractionTime: Long = System.currentTimeMillis()): PetEntity = PetEntity(
    id = id,
    name = name,
    species = species,
    color = color,
    level = level,
    hunger = hunger,
    mood = mood,
    intimacy = intimacy,
    diamonds = diamonds,
    personalityTags = personalityTags,
    equippedHead = equippedHead,
    equippedGlasses = equippedGlasses,
    equippedCollar = equippedCollar,
    equippedClothing = equippedClothing,
    equippedTail = equippedTail,
    equippedAccessory = equippedAccessory,
    createdAt = createdAt,
    lastInteractionTime = lastInteractionTime
)
```

- [ ] **Step 3: 创建 PetDao.kt**

```kotlin
// app/src/main/java/com/deskpet/app/data/db/PetDao.kt
package com.deskpet.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.deskpet.app.data.model.PetEntity

@Dao
interface PetDao {

    @Query("SELECT * FROM pet_state WHERE id = 1 LIMIT 1")
    suspend fun getPet(): PetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(pet: PetEntity)

    @Query("SELECT lastInteractionTime FROM pet_state WHERE id = 1 LIMIT 1")
    suspend fun getLastInteractionTime(): Long?
}
```

- [ ] **Step 4: 更新 Converters.kt 添加新类型转换器**

在 `Converters` 类中新增以下方法（保留现有的 MoodLevel 转换器不变）：

```kotlin
// 在 Converters 类内部新增：

@TypeConverter
fun toPetSpecies(value: String): PetSpecies = PetSpecies.valueOf(value)

@TypeConverter
fun fromPetSpecies(species: PetSpecies): String = species.name

@TypeConverter
fun toPetColor(value: String): PetColor = PetColor.valueOf(value)

@TypeConverter
fun fromPetColor(color: PetColor): String = color.name

@TypeConverter
fun toPersonalityTagList(value: String): List<PersonalityTag> =
    if (value.isBlank()) emptyList()
    else value.split(",").map { PersonalityTag.valueOf(it) }

@TypeConverter
fun fromPersonalityTagList(tags: List<PersonalityTag>): String =
    tags.joinToString(",") { it.name }
```

同时在文件顶部添加 import：

```kotlin
import com.deskpet.app.data.model.PetColor
import com.deskpet.app.data.model.PetSpecies
import com.deskpet.app.data.model.PersonalityTag
```

- [ ] **Step 5: 更新 AppDatabase.kt — 新增实体、版本升级、迁移**

将 `AppDatabase` 修改为：

```kotlin
@Database(
    entities = [MoodLog::class, PeriodLog::class, PetEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun moodLogDao(): MoodLogDao
    abstract fun periodLogDao(): PeriodLogDao
    abstract fun petDao(): PetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "deskpet.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS pet_state (
                        id INTEGER NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        species TEXT NOT NULL,
                        color TEXT NOT NULL,
                        level INTEGER NOT NULL,
                        hunger INTEGER NOT NULL,
                        mood INTEGER NOT NULL,
                        intimacy INTEGER NOT NULL,
                        diamonds INTEGER NOT NULL,
                        personalityTags TEXT NOT NULL,
                        equippedHead TEXT,
                        equippedGlasses TEXT,
                        equippedCollar TEXT,
                        equippedClothing TEXT,
                        equippedTail TEXT,
                        equippedAccessory TEXT,
                        createdAt INTEGER NOT NULL,
                        lastInteractionTime INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }
    }
}
```

添加 import：

```kotlin
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.deskpet.app.data.model.PetEntity
```

- [ ] **Step 6: 编译验证**

Run: `cd /workspace/DeskPet-android/DeskPet && ./gradlew assembleDebug 2>&1 | tail -20`
Expected: BUILD SUCCESSFUL（Room KSP 生成 PetDao 实现，无编译错误）

- [ ] **Step 7: Commit**

```bash
cd /workspace/DeskPet-android/DeskPet
git add app/src/main/java/com/deskpet/app/data/model/PetEntity.kt \
        app/src/main/java/com/deskpet/app/data/db/PetDao.kt \
        app/src/main/java/com/deskpet/app/data/db/Converters.kt \
        app/src/main/java/com/deskpet/app/data/db/AppDatabase.kt
git commit -m "feat(L0-1): add PetEntity Room entity, PetDao, and DB migration v1→v2"
```

---

## Task 2: PetRepository 持久化集成 + 离线衰减

**Files:**
- Modify: `app/src/main/java/com/deskpet/app/data/repository/PetRepository.kt`

- [ ] **Step 1: 修改 PetRepository 构造函数，注入 AppDatabase**

将 `PetRepository` 的构造函数改为接收 `AppDatabase`：

```kotlin
class PetRepository private constructor(
    context: Context,
    private val database: AppDatabase
) {
    private val petDao = database.petDao()
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
```

添加 import：

```kotlin
import com.deskpet.app.data.db.AppDatabase
import com.deskpet.app.data.model.PetEntity
import com.deskpet.app.data.model.toEntity
import com.deskpet.app.data.model.toPet
import kotlinx.coroutines.runBlocking
```

- [ ] **Step 2: 修改 _petState 初始化为从 Room 加载**

将 `private val _petState = MutableStateFlow(createDefaultPet())` 替换为：

```kotlin
    private val _petState = MutableStateFlow(loadPersistedPet())
    val petState: StateFlow<Pet> = _petState.asStateFlow()
```

新增 `loadPersistedPet` 方法（在 `createDefaultPet` 上方）：

```kotlin
    /**
     * Loads the persisted pet from Room, applying offline decay.
     * Falls back to a default pet on first launch.
     */
    private fun loadPersistedPet(): Pet {
        val entity = runBlocking { petDao.getPet() }
        return if (entity != null) {
            val decayed = applyOfflineDecay(entity)
            val pet = decayed.toPet()
            // Persist the decayed state so it's not re-applied
            runBlocking { petDao.upsert(decayed) }
            pet
        } else {
            // First launch: create and persist default pet
            val defaultPet = createDefaultPet()
            runBlocking { petDao.upsert(defaultPet.toEntity()) }
            defaultPet
        }
    }
```

- [ ] **Step 3: 实现离线衰减机制**

在 `loadPersistedPet` 下方新增：

```kotlin
    /**
     * Applies offline stat decay based on time since lastInteractionTime.
     *
     * Rules (calculated once at startup):
     * - > 4h: hunger -2 per hour
     * - > 8h: mood -1 per hour
     * - > 24h: intimacy -0.5 per hour (rounded down)
     * - All stats clamped to 0
     */
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
            val intimacyDecay = (elapsedHours - 24) / 2  // -0.5 per hour = 1 per 2 hours
            intimacy = (intimacy - intimacyDecay).coerceAtLeast(0)
        }

        return entity.copy(
            hunger = hunger,
            mood = mood,
            intimacy = intimacy,
            lastInteractionTime = now
        )
    }
```

- [ ] **Step 4: 添加 persistPet 辅助方法**

在 `loadPersistedPet` 上方新增：

```kotlin
    /**
     * Persists the current pet state to Room.
     * Called after every state mutation.
     */
    private fun persistPet() {
        val pet = _petState.value
        runBlocking {
            petDao.upsert(pet.toEntity(lastInteractionTime = System.currentTimeMillis()))
        }
    }
```

- [ ] **Step 5: 在所有状态修改方法中调用 persistPet()**

在每个修改 `_petState` 的方法末尾添加 `persistPet()` 调用：

`feedPet`:
```kotlin
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
```

`petPet`:
```kotlin
    fun petPet() {
        _petState.update { pet ->
            pet.copy(
                mood = (pet.mood + PET_MOOD_GAIN).coerceIn(0, MAX_STAT),
                intimacy = (pet.intimacy + PET_INTIMACY_GAIN).coerceIn(0, MAX_STAT)
            )
        }
        persistPet()
    }
```

`updateMood`:
```kotlin
    fun updateMood(delta: Int) {
        _petState.update { pet ->
            pet.copy(mood = (pet.mood + delta).coerceIn(0, MAX_STAT))
        }
        persistPet()
    }
```

`updateHunger`:
```kotlin
    fun updateHunger(delta: Int) {
        _petState.update { pet ->
            pet.copy(hunger = (pet.hunger + delta).coerceIn(0, MAX_STAT))
        }
        persistPet()
    }
```

`addDiamonds`:
```kotlin
    fun addDiamonds(amount: Int) {
        _petState.update { pet ->
            pet.copy(diamonds = (pet.diamonds + amount).coerceAtLeast(0))
        }
        persistPet()
    }
```

`updatePet`:
```kotlin
    fun updatePet(transform: (Pet) -> Pet) {
        _petState.update(transform)
        persistPet()
    }
```

`purchaseItem` — 在 `saveOwnedOutfits(updated)` 之后添加 `persistPet()`：
```kotlin
        _petState.update { it.copy(diamonds = it.diamonds - item.price) }
        val updated = _ownedOutfits.value + item.id
        _ownedOutfits.value = updated
        saveOwnedOutfits(updated)
        persistPet()
        return true
```

`equipItem` — 在 `return true` 之前添加 `persistPet()`：
```kotlin
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
```

`unequip` — 在方法末尾添加 `persistPet()`：
```kotlin
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
```

- [ ] **Step 6: 更新 getInstance 传入 database**

修改 companion object 的 `getInstance`：

```kotlin
        fun getInstance(context: Context): PetRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: PetRepository(context, AppDatabase.getInstance(context)).also { INSTANCE = it }
            }
```

添加 import：`import com.deskpet.app.data.db.AppDatabase`（如果尚未导入）。

- [ ] **Step 7: 编译验证**

Run: `cd /workspace/DeskPet-android/DeskPet && ./gradlew assembleDebug 2>&1 | tail -20`
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Commit**

```bash
cd /workspace/DeskPet-android/DeskPet
git add app/src/main/java/com/deskpet/app/data/repository/PetRepository.kt
git commit -m "feat(L0-1): integrate Room persistence with offline stat decay in PetRepository"
```

---

## Task 3: 音效系统完善 — 新增 8 种音效

**Files:**
- Modify: `app/src/main/java/com/deskpet/app/util/SoundHelper.kt`
- Modify: `app/src/main/java/com/deskpet/app/DeskPetApplication.kt`
- Modify: `app/src/main/java/com/deskpet/app/service/PetBehaviorEngine.kt`

- [ ] **Step 1: 扩展 SoundType enum**

在 `SoundHelper.kt` 的 `SoundType` enum 中新增条目（保留现有 7 种不变）：

```kotlin
enum class SoundType {
    PET,
    FEED,
    CLICK,
    PURCHASE,
    EQUIP,
    ERROR,
    LEVEL_UP,
    /** App 启动/恢复 — C-E 轻柔上行 */
    GREETING,
    /** 进入 SLEEPY — G-C 下行渐弱 */
    SLEEP,
    /** 从 SLEEPY 恢复 — C-G 上行渐强 */
    WAKE,
    /** 解锁成就 — C-E-G-C 四音琶音 */
    ACHIEVEMENT,
    /** 习惯打卡完成 — E-A 两音轻快上行 */
    CHECKIN,
    /** 旅行出发 — D-F-A 轻快短促 */
    TRAVEL_DEPART,
    /** 旅行归来 — C-E-G-C 温馨和弦 */
    TRAVEL_RETURN
}
```

- [ ] **Step 2: 在 playToneSync 中为新音效添加频率定义**

将 `playToneSync` 的 `when` 表达式扩展（在 `LEVEL_UP` 分支后添加）：

```kotlin
    private fun playToneSync(type: SoundType) {
        val (freqs, durationMs) = when (type) {
            SoundType.PET -> listOf(523.0, 659.0, 784.0) to 180
            SoundType.FEED -> listOf(440.0, 587.0) to 200
            SoundType.CLICK -> listOf(880.0) to 60
            SoundType.PURCHASE -> listOf(659.0, 880.0, 1047.0) to 220
            SoundType.EQUIP -> listOf(587.0, 784.0) to 120
            SoundType.ERROR -> listOf(220.0, 185.0) to 150
            SoundType.LEVEL_UP -> listOf(523.0, 659.0, 784.0, 1047.0) to 300
            SoundType.GREETING -> listOf(523.0, 659.0) to 250
            SoundType.SLEEP -> listOf(784.0, 523.0) to 350
            SoundType.WAKE -> listOf(523.0, 784.0) to 300
            SoundType.ACHIEVEMENT -> listOf(523.0, 659.0, 784.0, 1047.0) to 400
            SoundType.CHECKIN -> listOf(659.0, 880.0) to 150
            SoundType.TRAVEL_DEPART -> listOf(587.0, 698.0, 880.0) to 200
            SoundType.TRAVEL_RETURN -> listOf(523.0, 659.0, 784.0, 1047.0) to 350
        }
```

- [ ] **Step 3: 在 DeskPetApplication.onCreate 中播放 GREETING**

在 `DeskPetApplication.onCreate()` 的 `SoundHelper.setEnabled(soundEnabled)` 之后添加：

```kotlin
        SoundHelper.setEnabled(soundEnabled)
        SoundHelper.play(SoundType.GREETING)
```

添加 import：`import com.deskpet.app.util.SoundType`

- [ ] **Step 4: 在 PetBehaviorEngine 中添加 SLEEP/WAKE 触发**

在 `PetBehaviorEngine.evaluateState()` 中，修改 SLEEPY 切换逻辑：

```kotlin
        when {
            hour in 22..23 || hour in 0..7 -> {
                if (_state.value != PetState.SLEEPY) {
                    _state.value = PetState.SLEEPY
                    SoundHelper.play(SoundType.SLEEP)
                }
            }
            _state.value == PetState.IDLE && Math.random() < 0.15 -> {
                _state.value = PetState.PLAYING
                delay(10_000)
                _state.value = PetState.IDLE
            }
            // Wake up if was sleepy but it's daytime now
            _state.value == PetState.SLEEPY && hour !in 22..23 && hour !in 0..7 -> {
                _state.value = PetState.IDLE
                SoundHelper.play(SoundType.WAKE)
            }
        }
```

添加 import：
```kotlin
import com.deskpet.app.util.SoundHelper
import com.deskpet.app.util.SoundType
```

- [ ] **Step 5: 细分 CLICK 为 TAP_LIGHT / TAP_HEAVY**

在 `SoundType` enum 中，将 `CLICK` 替换为两个新条目（并更新所有引用处）：

```kotlin
    /** 轻触反馈 — 880Hz 短促 */
    TAP_LIGHT,
    /** 长按反馈 — 440Hz 稍长 */
    TAP_HEAVY,
```

在 `playToneSync` 中替换 `CLICK` 分支：

```kotlin
            SoundType.TAP_LIGHT -> listOf(880.0) to 60
            SoundType.TAP_HEAVY -> listOf(440.0) to 100
```

全局搜索 `SoundType.CLICK` 并替换：
- `PetViewModel.kt` 中 `onPhoto()` 的 `SoundHelper.play(SoundType.CLICK)` → `SoundHelper.play(SoundType.TAP_LIGHT)`
- `DressUpScreen.kt` 中所有 `SoundType.CLICK` → `SoundType.TAP_LIGHT`

- [ ] **Step 6: 编译验证**

Run: `cd /workspace/DeskPet-android/DeskPet && ./gradlew assembleDebug 2>&1 | tail -20`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
cd /workspace/DeskPet-android/DeskPet
git add app/src/main/java/com/deskpet/app/util/SoundHelper.kt \
        app/src/main/java/com/deskpet/app/DeskPetApplication.kt \
        app/src/main/java/com/deskpet/app/service/PetBehaviorEngine.kt \
        app/src/main/java/com/deskpet/app/ui/screens/home/PetViewModel.kt \
        app/src/main/java/com/deskpet/app/ui/screens/dressup/DressUpScreen.kt
git commit -m "feat(L0-4): add 8 new sound types (GREETING/SLEEP/WAKE/ACHIEVEMENT/CHECKIN/TRAVEL) and split CLICK into TAP_LIGHT/TAP_HEAVY"
```

---

## Task 4: 装扮系统升级 — OutfitRenderer 矢量渲染

**Files:**
- Create: `app/src/main/java/com/deskpet/app/ui/components/OutfitRenderer.kt`
- Modify: `app/src/main/java/com/deskpet/app/ui/components/PetCanvas.kt`
- Modify: `app/src/main/java/com/deskpet/app/data/model/OutfitItem.kt`
- Modify: `app/src/main/java/com/deskpet/app/ui/screens/dressup/DressUpScreen.kt`

- [ ] **Step 1: 创建 OutfitRenderer.kt**

```kotlin
// app/src/main/java/com/deskpet/app/ui/components/OutfitRenderer.kt
package com.deskpet.app.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.deskpet.app.data.model.OutfitCategory
import com.deskpet.app.data.model.PetSpecies

/**
 * Renders outfits as vector Canvas drawings instead of emoji.
 *
 * Maintains a registry of outfitId → DrawScope extension function.
 * Falls back to emoji rendering for outfits not yet vectorized.
 */
object OutfitRenderer {

    private val GoldColor = Color(0xFFFFD700)
    private val SilverColor = Color(0xFFC0C0C0)
    private val RedColor = Color(0xFFE8392B)
    private val PinkAccent = Color(0xFFFF6B9D)
    private val DarkAccent = Color(0xFF2D2420)

    /**
     * Draws the given outfit onto the canvas.
     *
     * @param outfitId the item id (e.g. "head_bow", "glasses_round")
     * @param category the outfit category (determines position)
     * @param species the pet species (determines position offsets)
     * @param w canvas width
     * @param h canvas height
     * @return true if vector-rendered, false if emoji fallback needed
     */
    fun DrawScope.render(
        outfitId: String,
        category: OutfitCategory,
        species: PetSpecies,
        w: Float,
        h: Float
    ): Boolean {
        val (cx, cy, scale) = getPosition(category, species, w, h)

        val rendered = when (outfitId) {
            // ---- HEAD ----
            "head_bow" -> { drawBow(cx, cy, w * 0.12f, PinkAccent); true }
            "head_flower" -> { drawFlower(cx, cy, w * 0.10f); true }
            "head_crown" -> { drawCrown(cx, cy, w * 0.14f, GoldColor); true }
            "head_beanie" -> { drawBeanie(cx, cy, w * 0.16f, RedColor); true }

            // ---- GLASSES ----
            "glasses_round" -> { drawRoundGlasses(cx, cy, w * 0.12f, DarkAccent); true }
            "glasses_sun" -> { drawSunglasses(cx, cy, w * 0.13f, DarkAccent); true }
            "glasses_heart" -> { drawHeartGlasses(cx, cy, w * 0.11f, PinkAccent); true }

            // ---- COLLAR ----
            "collar_bell" -> { drawBellCollar(cx, cy, w * 0.14f, GoldColor); true }
            "collar_ribbon" -> { drawRibbonCollar(cx, cy, w * 0.13f, PinkAccent); true }

            // ---- CLOTHING ----
            "cloth_scarf" -> { drawScarf(cx, cy, w * 0.18f, RedColor); true }

            // ---- TAIL ----
            "tail_ribbon" -> { drawTailRibbon(cx, cy, w * 0.10f, PinkAccent); true }
            "tail_star" -> { drawStar(cx, cy, w * 0.10f, GoldColor); true }

            // ---- ACCESSORY ----
            "acc_balloon" -> { drawBalloon(cx, cy, w * 0.12f, PinkAccent); true }

            else -> false  // emoji fallback
        }
        return rendered
    }

    /**
     * Returns the (centerX, centerY, scaleFactor) for a given category + species.
     */
    private fun getPosition(
        category: OutfitCategory,
        species: PetSpecies,
        w: Float,
        h: Float
    ): Triple<Float, Float, Float> = when (category) {
        OutfitCategory.HEAD -> when (species) {
            PetSpecies.RABBIT -> Triple(w * 0.5f, h * 0.18f, 0.15f)
            PetSpecies.HAMSTER -> Triple(w * 0.5f, h * 0.02f, 0.16f)
            else -> Triple(w * 0.5f, h * 0.05f, 0.18f)
        }
        OutfitCategory.GLASSES -> when (species) {
            PetSpecies.RABBIT -> Triple(w * 0.5f, h * 0.40f, 0.13f)
            PetSpecies.HAMSTER -> Triple(w * 0.5f, h * 0.36f, 0.13f)
            else -> Triple(w * 0.5f, h * 0.38f, 0.14f)
        }
        OutfitCategory.COLLAR -> when (species) {
            PetSpecies.HAMSTER -> Triple(w * 0.5f, h * 0.82f, 0.13f)
            else -> Triple(w * 0.5f, h * 0.78f, 0.12f)
        }
        OutfitCategory.CLOTHING -> when (species) {
            PetSpecies.HAMSTER -> Triple(w * 0.5f, h * 0.68f, 0.22f)
            PetSpecies.RABBIT -> Triple(w * 0.5f, h * 0.62f, 0.19f)
            else -> Triple(w * 0.5f, h * 0.65f, 0.20f)
        }
        OutfitCategory.TAIL -> Triple(w * 0.88f, h * 0.80f, 0.13f)
        OutfitCategory.ACCESSORY -> Triple(w * 0.13f, h * 0.45f, 0.12f)
    }

    // ============================================= Vector drawing functions

    private fun DrawScope.drawBow(cx: Float, cy: Float, r: Float, color: Color) {
        val path = Path().apply {
            moveTo(cx, cy)
            cubicTo(cx - r, cy - r * 0.6f, cx - r, cy + r * 0.6f, cx, cy)
            cubicTo(cx + r, cy - r * 0.6f, cx + r, cy + r * 0.6f, cx, cy)
            close()
        }
        drawPath(path, color)
        drawCircle(color.darker(), r * 0.2f, Offset(cx, cy))
    }

    private fun DrawScope.drawFlower(cx: Float, cy: Float, r: Float) {
        val petalColor = Color(0xFFFF69B4)
        val centerColor = Color(0xFFFFD700)
        repeat(5) { i ->
            val angle = (i * 72.0 - 90.0) * Math.PI / 180.0
            val px = cx + (r * 0.7f * Math.cos(angle)).toFloat()
            val py = cy + (r * 0.7f * Math.sin(angle)).toFloat()
            drawCircle(petalColor, r * 0.5f, Offset(px, py))
        }
        drawCircle(centerColor, r * 0.35f, Offset(cx, cy))
    }

    private fun DrawScope.drawCrown(cx: Float, cy: Float, r: Float, color: Color) {
        val path = Path().apply {
            moveTo(cx - r, cy + r * 0.5f)
            lineTo(cx - r, cy - r * 0.2f)
            lineTo(cx - r * 0.5f, cy + r * 0.1f)
            lineTo(cx, cy - r * 0.6f)
            lineTo(cx + r * 0.5f, cy + r * 0.1f)
            lineTo(cx + r, cy - r * 0.2f)
            lineTo(cx + r, cy + r * 0.5f)
            close()
        }
        drawPath(path, color)
        // Gems
        drawCircle(Color(0xFFFF6B9D), r * 0.12f, Offset(cx, cy - r * 0.25f))
        drawCircle(Color(0xFF4FC3F7), r * 0.08f, Offset(cx - r * 0.5f, cy))
        drawCircle(Color(0xFF4FC3F7), r * 0.08f, Offset(cx + r * 0.5f, cy))
    }

    private fun DrawScope.drawBeanie(cx: Float, cy: Float, r: Float, color: Color) {
        drawArc(
            color = color,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(cx - r, cy - r),
            size = Size(r * 2, r * 2)
        )
        drawCircle(color.darker(), r * 0.15f, Offset(cx, cy - r))
    }

    private fun DrawScope.drawRoundGlasses(cx: Float, cy: Float, r: Float, color: Color) {
        drawCircle(color, r * 0.5f, Offset(cx - r * 0.55f, cy), style = androidx.compose.ui.graphics.drawscope.Stroke(width = r * 0.1f))
        drawCircle(color, r * 0.5f, Offset(cx + r * 0.55f, cy), style = androidx.compose.ui.graphics.drawscope.Stroke(width = r * 0.1f))
        drawLine(color, Offset(cx - r * 0.05f, cy), Offset(cx + r * 0.05f, cy), r * 0.08f)
    }

    private fun DrawScope.drawSunglasses(cx: Float, cy: Float, r: Float, color: Color) {
        drawRoundRect(
            color = color,
            topLeft = Offset(cx - r, cy - r * 0.3f),
            size = Size(r * 2, r * 0.6f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(r * 0.15f, r * 0.15f)
        )
    }

    private fun DrawScope.drawHeartGlasses(cx: Float, cy: Float, r: Float, color: Color) {
        val leftHeart = Path().apply {
            moveTo(cx - r * 0.55f, cy + r * 0.15f)
            cubicTo(cx - r * 0.55f - r * 0.4f, cy - r * 0.2f, cx - r * 0.55f - r * 0.3f, cy - r * 0.5f, cx - r * 0.55f, cy - r * 0.25f)
            cubicTo(cx - r * 0.55f + r * 0.3f, cy - r * 0.5f, cx - r * 0.55f + r * 0.4f, cy - r * 0.2f, cx - r * 0.55f, cy + r * 0.15f)
            close()
        }
        drawPath(leftHeart, color)
        val rightHeart = Path().apply {
            moveTo(cx + r * 0.55f, cy + r * 0.15f)
            cubicTo(cx + r * 0.55f - r * 0.4f, cy - r * 0.2f, cx + r * 0.55f - r * 0.3f, cy - r * 0.5f, cx + r * 0.55f, cy - r * 0.25f)
            cubicTo(cx + r * 0.55f + r * 0.3f, cy - r * 0.5f, cx + r * 0.55f + r * 0.4f, cy - r * 0.2f, cx + r * 0.55f, cy + r * 0.15f)
            close()
        }
        drawPath(rightHeart, color)
    }

    private fun DrawScope.drawBellCollar(cx: Float, cy: Float, r: Float, color: Color) {
        drawLine(color.darker(), Offset(cx - r, cy), Offset(cx + r, cy), r * 0.12f)
        drawCircle(color, r * 0.3f, Offset(cx, cy + r * 0.2f))
        drawLine(color.darker(), Offset(cx, cy + r * 0.1f), Offset(cx, cy + r * 0.3f), r * 0.06f)
    }

    private fun DrawScope.drawRibbonCollar(cx: Float, cy: Float, r: Float, color: Color) {
        drawLine(color, Offset(cx - r, cy), Offset(cx + r, cy), r * 0.1f)
        drawBow(cx, cy, r * 0.5f, color)
    }

    private fun DrawScope.drawScarf(cx: Float, cy: Float, r: Float, color: Color) {
        val path = Path().apply {
            moveTo(cx - r, cy - r * 0.3f)
            lineTo(cx + r, cy - r * 0.3f)
            lineTo(cx + r * 0.8f, cy + r * 0.5f)
            lineTo(cx + r * 0.3f, cy + r * 0.3f)
            lineTo(cx - r * 0.3f, cy + r * 0.5f)
            lineTo(cx - r * 0.8f, cy + r * 0.3f)
            close()
        }
        drawPath(path, color)
        // Stripes
        drawLine(color.darker(), Offset(cx - r * 0.5f, cy - r * 0.2f), Offset(cx + r * 0.5f, cy - r * 0.2f), r * 0.04f)
    }

    private fun DrawScope.drawTailRibbon(cx: Float, cy: Float, r: Float, color: Color) {
        drawBow(cx, cy, r, color)
    }

    private fun DrawScope.drawStar(cx: Float, cy: Float, r: Float, color: Color) {
        val path = Path()
        for (i in 0..10) {
            val angle = (i * 36.0 - 90.0) * Math.PI / 180.0
            val radius = if (i % 2 == 0) r else r * 0.4f
            val x = cx + (radius * Math.cos(angle)).toFloat()
            val y = cy + (radius * Math.sin(angle)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        drawPath(path, color)
    }

    private fun DrawScope.drawBalloon(cx: Float, cy: Float, r: Float, color: Color) {
        drawOval(color, Offset(cx - r * 0.7f, cy - r), Size(r * 1.4f, r * 1.6f))
        drawLine(color.darker(), Offset(cx, cy + r * 0.6f), Offset(cx, cy + r * 1.5f), r * 0.04f)
    }

    /** Returns a slightly darker version of this color. */
    private fun Color.darker(factor: Float = 0.7f): Color = Color(
        red = (red * factor).coerceIn(0f, 1f),
        green = (green * factor).coerceIn(0f, 1f),
        blue = (blue * factor).coerceIn(0f, 1f),
        alpha = alpha
    )
}
```

- [ ] **Step 2: 修改 PetCanvas 的 outfit overlay 部分**

在 `PetCanvas.kt` 中，找到 `// ---- Outfit overlays ----` 部分，替换为：

```kotlin
        // ---- Outfit overlays ----
        if (outfits.isNotEmpty()) {
            outfits.forEach { (category, outfitId) ->
                // Try vector rendering first; fall back to emoji if not yet vectorized
                val rendered = with(OutfitRenderer) {
                    this@Canvas.render(outfitId, category, species, w, h)
                }
                if (!rendered) {
                    drawIntoCanvas { canvas ->
                        val paint = android.graphics.Paint().apply {
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                        }
                        val (cx, cy, sizeFactor) = when (category) {
                            OutfitCategory.HEAD -> when (species) {
                                PetSpecies.RABBIT -> Triple(w * 0.5f, h * 0.18f, 0.15f)
                                PetSpecies.HAMSTER -> Triple(w * 0.5f, h * 0.02f, 0.16f)
                                else -> Triple(w * 0.5f, h * 0.05f, 0.18f)
                            }
                            OutfitCategory.GLASSES -> when (species) {
                                PetSpecies.RABBIT -> Triple(w * 0.5f, h * 0.40f, 0.13f)
                                PetSpecies.HAMSTER -> Triple(w * 0.5f, h * 0.36f, 0.13f)
                                else -> Triple(w * 0.5f, h * 0.38f, 0.14f)
                            }
                            OutfitCategory.COLLAR -> when (species) {
                                PetSpecies.HAMSTER -> Triple(w * 0.5f, h * 0.82f, 0.13f)
                                else -> Triple(w * 0.5f, h * 0.78f, 0.12f)
                            }
                            OutfitCategory.CLOTHING -> when (species) {
                                PetSpecies.HAMSTER -> Triple(w * 0.5f, h * 0.68f, 0.22f)
                                PetSpecies.RABBIT -> Triple(w * 0.5f, h * 0.62f, 0.19f)
                                else -> Triple(w * 0.5f, h * 0.65f, 0.20f)
                            }
                            OutfitCategory.TAIL -> Triple(w * 0.88f, h * 0.80f, 0.13f)
                            OutfitCategory.ACCESSORY -> Triple(w * 0.13f, h * 0.45f, 0.12f)
                        }
                        paint.textSize = w * sizeFactor
                        val fm = paint.fontMetrics
                        val baseline = cy - (fm.ascent + fm.descent) / 2f
                        canvas.nativeCanvas.drawText(outfitId, cx, baseline, paint)
                    }
                }
            }
        }
```

注意：此时 `outfits` 参数的类型从 `Map<OutfitCategory, String>` 保持不变（String 现在是 outfitId 而非 emoji）。

添加 import：`import com.deskpet.app.ui.components.OutfitRenderer`（如果 `PetCanvas` 和 `OutfitRenderer` 在同一包则不需要）。

- [ ] **Step 3: 修改 OutfitItem.kt 的 equippedOutfitEmojis 函数**

将 `equippedOutfitEmojis` 重命名为 `equippedOutfitIds`，返回 outfitId 而非 emoji：

```kotlin
/**
 * Builds a map of equipped [OutfitCategory] → outfitId from a [Pet]'s equipped
 * item ids.
 *
 * Used by [com.deskpet.app.ui.components.PetCanvas] to render worn outfits.
 */
fun Pet.equippedOutfitIds(catalogue: List<OutfitItem>): Map<OutfitCategory, String> {
    val result = mutableMapOf<OutfitCategory, String>()
    equippedHead?.let { result[OutfitCategory.HEAD] = it }
    equippedGlasses?.let { result[OutfitCategory.GLASSES] = it }
    equippedCollar?.let { result[OutfitCategory.COLLAR] = it }
    equippedClothing?.let { result[OutfitCategory.CLOTHING] = it }
    equippedTail?.let { result[OutfitCategory.TAIL] = it }
    equippedAccessory?.let { result[OutfitCategory.ACCESSORY] = it }
    return result
}
```

保留旧的 `equippedOutfitEmojis` 函数但标记为 deprecated：

```kotlin
@Deprecated("Use equippedOutfitIds instead", ReplaceWith("equippedOutfitIds(catalogue)"))
fun Pet.equippedOutfitEmojis(catalogue: List<OutfitItem>): Map<OutfitCategory, String> =
    equippedOutfitIds(catalogue)
```

- [ ] **Step 4: 更新 PetHomeScreen.kt 中的调用**

将 `pet.equippedOutfitEmojis(...)` 改为 `pet.equippedOutfitIds(...)`：

```kotlin
            val stageOutfits = remember(pet) {
                pet.equippedOutfitIds(DeskPetApplication.get().repository.getOutfitItems())
            }
```

更新 import：`import com.deskpet.app.data.model.equippedOutfitIds`

- [ ] **Step 5: 更新 DressUpScreen.kt 中的调用和预览大小**

搜索 `equippedOutfitEmojis` 并替换为 `equippedOutfitIds`。

将 `DressUpPreview` 中的 `PetCanvas` 的 `Modifier.size(120.dp)` 改为 `Modifier.size(180.dp)`。

- [ ] **Step 6: 更新 OverlayPetContent.kt 中的调用**

搜索 `equippedOutfitEmojis` 并替换为 `equippedOutfitIds`，更新 import。

- [ ] **Step 7: 更新 OnboardingScreen.kt 中的调用**

搜索 `equippedOutfitEmojis` 并替换为 `equippedOutfitIds`，更新 import。

- [ ] **Step 8: 编译验证**

Run: `cd /workspace/DeskPet-android/DeskPet && ./gradlew assembleDebug 2>&1 | tail -20`
Expected: BUILD SUCCESSFUL

- [ ] **Step 9: Commit**

```bash
cd /workspace/DeskPet-android/DeskPet
git add app/src/main/java/com/deskpet/app/ui/components/OutfitRenderer.kt \
        app/src/main/java/com/deskpet/app/ui/components/PetCanvas.kt \
        app/src/main/java/com/deskpet/app/data/model/OutfitItem.kt \
        app/src/main/java/com/deskpet/app/ui/screens/home/PetHomeScreen.kt \
        app/src/main/java/com/deskpet/app/ui/screens/dressup/DressUpScreen.kt \
        app/src/main/java/com/deskpet/app/ui/components/OverlayPetContent.kt \
        app/src/main/java/com/deskpet/app/ui/screens/onboarding/OnboardingScreen.kt
git commit -m "feat(L0-2): add OutfitRenderer for vector outfit drawing, replace emoji rendering with Canvas vectors"
```

---

## Task 5: 拍照功能落地 — PhotoHelper + FileProvider

**Files:**
- Create: `app/src/main/java/com/deskpet/app/util/PhotoHelper.kt`
- Create: `app/src/main/res/xml/file_paths.xml`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/java/com/deskpet/app/ui/screens/home/PetViewModel.kt`

- [ ] **Step 1: 创建 file_paths.xml**

```xml
<!-- app/src/main/res/xml/file_paths.xml -->
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <external-files-path
        name="deskpet_photos"
        path="Pictures/deskpet_photos/" />
</paths>
```

- [ ] **Step 2: 在 AndroidManifest.xml 中注册 FileProvider**

在 `<application>` 标签内，`</application>` 之前添加：

```xml
        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="${applicationId}.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" />
        </provider>
```

- [ ] **Step 3: 创建 PhotoHelper.kt**

```kotlin
// app/src/main/java/com/deskpet/app/util/PhotoHelper.kt
package com.deskpet.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import androidx.core.content.FileProvider
import com.deskpet.app.data.model.OutfitCategory
import com.deskpet.app.data.model.PetColor
import com.deskpet.app.data.model.PetSpecies
import com.deskpet.app.data.model.PetState
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Renders the pet + background + watermark to a Bitmap and saves it
 * to the app's external pictures directory, then launches a share intent.
 */
object PhotoHelper {

    private const val IMAGE_SIZE = 1080

    /**
     * Renders a pet photo and saves it to external storage.
     *
     * @param context  app context
     * @param petColor pet body color
     * @param species  pet species
     * @param petState pet state (affects expression)
     * @param outfits  equipped outfit ids by category
     * @param petName  pet name (for watermark)
     * @return the saved file's content URI, or null on failure
     */
    fun captureAndSave(
        context: Context,
        petColor: PetColor,
        species: PetSpecies,
        petState: PetState,
        outfits: Map<OutfitCategory, String>,
        petName: String
    ): Uri? {
        val bitmap = renderBitmap(petColor, species, petState, outfits, petName)
        return saveBitmap(context, bitmap, petName)
    }

    /**
     * Renders the pet scene to a 1080x1080 Bitmap.
     */
    private fun renderBitmap(
        petColor: PetColor,
        species: PetSpecies,
        petState: PetState,
        outfits: Map<OutfitCategory, String>,
        petName: String
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(IMAGE_SIZE, IMAGE_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val w = IMAGE_SIZE.toFloat()
        val h = IMAGE_SIZE.toFloat()

        // ---- Background gradient circle ----
        val bgPaint = Paint().apply {
            isAntiAlias = true
            shader = android.graphics.LinearGradient(
                w * 0.3f, h * 0.3f,
                w * 0.7f, h * 0.7f,
                intArrayOf(0xFFFFE0EC.toInt(), 0xFFE0F0FF.toInt()),
                null,
                android.graphics.Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(w / 2, h / 2, w * 0.42f, bgPaint)

        // ---- Pet body (simplified vector drawing on Android Canvas) ----
        drawPetOnCanvas(canvas, w, h, petColor, species, petState)

        // ---- Watermark ----
        val watermarkPaint = Paint().apply {
            isAntiAlias = true
            color = 0x88000000.toInt()
            textSize = 36f
            textAlign = Paint.Align.LEFT
        }
        val dateStr = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date())
        canvas.drawText("$petName  $dateStr", 30f, h - 30f, watermarkPaint)

        return bitmap
    }

    /**
     * Draws the pet body on an Android Canvas (not Compose DrawScope).
     * This is a simplified version that mirrors PetCanvas drawing logic.
     */
    private fun drawPetOnCanvas(
        canvas: Canvas,
        w: Float,
        h: Float,
        petColor: PetColor,
        species: PetSpecies,
        petState: PetState
    ) {
        val bodyColor = android.graphics.Color.parseColor(petColor.hex)
        val bodyPaint = Paint().apply { isAntiAlias = true; color = bodyColor }

        // Body — rounded rect centered on canvas
        val bodyRect = RectF(w * 0.25f, h * 0.25f, w * 0.75f, h * 0.80f)
        canvas.drawRoundRect(bodyRect, w * 0.25f, w * 0.25f, bodyPaint)

        // Ears (species-specific)
        when (species) {
            PetSpecies.CAT -> {
                val earPaint = bodyPaint
                // Left ear
                val leftEar = android.graphics.Path()
                leftEar.moveTo(w * 0.28f, h * 0.30f)
                leftEar.lineTo(w * 0.20f, h * 0.15f)
                leftEar.lineTo(w * 0.38f, h * 0.22f)
                leftEar.close()
                canvas.drawPath(leftEar, earPaint)
                // Right ear
                val rightEar = android.graphics.Path()
                rightEar.moveTo(w * 0.72f, h * 0.30f)
                rightEar.lineTo(w * 0.80f, h * 0.15f)
                rightEar.lineTo(w * 0.62f, h * 0.22f)
                rightEar.close()
                canvas.drawPath(rightEar, earPaint)
            }
            PetSpecies.DOG -> {
                val earPaint = Paint().apply {
                    isAntiAlias = true
                    color = (bodyColor and 0xFF000000.toInt()) or
                        ((android.graphics.Color.red(bodyColor) * 0.82f).toInt() shl 16) or
                        ((android.graphics.Color.green(bodyColor) * 0.82f).toInt() shl 8) or
                        (android.graphics.Color.blue(bodyColor) * 0.82f).toInt()
                }
                canvas.drawOval(RectF(w * 0.15f, h * 0.25f, w * 0.30f, h * 0.50f), earPaint)
                canvas.drawOval(RectF(w * 0.70f, h * 0.25f, w * 0.85f, h * 0.50f), earPaint)
            }
            PetSpecies.RABBIT -> {
                canvas.drawOval(RectF(w * 0.38f, h * 0.05f, w * 0.46f, h * 0.30f), bodyPaint)
                canvas.drawOval(RectF(w * 0.54f, h * 0.05f, w * 0.62f, h * 0.30f), bodyPaint)
            }
            PetSpecies.HAMSTER -> {
                canvas.drawCircle(w * 0.35f, h * 0.25f, w * 0.06f, bodyPaint)
                canvas.drawCircle(w * 0.65f, h * 0.25f, w * 0.06f, bodyPaint)
            }
        }

        // Eyes
        val eyeWhitePaint = Paint().apply { isAntiAlias = true; color = 0xFFFFFFFF.toInt() }
        val eyePupilPaint = Paint().apply { isAntiAlias = true; color = 0xFF2D2420.toInt() }
        val eyeY = h * 0.45f
        val isSleepy = petState == PetState.SLEEPY
        if (isSleepy) {
            val linePaint = Paint().apply { isAntiAlias = true; color = 0xFFFFFFFF.toInt(); strokeWidth = 6f }
            canvas.drawLine(w * 0.32f, eyeY, w * 0.40f, eyeY, linePaint)
            canvas.drawLine(w * 0.60f, eyeY, w * 0.68f, eyeY, linePaint)
        } else {
            canvas.drawCircle(w * 0.38f, eyeY, w * 0.04f, eyeWhitePaint)
            canvas.drawCircle(w * 0.62f, eyeY, w * 0.04f, eyeWhitePaint)
            canvas.drawCircle(w * 0.38f, eyeY, w * 0.02f, eyePupilPaint)
            canvas.drawCircle(w * 0.62f, eyeY, w * 0.02f, eyePupilPaint)
        }

        // Blush
        val blushPaint = Paint().apply { isAntiAlias = true; color = 0x66F4A7B9.toInt() }
        canvas.drawCircle(w * 0.30f, h * 0.55f, w * 0.05f, blushPaint)
        canvas.drawCircle(w * 0.70f, h * 0.55f, w * 0.05f, blushPaint)

        // Mouth (simplified — happy arc)
        val mouthPaint = Paint().apply {
            isAntiAlias = true
            color = 0xFFFFFFFF.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 5f
        }
        val mouthPath = android.graphics.Path()
        when (petState) {
            PetState.HAPPY, PetState.EXCITED -> {
                mouthPath.moveTo(w * 0.44f, h * 0.62f)
                mouthPath.quadTo(w * 0.50f, h * 0.68f, w * 0.56f, h * 0.62f)
            }
            else -> {
                mouthPath.moveTo(w * 0.46f, h * 0.63f)
                mouthPath.lineTo(w * 0.54f, h * 0.63f)
            }
        }
        canvas.drawPath(mouthPath, mouthPaint)
    }

    /**
     * Saves the bitmap to the app's external pictures directory and returns a
     * content URI via FileProvider.
     */
    private fun saveBitmap(context: Context, bitmap: Bitmap, petName: String): Uri? {
        val dir = File(
            context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES),
            "deskpet_photos"
        )
        if (!dir.exists()) dir.mkdirs()

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(dir, "deskpet_${timestamp}.png")

        return runCatching {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        }.getOrNull()
    }

    /**
     * Launches the system share sheet with the given image URI.
     */
    fun launchShare(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "分享合影"))
    }
}
```

- [ ] **Step 4: 修改 PetViewModel.onPhoto() 实现真实拍照**

在 `PetViewModel.kt` 中，替换 `onPhoto()` 方法：

```kotlin
    /** 拍照: render pet to Bitmap, save, and share. */
    fun onPhoto() {
        val pet = repository.getPet()
        val outfits = pet.equippedOutfitIds(
            getApplication<DeskPetApplication>().repository.getOutfitItems()
        )
        val context = getApplication<Application>()

        viewModelScope.launch {
            val uri = PhotoHelper.captureAndSave(
                context = context,
                petColor = pet.color,
                species = pet.species,
                petState = _petState.value,
                outfits = outfits,
                petName = pet.name
            )

            // Flash animation
            _flash.value = true
            delay(400)
            _flash.value = false

            if (uri != null) {
                _toast.value = "合影已保存"
                PhotoHelper.launchShare(context, uri)
            } else {
                _toast.value = "保存失败，请重试"
            }
            delay(2000)
            _toast.value = null
        }
    }
```

添加 import：
```kotlin
import com.deskpet.app.util.PhotoHelper
import com.deskpet.app.data.model.equippedOutfitIds
```

- [ ] **Step 5: 编译验证**

Run: `cd /workspace/DeskPet-android/DeskPet && ./gradlew assembleDebug 2>&1 | tail -20`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
cd /workspace/DeskPet-android/DeskPet
git add app/src/main/res/xml/file_paths.xml \
        app/src/main/AndroidManifest.xml \
        app/src/main/java/com/deskpet/app/util/PhotoHelper.kt \
        app/src/main/java/com/deskpet/app/ui/screens/home/PetViewModel.kt
git commit -m "feat(L0-3): implement real photo capture with Bitmap rendering, FileProvider, and share"
```

---

## Task 6: 全量编译 + APK 构建

**Files:** 无修改，仅验证

- [ ] **Step 1: 清理并全量编译**

Run: `cd /workspace/DeskPet-android/DeskPet && ./gradlew clean assembleDebug 2>&1 | tail -30`
Expected: BUILD SUCCESSFUL, APK 生成在 `app/build/outputs/apk/debug/app-debug.apk`

- [ ] **Step 2: 检查 APK 文件**

Run: `ls -lh /workspace/DeskPet-android/DeskPet/app/build/outputs/apk/debug/app-debug.apk`
Expected: 文件存在，大小 > 5MB

- [ ] **Step 3: 最终 Commit**

```bash
cd /workspace/DeskPet-android/DeskPet
git add -A
git commit -m "chore(L0): final build verification — L0 foundation layer complete"
```

---

## 自检清单

- [x] L0-1 宠物状态持久化 — Task 1 + Task 2 覆盖
- [x] L0-2 装扮系统升级 — Task 4 覆盖
- [x] L0-3 拍照功能落地 — Task 5 覆盖
- [x] L0-4 音效系统完善 — Task 3 覆盖
- [x] 所有新建文件路径准确
- [x] 所有修改文件路径准确
- [x] 无占位符或 TODO
- [x] 类型一致性：PetEntity.toPet() / Pet.toEntity() 在 Task 1 定义，Task 2 使用
- [x] 方法签名一致：equippedOutfitIds() 在 Task 4 定义后统一替换所有引用
- [x] SoundType 新增条目在 Task 3 定义后，触发点在 Task 3 内完整接入
