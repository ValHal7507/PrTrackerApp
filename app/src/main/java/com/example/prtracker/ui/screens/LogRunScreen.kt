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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.prtracker.data.RunEntry
import com.example.prtracker.data.RunningPREngine
import com.example.prtracker.navigation.Routes
import com.example.prtracker.ui.components.GlowingCard
import com.example.prtracker.ui.components.GridBackground
import com.example.prtracker.ui.components.PRCelebrationOverlay
import com.example.prtracker.ui.theme.LocalAppearance
import com.example.prtracker.ui.theme.runningAccentColor
import com.example.prtracker.ui.theme.runningSecondaryColor
import com.example.prtracker.ui.theme.Background
import com.example.prtracker.ui.theme.CardBackground
import com.example.prtracker.ui.theme.TextPrimary
import com.example.prtracker.ui.theme.TextSecondary
import com.example.prtracker.viewmodel.PRViewModel
import java.util.UUID

@Composable
fun LogRunScreen(
    viewModel: PRViewModel,
    navController: NavHostController
) {
    val runEntries by viewModel.runEntries.collectAsState()
    val runningPRs by viewModel.runningPRs.collectAsState()
    val appearance = LocalAppearance.current

    var distanceMeters by remember { mutableStateOf(1000f) }
    var hours by remember { mutableIntStateOf(0) }
    var minutes by remember { mutableIntStateOf(10) }
    var seconds by remember { mutableIntStateOf(0) }
    var note by remember { mutableStateOf("") }
    var distanceText by remember { mutableStateOf("1000") }

    var showPRCelebration by remember { mutableStateOf(false) }
    var shouldPop by remember { mutableStateOf(false) }

    val durationSeconds = hours * 3600 + minutes * 60 + seconds
    val currentEntry = remember(distanceMeters, durationSeconds) {
        if (distanceMeters > 0f && durationSeconds > 0) {
            RunEntry(
                id = "",
                distanceMeters = distanceMeters,
                durationSeconds = durationSeconds,
                date = System.currentTimeMillis(),
                note = ""
            )
        } else null
    }
    val isNewPR = currentEntry?.let { viewModel.isNewRunPR(it) } ?: false

    val amberBorderBrush = remember {
        Brush.linearGradient(listOf(appearance.runningAccentColor, appearance.runningSecondaryColor))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GridBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                    text = "LOG RUN",
                    style = MaterialTheme.typography.headlineLarge,
                    color = appearance.runningAccentColor,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // DISTANCE
            GlowingCard(
                modifier = Modifier.fillMaxWidth(),
                borderBrush = amberBorderBrush
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "DISTANCE",
                        style = MaterialTheme.typography.labelLarge,
                        color = appearance.runningAccentColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = { distanceMeters = (distanceMeters - 100f).coerceAtLeast(100f); distanceText = distanceMeters.toInt().toString() },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrease distance",
                                tint = appearance.runningAccentColor
                            )
                        }
                        Text(
                            text = distanceMeters.toInt().toString(),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = MaterialTheme.typography.headlineLarge.fontSize
                            ),
                            color = appearance.runningAccentColor,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.width(120.dp),
                            textAlign = TextAlign.Center
                        )
                        IconButton(
                            onClick = { distanceMeters = (distanceMeters + 100f).coerceAtMost(100000f); distanceText = distanceMeters.toInt().toString() },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase distance",
                                tint = appearance.runningAccentColor
                            )
                        }
                    }
                    Text(
                        text = RunningPREngine.formatDistance(distanceMeters),
                        style = MaterialTheme.typography.bodyMedium,
                        color = appearance.runningSecondaryColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = distanceText,
                        onValueChange = { text ->
                            val filtered = text.filter { it.isDigit() }
                            distanceText = filtered
                            val v = filtered.toIntOrNull()
                            if (v != null && v in 100..100000) {
                                distanceMeters = v.toFloat()
                            }
                        },
                        label = { Text("Meters", color = appearance.runningSecondaryColor) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = appearance.runningAccentColor,
                            focusedBorderColor = appearance.runningAccentColor,
                            unfocusedBorderColor = appearance.runningSecondaryColor.copy(alpha = 0.5f),
                            focusedContainerColor = CardBackground,
                            unfocusedContainerColor = CardBackground
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // DURATION
            GlowingCard(
                modifier = Modifier.fillMaxWidth(),
                borderBrush = amberBorderBrush
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "DURATION",
                        style = MaterialTheme.typography.labelLarge,
                        color = appearance.runningAccentColor
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TimePickerColumn(
                            value = hours,
                            label = "HH",
                            max = 23,
                            onIncrement = { hours = (hours + 1).coerceAtMost(23) },
                            onDecrement = { hours = (hours - 1).coerceAtLeast(0) }
                        )
                        TimePickerColumn(
                            value = minutes,
                            label = "MM",
                            max = 59,
                            onIncrement = { minutes = (minutes + 1).coerceAtMost(59) },
                            onDecrement = { minutes = (minutes - 1).coerceAtLeast(0) }
                        )
                        TimePickerColumn(
                            value = seconds,
                            label = "SS",
                            max = 59,
                            onIncrement = { seconds = (seconds + 1).coerceAtMost(59) },
                            onDecrement = { seconds = (seconds - 1).coerceAtLeast(0) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // LIVE STATS
            if (distanceMeters > 0f && durationSeconds > 0) {
                GlowingCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderBrush = amberBorderBrush
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem(
                                value = RunningPREngine.formatPace(currentEntry?.paceSecondsPerKm ?: 0f),
                                label = "PACE"
                            )
                            StatItem(
                                value = "%.1f km/h".format(currentEntry?.speedKmh ?: 0f),
                                label = "SPEED"
                            )
                            StatItem(
                                value = "~${currentEntry?.caloriesBurned ?: 0} kcal",
                                label = "CALORIES"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // PR PREVIEW
                if (isNewPR) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(appearance.runningAccentColor.copy(alpha = 0.15f))
                            .border(
                                BorderStroke(1.dp, appearance.runningAccentColor),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "NEW PR INCOMING ⚡",
                            style = MaterialTheme.typography.labelMedium,
                            color = appearance.runningAccentColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // NOTE
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional)", color = appearance.runningSecondaryColor) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = appearance.runningAccentColor,
                    focusedBorderColor = appearance.runningAccentColor,
                    unfocusedBorderColor = appearance.runningSecondaryColor.copy(alpha = 0.5f),
                    focusedContainerColor = CardBackground,
                    unfocusedContainerColor = CardBackground
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // LOG RUN BUTTON (amber styled)
            val canLog = distanceMeters > 0f && durationSeconds > 0
            Box(modifier = Modifier.fillMaxWidth()) {
                androidx.compose.material3.Button(
                    onClick = {
                        val entry = RunEntry(
                            id = UUID.randomUUID().toString(),
                            distanceMeters = distanceMeters,
                            durationSeconds = durationSeconds,
                            date = System.currentTimeMillis(),
                            note = note
                        )
                        val pr = viewModel.isNewRunPR(entry)
                        viewModel.addRunEntry(entry)
                        if (pr) {
                            showPRCelebration = true
                            shouldPop = true
                        } else {
                            navController.popBackStack()
                        }
                    },
                    enabled = canLog,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = appearance.runningAccentColor.copy(alpha = 0.15f),
                        disabledContainerColor = appearance.runningAccentColor.copy(alpha = 0.05f)
                    )
                ) {
                    Text(
                        text = "LOG RUN",
                        color = if (canLog) appearance.runningAccentColor else Color.Gray,
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .border(
                            BorderStroke(1.dp, Brush.linearGradient(listOf(appearance.runningAccentColor, Color.Transparent))),
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) { }
            }
        }

        PRCelebrationOverlay(
            visible = showPRCelebration,
            onDismiss = {
                showPRCelebration = false
                if (shouldPop) {
                    navController.popBackStack()
                }
            }
        )
    }
}

@Composable
private fun TimePickerColumn(
    value: Int,
    label: String,
    max: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    val appearance = LocalAppearance.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onIncrement,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Increase $label",
                tint = appearance.runningAccentColor
            )
        }
        Text(
            text = "%02d".format(value),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = MaterialTheme.typography.headlineSmall.fontSize
            ),
            color = appearance.runningAccentColor,
            fontFamily = FontFamily.Monospace
        )
        IconButton(
            onClick = onDecrement,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Decrease $label",
                tint = appearance.runningAccentColor
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = appearance.runningSecondaryColor
        )
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String
) {
    val appearance = LocalAppearance.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = appearance.runningAccentColor,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = appearance.runningSecondaryColor
        )
    }
}
