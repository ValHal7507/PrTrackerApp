package com.example.prtracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.prtracker.data.Exercise
import com.example.prtracker.data.PREntry
import com.example.prtracker.data.SessionExerciseProgress
import com.example.prtracker.navigation.Routes
import com.example.prtracker.ui.components.GlowingCard
import com.example.prtracker.ui.components.GridBackground
import com.example.prtracker.ui.theme.Background
import com.example.prtracker.ui.theme.CardBackground
import com.example.prtracker.ui.theme.GoalComplete
import com.example.prtracker.ui.theme.LocalAppearance
import com.example.prtracker.ui.theme.TextPrimary
import com.example.prtracker.ui.theme.TextSecondary
import com.example.prtracker.ui.theme.systemAccentColor
import com.example.prtracker.ui.theme.systemSecondaryColor
import com.example.prtracker.viewmodel.PRViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LiveWorkoutScreen(
    viewModel: PRViewModel,
    navController: NavHostController
) {
    val appearance = LocalAppearance.current
    var tick by remember { mutableStateOf(0) }
    val session by viewModel.activeSession.collectAsState()
    val allExercises by viewModel.exercises.collectAsState()

    var inputValue by remember { mutableStateOf("") }
    var inputNote by remember { mutableStateOf("") }
    var expandedIndex by remember { mutableStateOf<Int?>(null) }

    if (session == null || session!!.isCompleted || session!!.presetId != "live") {
        Box(modifier = Modifier.fillMaxSize()) {
            GridBackground()
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "NO ACTIVE LIVE WORKOUT",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextSecondary,
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

    val elapsedSec = (session!!.elapsedMs(System.currentTimeMillis()) / 1000L).toInt()
    val hours = elapsedSec / 3600
    val minutes = (elapsedSec % 3600) / 60
    val seconds = elapsedSec % 60
    val timeString = "%02d:%02d:%02d".format(hours, minutes, seconds)

    LaunchedEffect(session?.isCompleted) {
        if (session?.isCompleted == false) {
            while (true) {
                kotlinx.coroutines.delay(100L)
                tick++
            }
        }
    }
    tick.let { }

    DisposableEffect(Unit) {
        onDispose {
            val currentSession = viewModel.activeSession.value
            if (currentSession != null && !currentSession.isCompleted && currentSession.presetId == "live") {
                val hasLoggedEntries = currentSession.exercises.any { it.completedSets.isNotEmpty() }
                if (!hasLoggedEntries) {
                    viewModel.discardWorkout()
                }
            }
        }
    }

    val sessionExerciseNames = remember(session) {
        session?.exercises?.map { it.exerciseName }?.toSet() ?: emptySet()
    }

    val sortedExercises = remember(allExercises) {
        allExercises.sortedBy { it.name.lowercase() }
    }

    val availableExercises = remember(sortedExercises, sessionExerciseNames) {
        sortedExercises.filter { it.name !in sessionExerciseNames }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GridBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = appearance.systemAccentColor)
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
                    text = "LIVE WORKOUT",
                    style = MaterialTheme.typography.headlineMedium,
                    color = appearance.systemAccentColor,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = timeString,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 48.sp),
                    color = appearance.systemAccentColor,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                var dropdownExpanded by remember { mutableStateOf(false) }

                GlowingCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (availableExercises.isNotEmpty()) dropdownExpanded = true
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "SELECT EXERCISE",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary.copy(alpha = 0.5f),
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (availableExercises.isNotEmpty()) "TAP TO SELECT"
                                else "ALL EXERCISES ADDED",
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (availableExercises.isNotEmpty()) TextPrimary else TextSecondary.copy(alpha = 0.3f),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = if (availableExercises.isNotEmpty()) appearance.systemAccentColor else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    availableExercises.forEach { exercise ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = exercise.name,
                                        color = TextPrimary,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = exercise.type.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = appearance.systemSecondaryColor,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            },
                            onClick = {
                                viewModel.addExerciseToLiveWorkout(exercise.id)
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(session!!.exercises) { index, exProgress ->
                    LiveWorkoutExerciseCard(
                        index = index,
                        progress = exProgress,
                        allExercises = allExercises,
                        isExpanded = expandedIndex == index,
                        onToggleExpand = {
                            if (expandedIndex == index) {
                                expandedIndex = null
                                inputValue = ""
                                inputNote = ""
                            } else {
                                expandedIndex = index
                                inputValue = ""
                                inputNote = ""
                            }
                        },
                        onLogEntry = { value, note ->
                            val exercise = allExercises.find { it.name == exProgress.exerciseName }
                            if (exercise != null) {
                                val entry = PREntry(
                                    id = java.util.UUID.randomUUID().toString(),
                                    value = value,
                                    date = System.currentTimeMillis(),
                                    note = note
                                )
                                viewModel.logEntry(exercise.id, entry)
                                viewModel.appendSetToLiveSession(index, value)
                            }
                            inputValue = ""
                            inputNote = ""
                            expandedIndex = null
                        },
                        onRemove = { viewModel.removeExerciseFromLiveWorkout(index) },
                        onDeleteEntry = { exIdx, setIdx -> viewModel.deleteEntryFromLiveSession(exIdx, setIdx) },
                        appearance = appearance
                    )
                }
                item {
                    if (session!!.exercises.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "ADD EXERCISES TO START",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                val hasEntries = session?.exercises?.any { it.completedSets.isNotEmpty() } == true
                Button(
                    onClick = {
                        viewModel.finishLiveWorkout()
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    enabled = hasEntries
                ) {
                    Text(
                        text = "FINISH",
                        color = if (hasEntries) appearance.systemAccentColor else TextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .border(
                            BorderStroke(
                                1.dp,
                                if (hasEntries) appearance.systemAccentColor else TextSecondary.copy(alpha = 0.3f)
                            ),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) { }
            }
        }
    }
}

@Composable
private fun LiveWorkoutExerciseCard(
    index: Int,
    progress: SessionExerciseProgress,
    allExercises: List<Exercise>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onLogEntry: (Int, String) -> Unit,
    onRemove: () -> Unit,
    onDeleteEntry: (exerciseIndex: Int, setIndex: Int) -> Unit,
    appearance: com.example.prtracker.data.AppearanceSettings
) {
    val exercise = remember(progress.exerciseName, allExercises) {
        allExercises.find { it.name == progress.exerciseName }
    }
    val isHold = progress.isHold
    val entryCount = progress.completedSets.size
    var localInput by remember { mutableStateOf("") }
    var localNote by remember { mutableStateOf("") }

    val dateFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    GlowingCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand() }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = progress.exerciseName,
                            style = MaterialTheme.typography.titleMedium,
                            color = appearance.systemAccentColor,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(appearance.systemSecondaryColor.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isHold) "HOLD" else "REPS",
                                style = MaterialTheme.typography.labelSmall,
                                color = appearance.systemSecondaryColor,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    if (entryCount > 0) {
                        val lastValue = progress.completedSets.last().value
                        val unit = if (isHold) "s" else ""
                        Text(
                            text = "$entryCount logged — Last: $lastValue$unit",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                if (entryCount == 0) {
                    IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove exercise",
                            tint = Color(0xFFFF003C).copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (progress.completedSets.isNotEmpty() && !isExpanded) {
                Spacer(modifier = Modifier.height(6.dp))
                val allSets = progress.completedSets
                val recentIndices = (allSets.size - 5).coerceAtLeast(0) until allSets.size
                recentIndices.reversed().forEach { setIdx ->
                    val setEntry = allSets[setIdx]
                    val unit = if (isHold) "s" else ""
                    val timeAgo = formatTimeAgo(setEntry.value.toLong(), dateFormat)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "  ${setEntry.value}$unit  $timeAgo",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary.copy(alpha = 0.7f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { onDeleteEntry(index, setIdx) },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete entry",
                                tint = Color(0xFFFF003C).copy(alpha = 0.6f),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    if (progress.completedSets.isNotEmpty()) {
                        Text(
                            text = "LOGGED ENTRIES",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary.copy(alpha = 0.5f),
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        progress.completedSets.forEachIndexed { setIdx, setEntry ->
                            val unit = if (isHold) "s" else ""
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "  ${setEntry.value}$unit",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { onDeleteEntry(index, setIdx) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete entry",
                                        tint = Color(0xFFFF003C).copy(alpha = 0.6f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = {
                                val current = localInput.toIntOrNull() ?: 0
                                localInput = (current - 1).coerceAtLeast(0).toString()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = "Decrement",
                                tint = appearance.systemAccentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        OutlinedTextField(
                            value = localInput,
                            onValueChange = { localInput = it.filter { c -> c.isDigit() } },
                            modifier = Modifier.weight(1f).height(56.dp),
                            singleLine = true,
                            placeholder = {
                                Text(
                                    if (isHold) "SECONDS" else "REPS",
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
                                cursorColor = appearance.systemAccentColor,
                                focusedBorderColor = appearance.systemAccentColor,
                                unfocusedBorderColor = TextSecondary.copy(alpha = 0.2f),
                                focusedContainerColor = Background,
                                unfocusedContainerColor = Background
                            )
                        )
                        IconButton(
                            onClick = {
                                val current = localInput.toIntOrNull() ?: 0
                                localInput = (current + 1).toString()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowUp,
                                contentDescription = "Increment",
                                tint = appearance.systemAccentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        val numValue = localInput.toIntOrNull() ?: 0
                        IconButton(
                            onClick = {
                                if (numValue > 0) {
                                    onLogEntry(numValue, localNote.trim())
                                    localInput = ""
                                    localNote = ""
                                }
                            },
                            enabled = numValue > 0,
                            modifier = Modifier.size(36.dp),
                            colors = androidx.compose.material3.IconButtonDefaults.iconButtonColors(
                                containerColor = if (numValue > 0) appearance.systemAccentColor.copy(alpha = 0.2f) else Color.Transparent
                            )
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Log entry",
                                tint = if (numValue > 0) appearance.systemAccentColor else TextSecondary.copy(alpha = 0.3f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = localNote,
                        onValueChange = { localNote = it },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        singleLine = true,
                        placeholder = { Text("Note (optional)", color = TextSecondary.copy(alpha = 0.3f)) },
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = appearance.systemAccentColor,
                            focusedBorderColor = appearance.systemAccentColor.copy(alpha = 0.5f),
                            unfocusedBorderColor = TextSecondary.copy(alpha = 0.15f),
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                }
            }
        }
    }
}

private fun formatTimeAgo(timestamp: Long, dateFormat: SimpleDateFormat): String {
    return dateFormat.format(Date(timestamp))
}
