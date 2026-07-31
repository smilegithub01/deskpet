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
// DESIGN TOKENS — v13 Watercolor Final Match (sitting pose)
// All ratios driven by canvas size, no magic pixels.
// ============================================================

// BODY (oval, sitting low, slightly flattened)
private const val BODY_CX_RATIO = 0.50f
private const val BODY_CY_RATIO = 0.73f
private const val BODY_RX_RATIO = 0.26f
private const val BODY_RY_RATIO = 0.22f

// HEAD (round, roughly as wide as body)
private const val HEAD_CX_RATIO = 0.50f
private const val HEAD_CY_RATIO = 0.39f
private const val HEAD_R_RATIO = 0.235f

// EYES (oval, positioned slightly below head center)
private const val EYE_Y_OFFSET = 0.14f
private const val EYE_X_OFFSET = 0.42f
private const val EYE_W_RATIO = 0.24f   // eyeW / headR
private const val EYE_H_RATIO = 0.28f   // eyeH / headR

// BLUSH
private const val BLUSH_Y_OFFSET = 0.42f
private const val BLUSH_X_OFFSET = 0.58f
private const val BLUSH_R_RATIO = 0.20f

// NOSE & MOUTH
private const val NOSE_Y_OFFSET = 0.52f
private const val NOSE_R_RATIO = 0.075f
private const val MOUTH_Y_OFFSET = 0.66f

// PAWS
private const val PAW_X_OFFSET = 0.48f   // from bodyCx
private const val PAW_Y_OFFSET = 0.10f   // from bodyBottom (upward)
private const val PAW_R_RATIO = 0.13f    // pawR / headR

// FEET
private const val FOOT_X_OFFSET = 0.68f
private const val FOOT_R_RATIO = 0.15f

// Local drawing colors
private val OutlineColor = Color(0x886E5046)        // 110, 85, 70 @ 53%
private val OutlineSoft  = Color(0x556E5046)        // same @ 33%
private val BlushColor   = Color(0x80F4A7B9)
private val EyeColor     = Color(0xFF2D2420)
private val EyeLight     = Color(0xFF7A5545)
private val EyeHiMain    = Color(0xEAFEF5EA)
private val EyeHiSub     = Color(0x8CF0EEE8)
private val HeartColor   = Color(0xFFFF6B9D)
private val WhiskerColor = Color(0x4D5A5041)
private val InnerEarPink = Color(0xB2FFBECE)
private val NosePink     = Color(0xE6FF8FAB)

// ============================================================
// ANCHOR SYSTEM
// ============================================================
private class PetAnchors(val w: Float, val h: Float) {
    val bodyCx = w * BODY_CX_RATIO
    val bodyCy = h * BODY_CY_RATIO
    val bodyRx = w * BODY_RX_RATIO
    val bodyRy = h * BODY_RY_RATIO
    val bodyTop = bodyCy - bodyRy
    val bodyBottom = bodyCy + bodyRy
    val bodyLeft = bodyCx - bodyRx
    val bodyRight = bodyCx + bodyRx

    val headCx = w * HEAD_CX_RATIO
    val headCy = h * HEAD_CY_RATIO
    val headR = w * HEAD_R_RATIO

    val eyeY = headCy + headR * EYE_Y_OFFSET
    val leftEyeX = headCx - headR * EYE_X_OFFSET
    val rightEyeX = headCx + headR * EYE_X_OFFSET
    val eyeW = headR * EYE_W_RATIO
    val eyeH = headR * EYE_H_RATIO

    val blushY = headCy + headR * BLUSH_Y_OFFSET
    val leftBlushX = headCx - headR * BLUSH_X_OFFSET
    val rightBlushX = headCx + headR * BLUSH_X_OFFSET
    val blushR = headR * BLUSH_R_RATIO

    val noseY = headCy + headR * NOSE_Y_OFFSET
    val noseR = headR * NOSE_R_RATIO
    val mouthY = headCy + headR * MOUTH_Y_OFFSET

    val leftPawX = bodyCx - bodyRx * PAW_X_OFFSET
    val rightPawX = bodyCx + bodyRx * PAW_X_OFFSET
    val pawY = bodyBottom - bodyRy * PAW_Y_OFFSET
    val pawR = headR * PAW_R_RATIO

    val leftFootX = bodyCx - bodyRx * FOOT_X_OFFSET
    val rightFootX = bodyCx + bodyRx * FOOT_X_OFFSET
    val footY = bodyBottom
    val footR = headR * FOOT_R_RATIO
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
    val baseColor = remember(color) {
        val c = android.graphics.Color.parseColor(color.hex)
        Color(android.graphics.Color.red(c), android.graphics.Color.green(c), android.graphics.Color.blue(c))
    }
    val darkColor = remember(color) {
        val c = android.graphics.Color.parseColor(color.hex)
        Color(
            (android.graphics.Color.red(c) * 0.80f).toInt().coerceIn(0, 255),
            (android.graphics.Color.green(c) * 0.80f).toInt().coerceIn(0, 255),
            (android.graphics.Color.blue(c) * 0.80f).toInt().coerceIn(0, 255)
        )
    }
    val midColor = baseColor
    val lightColor = remember(color) {
        val c = android.graphics.Color.parseColor(color.hex)
        Color(
            (android.graphics.Color.red(c) * 1.12f).toInt().coerceIn(0, 255),
            (android.graphics.Color.green(c) * 1.12f).toInt().coerceIn(0, 255),
            (android.graphics.Color.blue(c) * 1.12f).toInt().coerceIn(0, 255)
        )
    }
    val lighterColor = remember(color) {
        val c = android.graphics.Color.parseColor(color.hex)
        Color(
            (android.graphics.Color.red(c) * 1.24f).toInt().coerceIn(0, 255),
            (android.graphics.Color.green(c) * 1.24f).toInt().coerceIn(0, 255),
            (android.graphics.Color.blue(c) * 1.24f).toInt().coerceIn(0, 255)
        )
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

        // 0. GROUND SHADOW
        drawGroundShadow(a, darkColor)

        // 1. BODY (watercolor)
        drawWatercolorBlob(
            cx = a.bodyCx, cy = a.bodyCy,
            rx = a.bodyRx, ry = a.bodyRy,
            mainColor = midColor, hiColor = lighterColor, dkColor = darkColor
        )

        // 2. EARS BEFORE HEAD
        drawEarsBeforeHead(a, species, midColor, darkColor, lightColor, lighterColor)

        // 3. BODY SOFT OUTLINE
        drawSoftOutline(
            ovalRect = androidx.compose.ui.geometry.Rect(
                left = a.bodyCx - a.bodyRx * 0.985f,
                top = a.bodyCy - a.bodyRy * 0.985f,
                right = a.bodyCx + a.bodyRx * 0.985f,
                bottom = a.bodyCy + a.bodyRy * 0.985f
            ),
            color = OutlineColor, width = 1.0f, alpha = 0.35f
        )

        // 4. HEAD (watercolor)
        drawWatercolorBlob(
            cx = a.headCx, cy = a.headCy,
            rx = a.headR, ry = a.headR,
            mainColor = midColor, hiColor = lighterColor, dkColor = darkColor
        )

        // 5. HEAD SOFT OUTLINE
        drawSoftCircleOutline(
            center = Offset(a.headCx, a.headCy),
            radius = a.headR * 0.985f,
            color = OutlineColor, width = 0.9f, alpha = 0.35f
        )

        // 6. EARS AFTER HEAD (rabbit)
        drawEarsAfterHead(a, species, midColor, darkColor, lightColor, lighterColor)

        // 7. FEET
        drawFoot(a.leftFootX, a.footY, a.footR, lightColor, midColor)
        drawFoot(a.rightFootX, a.footY, a.footR, lightColor, midColor)

        // 8. PAWS
        drawPaw(a.leftPawX, a.pawY, a.pawR, lightColor, midColor)
        drawPaw(a.rightPawX, a.pawY, a.pawR, lightColor, midColor)

        // 9. BLUSH
        drawBlushBlob(a.leftBlushX, a.blushY, a.blushR)
        drawBlushBlob(a.rightBlushX, a.blushY, a.blushR)

        // 10. EYES
        if (blink || state == PetState.SLEEPY) {
            val bh = a.eyeW * 0.9f
            drawLine(EyeColor.copy(alpha = 0.9f),
                Offset(a.leftEyeX - bh, a.eyeY),
                Offset(a.leftEyeX + bh, a.eyeY),
                strokeWidth = a.eyeH * 0.6f, cap = StrokeCap.Round)
            drawLine(EyeColor.copy(alpha = 0.9f),
                Offset(a.rightEyeX - bh, a.eyeY),
                Offset(a.rightEyeX + bh, a.eyeY),
                strokeWidth = a.eyeH * 0.6f, cap = StrokeCap.Round)
        } else {
            drawEye(a.leftEyeX, a.eyeY, a.eyeW, a.eyeH)
            drawEye(a.rightEyeX, a.eyeY, a.eyeW, a.eyeH)
        }

        // 11. SNOUT
        when (species) {
            PetSpecies.CAT -> drawCatSnout(a)
            PetSpecies.DOG -> drawDogSnout(a)
            PetSpecies.RABBIT -> drawRabbitSnout(a)
            PetSpecies.HAMSTER -> drawHamsterSnout(a)
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
                            OutfitCategory.COLLAR -> Triple(a.headCx, a.bodyTop + a.bodyRy * 0.15f, 0.12f)
                            OutfitCategory.CLOTHING -> Triple(a.bodyCx, a.bodyCy + a.bodyRy * 0.2f, 0.20f)
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
// WATERCOLOR PRIMITIVES
// ============================================================

private fun DrawScope.drawGroundShadow(a: PetAnchors, dark: Color) {
    val g = Brush.radialGradient(
        colors = listOf(dark.copy(alpha = 0.22f), dark.copy(alpha = 0f)),
        center = Offset(a.bodyCx, a.bodyBottom + 6f),
        radius = a.bodyRx * 1.5f
    )
    drawOval(
        brush = g,
        topLeft = Offset(a.bodyCx - a.bodyRx * 1.25f, a.bodyBottom - a.bodyRy * 0.05f),
        size = Size(a.bodyRx * 2.5f, a.bodyRy * 0.6f)
    )
}

// Multi-layer watercolor blob (halo + body + highlight)
private fun DrawScope.drawWatercolorBlob(
    cx: Float, cy: Float, rx: Float, ry: Float,
    mainColor: Color, hiColor: Color, dkColor: Color
) {
    val maxR = maxOf(rx, ry)

    // Layer 1: soft halo (transparent)
    val halo = Brush.radialGradient(
        colors = listOf(mainColor.copy(alpha = 0.08f), mainColor.copy(alpha = 0f)),
        center = Offset(cx, cy),
        radius = maxR * 1.8f
    )
    drawOval(
        brush = halo,
        topLeft = Offset(cx - rx * 1.7f, cy - ry * 1.7f),
        size = Size(rx * 3.4f, ry * 3.4f)
    )

    // Layer 2: body (gradient, soft edge)
    val body = Brush.radialGradient(
        colors = listOf(hiColor, mainColor, dkColor.copy(alpha = 0.85f), dkColor.copy(alpha = 0f)),
        stops = listOf(0f, 0.55f, 0.85f, 1.0f),
        center = Offset(cx - rx * 0.25f, cy - ry * 0.3f),
        radius = maxR * 1.15f
    )
    drawOval(
        brush = body,
        topLeft = Offset(cx - rx, cy - ry),
        size = Size(rx * 2f, ry * 2f)
    )

    // Layer 3: upper highlight
    val hl = Brush.radialGradient(
        colors = listOf(hiColor.copy(alpha = 0.6f), hiColor.copy(alpha = 0f)),
        center = Offset(cx - rx * 0.3f, cy - ry * 0.45f),
        radius = rx * 0.6f
    )
    drawOval(
        brush = hl,
        topLeft = Offset(cx - rx * 0.7f, cy - ry * 0.85f),
        size = Size(rx * 1.1f, ry * 0.9f)
    )
}

private fun DrawScope.drawSoftOutline(
    ovalRect: androidx.compose.ui.geometry.Rect,
    strokeColor: Color, width: Float, alpha: Float
) {
    drawIntoCanvas { canvas ->
        val paint = android.graphics.Paint().apply {
            this.color = android.graphics.Color.argb(
                (alpha * 255).toInt().coerceIn(0, 255),
                (strokeColor.red * 255).toInt(),
                (strokeColor.green * 255).toInt(),
                (strokeColor.blue * 255).toInt()
            )
            strokeWidth = width
            style = android.graphics.Paint.Style.STROKE
            isAntiAlias = true
            strokeCap = android.graphics.Paint.Cap.ROUND
            strokeJoin = android.graphics.Paint.Join.ROUND
        }
        canvas.nativeCanvas.drawOval(
            ovalRect.left, ovalRect.top,
            ovalRect.right, ovalRect.bottom,
            paint
        )
    }
}

private fun DrawScope.drawSoftCircleOutline(
    center: Offset, radius: Float,
    strokeColor: Color, width: Float, alpha: Float
) {
    drawIntoCanvas { canvas ->
        val paint = android.graphics.Paint().apply {
            this.color = android.graphics.Color.argb(
                (alpha * 255).toInt().coerceIn(0, 255),
                (strokeColor.red * 255).toInt(),
                (strokeColor.green * 255).toInt(),
                (strokeColor.blue * 255).toInt()
            )
            strokeWidth = width
            style = android.graphics.Paint.Style.STROKE
            isAntiAlias = true
            strokeCap = android.graphics.Paint.Cap.ROUND
        }
        canvas.nativeCanvas.drawCircle(center.x, center.y, radius, paint)
    }
}

// ============================================================
// EYES
// ============================================================
private fun DrawScope.drawEye(ex: Float, ey: Float, ew: Float, eh: Float) {
    // Eye body (brown gradient)
    val g = Brush.radialGradient(
        colors = listOf(EyeLight, EyeColor),
        center = Offset(ex - ew * 0.25f, ey - eh * 0.25f),
        radius = maxOf(ew, eh) * 1.05f
    )
    drawOval(
        brush = g,
        topLeft = Offset(ex - ew, ey - eh),
        size = Size(ew * 2f, eh * 2f)
    )

    // Main highlight (upper-left diagonal ellipse)
    drawOval(
        color = EyeHiMain,
        topLeft = Offset(ex - ew * 0.8f, ey - eh * 0.7f),
        size = Size(ew * 0.8f, eh * 0.56f)
    )

    // Secondary catchlight
    drawCircle(
        color = EyeHiSub,
        radius = ew * 0.13f,
        center = Offset(ex + ew * 0.35f, ey + eh * 0.22f)
    )
}

// ============================================================
// BLUSH (very soft watercolor)
// ============================================================
private fun DrawScope.drawBlushBlob(bx: Float, by: Float, br: Float) {
    val g = Brush.radialGradient(
        colors = listOf(BlushColor.copy(alpha = 0.40f), BlushColor.copy(alpha = 0.18f), BlushColor.copy(alpha = 0f)),
        stops = listOf(0f, 0.6f, 1f),
        center = Offset(bx, by),
        radius = br * 2.0f
    )
    drawOval(
        brush = g,
        topLeft = Offset(bx - br * 1.4f, by - br * 0.85f),
        size = Size(br * 2.8f, br * 1.7f)
    )
}

// ============================================================
// PAWS & FEET (with pink pads)
// ============================================================
private fun DrawScope.drawFoot(cx: Float, cy: Float, r: Float, light: Color, mid: Color) {
    val g = Brush.radialGradient(
        colors = listOf(light, mid),
        center = Offset(cx, cy - r * 0.3f),
        radius = r * 1.3f
    )
    drawOval(
        brush = g,
        topLeft = Offset(cx - r, cy - r * 0.85f),
        size = Size(r * 2f, r * 1.7f)
    )
    drawOval(
        color = InnerEarPink,
        topLeft = Offset(cx - r * 0.45f, cy - r * 0.45f),
        size = Size(r * 0.9f, r * 0.56f)
    )
}

private fun DrawScope.drawPaw(cx: Float, cy: Float, r: Float, light: Color, mid: Color) {
    val g = Brush.radialGradient(
        colors = listOf(light, mid),
        center = Offset(cx, cy - r * 0.3f),
        radius = r * 1.2f
    )
    drawCircle(brush = g, radius = r, center = Offset(cx, cy))
    drawOval(
        color = InnerEarPink,
        topLeft = Offset(cx - r * 0.55f, cy - r * 0.28f),
        size = Size(r * 1.1f, r * 0.76f)
    )
}

// ============================================================
// EARS (before head)
// ============================================================
private fun DrawScope.drawEarsBeforeHead(
    a: PetAnchors,
    species: PetSpecies,
    base: Color, dark: Color, light: Color, lighter: Color
) {
    val hcx = a.headCx
    val hcy = a.headCy
    val hr = a.headR

    when (species) {
        PetSpecies.CAT -> {
            val earBaseY = hcy - hr * 0.85f
            val earTipY  = hcy - hr * 1.55f
            val earInnerTipY = hcy - hr * 1.20f
            val earW = hr * 0.55f
            for (side in intArrayOf(-1, 1)) {
                val cx = hcx + side * hr * 0.72f
                // Outer ear
                drawPath(Path().apply {
                    moveTo(cx - earW * 0.5f, earBaseY)
                    quadTo(cx - earW * 0.18f, earTipY + hr * 0.05f, cx, earTipY)
                    quadTo(cx + earW * 0.18f, earTipY + hr * 0.05f, cx + earW * 0.5f, earBaseY)
                    close()
                }, base)
                // Inner pink
                drawPath(Path().apply {
                    moveTo(cx - earW * 0.28f, earBaseY - hr * 0.03f)
                    quadTo(cx - earW * 0.08f, earInnerTipY, cx + earW * 0.08f, earInnerTipY)
                    quadTo(cx + earW * 0.28f, earBaseY - hr * 0.03f, cx + earW * 0.28f, earBaseY - hr * 0.03f)
                    close()
                }, InnerEarPink)
            }
        }

        PetSpecies.DOG -> {
            val earTopY = hcy - hr * 0.55f
            val earBottomY = hcy + hr * 0.55f
            val earW = hr * 0.40f
            for (side in intArrayOf(-1, 1)) {
                val outerX = hcx + side * hr * 0.92f
                val innerX = hcx + side * hr * 0.32f
                // Outer ear
                drawPath(Path().apply {
                    moveTo(innerX, earTopY)
                    cubicTo(
                        outerX - earW * 0.2f, earTopY - hr * 0.02f,
                        outerX + side * earW * 0.5f, earTopY + hr * 0.25f,
                        outerX + side * earW * 0.38f, earBottomY
                    )
                    cubicTo(
                        outerX + side * earW * 0.28f, earBottomY + hr * 0.08f,
                        outerX - side * earW * 0.05f, earTopY + hr * 0.18f,
                        innerX + side * 2f, earTopY + hr * 0.02f
                    )
                    close()
                }, base)
                // Inner pink
                drawPath(Path().apply {
                    moveTo(innerX + side * 3f, earTopY + hr * 0.02f)
                    cubicTo(
                        outerX - side * earW * 0.12f, earTopY + hr * 0.05f,
                        outerX + side * earW * 0.30f, earTopY + hr * 0.30f,
                        outerX + side * earW * 0.25f, earBottomY - hr * 0.03f
                    )
                    cubicTo(
                        outerX + side * earW * 0.15f, earBottomY + hr * 0.04f,
                        outerX - side * earW * 0.03f, earTopY + hr * 0.18f,
                        innerX + side * 4f, earTopY + hr * 0.06f
                    )
                    close()
                }, InnerEarPink.copy(alpha = 0.75f))
            }
        }

        PetSpecies.HAMSTER -> {
            val earR = hr * 0.20f
            val earY = hcy - hr * 0.88f
            for (side in intArrayOf(-1, 1)) {
                val ex = hcx + side * hr * 0.52f
                drawCircle(base, radius = earR, center = Offset(ex, earY))
                drawCircle(InnerEarPink.copy(alpha = 0.9f), radius = earR * 0.52f, center = Offset(ex, earY))
            }
        }

        else -> {} // rabbit ears drawn after head
    }
}

// ============================================================
// EARS (after head) — rabbit
// ============================================================
private fun DrawScope.drawEarsAfterHead(
    a: PetAnchors,
    species: PetSpecies,
    base: Color, dark: Color, light: Color, lighter: Color
) {
    if (species != PetSpecies.RABBIT) return
    val hcx = a.headCx
    val hcy = a.headCy
    val hr = a.headR

    val earH = hr * 1.80f
    val earW = hr * 0.30f
    val topY = hcy - hr * 1.75f
    val bottomY = hcy - hr * 0.22f
    val midY = (topY + bottomY) / 2f

    for (side in intArrayOf(-1, 1)) {
        val ex = hcx + side * hr * 0.28f
        // Outer ear
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(lighter, base),
                center = Offset(ex, midY - earH * 0.15f),
                radius = earH * 0.9f
            ),
            topLeft = Offset(ex - earW / 2f, midY - earH / 2f),
            size = Size(earW, earH)
        )
        // Inner pink
        drawOval(
            InnerEarPink,
            topLeft = Offset(ex - earW * 0.225f, midY + earH * 0.08f - earH * 0.2475f),
            size = Size(earW * 0.45f, earH * 0.55f)
        )
    }
}

// ============================================================
// SNOUT
// ============================================================
private fun DrawScope.drawCatSnout(a: PetAnchors) {
    // Triangular nose
    drawPath(Path().apply {
        moveTo(a.headCx, a.noseY - a.noseR)
        lineTo(a.headCx - a.noseR * 1.15f, a.noseY + a.noseR * 0.85f)
        lineTo(a.headCx + a.noseR * 1.15f, a.noseY + a.noseR * 0.85f)
        close()
    }, NosePink)
    // Mouth
    drawPath(Path().apply {
        moveTo(a.headCx - a.noseR * 1.15f, a.mouthY)
        quadTo(a.headCx, a.mouthY + a.noseR * 0.55f, a.headCx + a.noseR * 1.15f, a.mouthY)
    }, color = OutlineSoft, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.6f, cap = StrokeCap.Round))
    // Whiskers
    strokeWhiskers(a)
}

private fun DrawScope.drawDogSnout(a: PetAnchors) {
    // Small round nose
    drawCircle(EyeColor, radius = a.noseR * 1.15f, center = Offset(a.headCx, a.noseY))
    // Snout pad below
    drawOval(
        NosePink.copy(alpha = 0.55f),
        topLeft = Offset(a.headCx - a.noseR * 1.35f, a.noseY + a.noseR * 0.85f),
        size = Size(a.noseR * 2.7f, a.noseR * 1.1f)
    )
    // Mouth
    drawPath(Path().apply {
        moveTo(a.headCx - a.noseR * 1.55f, a.mouthY + a.noseR * 0.25f)
        quadTo(a.headCx, a.mouthY + a.noseR * 1.0f, a.headCx + a.noseR * 1.55f, a.mouthY + a.noseR * 0.25f)
    }, color = OutlineSoft, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.6f, cap = StrokeCap.Round))
}

private fun DrawScope.drawRabbitSnout(a: PetAnchors) {
    // Oval nose
    drawOval(
        NosePink,
        topLeft = Offset(a.headCx - a.noseR * 0.95f, a.noseY - a.noseR * 0.7f),
        size = Size(a.noseR * 1.9f, a.noseR * 1.4f)
    )
    // Mouth
    drawPath(Path().apply {
        moveTo(a.headCx, a.noseY + a.noseR * 0.75f)
        lineTo(a.headCx, a.mouthY)
    }, color = OutlineSoft, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.6f, cap = StrokeCap.Round))
    drawPath(Path().apply {
        moveTo(a.headCx - a.noseR * 1.15f, a.mouthY)
        quadTo(a.headCx, a.mouthY + a.noseR * 0.5f, a.headCx + a.noseR * 1.15f, a.mouthY)
    }, color = OutlineSoft, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.6f, cap = StrokeCap.Round))
}

private fun DrawScope.drawHamsterSnout(a: PetAnchors) {
    // Round nose
    drawCircle(NosePink, radius = a.noseR * 0.95f, center = Offset(a.headCx, a.noseY))
    // Mouth
    drawPath(Path().apply {
        moveTo(a.headCx - a.noseR, a.mouthY)
        quadTo(a.headCx, a.mouthY + a.noseR * 0.4f, a.headCx + a.noseR, a.mouthY)
    }, color = OutlineSoft, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f, cap = StrokeCap.Round))
    strokeWhiskers(a)
}

private fun DrawScope.strokeWhiskers(a: PetAnchors) {
    // Left whiskers
    drawLine(WhiskerColor,
        Offset(a.headCx - a.headR * 0.28f, a.noseY + a.noseR),
        Offset(a.headCx - a.headR * 0.82f, a.noseY + a.noseR * 0.25f), 1.1f)
    drawLine(WhiskerColor,
        Offset(a.headCx - a.headR * 0.28f, a.noseY + a.noseR * 1.6f),
        Offset(a.headCx - a.headR * 0.82f, a.noseY + a.noseR * 1.05f), 1.1f)
    // Right whiskers
    drawLine(WhiskerColor,
        Offset(a.headCx + a.headR * 0.28f, a.noseY + a.noseR),
        Offset(a.headCx + a.headR * 0.82f, a.noseY + a.noseR * 0.25f), 1.1f)
    drawLine(WhiskerColor,
        Offset(a.headCx + a.headR * 0.28f, a.noseY + a.noseR * 1.6f),
        Offset(a.headCx + a.headR * 0.82f, a.noseY + a.noseR * 1.05f), 1.1f)
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
