package com.example.prtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.TextButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.example.prtracker.data.SoundEngine
import com.example.prtracker.data.WeightEntry
import com.example.prtracker.navigation.Routes
import com.example.prtracker.ui.components.GlowingCard
import com.example.prtracker.ui.components.GridBackground
import com.example.prtracker.ui.theme.Background
import com.example.prtracker.ui.theme.GoalComplete
import com.example.prtracker.ui.theme.LocalAppearance
import com.example.prtracker.ui.theme.TextPrimary
import com.example.prtracker.ui.theme.TextSecondary
import com.example.prtracker.ui.theme.systemAccentColor
import com.example.prtracker.viewmodel.PRViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WeightScreen(
    viewModel: PRViewModel,
    navController: NavHostController
) {
    val weightEntries by viewModel.weightEntries.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()
    val appearance = LocalAppearance.current
    val sortedEntries = remember(weightEntries) {
        weightEntries.sortedByDescending { it.date }
    }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingDeleteEntry by remember { mutableStateOf<WeightEntry?>(null) }

    val currentWeight = viewModel.getCurrentWeight()
    val lowestWeight = viewModel.getLowestWeight()
    val highestWeight = viewModel.getHighestWeight()

    Box(modifier = Modifier.fillMaxSize()) {
        GridBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "BODY WEIGHT",
                style = MaterialTheme.typography.displayLarge,
                color = appearance.systemAccentColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, bottom = 16.dp)
                    .drawBehind {
                        drawRoundRect(
                            color = appearance.systemAccentColor.copy(alpha = 0.3f),
                            topLeft = Offset(size.width * 0.1f, size.height - 2.dp.toPx()),
                            size = androidx.compose.ui.geometry.Size(size.width * 0.8f, 2.dp.toPx()),
                            cornerRadius = CornerRadius(1.dp.toPx())
                        )
                    },
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Monospace
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    label = "CURRENT",
                    value = currentWeight?.let { "%.1f".format(it) } ?: "--",
                    unit = appSettings.weightUnit,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "LOWEST",
                    value = lowestWeight?.let { "%.1f".format(it) } ?: "--",
                    unit = appSettings.weightUnit,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "HIGHEST",
                    value = highestWeight?.let { "%.1f".format(it) } ?: "--",
                    unit = appSettings.weightUnit,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            appSettings.targetWeight?.let { target ->
                val diff = currentWeight?.let { it - target }
                if (diff != null) {
                    val absDiff = kotlin.math.abs(diff)
                    val label = if (diff < 0) "%.1f %s to goal".format(absDiff, appSettings.weightUnit)
                    else "%.1f %s above goal".format(absDiff, appSettings.weightUnit)
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (diff <= 0) GoalComplete else TextSecondary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } ?: Spacer(modifier = Modifier.height(0.dp))

            Spacer(modifier = Modifier.height(12.dp))

            if (sortedEntries.size >= 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(horizontal = 16.dp)
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
                                        sortedEntries.sortedBy { it.date }.map {
                                            SimpleDateFormat("MM/dd", Locale.getDefault()).format(Date(it.date))
                                        }
                                    )
                                }

                                axisLeft.apply {
                                    setDrawGridLines(true)
                                    gridColor = android.graphics.Color.argb(40, 0, 245, 255)
                                    textColor = android.graphics.Color.argb(255, 107, 140, 174)
                                }

                                axisRight.isEnabled = false

                                appSettings.targetWeight?.let { target ->
                                    val limitLine = LimitLine(target, "GOAL").apply {
                                        lineColor = android.graphics.Color.argb(255, 0, 255, 133)
                                        lineWidth = 2f
                                        enableDashedLine(10f, 5f, 0f)
                                        textColor = android.graphics.Color.argb(255, 0, 255, 133)
                                    }
                                    axisLeft.addLimitLine(limitLine)
                                }

                                data = createWeightChartData(sortedEntries)
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
                        .padding(horizontal = 16.dp)
                        .background(Background, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (sortedEntries.isEmpty()) "LOG YOUR FIRST WEIGHT\nTO GET STARTED"
                               else "LOG AT LEAST 2 ENTRIES\nTO SEE YOUR CHART",
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "HISTORY",
                style = MaterialTheme.typography.titleLarge,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sortedEntries, key = { it.id }) { entry ->
                    WeightEntryRow(
                        entry = entry,
                        previousWeight = sortedEntries
                            .firstOrNull { e -> e.date < entry.date }?.weight,
                        unit = appSettings.weightUnit,
                        onDelete = {
                            if (appSettings.soundEnabled) SoundEngine.playDeleteEntry()
                            pendingDeleteEntry = entry
                            viewModel.deleteWeightEntry(entry.id)
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "Entry deleted",
                                    actionLabel = "UNDO",
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    pendingDeleteEntry?.let { e ->
                                        viewModel.addWeightEntry(e)
                                    }
                                }
                                pendingDeleteEntry = null
                            }
                        }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { navController.navigate(Routes.LOG_WEIGHT) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = appearance.systemAccentColor.copy(alpha = 0.15f),
            contentColor = appearance.systemAccentColor,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Log weight",
                tint = appearance.systemAccentColor
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = Background,
                contentColor = TextPrimary,
                actionColor = appearance.systemAccentColor
            )
        }
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    val appearance = LocalAppearance.current
    GlowingCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 22.sp
                ),
                color = appearance.systemAccentColor,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = appearance.systemAccentColor.copy(alpha = 0.7f),
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun WeightEntryRow(
    entry: WeightEntry,
    previousWeight: Float?,
    unit: String,
    onDelete: () -> Unit
) {
    val appearance = LocalAppearance.current
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    GlowingCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
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
            }

            val diffIcon = if (previousWeight != null) {
                if (entry.weight > previousWeight) Icons.Default.ArrowUpward
                else if (entry.weight < previousWeight) Icons.Default.ArrowDownward
                else null
            } else null

            if (diffIcon != null) {
                Icon(
                    imageVector = diffIcon,
                    contentDescription = null,
                    tint = GoalComplete,
                    modifier = Modifier.size(18.dp)
                )
            } else if (previousWeight != null) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "%.1f".format(entry.weight),
                style = MaterialTheme.typography.titleLarge,
                color = appearance.systemAccentColor,
                fontFamily = FontFamily.Monospace
            )

            Text(
                text = unit,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(start = 2.dp)
            )

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
            text = { Text("Delete this weight entry (${"%.1f".format(entry.weight)} $unit)?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete()
                }) {
                    Text("Delete", color = appearance.systemAccentColor)
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

private fun createWeightChartData(entries: List<WeightEntry>): LineData {
    val sorted = entries.sortedBy { it.date }
    val chartEntries = sorted.mapIndexed { index, entry ->
        Entry(index.toFloat(), entry.weight)
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
