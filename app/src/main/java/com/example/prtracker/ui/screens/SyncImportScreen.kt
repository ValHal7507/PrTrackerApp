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

@Composable
fun SyncImportScreen(
    viewModel: PRViewModel,
    navController: NavHostController
) {
    val gson = remember { Gson() }
    var parsedData by remember { mutableStateOf<StorageData?>(null) }
    var parseError by remember { mutableStateOf<String?>(null) }
    var showSuccess by remember { mutableStateOf(false) }

    val json = viewModel.consumePendingImportJson()

    LaunchedEffect(json) {
        if (json != null && parsedData == null && parseError == null) {
            try {
                parsedData = gson.fromJson(json, StorageData::class.java)
            } catch (e: Exception) {
                parseError = "Invalid JSON file: ${e.message}"
            }
        }
    }

    if (showSuccess) {
        val importedExercises = viewModel.exercises.value.size
        val importedGoals = viewModel.goals.value.size
        val importedWeight = viewModel.weightEntries.value.size
        val importedPresets = viewModel.workoutPresets.value.size
        val importedRuns = viewModel.runEntries.value.size
        val importedHistory = viewModel.workoutHistory.value.size

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
                Text(
                    text = "$importedExercises exercises \u00b7 $importedGoals goals \u00b7 $importedWeight weight \u00b7 $importedPresets presets \u00b7 $importedRuns runs \u00b7 $importedHistory workouts",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
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

    val data = parsedData
    if (data != null) {
        val exercisesIncoming = data.exercises.size
        val goalsIncoming = data.goals.size
        val weightIncoming = data.weightEntries.size
        val presetsIncoming = data.workoutPresets.size
        val runsIncoming = data.runEntries.size
        val historyIncoming = data.workoutHistory.size

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

                Text(
                    text = "Choose how to add this data",
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
                        Text(
                            text = "$exercisesIncoming exercises \u00b7 $goalsIncoming goals \u00b7 $weightIncoming weight \u00b7 $presetsIncoming presets \u00b7 $runsIncoming runs \u00b7 $historyIncoming workouts",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        viewModel.importSyncData(data, PRViewModel.SyncMode.REPLACE)
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
                        viewModel.importSyncData(data, PRViewModel.SyncMode.MERGE)
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
                    text = "Open a prtracker_backup.json file from your file manager to import data.",
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
