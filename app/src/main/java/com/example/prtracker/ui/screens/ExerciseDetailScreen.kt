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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.example.prtracker.data.ExerciseDifficulty
import com.example.prtracker.data.PREntry
import com.example.prtracker.data.SoundEngine
import com.example.prtracker.data.parsedDifficulty
import com.example.prtracker.navigation.Routes
import com.example.prtracker.ui.components.GlowingCard
import com.example.prtracker.ui.components.GridBackground
import com.example.prtracker.ui.components.NeonButton
import com.example.prtracker.ui.theme.Background
import com.example.prtracker.ui.theme.GoalComplete
import com.example.prtracker.ui.theme.LocalAppearance
import com.example.prtracker.ui.theme.TextPrimary
import com.example.prtracker.ui.theme.TextSecondary
import com.example.prtracker.ui.theme.exerciseAccentColor
import com.example.prtracker.ui.theme.exerciseSecondaryColor
import com.example.prtracker.ui.theme.pinnedAccentColor
import com.example.prtracker.viewmodel.PRViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExerciseDetailScreen(
    viewModel: PRViewModel,
    navController: NavHostController,
    exerciseId: String
) {
    val exercises by viewModel.exercises.collectAsState()
    val exercise = exercises.find { it.id == exerciseId }
    val appearance = LocalAppearance.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }

    if (exercise == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Exercise not found", color = TextSecondary)
        }
        return
    }

    val sortedEntries = exercise.entries.sortedByDescending { it.date }
    val prValues = mutableListOf<Int>()
    var runningMax = 0
    for (entry in sortedEntries.reversed()) {
        if (entry.value > runningMax) {
            runningMax = entry.value
        }
        prValues.add(runningMax)
    }
    val prSet = sortedEntries.reversed().map { it.value }.toSet()
    val prValuesReversed = prValues.reversed()

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Exercise", color = TextPrimary) },
            text = { Text("Are you sure you want to delete \"${exercise.name}\"? This cannot be undone.", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteExercise(exerciseId)
                    showDeleteDialog = false
                    navController.popBackStack()
                }) {
                    Text("Delete", color = appearance.exerciseAccentColor)
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

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Exercise", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = appearance.exerciseAccentColor,
                        focusedBorderColor = appearance.exerciseAccentColor,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                        focusedContainerColor = Background,
                        unfocusedContainerColor = Background
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.renameExercise(exerciseId, renameText.trim())
                        showRenameDialog = false
                    },
                    enabled = renameText.isNotBlank() && renameText.trim() != exercise.name
                ) {
                    Text("Rename", color = appearance.exerciseAccentColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = Background
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GridBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 24.dp, end = 24.dp, top = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = appearance.exerciseAccentColor)
                    }

                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = appearance.exerciseAccentColor,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    val diffColor = when (exercise.parsedDifficulty()) {
                        ExerciseDifficulty.EASY -> Color(0xFF00FF85)
                        ExerciseDifficulty.MEDIUM -> appearance.exerciseAccentColor
                        ExerciseDifficulty.HARD -> Color(0xFFFF6B00)
                        ExerciseDifficulty.EXTREME -> Color(0xFFFF003C)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(diffColor.copy(alpha = 0.2f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = exercise.difficulty,
                            style = MaterialTheme.typography.labelSmall,
                            color = diffColor,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(onClick = {
                        renameText = exercise.name
                        showRenameDialog = true
                    }) {
                        Icon(Icons.Default.Edit, "Rename", tint = appearance.exerciseAccentColor.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                    }

                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, "Delete", tint = appearance.exerciseAccentColor.copy(alpha = 0.6f))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (sortedEntries.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Background, RoundedCornerShape(16.dp))
                    ) {
                        AndroidView(
                            factory = { context ->
                                LineChart(context).apply {
                                    description.isEnabled = false
                                    legend.isEnabled = false
                                    setTouchEnabled(true)
                                    isDragEnabled = true
                                    setScaleEnabled(true)
                                    setPinchZoom(true)
                                    setBackgroundColor(android.graphics.Color.TRANSPARENT)

                                    xAxis.apply {
                                        position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                                        setDrawGridLines(false)
                                        textColor = android.graphics.Color.argb(255, 107, 140, 174)
                                        setDrawLabels(true)
                                        granularity = 1f
                                        valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(
                                            exercise.entries.map {
                                                SimpleDateFormat("MM/dd", Locale.getDefault()).format(Date(it.date))
                                            }
                                        )
                                    }

                                    axisLeft.apply {
                                        setDrawGridLines(true)
                                        gridColor = android.graphics.Color.argb(40, 0, 245, 255)
                                        textColor = android.graphics.Color.argb(255, 107, 140, 174)
                                        axisMinimum = 0f
                                    }

                                    axisRight.isEnabled = false

                                    data = createChartData(exercise.entries)
                                    animateX(800)
                                    invalidate()
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Background, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No entries yet.\nLog your first entry from the dashboard.",
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "HISTORY",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    sortedEntries.forEachIndexed { index, entry ->
                        val isPR = entry.value > (sortedEntries.drop(index + 1).maxOfOrNull { it.value } ?: -1)
                        EntryRow(
                            entry = entry,
                            isPR = isPR,
                            type = exercise.type,
                            onDelete = {
                                if (viewModel.appSettings.value.soundEnabled) SoundEngine.playDeleteEntry()
                                viewModel.deleteEntry(exerciseId, entry.id)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
            ) {
                if (exercise.type == "hold") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        NeonButton(
                            text = "LOG ENTRY",
                            onClick = { navController.navigate(Routes.logEntry(exerciseId)) },
                            modifier = Modifier.weight(1f)
                        )
                        TimerButton(
                            onClick = { navController.navigate(Routes.holdTimer(exerciseId)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    NeonButton(
                        text = "LOG ENTRY",
                        onClick = { navController.navigate(Routes.logEntry(exerciseId)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TimerButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appearance = LocalAppearance.current
    Box(modifier = modifier.fillMaxWidth()) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = appearance.exerciseAccentColor.copy(alpha = 0.15f)
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = appearance.exerciseAccentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "TIMER",
                    color = appearance.exerciseAccentColor,
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    BorderStroke(1.dp, Brush.linearGradient(listOf(appearance.exerciseAccentColor, Color.Transparent))),
                    RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) { }
    }
}

@Composable
private fun EntryRow(entry: PREntry, isPR: Boolean, type: String, onDelete: () -> Unit) {
    val appearance = LocalAppearance.current
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    GlowingCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dateFormat.format(Date(entry.date)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                if (entry.note.isNotBlank()) {
                    Text(
                        text = entry.note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }
                if (entry.xpEarned > 0L) {
                    Text(
                        text = "+${entry.xpEarned} XP",
                        style = MaterialTheme.typography.bodySmall,
                        color = GoalComplete,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Text(
                text = if (type == "reps") "${entry.value} reps" else "${entry.value}s",
                style = MaterialTheme.typography.titleLarge,
                color = if (isPR) appearance.pinnedAccentColor else appearance.exerciseAccentColor,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            if (isPR) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "PR",
                    tint = appearance.pinnedAccentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete entry",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Entry", color = TextPrimary) },
            text = { Text("Delete this entry (${entry.value}${if (type == "hold") "s" else " reps"})?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete()
                }) {
                    Text("Delete", color = appearance.exerciseAccentColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = Background
        )
    }
}

private fun createChartData(entries: List<PREntry>): LineData {
    val sorted = entries.sortedBy { it.date }
    val chartEntries = sorted.mapIndexed { index, entry ->
        Entry(index.toFloat(), entry.value.toFloat())
    }

    val dataSet = LineDataSet(chartEntries, "").apply {
        color = android.graphics.Color.argb(255, 0, 245, 255)
        valueTextColor = android.graphics.Color.argb(255, 107, 140, 174)
        setDrawValues(false)
        setDrawCircles(true)
        circleRadius = 5f
        circleColors = listOf(android.graphics.Color.argb(255, 0, 245, 255))
        setDrawCircleHole(true)
        circleHoleColor = android.graphics.Color.argb(255, 5, 10, 24)
        circleHoleRadius = 2.5f
        lineWidth = 2f
        mode = LineDataSet.Mode.CUBIC_BEZIER
        setDrawFilled(true)
        fillColor = android.graphics.Color.argb(255, 0, 245, 255)
        fillAlpha = 30
    }

    return LineData(dataSet)
}
