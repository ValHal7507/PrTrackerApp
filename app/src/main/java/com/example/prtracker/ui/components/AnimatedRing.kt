package com.example.prtracker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prtracker.ui.theme.GoalReachedColor
import com.example.prtracker.ui.theme.GoalWarningColor
import com.example.prtracker.ui.theme.LocalAppearance
import com.example.prtracker.ui.theme.PrimaryAccent
import com.example.prtracker.ui.theme.SecondaryAccent
import com.example.prtracker.ui.theme.Surface
import com.example.prtracker.ui.theme.exerciseAccentColor
import com.example.prtracker.ui.theme.exerciseSecondaryColor
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedRing(
    progress: Float,
    modifier: Modifier = Modifier,
    colorStart: Color = LocalAppearance.current.exerciseAccentColor,
    colorEnd: Color = LocalAppearance.current.exerciseSecondaryColor,
    hasGoal: Boolean = false,
    isGoalReached: Boolean = false,
    isPinned: Boolean = false
) {
    val textMeasurer = rememberTextMeasurer()
    val defaultBrush = remember(colorStart, colorEnd) {
        Brush.sweepGradient(
            colors = listOf(colorStart, colorEnd),
            center = Offset(30f, 30f)
        )
    }
    val warningBrush = remember {
        Brush.sweepGradient(
            colors = listOf(GoalWarningColor, GoalReachedColor),
            center = Offset(30f, 30f)
        )
    }

    val ringColor by animateColorAsState(
        targetValue = when {
            isGoalReached -> GoalReachedColor
            hasGoal && progress >= 0.75f -> GoalWarningColor
            else -> colorStart
        },
        animationSpec = tween(500)
    )

    Canvas(modifier = modifier.size(60.dp)) {
        val strokeWidth = 6.dp.toPx()
        val arcSize = Size(
            width = size.width - strokeWidth,
            height = size.height - strokeWidth
        )
        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
        val center = Offset(size.width / 2f, size.height / 2f)

        drawArc(
            color = Surface,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        if (hasGoal) {
            drawArc(
                color = GoalReachedColor.copy(alpha = 0.25f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )
        }

        if (isGoalReached) {
            drawArc(
                color = ringColor.copy(alpha = 0.15f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth + 4.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = ringColor.copy(alpha = 0.08f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth + 8.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        } else if (hasGoal) {
            val sweepBrush = if (progress < 0.75f) defaultBrush else warningBrush
            drawArc(
                brush = sweepBrush,
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            val markAngle = -90f + 360f
            val markRad = Math.toRadians(markAngle.toDouble())
            val radius = arcSize.width / 2f
            val outerX = center.x + (radius + strokeWidth / 2f + 2.dp.toPx()) * cos(markRad).toFloat()
            val outerY = center.y + (radius + strokeWidth / 2f + 2.dp.toPx()) * sin(markRad).toFloat()
            val innerX = center.x + (radius - strokeWidth / 2f - 2.dp.toPx()) * cos(markRad).toFloat()
            val innerY = center.y + (radius - strokeWidth / 2f - 2.dp.toPx()) * sin(markRad).toFloat()
            drawLine(
                color = GoalReachedColor,
                start = Offset(outerX, outerY),
                end = Offset(innerX, innerY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        } else {
            drawArc(
                brush = defaultBrush,
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        if (hasGoal) {
            val text = "${(progress * 100).toInt()}%"
            val textStyle = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = ringColor
            )
            val textLayoutResult = textMeasurer.measure(text, textStyle)
            val textOffset = Offset(
                x = (size.width - textLayoutResult.size.width) / 2f,
                y = (size.height - textLayoutResult.size.height) / 2f
            )
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = textOffset
            )
        }
    }
}
