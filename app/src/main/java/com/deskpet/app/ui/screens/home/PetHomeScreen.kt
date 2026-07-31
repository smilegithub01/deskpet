package com.deskpet.app.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deskpet.app.data.model.Achievement
import com.deskpet.app.data.model.MoodLevel
import com.deskpet.app.data.model.OutfitCategory
import com.deskpet.app.data.model.PetState
import com.deskpet.app.data.model.equippedOutfitIds
import com.deskpet.app.ui.components.HeartParticles
import com.deskpet.app.ui.components.PetCanvas
import com.deskpet.app.ui.components.StatusBars
import com.deskpet.app.DeskPetApplication

/**
 * Pet Home screen.
 *
 * Top bar (name/level/mood/diamonds) → interactive pet stage with speech
 * bubble + heart particles → status bars → mood selector → action row.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetHomeScreen(
    onNavigateToDressUp: () -> Unit,
    onNavigateToDiary: () -> Unit,
    onNavigateToDecor: () -> Unit,
    onNavigateToTravel: () -> Unit,
    onNavigateToCodex: () -> Unit,
    onNavigateToCompanion: () -> Unit,
    viewModel: PetViewModel = viewModel()
) {
    val pet by viewModel.pet.collectAsStateWithLifecycle()
    val petState by viewModel.petState.collectAsStateWithLifecycle()
    val showFoodSheet by viewModel.showFoodSheet.collectAsStateWithLifecycle()
    val toast by viewModel.toast.collectAsStateWithLifecycle()
    val showHearts by viewModel.showHearts.collectAsStateWithLifecycle()
    val speechBubble by viewModel.speechBubble.collectAsStateWithLifecycle()
    val flash by viewModel.flash.collectAsStateWithLifecycle()
    val newlyUnlockedAchievements by viewModel.newlyUnlockedAchievements.collectAsStateWithLifecycle()
    val showGoldenHeart by viewModel.showGoldenHeart.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Show toast text as a snackbar.
    LaunchedEffect(toast) {
        toast?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onToastShown()
        }
    }

    val sheetState = rememberModalBottomSheetState()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp)
        ) {
            // ---- Top bar: name + level + mood text + diamonds ----
            HomeTopBar(
                name = pet.name,
                level = pet.level,
                moodText = moodDescription(petState),
                diamonds = pet.diamonds,
                onNavigateToDiary = onNavigateToDiary
            )

            Spacer(Modifier.height(12.dp))

            // ---- Pet stage ----
            // Use individual equip fields as remember keys so the wardrobe
            // overlay updates as soon as the user equips / unequips items.
            val stageOutfits = remember(
                pet.equippedHead, pet.equippedGlasses, pet.equippedCollar,
                pet.equippedClothing, pet.equippedTail, pet.equippedAccessory
            ) {
                pet.equippedOutfitIds(DeskPetApplication.get().repository.getOutfitItems())
            }
            Box(contentAlignment = Alignment.TopCenter) {
                PetStage(
                    petColor = pet.color,
                    petSpecies = pet.species,
                    petState = petState,
                    speechBubble = speechBubble,
                    showHearts = showHearts,
                    outfits = stageOutfits,
                    onClick = { viewModel.onPetClicked() }
                )
                // Golden heart overlay when all habits are checked in today.
                androidx.compose.animation.AnimatedVisibility(
                    visible = showGoldenHeart,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    val pulse by animateFloatAsState(
                        targetValue = if (showGoldenHeart) 1.2f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "goldenPulse"
                    )
                    Text(
                        text = "💛",
                        fontSize = 40.sp,
                        modifier = Modifier
                            .graphicsLayer(scaleX = pulse, scaleY = pulse)
                            .padding(top = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---- Status bars ----
            StatusBars(pet = pet)

            Spacer(Modifier.height(12.dp))

            // ---- Quick access: 小窝 / 旅行 / 图鉴 / 伙伴 ----
            QuickAccessRow(
                onDecor = onNavigateToDecor,
                onTravel = onNavigateToTravel,
                onCodex = onNavigateToCodex,
                onCompanion = onNavigateToCompanion
            )

            Spacer(Modifier.height(12.dp))

            // ---- Mood selector ----
            MoodSelector { viewModel.onMoodSelected(it) }

            Spacer(Modifier.height(16.dp))

            // ---- Action buttons ----
            ActionRow(
                onPet = viewModel::onPet,
                onFeed = viewModel::onOpenFoodSheet,
                onDressUp = onNavigateToDressUp,
                onPhoto = viewModel::onPhoto,
                onShare = viewModel::onShareDailyStatus
            )
        }

        // Snackbar host at the bottom (above nav bar handled by scaffold in NavGraph).
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )

        // White flash overlay for photo action.
        AnimatedVisibility(
            visible = flash,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(Modifier.fillMaxSize().background(Color.White))
        }

        // Achievement celebration popup.
        if (newlyUnlockedAchievements.isNotEmpty()) {
            AchievementCelebrationDialog(
                achievements = newlyUnlockedAchievements,
                onDismiss = viewModel::onAchievementsCelebrated
            )
        }
    }

    // ---- Food bottom sheet ----
    if (showFoodSheet) {
        ModalBottomSheet(
            onDismissRequest = viewModel::onDismissFoodSheet,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "选择食物",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(12.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(220.dp)
                ) {
                    items(foodOptions, key = { it.name }) { food ->
                        FoodCard(food = food) { viewModel.onFeed(food) }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ----------------------------------------------------------- Sub-components

@Composable
private fun HomeTopBar(
    name: String,
    level: Int,
    moodText: String,
    diamonds: Int,
    onNavigateToDiary: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.width(8.dp))
                LevelBadge(level = level)
            }
            Text(
                text = moodText,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Diary entry button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                    .clickable { onNavigateToDiary() },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "💌", fontSize = 16.sp)
            }
            Spacer(Modifier.width(8.dp))
            DiamondsChip(diamonds = diamonds)
        }
    }
}

@Composable
private fun LevelBadge(level: Int) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = "Lv.$level",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun DiamondsChip(diamonds: Int) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "💎", fontSize = 16.sp)
        Spacer(Modifier.width(4.dp))
        Text(
            text = diamonds.toString(),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PetStage(
    petColor: com.deskpet.app.data.model.PetColor,
    petSpecies: com.deskpet.app.data.model.PetSpecies,
    petState: PetState,
    speechBubble: String?,
    showHearts: Boolean,
    outfits: Map<OutfitCategory, String>,
    onClick: () -> Unit
) {
    // Bounce on happy/excited.
    val bounce by animateFloatAsState(
        targetValue = if (petState == PetState.HAPPY || petState == PetState.EXCITED) -12f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bounce"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.3f),
        contentAlignment = Alignment.Center
    ) {
        // Gradient circle background.
        Box(
            modifier = Modifier
                .size(220.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.tertiaryContainer
                        )
                    )
                )
        )

        // Heart particles layer.
        HeartParticles(
            modifier = Modifier.size(220.dp),
            active = showHearts
        )

        // Pet drawing.
        PetCanvas(
            modifier = Modifier
                .size(160.dp)
                .graphicsLayerY(bounce),
            color = petColor,
            species = petSpecies,
            state = petState,
            outfits = outfits
        )

        // Speech bubble.
        AnimatedVisibility(
            visible = speechBubble != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 4.dp)
        ) {
            speechBubble?.let {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = it,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Click target covering the whole stage.
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
        )
    }
}

/** Applies a vertical translation via graphicsLayer. */
private fun Modifier.graphicsLayerY(ty: Float): Modifier =
    this.graphicsLayer { translationY = ty }

@Composable
private fun MoodSelector(onMoodSelected: (MoodLevel) -> Unit) {
    Column {
        Text(
            text = "今天的心情",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MoodLevel.entries.forEach { mood ->
                MoodButton(mood = mood) { onMoodSelected(mood) }
            }
        }
    }
}

@Composable
private fun MoodButton(mood: MoodLevel, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(text = mood.emoji, fontSize = 22.sp)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = mood.displayName,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ActionRow(
    onPet: () -> Unit,
    onFeed: () -> Unit,
    onDressUp: () -> Unit,
    onPhoto: () -> Unit,
    onShare: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ActionButton("抚摸", Icons.Filled.Pets, onPet)
        ActionButton("喂食", Icons.Filled.Fastfood, onFeed)
        ActionButton("装扮", Icons.Filled.Checkroom, onDressUp)
        ActionButton("拍照", Icons.Filled.CameraAlt, onPhoto)
        ActionButton("分享", Icons.Filled.Share, onShare)
    }
}

@Composable
private fun ActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .shadow(2.dp, CircleShape)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun FoodCard(food: FoodOption, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = food.emoji, fontSize = 36.sp)
        Spacer(Modifier.height(6.dp))
        Text(
            text = food.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = "饱腹 +${food.hungerGain}",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ----------------------------------------------------------- Helpers

@Composable
private fun QuickAccessRow(
    onDecor: () -> Unit,
    onTravel: () -> Unit,
    onCodex: () -> Unit,
    onCompanion: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        QuickAccessButton(emoji = "🏠", label = "小窝", onClick = onDecor)
        QuickAccessButton(emoji = "✈️", label = "旅行", onClick = onTravel)
        QuickAccessButton(emoji = "📖", label = "图鉴", onClick = onCodex)
        QuickAccessButton(emoji = "🤝", label = "伙伴", onClick = onCompanion)
    }
}

@Composable
private fun QuickAccessButton(
    emoji: String,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 22.sp)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun moodDescription(state: PetState): String = when (state) {
    PetState.HAPPY, PetState.EXCITED -> "心情超好～"
    PetState.EATING -> "正在吃饭～"
    PetState.HUNGRY -> "肚子饿了…"
    PetState.SLEEPY -> "困了…"
    PetState.COMFORTING -> "很安心～"
    PetState.PLAYING -> "玩耍中～"
    PetState.HIDDEN -> "躲起来啦"
    PetState.PAUSED -> "休息中"
    PetState.IDLE -> "乖巧地等着你"
}

/**
 * Celebration popup shown when one or more achievements are newly unlocked.
 *
 * Shows the achievement emoji, title, description and diamond reward, with a
 * single "太棒了" dismiss button. The achievement fanfare is played by
 * [PetViewModel.checkAchievementsAfterInteraction] when the unlock is detected.
 */
@Composable
private fun AchievementCelebrationDialog(
    achievements: List<Achievement>,
    onDismiss: () -> Unit
) {
    val current = achievements.first()
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎉 成就解锁",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = current.emoji,
                fontSize = 56.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = current.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = current.description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "奖励 💎 x${current.rewardDiamonds}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.SemiBold
            )
            if (achievements.size > 1) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "还有 ${achievements.size - 1} 个新成就",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("太棒了！")
            }
        }
    }
}
