package com.deskpet.app.ui.screens.onboarding

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deskpet.app.DeskPetApplication
import com.deskpet.app.data.model.PersonalityTag
import com.deskpet.app.data.model.PetColor
import com.deskpet.app.data.model.PetSpecies
import com.deskpet.app.data.model.PetState
import com.deskpet.app.ui.components.PetCanvas

/**
 * 3-step onboarding: choose species → choose color → name + personality.
 *
 * On completion the chosen attributes are written to the repository via
 * [com.deskpet.app.data.repository.PetRepository.updatePet], the overlay
 * permission is requested, and [onComplete] navigates to Home.
 */
@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val repository = remember { DeskPetApplication.get().repository }
    val context = LocalContext.current

    var step by remember { mutableIntStateOf(1) }
    var species by remember { mutableStateOf(PetSpecies.CAT) }
    var color by remember { mutableStateOf(PetColor.PINK) }
    var name by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf(setOf<PersonalityTag>()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Step indicator
            StepIndicator(step = step)

            Spacer(Modifier.height(20.dp))

            // Live preview
            OnboardingPreview(color = color, species = species)

            Spacer(Modifier.height(24.dp))

            // Step content
            when (step) {
                1 -> SpeciesStep(
                    selected = species,
                    onSelect = { species = it }
                )
                2 -> ColorStep(
                    selected = color,
                    onSelect = { color = it }
                )
                3 -> NamePersonalityStep(
                    name = name,
                    onNameChange = { name = it },
                    tags = tags,
                    onTagToggle = { tag ->
                        tags = if (tags.contains(tag)) tags - tag else tags + tag
                    }
                )
            }

            Spacer(Modifier.weight(1f))

            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (step > 1) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { step-- }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "上一步",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable {
                            if (step < 3) {
                                step++
                            } else {
                                finishOnboarding(
                                    repository = repository,
                                    name = name.ifBlank { "小团子" },
                                    species = species,
                                    color = color,
                                    tags = tags.toList(),
                                    context = context
                                )
                                onComplete()
                            }
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (step < 3) "下一步" else "完成",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

// ----------------------------------------------------------- Step indicator

@Composable
private fun StepIndicator(step: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(3) { index ->
            val active = index < step
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(if (active) 24.dp else 12.dp)
                    .clip(CircleShape)
                    .background(
                        if (active) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
            )
        }
    }
}

// ----------------------------------------------------------- Live preview

@Composable
private fun OnboardingPreview(color: PetColor, species: PetSpecies) {
    Box(
        modifier = Modifier
            .size(160.dp)
            .shadow(8.dp, CircleShape)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.tertiaryContainer
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        PetCanvas(
            modifier = Modifier.size(120.dp),
            color = color,
            species = species,
            state = PetState.HAPPY
        )
    }
}

// ----------------------------------------------------------- Step 1: species

@Composable
private fun SpeciesStep(
    selected: PetSpecies,
    onSelect: (PetSpecies) -> Unit
) {
    Text(
        text = "选择你的宠物",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(Modifier.height(8.dp))
    Text(
        text = "选一个陪伴你的小伙伴吧",
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(20.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        PetSpecies.entries.forEach { s ->
            val isSelected = s == selected
            val defaultColor = when (s) {
                PetSpecies.CAT -> PetColor.PINK
                PetSpecies.DOG -> PetColor.PEACH
                PetSpecies.RABBIT -> PetColor.BLUE
                PetSpecies.HAMSTER -> PetColor.MINT
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onSelect(s) }
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            width = if (isSelected) 3.dp else 0.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    PetCanvas(
                        modifier = Modifier.size(64.dp),
                        color = defaultColor,
                        species = s,
                        state = PetState.IDLE,
                        enableBreath = false
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = s.displayName,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ----------------------------------------------------------- Step 2: color

@Composable
private fun ColorStep(
    selected: PetColor,
    onSelect: (PetColor) -> Unit
) {
    Text(
        text = "选择颜色",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(Modifier.height(8.dp))
    Text(
        text = "给你的宠物挑一个喜欢的颜色",
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(20.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        PetColor.entries.forEach { c ->
            val isSelected = c == selected
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(android.graphics.Color.parseColor(c.hex)))
                    .border(
                        width = if (isSelected) 4.dp else 0.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable { onSelect(c) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }
        }
    }
    Spacer(Modifier.height(12.dp))
    Text(
        text = selected.displayName,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
}

// ----------------------------------------------------------- Step 3: name + personality

@Composable
private fun NamePersonalityStep(
    name: String,
    onNameChange: (String) -> Unit,
    tags: Set<PersonalityTag>,
    onTagToggle: (PersonalityTag) -> Unit
) {
    Text(
        text = "给它取个名字",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(Modifier.height(16.dp))
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("例如：小团子") },
        singleLine = true,
        shape = RoundedCornerShape(16.dp)
    )
    Spacer(Modifier.height(24.dp))
    Text(
        text = "性格标签（可多选）",
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        val firstRow = PersonalityTag.entries.take(4)
        firstRow.forEach { tag ->
            PersonalityChip(
                tag = tag,
                selected = tags.contains(tag),
                onClick = { onTagToggle(tag) }
            )
        }
    }
    Spacer(Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        val secondRow = PersonalityTag.entries.drop(4)
        secondRow.forEach { tag ->
            PersonalityChip(
                tag = tag,
                selected = tags.contains(tag),
                onClick = { onTagToggle(tag) }
            )
        }
    }
}

@Composable
private fun PersonalityChip(
    tag: PersonalityTag,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .border(
                width = 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = tag.displayName,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ----------------------------------------------------------- Completion

private fun finishOnboarding(
    repository: com.deskpet.app.data.repository.PetRepository,
    name: String,
    species: PetSpecies,
    color: PetColor,
    tags: List<PersonalityTag>,
    context: android.content.Context
) {
    val finalTags = if (tags.isEmpty()) listOf(PersonalityTag.LIVELY) else tags
    repository.updatePet { pet ->
        pet.copy(
            name = name,
            species = species,
            color = color,
            personalityTags = finalTags
        )
    }
    // Request overlay permission if not already granted.
    if (!Settings.canDrawOverlays(context)) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
