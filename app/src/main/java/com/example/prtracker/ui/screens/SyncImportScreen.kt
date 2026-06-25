package com.example.prtracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.prtracker.data.StorageData
import com.example.prtracker.navigation.Routes
import com.example.prtracker.ui.components.GlowingCard
import com.example.prtracker.ui.components.GridBackground
import com.example.prtracker.ui.components.NeonButton
import com.example.prtracker.ui.theme.GoalComplete
import com.example.prtracker.ui.theme.PinnedAccentSecondary
import com.example.prtracker.ui.theme.PrimaryAccent
import com.example.prtracker.ui.theme.TextPrimary
import com.example.prtracker.ui.theme.TextSecondary
import com.example.prtracker.viewmodel.PRViewModel
import com.google.gson.Gson

private enum class ImportFormat { APP, PET, COMBINED }

@Composable
fun SyncImportScreen(
    viewModel: PRViewModel,
    navController: NavHostController
) {
    val gson = remember { Gson() }
    var parsedStorageData by remember { mutableStateOf<StorageData?>(null) }
    var parsedPetData by remember { mutableStateOf<com.example.prtracker.data.PetStorageData?>(null) }
    var detectedFormat by remember { mutableStateOf<ImportFormat?>(null) }
    var parseError by remember { mutableStateOf<String?>(null) }
    var showSuccess by remember { mutableStateOf(false) }

    val json = remember { viewModel.consumePendingImportJson() }

    LaunchedEffect(json) {
        if (json != null && detectedFormat == null && parseError == null) {
            try {
                val hasExercises = json.contains("\"exercises\"")
                val hasPetInventory = json.contains("\"petInventory\"")
                when {
                    hasExercises && hasPetInventory -> {
                        parsedStorageData = gson.fromJson(json, StorageData::class.java)
                        detectedFormat = ImportFormat.COMBINED
                    }
                    hasExercises -> {
                        parsedStorageData = gson.fromJson(json, StorageData::class.java)
                        detectedFormat = ImportFormat.APP
                    }
                    hasPetInventory -> {
                        parsedPetData = gson.fromJson(json, com.example.prtracker.data.PetStorageData::class.java)
                        detectedFormat = ImportFormat.PET
                    }
                    else -> {
                        parseError = "Unrecognized data format"
                    }
                }
            } catch (e: Exception) {
                parseError = "Invalid JSON file: ${e.message}"
            }
        }
    }

    if (showSuccess) {
        Box(modifier = Modifier.fillMaxSize()) {
            GridBackground()
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "\u2713",
                    style = MaterialTheme.typography.displayLarge,
                    color = GoalComplete
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "IMPORT COMPLETE",
                    style = MaterialTheme.typography.displayLarge,
                    color = GoalComplete,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(12.dp))
                when (detectedFormat) {
                    ImportFormat.APP -> {
                        val d = parsedStorageData
                        if (d != null) {
                            Text(
                                text = "${d.exercises.size} exercises \u00b7 ${d.goals.size} goals \u00b7 ${d.weightEntries.size} weight \u00b7 ${d.workoutPresets.size} presets \u00b7 ${d.runEntries.size} runs \u00b7 ${d.workoutHistory.size} workouts",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    ImportFormat.PET -> {
                        val d = parsedPetData
                        if (d != null) {
                            Text(
                                text = "${d.petInventory.size} pets \u00b7 ${d.coins} coins \u00b7 ${d.totalRolls} rolls",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    ImportFormat.COMBINED -> Text(
                        text = "All app data + pet data",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    null -> {}
                }
                Spacer(modifier = Modifier.height(32.dp))
                NeonButton(
                    text = "GO TO DASHBOARD",
                    onClick = {
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
        return
    }

    if (parseError != null) {
        Box(modifier = Modifier.fillMaxSize()) {
            GridBackground()
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "IMPORT FAILED",
                    style = MaterialTheme.typography.displayLarge,
                    color = PinnedAccentSecondary,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = parseError!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                NeonButton(
                    text = "GO BACK",
                    onClick = { navController.popBackStack() }
                )
            }
        }
        return
    }

    val format = detectedFormat
    if (format != null) {
        Box(modifier = Modifier.fillMaxSize()) {
            GridBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    text = "IMPORT DATA",
                    style = MaterialTheme.typography.displayLarge,
                    color = PrimaryAccent,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            drawRoundRect(
                                color = PrimaryAccent.copy(alpha = 0.3f),
                                topLeft = Offset(size.width * 0.1f, size.height - 2.dp.toPx()),
                                size = androidx.compose.ui.geometry.Size(size.width * 0.8f, 2.dp.toPx()),
                                cornerRadius = CornerRadius(1.dp.toPx())
                            )
                        },
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                val formatLabel = when (format) {
                    ImportFormat.APP -> "App data detected"
                    ImportFormat.PET -> "Pet data detected"
                    ImportFormat.COMBINED -> "Combined data detected"
                }
                Text(
                    text = formatLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                GlowingCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "DATA SUMMARY",
                            style = MaterialTheme.typography.labelLarge,
                            color = PrimaryAccent,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        when (format) {
                            ImportFormat.APP -> {
                                val d = parsedStorageData!!
                                Text(
                                    text = "${d.exercises.size} exercises \u00b7 ${d.goals.size} goals \u00b7 ${d.weightEntries.size} weight \u00b7 ${d.workoutPresets.size} presets \u00b7 ${d.runEntries.size} runs \u00b7 ${d.workoutHistory.size} workouts",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    textAlign = TextAlign.Center
                                )
                            }
                            ImportFormat.PET -> {
                                val d = parsedPetData!!
                                Text(
                                    text = "${d.petInventory.size} pets \u00b7 ${d.coins} coins \u00b7 ${d.totalRolls} rolls \u00b7 ${d.petUpgrades.size} upgrades",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    textAlign = TextAlign.Center
                                )
                            }
                            ImportFormat.COMBINED -> {
                                val d = parsedStorageData!!
                                Text(
                                    text = "${d.exercises.size} exercises \u00b7 ${d.goals.size} goals \u00b7 ${d.weightEntries.size} weight \u00b7 ${d.workoutPresets.size} presets \u00b7 ${d.runEntries.size} runs \u00b7 ${d.workoutHistory.size} workouts",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${d.petInventory.size} pets \u00b7 ${d.coins} coins \u00b7 ${d.totalRolls} rolls",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        when (format) {
                            ImportFormat.APP -> viewModel.importAppData(json!!, PRViewModel.SyncMode.REPLACE)
                            ImportFormat.PET -> viewModel.importPetData(json!!, PRViewModel.SyncMode.REPLACE)
                            ImportFormat.COMBINED -> viewModel.importSyncData(parsedStorageData!!, PRViewModel.SyncMode.REPLACE)
                        }
                        showSuccess = true
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PinnedAccentSecondary.copy(alpha = 0.2f)
                    )
                ) {
                    Text(
                        text = "REPLACE ALL",
                        color = PinnedAccentSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        when (format) {
                            ImportFormat.APP -> viewModel.importAppData(json!!, PRViewModel.SyncMode.MERGE)
                            ImportFormat.PET -> viewModel.importPetData(json!!, PRViewModel.SyncMode.MERGE)
                            ImportFormat.COMBINED -> viewModel.importSyncData(parsedStorageData!!, PRViewModel.SyncMode.MERGE)
                        }
                        showSuccess = true
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryAccent.copy(alpha = 0.15f)
                    )
                ) {
                    Text(
                        text = "MERGE",
                        color = PrimaryAccent,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                TextButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "CANCEL",
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    } else if (json == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            GridBackground()
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "NO DATA",
                    style = MaterialTheme.typography.displayLarge,
                    color = TextSecondary,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Open a prtracker_backup.json, prtracker_app_backup.json, or prtracker_pets_backup.json file from your file manager to import data.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                NeonButton(
                    text = "GO BACK",
                    onClick = { navController.popBackStack() }
                )
            }
        }
    }
}
