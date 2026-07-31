package com.deskpet.app.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.deskpet.app.data.model.OutfitCategory
import com.deskpet.app.data.model.PetSpecies

object OutfitRenderer {

    private val GoldColor = Color(0xFFFFD700)
    private val GoldHi = Color(0xFFFFEB82)
    private val GoldDk = Color(0xFFD4A017)
    private val SilverColor = Color(0xFFC0C0C0)
    private val RedColor = Color(0xFFE8392B)
    private val RedHi = Color(0xFFFF7864)
    private val RedDk = Color(0xFFB41E14)
    private val PinkAccent = Color(0xFFFF6B9D)
    private val PinkHi = Color(0xFFFFB3CE)
    private val PinkDk = Color(0xFFE44D82)
    private val DarkAccent = Color(0xFF2D2420)
    private val BlueHi = Color(0xFFB4E1FF)
    private val BlueColor = Color(0xFF4FC3F7)
    private val BlueDk = Color(0xFF2896DC)

    // Emoji lookup for all outfit IDs (fallback rendering)
    private val emojiMap = mapOf(
        "head_bow" to "🎀", "head_flower" to "🌸", "head_beanie" to "🧢",
        "head_santa" to "🎅", "head_hat" to "🎩", "head_crown" to "👑",
        "head_headphone" to "🎧", "head_tophat" to "🎩",
        "glasses_round" to "👓", "glasses_sun" to "🕶️", "glasses_3d" to "🎞️",
        "glasses_star" to "🤓", "glasses_monocle" to "🧐",
        "glasses_party" to "🥳", "glasses_neon" to "😎", "glasses_heart" to "😍",
        "collar_bell" to "🔔", "collar_bow" to "🎀", "collar_ribbon" to "🎀",
        "collar_pearl" to "📿", "collar_gold" to "💰", "collar_bone" to "🦴",
        "collar_crystal" to "💎", "collar_flower" to "🌺",
        "cloth_scarf" to "🧣", "cloth_sweater" to "👕", "cloth_dress" to "👗",
        "cloth_cape" to "🧥", "cloth_suit" to "🤵", "cloth_kimono" to "👘",
        "cloth_swimsuit" to "🩱", "cloth_pajama" to "🩲",
        "tail_ribbon" to "🎀", "tail_star" to "⭐", "tail_flower" to "🌸",
        "tail_balloon" to "🎈", "tail_butterfly" to "🦋", "tail_rainbow" to "🌈",
        "tail_cloud" to "☁️", "tail_heart" to "💕",
        "acc_balloon" to "🎈", "acc_lollipop" to "🍭", "acc_umbrella" to "☂️",
        "acc_wand" to "🪄", "acc_book" to "📖", "acc_camera" to "📷",
        "acc_gift" to "🎁", "acc_star" to "✨"
    )

    fun getEmoji(outfitId: String): String = emojiMap[outfitId] ?: "⭐"

    fun DrawScope.render(
        outfitId: String,
        category: OutfitCategory,
        species: PetSpecies,
        w: Float,
        h: Float
    ): Boolean {
        val (cx, cy, sizeFactor) = getPosition(category, species, w, h)
        val r = w * sizeFactor

        val rendered = when (outfitId) {
            "head_bow" -> { drawBow(cx, cy, r); true }
            "head_flower" -> { drawFlower(cx, cy, r); true }
            "head_crown" -> { drawCrown(cx, cy, r); true }
            "head_beanie" -> { drawBeanie(cx, cy, r); true }
            "head_santa" -> { drawSantaHat(cx, cy, r); true }
            "head_hat" -> { drawTopHat(cx, cy, r); true }
            "head_tophat" -> { drawTopHat(cx, cy, r); true }
            "glasses_round" -> { drawRoundGlasses(cx, cy, r); true }
            "glasses_sun" -> { drawSunglasses(cx, cy, r); true }
            "glasses_heart" -> { drawHeartGlasses(cx, cy, r); true }
            "collar_bell" -> { drawBellCollar(cx, cy, r); true }
            "collar_ribbon" -> { drawRibbonCollar(cx, cy, r); true }
            "cloth_scarf" -> { drawScarf(cx, cy, r); true }
            "tail_ribbon" -> { drawTailRibbon(cx, cy, r); true }
            "tail_star" -> { drawStar(cx, cy, r); true }
            "acc_balloon" -> { drawBalloon(cx, cy, r); true }
            else -> false
        }
        return rendered
    }

    private fun getPosition(
        category: OutfitCategory,
        species: PetSpecies,
        w: Float,
        h: Float
    ): Triple<Float, Float, Float> = when (category) {
        OutfitCategory.HEAD -> when (species) {
            PetSpecies.RABBIT -> Triple(w * 0.5f, h * 0.15f, 0.14f)
            PetSpecies.HAMSTER -> Triple(w * 0.5f, h * 0.22f, 0.15f)
            else -> Triple(w * 0.5f, h * 0.20f, 0.15f)
        }
        OutfitCategory.GLASSES -> Triple(w * 0.5f, h * 0.40f, 0.14f)
        OutfitCategory.COLLAR -> Triple(w * 0.5f, h * 0.58f, 0.14f)
        OutfitCategory.CLOTHING -> when (species) {
            PetSpecies.RABBIT -> Triple(w * 0.5f, h * 0.68f, 0.20f)
            else -> Triple(w * 0.5f, h * 0.70f, 0.20f)
        }
        OutfitCategory.TAIL -> Triple(w * 0.82f, h * 0.76f, 0.12f)
        OutfitCategory.ACCESSORY -> Triple(w * 0.12f, h * 0.42f, 0.12f)
    }

    // ============================================================
    // WATERCOLOR HELPER
    // ============================================================
    private fun DrawScope.drawWatercolorBlob(
        cx: Float, cy: Float, rx: Float, ry: Float,
        mainColor: Color, hiColor: Color, dkColor: Color
    ) {
        val maxR = maxOf(rx, ry)
        val halo = Brush.radialGradient(
            colors = listOf(mainColor.copy(alpha = 0.12f), mainColor.copy(alpha = 0f)),
            center = Offset(cx, cy),
            radius = maxR * 2.0f
        )
        drawOval(
            brush = halo,
            topLeft = Offset(cx - rx * 1.8f, cy - ry * 1.8f),
            size = Size(rx * 3.6f, ry * 3.6f)
        )
        val body = Brush.radialGradient(
            0f to hiColor,
            0.5f to mainColor,
            0.85f to dkColor.copy(alpha = 0.85f),
            1f to dkColor.copy(alpha = 0f),
            center = Offset(cx - rx * 0.2f, cy - ry * 0.25f),
            radius = maxR * 1.1f
        )
        drawOval(
            brush = body,
            topLeft = Offset(cx - rx, cy - ry),
            size = Size(rx * 2f, ry * 2f)
        )
        val hl = Brush.radialGradient(
            colors = listOf(hiColor.copy(alpha = 0.50f), hiColor.copy(alpha = 0f)),
            center = Offset(cx - rx * 0.3f, cy - ry * 0.4f),
            radius = rx * 0.5f
        )
        drawOval(
            brush = hl,
            topLeft = Offset(cx - rx * 0.7f, cy - ry * 0.75f),
            size = Size(rx * 1.1f, ry * 0.9f)
        )
    }

    private fun DrawScope.drawWatercolorCircle(
        cx: Float, cy: Float, r: Float,
        mainColor: Color, hiColor: Color, dkColor: Color
    ) {
        drawWatercolorBlob(cx, cy, r, r, mainColor, hiColor, dkColor)
    }

    private fun DrawScope.drawWatercolorLeaf(
        cx: Float, cy: Float, r: Float,
        mainColor: Color, hiColor: Color, dkColor: Color,
        angle: Float
    ) {
        val path = Path().apply {
            val cos = Math.cos(angle.toDouble()).toFloat()
            val sin = Math.sin(angle.toDouble()).toFloat()
            val topX = cx - (-r) * sin
            val topY = cy - r * cos
            val botX = cx - r * sin
            val botY = cy + r * cos
            val ctrl1X = cx + r * 0.6f * cos
            val ctrl1Y = cy + r * 0.6f * sin
            val ctrl2X = cx + (-r * 0.6f) * cos
            val ctrl2Y = cy + (-r * 0.6f) * sin
            moveTo(topX, topY)
            cubicTo(ctrl1X, ctrl1Y, ctrl1X, ctrl1Y, botX, botY)
            cubicTo(ctrl2X, ctrl2Y, ctrl2X, ctrl2Y, topX, topY)
            close()
        }
        val g = Brush.linearGradient(
            colors = listOf(hiColor, mainColor, dkColor),
            start = Offset(cx, cy - r),
            end = Offset(cx, cy + r)
        )
        drawPath(path, g)
    }

    // ============================================================
    // DECORATIONS
    // ============================================================
    private fun DrawScope.drawBow(cx: Float, cy: Float, r: Float) {
        drawWatercolorLeaf(cx - r * 0.5f, cy, r * 0.6f, PinkAccent, PinkHi, PinkDk, -0.3f)
        drawWatercolorLeaf(cx + r * 0.5f, cy, r * 0.6f, PinkAccent, PinkHi, PinkDk, 0.3f)
        drawWatercolorCircle(cx, cy, r * 0.22f, PinkDk, PinkAccent, PinkDk)
    }

    private fun DrawScope.drawFlower(cx: Float, cy: Float, r: Float) {
        repeat(5) { i ->
            val angle = (i * 72.0 - 90.0) * Math.PI / 180.0
            val px = cx + (r * 0.55f * Math.cos(angle)).toFloat()
            val py = cy + (r * 0.55f * Math.sin(angle)).toFloat()
            drawWatercolorCircle(px, py, r * 0.35f, PinkAccent, PinkHi, PinkDk)
        }
        drawWatercolorCircle(cx, cy, r * 0.28f, GoldColor, GoldHi, GoldDk)
    }

    private fun DrawScope.drawCrown(cx: Float, cy: Float, r: Float) {
        val path = Path().apply {
            moveTo(cx - r, cy + r * 0.4f)
            lineTo(cx - r, cy - r * 0.1f)
            lineTo(cx - r * 0.5f, cy + r * 0.15f)
            lineTo(cx, cy - r * 0.5f)
            lineTo(cx + r * 0.5f, cy + r * 0.15f)
            lineTo(cx + r, cy - r * 0.1f)
            lineTo(cx + r, cy + r * 0.4f)
            close()
        }
        val g = Brush.linearGradient(
            colors = listOf(GoldHi, GoldColor, GoldDk),
            start = Offset(cx, cy - r * 0.5f),
            end = Offset(cx, cy + r * 0.4f)
        )
        drawPath(path, g)
        drawWatercolorCircle(cx, cy - r * 0.2f, r * 0.08f, PinkAccent, PinkHi, PinkDk)
        drawWatercolorCircle(cx - r * 0.5f, cy + r * 0.05f, r * 0.06f, BlueColor, BlueHi, BlueDk)
        drawWatercolorCircle(cx + r * 0.5f, cy + r * 0.05f, r * 0.06f, BlueColor, BlueHi, BlueDk)
    }

    private fun DrawScope.drawBeanie(cx: Float, cy: Float, r: Float) {
        val g = Brush.linearGradient(
            colors = listOf(RedHi, RedColor, RedDk),
            start = Offset(cx, cy - r),
            end = Offset(cx, cy + r)
        )
        drawArc(
            brush = g,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(cx - r, cy - r),
            size = Size(r * 2, r * 2)
        )
        drawCircle(RedDk, r * 0.12f, Offset(cx, cy - r))
    }

    private fun DrawScope.drawSantaHat(cx: Float, cy: Float, r: Float) {
        val g = Brush.linearGradient(
            colors = listOf(Color.White, Color(0xFFFFF5F5)),
            start = Offset(cx, cy - r),
            end = Offset(cx, cy + r * 0.5f)
        )
        drawArc(
            brush = Brush.linearGradient(
                colors = listOf(RedHi, RedColor, RedDk),
                start = Offset(cx, cy - r),
                end = Offset(cx, cy + r * 0.5f)
            ),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(cx - r, cy - r * 0.8f),
            size = Size(r * 2, r * 1.6f)
        )
        drawOval(
            brush = g,
            topLeft = Offset(cx - r * 1.1f, cy + r * 0.1f),
            size = Size(r * 2.2f, r * 0.35f)
        )
        drawCircle(Color.White, r * 0.22f, Offset(cx + r * 0.6f, cy - r * 0.6f))
    }

    private fun DrawScope.drawTopHat(cx: Float, cy: Float, r: Float) {
        val g = Brush.linearGradient(
            colors = listOf(DarkAccent, Color(0xFF4A4036)),
            start = Offset(cx, cy - r),
            end = Offset(cx, cy + r)
        )
        drawRoundRect(
            brush = g,
            topLeft = Offset(cx - r * 0.6f, cy - r * 0.9f),
            size = Size(r * 1.2f, r * 1.3f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(r * 0.1f, r * 0.1f)
        )
        drawRoundRect(
            brush = g,
            topLeft = Offset(cx - r * 0.95f, cy + r * 0.35f),
            size = Size(r * 1.9f, r * 0.15f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(r * 0.05f, r * 0.05f)
        )
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(RedHi, RedColor, RedDk),
                start = Offset(cx, cy),
                end = Offset(cx, cy + r * 0.2f)
            ),
            topLeft = Offset(cx - r * 0.6f, cy + r * 0.15f),
            size = Size(r * 1.2f, r * 0.12f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(r * 0.05f, r * 0.05f)
        )
    }

    private fun DrawScope.drawRoundGlasses(cx: Float, cy: Float, r: Float) {
        val g = Brush.radialGradient(
            colors = listOf(Color(0x33FFFFFF), Color(0x00FFFFFF)),
            center = Offset(cx, cy),
            radius = r * 0.5f
        )
        drawCircle(
            brush = g,
            radius = r * 0.45f,
            center = Offset(cx - r * 0.55f, cy)
        )
        drawCircle(
            brush = g,
            radius = r * 0.45f,
            center = Offset(cx + r * 0.55f, cy)
        )
        drawCircle(DarkAccent, r * 0.5f, Offset(cx - r * 0.55f, cy), style = Stroke(width = r * 0.08f))
        drawCircle(DarkAccent, r * 0.5f, Offset(cx + r * 0.55f, cy), style = Stroke(width = r * 0.08f))
        drawLine(DarkAccent, Offset(cx - r * 0.05f, cy), Offset(cx + r * 0.05f, cy), r * 0.08f)
    }

    private fun DrawScope.drawSunglasses(cx: Float, cy: Float, r: Float) {
        val g = Brush.radialGradient(
            colors = listOf(Color(0xFF555555), Color(0xFF2D2420)),
            center = Offset(cx, cy),
            radius = r
        )
        drawRoundRect(
            brush = g,
            topLeft = Offset(cx - r, cy - r * 0.3f),
            size = Size(r * 2, r * 0.6f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(r * 0.15f, r * 0.15f)
        )
    }

    private fun DrawScope.drawHeartGlasses(cx: Float, cy: Float, r: Float) {
        val leftHeart = Path().apply {
            moveTo(cx - r * 0.55f, cy + r * 0.15f)
            cubicTo(cx - r * 0.55f - r * 0.4f, cy - r * 0.2f, cx - r * 0.55f - r * 0.3f, cy - r * 0.5f, cx - r * 0.55f, cy - r * 0.25f)
            cubicTo(cx - r * 0.55f + r * 0.3f, cy - r * 0.5f, cx - r * 0.55f + r * 0.4f, cy - r * 0.2f, cx - r * 0.55f, cy + r * 0.15f)
            close()
        }
        val g = Brush.radialGradient(
            colors = listOf(PinkHi, PinkAccent, PinkDk),
            center = Offset(cx - r * 0.55f, cy),
            radius = r * 0.6f
        )
        drawPath(leftHeart, g)
        val rightHeart = Path().apply {
            moveTo(cx + r * 0.55f, cy + r * 0.15f)
            cubicTo(cx + r * 0.55f - r * 0.4f, cy - r * 0.2f, cx + r * 0.55f - r * 0.3f, cy - r * 0.5f, cx + r * 0.55f, cy - r * 0.25f)
            cubicTo(cx + r * 0.55f + r * 0.3f, cy - r * 0.5f, cx + r * 0.55f + r * 0.4f, cy - r * 0.2f, cx + r * 0.55f, cy + r * 0.15f)
            close()
        }
        drawPath(rightHeart, g)
    }

    private fun DrawScope.drawBellCollar(cx: Float, cy: Float, r: Float) {
        drawLine(GoldDk, Offset(cx - r, cy), Offset(cx + r, cy), r * 0.10f)
        drawWatercolorCircle(cx, cy + r * 0.25f, r * 0.28f, GoldColor, GoldHi, GoldDk)
        drawLine(GoldDk, Offset(cx, cy + r * 0.1f), Offset(cx, cy + r * 0.35f), r * 0.04f)
    }

    private fun DrawScope.drawRibbonCollar(cx: Float, cy: Float, r: Float) {
        drawLine(PinkAccent, Offset(cx - r, cy), Offset(cx + r, cy), r * 0.08f)
        drawBow(cx, cy, r * 0.5f)
    }

    private fun DrawScope.drawScarf(cx: Float, cy: Float, r: Float) {
        val path = Path().apply {
            moveTo(cx - r, cy - r * 0.3f)
            lineTo(cx + r, cy - r * 0.3f)
            lineTo(cx + r * 0.8f, cy + r * 0.5f)
            lineTo(cx + r * 0.3f, cy + r * 0.3f)
            lineTo(cx - r * 0.3f, cy + r * 0.5f)
            lineTo(cx - r * 0.8f, cy + r * 0.3f)
            close()
        }
        val g = Brush.linearGradient(
            colors = listOf(RedHi, RedColor, RedDk),
            start = Offset(cx, cy - r * 0.3f),
            end = Offset(cx, cy + r * 0.5f)
        )
        drawPath(path, g)
        drawLine(RedDk, Offset(cx - r * 0.5f, cy - r * 0.2f), Offset(cx + r * 0.5f, cy - r * 0.2f), r * 0.04f)
    }

    private fun DrawScope.drawTailRibbon(cx: Float, cy: Float, r: Float) {
        drawBow(cx, cy, r)
    }

    private fun DrawScope.drawStar(cx: Float, cy: Float, r: Float) {
        val path = Path()
        for (i in 0..10) {
            val angle = (i * 36.0 - 90.0) * Math.PI / 180.0
            val radius = if (i % 2 == 0) r else r * 0.4f
            val x = cx + (radius * Math.cos(angle)).toFloat()
            val y = cy + (radius * Math.sin(angle)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        val g = Brush.radialGradient(
            colors = listOf(GoldHi, GoldColor, GoldDk),
            center = Offset(cx, cy),
            radius = r
        )
        drawPath(path, g)
    }

    private fun DrawScope.drawBalloon(cx: Float, cy: Float, r: Float) {
        val g = Brush.radialGradient(
            colors = listOf(PinkHi, PinkAccent, PinkDk),
            center = Offset(cx - r * 0.15f, cy - r * 0.3f),
            radius = r * 1.2f
        )
        drawOval(
            brush = g,
            topLeft = Offset(cx - r * 0.7f, cy - r),
            size = Size(r * 1.4f, r * 1.6f)
        )
        drawLine(PinkDk, Offset(cx, cy + r * 0.6f), Offset(cx, cy + r * 1.5f), r * 0.04f)
    }
}