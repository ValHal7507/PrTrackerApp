package com.example.prtracker.ui.screens

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.prtracker.data.SoundEngine
import com.example.prtracker.ui.components.GlowingCard
import com.example.prtracker.ui.components.GridBackground
import com.example.prtracker.ui.components.NeonButton
import com.example.prtracker.navigation.Routes
import com.example.prtracker.ui.theme.Background
import com.example.prtracker.ui.theme.CardBackground
import com.example.prtracker.ui.theme.LocalAppearance
import com.example.prtracker.ui.theme.PrimaryAccent
import com.example.prtracker.ui.theme.TextPrimary
import com.example.prtracker.ui.theme.TextSecondary
import com.example.prtracker.ui.theme.systemAccentColor
import com.example.prtracker.viewmodel.PRViewModel
import java.io.File

@Composable
fun SettingsScreen(
    viewModel: PRViewModel,
    navController: NavHostController
) {
    val context = LocalContext.current
    val appSettings by viewModel.appSettings.collectAsState()
    val appearance = LocalAppearance.current
    var showClearDialog by remember { mutableStateOf(false) }
    var showExportSuccess by remember { mutableStateOf(false) }
    var targetWeightText by remember(appSettings.targetWeight) {
        mutableStateOf(appSettings.targetWeight?.let { "%.1f".format(it) } ?: "")
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All Data", color = TextPrimary) },
            text = { Text("This will permanently delete all exercises and entries. Are you sure?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAllData()
                    showClearDialog = false
                }) {
                    Text("Clear", color = PrimaryAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = Background
        )
    }

    if (showExportSuccess) {
        AlertDialog(
            onDismissRequest = { showExportSuccess = false },
            title = { Text("Exported", color = TextPrimary) },
            text = { Text("Data exported to Downloads/prs_backup.json", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { showExportSuccess = false }) {
                    Text("OK", color = PrimaryAccent)
                }
            },
            containerColor = Background
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GridBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "SETTINGS",
                style = MaterialTheme.typography.displayLarge,
                color = appearance.systemAccentColor
            )

            Spacer(modifier = Modifier.height(32.dp))

            GlowingCard(
                modifier = Modifier.fillMaxWidth().clickable { navController.navigate("appearance") }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Appearance",
                        tint = appearance.systemAccentColor
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "APPEARANCE",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            GlowingCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("notification_settings") }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notification settings",
                        tint = PrimaryAccent
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "NOTIFICATIONS",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            GlowingCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Haptic feedback on log",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = appSettings.hapticEnabled,
                        onCheckedChange = {
                            if (appSettings.soundEnabled) SoundEngine.playToggleHaptic()
                            viewModel.toggleHaptic(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PrimaryAccent,
                            checkedTrackColor = PrimaryAccent.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            GlowingCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mascot speech",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Voice lines on tab entry",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = appSettings.speechesEnabled,
                        onCheckedChange = {
                            if (appSettings.soundEnabled) SoundEngine.playToggleHaptic()
                            viewModel.toggleSpeeches(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PrimaryAccent,
                            checkedTrackColor = PrimaryAccent.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            GlowingCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Text(
                        text = "SOUND EFFECTS",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "UI interaction sounds",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf(true to "ON", false to "OFF").forEach { (value, label) ->
                            val selected = appSettings.soundEnabled == value
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(25.dp))
                                    .background(
                                        if (selected) PrimaryAccent.copy(alpha = 0.15f)
                                        else CardBackground
                                    )
                                    .border(
                                        BorderStroke(
                                            1.dp,
                                            if (selected) PrimaryAccent
                                            else TextSecondary.copy(alpha = 0.3f)
                                        ),
                                        RoundedCornerShape(25.dp)
                                    )
                                    .clickable {
                                        viewModel.setSoundEnabled(value)
                                        if (value) SoundEngine.playToggleHaptic()
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (selected) PrimaryAccent else TextSecondary,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "VOL",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.width(32.dp)
                        )
                        Slider(
                            value = appSettings.soundVolume,
                            onValueChange = { viewModel.setSoundVolume(it) },
                            valueRange = 0f..1f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = PrimaryAccent,
                                activeTrackColor = PrimaryAccent,
                                inactiveTrackColor = TextSecondary.copy(alpha = 0.2f)
                            )
                        )
                        Text(
                            text = "${(appSettings.soundVolume * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryAccent,
                            modifier = Modifier.width(40.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            GlowingCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Text(
                        text = "CALENDAR DAY VIEW",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "What to show when tapping a day",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf("pr" to "DAY PR", "sum" to "SUM").forEach { (value, label) ->
                            val selected = appSettings.calendarDayViewMode == value
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(25.dp))
                                    .background(
                                        if (selected) PrimaryAccent.copy(alpha = 0.15f)
                                        else CardBackground
                                    )
                                    .border(
                                        BorderStroke(
                                            1.dp,
                                            if (selected) PrimaryAccent
                                            else TextSecondary.copy(alpha = 0.3f)
                                        ),
                                        RoundedCornerShape(25.dp)
                                    )
                                    .clickable { viewModel.setCalendarDayViewMode(value) }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (selected) PrimaryAccent else TextSecondary,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            GlowingCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Text(
                        text = "Weight Unit",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf("kg" to "kg", "lbs" to "lbs").forEach { (value, label) ->
                            val selected = appSettings.weightUnit == value
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(25.dp))
                                    .background(
                                        if (selected) PrimaryAccent.copy(alpha = 0.15f)
                                        else CardBackground
                                    )
                                    .border(
                                        BorderStroke(
                                            1.dp,
                                            if (selected) PrimaryAccent
                                            else TextSecondary.copy(alpha = 0.3f)
                                        ),
                                        RoundedCornerShape(25.dp)
                                    )
                                    .clickable { viewModel.setWeightUnit(value) }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (selected) PrimaryAccent else TextSecondary,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            GlowingCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Text(
                        text = "Target Weight (optional)",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = targetWeightText,
                        onValueChange = { text ->
                            val filtered = text.filter { c -> c.isDigit() || c == '.' }
                            val dotCount = filtered.count { it == '.' }
                            if (filtered.isEmpty()) {
                                targetWeightText = ""
                                viewModel.setTargetWeight(null)
                            } else if (dotCount <= 1 && filtered.replace(".", "").length <= 5) {
                                targetWeightText = filtered
                                filtered.toFloatOrNull()?.let { viewModel.setTargetWeight(it) }
                            }
                        },
                        placeholder = { Text("e.g. 75.0", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = PrimaryAccent,
                            focusedBorderColor = PrimaryAccent,
                            unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                            focusedContainerColor = Background,
                            unfocusedContainerColor = Background
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            GlowingCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "DEVICE SYNC",
                        style = MaterialTheme.typography.titleLarge,
                        color = PrimaryAccent,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    NeonButton(
                        text = "SHARE DATA",
                        onClick = { navController.navigate("sync_export") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "To import: open a prtracker_backup.json file from your file manager",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            NeonButton(
                text = "EXPORT DATA",
                onClick = {
                    exportToDownloads(context)
                    showExportSuccess = true
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            NeonButton(
                text = "CLEAR ALL DATA",
                onClick = { showClearDialog = true }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "PR TRACKER v1.0",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun exportToDownloads(context: Context) {
    try {
        val sourceFile = File(context.filesDir, "prs.json")
        if (!sourceFile.exists()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, "prs_backup.json")
                put(MediaStore.Downloads.MIME_TYPE, "application/json")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val uri = context.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                contentValues
            )
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    sourceFile.inputStream().copyTo(outputStream)
                }
                contentValues.clear()
                contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                context.contentResolver.update(it, contentValues, null, null)
            }
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            )
            val destFile = File(downloadsDir, "prs_backup.json")
            sourceFile.copyTo(destFile, overwrite = true)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
