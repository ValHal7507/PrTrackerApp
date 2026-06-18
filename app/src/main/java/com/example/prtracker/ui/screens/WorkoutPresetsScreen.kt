package com.example.prtracker.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.prtracker.data.Exercise
import com.example.prtracker.data.PresetExercise
import com.example.prtracker.data.WorkoutPreset
import com.example.prtracker.data.WorkoutSession
import com.example.prtracker.navigation.Routes
import com.example.prtracker.ui.components.GlowingCard
import com.example.prtracker.ui.components.GridBackground
import com.example.prtracker.ui.components.NeonButton
import com.example.prtracker.ui.theme.Background
import com.example.prtracker.ui.theme.CardBackground
import com.example.prtracker.ui.theme.GoalComplete
import com.example.prtracker.ui.theme.LocalAppearance
import com.example.prtracker.ui.theme.PrimaryAccent
import com.example.prtracker.ui.theme.TextPrimary
import com.example.prtracker.ui.theme.TextSecondary
import com.example.prtracker.ui.theme.pinnedSecondaryColor
import com.example.prtracker.ui.theme.systemAccentColor
import com.example.prtracker.ui.theme.systemSecondaryColor
import com.example.prtracker.viewmodel.PRViewModel

private val HardOrange = Color(0xFFFF8C00)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutPresetsScreen(
    viewModel: PRViewModel,
    navController: NavHostController,
    initialEditId: String? = null
) {
    val presets by viewModel.workoutPresets.collectAsState()
    val allExercises by viewModel.exercises.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()
    val appearance = LocalAppearance.current
    var showSheet by remember { mutableStateOf(false) }
    var editingPreset by remember { mutableStateOf<WorkoutPreset?>(null) }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }
    var reorderMode by remember { mutableStateOf(false) }

    LaunchedEffect(initialEditId) {
        if (initialEditId != null) {
            val preset = presets.find { it.id == initialEditId }
            if (preset != null) {
                editingPreset = preset
                showSheet = true
            }
        }
    }

    val sortedPresets = remember(presets) {
        presets.sortedWith(
            compareByDescending<WorkoutPreset> { it.isPinned }.thenBy { it.sortOrder }.thenByDescending { it.createdAt }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GridBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "WORKOUT PRESETS",
                style = MaterialTheme.typography.displayLarge,
                color = appearance.systemAccentColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, bottom = 4.dp),
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Monospace
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(appearance.systemSecondaryColor.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${presets.size} PRESETS",
                        style = MaterialTheme.typography.labelLarge,
                        color = appearance.systemSecondaryColor,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (reorderMode) {
                        Text(
                            text = "REORDERING",
                            style = MaterialTheme.typography.labelSmall,
                            color = appearance.systemAccentColor,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    IconButton(
                        onClick = { reorderMode = !reorderMode },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = "Toggle reorder mode",
                            tint = if (reorderMode) appearance.systemAccentColor else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (activeSession != null && !activeSession!!.isCompleted) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFF2D78).copy(alpha = 0.15f))
                        .border(1.dp, Color(0xFFFF2D78).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clickable { navController.navigate(Routes.workoutSession(activeSession!!.presetId)) }
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color(0xFFFF2D78),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ACTIVE WORKOUT",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFFFF2D78),
                                fontFamily = FontFamily.Monospace
                            )
                            if (activeSession!!.isPaused) {
                                Text(
                                    text = "PAUSED — tap to resume",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    fontFamily = FontFamily.Monospace
                                )
                            } else {
                                Text(
                                    text = "IN PROGRESS — tap to continue",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        Text(
                            text = "RESUME →",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFFFF2D78),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (presets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GlowingCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = appearance.systemAccentColor.copy(alpha = 0.5f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "NO PRESETS\n— TAP + TO CREATE ONE —",
                                style = MaterialTheme.typography.headlineSmall,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(sortedPresets, key = { _, preset -> preset.id }) { index, preset ->
                        WorkoutPresetCard(
                            preset = preset,
                            appearance = appearance,
                            reorderMode = reorderMode,
                            isFirst = index == 0,
                            isLast = index == sortedPresets.lastIndex,
                            onViewDetail = { navController.navigate("${Routes.PRESET_DETAIL}/${preset.id}") },
                            onPinToggle = { viewModel.togglePresetPin(preset.id) },
                            onEdit = {
                                editingPreset = preset
                                showSheet = true
                            },
                            onDelete = { showDeleteDialog = preset.id },
                            onMoveUp = { viewModel.reorderPresets(index, index - 1) },
                            onMoveDown = { viewModel.reorderPresets(index, index + 1) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(72.dp)) }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                editingPreset = null
                showSheet = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = appearance.systemAccentColor.copy(alpha = 0.15f),
            contentColor = appearance.systemAccentColor,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add preset",
                tint = appearance.systemAccentColor
            )
        }
    }

    if (showDeleteDialog != null) {
        val preset = presets.find { it.id == showDeleteDialog }
        if (preset != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Delete Preset", color = TextPrimary) },
                text = { Text("Delete preset \"${preset.name}\"?", color = TextSecondary) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deletePreset(preset.id)
                        showDeleteDialog = null
                    }) {
                        Text("Delete", color = appearance.systemAccentColor)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = null }) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = Background
            )
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = {},
            sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
                confirmValueChange = { it != SheetValue.Hidden }
            ),
            containerColor = Background
        ) {
            PresetFormSheet(
                preset = editingPreset,
                allExercises = allExercises,
                onSave = { preset ->
                    if (editingPreset != null) {
                        viewModel.updatePreset(preset)
                    } else {
                        viewModel.addPreset(preset)
                    }
                    showSheet = false
                    editingPreset = null
                },
                onDismiss = {
                    showSheet = false
                    editingPreset = null
                }
            )
        }
    }
}

@Composable
private fun WorkoutPresetCard(
    preset: WorkoutPreset,
    appearance: com.example.prtracker.data.AppearanceSettings,
    reorderMode: Boolean = false,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    onViewDetail: () -> Unit,
    onPinToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit = {},
    onMoveDown: () -> Unit = {}
) {
    val diffColor = difficultyColor(preset.difficulty, appearance)

    GlowingCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            if (reorderMode) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = !isFirst,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Move up",
                            tint = if (isFirst) TextSecondary.copy(alpha = 0.3f) else appearance.systemAccentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onMoveDown,
                        enabled = !isLast,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Move down",
                            tint = if (isLast) TextSecondary.copy(alpha = 0.3f) else appearance.systemAccentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.weight(1f)
            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .clickable { onViewDetail() }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = preset.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = appearance.systemAccentColor,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "View detail",
                        tint = appearance.systemAccentColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(diffColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = preset.difficulty,
                            style = MaterialTheme.typography.labelSmall,
                            color = diffColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                if (preset.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = preset.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (preset.exercises.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    preset.exercises.forEachIndexed { index, ex ->
                        val detail = if (ex.isUntilFailure) {
                            "${ex.sets}x UNTIL FAILURE"
                        } else {
                            buildString {
                                append("${ex.sets}x")
                                if (ex.targetHoldSeconds > 0) {
                                    append("${ex.targetHoldSeconds}s")
                                } else {
                                    append(ex.targetReps)
                                }
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 1.dp)
                        ) {
                            Text(
                                text = "${index + 1}. ${ex.exerciseName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = detail,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = onPinToggle,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = if (preset.isPinned) "Unpin" else "Pin",
                        tint = if (preset.isPinned) appearance.systemAccentColor else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit preset",
                        tint = appearance.systemAccentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete preset",
                        tint = appearance.pinnedSecondaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PresetFormSheet(
    preset: WorkoutPreset?,
    allExercises: List<Exercise>,
    onSave: (WorkoutPreset) -> Unit,
    onDismiss: () -> Unit
) {
    val appearance = LocalAppearance.current
    val scrollState = rememberScrollState()

    var name by remember { mutableStateOf(preset?.name ?: "") }
    var description by remember { mutableStateOf(preset?.description ?: "") }
    var difficulty by remember { mutableStateOf(preset?.difficulty ?: "MEDIUM") }
    var exercises by remember { mutableStateOf(preset?.exercises ?: emptyList()) }
    var showAddExercise by remember { mutableStateOf(false) }
    var confirmDeleteExerciseIndex by remember { mutableStateOf<Int?>(null) }
    val sortedExercises = remember(allExercises) { allExercises.sortedBy { it.name.lowercase() } }

    var exName by remember { mutableStateOf("") }
    var exType by remember { mutableStateOf("REPS") }
    var exTargetReps by remember { mutableStateOf("") }
    var exTargetHold by remember { mutableStateOf("") }
    var exUntilFailure by remember { mutableStateOf(false) }
    var exSets by remember { mutableStateOf("3") }
    var exNotes by remember { mutableStateOf("") }

    fun addExercise() {
        if (exName.isBlank()) return
        val exercise = PresetExercise(
            exerciseName = exName.trim(),
            targetReps = if (exUntilFailure) 0 else if (exType == "REPS") (exTargetReps.toIntOrNull() ?: 0) else 0,
            targetHoldSeconds = if (exUntilFailure) 0 else if (exType == "HOLD") (exTargetHold.toIntOrNull() ?: 0) else 0,
            isUntilFailure = exUntilFailure,
            sets = exSets.toIntOrNull() ?: 3,
            notes = exNotes.trim()
        )
        exercises = exercises + exercise
        exName = ""
        exTargetReps = ""
        exTargetHold = ""
        exUntilFailure = false
        exSets = "3"
        exNotes = ""
        showAddExercise = false
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = if (preset != null) "EDIT PRESET" else "NEW PRESET",
            style = MaterialTheme.typography.headlineLarge,
            color = appearance.systemAccentColor,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Preset Name") },
            placeholder = { Text("e.g. Upper Body Blast") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors(appearance),
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description (optional)") },
            placeholder = { Text("e.g. Push-focused workout") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors(appearance),
            textStyle = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "DIFFICULTY",
            style = MaterialTheme.typography.labelLarge,
            color = TextSecondary,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("EASY", "MEDIUM", "HARD", "MAX EFFORT").forEach { level ->
                val selected = difficulty == level
                val color = difficultyColor(level, appearance)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .then(
                            if (selected) Modifier.background(color.copy(alpha = 0.2f))
                            else Modifier.background(Color.Transparent)
                        )
                        .border(
                            BorderStroke(1.dp, if (selected) color else color.copy(alpha = 0.3f)),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { difficulty = level },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = level,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) color else color.copy(alpha = 0.6f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "EXERCISES",
            style = MaterialTheme.typography.labelLarge,
            color = TextSecondary,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        exercises.forEachIndexed { index, ex ->
            EditableExerciseCard(
                exercise = ex,
                index = index,
                allExercises = sortedExercises,
                appearance = appearance,
                isFirst = index == 0,
                isLast = index == exercises.lastIndex,
                onUpdate = { i, updated ->
                    exercises = exercises.toMutableList().also { it[i] = updated }
                },
                onRemove = { confirmDeleteExerciseIndex = it },
                onMoveUp = {
                    val i = index
                    val mutable = exercises.toMutableList()
                    val temp = mutable[i]
                    mutable[i] = mutable[i - 1]
                    mutable[i - 1] = temp
                    exercises = mutable
                },
                onMoveDown = {
                    val i = index
                    val mutable = exercises.toMutableList()
                    val temp = mutable[i]
                    mutable[i] = mutable[i + 1]
                    mutable[i + 1] = temp
                    exercises = mutable
                }
            )
        }

        if (showAddExercise) {
            Spacer(modifier = Modifier.height(6.dp))
            GlowingCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    val hasExercises = allExercises.isNotEmpty()
                    var expanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { if (hasExercises) expanded = it }
                    ) {
                        OutlinedTextField(
                            value = exName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Exercise") },
                            placeholder = { Text(if (hasExercises) "SELECT EXERCISE" else "NO EXERCISES FOUND — ADD ONE FIRST") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = textFieldColors(appearance),
                            enabled = hasExercises,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace)
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            sortedExercises.forEach { exercise ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = exercise.name,
                                            color = TextPrimary,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    },
                                    onClick = {
                                        exName = exercise.name
                                        exType = if (exercise.type == "hold") "HOLD" else "REPS"
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (exType == "REPS") {
                        OutlinedTextField(
                            value = exTargetReps,
                            onValueChange = { exTargetReps = it.filter { c -> c.isDigit() } },
                            label = { Text("Target Reps") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = textFieldColors(appearance),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace)
                        )
                    } else {
                        OutlinedTextField(
                            value = exTargetHold,
                            onValueChange = { exTargetHold = it.filter { c -> c.isDigit() } },
                            label = { Text("Target Hold (seconds)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = textFieldColors(appearance),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .then(
                                    if (exUntilFailure) Modifier.background(appearance.systemAccentColor.copy(alpha = 0.2f))
                                    else Modifier.background(Color.Transparent)
                                )
                                .border(
                                    BorderStroke(1.dp, if (exUntilFailure) appearance.systemAccentColor else TextSecondary.copy(alpha = 0.3f)),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    exUntilFailure = !exUntilFailure
                                    if (exUntilFailure) {
                                        exTargetReps = ""
                                        exTargetHold = ""
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "UNTIL FAILURE",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (exUntilFailure) appearance.systemAccentColor else TextSecondary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = exSets,
                        onValueChange = { exSets = it.filter { c -> c.isDigit() } },
                        label = { Text("Sets") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors(appearance),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = exNotes,
                        onValueChange = { exNotes = it },
                        label = { Text("Notes (optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors(appearance),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    NeonButton(
                        text = "CONFIRM EXERCISE",
                        onClick = { addExercise() },
                        enabled = allExercises.isNotEmpty() && exName.isNotBlank() && (
                            exUntilFailure ||
                            (exType == "REPS" && (exTargetReps.toIntOrNull() ?: 0) > 0) ||
                            (exType == "HOLD" && (exTargetHold.toIntOrNull() ?: 0) > 0)
                        )
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { showAddExercise = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = appearance.systemAccentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ADD EXERCISE",
                        color = appearance.systemAccentColor,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .border(
                            BorderStroke(1.dp, Brush.linearGradient(listOf(appearance.systemAccentColor, Color.Transparent))),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) { }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                )
            ) {
                Text(
                    text = "CANCEL",
                    color = TextSecondary,
                    fontFamily = FontFamily.Monospace
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                NeonButton(
                    text = "SAVE PRESET",
                    onClick = {
                        val preset = WorkoutPreset(
                            id = preset?.id ?: java.util.UUID.randomUUID().toString(),
                            name = name.trim(),
                            description = description.trim(),
                            exercises = exercises,
                            difficulty = difficulty,
                            createdAt = preset?.createdAt ?: System.currentTimeMillis(),
                            isPinned = preset?.isPinned ?: false
                        )
                        onSave(preset)
                    },
                    enabled = name.isNotBlank() && exercises.isNotEmpty() && exercises.all { ex ->
                        ex.exerciseName.isNotBlank() && ex.sets >= 1 &&
                        (ex.isUntilFailure ||
                        if (ex.targetHoldSeconds > 0) ex.targetHoldSeconds >= 1
                        else ex.targetReps >= 1)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    confirmDeleteExerciseIndex?.let { idx ->
        val exName = exercises.getOrNull(idx)?.exerciseName ?: ""
        AlertDialog(
            onDismissRequest = { confirmDeleteExerciseIndex = null },
            title = { Text("REMOVE EXERCISE", color = TextPrimary) },
            text = { Text("Remove $exName from this preset?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    exercises = exercises.toMutableList().also { it.removeAt(idx) }
                    confirmDeleteExerciseIndex = null
                }) {
                    Text("REMOVE", color = appearance.pinnedSecondaryColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteExerciseIndex = null }) {
                    Text("CANCEL", color = TextSecondary)
                }
            },
            containerColor = Background
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditableExerciseCard(
    exercise: PresetExercise,
    index: Int,
    allExercises: List<Exercise>,
    appearance: com.example.prtracker.data.AppearanceSettings,
    isFirst: Boolean,
    isLast: Boolean,
    onUpdate: (Int, PresetExercise) -> Unit,
    onRemove: (Int) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val isHold = exercise.targetHoldSeconds > 0
    var expanded by remember { mutableStateOf(false) }

    GlowingCard(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 12.dp, bottom = 12.dp, end = 12.dp)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(end = 4.dp)
            ) {
                IconButton(
                    onClick = onMoveUp,
                    enabled = !isFirst,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Move up",
                        tint = if (isFirst) TextSecondary.copy(alpha = 0.3f) else appearance.systemAccentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onMoveDown,
                    enabled = !isLast,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Move down",
                        tint = if (isLast) TextSecondary.copy(alpha = 0.3f) else appearance.systemAccentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = exercise.exerciseName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Exercise") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        singleLine = true,
                        modifier = Modifier.weight(1f).menuAnchor(),
                        colors = textFieldColors(appearance),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = { onRemove(index) }, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove exercise",
                            tint = appearance.pinnedSecondaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    allExercises.forEach { ex ->
                        DropdownMenuItem(
                            text = {
                                Text(text = ex.name, color = TextPrimary, fontFamily = FontFamily.Monospace)
                            },
                            onClick = {
                                val isHoldType = ex.type == "hold"
                                onUpdate(index, exercise.copy(
                                    exerciseName = ex.name,
                                    targetReps = if (isHoldType) 0 else exercise.targetReps,
                                    targetHoldSeconds = if (isHoldType) exercise.targetHoldSeconds else 0
                                ))
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (exercise.isUntilFailure) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(appearance.systemAccentColor.copy(alpha = 0.15f))
                        .border(
                            BorderStroke(1.dp, appearance.systemAccentColor.copy(alpha = 0.5f)),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            onUpdate(index, exercise.copy(isUntilFailure = false))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "UNTIL FAILURE",
                        style = MaterialTheme.typography.labelSmall,
                        color = appearance.systemAccentColor,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
            } else {
                if (isHold) {
                    OutlinedTextField(
                        value = if (exercise.targetHoldSeconds > 0) exercise.targetHoldSeconds.toString() else "",
                        onValueChange = { text ->
                            val v = text.filter { it.isDigit() }.take(5)
                            onUpdate(index, exercise.copy(targetHoldSeconds = v.toIntOrNull() ?: 0))
                        },
                        label = { Text("HOLD SECONDS") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors(appearance),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace)
                    )
                } else {
                    OutlinedTextField(
                        value = if (exercise.targetReps > 0) exercise.targetReps.toString() else "",
                        onValueChange = { text ->
                            val v = text.filter { it.isDigit() }.take(5)
                            onUpdate(index, exercise.copy(targetReps = v.toIntOrNull() ?: 0))
                        },
                        label = { Text("TARGET REPS") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors(appearance),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Transparent)
                        .border(
                            BorderStroke(1.dp, TextSecondary.copy(alpha = 0.2f)),
                            RoundedCornerShape(6.dp)
                        )
                        .clickable {
                            onUpdate(index, exercise.copy(
                                isUntilFailure = true,
                                targetReps = 0,
                                targetHoldSeconds = 0
                            ))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "UNTIL FAILURE",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary.copy(alpha = 0.6f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = exercise.sets.toString(),
                onValueChange = { text ->
                    val v = text.filter { it.isDigit() }.take(3)
                    val sets = maxOf(v.toIntOrNull() ?: 1, 1)
                    onUpdate(index, exercise.copy(sets = sets))
                },
                label = { Text("SETS") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors(appearance),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = exercise.notes,
                onValueChange = { text ->
                    onUpdate(index, exercise.copy(notes = text))
                },
                label = { Text("NOTES (OPTIONAL)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors(appearance),
                textStyle = MaterialTheme.typography.bodyMedium
            )
        }
    }
    }
}

private fun difficultyColor(difficulty: String, appearance: com.example.prtracker.data.AppearanceSettings): Color {
    return when (difficulty) {
        "EASY" -> GoalComplete
        "MEDIUM" -> appearance.systemAccentColor
        "HARD" -> HardOrange
        "MAX EFFORT" -> appearance.pinnedSecondaryColor
        else -> appearance.systemAccentColor
    }
}

@Composable
private fun textFieldColors(appearance: com.example.prtracker.data.AppearanceSettings) = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedBorderColor = appearance.systemAccentColor,
    unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
    cursorColor = appearance.systemAccentColor,
    focusedLabelColor = appearance.systemAccentColor,
    unfocusedLabelColor = TextSecondary,
    focusedContainerColor = CardBackground,
    unfocusedContainerColor = CardBackground
)
