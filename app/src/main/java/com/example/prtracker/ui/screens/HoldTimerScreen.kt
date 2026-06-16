package com.example.prtracker.ui.screens

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.prtracker.data.PREntry
import com.example.prtracker.data.SoundEngine
import com.example.prtracker.navigation.Routes
import com.example.prtracker.ui.components.GridBackground
import com.example.prtracker.ui.components.PRCelebrationOverlay
import com.example.prtracker.ui.theme.Background
import com.example.prtracker.ui.theme.GoalComplete
import com.example.prtracker.ui.theme.LocalAppearance
import com.example.prtracker.ui.theme.TextPrimary
import com.example.prtracker.ui.theme.TextSecondary
import com.example.prtracker.ui.theme.systemAccentColor
import com.example.prtracker.ui.theme.systemSecondaryColor
import com.example.prtracker.viewmodel.PRViewModel
import kotlinx.coroutines.delay
import java.util.UUID

@Composable
fun HoldTimerScreen(
    viewModel: PRViewModel,
    navController: NavHostController,
    exerciseId: String
) {
    val exercises by viewModel.exercises.collectAsState()
    val exercise = exercises.find { it.id == exerciseId }
    val appSettings by viewModel.appSettings.collectAsState()
    val appearance = LocalAppearance.current
    val context = LocalContext.current

    if (exercise == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Exercise not found", color = TextSecondary)
        }
        return
    }

    var elapsedMillis by remember { mutableLongStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    var targetSeconds by remember { mutableIntStateOf(0) }
    var targetText by remember { mutableStateOf("") }
    var showCelebration by remember { mutableStateOf(false) }
    var targetHitHandled by remember { mutableStateOf(false) }
    var showTargetFlash by remember { mutableStateOf(false) }

    val elapsedSeconds = (elapsedMillis / 1000L).toInt()
    val timeString = "%02d:%02d".format(elapsedSeconds / 60, elapsedSeconds % 60)

    val progressSweep = if (targetSeconds > 0) {
        (elapsedSeconds.toFloat() / targetSeconds).coerceAtMost(1f) * 360f
    } else {
        (elapsedSeconds % 60) / 60f * 360f
    }

    val ringGradientBrush = remember {
        Brush.sweepGradient(listOf(appearance.systemAccentColor, appearance.systemSecondaryColor))
    }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (isRunning) {
                delay(100L)
                elapsedMillis += 100L
            }
        }
    }

    LaunchedEffect(elapsedSeconds, targetSeconds, isRunning) {
        if (isRunning && targetSeconds > 0 && elapsedSeconds >= targetSeconds && !targetHitHandled) {
            targetHitHandled = true
            showTargetFlash = true
            if (appSettings.hapticEnabled) {
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                }
                vibrator.vibrate(
                    VibrationEffect.createOneShot(50L, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            }
            if (appSettings.soundEnabled) SoundEngine.playTimerTargetReached()
            delay(500)
            showTargetFlash = false
        }
    }

    DisposableEffect(Unit) {
        val activity = context as? Activity
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GridBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(Icons.Default.ArrowBack, "Back", tint = appearance.systemAccentColor)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = exercise.name,
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "HOLD TIMER",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier.size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(240.dp)) {
                    val strokeWidth = 8.dp.toPx()
                    val arcSize = Size(
                        size.width - strokeWidth,
                        size.height - strokeWidth
                    )
                    val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                    drawArc(
                        color = appearance.systemAccentColor.copy(alpha = 0.15f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    val progressBrush = if (showTargetFlash) {
                        Brush.sweepGradient(listOf(GoalComplete, GoalComplete))
                    } else {
                        ringGradientBrush
                    }

                    drawArc(
                        brush = progressBrush,
                        startAngle = -90f,
                        sweepAngle = progressSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timeString,
                        fontSize = 56.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "SECONDS",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(appearance.systemAccentColor.copy(alpha = 0.2f))
                        .border(2.dp, appearance.systemAccentColor, CircleShape)
                        .clickable {
                                if (appSettings.soundEnabled) {
                                    if (!isRunning) SoundEngine.playTimerStart()
                                    else SoundEngine.playTimerStop()
                                }
                            isRunning = !isRunning
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Start",
                        tint = appearance.systemAccentColor,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(TextSecondary.copy(alpha = 0.1f))
                        .clickable {
                            elapsedMillis = 0L
                            isRunning = false
                            targetHitHandled = false
                            showTargetFlash = false
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset",
                        tint = TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .then(
                            if (elapsedSeconds > 0) {
                                Modifier
                                    .clip(CircleShape)
                                    .background(GoalComplete.copy(alpha = 0.2f))
                                    .border(2.dp, GoalComplete, CircleShape)
                            } else {
                                Modifier
                            }
                        )
                        .clickable(enabled = elapsedSeconds > 0) {
                            val secs = elapsedSeconds
                            val isPr = viewModel.isNewPR(exerciseId, secs)
                            if (appSettings.hapticEnabled) {
                                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
                                } else {
                                    @Suppress("DEPRECATION")
                                    context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                                }
                                val duration = if (isPr && secs > 0) 300L else 50L
                                vibrator.vibrate(android.os.VibrationEffect.createOneShot(duration, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                            }
                            if (appSettings.soundEnabled) {
                                if (isPr && secs > 0) SoundEngine.playNewPR() else SoundEngine.playLogEntry()
                            }
                            val entry = PREntry(
                                id = UUID.randomUUID().toString(),
                                value = secs,
                                date = System.currentTimeMillis(),
                                note = ""
                            )
                            viewModel.logEntry(exerciseId, entry)
                            if (isPr && secs > 0) {
                                showCelebration = true
                            } else {
                                navController.popBackStack()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Log it",
                        tint = if (elapsedSeconds > 0) GoalComplete else TextSecondary.copy(alpha = 0.3f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "TARGET",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.width(12.dp))

                IconButton(
                    onClick = {
                        if (targetSeconds > 0) {
                            targetSeconds = (targetSeconds - 5).coerceAtLeast(0)
                            targetText = if (targetSeconds > 0) targetSeconds.toString() else ""
                            targetHitHandled = false
                            showTargetFlash = false
                        }
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Decrease target",
                        tint = appearance.systemAccentColor
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedTextField(
                    value = targetText,
                    onValueChange = { text ->
                        val filtered = text.filter { it.isDigit() }
                        targetText = filtered
                        targetSeconds = if (filtered.isEmpty()) 0 else filtered.toInt().coerceAtMost(300)
                        targetHitHandled = false
                        showTargetFlash = false
                    },
                    placeholder = { Text("0", color = TextSecondary) },
                    modifier = Modifier.width(100.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        color = appearance.systemAccentColor,
                        textAlign = TextAlign.Center
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = appearance.systemAccentColor,
                        unfocusedTextColor = appearance.systemAccentColor,
                        cursorColor = appearance.systemAccentColor,
                        focusedBorderColor = appearance.systemAccentColor,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                        focusedContainerColor = Background,
                        unfocusedContainerColor = Background
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (targetSeconds < 300) {
                            targetSeconds = (targetSeconds + 5).coerceAtMost(300)
                            targetText = targetSeconds.toString()
                        }
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase target",
                        tint = appearance.systemAccentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showCelebration) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            )
        }

        PRCelebrationOverlay(
            visible = showCelebration,
            onDismiss = {
                showCelebration = false
                navController.popBackStack()
            }
        )
    }
}
