// app/src/main/java/com/deskpet/app/ui/components/FurnitureRenderer.kt
package com.deskpet.app.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Renders furniture items as vector graphics on a room canvas.
 * Each function returns true if rendered, false to fall back to emoji.
 */
object FurnitureRenderer {

    /**
     * Renders a furniture item by its ID. Returns true if vector-rendered.
     */
    fun DrawScope.render(furnitureId: String, slotIndex: Int, w: Float, h: Float): Boolean {
        return when (furnitureId) {
            // Wallpaper
            "wall_pink" -> { drawWallpaper(Color(0xFFFFE0E0), Color(0xFFFFC0CB), w, h); true }
            "wall_mint" -> { drawWallpaper(Color(0xFFE0F5E0), Color(0xFFB0E0B0), w, h); true }
            "wall_sky" -> { drawWallpaper(Color(0xFFE0ECF5), Color(0xFFB0CCE0), w, h); true }
            // Floor
            "floor_wood" -> { drawFloor(Color(0xFFD2B48C), Color(0xFFC19A6B), w, h); true }
            "floor_tile" -> { drawFloor(Color(0xFFF0F0F0), Color(0xFFD0D0D0), w, h); true }
            "floor_carpet" -> { drawFloor(Color(0xFFE8C4D4), Color(0xFFD4A4C0), w, h); true }
            // Bed
            "bed_round" -> { drawRoundBed(w, h); true }
            "bed_cushion" -> { drawCushionBed(w, h); true }
            "bed_basket" -> { drawBasketBed(w, h); true }
            "bed_canopy" -> { drawCanopyBed(w, h); true }
            // Table
            "table_small" -> { drawSmallTable(w, h); true }
            "table_round" -> { drawRoundTable(w, h); true }
            "table_desk" -> { drawDesk(w, h); true }
            "table_shelf" -> { drawShelf(w, h); true }
            // Decoration
            "decor_plant" -> { drawPlant(w, h); true }
            "decor_lamp" -> { drawLamp(w, h); true }
            "decor_frame" -> { drawFrame(w, h); true }
            "decor_mirror" -> { drawMirror(w, h); true }
            "decor_clock" -> { drawClock(w, h); true }
            "decor_vase" -> { drawVase(w, h); true }
            // Toy
            "toy_ball" -> { drawBall(w, h); true }
            "toy_yarn" -> { drawYarn(w, h); true }
            "toy_mouse" -> { drawToyMouse(w, h); true }
            "toy_feather" -> { drawFeather(w, h); true }
            else -> false
        }
    }

    // --- Wallpaper ---
    private fun DrawScope.drawWallpaper(c1: Color, c2: Color, w: Float, h: Float) {
        val wallH = h * 0.6f
        drawRect(c1, topLeft = Offset(0f, 0f), size = Size(w, wallH))
        // Polka dot pattern
        val dotColor = c2.copy(alpha = 0.5f)
        val spacing = w / 8f
        var row = 0
        var y = spacing * 0.5f
        while (y < wallH) {
            var x = spacing * 0.5f + (if (row % 2 == 0) 0f else spacing * 0.5f)
            while (x < w) {
                drawCircle(dotColor, radius = spacing * 0.12f, center = Offset(x, y))
                x += spacing
            }
            row++
            y += spacing
        }
    }

    // --- Floor ---
    private fun DrawScope.drawFloor(c1: Color, c2: Color, w: Float, h: Float) {
        val floorY = h * 0.6f
        drawRect(c1, topLeft = Offset(0f, floorY), size = Size(w, h - floorY))
        // Plank lines
        val lineColor = c2.copy(alpha = 0.6f)
        val plankH = (h - floorY) / 4f
        for (i in 1 until 4) {
            val y = floorY + plankH * i
            drawLine(lineColor, Offset(0f, y), Offset(w, y), strokeWidth = 2f)
        }
    }

    // --- Bed ---
    private fun DrawScope.drawRoundBed(w: Float, h: Float) {
        val cx = w * 0.3f
        val cy = h * 0.78f
        val r = w * 0.12f
        drawOval(
            color = Color(0xFFE8C4D4),
            topLeft = Offset(cx - r, cy - r * 0.6f),
            size = Size(r * 2, r * 1.2f)
        )
        drawOval(
            color = Color(0xFFFFF0F5),
            topLeft = Offset(cx - r * 0.7f, cy - r * 0.4f),
            size = Size(r * 1.4f, r * 0.8f)
        )
    }

    private fun DrawScope.drawCushionBed(w: Float, h: Float) {
        val x = w * 0.2f
        val y = h * 0.75f
        val bw = w * 0.2f
        val bh = h * 0.06f
        drawRoundRect(
            color = Color(0xFFFFB6C1),
            topLeft = Offset(x, y),
            size = Size(bw, bh),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(bh * 0.5f, bh * 0.5f)
        )
        drawRoundRect(
            color = Color(0xFFFFE4E1),
            topLeft = Offset(x + bw * 0.1f, y + bh * 0.2f),
            size = Size(bw * 0.8f, bh * 0.6f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(bh * 0.3f, bh * 0.3f)
        )
    }

    private fun DrawScope.drawBasketBed(w: Float, h: Float) {
        val cx = w * 0.3f
        val cy = h * 0.78f
        val bw = w * 0.18f
        val bh = h * 0.08f
        drawOval(
            color = Color(0xFFC19A6B),
            topLeft = Offset(cx - bw * 0.5f, cy - bh * 0.5f),
            size = Size(bw, bh)
        )
        drawOval(
            color = Color(0xFFD2B48C),
            topLeft = Offset(cx - bw * 0.4f, cy - bh * 0.35f),
            size = Size(bw * 0.8f, bh * 0.7f)
        )
    }

    private fun DrawScope.drawCanopyBed(w: Float, h: Float) {
        val x = w * 0.22f
        val y = h * 0.7f
        val bw = w * 0.16f
        // Canopy top
        drawArc(
            color = Color(0xFFE6B8D4),
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(x, y - h * 0.05f),
            size = Size(bw, h * 0.1f)
        )
        // Bed base
        drawRoundRect(
            color = Color(0xFFFFC0CB),
            topLeft = Offset(x, y),
            size = Size(bw, h * 0.05f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
        )
    }

    // --- Table ---
    private fun DrawScope.drawSmallTable(w: Float, h: Float) {
        val x = w * 0.6f
        val y = h * 0.78f
        val tw = w * 0.1f
        val th = h * 0.04f
        drawRoundRect(
            color = Color(0xFF8B4513),
            topLeft = Offset(x, y),
            size = Size(tw, th),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )
        // Legs
        drawLine(Color(0xFF8B4513), Offset(x + 2f, y + th), Offset(x + 2f, y + th + h * 0.06f), 3f)
        drawLine(Color(0xFF8B4513), Offset(x + tw - 2f, y + th), Offset(x + tw - 2f, y + th + h * 0.06f), 3f)
    }

    private fun DrawScope.drawRoundTable(w: Float, h: Float) {
        val cx = w * 0.65f
        val cy = h * 0.8f
        val r = w * 0.06f
        drawOval(
            color = Color(0xFFD2B48C),
            topLeft = Offset(cx - r, cy - r * 0.3f),
            size = Size(r * 2, r * 0.6f)
        )
        drawLine(Color(0xFF8B4513), Offset(cx, cy + r * 0.3f), Offset(cx, cy + r * 0.3f + h * 0.05f), 3f)
    }

    private fun DrawScope.drawDesk(w: Float, h: Float) {
        val x = w * 0.58f
        val y = h * 0.76f
        val dw = w * 0.14f
        val dh = h * 0.03f
        drawRect(Color(0xFF6B4226), Offset(x, y), Size(dw, dh))
        drawRect(Color(0xFF6B4226), Offset(x, y + dh), Size(w * 0.02f, h * 0.08f))
        drawRect(Color(0xFF6B4226), Offset(x + dw - w * 0.02f, y + dh), Size(w * 0.02f, h * 0.08f))
    }

    private fun DrawScope.drawShelf(w: Float, h: Float) {
        val x = w * 0.6f
        val y = h * 0.68f
        val sw = w * 0.12f
        drawRect(Color(0xFF8B4513), Offset(x, y), Size(sw, h * 0.02f))
        drawRect(Color(0xFF8B4513), Offset(x, y + h * 0.04f), Size(sw, h * 0.02f))
    }

    // --- Decoration ---
    private fun DrawScope.drawPlant(w: Float, h: Float) {
        val cx = w * 0.85f
        val cy = h * 0.78f
        // Pot
        drawRoundRect(
            color = Color(0xFF8B4513),
            Offset(cx - w * 0.03f, cy),
            Size(w * 0.06f, h * 0.05f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )
        // Leaves
        drawCircle(Color(0xFF4CAF50), w * 0.035f, Offset(cx - w * 0.02f, cy - h * 0.02f))
        drawCircle(Color(0xFF66BB6A), w * 0.03f, Offset(cx + w * 0.01f, cy - h * 0.03f))
        drawCircle(Color(0xFF81C784), w * 0.025f, Offset(cx, cy - h * 0.04f))
    }

    private fun DrawScope.drawLamp(w: Float, h: Float) {
        val cx = w * 0.15f
        val cy = h * 0.72f
        // Shade
        val path = Path().apply {
            moveTo(cx - w * 0.03f, cy)
            lineTo(cx + w * 0.03f, cy)
            lineTo(cx + w * 0.02f, cy - h * 0.04f)
            lineTo(cx - w * 0.02f, cy - h * 0.04f)
            close()
        }
        drawPath(path, Color(0xFFFFF9C4))
        // Pole
        drawLine(Color(0xFF888888), Offset(cx, cy), Offset(cx, cy + h * 0.06f), 3f)
        // Base
        drawRoundRect(
            color = Color(0xFF888888),
            Offset(cx - w * 0.025f, cy + h * 0.06f),
            Size(w * 0.05f, h * 0.01f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
        )
    }

    private fun DrawScope.drawFrame(w: Float, h: Float) {
        val x = w * 0.08f
        val y = h * 0.18f
        val fw = w * 0.08f
        val fh = h * 0.1f
        drawRect(Color(0xFFD4A4C0), Offset(x, y), Size(fw, fh))
        drawRect(Color(0xFFFFFFFF), Offset(x + w * 0.008f, y + h * 0.01f), Size(fw - w * 0.016f, fh - h * 0.02f))
    }

    private fun DrawScope.drawMirror(w: Float, h: Float) {
        val cx = w * 0.12f
        val cy = h * 0.22f
        val r = w * 0.04f
        drawCircle(Color(0xFFB0C4DE), r, Offset(cx, cy))
        drawCircle(Color(0xFFE0E0E0), r * 0.8f, Offset(cx, cy))
    }

    private fun DrawScope.drawClock(w: Float, h: Float) {
        val cx = w * 0.88f
        val cy = h * 0.15f
        val r = w * 0.03f
        drawCircle(Color(0xFF8B4513), r, Offset(cx, cy))
        drawCircle(Color(0xFFFFF8DC), r * 0.8f, Offset(cx, cy))
        drawLine(Color(0xFF333333), Offset(cx, cy), Offset(cx, cy - r * 0.6f), 2f)
        drawLine(Color(0xFF333333), Offset(cx, cy), Offset(cx + r * 0.5f, cy), 2f)
    }

    private fun DrawScope.drawVase(w: Float, h: Float) {
        val cx = w * 0.5f
        val cy = h * 0.82f
        val path = Path().apply {
            moveTo(cx - w * 0.02f, cy)
            cubicTo(cx - w * 0.04f, cy + h * 0.03f, cx - w * 0.03f, cy + h * 0.05f, cx, cy + h * 0.05f)
            cubicTo(cx + w * 0.03f, cy + h * 0.05f, cx + w * 0.04f, cy + h * 0.03f, cx + w * 0.02f, cy)
            close()
        }
        drawPath(path, Color(0xFF64B5F6))
    }

    // --- Toy ---
    private fun DrawScope.drawBall(w: Float, h: Float) {
        val cx = w * 0.75f
        val cy = h * 0.85f
        val r = w * 0.025f
        drawCircle(Color(0xFFE53935), r, Offset(cx, cy))
        drawLine(Color(0xFFFFFFFF), Offset(cx - r, cy), Offset(cx + r, cy), 2f)
        drawLine(Color(0xFFFFFFFF), Offset(cx, cy - r), Offset(cx, cy + r), 2f)
    }

    private fun DrawScope.drawYarn(w: Float, h: Float) {
        val cx = w * 0.8f
        val cy = h * 0.85f
        val r = w * 0.03f
        drawCircle(Color(0xFFAB47BC), r, Offset(cx, cy))
        // Yarn lines
        for (i in 0..5) {
            val angle = i * 60f
            val rad = Math.toRadians(angle.toDouble())
            val x1 = cx + (r * Math.cos(rad)).toFloat()
            val y1 = cy + (r * Math.sin(rad)).toFloat()
            val x2 = cx + (r * Math.cos(rad + Math.PI)).toFloat()
            val y2 = cy + (r * Math.sin(rad + Math.PI)).toFloat()
            drawLine(Color(0xFFCE93D8), Offset(x1, y1), Offset(x2, y2), 1.5f)
        }
    }

    private fun DrawScope.drawToyMouse(w: Float, h: Float) {
        val cx = w * 0.78f
        val cy = h * 0.87f
        val bw = w * 0.025f
        drawOval(Color(0xFF9E9E9E), Offset(cx - bw, cy - bw * 0.6f), Size(bw * 2, bw * 1.2f))
        // Ears
        drawCircle(Color(0xFF9E9E9E), bw * 0.4f, Offset(cx - bw * 0.6f, cy - bw * 0.5f))
        drawCircle(Color(0xFF9E9E9E), bw * 0.4f, Offset(cx + bw * 0.6f, cy - bw * 0.5f))
        // Tail
        drawLine(Color(0xFF9E9E9E), Offset(cx + bw, cy), Offset(cx + bw * 2f, cy + bw), 2f)
    }

    private fun DrawScope.drawFeather(w: Float, h: Float) {
        val cx = w * 0.82f
        val cy = h * 0.83f
        val path = Path().apply {
            moveTo(cx, cy + h * 0.04f)
            cubicTo(cx - w * 0.03f, cy, cx - w * 0.02f, cy - h * 0.04f, cx, cy - h * 0.05f)
            cubicTo(cx + w * 0.02f, cy - h * 0.04f, cx + w * 0.03f, cy, cx, cy + h * 0.04f)
            close()
        }
        drawPath(path, Color(0xFFEF5350))
        drawLine(Color(0xFF8B4513), Offset(cx, cy + h * 0.04f), Offset(cx, cy + h * 0.08f), 2f)
    }
}
