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
// DESIGN TOKENS — Head + Body sitting pose, ratio-driven
// ============================================================
// HEAD (circle, upper portion)
private const val HEAD_CX_RATIO = 0.50f        // head center X / canvas width
private const val HEAD_CY_RATIO = 0.37f        // head center Y / canvas height
private const val HEAD_R_RATIO = 0.21f         // head radius / canvas width

// BODY (ellipse, lower portion — sitting pose)
private const val BODY_CX_RATIO = 0.50f        // body center X / canvas width
private const val BODY_CY_RATIO = 0.62f        // body center Y / canvas height (below head)
private const val BODY_W_RATIO = 0.44f         // body ellipse width / canvas width
private const val BODY_H_RATIO = 0.32f         // body ellipse height / canvas height

// EYES — relative to head center
private const val EYE_OFFSET_Y = 0.08f         // eye Y offset below head center / headR
private const val EYE_SPACING_RATIO = 0.45f    // eye spacing factor (× headR)
private const val PUPIL_RATIO = 0.055f         // pupil radius / canvas width
private const val PUPIL_HIGHLIGHT_RATIO = 0.022f
private const val PUPIL_HIGHLIGHT_OFFSET_X = 0.020f
private const val PUPIL_HIGHLIGHT_OFFSET_Y = 0.022f

// BLUSH — on cheeks, below eyes
private const val BLUSH_OFFSET_Y = 0.42f       // blush Y below head center / headR
private const val BLUSH_SPACING_RATIO = 0.55f  // blush X offset factor (× headR)
private const val BLUSH_RADIUS_RATIO = 0.055f
private const val BLUSH_OVAL_W_FACTOR = 1.5f

// NOSE & MOUTH — on head
private const val NOSE_OFFSET_Y = 0.52f        // nose Y below head center / headR
private const val NOSE_RADIUS_RATIO = 0.022f
private const val MOUTH_OFFSET_Y = 0.68f        // mouth Y below head center / headR

// PAWS — front paws at body bottom (sitting pose)
private const val PAW_W_RATIO = 0.070f
private const val PAW_H_RATIO = 0.065f
private const val PAW_INSET_RATIO = 0.05f       // paw inset from body edge / bodyHalfW

// BELLY PATCH — on body
private const val BELLY_PATCH_W_RATIO = 0.24f
private const val BELLY_PATCH_H_RATIO = 0.20f
private const val BELLY_PATCH_OFFSET_Y = 0.15f  // belly Y offset below body center / bodyHalfH

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
/** All anchors computed once per canvas size — Head + Body sitting pose. */
private class PetAnchors(val w: Float, val h: Float) {
    // HEAD (circle, upper portion)
    val headCx = w * HEAD_CX_RATIO
    val headCy = h * HEAD_CY_RATIO
    val headR = w * HEAD_R_RATIO
    val headLeft = headCx - headR
    val headRight = headCx + headR
    val headTop = headCy - headR
    val headBottom = headCy + headR

    // BODY (ellipse, lower portion — sitting, head overlaps body top)
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

    // EYES — offset from head center (slightly below head center)
    val eyeY = headCy + headR * EYE_OFFSET_Y
    val leftEyeX = headCx - headR * EYE_SPACING_RATIO
    val rightEyeX = headCx + headR * EYE_SPACING_RATIO
    val pupilR = w * PUPIL_RATIO

    // BLUSH — below eyes on cheeks
    val blushY = headCy + headR * BLUSH_OFFSET_Y
    val leftBlushX = headCx - headR * BLUSH_SPACING_RATIO
    val rightBlushX = headCx + headR * BLUSH_SPACING_RATIO
    val blushR = w * BLUSH_RADIUS_RATIO

    // NOSE & MOUTH — on head
    val noseY = headCy + headR * NOSE_OFFSET_Y
    val noseR = w * NOSE_RADIUS_RATIO
    val mouthY = headCy + headR * MOUTH_OFFSET_Y

    // FRONT PAWS — at body bottom corners (sitting pose)
    val pawW = w * PAW_W_RATIO
    val pawH = h * PAW_H_RATIO
    val pawR = Math.min(pawW, pawH) / 2f
    val leftPawX = bodyCx - bodyHalfW * PAW_INSET_RATIO
    val rightPawX = bodyCx + bodyHalfW * PAW_INSET_RATIO
    val pawY = bodyBottom - pawH * 0.1f

    // BELLY PATCH — on body
    val bellyCx = bodyCx
    val bellyCy = bodyCy + bodyHalfH * BELLY_PATCH_OFFSET_Y
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

        // ---- Bottom shadow (under body) ----
        drawOval(
            color = Color.Black.copy(alpha = 0.05f),
            topLeft = Offset(anchors.bodyCx - anchors.bodyHalfW * 0.9f, anchors.bodyBottom - size.height * 0.005f),
            size = Size(anchors.bodyHalfW * 1.8f, size.height * 0.025f)
        )

        // ---- Back paws (behind body, at bottom corners) ----
        val pawR = size.width * 0.045f
        drawCircle(darkerColor.copy(alpha = 0.8f), radius = pawR, center = Offset(anchors.bodyCx - anchors.bodyHalfW * 0.6f, anchors.bodyBottom))
        drawCircle(darkerColor.copy(alpha = 0.8f), radius = pawR, center = Offset(anchors.bodyCx + anchors.bodyHalfW * 0.6f, anchors.bodyBottom))

        // ---- Body watercolor halo ----
        drawOval(
            color = bodyColor.copy(alpha = 0.12f),
            topLeft = Offset(anchors.bodyLeft - size.width * 0.025f, anchors.bodyTop - size.height * 0.02f),
            size = Size(anchors.bodyW + size.width * 0.05f, anchors.bodyH + size.height * 0.04f)
        )

        // ---- Body (radial gradient, oval sitting pose) ----
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(bellyColor, bodyColor),
                center = Offset(anchors.bodyCx, anchors.bodyCy - anchors.bodyHalfH * 0.3f),
                radius = anchors.bodyHalfH * 1.2f
            ),
            topLeft = Offset(anchors.bodyLeft, anchors.bodyTop),
            size = Size(anchors.bodyW, anchors.bodyH)
        )

        // Belly patch (lighter watercolor wash)
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(bellyColor, bellyColor.copy(alpha = 0.3f)),
                center = Offset(anchors.bodyCx, anchors.bodyCy + anchors.bodyHalfH * 0.1f),
                radius = anchors.bodyHalfW * 0.5f
            ),
            topLeft = Offset(anchors.bodyCx - anchors.bellyW / 2f, anchors.bellyCy - anchors.bellyH / 2f),
            size = Size(anchors.bellyW, anchors.bellyH)
        )

        // ---- Ears (behind head) ----
        when (species) {
            PetSpecies.CAT -> drawCatEars(anchors, bodyColor, darkerColor, bellyColor)
            PetSpecies.DOG -> drawDogEars(anchors, bodyColor, darkerColor, bellyColor)
            PetSpecies.RABBIT -> drawRabbitEars(anchors, bodyColor, darkerColor, bellyColor)
            PetSpecies.HAMSTER -> drawHamsterEars(anchors, bodyColor, darkerColor, bellyColor)
        }

        // ---- Head watercolor halo ----
        drawCircle(
            color = bodyColor.copy(alpha = 0.15f),
            radius = anchors.headR + size.width * 0.012f,
            center = Offset(anchors.headCx, anchors.headCy)
        )

        // ---- Head (radial gradient) ----
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(bellyColor, bodyColor),
                center = Offset(anchors.headCx, anchors.headCy - anchors.headR * 0.2f),
                radius = anchors.headR * 1.05f
            ),
            radius = anchors.headR,
            center = Offset(anchors.headCx, anchors.headCy)
        )

        // ---- Front paws (sitting pose, visible at front) ----
        drawPaws(anchors, pawColor = darkerColor)

        // ---- Blush marks (on cheeks) ----
        drawWatercolorBlush(anchors.leftBlushX, anchors.blushY, anchors.blushR, BlushColor)
        drawWatercolorBlush(anchors.rightBlushX, anchors.blushY, anchors.blushR, BlushColor)

        // ---- Eyes ----
        if (blink || state == PetState.SLEEPY) {
            val blinkHalf = anchors.pupilR * 0.6f
            drawLine(
                EyeColor,
                Offset(anchors.leftEyeX - blinkHalf, anchors.eyeY),
                Offset(anchors.leftEyeX + blinkHalf, anchors.eyeY),
                strokeWidth = anchors.pupilR * 0.55f, cap = StrokeCap.Round
            )
            drawLine(
                EyeColor,
                Offset(anchors.rightEyeX - blinkHalf, anchors.eyeY),
                Offset(anchors.rightEyeX + blinkHalf, anchors.eyeY),
                strokeWidth = anchors.pupilR * 0.55f, cap = StrokeCap.Round
            )
        } else {
            // Full pupil — larger for watercolor look
            drawCircle(EyeColor, radius = anchors.pupilR, center = Offset(anchors.leftEyeX, anchors.eyeY))
            drawCircle(EyeColor, radius = anchors.pupilR, center = Offset(anchors.rightEyeX, anchors.eyeY))
            // Primary catchlight (top-right)
            val hlR = size.width * PUPIL_HIGHLIGHT_RATIO
            drawCircle(
                Color.White, radius = hlR,
                center = Offset(anchors.leftEyeX + size.width * PUPIL_HIGHLIGHT_OFFSET_X, anchors.eyeY - size.height * PUPIL_HIGHLIGHT_OFFSET_Y)
            )
            drawCircle(
                Color.White, radius = hlR,
                center = Offset(anchors.rightEyeX + size.width * PUPIL_HIGHLIGHT_OFFSET_X, anchors.eyeY - size.height * PUPIL_HIGHLIGHT_OFFSET_Y)
            )
            // Secondary highlight (bottom-left, half opacity)
            drawCircle(
                Color.White.copy(alpha = 0.45f), radius = hlR * 0.45f,
                center = Offset(anchors.leftEyeX - size.width * 0.015f, anchors.eyeY + size.height * 0.015f)
            )
            drawCircle(
                Color.White.copy(alpha = 0.45f), radius = hlR * 0.45f,
                center = Offset(anchors.rightEyeX - size.width * 0.015f, anchors.eyeY + size.height * 0.015f)
            )
        }

        // ---- Snout / nose / mouth (species-specific) ----
        when (species) {
            PetSpecies.CAT -> drawCatSnout(anchors, size.width)
            PetSpecies.DOG -> drawDogSnout(anchors, size.width)
            PetSpecies.RABBIT -> drawRabbitSnout(anchors, size.width)
            PetSpecies.HAMSTER -> drawHamsterSnout(anchors, size.width)
        }

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
// SPECIES: EARS — positioned from Head anchors, with inner gradient
// ============================================================

private fun DrawScope.drawCatEars(a: PetAnchors, body: Color, dark: Color, belly: Color) {
    // Short triangular ears, upright on top of head
    val halfW = a.headR * 0.28f
    val innerHalfW = a.headR * 0.14f
    val baseY = a.headCy - a.headR * 0.75f
    val tipY = a.headCy - a.headR * 1.35f
    val innerTipY = a.headCy - a.headR * 1.10f

    // Left ear
    val leftBaseCenterX = a.headCx - a.headR * 0.55f
    drawPath(Path().apply {
        moveTo(leftBaseCenterX - halfW, baseY)
        lineTo(leftBaseCenterX, tipY)
        lineTo(leftBaseCenterX + halfW, baseY)
        close()
    }, brush = Brush.radialGradient(listOf(belly, body), center = Offset(leftBaseCenterX, tipY), radius = a.headR * 0.7f))
    drawPath(Path().apply {
        moveTo(leftBaseCenterX - innerHalfW * 0.6f, baseY - a.headR * 0.1f)
        lineTo(leftBaseCenterX, innerTipY)
        lineTo(leftBaseCenterX + innerHalfW * 0.6f, baseY - a.headR * 0.1f)
        close()
    }, InnerEarColor)

    // Right ear (mirror)
    val rightBaseCenterX = a.headCx + a.headR * 0.55f
    drawPath(Path().apply {
        moveTo(rightBaseCenterX - halfW, baseY)
        lineTo(rightBaseCenterX, tipY)
        lineTo(rightBaseCenterX + halfW, baseY)
        close()
    }, brush = Brush.radialGradient(listOf(belly, body), center = Offset(rightBaseCenterX, tipY), radius = a.headR * 0.7f))
    drawPath(Path().apply {
        moveTo(rightBaseCenterX - innerHalfW * 0.6f, baseY - a.headR * 0.1f)
        lineTo(rightBaseCenterX, innerTipY)
        lineTo(rightBaseCenterX + innerHalfW * 0.6f, baseY - a.headR * 0.1f)
        close()
    }, InnerEarColor)
}

private fun DrawScope.drawDogEars(a: PetAnchors, body: Color, dark: Color, belly: Color) {
    // Short triangular ears with slight droop (corgi/shiba style)
    val baseY = a.headCy - a.headR * 0.65f
    val tipY = a.headCy - a.headR * 1.10f

    // Left ear
    val leftInnerX = a.headCx - a.headR * 0.75f
    val leftOuterX = a.headCx - a.headR * 1.15f
    val leftDroopX = a.headCx - a.headR * 1.35f
    drawPath(Path().apply {
        moveTo(leftInnerX, baseY)
        lineTo(leftOuterX, tipY)
        lineTo(leftDroopX, baseY + a.headR * 0.30f)
        close()
    }, brush = Brush.radialGradient(listOf(body, dark), center = Offset(leftOuterX, tipY), radius = a.headR * 0.8f))
    drawPath(Path().apply {
        moveTo(leftInnerX + a.headR * 0.05f, baseY - a.headR * 0.05f)
        lineTo(leftOuterX + a.headR * 0.05f, tipY + a.headR * 0.05f)
        lineTo(leftDroopX + a.headR * 0.05f, baseY + a.headR * 0.25f)
        close()
    }, InnerEarColor)

    // Right ear (mirror)
    val rightInnerX = a.headCx + a.headR * 0.75f
    val rightOuterX = a.headCx + a.headR * 1.15f
    val rightDroopX = a.headCx + a.headR * 1.35f
    drawPath(Path().apply {
        moveTo(rightInnerX, baseY)
        lineTo(rightOuterX, tipY)
        lineTo(rightDroopX, baseY + a.headR * 0.30f)
        close()
    }, brush = Brush.radialGradient(listOf(body, dark), center = Offset(rightOuterX, tipY), radius = a.headR * 0.8f))
    drawPath(Path().apply {
        moveTo(rightInnerX - a.headR * 0.05f, baseY - a.headR * 0.05f)
        lineTo(rightOuterX - a.headR * 0.05f, tipY + a.headR * 0.05f)
        lineTo(rightDroopX - a.headR * 0.05f, baseY + a.headR * 0.25f)
        close()
    }, InnerEarColor)
}

private fun DrawScope.drawRabbitEars(a: PetAnchors, body: Color, dark: Color, belly: Color) {
    // Long upright oval ears — height = headR * 1.2 (per spec)
    val earH = a.headR * 1.3f
    val earW = a.headR * 0.38f
    val innerEarW = earW * 0.55f
    val topY = a.headCy - a.headR * 1.15f
    val bottomY = a.headCy - a.headR * 0.10f

    // Left ear
    val leftCx = a.headCx - a.headR * 0.45f
    drawOval(
        brush = Brush.radialGradient(listOf(belly, body), center = Offset(leftCx, topY + earH * 0.3f), radius = earH),
        topLeft = Offset(leftCx - earW / 2f, topY),
        size = Size(earW, bottomY - topY)
    )
    drawOval(InnerEarColor, Offset(leftCx - innerEarW / 2f, topY + earH * 0.15f), Size(innerEarW, earH * 0.8f))

    // Right ear
    val rightCx = a.headCx + a.headR * 0.45f
    drawOval(
        brush = Brush.radialGradient(listOf(belly, body), center = Offset(rightCx, topY + earH * 0.3f), radius = earH),
        topLeft = Offset(rightCx - earW / 2f, topY),
        size = Size(earW, bottomY - topY)
    )
    drawOval(InnerEarColor, Offset(rightCx - innerEarW / 2f, topY + earH * 0.15f), Size(innerEarW, earH * 0.8f))
}

private fun DrawScope.drawHamsterEars(a: PetAnchors, body: Color, dark: Color, belly: Color) {
    // Small round ears on top of head
    val earY = a.headCy - a.headR * 0.85f
    val earR = a.headR * 0.28f
    val innerR = earR * 0.50f
    val leftX = a.headCx - a.headR * 0.65f
    val rightX = a.headCx + a.headR * 0.65f

    drawCircle(
        brush = Brush.radialGradient(listOf(belly, body), center = Offset(leftX, earY - earR * 0.3f), radius = earR * 1.1f),
        radius = earR, center = Offset(leftX, earY)
    )
    drawCircle(
        brush = Brush.radialGradient(listOf(belly, body), center = Offset(rightX, earY - earR * 0.3f), radius = earR * 1.1f),
        radius = earR, center = Offset(rightX, earY)
    )
    drawCircle(InnerEarColor, radius = innerR, center = Offset(leftX, earY))
    drawCircle(InnerEarColor, radius = innerR, center = Offset(rightX, earY))
}

// ============================================================
// SPECIES: SNOUT / NOSE / MOUTH
// ============================================================

private fun DrawScope.drawCatSnout(a: PetAnchors, w: Float) {
    val noseTopY = a.noseY - a.noseR
    val noseBottomY = a.noseY + a.noseR
    drawPath(Path().apply {
        moveTo(a.headCx, noseTopY)
        lineTo(a.headCx - a.noseR * 1.1f, noseBottomY)
        lineTo(a.headCx + a.noseR * 1.1f, noseBottomY)
        close()
    }, SnoutColor)
    drawLine(WhiskerColor, Offset(a.headCx - a.headR * 0.5f, a.noseY + a.noseR), Offset(a.headCx - a.headR * 1.0f, a.noseY + a.noseR * 0.5f), 2f)
    drawLine(WhiskerColor, Offset(a.headCx - a.headR * 0.5f, a.noseY + a.noseR * 2f), Offset(a.headCx - a.headR * 1.0f, a.noseY + a.noseR * 1.5f), 2f)
    drawLine(WhiskerColor, Offset(a.headCx + a.headR * 0.5f, a.noseY + a.noseR), Offset(a.headCx + a.headR * 1.0f, a.noseY + a.noseR * 0.5f), 2f)
    drawLine(WhiskerColor, Offset(a.headCx + a.headR * 0.5f, a.noseY + a.noseR * 2f), Offset(a.headCx + a.headR * 1.0f, a.noseY + a.noseR * 1.5f), 2f)
    drawLine(
        Color.White,
        Offset(a.headCx - a.noseR * 1.2f, a.mouthY),
        Offset(a.headCx + a.noseR * 1.2f, a.mouthY),
        strokeWidth = 2.5f, cap = StrokeCap.Round
    )
}

private fun DrawScope.drawDogSnout(a: PetAnchors, w: Float) {
    val snoutR = w * 0.075f
    drawCircle(SnoutColor.copy(alpha = 0.55f), radius = snoutR, center = Offset(a.headCx, a.noseY + a.noseR * 0.5f))
    drawCircle(EyeColor, radius = a.noseR * 1.2f, center = Offset(a.headCx, a.noseY))
    drawLine(
        Color.White,
        Offset(a.headCx - a.noseR * 1.8f, a.mouthY + a.noseR),
        Offset(a.headCx + a.noseR * 1.8f, a.mouthY + a.noseR),
        strokeWidth = 2.5f, cap = StrokeCap.Round
    )
}

private fun DrawScope.drawRabbitSnout(a: PetAnchors, w: Float) {
    drawCircle(SnoutColor, radius = a.noseR * 0.9f, center = Offset(a.headCx, a.noseY))
    drawLine(WhiskerColor, Offset(a.headCx, a.noseY + a.noseR * 0.9f), Offset(a.headCx, a.mouthY), 2f)
    drawLine(
        Color.White,
        Offset(a.headCx - a.noseR * 1.2f, a.mouthY),
        Offset(a.headCx + a.noseR * 1.2f, a.mouthY),
        strokeWidth = 2.5f, cap = StrokeCap.Round
    )
}

private fun DrawScope.drawHamsterSnout(a: PetAnchors, w: Float) {
    drawCircle(SnoutColor, radius = a.noseR, center = Offset(a.headCx, a.noseY))
    drawLine(
        Color.White,
        Offset(a.headCx - a.noseR * 1.0f, a.mouthY),
        Offset(a.headCx + a.noseR * 1.0f, a.mouthY),
        strokeWidth = 2.5f, cap = StrokeCap.Round
    )
}

// ============================================================
// PAWS — sitting pose, front paws visible at body bottom
// ============================================================
private fun DrawScope.drawPaws(a: PetAnchors, pawColor: Color) {
    // Front left paw
    drawCircle(
        brush = Brush.radialGradient(
            listOf(pawColor.copy(alpha = 0.9f), pawColor),
            center = Offset(a.leftPawX, a.pawY - a.pawR * 0.3f),
            radius = a.pawR * 1.1f
        ),
        radius = a.pawR,
        center = Offset(a.leftPawX, a.pawY)
    )
    // Front right paw
    drawCircle(
        brush = Brush.radialGradient(
            listOf(pawColor.copy(alpha = 0.9f), pawColor),
            center = Offset(a.rightPawX, a.pawY - a.pawR * 0.3f),
            radius = a.pawR * 1.1f
        ),
        radius = a.pawR,
        center = Offset(a.rightPawX, a.pawY)
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
 * Draw watercolor-style blush: 3 overlapping transparent horizontal ellipses for soft wash effect.
 * Horizontal oval shape matches the watercolor design sheet.
 */
private fun DrawScope.drawWatercolorBlush(
    centerX: Float,
    centerY: Float,
    baseRadius: Float,
    baseColor: Color
) {
    val w = baseRadius * BLUSH_OVAL_W_FACTOR
    // Layer 1: large, faint wash
    drawOval(
        color = baseColor.copy(alpha = 0.25f),
        topLeft = Offset(centerX - w * 1.4f, centerY - baseRadius * 1.2f),
        size = Size(w * 2.8f, baseRadius * 2.4f)
    )
    // Layer 2: medium opacity
    drawOval(
        color = baseColor.copy(alpha = 0.35f),
        topLeft = Offset(centerX - w, centerY - baseRadius * 0.85f),
        size = Size(w * 2f, baseRadius * 1.7f)
    )
    // Layer 3: core, most opaque
    drawOval(
        color = baseColor.copy(alpha = 0.50f),
        topLeft = Offset(centerX - w * 0.6f, centerY - baseRadius * 0.5f),
        size = Size(w * 1.2f, baseRadius * 1.0f)
    )
}
