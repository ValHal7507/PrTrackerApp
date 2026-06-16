package com.example.prtracker.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.prtracker.data.CriterionStatus
import com.example.prtracker.data.TierResult
import com.example.prtracker.ui.components.GlowingCard
import com.example.prtracker.ui.components.GridBackground
import com.example.prtracker.ui.theme.Background
import com.example.prtracker.ui.theme.GoalComplete
import com.example.prtracker.ui.theme.LocalAppearance
import com.example.prtracker.ui.theme.PrimaryAccent
import com.example.prtracker.ui.theme.TextPrimary
import com.example.prtracker.ui.theme.pinnedSecondaryColor
import com.example.prtracker.ui.theme.systemAccentColor
import com.example.prtracker.ui.theme.systemSecondaryColor
import com.example.prtracker.ui.theme.TextSecondary
import com.example.prtracker.ui.theme.TierBootSequence
import com.example.prtracker.ui.theme.TierGravityRebel
import com.example.prtracker.ui.theme.TierKineticFlow
import com.example.prtracker.ui.theme.TierNeuralBridge
import com.example.prtracker.ui.theme.TierOverclockV1
import com.example.prtracker.ui.theme.TierQuantumLever
import com.example.prtracker.ui.theme.TierRawCarbon
import com.example.prtracker.ui.theme.TierStructuralPatch
import com.example.prtracker.ui.theme.TierSystemOverride
import com.example.prtracker.viewmodel.PRViewModel

@Composable
fun RankScreen(
    navController: NavController,
    viewModel: PRViewModel
) {
    val tierResult by viewModel.tierResult.collectAsState()

    var visible by remember { mutableStateOf(false) }
    val appearance = LocalAppearance.current
    LaunchedEffect(Unit) {
        withFrameNanos {}
        visible = true
    }
    val contentAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(500), label = "contentAlpha"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        GridBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .alpha(contentAlpha)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RankTerminalHeader()

            ActiveTierCard(tierResult = tierResult)

            if (tierResult.currentTier < 10 && tierResult.nextTierCriteria.isNotEmpty()) {
                NextUnlockSection(tierResult = tierResult)
            }

            FullProgressionMatrix(tierResult = tierResult)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "// criteria: achieve any 2 of 3 per tier to unlock",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun RankTerminalHeader() {
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "cursorAlpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "> RANK EVALUATOR",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, letterSpacing = 2.sp),
            color = TextSecondary,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = "|",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
            color = TextSecondary.copy(alpha = cursorAlpha),
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun ActiveTierCard(tierResult: TierResult) {
    val appearance = LocalAppearance.current
    val isUnranked = tierResult.currentTier == 0
    val tierColor = if (isUnranked) TextSecondary else tierResult.tierColor

    val borderBrush = remember(tierResult.currentTier) {
        if (isUnranked) null
        else if (tierResult.currentTier == 7) {
            Brush.linearGradient(listOf(appearance.systemAccentColor, appearance.systemSecondaryColor))
        } else {
            Brush.linearGradient(listOf(tierResult.tierColor, tierResult.tierColor))
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = tierResult.overallProgress,
        animationSpec = tween(1000), label = "progress"
    )

    val progressBarBrush = remember(tierResult.currentTier) {
        if (isUnranked) Brush.linearGradient(listOf(appearance.systemAccentColor, appearance.systemAccentColor))
        else Brush.linearGradient(listOf(appearance.systemSecondaryColor, tierResult.tierColor))
    }

    GlowingCard(
        modifier = Modifier.fillMaxWidth(),
        borderBrush = borderBrush
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isUnranked) "--" else "%02d".format(tierResult.currentTier),
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 56.sp),
                color = tierColor,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.graphicsLayer {
                    shadowElevation = 16f
                    ambientShadowColor = tierColor
                    spotShadowColor = tierColor
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = tierResult.tierName,
                style = MaterialTheme.typography.headlineMedium.copy(letterSpacing = 3.sp),
                color = tierColor,
                fontFamily = FontFamily.Monospace
            )

            if (isUnranked) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "// awaiting input data",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    color = TextSecondary,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(TextSecondary.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(progressBarBrush)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "TIER ${tierResult.currentTier} / 10",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = TextSecondary,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun NextUnlockSection(tierResult: TierResult) {
    Text(
        text = "> NEXT: ${tierResult.nextTierName}",
        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, letterSpacing = 2.sp),
        color = TextSecondary,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier.padding(top = 16.dp)
    )

    GlowingCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            tierResult.nextTierCriteria.forEachIndexed { index, criterion ->
                CriterionRow(criterion = criterion)
                if (index < tierResult.nextTierCriteria.size - 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(TextSecondary.copy(alpha = 0.1f))
                    )
                }
            }
        }
    }
}

@Composable
private fun CriterionRow(criterion: CriterionStatus) {
    val appearance = LocalAppearance.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (criterion.met) Icons.Default.CheckCircle else Icons.Default.Lock,
            contentDescription = if (criterion.met) "Met" else "Not met",
            tint = if (criterion.met) GoalComplete else appearance.pinnedSecondaryColor.copy(alpha = 0.7f),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = criterion.label,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = TextPrimary,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (criterion.userBest == 0) "your best: \u2014" else "your best: ${criterion.userBest}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = TextSecondary
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(
                    if (criterion.met) GoalComplete.copy(alpha = 0.15f)
                    else appearance.pinnedSecondaryColor.copy(alpha = 0.15f)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "${criterion.userBest} / ${criterion.required}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = if (criterion.met) GoalComplete else TextSecondary,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun FullProgressionMatrix(tierResult: TierResult) {
    Text(
        text = "> FULL PROGRESSION MATRIX",
        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, letterSpacing = 2.sp),
        color = TextSecondary,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier.padding(top = 16.dp)
    )

    GlowingCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            for (tierNumber in 10 downTo 1) {
                TierMatrixRow(
                    tierNumber = tierNumber,
                    isUnlocked = tierNumber <= tierResult.currentTier,
                    isNext = tierNumber == tierResult.nextTierNumber
                )
                if (tierNumber > 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(TextSecondary.copy(alpha = 0.1f))
                    )
                }
            }
        }
    }
}

@Composable
private fun TierMatrixRow(
    tierNumber: Int,
    isUnlocked: Boolean,
    isNext: Boolean
) {
    val tierColor = tierNumberToColor(tierNumber)
    val rowAlpha = when {
        isUnlocked -> 1f
        isNext -> 0.8f
        else -> 0.3f
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .alpha(rowAlpha)
            .drawBehind {
                if (isUnlocked) {
                    drawLine(
                        color = tierColor,
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height),
                        strokeWidth = 3.dp.toPx()
                    )
                } else if (isNext) {
                    drawLine(
                        color = tierColor.copy(alpha = 0.6f),
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height),
                        strokeWidth = 3.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(8.dp.toPx(), 6.dp.toPx()), 0f
                        )
                    )
                }
            }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val numberColor = when {
            isUnlocked -> tierColor
            isNext -> tierColor.copy(alpha = 0.6f)
            else -> TextSecondary
        }
        Text(
            text = "%02d".format(tierNumber),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
            color = numberColor,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(32.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        val nameColor = if (isUnlocked) TextPrimary else TextSecondary
        Text(
            text = tierNumberToName(tierNumber),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            color = nameColor,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (isUnlocked) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Unlocked",
                tint = GoalComplete,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked",
                tint = if (isNext) TextSecondary else TextSecondary.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun tierNumberToColor(number: Int): Color = when (number) {
    1 -> TierRawCarbon
    2 -> TierBootSequence
    3 -> TierStructuralPatch
    4 -> TierOverclockV1
    5 -> TierNeuralBridge
    6 -> TierKineticFlow
    7 -> PrimaryAccent
    8 -> TierGravityRebel
    9 -> TierQuantumLever
    10 -> TierSystemOverride
    else -> TextSecondary
}

private fun tierNumberToName(number: Int): String = when (number) {
    1 -> "RAW CARBON"
    2 -> "BOOT SEQUENCE"
    3 -> "STRUCTURAL PATCH"
    4 -> "OVERCLOCK V1"
    5 -> "NEURAL BRIDGE"
    6 -> "KINETIC FLOW"
    7 -> "CHASSIS APEX"
    8 -> "GRAVITY REBEL"
    9 -> "QUANTUM LEVER"
    10 -> "SYSTEM OVERRIDE"
    else -> ""
}
