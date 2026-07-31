package com.deskpet.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.deskpet.app.data.model.OutfitCategory
import com.deskpet.app.data.model.PetColor
import com.deskpet.app.data.model.PetSpecies
import com.deskpet.app.data.model.PetState
import kotlinx.coroutines.delay

// ============================================================
// DESIGN TOKENS — Watercolor Storybook (matches reference illustration)
//  - Large expressive eyes with dual highlights
//  - Soft multi-layer watercolor halos
//  - Visible front paws (sitting pose)
//  - Species-correct ear shapes
//  - Prominent blush cheeks
// ============================================================

// BODY (oval, sitting, wider than tall)
private const val BODY_CX_RATIO = 0.50f
private const val BODY_CY_RATIO = 0.73f
private const val BODY_RX_RATIO = 0.27f
private const val BODY_RY_RATIO = 0.22f

// HEAD (round, slightly smaller than body)
private const val HEAD_CX_RATIO = 0.50f
private const val HEAD_CY_RATIO = 0.38f
private const val HEAD_R_RATIO = 0.22f

// EYES (large, oval, the focal point)
private const val EYE_Y_OFFSET = 0.08f
private const val EYE_X_OFFSET = 0.40f
private const val EYE_W_RATIO = 0.35f
private const val EYE_H_RATIO = 0.38f

// BLUSH (prominent, on cheek area)
private const val BLUSH_Y_OFFSET = 0.38f
private const val BLUSH_X_OFFSET = 0.62f
private const val BLUSH_R_RATIO = 0.28f

// NOSE & MOUTH
private const val NOSE_Y_OFFSET = 0.52f
private const val NOSE_R_RATIO = 0.07f
private const val MOUTH_Y_OFFSET = 0.66f

// PAWS (visible at front, sitting pose)
private const val PAW_X_OFFSET = 0.45f
private const val PAW_Y_OFFSET = 0.02f
private const val PAW_R_RATIO = 0.15f

// FEET (back feet, under body)
private const val FOOT_X_OFFSET = 0.65f
private const val FOOT_R_RATIO = 0.14f

// Drawing colors
private val OutlineColor = Color(0x886E5046)
private val OutlineSoft  = Color(0x556E5046)
private val BlushColor   = Color(0x88F4A7B9)
private val EyeColor     = Color(0xFF2D2420)
private val EyeLight     = Color(0xFF7A5545)
private val EyeHiMain    = Color(0xF0FEF5EA)
private val EyeHiSub     = Color(0x9CF0EEE8)
private val HeartColor   = Color(0xFFFF6B9D)
private val WhiskerColor = Color(0x4D5A5041)
private val InnerEarPink = Color(0xB2FFBECE)
private val NosePink     = Color(0xE6FF8FAB)
private val HaloColor    = Color(0x40FFFFFF)

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

    // Outfit anchors (shared, no hardcoded magic numbers)
    val headTopY = headCy - headR * 1.05f
    val collarY = bodyTop + bodyRy * 0.12f
    val clothingY = bodyCy + bodyRy * 0.22f
    val tailX = bodyRight + headR * 0.15f
    val tailY = bodyBottom - headR * 0.35f
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

    // 行业方案：图片资源优先，代码做轻量动效；无图时走矢量 fallback
    val context = LocalContext.current
    val petBitmap = remember(species, color) {
        PetImageLoader.loadPetBitmap(context, species, color)
    }

    if (petBitmap != null) {
        Image(
            bitmap = petBitmap,
            contentDescription = "桌宠",
            modifier = modifier.graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationY = if (state == PetState.HAPPY || state == PetState.EXCITED) -6f else 0f
            },
            contentScale = ContentScale.Fit
        )
    } else {
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

        // 2. EARS BEFORE HEAD (cat, dog, hamster)
        drawEarsBeforeHead(a, species, baseColor, darkColor, lightColor, lighterColor)

        // 3. BODY OUTLINE (very soft)
        drawSoftOutline(
            ovalRect = androidx.compose.ui.geometry.Rect(
                left = a.bodyCx - a.bodyRx * 0.97f,
                top = a.bodyCy - a.bodyRy * 0.97f,
                right = a.bodyCx + a.bodyRx * 0.97f,
                bottom = a.bodyCy + a.bodyRy * 0.97f
            ),
            strokeColor = OutlineColor, width = 0.8f, alpha = 0.25f
        )

        // 4. HEAD (watercolor)
        drawWatercolorBlob(
            cx = a.headCx, cy = a.headCy,
            rx = a.headR, ry = a.headR,
            mainColor = midColor, hiColor = lighterColor, dkColor = darkColor
        )

        // 5. HEAD OUTLINE (very soft)
        drawSoftCircleOutline(
            center = Offset(a.headCx, a.headCy),
            radius = a.headR * 0.97f,
            strokeColor = OutlineColor, width = 0.7f, alpha = 0.25f
        )

        // 6. EARS AFTER HEAD (rabbit)
        drawEarsAfterHead(a, species, baseColor, darkColor, lightColor, lighterColor)

        // 7. BLUSH (under eyes, on cheeks)
        drawBlushBlob(a.leftBlushX, a.blushY, a.blushR)
        drawBlushBlob(a.rightBlushX, a.blushY, a.blushR)

        // 8. EYES
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

        // 9. SNOUT
        when (species) {
            PetSpecies.CAT -> drawCatSnout(a)
            PetSpecies.DOG -> drawDogSnout(a)
            PetSpecies.RABBIT -> drawRabbitSnout(a)
            PetSpecies.HAMSTER -> drawHamsterSnout(a)
        }

        // 10. FEET (back feet)
        drawFoot(a.leftFootX, a.footY, a.footR, lightColor, midColor)
        drawFoot(a.rightFootX, a.footY, a.footR, lightColor, midColor)

        // 11. PAWS (front paws, visible)
        drawPaw(a.leftPawX, a.pawY, a.pawR, lightColor, midColor)
        drawPaw(a.rightPawX, a.pawY, a.pawR, lightColor, midColor)

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
                            OutfitCategory.HEAD -> Triple(a.headCx, a.headTopY - a.headR * 0.1f, 0.15f)
                            OutfitCategory.GLASSES -> Triple(a.headCx, a.eyeY, 0.14f)
                            OutfitCategory.COLLAR -> Triple(a.headCx, a.collarY, 0.12f)
                            OutfitCategory.CLOTHING -> Triple(a.bodyCx, a.clothingY, 0.20f)
                            OutfitCategory.TAIL -> Triple(a.tailX, a.tailY, 0.13f)
                            OutfitCategory.ACCESSORY -> Triple(a.bodyLeft - a.headR * 0.15f, a.eyeY, 0.12f)
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
    } // else vector fallback
}

// ============================================================
// WATERCOLOR PRIMITIVES
// ============================================================

private fun DrawScope.drawGroundShadow(a: PetAnchors, dark: Color) {
    val g = Brush.radialGradient(
        colors = listOf(dark.copy(alpha = 0.18f), dark.copy(alpha = 0f)),
        center = Offset(a.bodyCx, a.bodyBottom + 4f),
        radius = a.bodyRx * 1.4f
    )
    drawOval(
        brush = g,
        topLeft = Offset(a.bodyCx - a.bodyRx * 1.2f, a.bodyBottom - a.bodyRy * 0.02f),
        size = Size(a.bodyRx * 2.4f, a.bodyRy * 0.5f)
    )
}

private fun DrawScope.drawWatercolorBlob(
    cx: Float, cy: Float, rx: Float, ry: Float,
    mainColor: Color, hiColor: Color, dkColor: Color
) {
    val maxR = maxOf(rx, ry)

    // Layer 1: wide soft halo (watercolor wash)
    val halo = Brush.radialGradient(
        colors = listOf(mainColor.copy(alpha = 0.12f), mainColor.copy(alpha = 0f)),
        center = Offset(cx, cy),
        radius = maxR * 2.2f
    )
    drawOval(
        brush = halo,
        topLeft = Offset(cx - rx * 2.0f, cy - ry * 2.0f),
        size = Size(rx * 4.0f, ry * 4.0f)
    )

    // Layer 2: mid halo
    val midHalo = Brush.radialGradient(
        colors = listOf(hiColor.copy(alpha = 0.18f), mainColor.copy(alpha = 0f)),
        center = Offset(cx - rx * 0.1f, cy - ry * 0.15f),
        radius = maxR * 1.5f
    )
    drawOval(
        brush = midHalo,
        topLeft = Offset(cx - rx * 1.6f, cy - ry * 1.6f),
        size = Size(rx * 3.2f, ry * 3.2f)
    )

    // Layer 3: body (gradient with soft edge fade-out)
    val body = Brush.radialGradient(
        0f to hiColor,
        0.45f to mainColor,
        0.75f to dkColor.copy(alpha = 0.82f),
        1.0f to dkColor.copy(alpha = 0f),
        center = Offset(cx - rx * 0.2f, cy - ry * 0.25f),
        radius = maxR * 1.2f
    )
    drawOval(
        brush = body,
        topLeft = Offset(cx - rx, cy - ry),
        size = Size(rx * 2f, ry * 2f)
    )

    // Layer 4: upper highlight (pale wash)
    val hl = Brush.radialGradient(
        colors = listOf(hiColor.copy(alpha = 0.55f), hiColor.copy(alpha = 0f)),
        center = Offset(cx - rx * 0.35f, cy - ry * 0.5f),
        radius = rx * 0.6f
    )
    drawOval(
        brush = hl,
        topLeft = Offset(cx - rx * 0.8f, cy - ry * 0.95f),
        size = Size(rx * 1.3f, ry * 1.1f)
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
// EYES (large, expressive, with dual highlights)
// ============================================================
private fun DrawScope.drawEye(ex: Float, ey: Float, ew: Float, eh: Float) {
    // Eye body (brown radial gradient)
    val g = Brush.radialGradient(
        colors = listOf(EyeLight, EyeColor),
        center = Offset(ex - ew * 0.25f, ey - eh * 0.25f),
        radius = maxOf(ew, eh) * 1.0f
    )
    drawOval(
        brush = g,
        topLeft = Offset(ex - ew, ey - eh),
        size = Size(ew * 2f, eh * 2f)
    )

    // Main highlight (large, upper-left)
    drawOval(
        color = EyeHiMain,
        topLeft = Offset(ex - ew * 0.9f, ey - eh * 0.8f),
        size = Size(ew * 0.95f, eh * 0.65f)
    )

    // Secondary catchlight (small, lower-right)
    drawCircle(
        color = EyeHiSub,
        radius = ew * 0.18f,
        center = Offset(ex + ew * 0.42f, ey + eh * 0.28f)
    )
}

// ============================================================
// BLUSH (soft watercolor on cheeks)
// ============================================================
private fun DrawScope.drawBlushBlob(bx: Float, by: Float, br: Float) {
    val g = Brush.radialGradient(
        0f to BlushColor.copy(alpha = 0.50f),
        0.5f to BlushColor.copy(alpha = 0.22f),
        1f to BlushColor.copy(alpha = 0f),
        center = Offset(bx, by),
        radius = br * 1.8f
    )
    drawOval(
        brush = g,
        topLeft = Offset(bx - br * 1.3f, by - br * 0.8f),
        size = Size(br * 2.6f, br * 1.6f)
    )
}

// ============================================================
// PAWS & FEET
// ============================================================
private fun DrawScope.drawFoot(cx: Float, cy: Float, r: Float, light: Color, mid: Color) {
    val g = Brush.radialGradient(
        colors = listOf(light, mid),
        center = Offset(cx, cy - r * 0.25f),
        radius = r * 1.2f
    )
    drawOval(
        brush = g,
        topLeft = Offset(cx - r, cy - r * 0.8f),
        size = Size(r * 2f, r * 1.6f)
    )
    // Pink pad
    drawOval(
        color = InnerEarPink,
        topLeft = Offset(cx - r * 0.4f, cy - r * 0.4f),
        size = Size(r * 0.8f, r * 0.5f)
    )
}

private fun DrawScope.drawPaw(cx: Float, cy: Float, r: Float, light: Color, mid: Color) {
    val g = Brush.radialGradient(
        colors = listOf(light, mid),
        center = Offset(cx, cy - r * 0.25f),
        radius = r * 1.1f
    )
    drawCircle(brush = g, radius = r, center = Offset(cx, cy))
    // Pink pad
    drawOval(
        color = InnerEarPink,
        topLeft = Offset(cx - r * 0.5f, cy - r * 0.25f),
        size = Size(r * 1.0f, r * 0.7f)
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
            val earBaseY = hcy - hr * 0.82f
            val earTipY  = hcy - hr * 1.52f
            val earW = hr * 0.55f
            for (side in intArrayOf(-1, 1)) {
                val cx = hcx + side * hr * 0.72f
                // Outer ear - triangular with soft watercolor
                drawPath(Path().apply {
                    moveTo(cx - earW * 0.5f, earBaseY)
                    cubicTo(cx - earW * 0.18f, earTipY + hr * 0.06f, cx - earW * 0.18f, earTipY + hr * 0.06f, cx, earTipY)
                    cubicTo(cx + earW * 0.18f, earTipY + hr * 0.06f, cx + earW * 0.18f, earTipY + hr * 0.06f, cx + earW * 0.5f, earBaseY)
                    close()
                }, base)
                // Inner pink
                drawPath(Path().apply {
                    moveTo(cx - earW * 0.32f, earBaseY - hr * 0.02f)
                    cubicTo(cx - earW * 0.12f, earTipY + hr * 0.18f, cx - earW * 0.12f, earTipY + hr * 0.18f, cx + earW * 0.12f, earTipY + hr * 0.18f)
                    cubicTo(cx + earW * 0.32f, earBaseY - hr * 0.02f, cx + earW * 0.32f, earBaseY - hr * 0.02f, cx + earW * 0.32f, earBaseY - hr * 0.02f)
                    close()
                }, InnerEarPink)
            }
        }

        PetSpecies.DOG -> {
            val earTopY = hcy - hr * 0.45f
            val earBottomY = hcy + hr * 0.75f
            val earW = hr * 0.42f
            for (side in intArrayOf(-1, 1)) {
                val outerX = hcx + side * hr * 0.92f
                val innerX = hcx + side * hr * 0.28f
                // Outer ear - floppy, hangs down with gentle curve
                drawPath(Path().apply {
                    moveTo(innerX, earTopY)
                    cubicTo(
                        outerX - earW * 0.2f, earTopY - hr * 0.02f,
                        outerX + side * earW * 0.7f, earTopY + hr * 0.35f,
                        outerX + side * earW * 0.5f, earBottomY
                    )
                    cubicTo(
                        outerX + side * earW * 0.3f, earBottomY + hr * 0.12f,
                        outerX - side * earW * 0.1f, earTopY + hr * 0.25f,
                        innerX + side * 2f, earTopY + hr * 0.03f
                    )
                    close()
                }, base)
                // Inner pink
                drawPath(Path().apply {
                    moveTo(innerX + side * 2f, earTopY + hr * 0.03f)
                    cubicTo(
                        outerX - side * earW * 0.1f, earTopY + hr * 0.1f,
                        outerX + side * earW * 0.4f, earTopY + hr * 0.4f,
                        outerX + side * earW * 0.35f, earBottomY - hr * 0.05f
                    )
                    cubicTo(
                        outerX + side * earW * 0.2f, earBottomY + hr * 0.08f,
                        outerX - side * earW * 0.05f, earTopY + hr * 0.22f,
                        innerX + side * 3f, earTopY + hr * 0.08f
                    )
                    close()
                }, InnerEarPink.copy(alpha = 0.70f))
            }
        }

        PetSpecies.HAMSTER -> {
            val earR = hr * 0.24f
            val earY = hcy - hr * 0.88f
            for (side in intArrayOf(-1, 1)) {
                val ex = hcx + side * hr * 0.52f
                drawCircle(base, radius = earR, center = Offset(ex, earY))
                drawCircle(InnerEarPink.copy(alpha = 0.85f), radius = earR * 0.55f, center = Offset(ex, earY))
            }
        }

        else -> {}
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
    val earW = hr * 0.34f
    val topY = hcy - hr * 1.75f
    val bottomY = hcy - hr * 0.20f
    val midY = (topY + bottomY) / 2f

    for (side in intArrayOf(-1, 1)) {
        val ex = hcx + side * hr * 0.32f
        // Outer ear
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(lighter, base),
                center = Offset(ex, midY - earH * 0.12f),
                radius = earH * 0.85f
            ),
            topLeft = Offset(ex - earW / 2f, midY - earH / 2f),
            size = Size(earW, earH)
        )
        // Inner pink
        drawOval(
            InnerEarPink,
            topLeft = Offset(ex - earW * 0.22f, midY + earH * 0.08f - earH * 0.25f),
            size = Size(earW * 0.44f, earH * 0.50f)
        )
    }
}

// ============================================================
// SNOUT
// ============================================================
private fun DrawScope.drawCatSnout(a: PetAnchors) {
    drawPath(Path().apply {
        moveTo(a.headCx, a.noseY - a.noseR)
        lineTo(a.headCx - a.noseR * 1.15f, a.noseY + a.noseR * 0.85f)
        lineTo(a.headCx + a.noseR * 1.15f, a.noseY + a.noseR * 0.85f)
        close()
    }, NosePink)
    drawPath(Path().apply {
        moveTo(a.headCx - a.noseR * 1.15f, a.mouthY)
        cubicTo(a.headCx, a.mouthY + a.noseR * 0.55f, a.headCx, a.mouthY + a.noseR * 0.55f, a.headCx + a.noseR * 1.15f, a.mouthY)
    }, color = OutlineSoft, style = Stroke(width = 1.5f, cap = StrokeCap.Round))
    strokeWhiskers(a)
}

private fun DrawScope.drawDogSnout(a: PetAnchors) {
    drawCircle(EyeColor, radius = a.noseR * 1.15f, center = Offset(a.headCx, a.noseY))
    drawOval(
        NosePink.copy(alpha = 0.55f),
        topLeft = Offset(a.headCx - a.noseR * 1.30f, a.noseY + a.noseR * 0.80f),
        size = Size(a.noseR * 2.6f, a.noseR * 1.0f)
    )
    drawPath(Path().apply {
        moveTo(a.headCx - a.noseR * 1.50f, a.mouthY + a.noseR * 0.20f)
        cubicTo(a.headCx, a.mouthY + a.noseR * 0.95f, a.headCx, a.mouthY + a.noseR * 0.95f, a.headCx + a.noseR * 1.50f, a.mouthY + a.noseR * 0.20f)
    }, color = OutlineSoft, style = Stroke(width = 1.5f, cap = StrokeCap.Round))
}

private fun DrawScope.drawRabbitSnout(a: PetAnchors) {
    drawOval(
        NosePink,
        topLeft = Offset(a.headCx - a.noseR * 0.90f, a.noseY - a.noseR * 0.65f),
        size = Size(a.noseR * 1.8f, a.noseR * 1.3f)
    )
    drawPath(Path().apply {
        moveTo(a.headCx, a.noseY + a.noseR * 0.70f)
        lineTo(a.headCx, a.mouthY)
    }, color = OutlineSoft, style = Stroke(width = 1.5f, cap = StrokeCap.Round))
    drawPath(Path().apply {
        moveTo(a.headCx - a.noseR * 1.10f, a.mouthY)
        cubicTo(a.headCx, a.mouthY + a.noseR * 0.45f, a.headCx, a.mouthY + a.noseR * 0.45f, a.headCx + a.noseR * 1.10f, a.mouthY)
    }, color = OutlineSoft, style = Stroke(width = 1.5f, cap = StrokeCap.Round))
}

private fun DrawScope.drawHamsterSnout(a: PetAnchors) {
    drawCircle(NosePink, radius = a.noseR * 0.90f, center = Offset(a.headCx, a.noseY))
    drawPath(Path().apply {
        moveTo(a.headCx - a.noseR, a.mouthY)
        cubicTo(a.headCx, a.mouthY + a.noseR * 0.35f, a.headCx, a.mouthY + a.noseR * 0.35f, a.headCx + a.noseR, a.mouthY)
    }, color = OutlineSoft, style = Stroke(width = 1.4f, cap = StrokeCap.Round))
    strokeWhiskers(a)
}

private fun DrawScope.strokeWhiskers(a: PetAnchors) {
    drawLine(WhiskerColor,
        Offset(a.headCx - a.headR * 0.25f, a.noseY + a.noseR),
        Offset(a.headCx - a.headR * 0.80f, a.noseY + a.noseR * 0.2f), 1.0f)
    drawLine(WhiskerColor,
        Offset(a.headCx - a.headR * 0.25f, a.noseY + a.noseR * 1.5f),
        Offset(a.headCx - a.headR * 0.80f, a.noseY + a.noseR * 1.0f), 1.0f)
    drawLine(WhiskerColor,
        Offset(a.headCx + a.headR * 0.25f, a.noseY + a.noseR),
        Offset(a.headCx + a.headR * 0.80f, a.noseY + a.noseR * 0.2f), 1.0f)
    drawLine(WhiskerColor,
        Offset(a.headCx + a.headR * 0.25f, a.noseY + a.noseR * 1.5f),
        Offset(a.headCx + a.headR * 0.80f, a.noseY + a.noseR * 1.0f), 1.0f)
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