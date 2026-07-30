package com.deskpet.app.ui.screens.decor

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deskpet.app.data.model.FurnitureCategory
import com.deskpet.app.data.model.FurnitureItem
import com.deskpet.app.ui.components.RoomSceneCanvas

@Composable
fun DecorScreen(
    onBack: () -> Unit,
    viewModel: DecorViewModel = viewModel()
) {
    val pet by viewModel.pet.collectAsStateWithLifecycle()
    val ownedFurniture by viewModel.ownedFurniture.collectAsStateWithLifecycle()
    val roomLayout by viewModel.roomLayout.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val toast by viewModel.toast.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(toast) {
        toast?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onToastShown()
        }
    }

    val catalogue = remember { viewModel.getFurnitureCatalogue() }
    val filteredItems = remember(selectedCategory) {
        catalogue.filter { it.category == selectedCategory }
    }

    val tabs = FurnitureCategory.entries

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "宠物小窝",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.weight(1f))
            Text(text = "💎 ${pet.diamonds}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.width(8.dp))
            Text(text = "Lv.${pet.level}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        }

        // Room scene preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            RoomSceneCanvas(
                layout = roomLayout,
                furnitureCatalogue = catalogue
            )
        }

        Spacer(Modifier.height(8.dp))

        // Category tabs
        ScrollableTabRow(
            selectedTabIndex = tabs.indexOf(selectedCategory),
            edgePadding = 16.dp
        ) {
            tabs.forEach { category ->
                Tab(
                    selected = category == selectedCategory,
                    onClick = { viewModel.selectCategory(category) },
                    text = { Text(category.displayName) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Furniture grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, end = 16.dp, bottom = 96.dp
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredItems, key = { it.id }) { item ->
                FurnitureCard(
                    item = item,
                    owned = ownedFurniture.contains(item.id),
                    petLevel = pet.level,
                    onTap = { viewModel.onTapFurniture(item) }
                )
            }
        }
        } // close Column

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )
    } // close Box
}

@Composable
private fun FurnitureCard(
    item: FurnitureItem,
    owned: Boolean,
    petLevel: Int,
    onTap: () -> Unit
) {
    val locked = petLevel < item.requiredLevel
    val bgColor = when {
        owned -> MaterialTheme.colorScheme.primaryContainer
        locked -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.surface
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(
                1.dp,
                if (owned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onTap)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = item.emoji, fontSize = 32.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            text = item.name,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        when {
            owned -> Text("✓ 已拥有", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
            locked -> Text("Lv.${item.requiredLevel}解锁", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            else -> Text("💎 ${item.price}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
        }
        // Show stats if any
        val stats = buildString {
            if (item.comfort > 0) append("舒适+${item.comfort} ")
            if (item.funLevel > 0) append("趣味+${item.funLevel} ")
            if (item.beauty > 0) append("美观+${item.beauty}")
        }
        if (stats.isNotBlank()) {
            Spacer(Modifier.height(2.dp))
            Text(stats.trim(), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
