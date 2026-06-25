package com.example.prtracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.prtracker.data.PREntry
import com.example.prtracker.data.PotionType
import com.example.prtracker.data.SoundEngine
import com.example.prtracker.data.XpEngine
import com.example.prtracker.data.parsedDifficulty
import com.example.prtracker.navigation.Routes
import com.example.prtracker.ui.components.GridBackground
import com.example.prtracker.ui.components.NeonButton
import com.example.prtracker.ui.components.PRCelebrationOverlay
import com.example.prtracker.ui.theme.Background
import com.example.prtracker.ui.theme.GoalComplete
import com.example.prtracker.ui.theme.GoalReachedColor
import com.example.prtracker.ui.theme.PrimaryAccent
import com.example.prtracker.ui.theme.SuccessPurple
import com.example.prtracker.ui.theme.TextPrimary
import com.example.prtracker.ui.theme.TextSecondary
import com.example.prtracker.viewmodel.PRViewModel
import kotlinx.coroutines.delay
import java.util.UUID

@Composable
fun LogEntryScreen(
    viewModel: PRViewModel,
    navController: NavHostController,
    exerciseId: String
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val appSettings by viewModel.appSettings.collectAsState()
    val exercise = viewModel.getExerciseById(exerciseId)
    var value by remember { mutableStateOf(0) }
    var note by remember { mutableStateOf("") }
    var showCelebration by remember { mutableStateOf(false) }
    var isNewPR by remember { mutableStateOf(false) }
    var showBonusXp by remember { mutableStateOf(false) }
    var bonusXpAmount by remember { mutableStateOf(0L) }

    val hasPotion = viewModel.activePotionType.collectAsState().value == PotionType.XP_DOUBLE
    val petMult = viewModel.petXpMultiplier()
    val entryXp = if (value > 0 && exercise != null) XpEngine.xpForEntry(value, exercise.type, exercise.parsedDifficulty()) else 0L
    val displayXp = if (value > 0) (entryXp * petMult * (if (hasPotion) 2 else 1)).toLong() else 0L

    LaunchedEffect(Unit) {
        viewModel.lastBonusXpEarned.collect { xp ->
            bonusXpAmount = xp
            showBonusXp = true
        }
    }

    LaunchedEffect(showBonusXp) {
        if (showBonusXp) {
            delay(1500L)
            showBonusXp = false
        }
    }

    if (exercise == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Exercise not found", color = TextSecondary)
        }
        return
    }

    val currentPR = viewModel.getCurrentPR(exerciseId)
    val valueColor by animateColorAsState(
        targetValue = if (isNewPR) SuccessPurple else PrimaryAccent,
        animationSpec = tween(300)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (showCelebration) Modifier.blur(12.dp) else Modifier
                )
        ) {
            GridBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, "Back", tint = PrimaryAccent)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = exercise.name,
                style = MaterialTheme.typography.headlineMedium,
                color = PrimaryAccent
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isNewPR) {
                Text(
                    text = "NEW PR!",
                    style = MaterialTheme.typography.titleMedium,
                    color = SuccessPurple,
                    modifier = Modifier
                        .drawBehind {
                            drawRoundRect(
                                color = SuccessPurple.copy(alpha = 0.2f),
                                cornerRadius = CornerRadius(8.dp.toPx()),
                                size = androidx.compose.ui.geometry.Size(size.width, size.height)
                            )
                        }
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (exercise.type == "reps") "$value" else "${value}s",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 64.sp
                    ),
                    color = valueColor,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (value > 0) {
                            value--
                            isNewPR = viewModel.isNewPR(exerciseId, value)
                        }
                    },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        "Decrease",
                        tint = TextSecondary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.size(32.dp))

                OutlinedTextField(
                    value = if (value == 0) "" else value.toString(),
                    onValueChange = { text ->
                        value = text.filter { it.isDigit() }.takeIf { it.isNotEmpty() }?.toInt() ?: 0
                        isNewPR = viewModel.isNewPR(exerciseId, value)
                    },
                    modifier = Modifier.width(100.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.headlineLarge.copy(
                        textAlign = TextAlign.Center,
                        color = valueColor
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        cursorColor = PrimaryAccent,
                        focusedBorderColor = valueColor,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                        focusedContainerColor = Background,
                        unfocusedContainerColor = Background
                    )
                )

                Spacer(modifier = Modifier.size(32.dp))

                IconButton(
                    onClick = {
                        value++
                        isNewPR = viewModel.isNewPR(exerciseId, value)
                    },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        "Increase",
                        tint = PrimaryAccent,
                        modifier = Modifier.size(40.dp)
                    )
                }

                if (exercise.type == "hold") {
                    Spacer(modifier = Modifier.size(16.dp))
                    IconButton(
                        onClick = { navController.navigate(Routes.holdTimer(exerciseId)) },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            Icons.Default.Timer,
                            "Timer",
                            tint = PrimaryAccent,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            if (displayXp > 0L) {
                Spacer(modifier = Modifier.height(8.dp))
                val xpLabel = buildString {
                    append("+$displayXp XP")
                    if (hasPotion && petMult > 1.0f) append(" (${String.format("%.1f", petMult)}x + 2x)")
                    else if (hasPotion) append(" (2x)")
                    else if (petMult > 1.0f) append(" (${String.format("%.1f", petMult)}x)")
                }
                Text(
                    text = xpLabel,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (hasPotion) GoalReachedColor else GoalComplete,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "CURRENT PR: $currentPR${if (exercise.type == "hold") "s" else " reps"}",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = PrimaryAccent,
                    focusedBorderColor = PrimaryAccent,
                    unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                    focusedLabelColor = PrimaryAccent,
                    unfocusedLabelColor = TextSecondary,
                    focusedContainerColor = Background,
                    unfocusedContainerColor = Background
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            NeonButton(
                text = buildString {
                    append("LOG IT")
                    if (hasPotion || petMult > 1.0f) {
                        append("  ")
                        if (petMult > 1.0f && hasPotion) append("${String.format("%.1f", petMult * 2)}x")
                        else if (hasPotion) append("2x")
                        else append("${String.format("%.1f", petMult)}x")
                    }
                },
                onClick = {
                    if (appSettings.hapticEnabled) {
                        val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                            (context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager).defaultVibrator
                        } else {
                            @Suppress("DEPRECATION")
                            context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
                        }
                        val duration = if (isNewPR && value > 0) 300L else 50L
                        vibrator.vibrate(android.os.VibrationEffect.createOneShot(duration, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                    if (appSettings.soundEnabled) {
                        if (isNewPR && value > 0) SoundEngine.playNewPR() else SoundEngine.playLogEntry()
                    }
                    val entry = PREntry(
                        id = UUID.randomUUID().toString(),
                        value = value,
                        date = System.currentTimeMillis(),
                        note = note.trim()
                    )
                    viewModel.logEntry(exerciseId, entry)
                    if (isNewPR && value > 0) {
                        showCelebration = true
                    } else {
                        navController.popBackStack()
                    }
                },
                enabled = value > 0
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
        }

        if (showCelebration) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.4f))
            )
        }

        PRCelebrationOverlay(
            visible = showCelebration,
            xpEarned = displayXp,
            onDismiss = {
                showCelebration = false
                navController.popBackStack()
            }
        )
    }
}
