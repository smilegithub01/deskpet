package com.deskpet.app.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.deskpet.app.data.model.OutfitCategory
import com.deskpet.app.data.model.PetSpecies

object OutfitRenderer {

    private val GoldColor = Color(0xFFFFD700)
    private val SilverColor = Color(0xFFC0C0C0)
    private val RedColor = Color(0xFFE8392B)
    private val PinkAccent = Color(0xFFFF6B9D)
    private val DarkAccent = Color(0xFF2D2420)

    fun DrawScope.render(
        outfitId: String,
        category: OutfitCategory,
        species: PetSpecies,
        w: Float,
        h: Float
    ): Boolean {
        val (cx, cy, _) = getPosition(category, species, w, h)

        val rendered = when (outfitId) {
            "head_bow" -> { drawBow(cx, cy, w * 0.12f, PinkAccent); true }
            "head_flower" -> { drawFlower(cx, cy, w * 0.10f); true }
            "head_crown" -> { drawCrown(cx, cy, w * 0.14f, GoldColor); true }
            "head_beanie" -> { drawBeanie(cx, cy, w * 0.16f, RedColor); true }
            "glasses_round" -> { drawRoundGlasses(cx, cy, w * 0.12f, DarkAccent); true }
            "glasses_sun" -> { drawSunglasses(cx, cy, w * 0.13f, DarkAccent); true }
            "glasses_heart" -> { drawHeartGlasses(cx, cy, w * 0.11f, PinkAccent); true }
            "collar_bell" -> { drawBellCollar(cx, cy, w * 0.14f, GoldColor); true }
            "collar_ribbon" -> { drawRibbonCollar(cx, cy, w * 0.13f, PinkAccent); true }
            "cloth_scarf" -> { drawScarf(cx, cy, w * 0.18f, RedColor); true }
            "tail_ribbon" -> { drawTailRibbon(cx, cy, w * 0.10f, PinkAccent); true }
            "tail_star" -> { drawStar(cx, cy, w * 0.10f, GoldColor); true }
            "acc_balloon" -> { drawBalloon(cx, cy, w * 0.12f, PinkAccent); true }
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
        drawCircle(color, r * 0.5f, Offset(cx - r * 0.55f, cy), style = Stroke(width = r * 0.1f))
        drawCircle(color, r * 0.5f, Offset(cx + r * 0.55f, cy), style = Stroke(width = r * 0.1f))
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

    private fun Color.darker(factor: Float = 0.7f): Color = Color(
        red = (red * factor).coerceIn(0f, 1f),
        green = (green * factor).coerceIn(0f, 1f),
        blue = (blue * factor).coerceIn(0f, 1f),
        alpha = alpha
    )
}
