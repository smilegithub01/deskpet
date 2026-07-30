package com.deskpet.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deskpet.app.DeskPetApplication
import com.deskpet.app.data.model.PetColor
import com.deskpet.app.service.PetOverlayService
import com.deskpet.app.util.PermissionHelper
import com.deskpet.app.util.SoundHelper

/**
 * Settings screen with grouped sections (宠物 / 桌面 / 外观 / 提醒 / 隐私 / 通知).
 *
 * Turning off the desktop overlay shows a confirmation dialog first.
 */
@Composable
fun SettingsScreen() {
    val repository = remember { DeskPetApplication.get().repository }
    val pet by repository.petState.collectAsStateWithLifecycle()
    val settings by repository.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showOverlayDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    // 悬浮窗权限引导对话框
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("需要悬浮窗权限") },
            text = { Text("已为您跳转系统设置，请授权「显示在其他应用上层」后返回重试。") },
            confirmButton = {
                TextButton(onClick = {
                    if (PermissionHelper.canDrawOverlays(context)) {
                        PetOverlayService.start(context)
                        repository.updateSettings { it.copy(overlayEnabled = true) }
                        showPermissionDialog = false
                    } else {
                        PermissionHelper.requestOverlayPermission(context)
                    }
                }) { Text("已授权，开启") }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) { Text("取消") }
            }
        )
    }

    if (showOverlayDialog) {
        AlertDialog(
            onDismissRequest = { showOverlayDialog = false },
            title = { Text("关闭桌面悬浮") },
            text = { Text("关闭后小团子将不再显示在桌面上，确定要关闭吗？") },
            confirmButton = {
                TextButton(onClick = {
                    PetOverlayService.stop(context)
                    repository.updateSettings { it.copy(overlayEnabled = false) }
                    showOverlayDialog = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showOverlayDialog = false }) { Text("取消") }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            top = 12.dp, bottom = 96.dp
        )
    ) {
        item {
            Text(
                text = "设置",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // ---- 宠物 ----
        item {
            SettingsSection(title = "宠物") {
                InfoRow(icon = "🐾", label = "宠物名称", value = pet.name)
                InfoRow(icon = "🐱", label = "宠物种类", value = pet.species.displayName)
                InfoRow(icon = "⭐", label = "等级", value = "Lv.${pet.level}")
                InfoRow(icon = "💎", label = "钻石", value = pet.diamonds.toString())
            }
        }

        // ---- 桌面 ----
        item {
            SettingsSection(title = "桌面") {
                ToggleRow(
                    icon = "🪟",
                    label = "桌面悬浮",
                    checked = settings.overlayEnabled,
                    onCheckedChange = { v ->
                        if (!v) {
                            showOverlayDialog = true
                        } else {
                            if (PermissionHelper.canDrawOverlays(context)) {
                                PetOverlayService.start(context)
                                repository.updateSettings { it.copy(overlayEnabled = true) }
                            } else {
                                PermissionHelper.requestOverlayPermission(context)
                                showPermissionDialog = true
                            }
                        }
                    }
                )
                ToggleRow(
                    icon = "🤖",
                    label = "自动行为",
                    checked = settings.autoBehavior,
                    onCheckedChange = { v ->
                        repository.updateSettings { it.copy(autoBehavior = v) }
                    }
                )
                ToggleRow(
                    icon = "🧠",
                    label = "智能避让",
                    checked = settings.smartAvoidance,
                    onCheckedChange = { v ->
                        repository.updateSettings { it.copy(smartAvoidance = v) }
                    }
                )
                ToggleRow(
                    icon = "📱",
                    label = "桌面小部件",
                    checked = settings.widgetEnabled,
                    onCheckedChange = { v ->
                        repository.updateSettings { it.copy(widgetEnabled = v) }
                    }
                )
                ToggleRow(
                    icon = "🖼️",
                    label = "动态壁纸",
                    checked = settings.liveWallpaperEnabled,
                    onCheckedChange = { v ->
                        repository.updateSettings { it.copy(liveWallpaperEnabled = v) }
                    }
                )
            }
        }

        // ---- 外观 ----
        item {
            SettingsSection(title = "外观") {
                InfoRow(
                    icon = "🎨",
                    label = "主题颜色",
                    value = settings.themeColor.displayName
                )
                ColorDotsRow(
                    selected = settings.themeColor,
                    onSelect = { c ->
                        repository.updateSettings { it.copy(themeColor = c) }
                    }
                )
                ToggleRow(
                    icon = "🔊",
                    label = "音效",
                    checked = settings.soundEnabled,
                    onCheckedChange = { v ->
                        SoundHelper.setEnabled(v)
                        repository.updateSettings { it.copy(soundEnabled = v) }
                    }
                )
                ToggleRow(
                    icon = "🗣️",
                    label = "宠物语音",
                    checked = settings.ttsEnabled,
                    onCheckedChange = { v ->
                        com.deskpet.app.util.SpeechHelper.setEnabled(v)
                        repository.updateSettings { it.copy(ttsEnabled = v) }
                    }
                )
                ToggleRow(
                    icon = "🏷️",
                    label = "分享水印",
                    checked = settings.shareWatermark,
                    onCheckedChange = { v ->
                        repository.updateSettings { it.copy(shareWatermark = v) }
                    }
                )
            }
        }

        // ---- 提醒 ----
        item {
            SettingsSection(title = "提醒") {
                ToggleRow(
                    icon = "💧",
                    label = "喝水提醒",
                    checked = settings.waterReminderEnabled,
                    onCheckedChange = { v ->
                        repository.updateSettings { it.copy(waterReminderEnabled = v) }
                    }
                )
                ToggleRow(
                    icon = "🪑",
                    label = "久坐提醒",
                    checked = settings.sitReminderEnabled,
                    onCheckedChange = { v ->
                        repository.updateSettings { it.copy(sitReminderEnabled = v) }
                    }
                )
                ToggleRow(
                    icon = "👁️",
                    label = "护眼提醒",
                    checked = settings.eyeReminderEnabled,
                    onCheckedChange = { v ->
                        repository.updateSettings { it.copy(eyeReminderEnabled = v) }
                    }
                )
                ToggleRow(
                    icon = "🌙",
                    label = "勿扰时段",
                    checked = settings.quietHoursEnabled,
                    onCheckedChange = { v ->
                        repository.updateSettings { it.copy(quietHoursEnabled = v) }
                    }
                )
                ToggleRow(
                    icon = "🌤️",
                    label = "环境感知",
                    checked = settings.envAwarenessEnabled,
                    onCheckedChange = { v ->
                        repository.updateSettings { it.copy(envAwarenessEnabled = v) }
                    }
                )
            }
        }

        // ---- 隐私 ----
        item {
            SettingsSection(title = "隐私") {
                ToggleRow(
                    icon = "📅",
                    label = "经期记录",
                    checked = settings.periodTrackingEnabled,
                    onCheckedChange = { v ->
                        repository.updateSettings { it.copy(periodTrackingEnabled = v) }
                    }
                )
                if (settings.periodTrackingEnabled) {
                    ToggleRow(
                        icon = "🐾",
                        label = "经期行为联动",
                        checked = settings.periodBehaviorLink,
                        onCheckedChange = { v ->
                            repository.updateSettings { it.copy(periodBehaviorLink = v) }
                        }
                    )
                }
                // Privacy notice
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔒",
                        fontSize = 20.sp
                    )
                    Spacer(Modifier.size(12.dp))
                    Text(
                        text = "你的经期数据仅保存在本机，不会上传任何服务器",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
                ToggleRow(
                    icon = "🔐",
                    label = "数据加密",
                    checked = settings.dataEncrypted,
                    onCheckedChange = { v ->
                        repository.updateSettings { it.copy(dataEncrypted = v) }
                    }
                )
            }
        }

        // ---- 通知 ----
        item {
            SettingsSection(title = "通知") {
                InfoRow(
                    icon = "🔕",
                    label = "勿扰开始",
                    value = "${settings.quietHoursStart}:00"
                )
                InfoRow(
                    icon = "🔔",
                    label = "勿扰结束",
                    value = "${settings.quietHoursEnd}:00"
                )
            }
        }
    }
}

// ----------------------------------------------------------- Sub-components

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title.uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 4.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun InfoRow(icon: String, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 20.sp)
        Spacer(Modifier.size(12.dp))
        Text(
            text = label,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ToggleRow(
    icon: String,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 20.sp)
        Spacer(Modifier.size(12.dp))
        Text(
            text = label,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        PetSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ColorDotsRow(
    selected: PetColor,
    onSelect: (PetColor) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PetColor.entries.forEach { color ->
            val isSelected = color == selected
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(android.graphics.Color.parseColor(color.hex)))
                    .clickable { onSelect(color) }
                    .then(
                        if (isSelected) Modifier.padding(0.dp)
                        else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }
        }
    }
}

/**
 * Custom toggle switch using Material3 [Switch] with brand colors.
 */
@Composable
private fun PetSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
            checkedTrackColor = MaterialTheme.colorScheme.primary,
            uncheckedThumbColor = MaterialTheme.colorScheme.surface,
            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}
