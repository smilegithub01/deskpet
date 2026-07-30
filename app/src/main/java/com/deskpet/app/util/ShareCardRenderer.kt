package com.deskpet.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.net.Uri
import androidx.core.content.FileProvider
import com.deskpet.app.data.model.Achievement
import com.deskpet.app.data.model.Pet
import com.deskpet.app.data.model.PetColor
import com.deskpet.app.data.model.PetSpecies
import com.deskpet.app.data.model.PetState
import com.deskpet.app.data.model.Postcard
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Card template styles for social sharing.
 */
enum class CardTemplate(val displayName: String, val bgColors: IntArray) {
    FRESH_PINK("清新粉", intArrayOf(0xFFFFE0EC.toInt(), 0xFFFFC1D9.toInt())),
    HEALING_GREEN("治愈绿", intArrayOf(0xFFE0F5E0.toInt(), 0xFFC1E8C1.toInt())),
    MINIMAL_WHITE("简约白", intArrayOf(0xFFF8F8F8.toInt(), 0xFFEEEEEE.toInt()))
}

/**
 * Card type identifiers.
 */
enum class ShareCardType {
    DAILY_STATUS,
    OUTFIT_COMBO,
    ACHIEVEMENT,
    POSTCARD,
    CHECKIN_STREAK
}

/**
 * Data payload for share card rendering.
 */
data class ShareCardData(
    val pet: Pet,
    val type: ShareCardType,
    val template: CardTemplate = CardTemplate.FRESH_PINK,
    val moodText: String = "",
    val diaryExcerpt: String = "",
    val outfitList: String = "",
    val achievement: Achievement? = null,
    val postcard: Postcard? = null,
    val streakDays: Int = 0,
    val showWatermark: Boolean = true
)

/**
 * Renders social sharing cards as Bitmaps.
 *
 * Supports 5 card types (daily status, outfit combo, achievement, postcard,
 * check-in streak) across 3 template styles (fresh pink, healing green,
 * minimal white). Cards are rendered at 1080x1080 (square) suitable for
 * WeChat / Xiaohongshu.
 */
object ShareCardRenderer {

    const val CARD_SIZE = 1080

    /**
     * Renders a share card bitmap from the given data.
     */
    fun render(data: ShareCardData): Bitmap {
        val bitmap = Bitmap.createBitmap(CARD_SIZE, CARD_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val w = CARD_SIZE.toFloat()
        val h = CARD_SIZE.toFloat()

        drawBackground(canvas, w, h, data.template)
        drawDecorativeBorder(canvas, w, h, data.template)

        when (data.type) {
            ShareCardType.DAILY_STATUS -> renderDailyStatus(canvas, w, h, data)
            ShareCardType.OUTFIT_COMBO -> renderOutfitCombo(canvas, w, h, data)
            ShareCardType.ACHIEVEMENT -> renderAchievement(canvas, w, h, data)
            ShareCardType.POSTCARD -> renderPostcard(canvas, w, h, data)
            ShareCardType.CHECKIN_STREAK -> renderCheckinStreak(canvas, w, h, data)
        }

        if (data.showWatermark) {
            drawWatermark(canvas, w, h, data.pet.name)
        }

        return bitmap
    }

    // ----------------------------------------------------------- Background

    private fun drawBackground(canvas: Canvas, w: Float, h: Float, template: CardTemplate) {
        val bgPaint = Paint().apply {
            isAntiAlias = true
            shader = LinearGradient(
                0f, 0f, w, h,
                template.bgColors,
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w, h, bgPaint)
    }

    private fun drawDecorativeBorder(canvas: Canvas, w: Float, h: Float, template: CardTemplate) {
        val borderColor = when (template) {
            CardTemplate.FRESH_PINK -> 0x33FF80AB.toInt()
            CardTemplate.HEALING_GREEN -> 0x334CAF50.toInt()
            CardTemplate.MINIMAL_WHITE -> 0x22BBBBBB.toInt()
        }
        val borderPaint = Paint().apply {
            isAntiAlias = true
            color = borderColor
            strokeWidth = 8f
            style = Paint.Style.STROKE
        }
        val inset = 24f
        canvas.drawRoundRect(
            RectF(inset, inset, w - inset, h - inset),
            32f, 32f,
            borderPaint
        )
    }

    // ----------------------------------------------------------- Daily Status

    private fun renderDailyStatus(canvas: Canvas, w: Float, h: Float, data: ShareCardData) {
        val pet = data.pet

        // Title
        drawCenteredText(
            canvas, "今日团子", w / 2, h * 0.10f,
            paint = titlePaint(36f, accentColor(data.template))
        )

        // Pet illustration
        drawPetBody(canvas, w * 0.25f, h * 0.18f, w * 0.50f, h * 0.50f, pet.color, pet.species)

        // Name + level
        drawCenteredText(
            canvas, "${pet.name}  Lv.${pet.level}", w / 2, h * 0.72f,
            paint = namePaint(32f)
        )

        // Mood
        if (data.moodText.isNotEmpty()) {
            drawCenteredText(
                canvas, data.moodText, w / 2, h * 0.78f,
                paint = bodyPaint(26f, 0xFF666666.toInt())
            )
        }

        // Diary excerpt
        if (data.diaryExcerpt.isNotEmpty()) {
            drawWrappedText(
                canvas, "\"${data.diaryExcerpt}\"", w * 0.12f, w * 0.88f, h * 0.85f,
                paint = italicPaint(24f, 0xFF888888.toInt()),
                lineHeight = 34f
            )
        }

        // Stats summary
        drawStatsBar(canvas, w, h * 0.93f, pet)
    }

    // ----------------------------------------------------------- Outfit Combo

    private fun renderOutfitCombo(canvas: Canvas, w: Float, h: Float, data: ShareCardData) {
        val pet = data.pet

        // Title
        drawCenteredText(
            canvas, "今日穿搭", w / 2, h * 0.10f,
            paint = titlePaint(36f, accentColor(data.template))
        )

        // Pet with outfit
        drawPetBody(canvas, w * 0.25f, h * 0.18f, w * 0.50f, h * 0.55f, pet.color, pet.species)

        // Outfit list
        drawCenteredText(
            canvas, "穿搭清单", w / 2, h * 0.76f,
            paint = namePaint(28f)
        )

        if (data.outfitList.isNotEmpty()) {
            drawWrappedText(
                canvas, data.outfitList, w * 0.12f, w * 0.88f, h * 0.82f,
                paint = bodyPaint(22f, 0xFF555555.toInt()),
                lineHeight = 32f
            )
        }
    }

    // ----------------------------------------------------------- Achievement

    private fun renderAchievement(canvas: Canvas, w: Float, h: Float, data: ShareCardData) {
        val achievement = data.achievement ?: return

        // Title
        drawCenteredText(
            canvas, "成就解锁!", w / 2, h * 0.10f,
            paint = titlePaint(40f, accentColor(data.template))
        )

        // Medal circle
        val medalPaint = Paint().apply {
            isAntiAlias = true
            shader = LinearGradient(
                w * 0.35f, h * 0.18f, w * 0.65f, h * 0.48f,
                intArrayOf(0xFFFFD700.toInt(), 0xFFFFA500.toInt()),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(w / 2, h * 0.33f, w * 0.15f, medalPaint)

        // Medal inner ring
        val ringPaint = Paint().apply {
            isAntiAlias = true
            color = 0xCCFFFFFF.toInt()
            strokeWidth = 6f
            style = Paint.Style.STROKE
        }
        canvas.drawCircle(w / 2, h * 0.33f, w * 0.15f - 10f, ringPaint)

        // Achievement emoji
        val emojiPaint = Paint().apply {
            isAntiAlias = true
            textSize = 80f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(achievement.emoji, w / 2, h * 0.33f + 28f, emojiPaint)

        // Achievement title
        drawCenteredText(
            canvas, achievement.title, w / 2, h * 0.55f,
            paint = namePaint(36f)
        )

        // "I unlocked xxx!"
        drawCenteredText(
            canvas, "我解锁了「${achievement.title}」!", w / 2, h * 0.63f,
            paint = bodyPaint(28f, accentColor(data.template))
        )

        // Description
        drawWrappedText(
            canvas, achievement.description, w * 0.12f, w * 0.88f, h * 0.72f,
            paint = bodyPaint(24f, 0xFF666666.toInt()),
            lineHeight = 34f
        )

        // Reward
        drawCenteredText(
            canvas, "奖励 💎 ${achievement.rewardDiamonds}", w / 2, h * 0.88f,
            paint = bodyPaint(26f, 0xFFFFB300.toInt())
        )
    }

    // ----------------------------------------------------------- Postcard

    private fun renderPostcard(canvas: Canvas, w: Float, h: Float, data: ShareCardData) {
        val postcard = data.postcard ?: return
        val pet = data.pet

        // Postcard frame
        val framePaint = Paint().apply {
            isAntiAlias = true
            color = 0xFFFFFFFF.toInt()
        }
        val frameInset = 40f
        canvas.drawRoundRect(
            RectF(frameInset, h * 0.08f, w - frameInset, h * 0.92f),
            20f, 20f, framePaint
        )

        // Destination scene (simplified landscape)
        drawSceneBackground(canvas, w, h, postcard.sceneDrawKey)

        // Pet in travel pose
        drawPetBody(
            canvas, w * 0.30f, h * 0.30f, w * 0.40f, h * 0.65f,
            pet.color, pet.species
        )

        // Destination name
        drawCenteredText(
            canvas, "${postcard.destinationEmoji} ${postcard.destinationName}",
            w / 2, h * 0.72f,
            paint = titlePaint(32f, accentColor(data.template))
        )

        // Date
        drawCenteredText(
            canvas, postcard.date, w / 2, h * 0.78f,
            paint = bodyPaint(22f, 0xFF999999.toInt())
        )

        // Handwritten message
        drawWrappedText(
            canvas, postcard.message, w * 0.12f, w * 0.88f, h * 0.84f,
            paint = italicPaint(24f, 0xFF555555.toInt()),
            lineHeight = 34f
        )
    }

    // ----------------------------------------------------------- Check-in Streak

    private fun renderCheckinStreak(canvas: Canvas, w: Float, h: Float, data: ShareCardData) {
        val pet = data.pet
        val days = data.streakDays

        // Title
        drawCenteredText(
            canvas, "习惯打卡", w / 2, h * 0.10f,
            paint = titlePaint(36f, accentColor(data.template))
        )

        // Big streak number
        val numberPaint = Paint().apply {
            isAntiAlias = true
            textSize = 140f
            textAlign = Paint.Align.CENTER
            color = accentColor(data.template)
            isFakeBoldText = true
        }
        canvas.drawText("${days}", w / 2, h * 0.30f, numberPaint)

        drawCenteredText(
            canvas, "天连续打卡", w / 2, h * 0.38f,
            paint = namePaint(30f)
        )

        // Celebration pet
        drawPetBody(
            canvas, w * 0.25f, h * 0.45f, w * 0.50f, h * 0.75f,
            pet.color, pet.species
        )

        // Message
        val message = when {
            days >= 30 -> "坚持就是胜利，团子为你骄傲!"
            days >= 7 -> "一周打卡达成，继续加油~"
            days >= 3 -> "连续三天啦，好习惯养成中!"
            else -> "打卡成功，团子为你加油!"
        }
        drawWrappedText(
            canvas, message, w * 0.12f, w * 0.88f, h * 0.82f,
            paint = bodyPaint(26f, 0xFF555555.toInt()),
            lineHeight = 36f
        )
    }

    // ----------------------------------------------------------- Pet Drawing

    private fun drawPetBody(
        canvas: Canvas,
        left: Float, top: Float,
        right: Float, bottom: Float,
        petColor: PetColor,
        species: PetSpecies
    ) {
        val w = right - left
        val h = bottom - top
        val bodyColor = android.graphics.Color.parseColor(petColor.hex)
        val bodyPaint = Paint().apply { isAntiAlias = true; color = bodyColor }

        // Background circle
        val bgCirclePaint = Paint().apply {
            isAntiAlias = true
            color = 0x22FFFFFF.toInt()
        }
        canvas.drawCircle(left + w / 2, top + h / 2, w * 0.48f, bgCirclePaint)

        // Body
        val bodyRect = RectF(left + w * 0.15f, top + h * 0.15f, left + w * 0.85f, top + h * 0.80f)
        canvas.drawRoundRect(bodyRect, w * 0.25f, w * 0.25f, bodyPaint)

        // Ears by species
        when (species) {
            PetSpecies.CAT -> {
                val leftEar = Path()
                leftEar.moveTo(left + w * 0.22f, top + h * 0.20f)
                leftEar.lineTo(left + w * 0.12f, top + h * 0.02f)
                leftEar.lineTo(left + w * 0.34f, top + h * 0.12f)
                leftEar.close()
                canvas.drawPath(leftEar, bodyPaint)
                val rightEar = Path()
                rightEar.moveTo(left + w * 0.78f, top + h * 0.20f)
                rightEar.lineTo(left + w * 0.88f, top + h * 0.02f)
                rightEar.lineTo(left + w * 0.66f, top + h * 0.12f)
                rightEar.close()
                canvas.drawPath(rightEar, bodyPaint)
            }
            PetSpecies.DOG -> {
                val darker = darkenColor(bodyColor)
                val earPaint = Paint().apply { isAntiAlias = true; color = darker }
                canvas.drawOval(RectF(left + w * 0.05f, top + h * 0.15f, left + w * 0.22f, top + h * 0.45f), earPaint)
                canvas.drawOval(RectF(left + w * 0.78f, top + h * 0.15f, left + w * 0.95f, top + h * 0.45f), earPaint)
            }
            PetSpecies.RABBIT -> {
                canvas.drawOval(RectF(left + w * 0.36f, top + h * 0.0f, left + w * 0.44f, top + h * 0.25f), bodyPaint)
                canvas.drawOval(RectF(left + w * 0.56f, top + h * 0.0f, left + w * 0.64f, top + h * 0.25f), bodyPaint)
            }
            PetSpecies.HAMSTER -> {
                canvas.drawCircle(left + w * 0.30f, top + h * 0.15f, w * 0.07f, bodyPaint)
                canvas.drawCircle(left + w * 0.70f, top + h * 0.15f, w * 0.07f, bodyPaint)
            }
        }

        // Eyes
        val eyeWhitePaint = Paint().apply { isAntiAlias = true; color = 0xFFFFFFFF.toInt() }
        val eyePupilPaint = Paint().apply { isAntiAlias = true; color = 0xFF2D2420.toInt() }
        val eyeY = top + h * 0.38f
        canvas.drawCircle(left + w * 0.35f, eyeY, w * 0.05f, eyeWhitePaint)
        canvas.drawCircle(left + w * 0.65f, eyeY, w * 0.05f, eyeWhitePaint)
        canvas.drawCircle(left + w * 0.35f, eyeY, w * 0.025f, eyePupilPaint)
        canvas.drawCircle(left + w * 0.65f, eyeY, w * 0.025f, eyePupilPaint)

        // Blush
        val blushPaint = Paint().apply { isAntiAlias = true; color = 0x66F4A7B9.toInt() }
        canvas.drawCircle(left + w * 0.25f, top + h * 0.50f, w * 0.06f, blushPaint)
        canvas.drawCircle(left + w * 0.75f, top + h * 0.50f, w * 0.06f, blushPaint)

        // Smile
        val mouthPaint = Paint().apply {
            isAntiAlias = true
            color = 0xFFFFFFFF.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 5f
        }
        val mouthPath = Path()
        mouthPath.moveTo(left + w * 0.42f, top + h * 0.58f)
        mouthPath.quadTo(left + w * 0.50f, top + h * 0.65f, left + w * 0.58f, top + h * 0.58f)
        canvas.drawPath(mouthPath, mouthPaint)
    }

    // ----------------------------------------------------------- Scene Drawing

    private fun drawSceneBackground(canvas: Canvas, w: Float, h: Float, sceneKey: String) {
        val skyPaint = Paint().apply { isAntiAlias = true }
        when {
            sceneKey.contains("beach", ignoreCase = true) || sceneKey.contains("sea", ignoreCase = true) -> {
                skyPaint.shader = LinearGradient(0f, h * 0.08f, 0f, h * 0.55f,
                    intArrayOf(0xFF87CEEB.toInt(), 0xFF4682B4.toInt()), null, Shader.TileMode.CLAMP)
                canvas.drawRect(0f, h * 0.08f, w, h * 0.55f, skyPaint)
                // Sand
                val sandPaint = Paint().apply { isAntiAlias = true; color = 0xFFF4E4BC.toInt() }
                canvas.drawRect(0f, h * 0.55f, w, h * 0.72f, sandPaint)
            }
            sceneKey.contains("mountain", ignoreCase = true) || sceneKey.contains("snow", ignoreCase = true) -> {
                skyPaint.shader = LinearGradient(0f, h * 0.08f, 0f, h * 0.55f,
                    intArrayOf(0xFFB0E0E6.toInt(), 0xFFE0F0FF.toInt()), null, Shader.TileMode.CLAMP)
                canvas.drawRect(0f, h * 0.08f, w, h * 0.55f, skyPaint)
                // Mountain silhouette
                val mtnPaint = Paint().apply { isAntiAlias = true; color = 0xFF8FBC8F.toInt() }
                val mtnPath = Path()
                mtnPath.moveTo(w * 0.2f, h * 0.55f)
                mtnPath.lineTo(w * 0.4f, h * 0.28f)
                mtnPath.lineTo(w * 0.6f, h * 0.55f)
                mtnPath.close()
                canvas.drawPath(mtnPath, mtnPaint)
            }
            sceneKey.contains("forest", ignoreCase = true) || sceneKey.contains("park", ignoreCase = true) -> {
                skyPaint.shader = LinearGradient(0f, h * 0.08f, 0f, h * 0.55f,
                    intArrayOf(0xFFB0E0E6.toInt(), 0xFFC1E8C1.toInt()), null, Shader.TileMode.CLAMP)
                canvas.drawRect(0f, h * 0.08f, w, h * 0.55f, skyPaint)
                // Trees
                val treePaint = Paint().apply { isAntiAlias = true; color = 0xFF228B22.toInt() }
                canvas.drawCircle(w * 0.2f, h * 0.48f, w * 0.08f, treePaint)
                canvas.drawCircle(w * 0.8f, h * 0.48f, w * 0.08f, treePaint)
            }
            sceneKey.contains("star", ignoreCase = true) || sceneKey.contains("sky", ignoreCase = true) -> {
                skyPaint.shader = LinearGradient(0f, h * 0.08f, 0f, h * 0.65f,
                    intArrayOf(0xFF1A1A2E.toInt(), 0xFF4A4A6E.toInt()), null, Shader.TileMode.CLAMP)
                canvas.drawRect(0f, h * 0.08f, w, h * 0.65f, skyPaint)
                // Stars
                val starPaint = Paint().apply { isAntiAlias = true; color = 0xFFFFFFFF.toInt() }
                for (i in 0..20) {
                    val sx = ((i * 37) % 100) / 100f * w
                    val sy = h * 0.10f + ((i * 53) % 100) / 100f * h * 0.40f
                    canvas.drawCircle(sx, sy, 3f, starPaint)
                }
            }
            else -> {
                skyPaint.shader = LinearGradient(0f, h * 0.08f, 0f, h * 0.55f,
                    intArrayOf(0xFFFFF0E0.toInt(), 0xFFFFE0C0.toInt()), null, Shader.TileMode.CLAMP)
                canvas.drawRect(0f, h * 0.08f, w, h * 0.55f, skyPaint)
            }
        }
    }

    // ----------------------------------------------------------- Watermark

    private fun drawWatermark(canvas: Canvas, w: Float, h: Float, petName: String) {
        val paint = Paint().apply {
            isAntiAlias = true
            color = 0x66000000.toInt()
            textSize = 28f
            textAlign = Paint.Align.LEFT
        }
        val dateStr = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date())
        canvas.drawText("团子 DeskPet  $petName  $dateStr", 36f, h - 30f, paint)
    }

    // ----------------------------------------------------------- Stats Bar

    private fun drawStatsBar(canvas: Canvas, w: Float, y: Float, pet: Pet) {
        val stats = listOf(
            "饱腹" to pet.hunger,
            "心情" to pet.mood,
            "亲密" to pet.intimacy
        )
        val barWidth = w * 0.20f
        val gap = w * 0.05f
        val startX = (w - (barWidth * 3 + gap * 2)) / 2

        stats.forEachIndexed { index, (label, value) ->
            val x = startX + index * (barWidth + gap)

            // Label
            val labelPaint = Paint().apply {
                isAntiAlias = true
                textSize = 20f
                textAlign = Paint.Align.LEFT
                color = 0xFF888888.toInt()
            }
            canvas.drawText(label, x, y - 12f, labelPaint)

            // Bar background
            val barBgPaint = Paint().apply {
                isAntiAlias = true
                color = 0x33000000.toInt()
            }
            canvas.drawRoundRect(
                RectF(x, y, x + barWidth, y + 8f),
                4f, 4f, barBgPaint
            )

            // Bar fill
            val barFillPaint = Paint().apply {
                isAntiAlias = true
                color = accentColor(CardTemplate.FRESH_PINK)
            }
            val fillWidth = barWidth * (value / 100f).coerceIn(0f, 1f)
            canvas.drawRoundRect(
                RectF(x, y, x + fillWidth, y + 8f),
                4f, 4f, barFillPaint
            )
        }
    }

    // ----------------------------------------------------------- Text Helpers

    private fun drawCenteredText(canvas: Canvas, text: String, cx: Float, y: Float, paint: Paint) {
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(text, cx, y, paint)
    }

    private fun drawWrappedText(
        canvas: Canvas, text: String,
        left: Float, right: Float, startY: Float,
        paint: Paint, lineHeight: Float
    ) {
        paint.textAlign = Paint.Align.LEFT
        val maxWidth = right - left
        val words = text.toCharArray()
        val current = StringBuilder()
        var y = startY

        for (ch in words) {
            current.append(ch)
            val width = paint.measureText(current.toString())
            if (width > maxWidth || ch == '\n') {
                if (ch == '\n') {
                    canvas.drawText(current.dropLast(1).toString(), left, y, paint)
                    current.clear()
                } else {
                    current.deleteCharAt(current.length - 1)
                    canvas.drawText(current.toString(), left, y, paint)
                    current.clear()
                    current.append(ch)
                }
                y += lineHeight
                if (y > startY + lineHeight * 3) break
            }
        }
        if (current.isNotEmpty() && y <= startY + lineHeight * 3) {
            canvas.drawText(current.toString(), left, y, paint)
        }
    }

    // ----------------------------------------------------------- Paint Factory

    private fun titlePaint(size: Float, color: Int) = Paint().apply {
        isAntiAlias = true
        textSize = size
        this.color = color
        isFakeBoldText = true
    }

    private fun namePaint(size: Float) = Paint().apply {
        isAntiAlias = true
        textSize = size
        color = 0xFF333333.toInt()
        isFakeBoldText = true
    }

    private fun bodyPaint(size: Float, color: Int) = Paint().apply {
        isAntiAlias = true
        textSize = size
        this.color = color
    }

    private fun italicPaint(size: Float, color: Int) = Paint().apply {
        isAntiAlias = true
        textSize = size
        this.color = color
        isFakeBoldText = false
        // Simulate italic with slight skew
        textSkewX = -0.25f
    }

    private fun accentColor(template: CardTemplate): Int = when (template) {
        CardTemplate.FRESH_PINK -> 0xFFE91E63.toInt()
        CardTemplate.HEALING_GREEN -> 0xFF4CAF50.toInt()
        CardTemplate.MINIMAL_WHITE -> 0xFF555555.toInt()
    }

    private fun darkenColor(color: Int): Int {
        return (color and 0xFF000000.toInt()) or
            ((android.graphics.Color.red(color) * 0.82f).toInt() shl 16) or
            ((android.graphics.Color.green(color) * 0.82f).toInt() shl 8) or
            (android.graphics.Color.blue(color) * 0.82f).toInt()
    }

    // ----------------------------------------------------------- Save & Share

    /**
     * Saves a bitmap to the app's external pictures directory and returns a
     * content URI via FileProvider.
     */
    fun saveBitmap(context: Context, bitmap: Bitmap, cardType: ShareCardType): Uri? {
        val dir = File(
            context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES),
            "deskpet_sharecards"
        )
        if (!dir.exists()) dir.mkdirs()

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "sharecard_${cardType.name.lowercase()}_$timestamp.png"
        val file = File(dir, fileName)

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
     * Convenience: render + save + launch share intent in one call.
     * Returns true if the share sheet was launched successfully.
     */
    fun renderAndShare(context: Context, data: ShareCardData): Boolean {
        val bitmap = render(data)
        val uri = saveBitmap(context, bitmap, data.type) ?: return false
        PhotoHelper.launchShare(context, uri)
        return true
    }
}
