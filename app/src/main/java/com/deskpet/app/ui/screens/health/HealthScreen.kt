package com.deskpet.app.ui.screens.health

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deskpet.app.DeskPetApplication
import com.deskpet.app.data.model.HabitStreak
import com.deskpet.app.data.model.HabitType
import com.deskpet.app.data.model.MoodLevel
import com.deskpet.app.data.model.MoodLog
import com.deskpet.app.data.model.PeriodLog
import com.deskpet.app.data.repository.HabitCheckinResult
import com.deskpet.app.util.SoundHelper
import com.deskpet.app.util.SoundType
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Health screen: mood diary, period calendar, health reminders and a
 * breathing meditation exercise.
 */
@Composable
fun HealthScreen() {
    val app = remember { DeskPetApplication.get() }
    val database = remember { app.database }
    val repository = remember { app.repository }
    val scope = rememberCoroutineScope()
    val healthViewModel: HealthViewModel = viewModel()

    val settings by repository.settings.collectAsStateWithLifecycle()
    val habitStreaks by healthViewModel.habitStreaks.collectAsStateWithLifecycle()
    val checkinResult by healthViewModel.checkinResult.collectAsStateWithLifecycle()
    val recentMoods by database.moodLogDao().getRecent(7)
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val periodLogs by database.periodLogDao().getAll()
        .collectAsStateWithLifecycle(initialValue = emptyList())

    var selectedMood by remember { mutableStateOf<MoodLevel?>(null) }
    var noteText by remember { mutableStateOf("") }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            toastMessage = null
        }
    }

    LaunchedEffect(checkinResult) {
        checkinResult?.let { result ->
            if (result.success) {
                SoundHelper.play(SoundType.CHECKIN)
                toastMessage = result.message
            } else {
                toastMessage = result.message
            }
            healthViewModel.clearResult()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                top = 12.dp, bottom = 96.dp
            )
        ) {
            item {
                Text(
                    text = "健康",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // ---- Mood diary card ----
            item {
                MoodDiaryCard(
                    selectedMood = selectedMood,
                    noteText = noteText,
                    recentMoods = recentMoods,
                    onMoodSelected = { selectedMood = it },
                    onNoteChange = { noteText = it },
                    onSave = {
                        val mood = selectedMood
                        if (mood == null) {
                            toastMessage = "请先选择心情"
                        } else {
                            scope.launch {
                                database.moodLogDao().insert(
                                    MoodLog(
                                        date = startOfToday(),
                                        mood = mood,
                                        note = noteText
                                    )
                                )
                                selectedMood = null
                                noteText = ""
                                toastMessage = "心情已记录"
                            }
                        }
                    }
                )
            }

            // ---- Period calendar card ----
            item {
                PeriodCalendarCard(
                    periodLogs = periodLogs,
                    onToggleDay = { day ->
                        scope.launch {
                            val date = dateForDay(day)
                            val existing = database.periodLogDao().getByDate(date)
                            if (existing == null) {
                                database.periodLogDao().insert(PeriodLog(date = date, isPeriodStart = false))
                            } else {
                                database.periodLogDao().deleteByDate(date)
                            }
                        }
                    }
                )
            }

            // ---- Health reminders card ----
            item {
                HealthRemindersCard(
                    waterEnabled = settings.waterReminderEnabled,
                    sitEnabled = settings.sitReminderEnabled,
                    eyeEnabled = settings.eyeReminderEnabled,
                    habitStreaks = habitStreaks,
                    onWaterChange = { v ->
                        repository.updateSettings { it.copy(waterReminderEnabled = v) }
                    },
                    onSitChange = { v ->
                        repository.updateSettings { it.copy(sitReminderEnabled = v) }
                    },
                    onEyeChange = { v ->
                        repository.updateSettings { it.copy(eyeReminderEnabled = v) }
                    },
                    onCheckin = { habitType ->
                        healthViewModel.checkin(habitType)
                    }
                )
            }

            // ---- Breathing meditation card ----
            item {
                BreathingMeditationCard()
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )
    }
}

// ----------------------------------------------------------- Mood diary

@Composable
private fun MoodDiaryCard(
    selectedMood: MoodLevel?,
    noteText: String,
    recentMoods: List<MoodLog>,
    onMoodSelected: (MoodLevel) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit
) {
    CardContainer(title = "今日心情") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MoodLevel.entries.forEach { mood ->
                val isSelected = selectedMood == mood
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = { onMoodSelected(mood) }
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = CircleShape
                            ),
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
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = noteText,
            onValueChange = onNoteChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("写点什么吧…") },
            shape = RoundedCornerShape(12.dp),
            minLines = 2
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 7-day history dots
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(7) { index ->
                    val log = recentMoods.getOrNull(index)
                    val color = if (log != null) {
                        Color(android.graphics.Color.parseColor(log.mood.colorHex))
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }
            SaveButton(onClick = onSave)
        }
    }
}

// ----------------------------------------------------------- Period calendar

@Composable
private fun PeriodCalendarCard(
    periodLogs: List<PeriodLog>,
    onToggleDay: (Int) -> Unit
) {
    val calendar = remember { Calendar.getInstance() }
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val today = calendar.get(Calendar.DAY_OF_MONTH)
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    val periodDays = remember(periodLogs, year, month) {
        periodLogs.mapNotNull { log ->
            val c = Calendar.getInstance().apply { timeInMillis = log.date }
            if (c.get(Calendar.YEAR) == year && c.get(Calendar.MONTH) == month) {
                c.get(Calendar.DAY_OF_MONTH)
            } else null
        }.toSet()
    }

    CardContainer(title = "经期日历 · ${month + 1}月") {
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth().height(((daysInMonth / 7 + 2) * 44).dp)
        ) {
            items(daysInMonth) { index ->
                val day = index + 1
                val isPeriod = periodDays.contains(day)
                val isToday = day == today
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(
                            if (isPeriod) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            width = if (isToday) 2.dp else 0.dp,
                            color = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { onToggleDay(day) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.toString(),
                        fontSize = 12.sp,
                        color = if (isPeriod) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "点击日期可标记/取消经期",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ----------------------------------------------------------- Health reminders

@Composable
private fun HealthRemindersCard(
    waterEnabled: Boolean,
    sitEnabled: Boolean,
    eyeEnabled: Boolean,
    habitStreaks: List<HabitStreak>,
    onWaterChange: (Boolean) -> Unit,
    onSitChange: (Boolean) -> Unit,
    onEyeChange: (Boolean) -> Unit,
    onCheckin: (HabitType) -> Unit
) {
    CardContainer(title = "健康提醒") {
        HabitReminderRow(
            label = "喝水提醒",
            icon = "💧",
            checked = waterEnabled,
            streak = habitStreaks.find { it.habitType == HabitType.DRINK.name },
            onCheckedChange = onWaterChange,
            onCheckin = { onCheckin(HabitType.DRINK) }
        )
        HabitReminderRow(
            label = "久坐提醒",
            icon = "🪑",
            checked = sitEnabled,
            streak = habitStreaks.find { it.habitType == HabitType.SIT.name },
            onCheckedChange = onSitChange,
            onCheckin = { onCheckin(HabitType.SIT) }
        )
        HabitReminderRow(
            label = "护眼提醒",
            icon = "👁️",
            checked = eyeEnabled,
            streak = habitStreaks.find { it.habitType == HabitType.EYE.name },
            onCheckedChange = onEyeChange,
            onCheckin = { onCheckin(HabitType.EYE) }
        )
    }
}

@Composable
private fun HabitReminderRow(
    label: String,
    icon: String,
    checked: Boolean,
    streak: HabitStreak?,
    onCheckedChange: (Boolean) -> Unit,
    onCheckin: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 20.sp)
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (streak != null && streak.currentStreak > 0) {
                Text(
                    text = "连续 ${streak.currentStreak} 天",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        // Check-in button
        if (checked) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable(onClick = onCheckin),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "打卡",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.size(8.dp))
        }
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
}

// ----------------------------------------------------------- Breathing meditation

@Composable
private fun BreathingMeditationCard() {
    var running by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(running) {
        while (running) {
            kotlinx.coroutines.delay(1000)
            elapsedSeconds += 1
        }
    }

    val transition = rememberInfiniteTransition(label = "breath")
    val scaleAnim by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            tween(4000, easing = LinearEasing),
            RepeatMode.Reverse
        ),
        label = "breathScale"
    )
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(4000, easing = LinearEasing),
            RepeatMode.Reverse
        ),
        label = "breathPhase"
    )
    val breathLabel = if (phase < 0.5f) "吸气…" else "呼气…"

    CardContainer(title = "呼吸冥想") {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .scale(if (running) scaleAnim else 0.7f)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.tertiaryContainer,
                                        MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            )
                    )
                    Text(
                        text = if (running) breathLabel else "点击开始",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = if (running) formatSeconds(elapsedSeconds) else "4秒吸气 · 4秒呼气",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (running) MaterialTheme.colorScheme.surfaceVariant
                            else MaterialTheme.colorScheme.primary
                        )
                        .clickable {
                            running = !running
                            if (!running) elapsedSeconds = 0
                        }
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (running) "停止" else "开始",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (running) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------------- Shared building blocks

@Composable
private fun CardContainer(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun SaveButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.size(4.dp))
        Text(
            text = "保存",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

// ----------------------------------------------------------- Helpers

private fun startOfToday(): Long {
    val c = Calendar.getInstance()
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    return c.timeInMillis
}

private fun dateForDay(day: Int): Long {
    val c = Calendar.getInstance()
    c.set(Calendar.DAY_OF_MONTH, day)
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    return c.timeInMillis
}

private fun formatSeconds(total: Int): String {
    val m = total / 60
    val s = total % 60
    return "%02d:%02d".format(m, s)
}
