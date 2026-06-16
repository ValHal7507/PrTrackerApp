package com.example.prtracker.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsRun
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.prtracker.data.RunEntry
import com.example.prtracker.navigation.Routes
import com.example.prtracker.data.RunningPREngine
import com.example.prtracker.ui.components.GlowingCard
import com.example.prtracker.ui.components.GridBackground
import com.example.prtracker.ui.theme.LocalAppearance
import com.example.prtracker.ui.theme.runningAccentColor
import com.example.prtracker.ui.theme.runningSecondaryColor
import com.example.prtracker.ui.theme.Background
import com.example.prtracker.ui.theme.TextPrimary
import com.example.prtracker.ui.theme.TextSecondary
import com.example.prtracker.viewmodel.PRViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RunHistoryScreen(
    viewModel: PRViewModel,
    navController: NavHostController
) {
    val runEntries by viewModel.runEntries.collectAsState()
    val runningPRs by viewModel.runningPRs.collectAsState()

    val appearance = LocalAppearance.current

    var deleteConfirmId by remember { mutableStateOf<String?>(null) }

    val amberBorderBrush = remember {
        Brush.linearGradient(listOf(appearance.runningAccentColor, appearance.runningSecondaryColor))
    }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    val bestPace = runningPRs.bestPaceSecondsPerKm
    val bestDistance = runningPRs.bestDistanceMeters
    val bestDuration = runningPRs.bestDurationSeconds
    val best500m = runningPRs.best500mSeconds
    val best1km = runningPRs.best1kmSeconds
    val best2km = runningPRs.best2kmSeconds
    val best5km = runningPRs.best5kmSeconds
    val best10km = runningPRs.best10kmSeconds
    val bestCalories = runningPRs.bestCaloriesInRun

    Box(modifier = Modifier.fillMaxSize()) {
        GridBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = appearance.runningAccentColor
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "RUN LOG",
                    style = MaterialTheme.typography.headlineLarge,
                    color = appearance.runningAccentColor,
                    fontFamily = FontFamily.Monospace
                )
            }

            // LIVE RUN BUTTON
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                val magenta = Color(0xFFFF2D78)
                val borderBrush = remember { Brush.linearGradient(listOf(magenta, Color.Transparent)) }
                Button(
                    onClick = { navController.navigate(Routes.LIVE_RUN) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = magenta.copy(alpha = 0.15f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsRun,
                        contentDescription = "Live Run",
                        tint = magenta,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LIVE RUN",
                        color = magenta,
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .border(BorderStroke(1.dp, borderBrush), RoundedCornerShape(16.dp))
                ) {}
            }

            Spacer(modifier = Modifier.height(8.dp))

            // PR BADGES
            GlowingCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                borderBrush = amberBorderBrush
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        text = "ALL-TIME RECORDS",
                        style = MaterialTheme.typography.labelLarge,
                        color = appearance.runningAccentColor,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PRBadge(
                            label = "BEST PACE",
                            value = if (bestPace != null)
                                RunningPREngine.formatPace(bestPace) else "—"
                        )
                        PRBadge(
                            label = "BEST DISTANCE",
                            value = if (bestDistance != null)
                                RunningPREngine.formatDistance(bestDistance) else "—"
                        )
                        PRBadge(
                            label = "BEST DURATION",
                            value = if (bestDuration != null)
                                RunningPREngine.formatDuration(bestDuration) else "—"
                        )
                        PRBadge(
                            label = "BEST 500M",
                            value = if (best500m != null)
                                RunningPREngine.formatDuration(best500m) else "—"
                        )
                        PRBadge(
                            label = "BEST 1KM",
                            value = if (best1km != null)
                                RunningPREngine.formatDuration(best1km) else "—"
                        )
                        PRBadge(
                            label = "BEST 2KM",
                            value = if (best2km != null)
                                RunningPREngine.formatDuration(best2km) else "—"
                        )
                        PRBadge(
                            label = "BEST 5KM",
                            value = if (best5km != null)
                                RunningPREngine.formatDuration(best5km) else "—"
                        )
                        PRBadge(
                            label = "BEST 10KM",
                            value = if (best10km != null)
                                RunningPREngine.formatDuration(best10km) else "—"
                        )
                        PRBadge(
                            label = "BEST CALORIES",
                            value = if (bestCalories != null)
                                "${bestCalories} kcal" else "—"
                        )
                        PRBadge(
                            label = "TOTAL DISTANCE",
                            value = RunningPREngine.formatDistance(runningPRs.totalDistanceMeters)
                        )
                        PRBadge(
                            label = "TOTAL RUNS",
                            value = runningPRs.totalRuns.toString()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ENTRY LIST
            if (runEntries.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NO RUNS LOGGED YET\n— HIT THE PAVEMENT —",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(runEntries, key = { it.id }) { entry ->
                        RunEntryCard(
                            entry = entry,
                            dateFormat = dateFormat,
                            onDelete = { deleteConfirmId = entry.id }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        // DELETE CONFIRMATION
        deleteConfirmId?.let { id ->
            val entry = runEntries.find { it.id == id }
            if (entry != null) {
                AlertDialog(
                    onDismissRequest = { deleteConfirmId = null },
                    title = { Text("Delete Entry", color = TextPrimary) },
                    text = {
                        Text(
                            "Delete run: ${RunningPREngine.formatDistance(entry.distanceMeters)} in ${RunningPREngine.formatDuration(entry.durationSeconds)}?",
                            color = TextSecondary
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.deleteRunEntry(id)
                            deleteConfirmId = null
                        }) {
                            Text("Delete", color = appearance.runningAccentColor)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { deleteConfirmId = null }) {
                            Text("Cancel", color = TextSecondary)
                        }
                    },
                    containerColor = Background
                )
            }
        }
    }
}

@Composable
private fun PRBadge(
    label: String,
    value: String
) {
    val appearance = LocalAppearance.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(appearance.runningAccentColor.copy(alpha = 0.1f))
            .border(1.dp, appearance.runningAccentColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = appearance.runningSecondaryColor
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = appearance.runningAccentColor,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun RunEntryCard(
    entry: RunEntry,
    dateFormat: java.text.SimpleDateFormat,
    onDelete: () -> Unit
) {
    val appearance = LocalAppearance.current
    val amberBorderBrush = remember {
        Brush.linearGradient(listOf(appearance.runningAccentColor, appearance.runningSecondaryColor))
    }

    GlowingCard(
        modifier = Modifier.fillMaxWidth(),
        borderBrush = amberBorderBrush
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dateFormat.format(entry.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Text(
                    text = RunningPREngine.formatDistance(entry.distanceMeters),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = MaterialTheme.typography.headlineSmall.fontSize
                    ),
                    color = appearance.runningAccentColor,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = RunningPREngine.formatDuration(entry.durationSeconds),
                    style = MaterialTheme.typography.bodySmall,
                    color = appearance.runningSecondaryColor
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    text = RunningPREngine.formatPace(entry.paceSecondsPerKm),
                    style = MaterialTheme.typography.bodyMedium,
                    color = appearance.runningAccentColor,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "%.1f km/h".format(entry.speedKmh),
                    style = MaterialTheme.typography.labelSmall,
                    color = appearance.runningSecondaryColor
                )
                Text(
                    text = "~${entry.caloriesBurned} kcal",
                    style = MaterialTheme.typography.labelSmall,
                    color = appearance.runningSecondaryColor
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete run entry",
                    tint = appearance.runningSecondaryColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
