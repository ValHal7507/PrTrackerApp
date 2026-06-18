package com.example.prtracker.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.prtracker.data.SessionExerciseProgress
import com.example.prtracker.ui.components.GridBackground
import com.example.prtracker.ui.theme.Background
import com.example.prtracker.ui.theme.CardBackground
import com.example.prtracker.ui.theme.GoalComplete
import com.example.prtracker.ui.theme.TextPrimary
import com.example.prtracker.ui.theme.TextSecondary
import com.example.prtracker.viewmodel.PRViewModel

private val RunningMagenta = Color(0xFFFF2D78)

@Composable
fun WorkoutSessionScreen(
    viewModel: PRViewModel,
    navController: NavHostController
) {
    val session by viewModel.activeSession.collectAsState()
    var tick by remember { mutableIntStateOf(0) }

    LaunchedEffect(session?.isPaused) {
        if (session?.isPaused == false && session?.isCompleted == false) {
            while (true) {
                kotlinx.coroutines.delay(100L)
                tick++
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.autoPauseWorkout()
        }
    }

    if (session == null || session!!.isCompleted) {
        Box(modifier = Modifier.fillMaxSize()) {
            GridBackground()
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (session?.isCompleted == true) "WORKOUT COMPLETE" else "NO ACTIVE WORKOUT",
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (session?.isCompleted == true) GoalComplete else TextSecondary,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Text("GO BACK", color = TextSecondary, fontFamily = FontFamily.Monospace)
                }
            }
        }
        return
    }

    // read tick to force recomposition on timer ticks
    tick.let { }
    val elapsedSec = (session!!.elapsedMs(System.currentTimeMillis()) / 1000L).toInt()
    val hours = elapsedSec / 3600
    val minutes = (elapsedSec % 3600) / 60
    val seconds = elapsedSec % 60
    val timeString = "%02d:%02d:%02d".format(hours, minutes, seconds)

    val totalSetsRemaining = session!!.exercises.sumOf { it.totalSets - it.completedSets.size }

    Box(modifier = Modifier.fillMaxSize()) {
        GridBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    viewModel.autoPauseWorkout()
                    navController.popBackStack()
                }) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = RunningMagenta)
                }
                Spacer(modifier = Modifier.weight(1f))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = session!!.presetName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = RunningMagenta,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = timeString,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 48.sp),
                    color = RunningMagenta,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "SETS REMAINING: $totalSetsRemaining",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(session!!.exercises) { index, exProgress ->
                    SessionExerciseCard(
                        index = index,
                        progress = exProgress,
                        onCompleteSet = { value ->
                            viewModel.completeSetInSession(index, value)
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    Button(
                        onClick = { viewModel.togglePauseWorkout() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Icon(
                            imageVector = if (session!!.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (session!!.isPaused) "Resume" else "Pause",
                            tint = RunningMagenta,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (session!!.isPaused) "RESUME" else "PAUSE",
                            color = RunningMagenta,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .border(
                                androidx.compose.foundation.BorderStroke(1.dp, RunningMagenta.copy(alpha = 0.5f)),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {}
                }

                Box(modifier = Modifier.weight(1f)) {
                    Button(
                        onClick = {
                            viewModel.finishWorkout()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Text(
                            text = "FINISH",
                            color = RunningMagenta,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .border(
                                androidx.compose.foundation.BorderStroke(1.dp, RunningMagenta),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {}
                }
            }
        }
    }
}

@Composable
private fun SessionExerciseCard(
    index: Int,
    progress: SessionExerciseProgress,
    onCompleteSet: (Int) -> Unit
) {
    val completedCount = progress.completedSets.size
    val isAllComplete = completedCount >= progress.totalSets

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .border(
                1.dp,
                if (isAllComplete) GoalComplete.copy(alpha = 0.4f) else RunningMagenta.copy(alpha = 0.3f),
                RoundedCornerShape(12.dp)
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${index + 1}. ${progress.exerciseName}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isAllComplete) GoalComplete else RunningMagenta,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(1f)
                )
                val doneTotal = progress.completedSets.sumOf { it.value }
                val label = if (progress.isUntilFailure) {
                    "$doneTotal${if (progress.isHold) "s" else ""} total"
                } else {
                    val totalNeeded = progress.targetValue * progress.totalSets
                    if (progress.isHold) "${doneTotal}s / ${totalNeeded}s" else "$doneTotal / $totalNeeded"
                }
                Text(
                    text = "$completedCount/${progress.totalSets} sets ($label)",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isAllComplete) GoalComplete else TextSecondary,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            for (setIdx in 0 until progress.totalSets) {
                val isSetDone = setIdx < completedCount
                val setValue = if (isSetDone) progress.completedSets[setIdx].value else 0
                var inputValue by remember { mutableStateOf("") }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Set ${setIdx + 1}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSetDone) GoalComplete else TextSecondary,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.width(48.dp)
                    )
                    if (isSetDone) {
                        val unit = if (progress.isHold) "s" else ""
                        Text(
                            text = "$setValue$unit",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GoalComplete,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Done",
                            tint = GoalComplete,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        OutlinedTextField(
                            value = inputValue,
                            onValueChange = { inputValue = it.filter { c -> c.isDigit() } },
                            modifier = Modifier.weight(1f).height(56.dp),
                            singleLine = true,
                            placeholder = {
                                Text(
                                    if (progress.isUntilFailure) "MAX" else progress.targetValue.toString(),
                                    color = TextSecondary.copy(alpha = 0.3f)
                                )
                            },
                            textStyle = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                cursorColor = RunningMagenta,
                                focusedBorderColor = RunningMagenta,
                                unfocusedBorderColor = TextSecondary.copy(alpha = 0.2f),
                                focusedContainerColor = Background,
                                unfocusedContainerColor = Background
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val numValue = inputValue.toIntOrNull() ?: 0
                        IconButton(
                            onClick = {
                                val v = inputValue.toIntOrNull() ?: return@IconButton
                                if (v > 0) {
                                    onCompleteSet(v)
                                    inputValue = ""
                                }
                            },
                            enabled = numValue > 0,
                            modifier = Modifier.size(36.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = if (numValue > 0) RunningMagenta.copy(alpha = 0.2f) else Color.Transparent
                            )
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Complete set",
                                tint = if (numValue > 0) RunningMagenta else TextSecondary.copy(alpha = 0.3f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
