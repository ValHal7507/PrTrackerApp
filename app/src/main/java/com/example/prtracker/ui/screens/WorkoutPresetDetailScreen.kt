package com.example.prtracker.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.prtracker.data.PresetExercise
import com.example.prtracker.navigation.Routes
import com.example.prtracker.ui.components.GridBackground
import com.example.prtracker.ui.theme.Background
import com.example.prtracker.ui.theme.GoalComplete
import com.example.prtracker.ui.theme.LocalAppearance
import com.example.prtracker.ui.theme.PinnedAccentSecondary
import com.example.prtracker.ui.theme.Surface
import com.example.prtracker.ui.theme.TextPrimary
import com.example.prtracker.ui.theme.TextSecondary
import com.example.prtracker.ui.theme.pinnedSecondaryColor
import com.example.prtracker.ui.theme.systemAccentColor
import com.example.prtracker.ui.theme.systemSecondaryColor
import com.example.prtracker.viewmodel.PRViewModel

@Composable
fun WorkoutPresetDetailScreen(
    presetId: String,
    viewModel: PRViewModel,
    navController: NavHostController
) {
    val presets by viewModel.workoutPresets.collectAsState()
    val preset = presets.find { it.id == presetId }
    val activeSession by viewModel.activeSession.collectAsState()
    val appearance = LocalAppearance.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    val editBrush = remember { Brush.linearGradient(listOf(appearance.systemAccentColor, Color.Transparent)) }
    val deleteBrush = remember { Brush.linearGradient(listOf(PinnedAccentSecondary, Color.Transparent)) }

    if (preset == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            GridBackground()
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = appearance.systemAccentColor
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "PRESET NOT FOUND",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextSecondary,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GridBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = appearance.systemAccentColor
                    )
                }
                Text(
                    text = preset.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = appearance.systemAccentColor,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                IconButton(
                    onClick = {
                        navController.navigate("${Routes.PRESETS}?editId=$presetId") {
                            popUpTo(Routes.PRESETS) { inclusive = true }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit preset",
                        tint = appearance.systemAccentColor
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                val diffColor = difficultyColor(preset.difficulty, appearance)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, diffColor, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(Surface)
                        .padding(16.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(diffColor.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = preset.difficulty,
                                style = MaterialTheme.typography.labelMedium,
                                color = diffColor,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        if (preset.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = preset.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "EXERCISES",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(TextSecondary.copy(alpha = 0.2f))
                )

                Spacer(modifier = Modifier.height(12.dp))

                preset.exercises.forEachIndexed { index, ex ->
                    PresetExerciseDetailRow(
                        exercise = ex,
                        diffColor = diffColor,
                        appearance = appearance
                    )
                    if (index < preset.exercises.lastIndex) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(TextSecondary.copy(alpha = 0.1f))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            navController.navigate("${Routes.PRESETS}?editId=$presetId") {
                                popUpTo(Routes.PRESETS) { inclusive = true }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = appearance.systemAccentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "EDIT PRESET",
                            color = appearance.systemAccentColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .border(BorderStroke(1.dp, editBrush), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) { }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    val hasActiveSession = activeSession != null && !activeSession!!.isCompleted
                    Button(
                        onClick = {
                            if (hasActiveSession) {
                                navController.navigate(Routes.workoutSession(activeSession!!.presetId))
                            } else {
                                viewModel.startWorkout(preset)
                                navController.navigate(Routes.workoutSession(presetId))
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color(0xFFFF2D78),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (hasActiveSession) "RESUME WORKOUT" else "START WORKOUT",
                            color = Color(0xFFFF2D78),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .border(BorderStroke(1.dp, Color(0xFFFF2D78)), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {}
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Text(
                            text = "DELETE PRESET",
                            color = PinnedAccentSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .border(BorderStroke(1.dp, deleteBrush), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) { }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Preset", color = TextPrimary) },
            text = { Text("Delete preset \"${preset.name}\"?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletePreset(preset.id)
                    showDeleteDialog = false
                    navController.popBackStack()
                }) {
                    Text("Delete", color = PinnedAccentSecondary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = Background
        )
    }
}

@Composable
private fun PresetExerciseDetailRow(
    exercise: PresetExercise,
    diffColor: Color,
    appearance: com.example.prtracker.data.AppearanceSettings
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = exercise.exerciseName,
            style = MaterialTheme.typography.titleMedium,
            color = appearance.systemAccentColor,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(appearance.systemSecondaryColor.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${exercise.sets} SETS",
                    style = MaterialTheme.typography.labelSmall,
                    color = appearance.systemSecondaryColor,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(appearance.systemAccentColor.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (exercise.isUntilFailure) "UNTIL FAILURE" else if (exercise.targetHoldSeconds > 0) "${exercise.targetHoldSeconds}s HOLD" else "${exercise.targetReps} REPS",
                    style = MaterialTheme.typography.labelSmall,
                    color = appearance.systemAccentColor,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }
        }
        if (exercise.notes.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = exercise.notes,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

private fun difficultyColor(difficulty: String, appearance: com.example.prtracker.data.AppearanceSettings): Color {
    return when (difficulty) {
        "EASY" -> GoalComplete
        "MEDIUM" -> appearance.systemAccentColor
        "HARD" -> Color(0xFFFF8C00)
        "MAX EFFORT" -> appearance.pinnedSecondaryColor
        else -> appearance.systemAccentColor
    }
}
