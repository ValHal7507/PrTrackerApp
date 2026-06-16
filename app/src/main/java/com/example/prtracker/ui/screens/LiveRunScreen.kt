package com.example.prtracker.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.prtracker.data.RunEntry
import com.example.prtracker.data.RunningPREngine
import com.example.prtracker.service.RunTrackingService
import com.example.prtracker.ui.components.GlowingCard
import com.example.prtracker.ui.components.GridBackground
import com.example.prtracker.ui.theme.Background
import com.example.prtracker.ui.theme.TextPrimary
import com.example.prtracker.ui.theme.TextSecondary
import com.example.prtracker.viewmodel.PRViewModel
import java.util.UUID

private val Magenta = Color(0xFFFF2D78)
private val MagentaDim = Color(0xFFC4005A)
private val Red = Color(0xFFFF003C)

private enum class RunState { IDLE, RUNNING, PAUSED, FINISHED }

@Composable
fun LiveRunScreen(
    navController: NavHostController,
    viewModel: PRViewModel
) {
    val context = LocalContext.current
    val distance by RunTrackingService.distanceMeters.collectAsState()
    val elapsed by RunTrackingService.elapsedSeconds.collectAsState()
    val currentPace by RunTrackingService.currentPaceSecPerKm.collectAsState()
    val isTracking by RunTrackingService.isTracking.collectAsState()
    val isPaused by RunTrackingService.isPaused.collectAsState()

    val runState = when {
        isTracking && !isPaused -> RunState.RUNNING
        isTracking && isPaused -> RunState.PAUSED
        else -> RunState.IDLE
    }

    var showFinishCard by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }
    var permissionDenied by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            val activity = context as? androidx.fragment.app.FragmentActivity
            val permanentlyDenied = activity == null ||
                !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)
            if (permanentlyDenied) {
                permissionDenied = true
            }
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GridBackground()

        if (permissionDenied) {
            PermissionDeniedView(
                onOpenSettings = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                },
                onBack = { navController.popBackStack() }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (runState == RunState.IDLE) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Magenta
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LIVE RUN",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Magenta,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Spacer(modifier = Modifier.weight(0.3f))

                if (runState == RunState.IDLE) {
                    Text(
                        text = "LIVE RUN",
                        style = MaterialTheme.typography.displayLarge,
                        color = Magenta,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "GPS TRACKING WITH\nFOREGROUND SERVICE",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.weight(0.7f))
                    StartButton(onClick = { RunTrackingService.start(context) })
                } else {
                    Text(
                        text = formatElapsed(elapsed),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 56.sp
                        ),
                        color = Magenta,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val distanceText = if (distance >= 1000f) {
                        "%.2f km".format(distance / 1000f)
                    } else {
                        "%.0f m".format(distance)
                    }
                    Text(
                        text = distanceText,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 40.sp
                        ),
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val avgPace = if (distance >= 100f && elapsed > 0L) {
                            elapsed.toFloat() / (distance / 1000f)
                        } else null

                        PaceCard(
                            label = "CURRENT PACE",
                            pace = currentPace,
                            modifier = Modifier.weight(1f)
                        )
                        PaceCard(
                            label = "AVG PACE",
                            pace = avgPace,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))

                    when (runState) {
                        RunState.RUNNING -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                PauseButton(
                                    onClick = { RunTrackingService.pause(context) },
                                    modifier = Modifier.weight(1f)
                                )
                                FinishButton(
                                    onClick = {
                                        RunTrackingService.stop(context)
                                        showFinishCard = true
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        RunState.PAUSED -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ResumeButton(
                                    onClick = { RunTrackingService.resume(context) },
                                    modifier = Modifier.weight(1f)
                                )
                                FinishButtonRed(
                                    onClick = {
                                        RunTrackingService.stop(context)
                                        showFinishCard = true
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        else -> {}
                    }
                }
                Spacer(modifier = Modifier.weight(0.7f))
            }
        }

        AnimatedVisibility(
            visible = showFinishCard,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            FinishCard(
                distanceMeters = distance,
                elapsedSeconds = elapsed,
                note = noteText,
                onNoteChange = { noteText = it },
                onSave = {
                    val entry = RunEntry(
                        id = UUID.randomUUID().toString(),
                        distanceMeters = distance,
                        durationSeconds = elapsed.toInt(),
                        date = System.currentTimeMillis(),
                        note = noteText
                    )
                    viewModel.addRunEntry(entry)
                    RunTrackingService.reset()
                    navController.popBackStack()
                },
                onDiscard = {
                    RunTrackingService.reset()
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
private fun PermissionDeniedView(
    onOpenSettings: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Magenta
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        GlowingCard(
            modifier = Modifier.fillMaxWidth(),
            borderBrush = remember {
                androidx.compose.ui.graphics.Brush.linearGradient(
                    listOf(Magenta, MagentaDim)
                )
            }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "LOCATION PERMISSION REQUIRED",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Magenta,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "PRTracker needs GPS access to track your runs in real time. Please enable it in system settings.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = onOpenSettings,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Magenta.copy(alpha = 0.15f)
                        )
                    ) {
                        Text(
                            text = "OPEN SETTINGS",
                            color = Magenta,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .border(
                                BorderStroke(1.dp, androidx.compose.ui.graphics.Brush.linearGradient(
                                    listOf(Magenta, Color.Transparent)
                                )),
                                RoundedCornerShape(16.dp)
                            )
                    ) {}
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PaceCard(
    label: String,
    pace: Float?,
    modifier: Modifier = Modifier
) {
    val borderBrush = remember {
        androidx.compose.ui.graphics.Brush.linearGradient(listOf(Magenta, MagentaDim))
    }
    GlowingCard(modifier = modifier, borderBrush = borderBrush) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (pace != null) RunningPREngine.formatPace(pace) else "--:-- /km",
                style = MaterialTheme.typography.headlineSmall,
                color = Magenta,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun StartButton(onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Magenta.copy(alpha = 0.15f)
            )
        ) {
            Text(
                text = "START RUN",
                color = Magenta,
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = FontFamily.Monospace
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    BorderStroke(1.dp, androidx.compose.ui.graphics.Brush.linearGradient(
                        listOf(Magenta, Color.Transparent)
                    )),
                    RoundedCornerShape(16.dp)
                )
        ) {}
    }
}

@Composable
private fun PauseButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Magenta.copy(alpha = 0.05f)
            )
        ) {
            Text(
                text = "PAUSE",
                color = Magenta,
                style = MaterialTheme.typography.titleLarge,
                fontFamily = FontFamily.Monospace
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    BorderStroke(1.dp, Magenta),
                    RoundedCornerShape(16.dp)
                )
        ) {}
    }
}

@Composable
private fun ResumeButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Magenta.copy(alpha = 0.15f)
            )
        ) {
            Text(
                text = "RESUME",
                color = Magenta,
                style = MaterialTheme.typography.titleLarge,
                fontFamily = FontFamily.Monospace
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    BorderStroke(1.dp, androidx.compose.ui.graphics.Brush.linearGradient(
                        listOf(Magenta, Color.Transparent)
                    )),
                    RoundedCornerShape(16.dp)
                )
        ) {}
    }
}

@Composable
private fun FinishButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Magenta.copy(alpha = 0.15f)
            )
        ) {
            Text(
                text = "FINISH",
                color = Magenta,
                style = MaterialTheme.typography.titleLarge,
                fontFamily = FontFamily.Monospace
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    BorderStroke(1.dp, androidx.compose.ui.graphics.Brush.linearGradient(
                        listOf(Magenta, Color.Transparent)
                    )),
                    RoundedCornerShape(16.dp)
                )
        ) {}
    }
}

@Composable
private fun FinishButtonRed(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Red.copy(alpha = 0.05f)
            )
        ) {
            Text(
                text = "FINISH",
                color = Red,
                style = MaterialTheme.typography.titleLarge,
                fontFamily = FontFamily.Monospace
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    BorderStroke(1.dp, Red),
                    RoundedCornerShape(16.dp)
                )
        ) {}
    }
}

@Composable
private fun FinishCard(
    distanceMeters: Float,
    elapsedSeconds: Long,
    note: String,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    onDiscard: () -> Unit
) {
    val pace = if (distanceMeters >= 100f) {
        elapsedSeconds.toFloat() / (distanceMeters / 1000f)
    } else null

    val borderBrush = remember {
        androidx.compose.ui.graphics.Brush.linearGradient(listOf(Magenta, MagentaDim))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(Background)
    ) {
        GlowingCard(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            borderBrush = borderBrush
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "RUN COMPLETE",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Magenta,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = RunningPREngine.formatDistance(distanceMeters),
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = RunningPREngine.formatDuration(elapsedSeconds.toInt()),
                    style = MaterialTheme.typography.titleLarge,
                    color = TextSecondary,
                    fontFamily = FontFamily.Monospace
                )
                if (pace != null) {
                    Text(
                        text = RunningPREngine.formatPace(pace),
                        style = MaterialTheme.typography.titleMedium,
                        color = Magenta,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = onNoteChange,
                    label = { Text("Note (optional)", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = Magenta,
                        unfocusedBorderColor = Magenta.copy(alpha = 0.3f),
                        cursorColor = Magenta
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                SaveButton(onClick = onSave)
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onDiscard) {
                    Text(
                        text = "DISCARD",
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun SaveButton(onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Magenta.copy(alpha = 0.15f)
            )
        ) {
            Text(
                text = "SAVE RUN",
                color = Magenta,
                style = MaterialTheme.typography.titleLarge,
                fontFamily = FontFamily.Monospace
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    BorderStroke(1.dp, androidx.compose.ui.graphics.Brush.linearGradient(
                        listOf(Magenta, Color.Transparent)
                    )),
                    RoundedCornerShape(16.dp)
                )
        ) {}
    }
}

private fun formatElapsed(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) {
        "%d:%02d:%02d".format(h, m, s)
    } else {
        "%02d:%02d".format(m, s)
    }
}
