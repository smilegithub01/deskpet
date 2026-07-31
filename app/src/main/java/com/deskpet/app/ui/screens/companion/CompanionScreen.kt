package com.deskpet.app.ui.screens.companion

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deskpet.app.DeskPetApplication
import com.deskpet.app.data.model.CompanionLink
import com.deskpet.app.data.model.PetSpecies
import com.deskpet.app.util.SoundHelper
import com.deskpet.app.util.SoundType
import kotlinx.coroutines.launch

/**
 * Companion raising screen (L3-2).
 *
 * Allows the user to generate a pair code to share with a partner, or to
 * enter a partner's code and start co-parenting immediately. Once paired the
 * screen shows the partner's pet card and a "送礼物" action. The local MVP
 * simulates the partner so this works fully offline; cloud sync (AGC Cloud DB
 * + Push Kit) can be plugged in later without changing the UI contract.
 */
@Composable
fun CompanionScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { DeskPetApplication.get().repository }

    val companion by repository.activeCompanion.collectAsStateWithLifecycle()
    var toastMessage by remember { mutableStateOf<String?>(null) }

    // Pair-code generation state.
    var generatedCode by remember { mutableStateOf<String?>(null) }
    var showInputDialog by remember { mutableStateOf(false) }
    var showBreakConfirm by remember { mutableStateOf(false) }

    // One-shot toast dismissal.
    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            kotlinx.coroutines.delay(2500)
            toastMessage = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top bar.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "共同养育",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.weight(1f))
            if (companion?.isActive == true) {
                TextButton(onClick = { showBreakConfirm = true }) {
                    Text("解除配对", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header icon + title.
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "👫", fontSize = 36.sp)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "共同养育",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "和 Ta 一起照顾小团子，分享日常，互相送礼物",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))

            if (companion?.isActive == true) {
                // ---- Already paired: show partner card ----
                PartnerCard(link = companion!!)
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                val msg = repository.sendCompanionGift()
                                SoundHelper.play(SoundType.PURCHASE)
                                toastMessage = msg
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("🎁 给 Ta 送小礼物")
                    }
                    Button(
                        onClick = { toastMessage = "马上就能看 ${companion!!.partnerName} 的宠物啦~" },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("👀 看 Ta 的宠物")
                    }
                }
                Spacer(Modifier.height(32.dp))

                // Status summary.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "共同养育小提示",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        BulletTip("每日互相送礼物，小团子心情会更好")
                        BulletTip("互相抚摸、喂食会增加共同回忆")
                        BulletTip("节日、生日、纪念日记得送惊喜哦")
                    }
                }
            } else {
                // ---- Not yet paired: code generation + input ----

                // Generated code card.
                if (generatedCode == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable {
                                scope.launch {
                                    val code = repository.generatePairCode()
                                    generatedCode = code
                                    SoundHelper.play(SoundType.TAP_LIGHT)
                                }
                            }
                            .padding(vertical = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "生成我的配对码",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                } else {
                    Text(
                        text = "你的配对码",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = generatedCode!!,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 6.sp
                        )
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = {
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                                as android.content.ClipboardManager
                            clipboard.setPrimaryClip(
                                android.content.ClipData.newPlainText("DeskPet配对码", generatedCode)
                            )
                            toastMessage = "配对码已复制"
                            SoundHelper.play(SoundType.TAP_LIGHT)
                        }) {
                            Icon(
                                Icons.Filled.ContentCopy,
                                contentDescription = "复制",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "将配对码分享给 Ta，Ta 输入后就能一起养育小团子~",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(28.dp))
                Text(
                    text = "或",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                        .clickable { showInputDialog = true }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "输入 Ta 的配对码",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // Floating toast.
    toastMessage?.let { msg ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .padding(bottom = 48.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.inverseSurface)
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = msg,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.inverseOnSurface
                )
            }
        }
    }

    // Pair-code input dialog.
    if (showInputDialog) {
        var input by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showInputDialog = false },
            title = { Text("输入配对码") },
            text = {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it.uppercase() },
                    singleLine = true,
                    placeholder = { Text("例如：A3H8K2") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val ok = repository.acceptPairCode(input)
                        if (ok) {
                            SoundHelper.play(SoundType.ACHIEVEMENT)
                            toastMessage = "配对成功！快和 Ta 一起养育吧~"
                        } else {
                            SoundHelper.play(SoundType.ERROR)
                            toastMessage = "配对码无效或已存在搭档"
                        }
                        showInputDialog = false
                    }
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInputDialog = false }) { Text("取消") }
            }
        )
    }

    // Break-link confirmation dialog.
    if (showBreakConfirm) {
        AlertDialog(
            onDismissRequest = { showBreakConfirm = false },
            title = { Text("解除共同养育？") },
            text = { Text("解除后双方将不再互相看宠物状态，可随时重新配对。") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        repository.breakCompanionLink()
                        toastMessage = "已解除配对"
                        showBreakConfirm = false
                    }
                }) {
                    Text("解除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBreakConfirm = false }) { Text("再想想") }
            }
        )
    }
}

// ============================================================
// Partner card + helpers
// ============================================================

@Composable
private fun PartnerCard(link: CompanionLink) {
    val species = runCatching { PetSpecies.valueOf(link.petSpecies) }.getOrDefault(PetSpecies.CAT)
    val (hours, mins) = minsSince(link.lastUpdate)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(text = species.emoji, fontSize = 32.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    text = link.partnerName.ifBlank { "搭档" },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = link.petName,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = "Lv.${link.petLevel}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatChip(
                modifier = Modifier.weight(1f),
                label = "心情",
                value = "${link.petMood}",
                color = MaterialTheme.colorScheme.primary
            )
            StatChip(
                modifier = Modifier.weight(1f),
                label = "配对码",
                value = link.pairCode,
                color = MaterialTheme.colorScheme.tertiary
            )
            StatChip(
                modifier = Modifier.weight(1f),
                label = "最近互动",
                value = "${hours}h${mins}m前",
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun StatChip(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(vertical = 8.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, fontSize = 11.sp, color = color)
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun BulletTip(text: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text("· ", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
        Text(
            text = text,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun minsSince(ts: Long): Pair<Int, Int> {
    val diff = (System.currentTimeMillis() - ts).coerceAtLeast(0L) / 1000L
    return (diff / 3600L).toInt() to ((diff % 3600L) / 60L).toInt()
}
