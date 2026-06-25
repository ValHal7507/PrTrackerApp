package com.example.prtracker.ui.screens

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.prtracker.data.PotionType
import com.example.prtracker.data.SoundEngine
import com.example.prtracker.data.XpEngine
import com.example.prtracker.ui.components.GlowingCard
import com.example.prtracker.ui.components.GridBackground
import com.example.prtracker.ui.components.NeonButton
import com.example.prtracker.navigation.Routes
import com.example.prtracker.ui.theme.Background
import com.example.prtracker.ui.theme.CardBackground
import com.example.prtracker.ui.theme.GoalComplete
import com.example.prtracker.ui.theme.GoalReachedColor
import com.example.prtracker.ui.theme.LocalAppearance
import com.example.prtracker.ui.theme.PrimaryAccent
import com.example.prtracker.ui.theme.SecondaryAccent
import com.example.prtracker.ui.theme.TextPrimary
import com.example.prtracker.ui.theme.TextSecondary
import com.example.prtracker.ui.theme.systemAccentColor
import com.example.prtracker.viewmodel.PRViewModel
import kotlinx.coroutines.delay
import java.io.File

private enum class SettingsExportType { APP, PET, BOTH }

@Composable
fun SettingsScreen(
    viewModel: PRViewModel,
    navController: NavHostController
) {
    val context = LocalContext.current
    val appSettings by viewModel.appSettings.collectAsState()
    val appearance = LocalAppearance.current
    val totalXp by viewModel.totalXp.collectAsState()
    val currentLevel by viewModel.currentLevel.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showExportSuccess by remember { mutableStateOf(false) }
    var selectedExportType by remember { mutableStateOf(SettingsExportType.BOTH) }
    var petClearStep by remember { mutableIntStateOf(0) }
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
        val filename = when (selectedExportType) {
            SettingsExportType.APP -> "prtracker_app_backup.json"
            SettingsExportType.PET -> "prtracker_pets_backup.json"
            SettingsExportType.BOTH -> "prtracker_backup.json"
        }
        AlertDialog(
            onDismissRequest = { showExportSuccess = false },
            title = { Text("Exported", color = TextPrimary) },
            text = { Text("Data exported to Downloads/$filename", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { showExportSuccess = false }) {
                    Text("OK", color = PrimaryAccent)
                }
            },
            containerColor = Background
        )
    }

    if (showExportDialog) {
        val exercisesCount = viewModel.exercises.value.size
        val goalsCount = viewModel.goals.value.size
        val weightCount = viewModel.weightEntries.value.size
        val presetCount = viewModel.workoutPresets.value.size
        val runCount = viewModel.runEntries.value.size
        val historyCount = viewModel.workoutHistory.value.size
        val petCount = viewModel.petInventory.value.size
        val coins = viewModel.coins.value
        val totalRolls = viewModel.totalRolls.value
        val upgradeCount = viewModel.petUpgrades.value.size

        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("EXPORT DATA", color = PrimaryAccent, fontFamily = FontFamily.Monospace) },
            text = {
                Column {
                    Text("Select what to export:", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SettingsExportType.entries.forEach { type ->
                            val label = when (type) {
                                SettingsExportType.APP -> "APP"
                                SettingsExportType.PET -> "PET"
                                SettingsExportType.BOTH -> "BOTH"
                            }
                            val isSelected = selectedExportType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) PrimaryAccent.copy(alpha = 0.15f)
                                        else Color(0xFF0D1526)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) PrimaryAccent else TextSecondary.copy(alpha = 0.3f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedExportType = type },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) PrimaryAccent else TextSecondary,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = when (selectedExportType) {
                            SettingsExportType.APP -> "$exercisesCount exercises · $goalsCount goals · $weightCount weight · $presetCount presets · $runCount runs · $historyCount workouts"
                            SettingsExportType.PET -> "$petCount pets · ${java.text.NumberFormat.getIntegerInstance().format(coins)} coins · $totalRolls rolls · $upgradeCount upgrades"
                            SettingsExportType.BOTH -> "All app data + pet data"
                        },
                        color = TextSecondary,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    exportToDownloads(context, viewModel, selectedExportType)
                    showExportDialog = false
                    showExportSuccess = true
                }) {
                    Text("EXPORT", color = PrimaryAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = Background
        )
    }

    if (petClearStep == 1) {
        AlertDialog(
            onDismissRequest = { petClearStep = 0 },
            title = { Text("Wipe Pet Data", color = Color(0xFFFF003C)) },
            text = { Text("This will wipe ALL pet data — pets, coins, rolls, and upgrades. Continue?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { petClearStep = 2 }) {
                    Text("CONFIRM", color = Color(0xFFFF003C))
                }
            },
            dismissButton = {
                TextButton(onClick = { petClearStep = 0 }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = Background
        )
    }

    if (petClearStep == 2) {
        AlertDialog(
            onDismissRequest = { petClearStep = 0 },
            title = { Text("Are You Sure?", color = Color(0xFFFF003C)) },
            text = { Text("This cannot be undone. All pets, coins, and upgrades will be permanently deleted.", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { petClearStep = 3 }) {
                    Text("CONFIRM", color = Color(0xFFFF003C))
                }
            },
            dismissButton = {
                TextButton(onClick = { petClearStep = 0 }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = Background
        )
    }

    if (petClearStep == 3) {
        AlertDialog(
            onDismissRequest = { petClearStep = 0 },
            title = { Text("FINAL CONFIRMATION", color = Color(0xFFFF003C)) },
            text = { Text("All pets and progress will be permanently deleted. This is your last chance to cancel.", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearPetData()
                    petClearStep = 0
                }) {
                    Text("DELETE PERMANENTLY", color = Color(0xFFFF003C))
                }
            },
            dismissButton = {
                TextButton(onClick = { petClearStep = 0 }) {
                    Text("Cancel", color = TextSecondary)
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
                onClick = { showExportDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            NeonButton(
                text = "CLEAR ALL DATA",
                onClick = { showClearDialog = true }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { petClearStep = 1 },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF003C).copy(alpha = 0.15f)
                    )
                ) {
                    Text(
                        text = "WIPE PET DATA",
                        color = Color(0xFFFF003C),
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .border(
                            BorderStroke(1.dp, Brush.linearGradient(listOf(Color(0xFFFF003C), Color.Transparent))),
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) { }
            }

            Spacer(modifier = Modifier.height(32.dp))

            GlowingCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TOTAL XP",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "%,d XP".format(totalXp),
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (currentLevel >= XpEngine.MAX_LEVEL) GoalComplete else appearance.systemAccentColor,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (currentLevel >= XpEngine.MAX_LEVEL) "MAX LEVEL" else "Level $currentLevel",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            PotionInventoryCard(viewModel = viewModel, navController = navController)

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

@Composable
private fun PotionInventoryCard(
    viewModel: PRViewModel,
    navController: NavHostController
) {
    val appearance = LocalAppearance.current
    val inventory by viewModel.potionInventory.collectAsState()
    val canPlay by viewModel.canPlayMiniGame.collectAsState()
    val cooldownMs by viewModel.miniGameCooldownRemainingMs.collectAsState()
    val highScore by viewModel.miniGameHighScore.collectAsState()
    val activePotionType by viewModel.activePotionType.collectAsState()
    var selectedFlaskType by remember { mutableStateOf<PotionType?>(null) }
    var cooldownDisplay by remember { mutableLongStateOf(cooldownMs) }

    LaunchedEffect(Unit) {
        while (true) {
            val ts = viewModel.lastPotionEarnedTimestamp.value
            val remaining = (12 * 60 * 60 * 1000 - (System.currentTimeMillis() - ts)).coerceAtLeast(0L)
            cooldownDisplay = remaining
            if (remaining <= 0) break
            delay(1000L)
        }
    }

    val hours = cooldownDisplay / (1000 * 60 * 60)
    val minutes = (cooldownDisplay % (1000 * 60 * 60)) / (1000 * 60)
    val seconds = (cooldownDisplay % (1000 * 60)) / 1000
    val cooldownText = "%02d:%02d:%02d".format(hours, minutes, seconds)

    val allMaxed = PotionType.entries.all { type ->
        viewModel.potionCount(type) >= type.maxStack
    }

    GlowingCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Text(
                text = "POTION INVENTORY",
                style = MaterialTheme.typography.labelLarge,
                color = appearance.systemAccentColor,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "BEST: $highScore servings",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(PotionType.entries.toList()) { type ->
                    val count = inventory.getOrDefault(type.id, 0)
                    val hasPotion = count > 0
                    val flaskColor = if (hasPotion) Color(type.flaskColor) else TextSecondary.copy(alpha = 0.3f)
                    val isSelected = selectedFlaskType == type
                    val isActive = activePotionType == type

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(80.dp)
                            .clickable {
                                if (hasPotion) {
                                    selectedFlaskType = if (selectedFlaskType == type) null else type
                                }
                            }
                    ) {
                        Box(
                            contentAlignment = Alignment.TopEnd,
                            modifier = Modifier
                                .then(
                                    if (isActive) Modifier.border(
                                        2.dp, GoalComplete, RoundedCornerShape(8.dp)
                                    ) else if (isSelected && hasPotion) Modifier.border(
                                        1.5.dp, flaskColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp)
                                    ) else Modifier
                                )
                                .padding(4.dp)
                        ) {
                            Canvas(
                                modifier = Modifier.size(48.dp)
                            ) {
                                val cx = size.width / 2f
                                val bodyTop = size.height * 0.35f
                                val bodyBottom = size.height * 0.85f
                                val bodyRadius = size.width * 0.35f
                                val neckWidth = size.width * 0.18f
                                val neckTop = size.height * 0.12f
                                val neckBottom = bodyTop + 2.dp.toPx()

                                if (hasPotion) {
                                    drawCircle(
                                        color = flaskColor.copy(alpha = 0.15f),
                                        radius = size.width * 0.48f,
                                        center = Offset(cx, size.height * 0.55f)
                                    )
                                }

                                drawRoundRect(
                                    color = flaskColor.copy(alpha = 0.15f),
                                    topLeft = Offset(cx - bodyRadius, bodyTop),
                                    size = Size(bodyRadius * 2, bodyBottom - bodyTop),
                                    cornerRadius = CornerRadius(bodyRadius * 0.3f)
                                )
                                drawRoundRect(
                                    color = flaskColor,
                                    topLeft = Offset(cx - bodyRadius, bodyTop),
                                    size = Size(bodyRadius * 2, bodyBottom - bodyTop),
                                    cornerRadius = CornerRadius(bodyRadius * 0.3f),
                                    style = Stroke(width = 1.5.dp.toPx())
                                )

                                drawRoundRect(
                                    color = flaskColor,
                                    topLeft = Offset(cx - neckWidth, neckTop),
                                    size = Size(neckWidth * 2, neckBottom - neckTop),
                                    cornerRadius = CornerRadius(neckWidth * 0.2f),
                                    style = Stroke(width = 1.5.dp.toPx())
                                )

                                drawRoundRect(
                                    color = flaskColor,
                                    topLeft = Offset(cx - neckWidth * 1.3f, neckTop - 3.dp.toPx()),
                                    size = Size(neckWidth * 2.6f, 3.dp.toPx()),
                                    cornerRadius = CornerRadius(1.dp.toPx())
                                )

                                if (hasPotion) {
                                    val liquidTop = bodyBottom - (bodyBottom - bodyTop) * 0.7f
                                    drawRoundRect(
                                        color = flaskColor.copy(alpha = 0.4f),
                                        topLeft = Offset(cx - bodyRadius * 0.9f, liquidTop),
                                        size = Size(bodyRadius * 1.8f, bodyBottom - liquidTop - 2.dp.toPx()),
                                        cornerRadius = CornerRadius(bodyRadius * 0.2f)
                                    )
                                }
                            }

                            if (hasPotion) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 2.dp, end = 2.dp)
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(GoalReachedColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = type.badgeLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = type.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (hasPotion) appearance.systemAccentColor else TextSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "x$count",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (hasPotion) appearance.systemAccentColor else TextSecondary.copy(alpha = 0.5f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            if (selectedFlaskType != null) {
                Spacer(modifier = Modifier.height(12.dp))
                val selType = selectedFlaskType!!
                val selCount = inventory.getOrDefault(selType.id, 0)
                if (activePotionType == selType) {
                    NeonButton(
                        text = "ACTIVE — TAP TO CANCEL",
                        onClick = {
                            viewModel.deactivatePotion()
                            selectedFlaskType = null
                        }
                    )
                } else if (selCount > 0) {
                    NeonButton(
                        text = "USE ${selType.displayName}",
                        onClick = { viewModel.activatePotion(selType) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (allMaxed) {
                NeonButton(
                    text = "INVENTORY FULL",
                    onClick = { },
                    enabled = false
                )
            } else if (canPlay) {
                NeonButton(
                    text = "PLAY MINIGAME",
                    onClick = { navController.navigate(Routes.REST_GAME) }
                )
            } else {
                NeonButton(
                    text = "NEXT GAME IN  $cooldownText",
                    onClick = { },
                    enabled = false
                )
            }
        }
    }
}

private fun exportToDownloads(context: Context, viewModel: PRViewModel, exportType: SettingsExportType) {
    try {
        val (json, filename) = when (exportType) {
            SettingsExportType.APP -> Pair(viewModel.generateAppExportJson(), "prtracker_app_backup.json")
            SettingsExportType.PET -> Pair(viewModel.generatePetExportJson(), "prtracker_pets_backup.json")
            SettingsExportType.BOTH -> Pair(viewModel.generateExportJson(), "prtracker_backup.json")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, filename)
                put(MediaStore.Downloads.MIME_TYPE, "application/json")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val uri = context.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                contentValues
            )
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    outputStream.write(json.toByteArray())
                }
                contentValues.clear()
                contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                context.contentResolver.update(it, contentValues, null, null)
            }
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            )
            val destFile = File(downloadsDir, filename)
            destFile.writeText(json)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
