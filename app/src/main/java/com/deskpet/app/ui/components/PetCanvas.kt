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
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// Local drawing colors
private val BlushColor = Color(0x99F4A7B9)
private val EyeColor = Color(0xFF2D2420)
private val HeartColor = Color(0xFFFF6B9D)
private val SnoutColor = Color(0x33FF8FAB)
private val WhiskerColor = Color(0x66555050)
private val InnerEarColor = Color(0xCCFFB3C8)

// Seed for wobble effect (can be made dynamic for animation)
private var wobbleSeed = Random(System.currentTimeMillis())

/**
 * Reusable pet character drawn entirely with Compose Canvas (no image assets).
 * Each [PetSpecies] gets a distinct silhouette so cats look like cats, dogs
 * look like dogs, etc.
 *
 * @param color        body color
 * @param species      drives the ear / body / snout shape
 * @param state        drives the mouth expression (and a happy bounce)
 * @param enableBreath toggles the idle breathing + blink animations
 * @param outfits      emoji map of worn items drawn on top
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

    // Breathing animation
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

    // Blink
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
        val w = size.width
        val h = size.height

        // ---- Species-specific ears + body ----
        when (species) {
            PetSpecies.CAT -> drawCat(w, h, bodyColor, darkerColor)
            PetSpecies.DOG -> drawDog(w, h, bodyColor, darkerColor)
            PetSpecies.RABBIT -> drawRabbit(w, h, bodyColor, darkerColor)
            PetSpecies.HAMSTER -> drawHamster(w, h, bodyColor, darkerColor)
        }

        // ---- Blush marks (watercolor style) ----
        drawWatercolorBlush(w * 0.28f, h * 0.50f, w * 0.06f, BlushColor)
        drawWatercolorBlush(w * 0.72f, h * 0.50f, w * 0.06f, BlushColor)

        // ---- Species-specific snout / whiskers ----
        when (species) {
            PetSpecies.CAT -> drawCatSnout(w, h)
            PetSpecies.DOG -> drawDogSnout(w, h)
            PetSpecies.RABBIT -> drawRabbitSnout(w, h)
            PetSpecies.HAMSTER -> drawHamsterSnout(w, h)
        }

        // ---- Eyes (larger pupils for cuteness) ----
        val eyeY = h * 0.38f
        if (blink || state == PetState.SLEEPY) {
            drawLine(Color.White, Offset(w * 0.27f, eyeY), Offset(w * 0.37f, eyeY), strokeWidth = 4f)
            drawLine(Color.White, Offset(w * 0.63f, eyeY), Offset(w * 0.73f, eyeY), strokeWidth = 4f)
        } else {
            // Eye whites
            drawCircle(Color.White, radius = w * 0.075f, center = Offset(w * 0.33f, eyeY))
            drawCircle(Color.White, radius = w * 0.075f, center = Offset(w * 0.67f, eyeY))
            // Pupils (26% larger: 0.038 -> 0.048)
            drawCircle(EyeColor, radius = w * 0.048f, center = Offset(w * 0.33f, eyeY))
            drawCircle(EyeColor, radius = w * 0.048f, center = Offset(w * 0.67f, eyeY))
            // Highlights
            drawCircle(Color.White, radius = w * 0.016f, center = Offset(w * 0.345f, eyeY - w * 0.018f))
            drawCircle(Color.White, radius = w * 0.016f, center = Offset(w * 0.685f, eyeY - w * 0.018f))
        }

        // ---- Mouth (shared, varies by state) ----
        val mouthPath = Path()
        when (state) {
            PetState.HAPPY, PetState.EXCITED -> {
                mouthPath.moveTo(w * 0.42f, h * 0.57f)
                mouthPath.quadraticBezierTo(w * 0.5f, h * 0.66f, w * 0.58f, h * 0.57f)
                mouthPath.quadraticBezierTo(w * 0.5f, h * 0.61f, w * 0.42f, h * 0.57f)
                drawPath(mouthPath, Color.White)
            }
            PetState.HUNGRY -> {
                drawLine(
                    Color.White,
                    Offset(w * 0.42f, h * 0.62f),
                    Offset(w * 0.58f, h * 0.60f),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }
            PetState.EATING -> {
                drawCircle(Color.White, radius = w * 0.028f, center = Offset(w * 0.5f, h * 0.59f))
            }
            PetState.SLEEPY -> {
                drawCircle(Color.White, radius = w * 0.018f, center = Offset(w * 0.5f, h * 0.60f))
            }
            else -> {
                drawLine(
                    Color.White,
                    Offset(w * 0.43f, h * 0.59f),
                    Offset(w * 0.57f, h * 0.59f),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }
        }

        // ---- Outfit overlays ----
        if (outfits.isNotEmpty()) {
            outfits.forEach { (category, outfitId) ->
                val rendered = with(OutfitRenderer) {
                    this@Canvas.render(outfitId, category, species, w, h)
                }
                if (!rendered) {
                    drawIntoCanvas { canvas ->
                        val paint = android.graphics.Paint().apply {
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                        }
                        val (cx, cy, sizeFactor) = when (category) {
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
                        paint.textSize = w * sizeFactor
                        val fm = paint.fontMetrics
                        val baseline = cy - (fm.ascent + fm.descent) / 2f
                        canvas.nativeCanvas.drawText(outfitId, cx, baseline, paint)
                    }
                }
            }
        }
    }
}

// ============================================================ CAT
private fun DrawScope.drawCat(w: Float, h: Float, body: Color, dark: Color) {
    // Pointy triangular ears
    drawPath(
        Path().apply {
            moveTo(w * 0.28f, h * 0.16f)
            lineTo(w * 0.18f, h * 0.0f)
            lineTo(w * 0.38f, h * 0.10f)
            close()
        },
        body
    )
    drawPath(
        Path().apply {
            moveTo(w * 0.72f, h * 0.16f)
            lineTo(w * 0.82f, h * 0.0f)
            lineTo(w * 0.62f, h * 0.10f)
            close()
        },
        body
    )
    // Inner ears
    drawPath(
        Path().apply {
            moveTo(w * 0.29f, h * 0.13f)
            lineTo(w * 0.24f, h * 0.04f)
            lineTo(w * 0.34f, h * 0.09f)
            close()
        },
        InnerEarColor
    )
    drawPath(
        Path().apply {
            moveTo(w * 0.71f, h * 0.13f)
            lineTo(w * 0.76f, h * 0.04f)
            lineTo(w * 0.66f, h * 0.09f)
            close()
        },
        InnerEarColor
    )
    // Body — rounded head-dominant (chibi cat)
    drawRoundRect(
        color = body,
        topLeft = Offset(w * 0.18f, h * 0.12f),
        size = Size(w * 0.64f, h * 0.78f),
        cornerRadius = CornerRadius(w * 0.32f, w * 0.32f)
    )
    // Paws (darker color for visibility against body)
    drawPaws(w, h, w * 0.18f, w * 0.82f, h * 0.12f, h * 0.90f, darkerColor)
}

private fun DrawScope.drawCatSnout(w: Float, h: Float) {
    // Tiny pink triangle nose
    drawPath(
        Path().apply {
            moveTo(w * 0.5f, h * 0.50f)
            lineTo(w * 0.47f, h * 0.53f)
            lineTo(w * 0.53f, h * 0.53f)
            close()
        },
        SnoutColor
    )
    // Whiskers
    drawLine(WhiskerColor, Offset(w * 0.18f, h * 0.52f), Offset(w * 0.34f, h * 0.53f), 2f)
    drawLine(WhiskerColor, Offset(w * 0.18f, h * 0.56f), Offset(w * 0.34f, h * 0.55f), 2f)
    drawLine(WhiskerColor, Offset(w * 0.82f, h * 0.52f), Offset(w * 0.66f, h * 0.53f), 2f)
    drawLine(WhiskerColor, Offset(w * 0.82f, h * 0.56f), Offset(w * 0.66f, h * 0.55f), 2f)
}

// ============================================================ DOG
private fun DrawScope.drawDog(w: Float, h: Float, body: Color, dark: Color) {
    // SHORT and NARROW floppy ears (shiba/corgi style)
    // Left ear - triangular with slight droop at tip
    drawPath(
        Path().apply {
            moveTo(w * 0.25f, h * 0.15f)  // Base on head
            lineTo(w * 0.15f, h * 0.05f)  // Tip top
            lineTo(w * 0.12f, h * 0.18f)  // Tip drooping down slightly
            close()
        },
        dark
    )
    // Right ear
    drawPath(
        Path().apply {
            moveTo(w * 0.75f, h * 0.15f)
            lineTo(w * 0.85f, h * 0.05f)
            lineTo(w * 0.88f, h * 0.18f)
            close()
        },
        dark
    )
    // Inner ears (pink tint)
    drawPath(
        Path().apply {
            moveTo(w * 0.24f, h * 0.14f)
            lineTo(w * 0.18f, h * 0.08f)
            lineTo(w * 0.16f, h * 0.15f)
            close()
        },
        InnerEarColor
    )
    drawPath(
        Path().apply {
            moveTo(w * 0.76f, h * 0.14f)
            lineTo(w * 0.82f, h * 0.08f)
            lineTo(w * 0.84f, h * 0.15f)
            close()
        },
        InnerEarColor
    )
    // Body — wider rounded
    drawRoundRect(
        color = body,
        topLeft = Offset(w * 0.20f, h * 0.14f),
        size = Size(w * 0.60f, h * 0.76f),
        cornerRadius = CornerRadius(w * 0.30f, w * 0.30f)
    )
    // Paws (darker color for visibility against body)
    drawPaws(w, h, w * 0.20f, w * 0.80f, h * 0.14f, h * 0.90f, darkerColor)
}

private fun DrawScope.drawDogSnout(w: Float, h: Float) {
    // Large round nose patch
    drawCircle(SnoutColor, radius = w * 0.09f, center = Offset(w * 0.5f, h * 0.50f))
    // Dark nose dot
    drawCircle(EyeColor, radius = w * 0.028f, center = Offset(w * 0.5f, h * 0.49f))
}

// ============================================================ RABBIT
private fun DrawScope.drawRabbit(w: Float, h: Float, body: Color, dark: Color) {
    // Long upright oval ears
    drawOval(
        color = body,
        topLeft = Offset(w * 0.32f, h * -0.08f),
        size = Size(w * 0.12f, h * 0.38f)
    )
    drawOval(
        color = body,
        topLeft = Offset(w * 0.56f, h * -0.08f),
        size = Size(w * 0.12f, h * 0.38f)
    )
    // Inner ears
    drawOval(
        color = InnerEarColor,
        topLeft = Offset(w * 0.35f, h * -0.04f),
        size = Size(w * 0.06f, h * 0.28f)
    )
    drawOval(
        color = InnerEarColor,
        topLeft = Offset(w * 0.59f, h * -0.04f),
        size = Size(w * 0.06f, h * 0.28f)
    )
    // Body — round head
    drawRoundRect(
        color = body,
        topLeft = Offset(w * 0.22f, h * 0.20f),
        size = Size(w * 0.56f, h * 0.70f),
        cornerRadius = CornerRadius(w * 0.28f, w * 0.28f)
    )
    // Paws (darker color for visibility against body)
    drawPaws(w, h, w * 0.22f, w * 0.78f, h * 0.20f, h * 0.90f, darkerColor)
}

private fun DrawScope.drawRabbitSnout(w: Float, h: Float) {
    // Small Y-shaped nose
    drawCircle(SnoutColor, radius = w * 0.03f, center = Offset(w * 0.5f, h * 0.50f))
    drawLine(WhiskerColor, Offset(w * 0.5f, h * 0.52f), Offset(w * 0.5f, h * 0.55f), 2f)
    drawLine(WhiskerColor, Offset(w * 0.5f, h * 0.55f), Offset(w * 0.44f, h * 0.58f), 2f)
    drawLine(WhiskerColor, Offset(w * 0.5f, h * 0.55f), Offset(w * 0.56f, h * 0.58f), 2f)
}

// ============================================================ HAMSTER
private fun DrawScope.drawHamster(w: Float, h: Float, body: Color, dark: Color) {
    // Small round ears on top
    drawCircle(body, radius = w * 0.08f, center = Offset(w * 0.32f, h * 0.14f))
    drawCircle(body, radius = w * 0.08f, center = Offset(w * 0.68f, h * 0.14f))
    drawCircle(InnerEarColor, radius = w * 0.04f, center = Offset(w * 0.32f, h * 0.14f))
    drawCircle(InnerEarColor, radius = w * 0.04f, center = Offset(w * 0.68f, h * 0.14f))
    // Body — plump round circle (hamsters are very round)
    drawCircle(body, radius = w * 0.36f, center = Offset(w * 0.5f, h * 0.55f))
    // Belly patch (lighter)
    drawOval(
        color = Color.White.copy(alpha = 0.25f),
        topLeft = Offset(w * 0.32f, h * 0.52f),
        size = Size(w * 0.36f, h * 0.30f)
    )
    // Paws (darker color for visibility against body)
    drawPaws(w, h, w * 0.30f, w * 0.70f, h * 0.19f, h * 0.86f, darkerColor)
}

private fun DrawScope.drawHamsterSnout(w: Float, h: Float) {
    // Tiny round nose
    drawCircle(SnoutColor, radius = w * 0.04f, center = Offset(w * 0.5f, h * 0.48f))
}

/**
 * Floating heart particles overlay. Spawns when the pet is happy.
 */
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
// HAND-DRAWN WATERCOLOR STYLE UTILITIES
// ============================================================

/**
 * Draw a wobbly (hand-drawn) path following the given path.
 * Adds small random offsets to simulate hand-drawn imperfection.
 */
private fun DrawScope.drawWobblyPath(
    path: Path,
    color: Color,
    wobbleAmount: Float = 1.5f
) {
    // Draw the main path with slight offset
    val wobblePath = Path()
    var firstPoint = true
    
    // Sample points along the path and add wobble
    val bounds = path.getBounds()
    val step = 2f // Sample every 2 pixels
    
    var x = bounds.left
    while (x <= bounds.right) {
        val y = if (firstPoint) bounds.top else bounds.top + (wobbleSeed.nextFloat() - 0.5f) * wobbleAmount
        if (firstPoint) {
            wobblePath.moveTo(x + (wobbleSeed.nextFloat() - 0.5f) * wobbleAmount,
                             y + (wobbleSeed.nextFloat() - 0.5f) * wobbleAmount)
            firstPoint = false
        } else {
            wobblePath.lineTo(x + (wobbleSeed.nextFloat() - 0.5f) * wobbleAmount,
                             y + (wobbleSeed.nextFloat() - 0.5f) * wobbleAmount)
        }
        x += step
    }
    
    drawPath(path, color) // Use original path for fill, wobble is visual hint
}

/**
 * Draw a wobbly oval (hand-drawn style).
 */
private fun DrawScope.drawWobblyOval(
    color: Color,
    topLeft: Offset,
    size: Size,
    wobbleAmount: Float = 1.0f
) {
    // Draw main oval
    drawOval(color, topLeft, size)
    
    // Draw slightly offset outline for hand-drawn effect (optional, can be skipped for performance)
    // For now, we keep the simple approach - just use the regular oval
}

/**
 * Draw a wobbly round rectangle (hand-drawn style).
 */
private fun DrawScope.drawWobblyRoundRect(
    color: Color,
    topLeft: Offset,
    size: Size,
    cornerRadius: CornerRadius,
    wobbleAmount: Float = 1.0f
) {
    // For simplicity, use regular round rect (wobble would require custom path)
    drawRoundRect(color, topLeft, size, cornerRadius)
}

/**
 * Draw watercolor-style blush (multiple overlapping transparent circles).
 */
private fun DrawScope.drawWatercolorBlush(
    centerX: Float,
    centerY: Float,
    baseRadius: Float,
    baseColor: Color
) {
    // Layer 1: Large, slightly transparent
    drawCircle(
        baseColor.copy(alpha = 0.30f),
        radius = baseRadius * 1.4f,
        center = Offset(centerX - baseRadius * 0.1f, centerY - baseRadius * 0.1f)
    )
    // Layer 2: Medium, semi-transparent
    drawCircle(
        baseColor.copy(alpha = 0.45f),
        radius = baseRadius,
        center = Offset(centerX + baseRadius * 0.15f, centerY + baseRadius * 0.05f)
    )
    // Layer 3: Small, more opaque
    drawCircle(
        baseColor.copy(alpha = 0.60f),
        radius = baseRadius * 0.55f,
        center = Offset(centerX - baseRadius * 0.05f, centerY)
    )
}

/**
 * Draw cute little paws/hands on the sides of the body.
 */
private fun DrawScope.drawPaws(
    w: Float,
    h: Float,
    bodyLeft: Float,
    bodyRight: Float,
    bodyTop: Float,
    bodyBottom: Float,
    pawColor: Color
) {
    val pawWidth = w * 0.06f
    val pawHeight = h * 0.08f
    val pawY = bodyBottom - pawHeight * 0.6f

    // Left paw at bottom-left corner
    drawOval(
        pawColor,
        topLeft = Offset(bodyLeft - pawWidth * 0.3f, pawY),
        size = Size(pawWidth, pawHeight)
    )
    // Right paw at bottom-right corner
    drawOval(
        pawColor,
        topLeft = Offset(bodyRight - pawWidth * 0.7f, pawY),
        size = Size(pawWidth, pawHeight)
    )
}
