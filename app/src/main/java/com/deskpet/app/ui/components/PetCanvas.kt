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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
// DESIGN TOKENS (from 2560x1440 design sheet, ratio-driven)
// ============================================================
// Canvas-level anchor ratios (all relative to canvas w/h, no magic px)
private const val BODY_CX_RATIO = 0.50f       // body center X / canvas width
private const val BODY_CY_RATIO = 0.52f       // body center Y / canvas height
private const val BODY_W_RATIO = 0.42f        // body ellipse width / canvas width
private const val BODY_H_RATIO = 0.58f        // body ellipse height / canvas height

private const val EYE_Y_RATIO = 0.38f         // eye center Y / canvas height
private const val EYE_SPACING_RATIO = 0.12f   // single eye offset from center X / canvas width
private const val PUPIL_RATIO = 0.045f        // pupil radius / canvas width
private const val PUPIL_HIGHLIGHT_RATIO = 0.015f // highlight radius / canvas width
private const val PUPIL_HIGHLIGHT_OFFSET_X = 0.018f // highlight X offset from pupil center / canvas width
private const val PUPIL_HIGHLIGHT_OFFSET_Y = 0.020f // highlight Y offset from pupil center / canvas width

private const val BLUSH_Y_RATIO = 0.46f       // blush center Y / canvas height
private const val BLUSH_SPACING_RATIO = 0.14f  // blush offset from center X / canvas width
private const val BLUSH_RADIUS_RATIO = 0.045f  // blush base radius / canvas width

private const val NOSE_Y_RATIO = 0.49f        // nose Y / canvas height
private const val NOSE_RADIUS_RATIO = 0.020f  // nose radius / canvas width

private const val MOUTH_Y_RATIO = 0.52f       // mouth Y / canvas height

private const val PAW_Y_RATIO = 0.82f         // paw top Y / canvas height
private const val PAW_W_RATIO = 0.055f        // paw width / canvas width
private const val PAW_H_RATIO = 0.070f        // paw height / canvas height
private const val PAW_INSET_RATIO = 0.025f     // paw inset from body edge / canvas width

private const val BELLY_PATCH_W_RATIO = 0.22f // belly patch width / canvas width
private const val BELLY_PATCH_H_RATIO = 0.28f // belly patch height / canvas height
private const val BELLY_PATCH_CY_RATIO = 0.58f // belly patch center Y / canvas height

// Cat ear anchors
private const val CAT_EAR_BASE_Y_RATIO = 0.20f
private const val CAT_EAR_TIP_Y_RATIO = 0.02f
private const val CAT_EAR_HALF_W = 0.06f
private const val CAT_INNER_EAR_HALF_W = 0.03f
private const val CAT_INNER_EAR_TIP_Y_RATIO = 0.08f

// Dog ear anchors
private const val DOG_EAR_BASE_Y_RATIO = 0.20f
private const val DOG_EAR_TIP_Y_RATIO = 0.10f
private const val DOG_EAR_OUTER_X_RATIO = 0.18f   // left ear outer X
private const val DOG_EAR_INNER_X_RATIO = 0.30f   // left ear inner X
private const val DOG_EAR_DROOP_X_RATIO = 0.14f   // left ear droop X

// Rabbit ear anchors
private const val RABBIT_EAR_TOP_Y_RATIO = 0.00f
private const val RABBIT_EAR_BOTTOM_Y_RATIO = 0.28f
private const val RABBIT_EAR_W_RATIO = 0.08f
private const val RABBIT_EAR_LEFT_X_RATIO = 0.33f
private const val RABBIT_EAR_RIGHT_X_RATIO = 0.59f
private const val RABBIT_INNER_EAR_W_RATIO = 0.04f

// Hamster ear anchors
private const val HAMSTER_EAR_Y_RATIO = 0.18f
private const val HAMSTER_EAR_X_RATIO = 0.32f
private const val HAMSTER_EAR_R_RATIO = 0.060f
private const val HAMSTER_INNER_EAR_R_RATIO = 0.030f

// Local drawing colors
private val BlushColor = Color(0x99F4A7B9)
private val EyeColor = Color(0xFF2D2420)
private val HeartColor = Color(0xFFFF6B9D)
private val SnoutColor = Color(0xFFFF8FAB)
private val WhiskerColor = Color(0x66555050)
private val InnerEarColor = Color(0xCCFFB3C8)

// ============================================================
// ANCHOR SYSTEM — derived from design ratios
// ============================================================
/** All anchors are computed once per canvas size, then passed to species drawers. */
private class PetAnchors(val w: Float, val h: Float) {
    // Body (one unified oval — head+body together, matching watercolor design)
    val bodyCx = w * BODY_CX_RATIO
    val bodyCy = h * BODY_CY_RATIO
    val bodyHalfW = w * BODY_W_RATIO / 2f
    val bodyHalfH = h * BODY_H_RATIO / 2f
    val bodyLeft = bodyCx - bodyHalfW
    val bodyRight = bodyCx + bodyHalfW
    val bodyTop = bodyCy - bodyHalfH
    val bodyBottom = bodyCy + bodyHalfH

    // Eyes — big round pupils, NO white sclera (design sheet feature)
    val eyeY = h * EYE_Y_RATIO
    val leftEyeX = bodyCx - w * EYE_SPACING_RATIO
    val rightEyeX = bodyCx + w * EYE_SPACING_RATIO
    val pupilR = w * PUPIL_RATIO

    // Blush — below eyes, on cheeks
    val blushY = h * BLUSH_Y_RATIO
    val leftBlushX = bodyCx - w * BLUSH_SPACING_RATIO
    val rightBlushX = bodyCx + w * BLUSH_SPACING_RATIO
    val blushR = w * BLUSH_RADIUS_RATIO

    // Nose & mouth
    val noseY = h * NOSE_Y_RATIO
    val noseR = w * NOSE_RADIUS_RATIO
    val mouthY = h * MOUTH_Y_RATIO

    // Paws — visible at body bottom edge
    val pawY = h * PAW_Y_RATIO
    val pawW = w * PAW_W_RATIO
    val pawH = h * PAW_H_RATIO
    val leftPawX = bodyLeft + w * PAW_INSET_RATIO
    val rightPawX = bodyRight - w * PAW_INSET_RATIO - pawW

    // Belly highlight patch
    val bellyCx = bodyCx
    val bellyCy = h * BELLY_PATCH_CY_RATIO
    val bellyW = w * BELLY_PATCH_W_RATIO
    val bellyH = h * BELLY_PATCH_H_RATIO
}

/**
 * Reusable pet character drawn entirely with Compose Canvas (no image assets).
 * Watercolor storybook style — one unified oval body, large eyes, blush cheeks.
 */
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
        val anchors = PetAnchors(size.width, size.height)

        // ---- Ears (behind body) ----
        when (species) {
            PetSpecies.CAT -> drawCatEars(anchors, bodyColor, darkerColor)
            PetSpecies.DOG -> drawDogEars(anchors, bodyColor, darkerColor)
            PetSpecies.RABBIT -> drawRabbitEars(anchors, bodyColor, darkerColor)
            PetSpecies.HAMSTER -> drawHamsterEars(anchors, bodyColor, darkerColor)
        }

        // ---- Body (one unified oval — design sheet principle) ----
        drawOval(
            color = bodyColor,
            topLeft = Offset(anchors.bodyLeft, anchors.bodyTop),
            size = Size(anchors.bodyHalfW * 2f, anchors.bodyHalfH * 2f)
        )

        // Belly highlight patch (lighter watercolor wash)
        drawOval(
            color = bellyColor.copy(alpha = 0.55f),
            topLeft = Offset(anchors.bellyCx - anchors.bellyW / 2f, anchors.bellyCy - anchors.bellyH / 2f),
            size = Size(anchors.bellyW, anchors.bellyH)
        )

        // ---- Blush marks (watercolor style, on cheeks) ----
        drawWatercolorBlush(anchors.leftBlushX, anchors.blushY, anchors.blushR, BlushColor)
        drawWatercolorBlush(anchors.rightBlushX, anchors.blushY, anchors.blushR, BlushColor)

        // ---- Eyes (big pupils, no sclera — watercolor design) ----
        if (blink || state == PetState.SLEEPY) {
            val blinkHalf = anchors.pupilR * 0.55f
            drawLine(
                EyeColor,
                Offset(anchors.leftEyeX - blinkHalf, anchors.eyeY),
                Offset(anchors.leftEyeX + blinkHalf, anchors.eyeY),
                strokeWidth = 4f, cap = StrokeCap.Round
            )
            drawLine(
                EyeColor,
                Offset(anchors.rightEyeX - blinkHalf, anchors.eyeY),
                Offset(anchors.rightEyeX + blinkHalf, anchors.eyeY),
                strokeWidth = 4f, cap = StrokeCap.Round
            )
        } else {
            // Full pupil (watercolor style — no white ring)
            drawCircle(EyeColor, radius = anchors.pupilR, center = Offset(anchors.leftEyeX, anchors.eyeY))
            drawCircle(EyeColor, radius = anchors.pupilR, center = Offset(anchors.rightEyeX, anchors.eyeY))
            // Catchlight highlight (top-right of pupil)
            val hlR = size.width * PUPIL_HIGHLIGHT_RATIO
            drawCircle(
                Color.White, radius = hlR,
                center = Offset(anchors.leftEyeX + size.width * PUPIL_HIGHLIGHT_OFFSET_X, anchors.eyeY - size.height * PUPIL_HIGHLIGHT_OFFSET_Y)
            )
            drawCircle(
                Color.White, radius = hlR,
                center = Offset(anchors.rightEyeX + size.width * PUPIL_HIGHLIGHT_OFFSET_X, anchors.eyeY - size.height * PUPIL_HIGHLIGHT_OFFSET_Y)
            )
        }

        // ---- Snout / nose / mouth (species-specific) ----
        when (species) {
            PetSpecies.CAT -> drawCatSnout(anchors, size.width)
            PetSpecies.DOG -> drawDogSnout(anchors, size.width)
            PetSpecies.RABBIT -> drawRabbitSnout(anchors, size.width)
            PetSpecies.HAMSTER -> drawHamsterSnout(anchors, size.width)
        }

        // ---- Paws (visible at bottom of body) ----
        drawPaws(anchors, darkerColor)

        // ---- Outfit overlays ----
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
                            OutfitCategory.GLASSES -> Triple(anchors.bodyCx, anchors.eyeY, 0.14f)
                            OutfitCategory.COLLAR -> Triple(anchors.bodyCx, anchors.bodyTop + anchors.bodyHalfH * 0.15f, 0.12f)
                            OutfitCategory.CLOTHING -> Triple(anchors.bodyCx, anchors.bodyCy + anchors.bodyHalfH * 0.2f, 0.20f)
                            OutfitCategory.TAIL -> Triple(anchors.bodyRight + size.width * 0.04f, anchors.bodyBottom - size.height * 0.06f, 0.13f)
                            OutfitCategory.ACCESSORY -> Triple(anchors.bodyLeft - size.width * 0.04f, anchors.eyeY, 0.12f)
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
// SPECIES: EARS (drawn first, behind body oval)
// ============================================================

private fun DrawScope.drawCatEars(a: PetAnchors, body: Color, dark: Color) {
    // Pointy triangular ears, symmetric on either side of head top
    val halfW = a.w * CAT_EAR_HALF_W
    val innerHalfW = a.w * CAT_INNER_EAR_HALF_W
    val baseY = a.h * CAT_EAR_BASE_Y_RATIO
    val tipY = a.h * CAT_EAR_TIP_Y_RATIO
    val innerTipY = a.h * CAT_INNER_EAR_TIP_Y_RATIO

    // Left ear
    val leftBaseCenterX = a.bodyCx - a.bodyHalfW * 0.45f
    drawPath(Path().apply {
        moveTo(leftBaseCenterX - halfW, baseY)
        lineTo(leftBaseCenterX, tipY)
        lineTo(leftBaseCenterX + halfW, baseY)
        close()
    }, body)
    // Left inner ear
    drawPath(Path().apply {
        moveTo(leftBaseCenterX - innerHalfW * 0.6f, baseY - a.h * 0.02f)
        lineTo(leftBaseCenterX, innerTipY)
        lineTo(leftBaseCenterX + innerHalfW * 0.6f, baseY - a.h * 0.02f)
        close()
    }, InnerEarColor)

    // Right ear (mirror)
    val rightBaseCenterX = a.bodyCx + a.bodyHalfW * 0.45f
    drawPath(Path().apply {
        moveTo(rightBaseCenterX - halfW, baseY)
        lineTo(rightBaseCenterX, tipY)
        lineTo(rightBaseCenterX + halfW, baseY)
        close()
    }, body)
    drawPath(Path().apply {
        moveTo(rightBaseCenterX - innerHalfW * 0.6f, baseY - a.h * 0.02f)
        lineTo(rightBaseCenterX, innerTipY)
        lineTo(rightBaseCenterX + innerHalfW * 0.6f, baseY - a.h * 0.02f)
        close()
    }, InnerEarColor)
}

private fun DrawScope.drawDogEars(a: PetAnchors, body: Color, dark: Color) {
    // Short floppy ears (corgi/shiba style) — triangular with slight droop
    val baseY = a.h * DOG_EAR_BASE_Y_RATIO
    val tipY = a.h * DOG_EAR_TIP_Y_RATIO

    // Left ear
    val leftInnerX = a.w * DOG_EAR_INNER_X_RATIO
    val leftOuterX = a.w * DOG_EAR_OUTER_X_RATIO
    val leftDroopX = a.w * DOG_EAR_DROOP_X_RATIO
    drawPath(Path().apply {
        moveTo(leftInnerX, baseY)   // base near head
        lineTo(leftOuterX, tipY)     // tip outer
        lineTo(leftDroopX, baseY + a.h * 0.06f) // droop bottom
        close()
    }, dark)
    // Left inner
    drawPath(Path().apply {
        moveTo(leftInnerX + a.w * 0.01f, baseY - a.h * 0.01f)
        lineTo(leftOuterX + a.w * 0.01f, tipY + a.h * 0.01f)
        lineTo(leftDroopX + a.w * 0.01f, baseY + a.h * 0.05f)
        close()
    }, InnerEarColor)

    // Right ear (mirror)
    val rightInnerX = a.w - a.w * DOG_EAR_INNER_X_RATIO
    val rightOuterX = a.w - a.w * DOG_EAR_OUTER_X_RATIO
    val rightDroopX = a.w - a.w * DOG_EAR_DROOP_X_RATIO
    drawPath(Path().apply {
        moveTo(rightInnerX, baseY)
        lineTo(rightOuterX, tipY)
        lineTo(rightDroopX, baseY + a.h * 0.06f)
        close()
    }, dark)
    drawPath(Path().apply {
        moveTo(rightInnerX - a.w * 0.01f, baseY - a.h * 0.01f)
        lineTo(rightOuterX - a.w * 0.01f, tipY + a.h * 0.01f)
        lineTo(rightDroopX - a.w * 0.01f, baseY + a.h * 0.05f)
        close()
    }, InnerEarColor)
}

private fun DrawScope.drawRabbitEars(a: PetAnchors, body: Color, dark: Color) {
    // Long upright oval ears (signature rabbit look)
    val topY = a.h * RABBIT_EAR_TOP_Y_RATIO
    val bottomY = a.h * RABBIT_EAR_BOTTOM_Y_RATIO
    val earW = a.w * RABBIT_EAR_W_RATIO
    val innerEarW = a.w * RABBIT_INNER_EAR_W_RATIO

    // Left ear
    val leftCx = a.w * RABBIT_EAR_LEFT_X_RATIO
    drawOval(body, Offset(leftCx - earW / 2f, topY), Size(earW, bottomY - topY))
    drawOval(InnerEarColor, Offset(leftCx - innerEarW / 2f, topY + a.h * 0.04f), Size(innerEarW, (bottomY - topY) * 0.75f))

    // Right ear
    val rightCx = a.w * RABBIT_EAR_RIGHT_X_RATIO
    drawOval(body, Offset(rightCx - earW / 2f, topY), Size(earW, bottomY - topY))
    drawOval(InnerEarColor, Offset(rightCx - innerEarW / 2f, topY + a.h * 0.04f), Size(innerEarW, (bottomY - topY) * 0.75f))
}

private fun DrawScope.drawHamsterEars(a: PetAnchors, body: Color, dark: Color) {
    // Small round ears on top of head
    val earY = a.h * HAMSTER_EAR_Y_RATIO
    val earR = a.w * HAMSTER_EAR_R_RATIO
    val innerR = a.w * HAMSTER_INNER_EAR_R_RATIO
    val leftX = a.bodyCx - a.bodyHalfW * 0.65f
    val rightX = a.bodyCx + a.bodyHalfW * 0.65f

    drawCircle(body, radius = earR, center = Offset(leftX, earY))
    drawCircle(body, radius = earR, center = Offset(rightX, earY))
    drawCircle(InnerEarColor, radius = innerR, center = Offset(leftX, earY))
    drawCircle(InnerEarColor, radius = innerR, center = Offset(rightX, earY))
}

// ============================================================
// SPECIES: SNOUT / NOSE / MOUTH
// ============================================================

private fun DrawScope.drawCatSnout(a: PetAnchors, w: Float) {
    // Tiny pink triangle nose
    val noseTopY = a.noseY - a.noseR
    val noseBottomY = a.noseY + a.noseR
    drawPath(Path().apply {
        moveTo(a.bodyCx, noseTopY)
        lineTo(a.bodyCx - a.noseR * 1.1f, noseBottomY)
        lineTo(a.bodyCx + a.noseR * 1.1f, noseBottomY)
        close()
    }, SnoutColor)
    // Whiskers
    drawLine(WhiskerColor, Offset(a.bodyLeft + w * 0.02f, a.noseY + a.noseR), Offset(a.bodyCx - w * 0.08f, a.noseY + a.noseR * 0.5f), 2f)
    drawLine(WhiskerColor, Offset(a.bodyLeft + w * 0.02f, a.noseY + a.noseR * 2f), Offset(a.bodyCx - w * 0.08f, a.noseY + a.noseR * 1.5f), 2f)
    drawLine(WhiskerColor, Offset(a.bodyRight - w * 0.02f, a.noseY + a.noseR), Offset(a.bodyCx + w * 0.08f, a.noseY + a.noseR * 0.5f), 2f)
    drawLine(WhiskerColor, Offset(a.bodyRight - w * 0.02f, a.noseY + a.noseR * 2f), Offset(a.bodyCx + w * 0.08f, a.noseY + a.noseR * 1.5f), 2f)
    // Mouth — small "w" or line
    drawLine(
        Color.White,
        Offset(a.bodyCx - a.noseR * 1.2f, a.mouthY),
        Offset(a.bodyCx + a.noseR * 1.2f, a.mouthY),
        strokeWidth = 2.5f, cap = StrokeCap.Round
    )
}

private fun DrawScope.drawDogSnout(a: PetAnchors, w: Float) {
    // Large round snout patch
    val snoutR = w * 0.075f
    drawCircle(SnoutColor.copy(alpha = 0.55f), radius = snoutR, center = Offset(a.bodyCx, a.noseY + a.noseR * 0.5f))
    // Dark nose dot
    drawCircle(EyeColor, radius = a.noseR * 1.2f, center = Offset(a.bodyCx, a.noseY))
    // Mouth — small curved line below snout
    drawLine(
        Color.White,
        Offset(a.bodyCx - a.noseR * 1.8f, a.mouthY + a.noseR),
        Offset(a.bodyCx + a.noseR * 1.8f, a.mouthY + a.noseR),
        strokeWidth = 2.5f, cap = StrokeCap.Round
    )
}

private fun DrawScope.drawRabbitSnout(a: PetAnchors, w: Float) {
    // Small pink nose
    drawCircle(SnoutColor, radius = a.noseR * 0.9f, center = Offset(a.bodyCx, a.noseY))
    // Mouth — small vertical + horizontal line
    drawLine(WhiskerColor, Offset(a.bodyCx, a.noseY + a.noseR * 0.9f), Offset(a.bodyCx, a.mouthY), 2f)
    drawLine(
        Color.White,
        Offset(a.bodyCx - a.noseR * 1.2f, a.mouthY),
        Offset(a.bodyCx + a.noseR * 1.2f, a.mouthY),
        strokeWidth = 2.5f, cap = StrokeCap.Round
    )
}

private fun DrawScope.drawHamsterSnout(a: PetAnchors, w: Float) {
    // Tiny round nose
    drawCircle(SnoutColor, radius = a.noseR, center = Offset(a.bodyCx, a.noseY))
    // Small mouth
    drawLine(
        Color.White,
        Offset(a.bodyCx - a.noseR * 1.0f, a.mouthY),
        Offset(a.bodyCx + a.noseR * 1.0f, a.mouthY),
        strokeWidth = 2.5f, cap = StrokeCap.Round
    )
}

// ============================================================
// PAWS
// ============================================================
private fun DrawScope.drawPaws(a: PetAnchors, pawColor: Color) {
    val pawColorLight = pawColor.copy(alpha = 0.75f)
    // Left paw
    drawOval(
        pawColorLight,
        topLeft = Offset(a.leftPawX, a.pawY),
        size = Size(a.pawW, a.pawH)
    )
    // Right paw
    drawOval(
        pawColorLight,
        topLeft = Offset(a.rightPawX, a.pawY),
        size = Size(a.pawW, a.pawH)
    )
}

// ============================================================
// HEART PARTICLES (overlay for happy state)
// ============================================================
@Composable
fun HeartParticles(
    modifier: Modifier = Modifier,
    active: Boolean = true
) {
    if (!active) return

    val transition = rememberInfiniteTransition(label = "hearts")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing)),
        label = "heartProgress"
    )

    val startFractions = listOf(0.2f, 0.45f, 0.7f, 0.9f, 0.35f)
    Canvas(modifier = modifier) {
        startFractions.forEachIndexed { index, startX ->
            val offset = (index * 0.2f) % 1f
            val p = (progress + offset) % 1f
            val y = size.height * (1f - p)
            val x = size.width * startX + (size.width * 0.04f * index * if (index % 2 == 0) 1 else -1)
            val alpha = (1f - p).coerceIn(0f, 1f)
            val r = size.minDimension * 0.05f * (1f - p * 0.4f)
            drawHeart(x, y, r, HeartColor.copy(alpha = alpha * 0.9f))
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
// WATERCOLOR STYLE UTILITIES
// ============================================================

/**
 * Draw watercolor-style blush: 3 overlapping transparent ellipses for soft wash effect.
 */
private fun DrawScope.drawWatercolorBlush(
    centerX: Float,
    centerY: Float,
    baseRadius: Float,
    baseColor: Color
) {
    // Layer 1: large, faint wash
    drawCircle(
        baseColor.copy(alpha = 0.30f),
        radius = baseRadius * 1.4f,
        center = Offset(centerX - baseRadius * 0.1f, centerY - baseRadius * 0.1f)
    )
    // Layer 2: medium opacity
    drawCircle(
        baseColor.copy(alpha = 0.45f),
        radius = baseRadius,
        center = Offset(centerX + baseRadius * 0.15f, centerY + baseRadius * 0.05f)
    )
    // Layer 3: core, most opaque
    drawCircle(
        baseColor.copy(alpha = 0.60f),
        radius = baseRadius * 0.55f,
        center = Offset(centerX - baseRadius * 0.05f, centerY)
    )
}
