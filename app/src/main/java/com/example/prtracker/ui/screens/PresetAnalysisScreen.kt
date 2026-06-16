package com.example.prtracker.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.prtracker.data.Exercise
import com.example.prtracker.data.PresetExercise
import com.example.prtracker.ui.components.GlowingCard
import com.example.prtracker.ui.components.GridBackground
import com.example.prtracker.ui.theme.GoalComplete
import com.example.prtracker.ui.theme.Magenta
import com.example.prtracker.ui.theme.PinnedAccentSecondary
import com.example.prtracker.ui.theme.PrimaryAccent
import com.example.prtracker.ui.theme.SecondaryAccent
import com.example.prtracker.ui.theme.TextPrimary
import com.example.prtracker.ui.theme.TextSecondary
import com.example.prtracker.viewmodel.PRViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private enum class MovementCategory(val abbreviation: String) {
    VP("VP"),
    HP("HP"),
    CL("CL"),
    VPUSH("VPush"),
    HPUSH("HPush")
}

private data class CategoryScore(
    val category: MovementCategory,
    val rawScore: Int,
    val normalizedScore: Int
)

private val categoryOrder = listOf(
    MovementCategory.VP,
    MovementCategory.HP,
    MovementCategory.CL,
    MovementCategory.VPUSH,
    MovementCategory.HPUSH
)

private fun categoryColor(category: MovementCategory): Color = when (category) {
    MovementCategory.VP -> PrimaryAccent
    MovementCategory.HP -> SecondaryAccent
    MovementCategory.CL -> GoalComplete
    MovementCategory.VPUSH -> Magenta
    MovementCategory.HPUSH -> PinnedAccentSecondary
}

private fun classifyExercise(name: String): MovementCategory {
    val n = name.lowercase()

    // VP (Vertical Pull)
    val vpKeywords = listOf("pull up", "pullup", "pull-up", "chin up", "chinup", "chin-up", "muscle up", "lat", "typewriter")
    if (vpKeywords.any { n.contains(it) }) return MovementCategory.VP

    // HP (Horizontal Pull)
    val hpKeywords = listOf("row", "inverted row", "australian", "horizontal pull", "rear delt", "face pull")
    if (hpKeywords.any { n.contains(it) }) return MovementCategory.HP

    // VPush (Vertical Push)
    val vpushKeywords = listOf("handstand", "hspu", "overhead", "pike", "shoulder press", "military press", "dip")
    if (vpushKeywords.any { n.contains(it) }) return MovementCategory.VPUSH

    // HPush (Horizontal Push)
    val hpushKeywords = listOf("push up", "pushup", "push-up", "chest", "bench", "ring push", "archer push", "planche")
    if (hpushKeywords.any { n.contains(it) }) return MovementCategory.HPUSH

    // CL (Core/Legs) — catch-all
    return MovementCategory.CL
}

private fun computeScore(pe: PresetExercise, exercise: Exercise?): Int {
    if (exercise != null && exercise.entries.isNotEmpty()) {
        return exercise.entries.maxOf { it.value }
    }
    return maxOf(pe.targetReps, pe.targetHoldSeconds)
}

@Composable
fun PresetAnalysisScreen(
    presetId: String,
    viewModel: PRViewModel,
    navController: NavController
) {
    val presets by viewModel.workoutPresets.collectAsState()
    val exercises by viewModel.exercises.collectAsState()
    val preset = presets.find { it.id == presetId }

    if (preset == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            GridBackground()
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextSecondary)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "PRESET NOT FOUND",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    val rawScores = mutableMapOf<MovementCategory, Int>()
    for (cat in categoryOrder) rawScores[cat] = 0

    val exerciseBreakdown = preset.exercises.map { pe ->
        val exercise = exercises.find { e -> e.name.equals(pe.exerciseName, ignoreCase = true) }
        val category = classifyExercise(pe.exerciseName)
        val score = computeScore(pe, exercise)
        rawScores[category] = (rawScores[category] ?: 0) + score
        Triple(pe, category, score)
    }

    val maxRaw = rawScores.values.maxOrNull() ?: 1
    val categoryScores = categoryOrder.map { cat ->
        val raw = rawScores[cat] ?: 0
        val norm = if (maxRaw > 0) (raw.toFloat() / maxRaw.toFloat() * 100f).toInt().coerceIn(0, 100) else 0
        CategoryScore(cat, raw, norm)
    }

    val categoriesCovered = categoryScores.count { it.normalizedScore > 0 }

    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    Box(modifier = Modifier.fillMaxSize()) {
        GridBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = PrimaryAccent)
                }
                Text(
                    text = preset.name,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = PrimaryAccent
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "MOVEMENT PATTERN ANALYSIS",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.Monospace
                ),
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            GlowingCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "BALANCE PROFILE",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = PrimaryAccent
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val radarProgress by animateFloatAsState(
                        targetValue = if (started) 1f else 0f,
                        animationSpec = tween(1000, easing = FastOutSlowInEasing),
                        label = "radarProgress"
                    )

                    val textMeasurer = rememberTextMeasurer()
                    val labelStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    val values = categoryScores.map { it.normalizedScore }

                    Canvas(
                        modifier = Modifier
                            .size(220.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val maxRadius = min(size.width, size.height) / 2f - 24f
                        val angles = (0 until 5).map { i -> -PI / 2 + i * 2 * PI / 5 }

                        fun vertex(i: Int, radius: Float): Offset = Offset(
                            center.x + (radius * cos(angles[i])).toFloat(),
                            center.y + (radius * sin(angles[i])).toFloat()
                        )

                        val gridColor = PrimaryAccent.copy(alpha = 0.1f)
                        for (level in 1..10) {
                            val r = maxRadius * level / 10f
                            val path = Path().apply {
                                moveTo(vertex(0, r).x, vertex(0, r).y)
                                for (i in 1 until 5) lineTo(vertex(i, r).x, vertex(i, r).y)
                                close()
                            }
                            drawPath(path, color = gridColor, style = Stroke(1f))
                        }

                        val spokeColor = PrimaryAccent.copy(alpha = 0.2f)
                        for (i in 0 until 5) {
                            drawLine(spokeColor, center, vertex(i, maxRadius), strokeWidth = 1f)
                        }

                        val keys = listOf("VP", "HP", "CL", "VPush", "HPush")
                        val dataPath = Path().apply {
                            val p0 = vertex(0, maxRadius * (values[0] / 100f) * radarProgress)
                            moveTo(p0.x, p0.y)
                            for (i in 1 until 5) {
                                val p = vertex(i, maxRadius * (values[i] / 100f) * radarProgress)
                                lineTo(p.x, p.y)
                            }
                            close()
                        }
                        drawPath(dataPath, color = PrimaryAccent.copy(alpha = 0.2f))
                        drawPath(dataPath, color = PrimaryAccent.copy(alpha = 0.8f), style = Stroke(2.dp.toPx()))

                        val labelOffset = 14.dp.toPx()
                        for (i in 0 until 5) {
                            val pos = vertex(i, maxRadius + labelOffset)
                            val textResult = textMeasurer.measure(
                                AnnotatedString(keys[i]),
                                style = labelStyle
                            )
                            drawText(
                                textLayoutResult = textResult,
                                topLeft = Offset(
                                    pos.x - textResult.size.width / 2f,
                                    pos.y - textResult.size.height / 2f
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    categoryScores.forEachIndexed { index, cs ->
                        val cc = categoryColor(cs.category)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .padding(end = 8.dp)
                                    .background(cc, CircleShape)
                            )
                            Text(
                                text = cs.category.abbreviation,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = cc,
                                modifier = Modifier.width(56.dp)
                            )
                            Text(
                                text = "${cs.rawScore}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            GlowingCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "EXERCISE BREAKDOWN",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = PrimaryAccent
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    exerciseBreakdown.forEachIndexed { index, (pe, category, _) ->
                        if (index > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color(0xFF1A2540))
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(categoryColor(category))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = pe.exerciseName,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = category.abbreviation,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = categoryColor(category)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "COVERAGE: $categoriesCovered/5 MOVEMENT PATTERNS",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                ),
                color = TextSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
