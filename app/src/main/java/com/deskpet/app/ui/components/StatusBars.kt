package com.deskpet.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deskpet.app.data.model.Pet

/**
 * The three primary status bars (饱腹度 / 心情值 / 亲密度) shown on the Home screen.
 *
 * Each row renders a label, an animated percentage value and a rounded progress
 * bar colored from the brand palette (pink / lavender / mint).
 */
@Composable
fun StatusBars(
    pet: Pet,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        StatusBarRow(
            label = "饱腹度",
            value = pet.hunger,
            barColor = PinkBar
        )
        Spacer(Modifier.height(10.dp))
        StatusBarRow(
            label = "心情值",
            value = pet.mood,
            barColor = LavenderBar
        )
        Spacer(Modifier.height(10.dp))
        StatusBarRow(
            label = "亲密度",
            value = pet.intimacy,
            barColor = MintBar
        )
    }
}

/**
 * A single status row: label on the left, percentage on the right, animated bar below.
 */
@Composable
fun StatusBarRow(
    label: String,
    value: Int,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    val clamped = value.coerceIn(0, 100)
    val animatedFraction by animateFloatAsState(
        targetValue = clamped / 100f,
        animationSpec = tween(durationMillis = 600),
        label = "statusBar_$label"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$clamped%",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = barColor
            )
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(TrackColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedFraction)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(barColor)
            )
        }
    }
}

// Bar colors drawn from the brand palette defined in Color.kt.
private val PinkBar = Color(0xFFFF8FAB)
private val LavenderBar = Color(0xFFC8B6FF)
private val MintBar = Color(0xFF7FD9B0)
private val TrackColor = Color(0xFFF0E0E5)
