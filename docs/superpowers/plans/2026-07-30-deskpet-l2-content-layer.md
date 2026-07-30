# L2 内容丰富层 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement the L2 content layer — pet home decoration (小窝), travel idle system, achievement/codex, and voice TTS interaction — building on the L0 persistence and L1 emotional engine foundations.

**Architecture:** New Room entities (RoomLayout, TravelLog, Postcard, AchievementRecord) with DB migration v5→v9. New engine classes (TravelEngine, AchievementEngine, SpeechHelper) handle business logic. New Canvas renderers (FurnitureRenderer, RoomSceneCanvas, PostcardRenderer) for visual content. Four new screens (DecorScreen, TravelScreen, CodexScreen) integrate with existing navigation.

**Tech Stack:** Kotlin, Jetpack Compose, Room 2.6.1, TextToSpeech (Android native), existing PetRepository/PetBehaviorEngine/SoundHelper/NotificationHelper/PhotoHelper

---

## File Structure Overview

### New Files

| File | Responsibility |
|------|---------------|
| `data/model/FurnitureItem.kt` | Furniture data model + FurnitureCategory enum |
| `data/model/RoomLayout.kt` | Room entity for furniture placement persistence |
| `data/model/TravelDestination.kt` | Travel destination data model + gift pool |
| `data/model/TravelLog.kt` | Room entity for travel records |
| `data/model/Postcard.kt` | Room entity for postcard collection |
| `data/model/AchievementRecord.kt` | Room entity for unlocked achievements |
| `data/db/RoomLayoutDao.kt` | DAO for room layout CRUD |
| `data/db/TravelLogDao.kt` | DAO for travel records |
| `data/db/PostcardDao.kt` | DAO for postcard collection |
| `data/db/AchievementDao.kt` | DAO for achievement records |
| `service/TravelEngine.kt` | Travel logic: start, check return, generate gifts/postcards |
| `service/AchievementEngine.kt` | Achievement detection + codex data derivation |
| `util/SpeechHelper.kt` | TextToSpeech wrapper with scene-based dialogue |
| `util/DialogueBank.kt` | Scene-based dialogue pools by personality tag |
| `ui/components/RoomSceneCanvas.kt` | Canvas: room background + furniture rendering |
| `ui/components/FurnitureRenderer.kt` | Vector drawing functions for furniture items |
| `ui/components/PostcardCanvas.kt` | Canvas: postcard scene rendering |
| `ui/screens/decor/DecorScreen.kt` | Home decoration screen (furniture shop + placement) |
| `ui/screens/decor/DecorViewModel.kt` | ViewModel for decor screen |
| `ui/screens/travel/TravelScreen.kt` | Travel selection + status + postcard gallery |
| `ui/screens/travel/TravelViewModel.kt` | ViewModel for travel screen |
| `ui/screens/codex/CodexScreen.kt` | Achievement + collection codex screen |
| `ui/screens/codex/CodexViewModel.kt` | ViewModel for codex screen |

### Modified Files

| File | Changes |
|------|---------|
| `data/db/AppDatabase.kt` | Add 4 entities, bump to v9, add migrations v5→v9 |
| `data/db/Converters.kt` | Add converters for new enums |
| `data/model/PetSettings.kt` | Add ttsEnabled, travelEnabled fields |
| `data/repository/PetRepository.kt` | Add furniture purchase/place/remove, travel methods, achievement check |
| `data/model/Pet.kt` | Add isTraveling flag (transient, not persisted in PetEntity) |
| `ui/components/BottomNav.kt` | Add DECOR route to PetDestinations |
| `ui/navigation/NavGraph.kt` | Add decor/travel/codex composables |
| `ui/screens/home/PetHomeScreen.kt` | Add RoomSceneCanvas background, travel entry, TTS triggers |
| `ui/screens/home/PetViewModel.kt` | Add travel/env/speech integration |
| `ui/screens/settings/SettingsScreen.kt` | Add TTS toggle, travel toggle |
| `service/PetBehaviorEngine.kt`` | Add furniture comfort/fun effects on behavior |
| `DeskPetApplication.kt` | Init SpeechHelper, check travel return on launch |
| `app/build.gradle.kts` | No new dependencies (TTS is Android native) |

---

## Part A: L2-1 家居装饰 / 宠物小窝

### Task 1: FurnitureItem 数据模型 + RoomLayout 实体 + DAO

**Files:**
- Create: `app/src/main/java/com/deskpet/app/data/model/FurnitureItem.kt`
- Create: `app/src/main/java/com/deskpet/app/data/model/RoomLayout.kt`
- Create: `app/src/main/java/com/deskpet/app/data/db/RoomLayoutDao.kt`

- [ ] **Step 1: 创建 FurnitureItem.kt**

```kotlin
// app/src/main/java/com/deskpet/app/data/model/FurnitureItem.kt
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
    val fun: Int = 0,
    val beauty: Int = 0
)
```

- [ ] **Step 2: 创建 RoomLayout.kt (Room entity)**

```kotlin
// app/src/main/java/com/deskpet/app/data/model/RoomLayout.kt
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
```

- [ ] **Step 3: 创建 RoomLayoutDao.kt**

```kotlin
// app/src/main/java/com/deskpet/app/data/db/RoomLayoutDao.kt
package com.deskpet.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.deskpet.app.data.model.RoomLayout
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomLayoutDao {

    @Query("SELECT * FROM room_layout ORDER BY slotIndex ASC")
    fun getAll(): Flow<List<RoomLayout>>

    @Query("SELECT * FROM room_layout ORDER BY slotIndex ASC")
    suspend fun getAllOnce(): List<RoomLayout>

    @Query("SELECT * FROM room_layout WHERE slotIndex = :slotIndex LIMIT 1")
    suspend fun getBySlot(slotIndex: Int): RoomLayout?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(layout: RoomLayout)

    @Query("DELETE FROM room_layout WHERE slotIndex = :slotIndex")
    suspend fun removeSlot(slotIndex: Int)

    @Query("DELETE FROM room_layout")
    suspend fun clearAll()
}
```

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/deskpet/app/data/model/FurnitureItem.kt \
  app/src/main/java/com/deskpet/app/data/model/RoomLayout.kt \
  app/src/main/java/com/deskpet/app/data/db/RoomLayoutDao.kt
git commit -m "feat(L2-1): add FurnitureItem model, RoomLayout entity and DAO"
```

---

### Task 2: AppDatabase 升级 v5→v6 + RoomLayout 集成

**Files:**
- Modify: `app/src/main/java/com/deskpet/app/data/db/AppDatabase.kt`

- [ ] **Step 1: 添加 RoomLayout 到 entities 数组, bump version to 6**

In `AppDatabase.kt`, update the `@Database` annotation:

```kotlin
@Database(
    entities = [MoodLog::class, PeriodLog::class, PetEntity::class,
                InteractionLog::class, PetDiary::class, HabitStreak::class, EnvCache::class,
                RoomLayout::class],
    version = 6,
    exportSchema = false
)
```

- [ ] **Step 2: 添加 DAO 抽象方法**

```kotlin
abstract fun roomLayoutDao(): RoomLayoutDao
```

- [ ] **Step 3: 添加 MIGRATION_5_6**

```kotlin
private val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS room_layout (
                slotIndex INTEGER NOT NULL PRIMARY KEY,
                furnitureId TEXT NOT NULL,
                placedAt INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())
    }
}
```

- [ ] **Step 4: 更新 getInstance 迁移链**

Change `.addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)` to:
```kotlin
.addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
```

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/deskpet/app/data/db/AppDatabase.kt
git commit -m "feat(L2-1): add RoomLayout entity, migrate DB to v6"
```

---

### Task 3: FurnitureRenderer + RoomSceneCanvas — 房间场景渲染

**Files:**
- Create: `app/src/main/java/com/deskpet/app/ui/components/FurnitureRenderer.kt`
- Create: `app/src/main/java/com/deskpet/app/ui/components/RoomSceneCanvas.kt`

- [ ] **Step 1: 创建 FurnitureRenderer.kt**

```kotlin
// app/src/main/java/com/deskpet/app/ui/components/FurnitureRenderer.kt
package com.deskpet.app.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Renders furniture items as vector graphics on a room canvas.
 * Each function returns true if rendered, false to fall back to emoji.
 */
object FurnitureRenderer {

    /**
     * Renders a furniture item by its ID. Returns true if vector-rendered.
     */
    fun DrawScope.render(furnitureId: String, slotIndex: Int, w: Float, h: Float): Boolean {
        return when (furnitureId) {
            // Wallpaper
            "wall_pink" -> { drawWallpaper(Color(0xFFFFE0E0), Color(0xFFFFC0CB), w, h); true }
            "wall_mint" -> { drawWallpaper(Color(0xFFE0F5E0), Color(0xFFB0E0B0), w, h); true }
            "wall_sky" -> { drawWallpaper(Color(0xFFE0ECF5), Color(0xFFB0CCE0), w, h); true }
            // Floor
            "floor_wood" -> { drawFloor(Color(0xFFD2B48C), Color(0xFFC19A6B), w, h); true }
            "floor_tile" -> { drawFloor(Color(0xFFF0F0F0), Color(0xFFD0D0D0), w, h); true }
            "floor_carpet" -> { drawFloor(Color(0xFFE8C4D4), Color(0xFFD4A4C0), w, h); true }
            // Bed
            "bed_round" -> { drawRoundBed(w, h); true }
            "bed_cushion" -> { drawCushionBed(w, h); true }
            "bed_basket" -> { drawBasketBed(w, h); true }
            "bed_canopy" -> { drawCanopyBed(w, h); true }
            // Table
            "table_small" -> { drawSmallTable(w, h); true }
            "table_round" -> { drawRoundTable(w, h); true }
            "table_desk" -> { drawDesk(w, h); true }
            "table_shelf" -> { drawShelf(w, h); true }
            // Decoration
            "decor_plant" -> { drawPlant(w, h); true }
            "decor_lamp" -> { drawLamp(w, h); true }
            "decor_frame" -> { drawFrame(w, h); true }
            "decor_mirror" -> { drawMirror(w, h); true }
            "decor_clock" -> { drawClock(w, h); true }
            "decor_vase" -> { drawVase(w, h); true }
            // Toy
            "toy_ball" -> { drawBall(w, h); true }
            "toy_yarn" -> { drawYarn(w, h); true }
            "toy_mouse" -> { drawToyMouse(w, h); true }
            "toy_feather" -> { drawFeather(w, h); true }
            else -> false
        }
    }

    // --- Wallpaper ---
    private fun DrawScope.drawWallpaper(c1: Color, c2: Color, w: Float, h: Float) {
        val wallH = h * 0.6f
        drawRect(c1, topLeft = Offset(0f, 0f), size = Size(w, wallH))
        // Polka dot pattern
        val dotColor = c2.copy(alpha = 0.5f)
        val spacing = w / 8f
        var row = 0
        var y = spacing * 0.5f
        while (y < wallH) {
            var x = spacing * 0.5f + (if (row % 2 == 0) 0f else spacing * 0.5f)
            while (x < w) {
                drawCircle(dotColor, radius = spacing * 0.12f, center = Offset(x, y))
                x += spacing
            }
            row++
            y += spacing
        }
    }

    // --- Floor ---
    private fun DrawScope.drawFloor(c1: Color, c2: Color, w: Float, h: Float) {
        val floorY = h * 0.6f
        drawRect(c1, topLeft = Offset(0f, floorY), size = Size(w, h - floorY))
        // Plank lines
        val lineColor = c2.copy(alpha = 0.6f)
        val plankH = (h - floorY) / 4f
        for (i in 1 until 4) {
            val y = floorY + plankH * i
            drawLine(lineColor, Offset(0f, y), Offset(w, y), strokeWidth = 2f)
        }
    }

    // --- Bed ---
    private fun DrawScope.drawRoundBed(w: Float, h: Float) {
        val cx = w * 0.3f
        val cy = h * 0.78f
        val r = w * 0.12f
        drawOval(
            color = Color(0xFFE8C4D4),
            topLeft = Offset(cx - r, cy - r * 0.6f),
            size = Size(r * 2, r * 1.2f)
        )
        drawOval(
            color = Color(0xFFFFF0F5),
            topLeft = Offset(cx - r * 0.7f, cy - r * 0.4f),
            size = Size(r * 1.4f, r * 0.8f)
        )
    }

    private fun DrawScope.drawCushionBed(w: Float, h: Float) {
        val x = w * 0.2f
        val y = h * 0.75f
        val bw = w * 0.2f
        val bh = h * 0.06f
        drawRoundRect(
            color = Color(0xFFFFB6C1),
            topLeft = Offset(x, y),
            size = Size(bw, bh),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(bh * 0.5f, bh * 0.5f)
        )
        drawRoundRect(
            color = Color(0xFFFFE4E1),
            topLeft = Offset(x + bw * 0.1f, y + bh * 0.2f),
            size = Size(bw * 0.8f, bh * 0.6f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(bh * 0.3f, bh * 0.3f)
        )
    }

    private fun DrawScope.drawBasketBed(w: Float, h: Float) {
        val cx = w * 0.3f
        val cy = h * 0.78f
        val bw = w * 0.18f
        val bh = h * 0.08f
        drawOval(
            color = Color(0xFFC19A6B),
            topLeft = Offset(cx - bw * 0.5f, cy - bh * 0.5f),
            size = Size(bw, bh)
        )
        drawOval(
            color = Color(0xFFD2B48C),
            topLeft = Offset(cx - bw * 0.4f, cy - bh * 0.35f),
            size = Size(bw * 0.8f, bh * 0.7f)
        )
    }

    private fun DrawScope.drawCanopyBed(w: Float, h: Float) {
        val x = w * 0.22f
        val y = h * 0.7f
        val bw = w * 0.16f
        // Canopy top
        drawArc(
            color = Color(0xFFE6B8D4),
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(x, y - h * 0.05f),
            size = Size(bw, h * 0.1f)
        )
        // Bed base
        drawRoundRect(
            color = Color(0xFFFFC0CB),
            topLeft = Offset(x, y),
            size = Size(bw, h * 0.05f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
        )
    }

    // --- Table ---
    private fun DrawScope.drawSmallTable(w: Float, h: Float) {
        val x = w * 0.6f
        val y = h * 0.78f
        val tw = w * 0.1f
        val th = h * 0.04f
        drawRoundRect(
            color = Color(0xFF8B4513),
            topLeft = Offset(x, y),
            size = Size(tw, th),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )
        // Legs
        drawLine(Color(0xFF8B4513), Offset(x + 2f, y + th), Offset(x + 2f, y + th + h * 0.06f), 3f)
        drawLine(Color(0xFF8B4513), Offset(x + tw - 2f, y + th), Offset(x + tw - 2f, y + th + h * 0.06f), 3f)
    }

    private fun DrawScope.drawRoundTable(w: Float, h: Float) {
        val cx = w * 0.65f
        val cy = h * 0.8f
        val r = w * 0.06f
        drawOval(
            color = Color(0xFFD2B48C),
            topLeft = Offset(cx - r, cy - r * 0.3f),
            size = Size(r * 2, r * 0.6f)
        )
        drawLine(Color(0xFF8B4513), Offset(cx, cy + r * 0.3f), Offset(cx, cy + r * 0.3f + h * 0.05f), 3f)
    }

    private fun DrawScope.drawDesk(w: Float, h: Float) {
        val x = w * 0.58f
        val y = h * 0.76f
        val dw = w * 0.14f
        val dh = h * 0.03f
        drawRect(Color(0xFF6B4226), Offset(x, y), Size(dw, dh))
        drawRect(Color(0xFF6B4226), Offset(x, y + dh), Size(w * 0.02f, h * 0.08f))
        drawRect(Color(0xFF6B4226), Offset(x + dw - w * 0.02f, y + dh), Size(w * 0.02f, h * 0.08f))
    }

    private fun DrawScope.drawShelf(w: Float, h: Float) {
        val x = w * 0.6f
        val y = h * 0.68f
        val sw = w * 0.12f
        drawRect(Color(0xFF8B4513), Offset(x, y), Size(sw, h * 0.02f))
        drawRect(Color(0xFF8B4513), Offset(x, y + h * 0.04f), Size(sw, h * 0.02f))
    }

    // --- Decoration ---
    private fun DrawScope.drawPlant(w: Float, h: Float) {
        val cx = w * 0.85f
        val cy = h * 0.78f
        // Pot
        drawRoundRect(
            color = Color(0xFF8B4513),
            Offset(cx - w * 0.03f, cy),
            Size(w * 0.06f, h * 0.05f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )
        // Leaves
        drawCircle(Color(0xFF4CAF50), w * 0.035f, Offset(cx - w * 0.02f, cy - h * 0.02f))
        drawCircle(Color(0xFF66BB6A), w * 0.03f, Offset(cx + w * 0.01f, cy - h * 0.03f))
        drawCircle(Color(0xFF81C784), w * 0.025f, Offset(cx, cy - h * 0.04f))
    }

    private fun DrawScope.drawLamp(w: Float, h: Float) {
        val cx = w * 0.15f
        val cy = h * 0.72f
        // Shade
        val path = Path().apply {
            moveTo(cx - w * 0.03f, cy)
            lineTo(cx + w * 0.03f, cy)
            lineTo(cx + w * 0.02f, cy - h * 0.04f)
            lineTo(cx - w * 0.02f, cy - h * 0.04f)
            close()
        }
        drawPath(path, Color(0xFFFFF9C4))
        // Pole
        drawLine(Color(0xFF888888), Offset(cx, cy), Offset(cx, cy + h * 0.06f), 3f)
        // Base
        drawRoundRect(
            color = Color(0xFF888888),
            Offset(cx - w * 0.025f, cy + h * 0.06f),
            Size(w * 0.05f, h * 0.01f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
        )
    }

    private fun DrawScope.drawFrame(w: Float, h: Float) {
        val x = w * 0.08f
        val y = h * 0.18f
        val fw = w * 0.08f
        val fh = h * 0.1f
        drawRect(Color(0xFFD4A4C0), Offset(x, y), Size(fw, fh))
        drawRect(Color(0xFFFFFFFF), Offset(x + w * 0.008f, y + h * 0.01f), Size(fw - w * 0.016f, fh - h * 0.02f))
    }

    private fun DrawScope.drawMirror(w: Float, h: Float) {
        val cx = w * 0.12f
        val cy = h * 0.22f
        val r = w * 0.04f
        drawCircle(Color(0xFFB0C4DE), r, Offset(cx, cy))
        drawCircle(Color(0xFFE0E0E0), r * 0.8f, Offset(cx, cy))
    }

    private fun DrawScope.drawClock(w: Float, h: Float) {
        val cx = w * 0.88f
        val cy = h * 0.15f
        val r = w * 0.03f
        drawCircle(Color(0xFF8B4513), r, Offset(cx, cy))
        drawCircle(Color(0xFFFFF8DC), r * 0.8f, Offset(cx, cy))
        drawLine(Color(0xFF333333), Offset(cx, cy), Offset(cx, cy - r * 0.6f), 2f)
        drawLine(Color(0xFF333333), Offset(cx, cy), Offset(cx + r * 0.5f, cy), 2f)
    }

    private fun DrawScope.drawVase(w: Float, h: Float) {
        val cx = w * 0.5f
        val cy = h * 0.82f
        val path = Path().apply {
            moveTo(cx - w * 0.02f, cy)
            cubicTo(cx - w * 0.04f, cy + h * 0.03f, cx - w * 0.03f, cy + h * 0.05f, cx, cy + h * 0.05f)
            cubicTo(cx + w * 0.03f, cy + h * 0.05f, cx + w * 0.04f, cy + h * 0.03f, cx + w * 0.02f, cy)
            close()
        }
        drawPath(path, Color(0xFF64B5F6))
    }

    // --- Toy ---
    private fun DrawScope.drawBall(w: Float, h: Float) {
        val cx = w * 0.75f
        val cy = h * 0.85f
        val r = w * 0.025f
        drawCircle(Color(0xFFE53935), r, Offset(cx, cy))
        drawLine(Color(0xFFFFFFFF), Offset(cx - r, cy), Offset(cx + r, cy), 2f)
        drawLine(Color(0xFFFFFFFF), Offset(cx, cy - r), Offset(cx, cy + r), 2f)
    }

    private fun DrawScope.drawYarn(w: Float, h: Float) {
        val cx = w * 0.8f
        val cy = h * 0.85f
        val r = w * 0.03f
        drawCircle(Color(0xFFAB47BC), r, Offset(cx, cy))
        // Yarn lines
        for (i in 0..5) {
            val angle = i * 60f
            val rad = Math.toRadians(angle.toDouble())
            val x1 = cx + (r * Math.cos(rad)).toFloat()
            val y1 = cy + (r * Math.sin(rad)).toFloat()
            val x2 = cx + (r * Math.cos(rad + Math.PI)).toFloat()
            val y2 = cy + (r * Math.sin(rad + Math.PI)).toFloat()
            drawLine(Color(0xFFCE93D8), Offset(x1, y1), Offset(x2, y2), 1.5f)
        }
    }

    private fun DrawScope.drawToyMouse(w: Float, h: Float) {
        val cx = w * 0.78f
        val cy = h * 0.87f
        val bw = w * 0.025f
        drawOval(Color(0xFF9E9E9E), Offset(cx - bw, cy - bw * 0.6f), Size(bw * 2, bw * 1.2f))
        // Ears
        drawCircle(Color(0xFF9E9E9E), bw * 0.4f, Offset(cx - bw * 0.6f, cy - bw * 0.5f))
        drawCircle(Color(0xFF9E9E9E), bw * 0.4f, Offset(cx + bw * 0.6f, cy - bw * 0.5f))
        // Tail
        drawLine(Color(0xFF9E9E9E), Offset(cx + bw, cy), Offset(cx + bw * 2f, cy + bw), 2f)
    }

    private fun DrawScope.drawFeather(w: Float, h: Float) {
        val cx = w * 0.82f
        val cy = h * 0.83f
        val path = Path().apply {
            moveTo(cx, cy + h * 0.04f)
            cubicTo(cx - w * 0.03f, cy, cx - w * 0.02f, cy - h * 0.04f, cx, cy - h * 0.05f)
            cubicTo(cx + w * 0.02f, cy - h * 0.04f, cx + w * 0.03f, cy, cx, cy + h * 0.04f)
            close()
        }
        drawPath(path, Color(0xFFEF5350))
        drawLine(Color(0xFF8B4513), Offset(cx, cy + h * 0.04f), Offset(cx, cy + h * 0.08f), 2f)
    }
}
```

- [ ] **Step 2: 创建 RoomSceneCanvas.kt**

```kotlin
// app/src/main/java/com/deskpet/app/ui/components/RoomSceneCanvas.kt
package com.deskpet.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.intoCanvas
import com.deskpet.app.data.model.FurnitureCategory
import com.deskpet.app.data.model.FurnitureItem
import com.deskpet.app.data.model.RoomLayout

/**
 * Renders the pet's room scene: wallpaper, floor, placed furniture, and pet.
 * The pet is drawn as an overlay by the caller (PetCanvas) on top of this.
 */
@Composable
fun RoomSceneCanvas(
    modifier: Modifier = Modifier,
    layout: List<RoomLayout>,
    furnitureCatalogue: List<FurnitureItem>,
    showDefaultBackground: Boolean = true
) {
    val furnitureMap = remember(furnitureCatalogue) {
        furnitureCatalogue.associateBy { it.id }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.3f)
    ) {
        val w = this.size.width
        val h = this.size.height

        // Draw placed furniture by slot index
        layout.sortedBy { it.slotIndex }.forEach { placed ->
            val furniture = furnitureMap[placed.furnitureId] ?: return@forEach
            val rendered = with(FurnitureRenderer) {
                this@Canvas.render(furniture.id, placed.slotIndex, w, h)
            }
            // Fallback to emoji if vector not available
            if (!rendered) {
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        textSize = w * 0.08f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    val positions = mapOf(
                        0 to Pair(w * 0.5f, h * 0.3f),   // wallpaper center
                        1 to Pair(w * 0.5f, h * 0.8f),   // floor center
                        2 to Pair(w * 0.3f, h * 0.78f),  // bed
                        3 to Pair(w * 0.65f, h * 0.8f),  // table
                        4 to Pair(w * 0.85f, h * 0.78f), // decor 1
                        5 to Pair(w * 0.15f, h * 0.75f), // decor 2
                        6 to Pair(w * 0.75f, h * 0.85f), // toy 1
                        7 to Pair(w * 0.82f, h * 0.85f)  // toy 2
                    )
                    val (cx, cy) = positions[placed.slotIndex] ?: Pair(w * 0.5f, h * 0.5f)
                    canvas.nativeCanvas.drawText(furniture.emoji, cx, cy, paint)
                }
            }
        }
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/deskpet/app/ui/components/FurnitureRenderer.kt \
  app/src/main/java/com/deskpet/app/ui/components/RoomSceneCanvas.kt
git commit -m "feat(L2-1): add FurnitureRenderer and RoomSceneCanvas for room rendering"
```

---

### Task 4: PetRepository 家具系统方法 + 家具目录

**Files:**
- Modify: `app/src/main/java/com/deskpet/app/data/repository/PetRepository.kt`

- [ ] **Step 1: 添加家具目录常量和拥有状态持久化**

In `PetRepository.kt`, add:

```kotlin
// --- Furniture System ---

val FURNITURE_CATALOGUE: List<FurnitureItem> = listOf(
    // Wallpaper (3)
    FurnitureItem("wall_pink", FurnitureCategory.WALLPAPER, "粉色墙纸", "🩷", 0, 1, 0, 0, 5),
    FurnitureItem("wall_mint", FurnitureCategory.WALLPAPER, "薄荷墙纸", "🌿", 80, 3, 0, 0, 8),
    FurnitureItem("wall_sky", FurnitureCategory.WALLPAPER, "天空墙纸", "☁️", 150, 5, 0, 0, 12),
    // Floor (3)
    FurnitureItem("floor_wood", FurnitureCategory.FLOOR, "木地板", "🪵", 0, 1, 3, 0, 0),
    FurnitureItem("floor_tile", FurnitureCategory.FLOOR, "瓷砖地板", "⬜", 60, 2, 2, 0, 2),
    FurnitureItem("floor_carpet", FurnitureCategory.FLOOR, "地毯地板", "🟥", 120, 4, 8, 0, 5),
    // Bed (4)
    FurnitureItem("bed_round", FurnitureCategory.BED, "圆形猫床", "🛏️", 100, 1, 10, 0, 0),
    FurnitureItem("bed_cushion", FurnitureCategory.BED, "软垫床", "🧸", 200, 3, 15, 0, 3),
    FurnitureItem("bed_basket", FurnitureCategory.BED, "藤篮床", "🧺", 350, 5, 20, 2, 5),
    FurnitureItem("bed_canopy", FurnitureCategory.BED, "帷幔床", "👒", 600, 8, 30, 5, 10),
    // Table (4)
    FurnitureItem("table_small", FurnitureCategory.TABLE, "小方桌", "🪑", 80, 1, 0, 0, 3),
    FurnitureItem("table_round", FurnitureCategory.TABLE, "圆桌", "⚪", 150, 3, 0, 2, 5),
    FurnitureItem("table_desk", FurnitureCategory.TABLE, "书桌", "📚", 300, 5, 0, 5, 8),
    FurnitureItem("table_shelf", FurnitureCategory.TABLE, "置物架", "🧾", 250, 4, 0, 3, 6),
    // Decoration (6)
    FurnitureItem("decor_plant", FurnitureCategory.DECORATION, "盆栽", "🪴", 50, 1, 2, 0, 8),
    FurnitureItem("decor_lamp", FurnitureCategory.DECORATION, "台灯", "💡", 100, 2, 3, 0, 5),
    FurnitureItem("decor_frame", FurnitureCategory.DECORATION, "相框", "🖼️", 80, 1, 0, 0, 6),
    FurnitureItem("decor_mirror", FurnitureCategory.DECORATION, "镜子", "🪞", 120, 3, 0, 0, 8),
    FurnitureItem("decor_clock", FurnitureCategory.DECORATION, "挂钟", "🕐", 90, 2, 0, 0, 4),
    FurnitureItem("decor_vase", FurnitureCategory.DECORATION, "花瓶", "🏺", 70, 1, 1, 0, 7),
    // Toy (4)
    FurnitureItem("toy_ball", FurnitureCategory.TOY, "毛线球", "🧶", 40, 1, 0, 5, 0),
    FurnitureItem("toy_yarn", FurnitureCategory.TOY, "线团", "🎈", 60, 2, 0, 8, 0),
    FurnitureItem("toy_mouse", FurnitureCategory.TOY, "玩具鼠", "🐭", 100, 3, 0, 12, 0),
    FurnitureItem("toy_feather", FurnitureCategory.TOY, "羽毛棒", "🪶", 150, 4, 0, 15, 0)
)

private val _ownedFurniture = MutableStateFlow(loadOwnedFurniture())
val ownedFurniture: StateFlow<List<String>> = _ownedFurniture.asStateFlow()

private val _roomLayout = MutableStateFlow<List<RoomLayout>>(emptyList())
val roomLayout: StateFlow<List<RoomLayout>> = _roomLayout.asStateFlow()

private fun loadOwnedFurniture(): List<String> {
    val raw = prefs.getString(KEY_OWNED_FURNITURE, "") ?: ""
    return if (raw.isBlank()) DEFAULT_OWNED_FURNITURE else raw.split(SEPARATOR)
}

private fun saveOwnedFurniture(ids: List<String>) {
    prefs.edit().putString(KEY_OWNED_FURNITURE, ids.joinToString(SEPARATOR)).apply()
}

private val DEFAULT_OWNED_FURNITURE = listOf("wall_pink", "floor_wood", "bed_round", "decor_plant")

private const val KEY_OWNED_FURNITURE = "owned_furniture"

// Load room layout from DB
suspend fun loadRoomLayout() {
    _roomLayout.value = roomLayoutDao.getAllOnce()
}
```

- [ ] **Step 2: 添加家具购买和摆放方法**

```kotlin
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

suspend fun placeFurniture(slotIndex: Int, furnitureId: String) {
    roomLayoutDao.upsert(RoomLayout(slotIndex = slotIndex, furnitureId = furnitureId))
    loadRoomLayout()
}

suspend fun removeFurniture(slotIndex: Int) {
    roomLayoutDao.removeSlot(slotIndex)
    loadRoomLayout()
}

fun getFurnitureItems(): List<FurnitureItem> = FURNITURE_CATALOGUE

/**
 * Calculates total comfort/fun/beauty from currently placed furniture.
 */
fun getRoomStats(): Triple<Int, Int, Int> {
    val placed = _roomLayout.value
    val map = FURNITURE_CATALOGUE.associateBy { it.id }
    var comfort = 0
    var fun = 0
    var beauty = 0
    placed.forEach { layout ->
        val item = map[layout.furnitureId] ?: return@forEach
        comfort += item.comfort
        fun += item.fun
        beauty += item.beauty
    }
    return Triple(
        comfort.coerceAtMost(50),
        fun.coerceAtMost(30),
        beauty.coerceAtMost(30)
    )
}
```

- [ ] **Step 3: 添加 roomLayoutDao 引用**

In the PetRepository class, add to the properties:

```kotlin
private val roomLayoutDao = database.roomLayoutDao()
```

And add the import:
```kotlin
import com.deskpet.app.data.db.RoomLayoutDao
import com.deskpet.app.data.model.FurnitureItem
import com.deskpet.app.data.model.FurnitureCategory
import com.deskpet.app.data.model.RoomLayout
```

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/deskpet/app/data/repository/PetRepository.kt
git commit -m "feat(L2-1): add furniture catalogue, purchase, placement logic to PetRepository"
```

---

### Task 5: DecorScreen + DecorViewModel — 家具商店/摆放 UI

**Files:**
- Create: `app/src/main/java/com/deskpet/app/ui/screens/decor/DecorViewModel.kt`
- Create: `app/src/main/java/com/deskpet/app/ui/screens/decor/DecorScreen.kt`
- Modify: `app/src/main/java/com/deskpet/app/ui/components/BottomNav.kt`
- Modify: `app/src/main/java/com/deskpet/app/ui/navigation/NavGraph.kt`

- [ ] **Step 1: 创建 DecorViewModel.kt**

```kotlin
// app/src/main/java/com/deskpet/app/ui/screens/decor/DecorViewModel.kt
package com.deskpet.app.ui.screens.decor

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deskpet.app.DeskPetApplication
import com.deskpet.app.data.model.FurnitureCategory
import com.deskpet.app.data.model.FurnitureItem
import com.deskpet.app.data.model.RoomLayout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DecorViewModel(application: android.app.Application) : AndroidViewModel(application) {
    private val repository = getApplication<DeskPetApplication>().repository

    val pet by lazy { repository.pet }
    val ownedFurniture by lazy { repository.ownedFurniture }
    val roomLayout by lazy { repository.roomLayout }

    private val _selectedCategory = MutableStateFlow(FurnitureCategory.WALLPAPER)
    val selectedCategory: StateFlow<FurnitureCategory> = _selectedCategory

    private val _toast = MutableStateFlow<String?>(null)
    val toast: StateFlow<String?> = _toast

    private val _selectedSlot = MutableStateFlow<Int?>(null)
    val selectedSlot: StateFlow<Int?> = _selectedSlot

    init {
        viewModelScope.launch {
            repository.loadRoomLayout()
        }
    }

    fun getFurnitureCatalogue(): List<FurnitureItem> = repository.getFurnitureItems()

    fun selectCategory(category: FurnitureCategory) {
        _selectedCategory.value = category
    }

    fun selectSlot(slotIndex: Int) {
        _selectedSlot.value = slotIndex
    }

    fun onTapFurniture(item: FurnitureItem) {
        val pet = pet.value
        val owned = ownedFurniture.value.contains(item.id)
        val layout = roomLayout.value

        viewModelScope.launch {
            if (owned) {
                // Find an available slot for this category
                val slotIndex = findAvailableSlot(item.category, layout)
                if (slotIndex == null) {
                    _toast.value = "该类型的格子已满，请先移除一件"
                } else {
                    repository.placeFurniture(slotIndex, item.id)
                    _toast.value = "已摆放「${item.name}」"
                }
            } else {
                if (pet.level < item.requiredLevel) {
                    _toast.value = "需要 Lv.${item.requiredLevel} 才能解锁"
                } else {
                    val ok = repository.purchaseFurniture(item)
                    _toast.value = if (ok) "购买成功！「${item.name}」已加入仓库" else "钻石不足"
                }
            }
        }
    }

    fun removeFurnitureAt(slotIndex: Int) {
        viewModelScope.launch {
            repository.removeFurniture(slotIndex)
            _toast.value = "已收起"
        }
    }

    private fun findAvailableSlot(category: FurnitureCategory, layout: List<RoomLayout>): Int? {
        val baseSlot = when (category) {
            FurnitureCategory.WALLPAPER -> 0
            FurnitureCategory.FLOOR -> 1
            FurnitureCategory.BED -> 2
            FurnitureCategory.TABLE -> 3
            FurnitureCategory.DECORATION -> 4
            FurnitureCategory.TOY -> 6
        }
        val slotCount = category.slotCount
        val occupiedSlots = layout.map { it.slotIndex }.toSet()
        for (i in 0 until slotCount) {
            if (baseSlot + i !in occupiedSlots) return baseSlot + i
        }
        return null
    }

    fun onToastShown() { _toast.value = null }
}
```

- [ ] **Step 2: 创建 DecorScreen.kt**

```kotlin
// app/src/main/java/com/deskpet/app/ui/screens/decor/DecorScreen.kt
package com.deskpet.app.ui.screens.decor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deskpet.app.data.model.FurnitureCategory
import com.deskpet.app.data.model.FurnitureItem
import com.deskpet.app.ui.components.RoomSceneCanvas

@Composable
fun DecorScreen(
    onBack: () -> Unit,
    viewModel: DecorViewModel = viewModel()
) {
    val pet by viewModel.pet.collectAsStateWithLifecycle()
    val ownedFurniture by viewModel.ownedFurniture.collectAsStateWithLifecycle()
    val roomLayout by viewModel.roomLayout.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val toast by viewModel.toast.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(toast) {
        toast?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onToastShown()
        }
    }

    val catalogue = remember { viewModel.getFurnitureCatalogue() }
    val filteredItems = remember(selectedCategory) {
        catalogue.filter { it.category == selectedCategory }
    }

    val tabs = FurnitureCategory.values()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "宠物小窝",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.weight(1f))
            Text(text = "💎 ${pet.diamonds}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.width(8.dp))
            Text(text = "Lv.${pet.level}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        }

        // Room scene preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            RoomSceneCanvas(
                layout = roomLayout,
                furnitureCatalogue = catalogue
            )
        }

        Spacer(Modifier.height(8.dp))

        // Category tabs
        ScrollableTabRow(
            selectedTabIndex = tabs.indexOf(selectedCategory),
            edgePadding = 16.dp
        ) {
            tabs.forEach { category ->
                Tab(
                    selected = category == selectedCategory,
                    onClick = { viewModel.selectCategory(category) },
                    text = { Text(category.displayName) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Furniture grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, end = 16.dp, bottom = 96.dp
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredItems, key = { it.id }) { item ->
                FurnitureCard(
                    item = item,
                    owned = ownedFurniture.contains(item.id),
                    petLevel = pet.level,
                    onTap = { viewModel.onTapFurniture(item) }
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp)
        )
    }
}

@Composable
private fun FurnitureCard(
    item: FurnitureItem,
    owned: Boolean,
    petLevel: Int,
    onTap: () -> Unit
) {
    val locked = petLevel < item.requiredLevel
    val bgColor = when {
        owned -> MaterialTheme.colorScheme.primaryContainer
        locked -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.surface
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(
                1.dp,
                if (owned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onTap)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = item.emoji, fontSize = 32.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            text = item.name,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        when {
            owned -> Text("✓ 已拥有", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
            locked -> Text("Lv.${item.requiredLevel}解锁", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            else -> Text("💎 ${item.price}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
        }
        // Show stats if any
        val stats = buildString {
            if (item.comfort > 0) append("舒适+${item.comfort} ")
            if (item.fun > 0) append("趣味+${item.fun} ")
            if (item.beauty > 0) append("美观+${item.beauty}")
        }
        if (stats.isNotBlank()) {
            Spacer(Modifier.height(2.dp))
            Text(stats.trim(), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
```

- [ ] **Step 3: 添加 DECOR 路由到 PetDestinations**

In `BottomNav.kt`, add to `PetDestinations`:

```kotlin
const val DECOR = "decor"
```

- [ ] **Step 4: 在 NavGraph 中添加 decor composable**

In `NavGraph.kt`, add after the diary composable:

```kotlin
composable(PetDestinations.DECOR) {
    DecorScreen(onBack = { navController.popBackStack() })
}
```

Add import:
```kotlin
import com.deskpet.app.ui.screens.decor.DecorScreen
```

- [ ] **Step 5: 在 PetHomeScreen 添加小窝入口**

In `PetHomeScreen.kt`, add a decor button to the ActionRow. In the `ActionRow` composable, add a new button:

```kotlin
// Add to ActionRow parameters:
onDecor: () -> Unit

// Add inside the Row:
ActionIconButton(
    icon = "🏠",
    label = "小窝",
    onClick = onDecor
)
```

And in the `PetHomeScreen` composable call:
```kotlin
ActionRow(
    onPet = viewModel::onPet,
    onFeed = viewModel::onOpenFoodSheet,
    onDressUp = onNavigateToDressUp,
    onPhoto = viewModel::onPhoto,
    onDecor = onNavigateToDecor
)
```

Add the navigation parameter:
```kotlin
fun PetHomeScreen(
    onNavigateToDressUp: () -> Unit,
    onNavigateToDiary: () -> Unit,
    onNavigateToDecor: () -> Unit,
    viewModel: PetViewModel = viewModel()
)
```

Update `NavGraph.kt` HOME composable:
```kotlin
composable(PetDestinations.HOME) {
    PetHomeScreen(
        onNavigateToDressUp = {
            navController.navigate(PetDestinations.DRESSUP) { launchSingleTop = true }
        },
        onNavigateToDiary = { navController.navigate(PetDestinations.DIARY) },
        onNavigateToDecor = { navController.navigate(PetDestinations.DECOR) }
    )
}
```

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/deskpet/app/ui/screens/decor/ \
  app/src/main/java/com/deskpet/app/ui/components/BottomNav.kt \
  app/src/main/java/com/deskpet/app/ui/navigation/NavGraph.kt \
  app/src/main/java/com/deskpet/app/ui/screens/home/PetHomeScreen.kt
git commit -m "feat(L2-1): add DecorScreen, DecorViewModel, navigation for pet home"
```

---

### Task 6: PetBehaviorEngine 家具属性影响 + 编译验证

**Files:**
- Modify: `app/src/main/java/com/deskpet/app/service/PetBehaviorEngine.kt`

- [ ] **Step 1: 添加家具舒适度对心情恢复的影响**

In `PetBehaviorEngine.kt`, add comfort bonus to mood recovery:

```kotlin
// In evaluateState() or moodRecovery logic, add:
val (comfort, fun_stat, beauty) = repository.getRoomStats()
val moodRecoveryBonus = comfort / 10 // each 10 comfort = +10% recovery speed

// When recovering mood:
val recoveryAmount = baseRecovery * (1f + moodRecoveryBonus / 10f)
```

Also add fun stat effect on PLAYING state probability:
```kotlin
// When random behavior triggers:
val playingChance = baseChance + (fun_stat / 10) * 0.05f // each 10 fun = +5%
```

- [ ] **Step 2: 全量编译验证**

```bash
cd /workspace/DeskPet-android/DeskPet
./gradlew assembleDebug --no-daemon
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "feat(L2-1): integrate furniture stats into PetBehaviorEngine, verify build"
```

---

## Part B: L2-2 旅行放置系统

### Task 7: TravelDestination + TravelLog + Postcard 数据模型

**Files:**
- Create: `app/src/main/java/com/deskpet/app/data/model/TravelDestination.kt`
- Create: `app/src/main/java/com/deskpet/app/data/model/TravelLog.kt`
- Create: `app/src/main/java/com/deskpet/app/data/model/Postcard.kt`
- Create: `app/src/main/java/com/deskpet/app/data/db/TravelLogDao.kt`
- Create: `app/src/main/java/com/deskpet/app/data/db/PostcardDao.kt`
- Modify: `app/src/main/java/com/deskpet/app/data/db/AppDatabase.kt`

- [ ] **Step 1: 创建 TravelDestination.kt**

```kotlin
// app/src/main/java/com/deskpet/app/data/model/TravelDestination.kt
package com.deskpet.app.data.model

enum class TravelType(val displayName: String, val durationRange: Pair<Long, Long>) {
    SHORT("短途", Pair(30 * 60 * 1000L, 60 * 60 * 1000L)),        // 30min - 1h
    MEDIUM("中途", Pair(2 * 60 * 60 * 1000L, 4 * 60 * 60 * 1000L)), // 2h - 4h
    LONG("长途", Pair(6 * 60 * 60 * 1000L, 12 * 60 * 60 * 1000L))   // 6h - 12h
}

data class TravelDestination(
    val id: String,
    val name: String,
    val type: TravelType,
    val emoji: String,
    val requiredLevel: Int,
    val requiredOutfit: String? = null, // null = no outfit requirement
    val sceneDrawKey: String,           // key for PostcardCanvas scene rendering
    val postcardTemplates: List<String>,
    val giftDiamondRange: Pair<Int, Int>,
    val giftOutfitChance: Float = 0.05f, // 5% chance for outfit gift
    val giftFurnitureChance: Float = 0.03f // 3% chance for furniture gift
)

// Destination catalogue
val TRAVEL_DESTINATIONS: List<TravelDestination> = listOf(
    TravelDestination("park", "公园", TravelType.SHORT, "🌳", 1, null, "park_scene",
        listOf("在公园遇到了蝴蝶！", "晒太阳好舒服~", "和别的小动物打招呼了"),
        Pair(5, 15)),
    TravelDestination("cafe", "咖啡馆", TravelType.SHORT, "☕", 1, null, "cafe_scene",
        listOf("咖啡好香~", "在咖啡馆睡了个午觉", "店主给了小饼干"),
        Pair(8, 20)),
    TravelDestination("beach", "海边", TravelType.MEDIUM, "🏖️", 5, null, "beach_scene",
        listOf("海风好舒服~", "捡到了贝壳！", "看到了海鸥"),
        Pair(15, 40)),
    TravelDestination("forest", "山林", TravelType.MEDIUM, "🌲", 5, null, "forest_scene",
        listOf("空气好清新！", "看到了小松鼠", "在草地上打滚"),
        Pair(15, 40)),
    TravelDestination("snow", "雪山", TravelType.LONG, "🏔️", 10, "head_beanie", "snow_scene",
        listOf("雪好白好美~", "堆了个小雪人！", "差点滑倒了"),
        Pair(30, 80)),
    TravelDestination("starry", "星空", TravelType.LONG, "✨", 15, null, "starry_scene",
        listOf("星星好漂亮！", "许了个愿望~", "看到了流星"),
        Pair(40, 100))
)
```

- [ ] **Step 2: 创建 TravelLog.kt**

```kotlin
// app/src/main/java/com/deskpet/app/data/model/TravelLog.kt
package com.deskpet.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "travel_logs")
data class TravelLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val destinationId: String,
    val destinationName: String,
    val departTime: Long,
    val returnTime: Long,
    val postcardsReceived: Int = 0,
    val giftsReceived: String = "", // JSON array of gift descriptions
    val completed: Boolean = false,
    val completedAt: Long? = null
)
```

- [ ] **Step 3: 创建 Postcard.kt**

```kotlin
// app/src/main/java/com/deskpet/app/data/model/Postcard.kt
package com.deskpet.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "postcards")
data class Postcard(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val destinationId: String,
    val destinationName: String,
    val destinationEmoji: String,
    val date: String, // yyyy-MM-dd
    val message: String,
    val sceneDrawKey: String,
    val petEmoji: String,
    val collected: Boolean = true
)
```

- [ ] **Step 4: 创建 TravelLogDao.kt**

```kotlin
// app/src/main/java/com/deskpet/app/data/db/TravelLogDao.kt
package com.deskpet.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.deskpet.app.data.model.TravelLog
import kotlinx.coroutines.flow.Flow

@Dao
interface TravelLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: TravelLog): Long

    @Query("SELECT * FROM travel_logs WHERE completed = 0 ORDER BY departTime DESC LIMIT 1")
    suspend fun getActiveTravel(): TravelLog?

    @Query("SELECT * FROM travel_logs ORDER BY departTime DESC")
    fun getAll(): Flow<List<TravelLog>>

    @Query("SELECT * FROM travel_logs WHERE id = :id")
    suspend fun getById(id: Long): TravelLog?

    @Query("UPDATE travel_logs SET completed = 1, completedAt = :completedAt, postcardsReceived = :postcards, giftsReceived = :gifts WHERE id = :id")
    suspend fun completeTravel(id: Long, completedAt: Long, postcards: Int, gifts: String)

    @Query("SELECT COUNT(*) FROM travel_logs WHERE completed = 1")
    suspend fun getCompletedCount(): Int
}
```

- [ ] **Step 5: 创建 PostcardDao.kt**

```kotlin
// app/src/main/java/com/deskpet/app/data/db/PostcardDao.kt
package com.deskpet.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.deskpet.app.data.model.Postcard
import kotlinx.coroutines.flow.Flow

@Dao
interface PostcardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(postcard: Postcard): Long

    @Query("SELECT * FROM postcards ORDER BY date DESC")
    fun getAll(): Flow<List<Postcard>>

    @Query("SELECT * FROM postcards ORDER BY date DESC LIMIT :limit")
    fun getRecent(limit: Int = 30): Flow<List<Postcard>>

    @Query("SELECT COUNT(*) FROM postcards")
    suspend fun count(): Int

    @Query("SELECT COUNT(DISTINCT destinationId) FROM postcards")
    suspend fun getUniqueDestinations(): Int
}
```

- [ ] **Step 6: 更新 AppDatabase — 添加 TravelLog + Postcard, 迁移到 v7**

```kotlin
// In @Database annotation:
entities = [..., RoomLayout::class, TravelLog::class, Postcard::class],
version = 7,

// Add abstract DAOs:
abstract fun travelLogDao(): TravelLogDao
abstract fun postcardDao(): PostcardDao

// Add MIGRATION_6_7:
private val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS travel_logs (
                id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                destinationId TEXT NOT NULL,
                destinationName TEXT NOT NULL,
                departTime INTEGER NOT NULL,
                returnTime INTEGER NOT NULL,
                postcardsReceived INTEGER NOT NULL DEFAULT 0,
                giftsReceived TEXT NOT NULL DEFAULT '',
                completed INTEGER NOT NULL DEFAULT 0,
                completedAt INTEGER
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS postcards (
                id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                destinationId TEXT NOT NULL,
                destinationName TEXT NOT NULL,
                destinationEmoji TEXT NOT NULL,
                date TEXT NOT NULL,
                message TEXT NOT NULL,
                sceneDrawKey TEXT NOT NULL,
                petEmoji TEXT NOT NULL,
                collected INTEGER NOT NULL DEFAULT 1
            )
        """.trimIndent())
    }
}

// Update migration chain:
.addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
```

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "feat(L2-2): add TravelDestination, TravelLog, Postcard models, DAOs, DB v7 migration"
```

---

### Task 8: TravelEngine — 旅行逻辑引擎

**Files:**
- Create: `app/src/main/java/com/deskpet/app/service/TravelEngine.kt`

- [ ] **Step 1: 创建 TravelEngine.kt**

```kotlin
// app/src/main/java/com/deskpet/app/service/TravelEngine.kt
package com.deskpet.app.service

import com.deskpet.app.data.db.AppDatabase
import com.deskpet.app.data.model.PetSpecies
import com.deskpet.app.data.model.Postcard
import com.deskpet.app.data.model.TRAVEL_DESTINATIONS
import com.deskpet.app.data.model.TravelDestination
import com.deskpet.app.data.model.TravelLog
import com.deskpet.app.data.repository.PetRepository
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.random.Random

/**
 * Manages pet travel: start, check return, generate gifts and postcards.
 */
class TravelEngine(
    private val database: AppDatabase,
    private val repository: PetRepository
) {
    private val travelLogDao = database.travelLogDao()
    private val postcardDao = database.postcardDao()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    data class TravelResult(
        val success: Boolean,
        val message: String,
        val returnTime: Long = 0L
    )

    data class TravelReturnResult(
        val returned: Boolean,
        val destinationName: String = "",
        val gifts: List<String> = emptyList(),
        val postcard: String? = null,
        val message: String = ""
    )

    /**
     * Starts a travel to the given destination with the specified duration.
     */
    suspend fun startTravel(destinationId: String, durationMs: Long): TravelResult {
        // Check if already traveling
        val active = travelLogDao.getActiveTravel()
        if (active != null) {
            return TravelResult(false, "宠物正在旅行中哦~")
        }

        val destination = TRAVEL_DESTINATIONS.find { it.id == destinationId }
            ?: return TravelResult(false, "未知目的地")

        val pet = repository.pet.value
        if (pet.level < destination.requiredLevel) {
            return TravelResult(false, "需要 Lv.${destination.requiredLevel} 才能前往${destination.name}")
        }

        // Check required outfit
        if (destination.requiredOutfit != null) {
            val hasOutfit = repository.ownedOutfits.value.contains(destination.requiredOutfit)
            if (!hasOutfit) {
                return TravelResult(false, "需要特定装扮才能前往${destination.name}")
            }
        }

        val now = System.currentTimeMillis()
        val returnTime = now + durationMs

        val log = TravelLog(
            destinationId = destination.id,
            destinationName = destination.name,
            departTime = now,
            returnTime = returnTime
        )
        travelLogDao.insert(log)

        return TravelResult(
            success = true,
            message = "${pet.name}出发去${destination.name}啦！旅途愉快~",
            returnTime = returnTime
        )
    }

    /**
     * Checks if the current travel has completed. If so, generates gifts and postcard.
     */
    suspend fun checkTravelReturn(): TravelReturnResult {
        val active = travelLogDao.getActiveTravel() ?: return TravelReturnResult(false)
        val now = System.currentTimeMillis()

        if (now < active.returnTime) {
            val remaining = active.returnTime - now
            val hours = remaining / (60 * 60 * 1000)
            val minutes = (remaining % (60 * 60 * 1000)) / (60 * 1000)
            return TravelReturnResult(
                returned = false,
                destinationName = active.destinationName,
                message = "${active.destinationName}旅行中，还有 ${hours}h${minutes}m 回来"
            )
        }

        // Travel complete — generate rewards
        val destination = TRAVEL_DESTINATIONS.find { it.id == active.destinationId }!!
        val pet = repository.pet.value
        val gifts = mutableListOf<String>()
        val giftJsonArray = JSONArray()

        // Diamond reward (always)
        val diamonds = Random.nextInt(destination.giftDiamondRange.first, destination.giftDiamondRange.second + 1)
        repository.addDiamonds(diamonds)
        gifts.add("💎 x$diamonds")
        giftJsonArray.put(JSONObject().apply {
            put("type", "diamond")
            put("amount", diamonds)
        })

        // Outfit gift (rare)
        if (Random.nextFloat() < destination.giftOutfitChance) {
            gifts.add("🎁 限定装扮!")
            giftJsonArray.put(JSONObject().apply { put("type", "outfit") })
        }

        // Furniture gift (rare)
        if (Random.nextFloat() < destination.giftFurnitureChance) {
            gifts.add("🏠 家具一件!")
            giftJsonArray.put(JSONObject().apply { put("type", "furniture") })
        }

        // Generate postcard
        val message = destination.postcardTemplates.random()
        val petEmoji = pet.species.emoji
        val postcard = Postcard(
            destinationId = destination.id,
            destinationName = destination.name,
            destinationEmoji = destination.emoji,
            date = dateFormat.format(java.util.Date()),
            message = message,
            sceneDrawKey = destination.sceneDrawKey,
            petEmoji = petEmoji
        )
        postcardDao.insert(postcard)

        // Complete the travel
        travelLogDao.completeTravel(
            active.id,
            now,
            postcardsReceived = 1,
            gifts = giftJsonArray.toString()
        )

        // Mood bonus
        repository.updateMood(10)

        return TravelReturnResult(
            returned = true,
            destinationName = destination.name,
            gifts = gifts,
            postcard = message,
            message = "${pet.name}从${destination.name}回来啦！带了${gifts.joinToString("、")}"
        )
    }

    /**
     * Gets the active travel (if any) for UI display.
     */
    suspend fun getActiveTravel(): TravelLog? = travelLogDao.getActiveTravel()

    /**
     * Gets all postcards for the collection.
     */
    fun getAllPostcards(): Flow<List<Postcard>> = postcardDao.getAll()

    /**
     * Gets travel history.
     */
    fun getAllTravels(): Flow<List<TravelLog>> = travelLogDao.getAll()
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/deskpet/app/service/TravelEngine.kt
git commit -m "feat(L2-2): add TravelEngine for travel start, return check, gift/postcard generation"
```

---

### Task 9: TravelScreen + TravelViewModel — 旅行 UI

**Files:**
- Create: `app/src/main/java/com/deskpet/app/ui/screens/travel/TravelViewModel.kt`
- Create: `app/src/main/java/com/deskpet/app/ui/screens/travel/TravelScreen.kt`
- Modify: `app/src/main/java/com/deskpet/app/ui/components/BottomNav.kt`
- Modify: `app/src/main/java/com/deskpet/app/ui/navigation/NavGraph.kt`
- Modify: `app/src/main/java/com/deskpet/app/ui/screens/home/PetHomeScreen.kt`

- [ ] **Step 1: 创建 TravelViewModel.kt**

```kotlin
// app/src/main/java/com/deskpet/app/ui/screens/travel/TravelViewModel.kt
package com.deskpet.app.ui.screens.travel

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deskpet.app.DeskPetApplication
import com.deskpet.app.data.model.TRAVEL_DESTINATIONS
import com.deskpet.app.data.model.TravelDestination
import com.deskpet.app.data.model.TravelLog
import com.deskpet.app.service.TravelEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TravelViewModel(application: android.app.Application) : AndroidViewModel(application) {
    private val repository = getApplication<DeskPetApplication>().repository
    private val travelEngine = TravelEngine(
        getApplication<DeskPetApplication>().database,
        repository
    )

    val pet by lazy { repository.pet }
    val postcards by lazy { travelEngine.getAllPostcards() }

    private val _activeTravel = MutableStateFlow<TravelLog?>(null)
    val activeTravel: StateFlow<TravelLog?> = _activeTravel

    private val _toast = MutableStateFlow<String?>(null)
    val toast: StateFlow<String?> = _toast

    private val _returnResult = MutableStateFlow<TravelEngine.TravelReturnResult?>(null)
    val returnResult: StateFlow<TravelEngine.TravelReturnResult?> = _returnResult

    init {
        checkTravelStatus()
    }

    fun checkTravelStatus() {
        viewModelScope.launch {
            val result = travelEngine.checkTravelReturn()
            if (result.returned) {
                _returnResult.value = result
                _toast.value = result.message
            }
            _activeTravel.value = travelEngine.getActiveTravel()
        }
    }

    fun startTravel(destination: TravelDestination, durationMs: Long) {
        viewModelScope.launch {
            val result = travelEngine.startTravel(destination.id, durationMs)
            _toast.value = result.message
            if (result.success) {
                _activeTravel.value = travelEngine.getActiveTravel()
            }
        }
    }

    fun getDestinations(): List<TravelDestination> = TRAVEL_DESTINATIONS

    fun onToastShown() { _toast.value = null }
    fun onReturnResultHandled() { _returnResult.value = null }
}
```

- [ ] **Step 2: 创建 TravelScreen.kt**

```kotlin
// app/src/main/java/com/deskpet/app/ui/screens/travel/TravelScreen.kt
package com.deskpet.app.ui.screens.travel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deskpet.app.data.model.TravelDestination
import com.deskpet.app.data.model.TravelType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TravelScreen(
    onBack: () -> Unit,
    viewModel: TravelViewModel = viewModel()
) {
    val pet by viewModel.pet.collectAsStateWithLifecycle()
    val activeTravel by viewModel.activeTravel.collectAsStateWithLifecycle()
    val toast by viewModel.toast.collectAsStateWithLifecycle()
    val returnResult by viewModel.returnResult.collectAsStateWithLifecycle()
    val postcards by viewModel.postcards.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(toast) {
        toast?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onToastShown()
        }
    }

    var selectedDestination by remember { mutableStateOf<TravelDestination?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "返回", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text("旅行", 20.sp, FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        }

        // Active travel status
        activeTravel?.let { travel ->
            TravelStatusCard(travel.destinationName, travel.returnTime)
        }

        // Destination list
        if (activeTravel == null) {
            Text(
                "选择目的地",
                16.sp, FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp, end = 16.dp, bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(viewModel.getDestinations(), key = { it.id }) { dest ->
                    DestinationCard(
                        destination = dest,
                        petLevel = pet.level,
                        onTap = { selectedDestination = dest }
                    )
                }
            }
        }

        // Postcard gallery (if any)
        if (postcards.isNotEmpty()) {
            Text(
                "明信片收藏 (${postcards.size})",
                16.sp, FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp, end = 16.dp, bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(postcards, key = { it.id }) { postcard ->
                    PostcardCard(
                        emoji = postcard.destinationEmoji,
                        destination = postcard.destinationName,
                        message = postcard.message,
                        date = postcard.date
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp)
        )
    }

    // Duration selection dialog
    selectedDestination?.let { dest ->
        DurationSelectDialog(
            destination = dest,
            onDismiss = { selectedDestination = null },
            onSelect = { durationMs ->
                viewModel.startTravel(dest, durationMs)
                selectedDestination = null
            }
        )
    }

    // Travel return dialog
    returnResult?.let { result ->
        if (result.returned) {
            AlertDialog(
                onDismissRequest = { viewModel.onReturnResultHandled() },
                title = { Text("旅行归来!") },
                text = {
                    Column {
                        Text(result.message)
                        if (result.gifts.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text("收获：", fontWeight = FontWeight.SemiBold)
                            result.gifts.forEach { Text("  $it") }
                        }
                        result.postcard?.let {
                            Spacer(Modifier.height(8.dp))
                            Text("明信片：$it", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.onReturnResultHandled() }) {
                        Text("好耶!")
                    }
                }
            )
        }
    }
}

@Composable
private fun TravelStatusCard(destName: String, returnTime: Long) {
    val sdf = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeStr = remember(returnTime) { sdf.format(Date(returnTime)) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("✈️", 32.sp)
        Spacer(Modifier.size(12.dp))
        Column {
            Text("正在前往 $destName", 16.sp, FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text("预计 $timeStr 回来", 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun DestinationCard(
    destination: TravelDestination,
    petLevel: Int,
    onTap: () -> Unit
) {
    val locked = petLevel < destination.requiredLevel

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (locked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .clickable(enabled = !locked, onClick = onTap)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(destination.emoji, 36.sp)
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(destination.name, 16.sp, FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text("${destination.type.displayName} · ${destination.type.durationRange.first / 60000}-${destination.type.durationRange.second / 60000}分钟",
                12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("💎 ${destination.giftDiamondRange.first}-${destination.giftDiamondRange.second}",
                12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (locked) {
            Text("Lv.${destination.requiredLevel}", 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DurationSelectDialog(
    destination: TravelDestination,
    onDismiss: () -> Unit,
    onSelect: (Long) -> Unit
) {
    val durations = remember(destination) {
        val (min, max) = destination.type.durationRange
        listOf(min, (min + max) / 2, max)
    }
    val labels = remember(destination) {
        val (min, max) = destination.type.durationRange
        listOf(
            "${min / 60000}分钟",
            "${(min + max) / 2 / 60000}分钟",
            "${max / 3600000}小时"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("前往${destination.name}") },
        text = {
            Column {
                Text("选择出行时长：", 14.sp)
                Spacer(Modifier.height(8.dp))
                durations.forEachIndexed { index, duration ->
                    TextButton(onClick = { onSelect(duration) }) {
                        Text(labels[index], 16.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
private fun PostcardCard(emoji: String, destination: String, message: String, date: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(emoji, 24.sp)
        Spacer(Modifier.size(8.dp))
        Column {
            Text("$destination · $date", 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(message, 14.sp, color = MaterialTheme.colorScheme.onSurface, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
        }
    }
}
```

- [ ] **Step 3: 添加 TRAVEL 路由和导航**

In `BottomNav.kt`:
```kotlin
const val TRAVEL = "travel"
```

In `NavGraph.kt`:
```kotlin
composable(PetDestinations.TRAVEL) {
    TravelScreen(onBack = { navController.popBackStack() })
}
```

Add travel entry in PetHomeScreen ActionRow (airplane icon):
```kotlin
onTravel: () -> Unit
// Button:
ActionIconButton(icon = "✈️", label = "旅行", onClick = onTravel)
```

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "feat(L2-2): add TravelScreen, TravelViewModel, navigation for travel system"
```

---

### Task 10: DeskPetApplication 旅行归来检查 + 编译验证

**Files:**
- Modify: `app/src/main/java/com/deskpet/app/DeskPetApplication.kt`

- [ ] **Step 1: 在 Application 启动时检查旅行归来**

```kotlin
// In DeskPetApplication.onCreate(), after existing init:
appScope.launch {
    val travelEngine = TravelEngine(database, repository)
    val result = travelEngine.checkTravelReturn()
    // Result will be picked up by UI when user opens travel screen
}
```

- [ ] **Step 2: 全量编译**

```bash
cd /workspace/DeskPet-android/DeskPet
./gradlew assembleDebug --no-daemon
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "feat(L2-2): add travel return check on app launch, verify build"
```

---

## Part C: L2-3 成就与图鉴系统

### Task 11: AchievementRecord 实体 + AchievementEngine

**Files:**
- Create: `app/src/main/java/com/deskpet/app/data/model/AchievementRecord.kt`
- Create: `app/src/main/java/com/deskpet/app/data/db/AchievementDao.kt`
- Create: `app/src/main/java/com/deskpet/app/service/AchievementEngine.kt`
- Modify: `app/src/main/java/com/deskpet/app/data/db/AppDatabase.kt`

- [ ] **Step 1: 创建 AchievementRecord.kt**

```kotlin
// app/src/main/java/com/deskpet/app/data/model/AchievementRecord.kt
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
```

- [ ] **Step 2: 创建 AchievementDao.kt**

```kotlin
// app/src/main/java/com/deskpet/app/data/db/AchievementDao.kt
package com.deskpet.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.deskpet.app.data.model.AchievementRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(record: AchievementRecord)

    @Query("SELECT * FROM achievement_records")
    fun getAll(): Flow<List<AchievementRecord>>

    @Query("SELECT achievementId FROM achievement_records")
    suspend fun getAllIds(): List<String>

    @Query("SELECT COUNT(*) FROM achievement_records")
    suspend fun count(): Int
}
```

- [ ] **Step 3: 创建 AchievementEngine.kt**

```kotlin
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
            uniqueDestinations = postcardDao.getUniqueDestinations().let { it },
            petLevel = pet.level,
            intimacy = pet.intimacy
        )
    }

    fun getUnlockedAchievements(): Flow<List<AchievementRecord>> = achievementDao.getAll()
}
```

- [ ] **Step 4: 更新 AppDatabase — 添加 AchievementRecord, 迁移到 v8**

```kotlin
entities = [..., TravelLog::class, Postcard::class, AchievementRecord::class],
version = 8,

abstract fun achievementDao(): AchievementDao

private val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS achievement_records (
                achievementId TEXT NOT NULL PRIMARY KEY,
                unlockedAt INTEGER NOT NULL
            )
        """.trimIndent())
    }
}

.addMigrations(..., MIGRATION_6_7, MIGRATION_7_8)
```

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "feat(L2-3): add AchievementRecord, AchievementDao, AchievementEngine, DB v8"
```

---

### Task 12: CodexScreen + CodexViewModel — 成就/图鉴 UI

**Files:**
- Create: `app/src/main/java/com/deskpet/app/ui/screens/codex/CodexViewModel.kt`
- Create: `app/src/main/java/com/deskpet/app/ui/screens/codex/CodexScreen.kt`
- Modify: `app/src/main/java/com/deskpet/app/ui/components/BottomNav.kt`
- Modify: `app/src/main/java/com/deskpet/app/ui/navigation/NavGraph.kt`
- Modify: `app/src/main/java/com/deskpet/app/ui/screens/settings/SettingsScreen.kt`

- [ ] **Step 1: 创建 CodexViewModel.kt**

```kotlin
// app/src/main/java/com/deskpet/app/ui/screens/codex/CodexViewModel.kt
package com.deskpet.app.ui.screens.codex

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deskpet.app.DeskPetApplication
import com.deskpet.app.data.model.Achievement
import com.deskpet.app.data.model.AchievementCategory
import com.deskpet.app.service.AchievementEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CodexViewModel(application: android.app.Application) : AndroidViewModel(application) {
    private val repository = getApplication<DeskPetApplication>().repository
    private val achievementEngine = AchievementEngine(
        getApplication<DeskPetApplication>().database,
        repository
    )

    val unlockedRecords by lazy {
        achievementEngine.getUnlockedAchievements()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    val allAchievements: List<Achievement> = achievementEngine.ALL_ACHIEVEMENTS

    private val _newlyUnlocked = MutableStateFlow<List<Achievement>>(emptyList())
    val newlyUnlocked: StateFlow<List<Achievement>> = _newlyUnlocked

    init {
        checkAchievements()
    }

    fun checkAchievements() {
        viewModelScope.launch {
            val result = achievementEngine.checkAll()
            if (result.newlyUnlocked.isNotEmpty()) {
                _newlyUnlocked.value = result.newlyUnlocked
            }
        }
    }

    fun clearNewlyUnlocked() {
        _newlyUnlocked.value = emptyList()
    }
}
```

- [ ] **Step 2: 创建 CodexScreen.kt**

```kotlin
// app/src/main/java/com/deskpet/app/ui/screens/codex/CodexScreen.kt
package com.deskpet.app.ui.screens.codex

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deskpet.app.data.model.Achievement
import com.deskpet.app.data.model.AchievementCategory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CodexScreen(
    onBack: () -> Unit,
    viewModel: CodexViewModel = viewModel()
) {
    val unlockedRecords by viewModel.unlockedRecords.collectAsStateWithLifecycle()
    val newlyUnlocked by viewModel.newlyUnlocked.collectAsStateWithLifecycle()

    val unlockedIds = remember(unlockedRecords) {
        unlockedRecords.map { it.achievementId }.toSet()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "返回", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text("成就图鉴", 20.sp, FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.weight(1f))
            Text("${unlockedIds.size}/${viewModel.allAchievements.size}",
                14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        LazyColumn(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, end = 16.dp, bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Group by category
            AchievementCategory.values().forEach { category ->
                item {
                    Text(
                        category.displayName,
                        16.sp,
                        FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(
                    viewModel.allAchievements.filter { it.category == category },
                    key = { it.id }
                ) { achievement ->
                    AchievementCard(
                        achievement = achievement,
                        unlocked = achievement.id in unlockedIds
                    )
                }
            }
        }
    }

    // Show newly unlocked achievement dialog
    if (newlyUnlocked.isNotEmpty()) {
        val achievement = newlyUnlocked.first()
        AlertDialog(
            onDismissRequest = { viewModel.clearNewlyUnlocked() },
            title = { Text("🎉 成就解锁!") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(achievement.emoji, 48.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(achievement.title, 18.sp, FontWeight.Bold)
                    Text(achievement.description, 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text("奖励 💎 x${achievement.rewardDiamonds}", 14.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.clearNewlyUnlocked() }) {
                    Text("太棒了!")
                }
            }
        )
    }
}

@Composable
private fun AchievementCard(achievement: Achievement, unlocked: Boolean) {
    val alpha = if (unlocked) 1f else 0.4f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (unlocked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            if (unlocked) achievement.emoji else "❓",
            28.sp,
            modifier = Modifier.graphicsLayerAlpha(alpha)
        )
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                achievement.title,
                15.sp,
                FontWeight.SemiBold,
                color = if (unlocked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
            Text(
                achievement.description,
                12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (unlocked) {
            Text("💎${achievement.rewardDiamonds}", 12.sp, color = MaterialTheme.colorScheme.primary)
        }
    }
}

// Helper for alpha modifier
@Composable
private fun Modifier.graphicsLayerAlpha(alpha: Float): Modifier =
    this.then(androidx.compose.ui.graphics.graphicsLayer { this.alpha = alpha })
```

- [ ] **Step 3: 添加 CODEX 路由和导航**

In `BottomNav.kt`:
```kotlin
const val CODEX = "codex"
```

In `NavGraph.kt`:
```kotlin
composable(PetDestinations.CODEX) {
    CodexScreen(onBack = { navController.popBackStack() })
}
```

- [ ] **Step 4: 在 SettingsScreen 添加图鉴入口**

```kotlin
// Add a clickable row in SettingsScreen:
Row(
    modifier = Modifier
        .fillMaxWidth()
        .clickable { /* navigate to codex */ }
        .padding(16.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    Text("🏆", 24.sp)
    Spacer(Modifier.size(12.dp))
    Text("成就图鉴", 16.sp)
}
```

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "feat(L2-3): add CodexScreen, CodexViewModel, achievement UI and navigation"
```

---

## Part D: L2-4 语音 TTS 互动

### Task 13: SpeechHelper + DialogueBank — TTS 引擎和文案池

**Files:**
- Create: `app/src/main/java/com/deskpet/app/util/SpeechHelper.kt`
- Create: `app/src/main/java/com/deskpet/app/util/DialogueBank.kt`

- [ ] **Step 1: 创建 SpeechHelper.kt**

```kotlin
// app/src/main/java/com/deskpet/app/util/SpeechHelper.kt
package com.deskpet.app.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

/**
 * Wrapper around Android TextToSpeech for pet voice interaction.
 * Uses Chinese voice with cute parameters (slower speed, higher pitch).
 */
object SpeechHelper : TextToSpeech.OnInitListener {

    private const val TAG = "SpeechHelper"

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    var isTtsAvailable = false
        private set

    private var enabled = false
    private var pendingText: String? = null

    fun init(context: Context) {
        if (tts != null) return
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.SIMPLIFIED_CHINESE)
            if (result == TextToSpeech.LANG_AVAILABLE || result == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                isTtsAvailable = true
                tts?.setSpeechRate(0.8f) // Slower = cuter
                tts?.setPitch(1.2f)      // Higher pitch = more adorable
                Log.i(TAG, "TTS initialized successfully")
                pendingText?.let {
                    speak(it)
                    pendingText = null
                }
            } else {
                isTtsAvailable = false
                Log.w(TAG, "Chinese TTS not available on this device")
            }
        } else {
            isTtsAvailable = false
            Log.w(TAG, "TTS init failed with status $status")
        }
        isInitialized = true
    }

    fun speak(text: String) {
        if (!enabled || !isTtsAvailable) return
        tts?.speak(text, TextToSpeech.QUEUE_ADD, null, "pet_speak_${System.currentTimeMillis()}")
    }

    /**
     * Speaks immediately, flushing any queued speech.
     */
    fun speakNow(text: String) {
        if (!enabled || !isTtsAvailable) return
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "pet_speak_now_${System.currentTimeMillis()}")
    }

    fun stop() {
        tts?.stop()
    }

    fun isSpeaking(): Boolean = tts?.isSpeaking == true

    fun setEnabled(value: Boolean) {
        enabled = value
        if (!value) stop()
    }

    fun destroy() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
        isTtsAvailable = false
    }
}
```

- [ ] **Step 2: 创建 DialogueBank.kt**

```kotlin
// app/src/main/java/com/deskpet/app/util/DialogueBank.kt
package com.deskpet.app.util

import com.deskpet.app.data.model.PersonalityTag
import kotlin.random.Random

/**
 * Scene-based dialogue pools for pet voice interaction.
 * Lines vary by personality tag for flavor.
 */
object DialogueBank {

    private fun pick(lines: List<String>, personalityTags: List<PersonalityTag>): String {
        val filtered = if (personalityTags.contains(PersonalityTag.LIVELY)) {
            lines.map { it.replace("~", "!") }
        } else if (personalityTags.contains(PersonalityTag.GENTLE)) {
            lines
        } else {
            lines
        }
        return filtered.random()
    }

    fun greeting(personalityTags: List<PersonalityTag>): String = pick(listOf(
        "主人你来啦~",
        "早安~",
        "辛苦一天啦~",
        "想你了~",
        "终于等到你了~"
    ), personalityTags)

    fun pet(personalityTags: List<PersonalityTag>): String = pick(listOf(
        "好舒服呀~",
        "再摸摸我嘛~",
        "最喜欢主人了~",
        "嘿嘿~好开心~",
        "主人的手好温暖~"
    ), personalityTags)

    fun feed(personalityTags: List<PersonalityTag>): String = pick(listOf(
        "小鱼干！我最爱了~",
        "好香好香~",
        "谢谢主人~",
        "开饭啦~好期待~",
        "真好吃~"
    ), personalityTags)

    fun periodLink(personalityTags: List<PersonalityTag>): String = pick(listOf(
        "我会陪着你的~",
        "主人辛苦了~多休息哦~",
        "抱抱~很快就好了~"
    ), personalityTags)

    fun dailyQuote(personalityTags: List<PersonalityTag>, quote: String): String {
        return pick(listOf(
            "今天想跟主人说：$quote",
            "看到一句话觉得很适合现在：$quote",
            "主人~$quote"
        ), personalityTags)
    }

    fun checkin(personalityTags: List<PersonalityTag>): String = pick(listOf(
        "主人真棒~",
        "又打卡啦~好习惯~",
        "坚持就是胜利~",
        "主人越来越健康了~"
    ), personalityTags)

    fun travelReturn(personalityTags: List<PersonalityTag>): String = pick(listOf(
        "我回来啦！想我了吗？",
        "旅途好开心~带了礼物哦~",
        "终于回到家了~还是家里好~"
    ), personalityTags)
}
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/deskpet/app/util/SpeechHelper.kt \
  app/src/main/java/com/deskpet/app/util/DialogueBank.kt
git commit -m "feat(L2-4): add SpeechHelper (TTS wrapper) and DialogueBank (scene dialogues)"
```

---

### Task 14: PetViewModel + SettingsScreen TTS 集成

**Files:**
- Modify: `app/src/main/java/com/deskpet/app/data/model/PetSettings.kt`
- Modify: `app/src/main/java/com/deskpet/app/data/repository/PetRepository.kt`
- Modify: `app/src/main/java/com/deskpet/app/ui/screens/home/PetViewModel.kt`
- Modify: `app/src/main/java/com/deskpet/app/ui/screens/settings/SettingsScreen.kt`
- Modify: `app/src/main/java/com/deskpet/app/DeskPetApplication.kt`

- [ ] **Step 1: 添加 ttsEnabled 到 PetSettings**

```kotlin
// In PetSettings.kt, add:
val ttsEnabled: Boolean = false  // Default off
```

- [ ] **Step 2: 在 PetRepository loadSettings/saveSettings 添加 ttsEnabled**

```kotlin
// In loadSettings():
ttsEnabled = prefs.getBoolean(KEY_TTS_ENABLED, false),

// In saveSettings():
putBoolean(KEY_TTS_ENABLED, settings.ttsEnabled)

// Add constant:
private const val KEY_TTS_ENABLED = "tts_enabled"
```

- [ ] **Step 3: 在 PetViewModel 中集成 TTS 触发**

```kotlin
// In PetViewModel init or wherever interactions happen:
// After onPet():
private fun speakPet() {
    val tags = repository.pet.value.personalityTags
    SpeechHelper.speak(DialogueBank.pet(tags))
}

// After onFeed():
private fun speakFeed() {
    val tags = repository.pet.value.personalityTags
    SpeechHelper.speak(DialogueBank.feed(tags))
}

// On app launch (greeting):
private fun speakGreeting() {
    val tags = repository.pet.value.personalityTags
    SpeechHelper.speak(DialogueBank.greeting(tags))
}
```

- [ ] **Step 4: 在 SettingsScreen 添加 TTS 开关**

```kotlin
// In SettingsScreen, add a toggle in the "声音" section:
SettingToggleRow(
    title = "宠物语音",
    description = "让宠物开口说话（需要设备支持中文语音）",
    checked = settings.ttsEnabled,
    onToggle = { enabled ->
        repository.updateSettings { it.copy(ttsEnabled = enabled) }
        SpeechHelper.setEnabled(enabled)
    }
)
```

- [ ] **Step 5: 在 DeskPetApplication 初始化 SpeechHelper**

```kotlin
// In onCreate():
SpeechHelper.init(this)
// Apply saved setting
val ttsEnabled = repository.settings.value.ttsEnabled
SpeechHelper.setEnabled(ttsEnabled)
```

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "feat(L2-4): integrate TTS into PetViewModel, Settings, Application"
```

---

### Task 15: 全量编译 + APK 生成 + Git 提交

**Files:**
- Modify: `app/src/main/java/com/deskpet/app/data/db/AppDatabase.kt` (final version check)

- [ ] **Step 1: 确认数据库版本和迁移链**

Verify AppDatabase has:
- version = 8
- entities include all 4 new: RoomLayout, TravelLog, Postcard, AchievementRecord
- migrations chain: MIGRATION_1_2 through MIGRATION_7_8

- [ ] **Step 2: 全量编译**

```bash
cd /workspace/DeskPet-android/DeskPet
./gradlew assembleDebug --no-daemon
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 修复编译错误（如有）**

Fix any compilation issues that arise from integration.

- [ ] **Step 4: 复制 APK 并验证**

```bash
cp app/build/outputs/apk/debug/app-debug.apk /workspace/DeskPet-L2-debug.apk
ls -lh /workspace/DeskPet-L2-debug.apk
```

- [ ] **Step 5: Git 提交**

```bash
git add -A
git commit -m "feat: implement L2 content layer

- L2-1 Home decoration: FurnitureItem, RoomLayout, FurnitureRenderer, RoomSceneCanvas, DecorScreen
- L2-2 Travel system: TravelEngine, TravelScreen, Postcard collection, gift generation
- L2-3 Achievement/codex: AchievementEngine, 23 achievements, CodexScreen with unlock dialogs
- L2-4 Voice TTS: SpeechHelper, DialogueBank, scene-based pet dialogues
- Database migrated to v8 with 4 new entities
- Furniture vector rendering for 24 items
- Travel system with 6 destinations, duration selection, postcard gallery"
```
