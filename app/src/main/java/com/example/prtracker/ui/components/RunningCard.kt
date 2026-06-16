package com.example.prtracker.ui.components

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.prtracker.data.RunningPRs
import com.example.prtracker.data.RunningPREngine
import com.example.prtracker.ui.theme.Background
import com.example.prtracker.ui.theme.CardBackground
import com.example.prtracker.ui.theme.LocalAppearance
import com.example.prtracker.ui.theme.TextPrimary
import com.example.prtracker.ui.theme.TextSecondary
import com.example.prtracker.ui.theme.runningAccentColor
import com.example.prtracker.ui.theme.runningSecondaryColor

@Composable
fun RunningCard(
    runEntries: List<com.example.prtracker.data.RunEntry>,
    runningPRs: RunningPRs,
    onLogRun: () -> Unit,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appearance = LocalAppearance.current
    val amberBorderBrush = remember(appearance.runningAccent, appearance.runningSecondary) {
        Brush.linearGradient(listOf(appearance.runningAccentColor, appearance.runningSecondaryColor))
    }

    val isEmpty = runEntries.isEmpty()

    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000)
        ),
        label = "pulseAlpha"
    )

    GlowingCard(
        modifier = modifier.fillMaxWidth(),
        borderBrush = amberBorderBrush
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isEmpty) {
                        Modifier.border(
                            2.dp,
                            appearance.runningAccentColor.copy(alpha = pulseAlpha),
                            RoundedCornerShape(16.dp)
                        )
                    } else Modifier
                )
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsRun,
                        contentDescription = "Running",
                        tint = appearance.runningAccentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RUNNING",
                        style = MaterialTheme.typography.labelLarge,
                        color = appearance.runningAccentColor,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "VIEW ALL",
                        style = MaterialTheme.typography.labelSmall,
                        color = appearance.runningSecondaryColor,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.clickable { onViewAll() }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isEmpty) {
                    Text(
                        text = "NO RUNS YET",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    val bestDistance = runningPRs.bestDistanceMeters
                    Text(
                        text = if (bestDistance != null) RunningPREngine.formatDistance(bestDistance) else "—",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = MaterialTheme.typography.headlineSmall.fontSize
                        ),
                        color = appearance.runningAccentColor,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "BEST RUN",
                        style = MaterialTheme.typography.labelSmall,
                        color = appearance.runningSecondaryColor
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MiniStatColumn(
                            value = if (runningPRs.bestPaceSecondsPerKm != null)
                                RunningPREngine.formatPace(runningPRs.bestPaceSecondsPerKm) else "—",
                            label = "BEST PACE"
                        )
                        MiniStatColumn(
                            value = runningPRs.totalRuns.toString(),
                            label = "RUNS"
                        )
                        MiniStatColumn(
                            value = "${runningPRs.totalDistanceMeters.toInt() / 1000 * 65} kcal",
                            label = "CALORIES"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(appearance.runningAccentColor.copy(alpha = 0.12f))
                        .border(
                            BorderStroke(1.dp, appearance.runningAccentColor),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onLogRun() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Log a run",
                            tint = appearance.runningAccentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LOG A RUN",
                            style = MaterialTheme.typography.labelLarge,
                            color = appearance.runningAccentColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniStatColumn(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    val appearance = LocalAppearance.current
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = appearance.runningAccentColor,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = appearance.runningSecondaryColor
        )
    }
}
