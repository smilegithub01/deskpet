package com.deskpet.app.ui.screens.travel

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deskpet.app.DeskPetApplication
import com.deskpet.app.data.model.InteractionLog
import com.deskpet.app.data.model.InteractionType
import com.deskpet.app.data.model.Postcard
import com.deskpet.app.data.model.TravelDestination
import com.deskpet.app.util.ShareCardData
import com.deskpet.app.util.ShareCardRenderer
import com.deskpet.app.util.ShareCardType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TravelScreen(
    onBack: () -> Unit,
    viewModel: TravelViewModel = viewModel()
) {
    val pet by viewModel.pet.collectAsStateWithLifecycle()
    val activeTravel by viewModel.activeTravel.collectAsStateWithLifecycle()
    val toast by viewModel.toast.collectAsStateWithLifecycle()
    val returnResult by viewModel.returnResult.collectAsStateWithLifecycle()
    val postcards by viewModel.postcards.collectAsStateWithLifecycle(initialValue = emptyList())

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(toast) {
        toast?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onToastShown()
        }
    }

    var selectedDestination by remember { mutableStateOf<TravelDestination?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, "返回", tint = MaterialTheme.colorScheme.onBackground)
                }
                Text(
                    text = "旅行",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Active travel status
            activeTravel?.let { travel ->
                TravelStatusCard(travel.destinationName, travel.returnTime)
            }

            // Destination list
            if (activeTravel == null) {
                Text(
                    text = "选择目的地",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                LazyColumn(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp, end = 16.dp, bottom = 96.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(viewModel.getDestinations(), key = { it.id }) { dest ->
                        DestinationCard(
                            destination = dest,
                            petLevel = pet.level,
                            onTap = { selectedDestination = dest }
                        )
                    }
                }
            }

            // Postcard gallery (if any)
            if (postcards.isNotEmpty()) {
                Text(
                    text = "明信片收藏 (${postcards.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                LazyColumn(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp, end = 16.dp, bottom = 96.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(postcards, key = { it.id }) { postcard ->
                        PostcardCard(
                            emoji = postcard.destinationEmoji,
                            destination = postcard.destinationName,
                            message = postcard.message,
                            date = postcard.date
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )
    }

    // Duration selection dialog
    selectedDestination?.let { dest ->
        DurationSelectDialog(
            destination = dest,
            onDismiss = { selectedDestination = null },
            onSelect = { durationMs ->
                viewModel.startTravel(dest, durationMs)
                selectedDestination = null
            }
        )
    }

    // Travel return dialog
    returnResult?.let { result ->
        if (result.returned) {
            AlertDialog(
                onDismissRequest = { viewModel.onReturnResultHandled() },
                title = { Text("旅行归来!") },
                text = {
                    Column {
                        Text(result.message)
                        if (result.gifts.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text("收获：", fontWeight = FontWeight.SemiBold)
                            result.gifts.forEach { Text("  $it") }
                        }
                        result.postcard?.let {
                            Spacer(Modifier.height(8.dp))
                            Text("明信片：$it", fontStyle = FontStyle.Italic)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.onReturnResultHandled() }) {
                        Text("好耶!")
                    }
                }
            )
        }
    }
}

@Composable
private fun TravelStatusCard(destName: String, returnTime: Long) {
    val sdf = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeStr = remember(returnTime) { sdf.format(Date(returnTime)) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "✈️", fontSize = 32.sp)
        Spacer(Modifier.size(12.dp))
        Column {
            Text(
                text = "正在前往 $destName",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "预计 $timeStr 回来",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun DestinationCard(
    destination: TravelDestination,
    petLevel: Int,
    onTap: () -> Unit
) {
    val locked = petLevel < destination.requiredLevel

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (locked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .clickable(enabled = !locked, onClick = onTap)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = destination.emoji, fontSize = 36.sp)
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = destination.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${destination.type.displayName} · ${destination.type.durationRange.first / 60000}-${destination.type.durationRange.second / 60000}分钟",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "💎 ${destination.giftDiamondRange.first}-${destination.giftDiamondRange.second}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (locked) {
            Text(
                text = "Lv.${destination.requiredLevel}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DurationSelectDialog(
    destination: TravelDestination,
    onDismiss: () -> Unit,
    onSelect: (Long) -> Unit
) {
    val durations = remember(destination) {
        val (min, max) = destination.type.durationRange
        listOf(min, (min + max) / 2, max)
    }
    val labels = remember(destination) {
        val (min, max) = destination.type.durationRange
        listOf(
            "${min / 60000}分钟",
            "${(min + max) / 2 / 60000}分钟",
            "${max / 3600000}小时"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("前往${destination.name}") },
        text = {
            Column {
                Text(text = "选择出行时长：", fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                durations.forEachIndexed { index, duration ->
                    TextButton(onClick = { onSelect(duration) }) {
                        Text(text = labels[index], fontSize = 16.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
private fun PostcardCard(emoji: String, destination: String, message: String, date: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(text = emoji, fontSize = 24.sp)
        Spacer(Modifier.size(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$destination · $date",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontStyle = FontStyle.Italic
            )
        }
        IconButton(
            onClick = {
                scope.launch(Dispatchers.IO) {
                    val app = context.applicationContext as DeskPetApplication
                    val pet = app.repository.getPet()
                    val settings = app.repository.getSettings()
                    val postcard = Postcard(
                        destinationId = destination,
                        destinationName = destination,
                        destinationEmoji = emoji,
                        date = date,
                        message = message,
                        sceneDrawKey = destination,
                        petEmoji = pet.species.displayName
                    )
                    app.database.interactionLogDao().insert(InteractionLog(
                        type = InteractionType.SHARE.name,
                        timestamp = System.currentTimeMillis(),
                        detail = ShareCardType.POSTCARD.name
                    ))
                    ShareCardRenderer.renderAndShare(context, ShareCardData(
                        pet = pet,
                        type = ShareCardType.POSTCARD,
                        postcard = postcard,
                        showWatermark = settings.shareWatermark
                    ))
                }
            },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Filled.Share,
                contentDescription = "分享明信片",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
