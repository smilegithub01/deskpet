package com.deskpet.app.ui.screens.dressup

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deskpet.app.DeskPetApplication
import com.deskpet.app.data.model.OutfitCategory
import com.deskpet.app.data.model.OutfitItem
import com.deskpet.app.data.model.Pet
import com.deskpet.app.data.model.equippedOutfitIds
import com.deskpet.app.ui.components.PetCanvas
import com.deskpet.app.util.SoundHelper
import com.deskpet.app.util.SoundType

/**
 * Returns the equipped item id for [category] from the live [Pet].
 */
private fun Pet.equippedIdFor(category: OutfitCategory): String? = when (category) {
    OutfitCategory.HEAD -> equippedHead
    OutfitCategory.GLASSES -> equippedGlasses
    OutfitCategory.COLLAR -> equippedCollar
    OutfitCategory.CLOTHING -> equippedClothing
    OutfitCategory.TAIL -> equippedTail
    OutfitCategory.ACCESSORY -> equippedAccessory
}

/**
 * Dress-Up screen.
 *
 * Pet preview at top → category tabs → 2-column item grid with equip / purchase
 * states → shop button at the bottom.
 */
@Composable
fun DressUpScreen() {
    val repository = remember { DeskPetApplication.get().repository }
    val pet by repository.petState.collectAsStateWithLifecycle()
    val ownedIds by repository.ownedOutfits.collectAsStateWithLifecycle()

    var selectedCategory by remember { mutableStateOf(OutfitCategory.HEAD) }
    val snackbarHostState = remember { SnackbarHostState() }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            toastMessage = null
        }
    }

    // Catalogue for the selected category with current ownership applied.
    val items = remember(selectedCategory, ownedIds) {
        repository.getOutfitShop().filter { it.category == selectedCategory }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp)
        ) {
            // ---- Header ----
            Text(
                text = "衣橱",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(12.dp))

            // ---- Pet preview ----
            // Use individual equip fields as remember keys so the preview
            // updates immediately when the user taps equip / unequip.
            val previewOutfits = remember(
                pet.equippedHead, pet.equippedGlasses, pet.equippedCollar,
                pet.equippedClothing, pet.equippedTail, pet.equippedAccessory
            ) {
                pet.equippedOutfitIds(repository.getOutfitItems())
            }
            DressUpPreview(color = pet.color, species = pet.species, outfits = previewOutfits)

            Spacer(Modifier.height(12.dp))

            // ---- Diamonds balance ----
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "💎 ${pet.diamonds}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Lv.${pet.level}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(12.dp))

            // ---- Category tabs ----
            CategoryTabs(
                selected = selectedCategory,
                onSelect = { selectedCategory = it }
            )

            Spacer(Modifier.height(12.dp))

            // ---- Item grid ----
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(items, key = { it.id }) { item ->
                    val equipped = pet.equippedIdFor(item.category) == item.id
                    val owned = ownedIds.contains(item.id)
                    OutfitCard(
                        item = item,
                        owned = owned,
                        equipped = equipped,
                        petLevel = pet.level,
                        onTap = {
                            when {
                                equipped -> {
                                    repository.unequip(item.category)
                                    SoundHelper.play(SoundType.TAP_LIGHT)
                                    toastMessage = "已脱下 ${item.name}"
                                }
                                owned -> {
                                    repository.equipItem(item)
                                    SoundHelper.play(SoundType.EQUIP)
                                    toastMessage = "已穿戴 ${item.name}"
                                }
                                pet.level < item.requiredLevel -> {
                                    SoundHelper.play(SoundType.ERROR)
                                    toastMessage = "需要 Lv.${item.requiredLevel} 才能解锁"
                                }
                                else -> {
                                    val ok = repository.purchaseItem(item)
                                    if (ok) SoundHelper.play(SoundType.PURCHASE) else SoundHelper.play(SoundType.ERROR)
                                    toastMessage = if (ok) "购买成功 ${item.name}" else "钻石不足"
                                }
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ---- Shop button ----
            ShopButton { toastMessage = "商店敬请期待～" }
            Spacer(Modifier.height(8.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )
    }
}

// ----------------------------------------------------------- Sub-components

@Composable
private fun DressUpPreview(
    color: com.deskpet.app.data.model.PetColor,
    species: com.deskpet.app.data.model.PetSpecies,
    outfits: Map<OutfitCategory, String> = emptyMap()
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(180.dp)
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
        PetCanvas(
            modifier = Modifier.size(180.dp),
            color = color,
            species = species,
            state = com.deskpet.app.data.model.PetState.IDLE,
            outfits = outfits
        )
    }
}

@Composable
private fun CategoryTabs(
    selected: OutfitCategory,
    onSelect: (OutfitCategory) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutfitCategory.entries.forEach { category ->
            val isSelected = category == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onSelect(category) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = category.displayName,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun OutfitCard(
    item: OutfitItem,
    owned: Boolean,
    equipped: Boolean,
    petLevel: Int,
    onTap: () -> Unit
) {
    val locked = petLevel < item.requiredLevel && !owned
    // 点击缩放动画反馈：通过 interactionSource.collectIsPressedAsState 监听按压状态
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        label = "cardScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (equipped) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surface
            )
            .border(
                width = if (equipped) 2.dp else 1.dp,
                color = if (equipped) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(),
                onClick = onTap
            )
            .padding(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(text = item.emoji, fontSize = 28.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = item.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            when {
                equipped -> Text(
                    text = "已穿戴",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                owned -> Text(
                    text = "点击穿戴",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                locked -> Text(
                    text = "Lv.${item.requiredLevel}解锁",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.error
                )
                else -> Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "💎", fontSize = 11.sp)
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = item.price.toString(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Equipped badge.
        if (equipped) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "✓",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun ShopButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.ShoppingBag,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "前往商店",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}
