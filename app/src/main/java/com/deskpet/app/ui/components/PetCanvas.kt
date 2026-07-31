package com.deskpet.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import com.deskpet.app.data.model.OutfitCategory
import com.deskpet.app.data.model.PetColor
import com.deskpet.app.data.model.PetSpecies
import com.deskpet.app.data.model.PetState
import kotlinx.coroutines.delay

// ============================================================
// DESIGN TOKENS — Sitting pose, strictly measured from reference
// Canvas assumed 300x300 for ratio derivation
// ============================================================

// HEAD (circle) — smaller than body, sits on top
private const val HEAD_CX_RATIO = 0.50f
private const val HEAD_CY_RATIO = 0.35f
private const val HEAD_R_RATIO = 0.153f

// BODY (rounded rect, pear-shaped lower portion)
private const val BODY_CX_RATIO = 0.50f
private const val BODY_CY_RATIO = 0.633f
private const val BODY_W_RATIO = 0.533f
private const val BODY_H_RATIO = 0.453f
private const val BODY_CORNER_RATIO = 0.12f  // corner radius / canvas width

// EYES — large pupils, positioned slightly above head center
private const val EYE_OFFSET_Y = -0.22f      // eye Y offset from headCy / headR
private const val EYE_SPACING_RATIO = 0.40f  // eye X offset from headCx / headR
private const val PUPIL_RATIO = 0.060f       // pupil radius / canvas width
private const val PUPIL_HIGHLIGHT_RATIO = 0.024f
private const val PUPIL_HIGHLIGHT_OFFSET_X = 0.018f
private const val PUPIL_HIGHLIGHT_OFFSET_Y = 0.020f

// BLUSH — oval on cheeks, below eyes
private const val BLUSH_OFFSET_Y = 0.28f
private const val BLUSH_SPACING_RATIO = 0.65f
private const val BLUSH_RADIUS_RATIO = 0.053f
private const val BLUSH_OVAL_W_FACTOR = 1.6f

// NOSE & MOUTH
private const val NOSE_OFFSET_Y = 0.35f
private const val NOSE_RADIUS_RATIO = 0.018f
private const val MOUTH_OFFSET_Y = 0.50f

// FRONT PAWS — at body upper sides, visible in front
private const val PAW_OFFSET_X_RATIO = 0.35f   // from bodyCx / bodyHalfW
private const val PAW_OFFSET_Y_RATIO = -0.30f  // from bodyCy / bodyHalfH
private const val PAW_W_RATIO = 0.075f
private const val PAW_H_RATIO = 0.090f

// BACK FEET — round, at body bottom corners
private const val FOOT_OFFSET_X_RATIO = 0.52f  // from bodyCx / bodyHalfW
private const val FOOT_R_RATIO = 0.067f

// BELLY PATCH
private const val BELLY_PATCH_W_RATIO = 0.28f
private const val BELLY_PATCH_H_RATIO = 0.20f
private const val BELLY_PATCH_OFFSET_Y = 0.00f

// Local drawing colors
private val BlushColor = Color(0x99F4A7B9)
private val EyeColor = Color(0xFF2D2420)
private val HeartColor = Color(0xFFFF6B9D)
private val SnoutColor = Color(0xFFFF8FAB)
private val WhiskerColor = Color(0x66555050)
private val InnerEarColor = Color(0xCCFFB3C8)

// ============================================================
// ANCHOR SYSTEM
// ============================================================
private class PetAnchors(val w: Float, val h: Float) {
    val headCx = w * HEAD_CX_RATIO
    val headCy = h * HEAD_CY_RATIO
    val headR = w * HEAD_R_RATIO

    val bodyCx = w * BODY_CX_RATIO
    val bodyCy = h * BODY_CY_RATIO
    val bodyHalfW = w * BODY_W_RATIO / 2f
    val bodyHalfH = h * BODY_H_RATIO / 2f
    val bodyW = w * BODY_W_RATIO
    val bodyH = h * BODY_H_RATIO
    val bodyLeft = bodyCx - bodyHalfW
    val bodyRight = bodyCx + bodyHalfW
    val bodyTop = bodyCy - bodyHalfH
    val bodyBottom = bodyCy + bodyHalfH
    val bodyCorner = w * BODY_CORNER_RATIO

    val eyeY = headCy + headR * EYE_OFFSET_Y
    val leftEyeX = headCx - headR * EYE_SPACING_RATIO
    val rightEyeX = headCx + headR * EYE_SPACING_RATIO
    val pupilR = w * PUPIL_RATIO

    val blushY = headCy + headR * BLUSH_OFFSET_Y
    val leftBlushX = headCx - headR * BLUSH_SPACING_RATIO
    val rightBlushX = headCx + headR * BLUSH_SPACING_RATIO
    val blushR = w * BLUSH_RADIUS_RATIO

    val noseY = headCy + headR * NOSE_OFFSET_Y
    val noseR = w * NOSE_RADIUS_RATIO
    val mouthY = headCy + headR * MOUTH_OFFSET_Y

    val leftPawX = bodyCx - bodyHalfW * PAW_OFFSET_X_RATIO
    val rightPawX = bodyCx + bodyHalfW * PAW_OFFSET_X_RATIO
    val pawY = bodyCy + bodyHalfH * PAW_OFFSET_Y_RATIO
    val pawW = w * PAW_W_RATIO
    val pawH = h * PAW_H_RATIO

    val leftFootX = bodyCx - bodyHalfW * FOOT_OFFSET_X_RATIO
    val rightFootX = bodyCx + bodyHalfW * FOOT_OFFSET_X_RATIO
    val footY = bodyBottom
    val footR = w * FOOT_R_RATIO

    val bellyCx = bodyCx
    val bellyCy = bodyCy + bodyHalfH * BELLY_PATCH_OFFSET_Y
    val bellyW = w * BELLY_PATCH_W_RATIO
    val bellyH = h * BELLY_PATCH_H_RATIO
}

@Composable
fun PetCanvas(
    modifier: Modifier = Modifier,
    color: PetColor = PetColor.PINK,
    species: PetSpecies = PetSpecies.CAT,
    state: PetState = PetState.IDLE,
    enableBreath: Boolean = true,
    outfits: Map<OutfitCategory, String> = emptyMap()
) {
    val bodyColor = remember(color) { Color(android.graphics.Color.parseColor(color.hex)) }
    val darkerColor = remember(color) {
        val c = android.graphics.Color.parseColor(color.hex)
        val r = (android.graphics.Color.red(c) * 0.82f).toInt().coerceIn(0, 255)
        val g = (android.graphics.Color.green(c) * 0.82f).toInt().coerceIn(0, 255)
        val b = (android.graphics.Color.blue(c) * 0.82f).toInt().coerceIn(0, 255)
        Color(r, g, b)
    }
    val bellyColor = remember(color) {
        val c = android.graphics.Color.parseColor(color.hex)
        val r = (android.graphics.Color.red(c) * 1.12f + 40).toInt().coerceIn(0, 255)
        val g = (android.graphics.Color.green(c) * 1.12f + 40).toInt().coerceIn(0, 255)
        val b = (android.graphics.Color.blue(c) * 1.12f + 40).toInt().coerceIn(0, 255)
        Color(r, g, b)
    }

    val breathTransition = rememberInfiniteTransition(label = "breath")
    val breathScale by breathTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            tween(2000, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "breathScale"
    )

    var blink by remember { mutableStateOf(false) }
    LaunchedEffect(enableBreath) {
        while (enableBreath) {
            delay(3000 + (Math.random() * 1500).toLong())
            blink = true
            delay(150)
            blink = false
        }
    }

    val scale = when {
        !enableBreath -> 1f
        state == PetState.HAPPY || state == PetState.EXCITED -> breathScale * 1.08f
        else -> breathScale
    }

    Canvas(
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        val a = PetAnchors(size.width, size.height)

        // 1. Bottom shadow
        drawOval(
            color = Color.Black.copy(alpha = 0.04f),
            topLeft = Offset(a.bodyCx - a.bodyHalfW * 0.85f, a.bodyBottom - a.h * 0.01f),
            size = Size(a.bodyHalfW * 1.7f, a.h * 0.02f)
        )

        // 2. Back feet (round, at body bottom corners)
        drawFoot(a.leftFootX, a.footY, a.footR, darkerColor)
        drawFoot(a.rightFootX, a.footY, a.footR, darkerColor)

        // 3. Body (rounded rect with radial gradient)
        drawRoundRect(
            brush = Brush.radialGradient(
                colors = listOf(bellyColor, bodyColor),
                center = Offset(a.bodyCx, a.bodyCy - a.bodyHalfH * 0.2f),
                radius = a.bodyHalfH * 1.3f
            ),
            topLeft = Offset(a.bodyLeft, a.bodyTop),
            size = Size(a.bodyW, a.bodyH),
            cornerRadius = CornerRadius(a.bodyCorner, a.bodyCorner)
        )

        // 4. Belly patch
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(bellyColor.copy(alpha = 0.7f), bellyColor.copy(alpha = 0.15f)),
                center = Offset(a.bellyCx, a.bellyCy),
                radius = a.bodyHalfW * 0.45f
            ),
            topLeft = Offset(a.bellyCx - a.bellyW / 2f, a.bellyCy - a.bellyH / 2f),
            size = Size(a.bellyW, a.bellyH)
        )

        // 5. Ears (behind head)
        when (species) {
            PetSpecies.CAT -> drawCatEars(a, bodyColor, darkerColor, bellyColor)
            PetSpecies.DOG -> drawDogEars(a, bodyColor, darkerColor, bellyColor)
            PetSpecies.RABBIT -> drawRabbitEars(a, bodyColor, darkerColor, bellyColor)
            PetSpecies.HAMSTER -> drawHamsterEars(a, bodyColor, darkerColor, bellyColor)
        }

        // 6. Head (circle with radial gradient)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(bellyColor, bodyColor),
                center = Offset(a.headCx, a.headCy - a.headR * 0.15f),
                radius = a.headR * 1.1f
            ),
            radius = a.headR,
            center = Offset(a.headCx, a.headCy)
        )

        // 7. Front paws (visible on body sides, below head)
        drawFrontPaw(a.leftPawX, a.pawY, a.pawW, a.pawH, darkerColor)
        drawFrontPaw(a.rightPawX, a.pawY, a.pawW, a.pawH, darkerColor)

        // 8. Blush (3-layer oval watercolor)
        drawWatercolorBlush(a.leftBlushX, a.blushY, a.blushR, BlushColor)
        drawWatercolorBlush(a.rightBlushX, a.blushY, a.blushR, BlushColor)

        // 9. Eyes
        if (blink || state == PetState.SLEEPY) {
            val bh = a.pupilR * 0.6f
            drawLine(EyeColor, Offset(a.leftEyeX - bh, a.eyeY), Offset(a.leftEyeX + bh, a.eyeY),
                strokeWidth = a.pupilR * 0.5f, cap = StrokeCap.Round)
            drawLine(EyeColor, Offset(a.rightEyeX - bh, a.eyeY), Offset(a.rightEyeX + bh, a.eyeY),
                strokeWidth = a.pupilR * 0.5f, cap = StrokeCap.Round)
        } else {
            drawCircle(EyeColor, radius = a.pupilR, center = Offset(a.leftEyeX, a.eyeY))
            drawCircle(EyeColor, radius = a.pupilR, center = Offset(a.rightEyeX, a.eyeY))
            val hlR = size.width * PUPIL_HIGHLIGHT_RATIO
            drawCircle(Color.White, radius = hlR,
                center = Offset(a.leftEyeX + size.width * PUPIL_HIGHLIGHT_OFFSET_X, a.eyeY - size.height * PUPIL_HIGHLIGHT_OFFSET_Y))
            drawCircle(Color.White, radius = hlR,
                center = Offset(a.rightEyeX + size.width * PUPIL_HIGHLIGHT_OFFSET_X, a.eyeY - size.height * PUPIL_HIGHLIGHT_OFFSET_Y))
            drawCircle(Color.White.copy(alpha = 0.45f), radius = hlR * 0.4f,
                center = Offset(a.leftEyeX - size.width * 0.015f, a.eyeY + size.height * 0.015f))
            drawCircle(Color.White.copy(alpha = 0.45f), radius = hlR * 0.4f,
                center = Offset(a.rightEyeX - size.width * 0.015f, a.eyeY + size.height * 0.015f))
        }

        // 10. Snout
        when (species) {
            PetSpecies.CAT -> drawCatSnout(a, size.width)
            PetSpecies.DOG -> drawDogSnout(a, size.width)
            PetSpecies.RABBIT -> drawRabbitSnout(a, size.width)
            PetSpecies.HAMSTER -> drawHamsterSnout(a, size.width)
        }

        // Outfits
        if (outfits.isNotEmpty()) {
            outfits.forEach { (category, outfitId) ->
                val rendered = with(OutfitRenderer) {
                    this@Canvas.render(outfitId, category, species, size.width, size.height)
                }
                if (!rendered) {
                    drawIntoCanvas { canvas ->
                        val paint = android.graphics.Paint().apply {
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                        }
                        val (cx, cy, sizeFactor) = when (category) {
                            OutfitCategory.HEAD -> when (species) {
                                PetSpecies.RABBIT -> Triple(size.width * 0.5f, size.height * 0.18f, 0.15f)
                                PetSpecies.HAMSTER -> Triple(size.width * 0.5f, size.height * 0.02f, 0.16f)
                                else -> Triple(size.width * 0.5f, size.height * 0.05f, 0.18f)
                            }
                            OutfitCategory.GLASSES -> Triple(a.headCx, a.eyeY, 0.14f)
                            OutfitCategory.COLLAR -> Triple(a.headCx, a.bodyTop + a.bodyHalfH * 0.15f, 0.12f)
                            OutfitCategory.CLOTHING -> Triple(a.bodyCx, a.bodyCy + a.bodyHalfH * 0.2f, 0.20f)
                            OutfitCategory.TAIL -> Triple(a.bodyRight + size.width * 0.04f, a.bodyBottom - size.height * 0.06f, 0.13f)
                            OutfitCategory.ACCESSORY -> Triple(a.bodyLeft - size.width * 0.04f, a.eyeY, 0.12f)
                        }
                        paint.textSize = size.width * sizeFactor
                        val fm = paint.fontMetrics
                        val baseline = cy - (fm.ascent + fm.descent) / 2f
                        canvas.nativeCanvas.drawText(outfitId, cx, baseline, paint)
                    }
                }
            }
        }
    }
}

// ============================================================
// EARS
// ============================================================
private fun DrawScope.drawCatEars(a: PetAnchors, body: Color, dark: Color, belly: Color) {
    val halfW = a.headR * 0.28f
    val innerHalfW = a.headR * 0.14f
    val baseY = a.headCy - a.headR * 0.70f
    val tipY = a.headCy - a.headR * 1.35f
    val innerTipY = a.headCy - a.headR * 1.05f

    val lbcx = a.headCx - a.headR * 0.55f
    drawPath(Path().apply {
        moveTo(lbcx - halfW, baseY); lineTo(lbcx, tipY); lineTo(lbcx + halfW, baseY); close()
    }, brush = Brush.radialGradient(listOf(belly, body), center = Offset(lbcx, tipY), radius = a.headR * 0.7f))
    drawPath(Path().apply {
        moveTo(lbcx - innerHalfW * 0.6f, baseY - a.headR * 0.08f)
        lineTo(lbcx, innerTipY)
        lineTo(lbcx + innerHalfW * 0.6f, baseY - a.headR * 0.08f); close()
    }, InnerEarColor)

    val rbcx = a.headCx + a.headR * 0.55f
    drawPath(Path().apply {
        moveTo(rbcx - halfW, baseY); lineTo(rbcx, tipY); lineTo(rbcx + halfW, baseY); close()
    }, brush = Brush.radialGradient(listOf(belly, body), center = Offset(rbcx, tipY), radius = a.headR * 0.7f))
    drawPath(Path().apply {
        moveTo(rbcx - innerHalfW * 0.6f, baseY - a.headR * 0.08f)
        lineTo(rbcx, innerTipY)
        lineTo(rbcx + innerHalfW * 0.6f, baseY - a.headR * 0.08f); close()
    }, InnerEarColor)
}

private fun DrawScope.drawDogEars(a: PetAnchors, body: Color, dark: Color, belly: Color) {
    val baseY = a.headCy - a.headR * 0.60f
    val tipY = a.headCy - a.headR * 1.10f

    val liX = a.headCx - a.headR * 0.70f
    val loX = a.headCx - a.headR * 1.10f
    val ldX = a.headCx - a.headR * 1.30f
    drawPath(Path().apply {
        moveTo(liX, baseY); lineTo(loX, tipY); lineTo(ldX, baseY + a.headR * 0.28f); close()
    }, brush = Brush.radialGradient(listOf(body, dark), center = Offset(loX, tipY), radius = a.headR * 0.8f))
    drawPath(Path().apply {
        moveTo(liX + a.headR * 0.04f, baseY - a.headR * 0.04f)
        lineTo(loX + a.headR * 0.04f, tipY + a.headR * 0.04f)
        lineTo(ldX + a.headR * 0.04f, baseY + a.headR * 0.24f); close()
    }, InnerEarColor)

    val riX = a.headCx + a.headR * 0.70f
    val roX = a.headCx + a.headR * 1.10f
    val rdX = a.headCx + a.headR * 1.30f
    drawPath(Path().apply {
        moveTo(riX, baseY); lineTo(roX, tipY); lineTo(rdX, baseY + a.headR * 0.28f); close()
    }, brush = Brush.radialGradient(listOf(body, dark), center = Offset(roX, tipY), radius = a.headR * 0.8f))
    drawPath(Path().apply {
        moveTo(riX - a.headR * 0.04f, baseY - a.headR * 0.04f)
        lineTo(roX - a.headR * 0.04f, tipY + a.headR * 0.04f)
        lineTo(rdX - a.headR * 0.04f, baseY + a.headR * 0.24f); close()
    }, InnerEarColor)
}

private fun DrawScope.drawRabbitEars(a: PetAnchors, body: Color, dark: Color, belly: Color) {
    val earH = a.headR * 1.35f
    val earW = a.headR * 0.36f
    val innerEarW = earW * 0.55f
    val topY = a.headCy - a.headR * 1.20f
    val bottomY = a.headCy - a.headR * 0.05f

    val lcx = a.headCx - a.headR * 0.42f
    drawOval(
        brush = Brush.radialGradient(listOf(belly, body), center = Offset(lcx, topY + earH * 0.35f), radius = earH),
        topLeft = Offset(lcx - earW / 2f, topY),
        size = Size(earW, bottomY - topY)
    )
    drawOval(InnerEarColor, Offset(lcx - innerEarW / 2f, topY + earH * 0.18f), Size(innerEarW, earH * 0.78f))

    val rcx = a.headCx + a.headR * 0.42f
    drawOval(
        brush = Brush.radialGradient(listOf(belly, body), center = Offset(rcx, topY + earH * 0.35f), radius = earH),
        topLeft = Offset(rcx - earW / 2f, topY),
        size = Size(earW, bottomY - topY)
    )
    drawOval(InnerEarColor, Offset(rcx - innerEarW / 2f, topY + earH * 0.18f), Size(innerEarW, earH * 0.78f))
}

private fun DrawScope.drawHamsterEars(a: PetAnchors, body: Color, dark: Color, belly: Color) {
    val earY = a.headCy - a.headR * 0.82f
    val earR = a.headR * 0.26f
    val innerR = earR * 0.48f
    val lx = a.headCx - a.headR * 0.62f
    val rx = a.headCx + a.headR * 0.62f

    drawCircle(
        brush = Brush.radialGradient(listOf(belly, body), center = Offset(lx, earY - earR * 0.3f), radius = earR * 1.1f),
        radius = earR, center = Offset(lx, earY)
    )
    drawCircle(
        brush = Brush.radialGradient(listOf(belly, body), center = Offset(rx, earY - earR * 0.3f), radius = earR * 1.1f),
        radius = earR, center = Offset(rx, earY)
    )
    drawCircle(InnerEarColor, radius = innerR, center = Offset(lx, earY))
    drawCircle(InnerEarColor, radius = innerR, center = Offset(rx, earY))
}

// ============================================================
// SNOUT
// ============================================================
private fun DrawScope.drawCatSnout(a: PetAnchors, w: Float) {
    val noseTopY = a.noseY - a.noseR
    val noseBottomY = a.noseY + a.noseR
    drawPath(Path().apply {
        moveTo(a.headCx, noseTopY)
        lineTo(a.headCx - a.noseR * 1.1f, noseBottomY)
        lineTo(a.headCx + a.noseR * 1.1f, noseBottomY); close()
    }, SnoutColor)
    drawLine(WhiskerColor, Offset(a.headCx - a.headR * 0.5f, a.noseY + a.noseR), Offset(a.headCx - a.headR * 1.0f, a.noseY + a.noseR * 0.5f), 2f)
    drawLine(WhiskerColor, Offset(a.headCx - a.headR * 0.5f, a.noseY + a.noseR * 2f), Offset(a.headCx - a.headR * 1.0f, a.noseY + a.noseR * 1.5f), 2f)
    drawLine(WhiskerColor, Offset(a.headCx + a.headR * 0.5f, a.noseY + a.noseR), Offset(a.headCx + a.headR * 1.0f, a.noseY + a.noseR * 0.5f), 2f)
    drawLine(WhiskerColor, Offset(a.headCx + a.headR * 0.5f, a.noseY + a.noseR * 2f), Offset(a.headCx + a.headR * 1.0f, a.noseY + a.noseR * 1.5f), 2f)
    drawLine(Color.White, Offset(a.headCx - a.noseR * 1.2f, a.mouthY), Offset(a.headCx + a.noseR * 1.2f, a.mouthY),
        strokeWidth = 2.5f, cap = StrokeCap.Round)
}

private fun DrawScope.drawDogSnout(a: PetAnchors, w: Float) {
    val snoutR = w * 0.075f
    drawCircle(SnoutColor.copy(alpha = 0.55f), radius = snoutR, center = Offset(a.headCx, a.noseY + a.noseR * 0.5f))
    drawCircle(EyeColor, radius = a.noseR * 1.2f, center = Offset(a.headCx, a.noseY))
    drawLine(Color.White, Offset(a.headCx - a.noseR * 1.8f, a.mouthY + a.noseR), Offset(a.headCx + a.noseR * 1.8f, a.mouthY + a.noseR),
        strokeWidth = 2.5f, cap = StrokeCap.Round)
}

private fun DrawScope.drawRabbitSnout(a: PetAnchors, w: Float) {
    drawCircle(SnoutColor, radius = a.noseR * 0.9f, center = Offset(a.headCx, a.noseY))
    drawLine(WhiskerColor, Offset(a.headCx, a.noseY + a.noseR * 0.9f), Offset(a.headCx, a.mouthY), 2f)
    drawLine(Color.White, Offset(a.headCx - a.noseR * 1.2f, a.mouthY), Offset(a.headCx + a.noseR * 1.2f, a.mouthY),
        strokeWidth = 2.5f, cap = StrokeCap.Round)
}

private fun DrawScope.drawHamsterSnout(a: PetAnchors, w: Float) {
    drawCircle(SnoutColor, radius = a.noseR, center = Offset(a.headCx, a.noseY))
    drawLine(Color.White, Offset(a.headCx - a.noseR * 1.0f, a.mouthY), Offset(a.headCx + a.noseR * 1.0f, a.mouthY),
        strokeWidth = 2.5f, cap = StrokeCap.Round)
}

// ============================================================
// PAWS & FEET
// ============================================================
private fun DrawScope.drawFrontPaw(cx: Float, cy: Float, w: Float, h: Float, color: Color) {
    drawOval(
        brush = Brush.radialGradient(
            listOf(color.copy(alpha = 0.9f), color),
            center = Offset(cx, cy - h * 0.25f),
            radius = h * 0.7f
        ),
        topLeft = Offset(cx - w / 2f, cy - h / 2f),
        size = Size(w, h)
    )
}

private fun DrawScope.drawFoot(cx: Float, cy: Float, r: Float, color: Color) {
    drawCircle(
        brush = Brush.radialGradient(
            listOf(color.copy(alpha = 0.85f), color),
            center = Offset(cx, cy - r * 0.3f),
            radius = r * 1.1f
        ),
        radius = r, center = Offset(cx, cy)
    )
}

// ============================================================
// HEART PARTICLES
// ============================================================
@Composable
fun HeartParticles(modifier: Modifier = Modifier, active: Boolean = true) {
    if (!active) return
    val transition = rememberInfiniteTransition(label = "hearts")
    val progress by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing)),
        label = "heartProgress"
    )
    val starts = listOf(0.2f, 0.45f, 0.7f, 0.9f, 0.35f)
    Canvas(modifier = modifier) {
        starts.forEachIndexed { i, sx ->
            val off = (i * 0.2f) % 1f
            val p = (progress + off) % 1f
            val y = size.height * (1f - p)
            val x = size.width * sx + (size.width * 0.04f * i * if (i % 2 == 0) 1 else -1)
            val a = (1f - p).coerceIn(0f, 1f)
            val r = size.minDimension * 0.05f * (1f - p * 0.4f)
            drawHeart(x, y, r, HeartColor.copy(alpha = a * 0.9f))
        }
    }
}

private fun DrawScope.drawHeart(cx: Float, cy: Float, r: Float, color: Color) {
    val path = Path().apply {
        moveTo(cx, cy + r * 0.25f)
        cubicTo(cx + r, cy - r * 0.25f, cx + r * 0.6f, cy - r * 0.9f, cx, cy - r * 0.3f)
        cubicTo(cx - r * 0.6f, cy - r * 0.9f, cx - r, cy - r * 0.25f, cx, cy + r * 0.25f)
        close()
    }
    drawPath(path, color)
}

// ============================================================
// WATERCOLOR BLUSH
// ============================================================
private fun DrawScope.drawWatercolorBlush(centerX: Float, centerY: Float, baseRadius: Float, baseColor: Color) {
    val w = baseRadius * BLUSH_OVAL_W_FACTOR
    drawOval(color = baseColor.copy(alpha = 0.22f),
        topLeft = Offset(centerX - w * 1.4f, centerY - baseRadius * 1.2f),
        size = Size(w * 2.8f, baseRadius * 2.4f))
    drawOval(color = baseColor.copy(alpha = 0.35f),
        topLeft = Offset(centerX - w, centerY - baseRadius * 0.85f),
        size = Size(w * 2f, baseRadius * 1.7f))
    drawOval(color = baseColor.copy(alpha = 0.50f),
        topLeft = Offset(centerX - w * 0.6f, centerY - baseRadius * 0.5f),
        size = Size(w * 1.2f, baseRadius * 1.0f))
}
