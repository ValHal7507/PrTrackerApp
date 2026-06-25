package com.example.prtracker.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.prtracker.data.Goal
import com.example.prtracker.data.LeverageTelemetry
import com.example.prtracker.navigation.Routes
import com.example.prtracker.ui.components.GlowingCard
import com.example.prtracker.ui.components.GridBackground
import com.example.prtracker.ui.components.NeonButton
import com.example.prtracker.ui.theme.Background
import com.example.prtracker.ui.theme.GoalComplete
import com.example.prtracker.ui.theme.LocalAppearance
import com.example.prtracker.ui.theme.TextPrimary
import com.example.prtracker.ui.theme.TextSecondary
import com.example.prtracker.ui.theme.pinnedSecondaryColor
import com.example.prtracker.ui.theme.systemAccentColor
import com.example.prtracker.ui.theme.systemSecondaryColor
import com.example.prtracker.viewmodel.PRViewModel

@Composable
fun GoalsScreen(
    viewModel: PRViewModel,
    navController: NavHostController
) {
    val goals by viewModel.goals.collectAsState()
    val exercises by viewModel.exercises.collectAsState()
    val weightEntries by viewModel.weightEntries.collectAsState()
    val allTelemetry by viewModel.allTelemetry.collectAsState()
    val isLoadingTelemetry by viewModel.isLoadingTelemetry.collectAsState()
    val appearance = LocalAppearance.current

    LaunchedEffect(Unit) {
        viewModel.loadAllTelemetry()
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearAllTelemetry() }
    }

    val telemetryExercises by remember {
        derivedStateOf {
            exercises.filter { exercise ->
                allTelemetry[exercise.id]?.isNotEmpty() == true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GridBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "GOALS",
                style = MaterialTheme.typography.displayLarge,
                color = appearance.systemAccentColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, bottom = 8.dp)
                    .drawBehind {
                        drawRoundRect(
                            color = appearance.systemAccentColor.copy(alpha = 0.3f),
                            topLeft = Offset(size.width * 0.1f, size.height - 2.dp.toPx()),
                            size = Size(size.width * 0.8f, 2.dp.toPx()),
                            cornerRadius = CornerRadius(1.dp.toPx())
                        )
                    },
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Monospace
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                if (goals.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "NO GOALS YET\n— ADD ONE —",
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(goals, key = { it.id }) { goal ->
                        val progress by remember(viewModel, goal) {
                            derivedStateOf { viewModel.getProgressForGoal(goal) }
                        }
                        val progressPercent by remember(viewModel, goal) {
                            derivedStateOf { viewModel.getProgressPercent(goal) }
                        }
                        val timeRemaining by remember(viewModel, goal) {
                            derivedStateOf { viewModel.getTimeRemaining(goal) }
                        }
                        GoalCard(
                            goal = goal,
                            progress = progress,
                            progressPercent = progressPercent,
                            timeRemaining = timeRemaining,
                            onDelete = { viewModel.deleteGoal(goal.id) }
                        )
                    }
                }

                item {
                    RankEvaluatorButton(navController = navController)
                }

                item {
                    RestGameButton(navController = navController)
                }

                item {
                    PetDiceRollButton(navController = navController)
                }

                item {
                    RsiSectionHeader()
                }

                if (isLoadingTelemetry) {
                    item {
                        RsiLoadingDots()
                    }
                } else if (weightEntries.isEmpty()) {
                    item {
                        RsiEmptyState(navController = navController)
                    }
                } else if (telemetryExercises.isNotEmpty()) {
                    items(telemetryExercises, key = { it.id }) { exercise ->
                        val telemetry = allTelemetry[exercise.id] ?: emptyList()
                        val currentRsi = viewModel.getCurrentRsi(exercise.id)
                        val delta = viewModel.getRsiDelta(exercise.id)
                        val statusLabel = viewModel.getRsiStatusLabel(delta)
                        val lastPairedWeight = telemetry.lastOrNull()?.pairedWeight ?: 0f
                        RsiCard(
                            exerciseName = exercise.name,
                            exerciseType = exercise.type,
                            telemetry = telemetry,
                            currentRsi = currentRsi,
                            delta = delta,
                            statusLabel = statusLabel,
                            lastPairedWeight = lastPairedWeight
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { navController.navigate(Routes.ADD_GOAL) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = appearance.systemAccentColor.copy(alpha = 0.15f),
            contentColor = appearance.systemAccentColor,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add goal",
                tint = appearance.systemAccentColor
            )
        }
    }
}

@Composable
private fun RsiSectionHeader() {
    val appearance = LocalAppearance.current
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .drawBehind {
                    drawRect(
                        brush = Brush.horizontalGradient(
                            listOf(Color.Transparent, appearance.systemAccentColor, Color.Transparent)
                        )
                    )
                }
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "\u26A1 LEVERAGE INDEX",
            style = MaterialTheme.typography.displayLarge,
            color = appearance.systemAccentColor,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRoundRect(
                        color = appearance.systemAccentColor.copy(alpha = 0.3f),
                        topLeft = Offset(size.width * 0.1f, size.height - 2.dp.toPx()),
                        size = Size(size.width * 0.8f, 2.dp.toPx()),
                        cornerRadius = CornerRadius(1.dp.toPx())
                    )
                },
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "TRUE STRENGTH RELATIVE TO BODY WEIGHT",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun RsiLoadingDots() {
    val appearance = LocalAppearance.current
    val infiniteTransition = rememberInfiniteTransition(label = "loadingDots")
    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "dot1"
    )
    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "dot2"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(appearance.systemAccentColor.copy(alpha = alpha1))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(appearance.systemAccentColor.copy(alpha = alpha2))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(appearance.systemAccentColor.copy(alpha = alpha1))
        )
    }
}

@Composable
private fun RsiEmptyState(navController: NavHostController) {
    val appearance = LocalAppearance.current
    GlowingCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "\u26A1 RSI TRACKING UNAVAILABLE",
                style = MaterialTheme.typography.titleLarge,
                color = appearance.systemAccentColor,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Log your body weight to unlock your true strength index.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            NeonButton(
                text = "GO TO WEIGHT LOG",
                onClick = { navController.navigate(Routes.WEIGHT) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun RsiCard(
    exerciseName: String,
    exerciseType: String,
    telemetry: List<LeverageTelemetry>,
    currentRsi: Int?,
    delta: Int?,
    statusLabel: String,
    lastPairedWeight: Float
) {
    val appearance = LocalAppearance.current
    val deltaColor = when {
        delta == null || delta == 0 -> TextSecondary
        delta > 0 -> GoalComplete
        else -> appearance.pinnedSecondaryColor
    }
    val statusBadgeColor = when (statusLabel) {
        "OVERLOAD" -> GoalComplete.copy(alpha = 0.2f)
        "DELOAD" -> appearance.pinnedSecondaryColor.copy(alpha = 0.2f)
        else -> TextSecondary.copy(alpha = 0.2f)
    }
    val statusTextColor = when (statusLabel) {
        "OVERLOAD" -> GoalComplete
        "DELOAD" -> appearance.pinnedSecondaryColor
        else -> TextSecondary
    }

    GlowingCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(start = 8.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MiniSparkChart(
                telemetry = telemetry,
                modifier = Modifier
                    .width(80.dp)
                    .height(60.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = exerciseName,
                    style = MaterialTheme.typography.titleMedium,
                    color = appearance.systemAccentColor,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "CURRENT RSI: ${currentRsi ?: "---"} pts",
                    style = MaterialTheme.typography.bodyLarge,
                    color = appearance.systemAccentColor,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val deltaText = when {
                        delta == null -> ""
                        delta >= 0 -> "+$delta pts"
                        else -> "$delta pts"
                    }
                    if (deltaText.isNotEmpty()) {
                        Text(
                            text = deltaText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = deltaColor,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(statusBadgeColor)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusTextColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "LAST LOGGED AT: ${"%.1f".format(lastPairedWeight)} kg",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(appearance.systemSecondaryColor.copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (exerciseType == "reps") "REPS" else "HOLD",
                    style = MaterialTheme.typography.labelSmall,
                    color = appearance.systemSecondaryColor,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun MiniSparkChart(
    telemetry: List<LeverageTelemetry>,
    modifier: Modifier = Modifier
) {
    val appearance = LocalAppearance.current
    val points = telemetry.takeLast(5)

    Canvas(modifier = modifier) {
        if (points.size < 2) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            drawCircle(
                color = appearance.systemSecondaryColor,
                radius = 4.dp.toPx(),
                center = Offset(centerX, centerY)
            )
            drawLine(
                color = appearance.systemSecondaryColor.copy(alpha = 0.3f),
                start = Offset(centerX - 8.dp.toPx(), centerY),
                end = Offset(centerX + 8.dp.toPx(), centerY),
                strokeWidth = 1.dp.toPx()
            )
        } else {
            val values = points.map { it.rsiScore.toFloat() }
            val minVal = values.min()
            val maxVal = values.max()
            val range = (maxVal - minVal).coerceAtLeast(1f)

            val padding = 4.dp.toPx()
            val chartWidth = size.width - padding * 2f
            val chartHeight = size.height - padding * 2f

            val stepX = if (values.size > 1) chartWidth / (values.size - 1) else chartWidth

            val linePoints = values.mapIndexed { index, value ->
                val x = padding + index * stepX
                val y = padding + chartHeight - ((value - minVal) / range * chartHeight)
                Offset(x, y)
            }

            for (i in 0 until linePoints.size - 1) {
                drawLine(
                    color = appearance.systemSecondaryColor,
                    start = linePoints[i],
                    end = linePoints[i + 1],
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            linePoints.forEach { point ->
                drawCircle(
                    color = appearance.systemSecondaryColor,
                    radius = 3.dp.toPx(),
                    center = point
                )
            }
        }
    }
}

@Composable
private fun GoalCard(
    goal: Goal,
    progress: Int,
    progressPercent: Float,
    timeRemaining: String,
    onDelete: () -> Unit
) {
    val appearance = LocalAppearance.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    val isComplete = progressPercent >= 1f

    val completedBorderBrush = remember {
        Brush.linearGradient(listOf(GoalComplete, GoalComplete.copy(alpha = 0.6f)))
    }
    val borderBrush = if (isComplete) completedBorderBrush else null

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Goal", color = TextPrimary) },
            text = { Text("Delete goal \"${goal.exerciseName}\"?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("Delete", color = appearance.systemAccentColor)
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

    GlowingCard(
        modifier = Modifier.fillMaxWidth(),
        borderBrush = borderBrush
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
        ) {
            if (isComplete) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 4.dp, end = 4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(GoalComplete.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "COMPLETED",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoalComplete,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 8.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProgressRing(
                    progress = progressPercent,
                    ringColor = if (isComplete) GoalComplete else appearance.systemAccentColor,
                    modifier = Modifier.size(72.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = goal.exerciseName,
                        style = MaterialTheme.typography.titleMedium,
                        color = appearance.systemAccentColor,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    val unit = if (goal.type == "reps") "reps" else "seconds"
                    Text(
                        text = "$progress / ${goal.targetValue} $unit",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(appearance.systemSecondaryColor.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = goal.period.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = appearance.systemSecondaryColor,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = timeRemaining,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete goal",
                        tint = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun RankEvaluatorButton(navController: NavHostController) {
    val appearance = LocalAppearance.current
    val accent = appearance.systemAccentColor
    val borderBrush = remember {
        Brush.linearGradient(listOf(accent, Color.Transparent))
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = { navController.navigate(Routes.RANK) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = accent.copy(alpha = 0.15f)
            )
        ) {
            Icon(
                imageVector = Icons.Default.Stars,
                contentDescription = "Rank Evaluator",
                tint = accent,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "RANK EVALUATOR",
                color = accent,
                style = MaterialTheme.typography.titleLarge,
                fontFamily = FontFamily.Monospace
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    BorderStroke(1.dp, borderBrush),
                    RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) { }
    }
}

@Composable
private fun RestGameButton(navController: NavHostController) {
    val appearance = LocalAppearance.current
    val accent = appearance.systemAccentColor
    val borderBrush = remember {
        Brush.linearGradient(listOf(accent, Color.Transparent))
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = { navController.navigate(Routes.REST_GAME) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = accent.copy(alpha = 0.15f)
            )
        ) {
            Icon(
                imageVector = Icons.Default.Stars,
                contentDescription = "Rest Game",
                tint = accent,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "REST GAME",
                color = accent,
                style = MaterialTheme.typography.titleLarge,
                fontFamily = FontFamily.Monospace
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    BorderStroke(1.dp, borderBrush),
                    RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) { }
    }
}

@Composable
private fun PetDiceRollButton(navController: NavHostController) {
    val appearance = LocalAppearance.current
    val accent = appearance.systemAccentColor
    val borderBrush = remember {
        Brush.linearGradient(listOf(accent, Color.Transparent))
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = { navController.navigate(Routes.DICE_ROLL) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = accent.copy(alpha = 0.15f)
            )
        ) {
            Icon(
                imageVector = Icons.Default.Casino,
                contentDescription = "Pet Dice Roll",
                tint = accent,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "PET DICE ROLL",
                color = accent,
                style = MaterialTheme.typography.titleLarge,
                fontFamily = FontFamily.Monospace
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    BorderStroke(1.dp, borderBrush),
                    RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) { }
    }
}

@Composable
private fun ProgressRing(
    progress: Float,
    ringColor: Color,
    modifier: Modifier = Modifier
) {
    val animatedProgress = remember(progress) { Animatable(if (progress > 0f) 1f else 0f) }
    LaunchedEffect(progress) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = progress.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 1000)
        )
    }

    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = modifier) {
            val strokeWidth = 6.dp.toPx()
            val arcSize = Size(
                width = size.width - strokeWidth,
                height = size.height - strokeWidth
            )
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

            drawArc(
                color = Color(0xFF0D1526),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress.value,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            ),
            color = ringColor
        )
    }
}


