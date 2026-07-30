package com.deskpet.app.ui.screens.codex

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deskpet.app.data.model.Achievement
import com.deskpet.app.data.model.AchievementCategory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CodexScreen(
    onBack: () -> Unit,
    viewModel: CodexViewModel = viewModel()
) {
    val unlockedRecords by viewModel.unlockedRecords.collectAsStateWithLifecycle()
    val newlyUnlocked by viewModel.newlyUnlocked.collectAsStateWithLifecycle()

    val unlockedIds = remember(unlockedRecords) {
        unlockedRecords.map { it.achievementId }.toSet()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "返回", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text(
                text = "成就图鉴",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${unlockedIds.size}/${viewModel.allAchievements.size}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LazyColumn(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, end = 16.dp, bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Group by category
            AchievementCategory.entries.forEach { category ->
                item {
                    Text(
                        text = category.displayName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(
                    viewModel.allAchievements.filter { it.category == category },
                    key = { it.id }
                ) { achievement ->
                    AchievementCard(
                        achievement = achievement,
                        unlocked = achievement.id in unlockedIds
                    )
                }
            }
        }
    }

    // Show newly unlocked achievement dialog
    if (newlyUnlocked.isNotEmpty()) {
        val achievement = newlyUnlocked.first()
        AlertDialog(
            onDismissRequest = { viewModel.clearNewlyUnlocked() },
            title = { Text("🎉 成就解锁!") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = achievement.emoji, fontSize = 48.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(text = achievement.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = achievement.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(text = "奖励 💎 x${achievement.rewardDiamonds}", fontSize = 14.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.clearNewlyUnlocked() }) {
                    Text("太棒了!")
                }
            }
        )
    }
}

@Composable
private fun AchievementCard(achievement: Achievement, unlocked: Boolean) {
    val alpha = if (unlocked) 1f else 0.4f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (unlocked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (unlocked) achievement.emoji else "❓",
            fontSize = 28.sp,
            modifier = Modifier.graphicsLayerAlpha(alpha)
        )
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = achievement.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (unlocked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = achievement.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (unlocked) {
            Text(
                text = "💎${achievement.rewardDiamonds}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// Helper for alpha modifier
@Composable
private fun Modifier.graphicsLayerAlpha(alpha: Float): Modifier =
    this.then(Modifier.graphicsLayer { this.alpha = alpha })
