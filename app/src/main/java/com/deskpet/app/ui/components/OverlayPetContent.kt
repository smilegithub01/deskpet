package com.deskpet.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deskpet.app.DeskPetApplication
import com.deskpet.app.data.model.PetState
import com.deskpet.app.data.model.equippedOutfitIds
import androidx.compose.ui.text.font.FontWeight

/**
 * Compose content rendered inside the [com.deskpet.app.service.PetOverlayService]
 * overlay window.
 *
 * Draws the pet via [PetCanvas] (color sourced from the live repository state)
 * and forwards drag / tap / long-press gestures back to the service.
 *
 * @param petState    current animation state
 * @param isVisible   whether the overlay is currently shown
 * @param isPaused    whether interactions are paused
 * @param onDrag      invoked with (dx, dy) drag deltas
 * @param onClick     invoked on a single tap
 * @param onLongPress invoked on a long press
 */
@Composable
fun OverlayPetContent(
    petState: PetState,
    isVisible: Boolean,
    isPaused: Boolean,
    onDrag: (Float, Float) -> Unit,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    // Pull the pet's color from the repository so the overlay matches the app.
    val pet by DeskPetApplication.get().repository.petState.collectAsStateWithLifecycle()
    val outfits = remember(pet) {
        pet.equippedOutfitIds(DeskPetApplication.get().repository.getOutfitItems())
    }

    val displayState = when {
        isPaused -> PetState.PAUSED
        !isVisible -> PetState.HIDDEN
        else -> petState
    }

    Box(
        modifier = Modifier
            .size(120.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x, dragAmount.y)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongPress() }
                )
            }
    ) {
        PetCanvas(
            modifier = Modifier.size(120.dp),
            color = pet.color,
            species = pet.species,
            state = displayState,
            enableBreath = !isPaused,
            outfits = outfits
        )

        // Pause indicator bubble.
        AnimatedVisibility(
            visible = isPaused,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xCC4A3F42))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "已暂停",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }
}
