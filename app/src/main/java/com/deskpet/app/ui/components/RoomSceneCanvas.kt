// app/src/main/java/com/deskpet/app/ui/components/RoomSceneCanvas.kt
package com.deskpet.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import com.deskpet.app.data.model.FurnitureCategory
import com.deskpet.app.data.model.FurnitureItem
import com.deskpet.app.data.model.RoomLayout

/**
 * Renders the pet's room scene: wallpaper, floor, placed furniture, and pet.
 * The pet is drawn as an overlay by the caller (PetCanvas) on top of this.
 */
@Composable
fun RoomSceneCanvas(
    modifier: Modifier = Modifier,
    layout: List<RoomLayout>,
    furnitureCatalogue: List<FurnitureItem>,
    showDefaultBackground: Boolean = true
) {
    val furnitureMap = remember(furnitureCatalogue) {
        furnitureCatalogue.associateBy { it.id }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.3f)
    ) {
        val w = this.size.width
        val h = this.size.height

        // Draw placed furniture by slot index
        layout.sortedBy { it.slotIndex }.forEach { placed ->
            val furniture = furnitureMap[placed.furnitureId] ?: return@forEach
            val rendered = with(FurnitureRenderer) {
                this@Canvas.render(furniture.id, placed.slotIndex, w, h)
            }
            // Fallback to emoji if vector not available
            if (!rendered) {
                val paint = android.graphics.Paint().apply {
                    textSize = w * 0.08f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                val positions = mapOf(
                    0 to Pair(w * 0.5f, h * 0.3f),   // wallpaper center
                    1 to Pair(w * 0.5f, h * 0.8f),   // floor center
                    2 to Pair(w * 0.3f, h * 0.78f),  // bed
                    3 to Pair(w * 0.65f, h * 0.8f),  // table
                    4 to Pair(w * 0.85f, h * 0.78f), // decor 1
                    5 to Pair(w * 0.15f, h * 0.75f), // decor 2
                    6 to Pair(w * 0.75f, h * 0.85f), // toy 1
                    7 to Pair(w * 0.82f, h * 0.85f)  // toy 2
                )
                val (cx, cy) = positions[placed.slotIndex] ?: Pair(w * 0.5f, h * 0.5f)
                drawContext.canvas.nativeCanvas.drawText(furniture.emoji, cx, cy, paint)
            }
        }
    }
}
