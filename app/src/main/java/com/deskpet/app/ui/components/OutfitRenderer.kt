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
            "cloth_sweater" -> { drawSweater(cx, cy, r); true }
            "cloth_dress" -> { drawDress(cx, cy, r); true }
            "cloth_cape" -> { drawCape(cx, cy, r); true }
            "cloth_suit" -> { drawSuit(cx, cy, r); true }
            "cloth_kimono" -> { drawKimono(cx, cy, r); true }
            "cloth_swimsuit" -> { drawSwimsuit(cx, cy, r); true }
            "cloth_pajama" -> { drawPajama(cx, cy, r); true }
            "tail_ribbon" -> { drawTailRibbon(cx, cy, r); true }
            "tail_star" -> { drawStar(cx, cy, r); true }
            "acc_balloon" -> { drawBalloon(cx, cy, r); true }
            "glasses_3d" -> { draw3DGlasses(cx, cy, r); true }
            "glasses_star" -> { drawStarGlasses(cx, cy, r); true }
            "glasses_monocle" -> { drawMonocle(cx, cy, r); true }
            "glasses_party" -> { drawPartyGlasses(cx, cy, r); true }
            "glasses_neon" -> { drawNeonGlasses(cx, cy, r); true }
            "collar_bow" -> { drawBowCollar(cx, cy, r); true }
            "collar_pearl" -> { drawPearlCollar(cx, cy, r); true }
            "collar_gold" -> { drawGoldChain(cx, cy, r); true }
            "collar_bone" -> { drawBoneCollar(cx, cy, r); true }
            "collar_crystal" -> { drawCrystalCollar(cx, cy, r); true }
            "collar_flower" -> { drawFlowerCollar(cx, cy, r); true }
            "tail_flower" -> { drawFlower(cx, cy, r); true }
            "tail_balloon" -> { drawBalloon(cx, cy, r); true }
            "tail_butterfly" -> { drawButterflyTail(cx, cy, r); true }
            "tail_rainbow" -> { drawRainbowTail(cx, cy, r); true }
            "tail_cloud" -> { drawCloudTail(cx, cy, r); true }
            "tail_heart" -> { drawHeartTail(cx, cy, r); true }
            "acc_lollipop" -> { drawLollipop(cx, cy, r); true }
            "acc_umbrella" -> { drawUmbrella(cx, cy, r); true }
            "acc_wand" -> { drawWand(cx, cy, r); true }
            "acc_book" -> { drawBook(cx, cy, r); true }
            "acc_camera" -> { drawCamera(cx, cy, r); true }
            "acc_gift" -> { drawGift(cx, cy, r); true }
            "acc_star" -> { drawStarWand(cx, cy, r); true }
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

    private fun DrawScope.drawSweater(cx: Float, cy: Float, r: Float) {
        // 椭圆覆盖身体
        drawWatercolorBlob(cx, cy, r * 0.95f, r * 0.8f, RedColor, RedHi, RedDk)
        // 圆弧领口（U形开口）
        drawArc(
            color = RedDk,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(cx - r * 0.25f, cy - r * 0.45f),
            size = Size(r * 0.5f, r * 0.3f),
            style = Stroke(width = r * 0.06f)
        )
    }

    private fun DrawScope.drawDress(cx: Float, cy: Float, r: Float) {
        val topY = cy - r * 0.5f
        val bottomY = cy + r * 0.5f
        val tipY = cy + r * 0.7f
        val topHalf = r * 0.4f
        val bottomHalf = r * 0.9f
        val teeth = 6
        val teethWidth = (bottomHalf * 2) / teeth
        // 梯形 + 锯齿下摆
        val path = Path().apply {
            moveTo(cx - topHalf, topY)
            lineTo(cx + topHalf, topY)
            lineTo(cx + bottomHalf, bottomY)
            // 锯齿下摆（从右向左，三角形锯齿）
            for (i in 0 until teeth) {
                val tipX = cx + bottomHalf - (i + 0.5f) * teethWidth
                val nextBaseX = cx + bottomHalf - (i + 1f) * teethWidth
                lineTo(tipX, tipY)
                lineTo(nextBaseX, bottomY)
            }
            close()
        }
        val g = Brush.linearGradient(
            colors = listOf(PinkHi, PinkAccent, PinkDk),
            start = Offset(cx, topY),
            end = Offset(cx, tipY)
        )
        drawPath(path, g)
    }

    private fun DrawScope.drawCape(cx: Float, cy: Float, r: Float) {
        val topY = cy - r * 0.5f
        val shoulderHalf = r * 0.5f
        // 半圆弧形 + 波浪底边
        val path = Path().apply {
            moveTo(cx - shoulderHalf, topY)
            // 左侧弧形向下
            cubicTo(
                cx - r * 1.0f, cy - r * 0.1f,
                cx - r * 0.95f, cy + r * 0.4f,
                cx - r * 0.85f, cy + r * 0.5f
            )
            // 波浪底边（cubicTo画波浪）
            cubicTo(
                cx - r * 0.5f, cy + r * 0.85f,
                cx - r * 0.2f, cy + r * 0.55f,
                cx, cy + r * 0.85f
            )
            cubicTo(
                cx + r * 0.2f, cy + r * 0.55f,
                cx + r * 0.5f, cy + r * 0.85f,
                cx + r * 0.85f, cy + r * 0.5f
            )
            // 右侧弧形向上
            cubicTo(
                cx + r * 0.95f, cy + r * 0.4f,
                cx + r * 1.0f, cy - r * 0.1f,
                cx + shoulderHalf, topY
            )
            // 顶部领口（往下凹的弧）
            cubicTo(
                cx + r * 0.2f, topY + r * 0.1f,
                cx - r * 0.2f, topY + r * 0.1f,
                cx - shoulderHalf, topY
            )
            close()
        }
        val g = Brush.linearGradient(
            colors = listOf(RedHi, RedColor, RedDk),
            start = Offset(cx, topY),
            end = Offset(cx, cy + r * 0.85f)
        )
        drawPath(path, g)
    }

    private fun DrawScope.drawSuit(cx: Float, cy: Float, r: Float) {
        val suitHi = Color(0xFF4A4036)
        val suitDk = Color(0xFF1A1310)
        // 矩形身体
        val bodyPath = Path().apply {
            moveTo(cx - r * 0.8f, cy - r * 0.5f)
            lineTo(cx + r * 0.8f, cy - r * 0.5f)
            lineTo(cx + r * 0.8f, cy + r * 0.7f)
            lineTo(cx - r * 0.8f, cy + r * 0.7f)
            close()
        }
        val g = Brush.linearGradient(
            colors = listOf(suitHi, DarkAccent, suitDk),
            start = Offset(cx, cy - r * 0.5f),
            end = Offset(cx, cy + r * 0.7f)
        )
        drawPath(bodyPath, g)
        // V领衬衫（三角形）
        val vPath = Path().apply {
            moveTo(cx - r * 0.25f, cy - r * 0.5f)
            lineTo(cx + r * 0.25f, cy - r * 0.5f)
            lineTo(cx, cy + r * 0.2f)
            close()
        }
        drawPath(vPath, Color.White)
        // 领带
        val tiePath = Path().apply {
            moveTo(cx - r * 0.08f, cy - r * 0.05f)
            lineTo(cx + r * 0.08f, cy - r * 0.05f)
            lineTo(cx + r * 0.15f, cy + r * 0.5f)
            lineTo(cx, cy + r * 0.6f)
            lineTo(cx - r * 0.15f, cy + r * 0.5f)
            close()
        }
        drawPath(tiePath, Brush.linearGradient(
            colors = listOf(RedHi, RedColor, RedDk),
            start = Offset(cx, cy - r * 0.05f),
            end = Offset(cx, cy + r * 0.6f)
        ))
    }

    private fun DrawScope.drawKimono(cx: Float, cy: Float, r: Float) {
        val topY = cy - r * 0.5f
        val bottomY = cy + r * 0.7f
        // 梯形身体
        val bodyPath = Path().apply {
            moveTo(cx - r * 0.5f, topY)
            lineTo(cx + r * 0.5f, topY)
            lineTo(cx + r * 0.95f, bottomY)
            lineTo(cx - r * 0.95f, bottomY)
            close()
        }
        val g = Brush.linearGradient(
            colors = listOf(PinkHi, PinkAccent, PinkDk),
            start = Offset(cx, topY),
            end = Offset(cx, bottomY)
        )
        drawPath(bodyPath, g)
        // 交叉领 - 左
        val leftCollar = Path().apply {
            moveTo(cx - r * 0.5f, topY)
            lineTo(cx, cy - r * 0.05f)
            lineTo(cx - r * 0.05f, cy - r * 0.15f)
            lineTo(cx - r * 0.55f, topY + r * 0.2f)
            close()
        }
        drawPath(leftCollar, Color.White)
        // 交叉领 - 右
        val rightCollar = Path().apply {
            moveTo(cx + r * 0.5f, topY)
            lineTo(cx, cy - r * 0.05f)
            lineTo(cx + r * 0.05f, cy - r * 0.15f)
            lineTo(cx + r * 0.55f, topY + r * 0.2f)
            close()
        }
        drawPath(rightCollar, Color(0xFFF0F0F0))
        // 腰带（矩形）
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(GoldHi, GoldColor, GoldDk),
                start = Offset(cx, cy + r * 0.1f),
                end = Offset(cx, cy + r * 0.25f)
            ),
            topLeft = Offset(cx - r * 0.9f, cy + r * 0.1f),
            size = Size(r * 1.8f, r * 0.15f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(r * 0.05f, r * 0.05f)
        )
    }

    private fun DrawScope.drawSwimsuit(cx: Float, cy: Float, r: Float) {
        // 小椭圆主体
        drawWatercolorBlob(cx, cy + r * 0.1f, r * 0.55f, r * 0.65f, BlueColor, BlueHi, BlueDk)
        // 两条肩带
        drawLine(
            BlueDk,
            Offset(cx - r * 0.4f, cy - r * 0.5f),
            Offset(cx - r * 0.2f, cy + r * 0.1f),
            r * 0.06f
        )
        drawLine(
            BlueDk,
            Offset(cx + r * 0.4f, cy - r * 0.5f),
            Offset(cx + r * 0.2f, cy + r * 0.1f),
            r * 0.06f
        )
    }

    private fun DrawScope.drawPajama(cx: Float, cy: Float, r: Float) {
        // 椭圆主体（浅蓝睡衣）
        drawWatercolorBlob(cx, cy, r * 0.95f, r * 0.85f, BlueColor, BlueHi, BlueDk)
        // 多条横纹
        val stripeCount = 4
        for (i in 0 until stripeCount) {
            val y = cy - r * 0.4f + i * r * 0.25f
            drawLine(
                BlueDk,
                Offset(cx - r * 0.85f, y),
                Offset(cx + r * 0.85f, y),
                r * 0.04f
            )
        }
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

    // ============================================================
    // GLASSES
    // ============================================================
    private fun DrawScope.draw3DGlasses(cx: Float, cy: Float, r: Float) {
        val cr = androidx.compose.ui.geometry.CornerRadius(r * 0.12f, r * 0.12f)
        drawRoundRect(
            color = RedColor,
            topLeft = Offset(cx - r * 0.95f, cy - r * 0.3f),
            size = Size(r * 0.9f, r * 0.6f),
            cornerRadius = cr
        )
        drawRoundRect(
            color = BlueColor,
            topLeft = Offset(cx + r * 0.05f, cy - r * 0.3f),
            size = Size(r * 0.9f, r * 0.6f),
            cornerRadius = cr
        )
        drawLine(DarkAccent, Offset(cx - r * 0.05f, cy), Offset(cx + r * 0.05f, cy), r * 0.08f)
    }

    private fun DrawScope.drawStarGlasses(cx: Float, cy: Float, r: Float) {
        drawStar(cx - r * 0.55f, cy, r * 0.5f)
        drawStar(cx + r * 0.55f, cy, r * 0.5f)
        drawLine(DarkAccent, Offset(cx - r * 0.05f, cy), Offset(cx + r * 0.05f, cy), r * 0.06f)
    }

    private fun DrawScope.drawMonocle(cx: Float, cy: Float, r: Float) {
        drawCircle(GoldColor, r * 0.6f, Offset(cx, cy), style = Stroke(width = r * 0.1f))
        drawLine(
            GoldDk,
            Offset(cx + r * 0.42f, cy + r * 0.42f),
            Offset(cx + r * 1.1f, cy + r * 1.1f),
            r * 0.05f
        )
    }

    private fun DrawScope.drawPartyGlasses(cx: Float, cy: Float, r: Float) {
        drawCircle(DarkAccent, r * 0.5f, Offset(cx - r * 0.55f, cy), style = Stroke(width = r * 0.08f))
        drawCircle(DarkAccent, r * 0.5f, Offset(cx + r * 0.55f, cy), style = Stroke(width = r * 0.08f))
        drawLine(DarkAccent, Offset(cx - r * 0.05f, cy), Offset(cx + r * 0.05f, cy), r * 0.06f)
        val ribbonColors = listOf(RedColor, GoldColor, BlueColor, PinkAccent)
        var x = cx - r * 0.7f
        ribbonColors.forEach { c ->
            val tri = Path().apply {
                moveTo(x, cy - r * 0.75f)
                lineTo(x + r * 0.2f, cy - r * 0.75f)
                lineTo(x + r * 0.1f, cy - r * 0.35f)
                close()
            }
            drawPath(tri, c)
            x += r * 0.22f
        }
    }

    private fun DrawScope.drawNeonGlasses(cx: Float, cy: Float, r: Float) {
        val g = Brush.radialGradient(
            colors = listOf(Color(0xFF00FF00), Color(0xFFFF00FF)),
            center = Offset(cx, cy),
            radius = r
        )
        val cr = androidx.compose.ui.geometry.CornerRadius(r * 0.15f, r * 0.15f)
        drawRoundRect(
            brush = g,
            topLeft = Offset(cx - r, cy - r * 0.3f),
            size = Size(r * 2, r * 0.6f),
            cornerRadius = cr
        )
    }

    // ============================================================
    // COLLAR
    // ============================================================
    private fun DrawScope.drawBowCollar(cx: Float, cy: Float, r: Float) {
        drawLine(PinkDk, Offset(cx - r, cy), Offset(cx + r, cy), r * 0.08f)
        drawBow(cx, cy, r * 0.5f)
    }

    private fun DrawScope.drawPearlCollar(cx: Float, cy: Float, r: Float) {
        val n = 9
        val step = (r * 2) / (n - 1)
        for (i in 0 until n) {
            val px = cx - r + i * step
            drawWatercolorCircle(px, cy, r * 0.11f, Color.White, Color(0xFFFFF5F5), Color(0xFFE0E0E0))
        }
    }

    private fun DrawScope.drawGoldChain(cx: Float, cy: Float, r: Float) {
        drawLine(GoldColor, Offset(cx - r, cy), Offset(cx + r, cy), r * 0.06f)
        drawWatercolorCircle(cx, cy + r * 0.3f, r * 0.25f, GoldColor, GoldHi, GoldDk)
    }

    private fun DrawScope.drawBoneCollar(cx: Float, cy: Float, r: Float) {
        drawLine(DarkAccent, Offset(cx - r, cy), Offset(cx + r, cy), r * 0.06f)
        val bx = cx
        val by = cy + r * 0.6f
        drawLine(DarkAccent, Offset(cx, cy), Offset(bx, by - r * 0.3f), r * 0.03f)
        val boneColor = Color(0xFFFFF5E0)
        drawCircle(boneColor, r * 0.13f, Offset(bx - r * 0.3f, by - r * 0.12f))
        drawCircle(boneColor, r * 0.13f, Offset(bx - r * 0.3f, by + r * 0.12f))
        drawCircle(boneColor, r * 0.13f, Offset(bx + r * 0.3f, by - r * 0.12f))
        drawCircle(boneColor, r * 0.13f, Offset(bx + r * 0.3f, by + r * 0.12f))
        drawRect(
            color = boneColor,
            topLeft = Offset(bx - r * 0.28f, by - r * 0.07f),
            size = Size(r * 0.56f, r * 0.14f)
        )
    }

    private fun DrawScope.drawCrystalCollar(cx: Float, cy: Float, r: Float) {
        drawLine(DarkAccent, Offset(cx - r, cy), Offset(cx + r, cy), r * 0.06f)
        val bx = cx
        val by = cy + r * 0.6f
        drawLine(DarkAccent, Offset(cx, cy), Offset(bx, by - r * 0.3f), r * 0.03f)
        val diamond = Path().apply {
            moveTo(bx, by - r * 0.3f)
            lineTo(bx + r * 0.25f, by)
            lineTo(bx, by + r * 0.3f)
            lineTo(bx - r * 0.25f, by)
            close()
        }
        val g = Brush.linearGradient(
            colors = listOf(BlueHi, BlueColor, BlueDk),
            start = Offset(bx, by - r * 0.3f),
            end = Offset(bx, by + r * 0.3f)
        )
        drawPath(diamond, g)
    }

    private fun DrawScope.drawFlowerCollar(cx: Float, cy: Float, r: Float) {
        drawLine(PinkAccent, Offset(cx - r, cy), Offset(cx + r, cy), r * 0.06f)
        drawFlower(cx, cy + r * 0.2f, r * 0.4f)
    }

    // ============================================================
    // TAIL
    // ============================================================
    private fun DrawScope.drawButterflyTail(cx: Float, cy: Float, r: Float) {
        drawWatercolorLeaf(cx - r * 0.5f, cy, r * 0.6f, PinkAccent, PinkHi, PinkDk, -0.4f)
        drawWatercolorLeaf(cx + r * 0.5f, cy, r * 0.6f, PinkAccent, PinkHi, PinkDk, 0.4f)
        drawWatercolorCircle(cx, cy, r * 0.18f, DarkAccent, Color(0xFF4A4036), DarkAccent)
    }

    private fun DrawScope.drawRainbowTail(cx: Float, cy: Float, r: Float) {
        val colors = listOf(
            Color(0xFFFF0000), Color(0xFFFF7F00), Color(0xFFFFFF00),
            Color(0xFF00FF00), Color(0xFF0000FF), Color(0xFF4B0082), Color(0xFF9400D3)
        )
        var radius = r
        val sw = r * 0.14f
        colors.forEach { c ->
            drawArc(
                color = c,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(cx - radius, cy - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = sw)
            )
            radius -= sw
        }
    }

    private fun DrawScope.drawCloudTail(cx: Float, cy: Float, r: Float) {
        val mainColor = Color.White
        val hiColor = Color(0xFFFFF5F5)
        val dkColor = Color(0xFFE0E0E0)
        drawWatercolorCircle(cx - r * 0.5f, cy, r * 0.5f, mainColor, hiColor, dkColor)
        drawWatercolorCircle(cx + r * 0.5f, cy, r * 0.5f, mainColor, hiColor, dkColor)
        drawWatercolorCircle(cx, cy - r * 0.2f, r * 0.6f, mainColor, hiColor, dkColor)
    }

    private fun DrawScope.drawHeartTail(cx: Float, cy: Float, r: Float) {
        val heart = Path().apply {
            moveTo(cx, cy + r * 0.5f)
            cubicTo(cx - r * 0.8f, cy - r * 0.1f, cx - r * 0.5f, cy - r * 0.7f, cx, cy - r * 0.2f)
            cubicTo(cx + r * 0.5f, cy - r * 0.7f, cx + r * 0.8f, cy - r * 0.1f, cx, cy + r * 0.5f)
            close()
        }
        val g = Brush.radialGradient(
            colors = listOf(PinkHi, PinkAccent, PinkDk),
            center = Offset(cx, cy),
            radius = r
        )
        drawPath(heart, g)
    }

    // ============================================================
    // ACCESSORY
    // ============================================================
    private fun DrawScope.drawLollipop(cx: Float, cy: Float, r: Float) {
        drawWatercolorCircle(cx, cy - r * 0.3f, r * 0.5f, PinkAccent, PinkHi, PinkDk)
        drawArc(
            color = Color.White,
            startAngle = 0f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(cx - r * 0.4f, cy - r * 0.7f),
            size = Size(r * 0.8f, r * 0.8f),
            style = Stroke(width = r * 0.06f)
        )
        drawArc(
            color = PinkDk,
            startAngle = 0f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(cx - r * 0.25f, cy - r * 0.55f),
            size = Size(r * 0.5f, r * 0.5f),
            style = Stroke(width = r * 0.06f)
        )
        drawLine(DarkAccent, Offset(cx, cy + r * 0.2f), Offset(cx, cy + r), r * 0.06f)
    }

    private fun DrawScope.drawUmbrella(cx: Float, cy: Float, r: Float) {
        val colors = listOf(RedColor, GoldColor, BlueColor, PinkAccent)
        val seg = 180f / colors.size
        colors.forEachIndexed { i, c ->
            drawArc(
                color = c,
                startAngle = 180f + i * seg,
                sweepAngle = seg,
                useCenter = true,
                topLeft = Offset(cx - r, cy - r),
                size = Size(r * 2, r * 2)
            )
        }
        drawLine(DarkAccent, Offset(cx, cy), Offset(cx, cy + r * 0.8f), r * 0.06f)
        drawArc(
            color = DarkAccent,
            startAngle = 180f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(cx - r * 0.15f, cy + r * 0.8f),
            size = Size(r * 0.3f, r * 0.3f),
            style = Stroke(width = r * 0.06f)
        )
    }

    private fun DrawScope.drawWand(cx: Float, cy: Float, r: Float) {
        drawWatercolorCircle(cx, cy - r * 0.3f, r * 0.7f, GoldHi, Color(0xFFFFF5B0), GoldColor)
        drawStar(cx, cy - r * 0.3f, r * 0.5f)
        drawLine(DarkAccent, Offset(cx, cy + r * 0.2f), Offset(cx, cy + r), r * 0.06f)
    }

    private fun DrawScope.drawBook(cx: Float, cy: Float, r: Float) {
        val cr = androidx.compose.ui.geometry.CornerRadius(r * 0.08f, r * 0.08f)
        val g = Brush.linearGradient(
            colors = listOf(BlueHi, BlueColor, BlueDk),
            start = Offset(cx, cy - r),
            end = Offset(cx, cy + r)
        )
        drawRoundRect(
            brush = g,
            topLeft = Offset(cx - r * 0.7f, cy - r * 0.9f),
            size = Size(r * 1.4f, r * 1.8f),
            cornerRadius = cr
        )
        drawLine(
            DarkAccent,
            Offset(cx - r * 0.7f, cy - r * 0.9f),
            Offset(cx - r * 0.7f, cy + r * 0.9f),
            r * 0.06f
        )
        drawStar(cx + r * 0.1f, cy - r * 0.4f, r * 0.18f)
        drawStar(cx + r * 0.1f, cy, r * 0.15f)
        drawStar(cx + r * 0.1f, cy + r * 0.4f, r * 0.15f)
    }

    private fun DrawScope.drawCamera(cx: Float, cy: Float, r: Float) {
        val cr = androidx.compose.ui.geometry.CornerRadius(r * 0.15f, r * 0.15f)
        drawRoundRect(
            color = DarkAccent,
            topLeft = Offset(cx - r, cy - r * 0.5f),
            size = Size(r * 2, r * 1.2f),
            cornerRadius = cr
        )
        drawCircle(Color(0xFF4A4036), r * 0.35f, Offset(cx, cy + r * 0.1f))
        drawCircle(DarkAccent, r * 0.3f, Offset(cx, cy + r * 0.1f), style = Stroke(width = r * 0.08f))
        drawCircle(Color(0xFF888888), r * 0.1f, Offset(cx - r * 0.08f, cy + r * 0.02f))
        drawCircle(GoldHi, r * 0.08f, Offset(cx + r * 0.7f, cy - r * 0.3f))
    }

    private fun DrawScope.drawGift(cx: Float, cy: Float, r: Float) {
        val cr = androidx.compose.ui.geometry.CornerRadius(r * 0.08f, r * 0.08f)
        val g = Brush.linearGradient(
            colors = listOf(RedHi, RedColor, RedDk),
            start = Offset(cx, cy - r),
            end = Offset(cx, cy + r)
        )
        drawRoundRect(
            brush = g,
            topLeft = Offset(cx - r * 0.8f, cy - r * 0.8f),
            size = Size(r * 1.6f, r * 1.6f),
            cornerRadius = cr
        )
        drawLine(GoldColor, Offset(cx, cy - r * 0.8f), Offset(cx, cy + r * 0.8f), r * 0.1f)
        drawLine(GoldColor, Offset(cx - r * 0.8f, cy), Offset(cx + r * 0.8f, cy), r * 0.1f)
        drawBow(cx, cy - r * 0.8f, r * 0.4f)
    }

    private fun DrawScope.drawStarWand(cx: Float, cy: Float, r: Float) {
        drawStar(cx, cy - r * 0.3f, r * 0.5f)
        drawLine(DarkAccent, Offset(cx, cy + r * 0.2f), Offset(cx, cy + r), r * 0.06f)
    }
}