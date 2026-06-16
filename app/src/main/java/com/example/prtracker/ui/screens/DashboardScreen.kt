package com.example.prtracker.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.example.prtracker.data.Exercise
import com.example.prtracker.data.RunningPREngine
import com.example.prtracker.data.SoundEngine
import com.example.prtracker.navigation.Routes
import com.example.prtracker.ui.components.AnimatedRing
import com.example.prtracker.ui.components.GlowingCard
import com.example.prtracker.ui.components.GridBackground
import com.example.prtracker.ui.components.NeonButton
import com.example.prtracker.ui.components.RunningCard
import com.example.prtracker.ui.components.SetGoalDialog
import com.example.prtracker.ui.theme.Background
import com.example.prtracker.ui.theme.CardBackground
import com.example.prtracker.ui.theme.GoalReachedColor
import com.example.prtracker.ui.theme.LocalAppearance
import com.example.prtracker.ui.theme.SuccessPurple
import com.example.prtracker.ui.theme.Surface
import com.example.prtracker.ui.theme.TextPrimary
import com.example.prtracker.ui.theme.TextSecondary
import com.example.prtracker.ui.theme.exerciseAccentColor
import com.example.prtracker.ui.theme.exerciseSecondaryColor
import com.example.prtracker.ui.theme.pinnedAccentColor
import com.example.prtracker.ui.theme.pinnedSecondaryColor
import com.example.prtracker.ui.theme.runningAccentColor
import com.example.prtracker.ui.theme.runningSecondaryColor
import com.example.prtracker.ui.theme.systemAccentColor
import com.example.prtracker.ui.theme.systemSecondaryColor
import com.example.prtracker.viewmodel.PRViewModel
import kotlin.math.roundToInt

private sealed class GridItem(val key: String) {
    class RunningSection : GridItem("running_section")
    class Header(val text: String, val color: Color) : GridItem("header_$text")
    class Divider : GridItem("divider")
    class CardItem(val exercise: Exercise) : GridItem("card_${exercise.id}")
}

private fun buildGridItems(
    exercises: List<Exercise>,
    query: String,
    pinnedAccentColor: Color
): List<GridItem> {
    val filtered = if (query.isBlank()) exercises
    else exercises.filter { it.name.contains(query, ignoreCase = true) }

    val pinned = filtered.filter { it.isPinned }.sortedBy { it.sortOrder }
    val unpinned = filtered.filter { !it.isPinned }.sortedBy { it.sortOrder }

    val items = mutableListOf<GridItem>()
    items.add(GridItem.RunningSection())
    if (pinned.isNotEmpty()) {
        items.add(GridItem.Header("PINNED", pinnedAccentColor))
        pinned.forEach { items.add(GridItem.CardItem(it)) }
        items.add(GridItem.Divider())
    }
    items.add(GridItem.Header("ALL EXERCISES", TextSecondary))
    unpinned.forEach { items.add(GridItem.CardItem(it)) }
    return items
}

@Composable
fun DashboardScreen(
    viewModel: PRViewModel,
    navController: NavHostController
) {
    val exercises by viewModel.exercises.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val appearance = LocalAppearance.current

    val gridItems = remember(exercises, searchQuery) {
        buildGridItems(exercises, searchQuery, appearance.pinnedAccentColor)
    }

    val itemPositions = remember { mutableStateMapOf<String, LayoutCoordinates>() }
    var draggingKey by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var dragStartPos by remember { mutableStateOf(Offset.Zero) }
    var hoverTargetKey by remember { mutableStateOf<String?>(null) }
    val shakeOffset = remember { Animatable(0f) }
    var shakeTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(shakeTrigger) {
        if (shakeTrigger > 0) {
            shakeOffset.snapTo(0f)
            shakeOffset.animateTo(-12f, animationSpec = spring(stiffness = Spring.StiffnessHigh))
            shakeOffset.animateTo(12f, animationSpec = spring(stiffness = Spring.StiffnessHigh))
            shakeOffset.animateTo(-8f, animationSpec = spring(stiffness = Spring.StiffnessHigh))
            shakeOffset.animateTo(8f, animationSpec = spring(stiffness = Spring.StiffnessHigh))
            shakeOffset.animateTo(0f, animationSpec = spring(stiffness = Spring.StiffnessHigh))
        }
    }

    val listState = rememberLazyGridState()

    val headerGlowBrush = remember {
        Brush.horizontalGradient(
            listOf(Color.Transparent, appearance.exerciseAccentColor, Color.Transparent)
        )
    }

    val hasContent = exercises.isNotEmpty() && gridItems.any { it is GridItem.CardItem }

    Box(modifier = Modifier.fillMaxSize()) {
        GridBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Background.copy(alpha = 0.95f))
            ) {
                Text(
                    text = "PR TRACKER",
                    style = MaterialTheme.typography.displayLarge,
                    color = appearance.exerciseAccentColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp, bottom = 8.dp)
                        .drawBehind {
                            drawRoundRect(
                                color = appearance.exerciseAccentColor.copy(alpha = 0.3f),
                                topLeft = Offset(size.width * 0.1f, size.height - 2.dp.toPx()),
                                size = Size(size.width * 0.8f, 2.dp.toPx()),
                                cornerRadius = CornerRadius(1.dp.toPx())
                            )
                        },
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Monospace
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search exercises...", color = TextSecondary) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextSecondary
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = appearance.exerciseAccentColor,
                        focusedBorderColor = appearance.exerciseAccentColor,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f),
                        focusedContainerColor = CardBackground,
                        unfocusedContainerColor = CardBackground
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(headerGlowBrush)
                )
            }

            if (hasContent) {
                LazyVerticalGrid(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = gridItems,
                        key = { it.key },
                        span = { item ->
                            when (item) {
                                is GridItem.RunningSection, is GridItem.Header, is GridItem.Divider -> GridItemSpan(2)
                                is GridItem.CardItem -> GridItemSpan(1)
                            }
                        }
                    ) { item ->
                        when (item) {
                            is GridItem.RunningSection -> {
                                val runEntries by viewModel.runEntries.collectAsState()
                                val runningPRs by viewModel.runningPRs.collectAsState()
                                RunningCard(
                                    runEntries = runEntries,
                                    runningPRs = runningPRs,
                                    onLogRun = { navController.navigate(Routes.LOG_RUN) },
                                    onViewAll = { navController.navigate(Routes.RUN_HISTORY) },
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                            is GridItem.Header -> {
                                SectionHeader(text = item.text, accentColor = item.color)
                            }
                            is GridItem.Divider -> {
                                SectionDivider()
                            }
                            is GridItem.CardItem -> {
                                val exercise = item.exercise
                                val isDragging = draggingKey == item.key
                                val isHoverTarget = hoverTargetKey == item.key && !isDragging

                                Box(modifier = Modifier.fillMaxWidth()) {
                                    if (isDragging) {
                                        val dragColors = if (exercise.isPinned)
                                            listOf(appearance.pinnedAccentColor.copy(alpha = 0.3f), appearance.pinnedSecondaryColor.copy(alpha = 0.3f))
                                        else
                                            listOf(appearance.exerciseAccentColor.copy(alpha = 0.3f), appearance.exerciseSecondaryColor.copy(alpha = 0.3f))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(200.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(CardBackground)
                                                .border(
                                                    1.dp,
                                                    Brush.linearGradient(dragColors),
                                                    RoundedCornerShape(16.dp)
                                                )
                                                .alpha(0.4f)
                                        )
                                    }
                                    ExerciseCard(
                                        exercise = exercise,
                                        modifier = (if (isHoverTarget) {
                                            Modifier.border(
                                                2.dp,
                                                 if (exercise.isPinned) appearance.pinnedAccentColor else appearance.exerciseAccentColor,
                                                RoundedCornerShape(16.dp)
                                            )
                                        } else if (!isDragging && shakeOffset.value != 0f) {
                                            Modifier.offset {
                                                IntOffset(shakeOffset.value.roundToInt(), 0)
                                            }
                                        } else {
                                            Modifier
                                        })
                                            .then(
                                                if (isDragging) {
                                                    Modifier
                                                        .zIndex(1f)
                                                        .graphicsLayer {
                                                            scaleX = 1.05f
                                                            scaleY = 1.05f
                                                            translationX = dragOffset.x
                                                            translationY = dragOffset.y
                                                            alpha = 0.9f
                                                            shadowElevation = 0f
                                                            ambientShadowColor = Color.Transparent
                                                            spotShadowColor = Color.Transparent
                                                        }
                                                } else Modifier
                                            )
                                            .onGloballyPositioned { coords ->
                                                itemPositions[item.key] = coords
                                            }
                                            .pointerInput(item.key) {
                                                detectDragGesturesAfterLongPress(
                                                    onDragStart = {
                                                        val c = itemPositions[item.key]
                                                        if (c != null) {
                                                            dragStartPos = c.positionInRoot()
                                                        }
                                                        draggingKey = item.key
                                                        dragOffset = Offset.Zero
                                                        hoverTargetKey = null
                                                    },
                                                    onDrag = { change, dragAmount ->
                                                        change.consume()
                                                        dragOffset += dragAmount
                                                        hoverTargetKey = findClosestKey(
                                                            draggedKey = item.key,
                                                            offset = dragOffset,
                                                            startPos = dragStartPos,
                                                            positions = itemPositions,
                                                            items = gridItems,
                                                            pinned = exercise.isPinned
                                                        )
                                                    },
                                                    onDragEnd = {
                                                        try {
                                                            if (draggingKey != null) {
                                                                val dc = itemPositions[item.key]
                                                                if (dc != null) {
                                                                    val targets = itemPositions.entries.toList().filter { entry ->
                                                                        entry.key != item.key && (gridItems.find { it.key == entry.key } as? GridItem.CardItem)?.exercise?.isPinned == exercise.isPinned
                                                                    }
                                                                    val cx = dragStartPos.x + dragOffset.x + dc.size.width / 2f
                                                                    val cy = dragStartPos.y + dragOffset.y + dc.size.height / 2f
                                                                    var bestKey: String? = null
                                                                    var bestDist = Float.MAX_VALUE
                                                                    for (entry in targets) {
                                                                        try {
                                                                            val ep = entry.value.positionInRoot()
                                                                            val ex = ep.x + entry.value.size.width / 2f
                                                                            val ey = ep.y + entry.value.size.height / 2f
                                                                            val dx = ex - cx
                                                                            val dy = ey - cy
                                                                            val d = dx * dx + dy * dy
                                                                            if (d < bestDist) {
                                                                                bestDist = d
                                                                                bestKey = entry.key
                                                                            }
                                                                        } catch (_: Exception) { }
                                                                    }
                                                                    bestKey?.let { targetKey ->
                                                                        val ti = gridItems.find { it.key == targetKey } as? GridItem.CardItem
                                                                        if (ti != null) {
                                                                            viewModel.swapExercises(exercise.id, ti.exercise.id)
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        } catch (_: Exception) { }
                                                        draggingKey = null
                                                        dragOffset = Offset.Zero
                                                        hoverTargetKey = null
                                                    },
                                                    onDragCancel = {
                                                        draggingKey = null
                                                        dragOffset = Offset.Zero
                                                        hoverTargetKey = null
                                                    }
                                                )
                                            },
                                        viewModel = viewModel,
                                        onClick = {
                                            if (draggingKey == null) {
                                                if (viewModel.appSettings.value.soundEnabled) SoundEngine.playTapExercise()
                                                navController.navigate(Routes.detail(exercise.id))
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (exercises.isEmpty()) "NO EXERCISES YET\n— ADD ONE —"
                        else "NO MATCHES",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NeonButton(
                    text = "EXERCISE HISTORY",
                    onClick = { navController.navigate(Routes.EXERCISE_HISTORY) },
                    modifier = Modifier.weight(1f)
                )
                NeonButton(
                    text = "WORKOUT HISTORY",
                    onClick = { navController.navigate(Routes.WORKOUT_HISTORY) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private fun findClosestKey(
    draggedKey: String,
    offset: Offset,
    startPos: Offset,
    positions: Map<String, LayoutCoordinates>,
    items: List<GridItem>,
    pinned: Boolean
): String? {
    val dc = positions[draggedKey] ?: return null
    val cx = startPos.x + offset.x + dc.size.width / 2f
    val cy = startPos.y + offset.y + dc.size.height / 2f
    var bestKey: String? = null
    var bestDist = Float.MAX_VALUE
    for ((key, coords) in positions) {
        if (key == draggedKey) continue
        val ci = items.find { it.key == key }
        if (ci !is GridItem.CardItem) continue
        if (ci.exercise.isPinned != pinned) continue
        try {
            val ep = coords.positionInRoot()
            val ex = ep.x + coords.size.width / 2f
            val ey = ep.y + coords.size.height / 2f
            val dx = ex - cx
            val dy = ey - cy
            val d = dx * dx + dy * dy
            if (d < bestDist) {
                bestDist = d
                bestKey = key
            }
        } catch (_: Exception) { }
    }
    return bestKey
}

@Composable
private fun SectionHeader(text: String, accentColor: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = accentColor,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun SectionDivider() {
    val appearance = LocalAppearance.current
    val dividerBrush = remember {
        Brush.horizontalGradient(
            listOf(Color.Transparent, appearance.pinnedAccentColor, appearance.pinnedSecondaryColor, Color.Transparent)
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .height(1.dp)
            .background(dividerBrush)
    )
}

@Composable
private fun ExerciseCard(
    exercise: Exercise,
    viewModel: PRViewModel,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }
    val appearance = LocalAppearance.current

    val currentPR = exercise.entries.maxOfOrNull { it.value } ?: 0
    val hasLoggedThisWeek = exercise.entries.any {
        val entryCal = java.util.Calendar.getInstance().apply { timeInMillis = it.date }
        val now = java.util.Calendar.getInstance()
        entryCal.get(java.util.Calendar.WEEK_OF_YEAR) == now.get(java.util.Calendar.WEEK_OF_YEAR) &&
                entryCal.get(java.util.Calendar.YEAR) == now.get(java.util.Calendar.YEAR)
    }

    val isGoalReached = exercise.goal != null && currentPR >= exercise.goal!!
    val goalProgress = if (exercise.goal != null && exercise.goal!! > 0)
        currentPR.toFloat() / exercise.goal!! else 0f

    val pinnedAccent = if (exercise.isPinned) appearance.pinnedAccentColor else appearance.exerciseAccentColor
    val pinnedAccentSecondary = if (exercise.isPinned) appearance.pinnedSecondaryColor else appearance.exerciseSecondaryColor

    val prColor = if (isGoalReached) GoalReachedColor else pinnedAccent

    val pinnedBorderBrush = remember(exercise.isPinned) {
        if (exercise.isPinned)
            Brush.linearGradient(listOf(appearance.pinnedAccentColor, appearance.pinnedSecondaryColor))
        else null
    }

    val pulseAlpha = remember { Animatable(0.4f) }
    LaunchedEffect(isGoalReached) {
        if (isGoalReached) {
            pulseAlpha.snapTo(0.4f)
            while (true) {
                pulseAlpha.animateTo(1f, tween(750))
                pulseAlpha.animateTo(0.4f, tween(750))
            }
        }
    }

    if (showGoalDialog) {
        SetGoalDialog(
            exerciseName = exercise.name,
            exerciseType = exercise.type,
            currentPR = currentPR,
            currentGoal = exercise.goal,
            onSave = { goalValue ->
                viewModel.setGoal(exercise.id, goalValue)
                showGoalDialog = false
            },
            onRemove = {
                viewModel.setGoal(exercise.id, null)
                showGoalDialog = false
            },
            onDismiss = { showGoalDialog = false }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Exercise", color = TextPrimary) },
            text = { Text("Delete \"${exercise.name}\" and all its entries?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    if (viewModel.appSettings.value.soundEnabled) SoundEngine.playDeleteExercise()
                    viewModel.deleteExercise(exercise.id)
                    showDeleteDialog = false
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

    Box {
        GlowingCard(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp)
                .clickable(enabled = true) { onClick() }
                .then(
                    if (isGoalReached) {
                        Modifier.border(
                            2.dp,
                            GoalReachedColor.copy(alpha = pulseAlpha.value),
                            RoundedCornerShape(16.dp)
                        )
                    } else Modifier
                ),
            borderBrush = if (isGoalReached) null else pinnedBorderBrush
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (exercise.isPinned) {
                        PinnedBadge()
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (hasLoggedThisWeek) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Logged this week",
                                tint = SuccessPurple,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        IconButton(
                            onClick = {
                                if (viewModel.appSettings.value.soundEnabled) SoundEngine.playTogglePin()
                                viewModel.togglePin(exercise.id)
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (exercise.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                contentDescription = if (exercise.isPinned) "Unpin exercise" else "Pin exercise",
                                tint = if (exercise.isPinned) pinnedAccent else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete exercise",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(0.5f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { showGoalDialog = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (exercise.goal != null) Icons.Filled.Flag else Icons.Outlined.Flag,
                            contentDescription = if (exercise.goal != null) "Edit Goal" else "Set Goal",
                            tint = if (isGoalReached) GoalReachedColor
                                   else if (exercise.goal != null) pinnedAccent
                                   else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(0.3f))

                Text(
                    text = when (exercise.type) {
                        "reps" -> "$currentPR"
                        "hold" -> "${currentPR}s"
                        else -> "$currentPR"
                    },
                    style = MaterialTheme.typography.displayLarge,
                    color = prColor,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (exercise.type == "reps") "REPS" else "HOLD",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    AnimatedRing(
                        progress = if (exercise.goal != null) goalProgress else 0.75f,
                        modifier = Modifier.size(36.dp),
                        colorStart = if (isGoalReached) GoalReachedColor else pinnedAccent,
                        colorEnd = if (isGoalReached) GoalReachedColor else pinnedAccentSecondary,
                        hasGoal = exercise.goal != null,
                        isGoalReached = isGoalReached,
                        isPinned = exercise.isPinned
                    )
                }
            }
        }
    }
}

@Composable
private fun PinnedBadge() {
    val appearance = LocalAppearance.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(appearance.pinnedAccentColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "PINNED",
            style = MaterialTheme.typography.labelSmall,
            color = Background,
            fontFamily = FontFamily.Monospace,
            maxLines = 1
        )
    }
}
