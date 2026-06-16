package com.example.prtracker.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prtracker.data.Exercise
import com.example.prtracker.ui.theme.GoalComplete
import com.example.prtracker.ui.theme.PinnedAccentSecondary
import com.example.prtracker.ui.theme.PrimaryAccent
import com.example.prtracker.ui.theme.SecondaryAccent
import com.example.prtracker.ui.theme.TextPrimary
import com.example.prtracker.ui.theme.TextSecondary
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

fun mapExercisesToVectors(exercises: List<Exercise>): Map<String, Float> {
    val vectorMap = mapOf(
        "VP" to listOf("pull-up", "pullup", "chin-up", "chinup", "muscle-up", "muscleup"),
        "HP" to listOf("front lever", "row"),
        "VPush" to listOf("handstand", "hspu", "dip"),
        "HPush" to listOf("push-up", "pushup", "planche"),
        "CL" to listOf("l-sit", "lsit", "leg raise", "dragon flag")
    )

    val result = mutableMapOf<String, Float>()
    for ((vector, keywords) in vectorMap) {
        val matchedExercises = exercises.filter { ex ->
            keywords.any { keyword -> ex.name.lowercase().contains(keyword) }
        }
        val total = matchedExercises.fold(0f) { acc, ex ->
            val pr = ex.entries.maxOfOrNull { it.value } ?: 0
            acc + min(10f, pr / 10f)
        }.coerceAtMost(10f)
        result[vector] = total
    }
    return result
}

fun computeAsymmetryIndex(vectors: Map<String, Float>): Float {
    val vp = vectors["VP"] ?: 0f
    val hp = vectors["HP"] ?: 0f
    val vpush = vectors["VPush"] ?: 0f
    val hpush = vectors["HPush"] ?: 0f
    val pullTotal = vp + hp
    val pushTotal = vpush + hpush
    val denominator = max(pullTotal, pushTotal)
    if (denominator == 0f) return 0f
    return abs(pullTotal - pushTotal) / denominator * 100f
}

@Composable
fun BiomechanicalRadarCard(exercises: List<Exercise>) {
    val vectors = remember(exercises) { mapExercisesToVectors(exercises) }
    val ai = remember(vectors) { computeAsymmetryIndex(vectors) }
    val hasData = vectors.values.any { it > 0f }
    val isAsymmetric = ai > 25f

    val infiniteTransition = rememberInfiniteTransition(label = "aberration")
    val aberrationOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "aberrationOffset"
    )

    GlowingCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.BarChart,
                    contentDescription = null,
                    tint = PrimaryAccent
                )
                Text(
                    text = "BIOMECHANICAL BALANCE ENGINE",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (hasData) {
                val textMeasurer = rememberTextMeasurer()
                val labelStyle = TextStyle(
                    color = TextPrimary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )

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

                    val dataColor = if (isAsymmetric) SecondaryAccent else PrimaryAccent
                    val keys = listOf("VP", "HP", "VPush", "HPush", "CL")
                    val dataPath = Path().apply {
                        val p0 = vertex(0, maxRadius * (vectors[keys[0]] ?: 0f) / 10f)
                        moveTo(p0.x, p0.y)
                        for (i in 1 until 5) {
                            val p = vertex(i, maxRadius * (vectors[keys[i]] ?: 0f) / 10f)
                            lineTo(p.x, p.y)
                        }
                        close()
                    }
                    drawPath(dataPath, color = dataColor.copy(alpha = 0.2f))
                    drawPath(dataPath, color = dataColor.copy(alpha = 0.8f), style = Stroke(2.dp.toPx()))

                    if (isAsymmetric) {
                        val aberrationColor = SecondaryAccent.copy(alpha = 0.15f)
                        val lineSpacing = 8.dp.toPx()
                        val offset = aberrationOffset * lineSpacing
                        val startY = -lineSpacing + offset
                        val totalLines = (size.height / lineSpacing).toInt() + 3
                        for (i in 0 until totalLines) {
                            val y = startY + i * lineSpacing
                            if (y in 0f..size.height) {
                                drawLine(
                                    aberrationColor,
                                    Offset(0f, y),
                                    Offset(size.width, y),
                                    strokeWidth = 1f
                                )
                            }
                        }
                    }

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

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Structural Deviation: ${"%.1f".format(ai)}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isAsymmetric) PinnedAccentSecondary else GoalComplete
                )
                if (isAsymmetric) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "CRITICAL_ASYMMETRY // ANTERIOR_CHAIN_DEFICIT",
                        style = MaterialTheme.typography.labelSmall,
                        color = SecondaryAccent
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                val vp = vectors["VP"] ?: 0f
                val hp = vectors["HP"] ?: 0f
                val vpush = vectors["VPush"] ?: 0f
                val hpush = vectors["HPush"] ?: 0f
                val pullTotal = vp + hp
                val pushTotal = vpush + hpush
                val actionHint = if (pullTotal < pushTotal) {
                    "Increase Pull training"
                } else {
                    "Increase Push training"
                }
                Text(
                    text = actionHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            } else {
                Text(
                    text = "LOG EXERCISES TO SCAN BALANCE",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
