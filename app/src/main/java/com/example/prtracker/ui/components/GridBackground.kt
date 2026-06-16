package com.example.prtracker.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.example.prtracker.ui.theme.PrimaryAccent
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun GridBackground(modifier: Modifier = Modifier) {
    var gridOffset by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        var lastFrameTime = 0L
        while (isActive) {
            withFrameMillis { frameTimeMillis ->
                if (lastFrameTime != 0L) {
                    val delta = frameTimeMillis - lastFrameTime
                    gridOffset = (gridOffset + delta * 0.01f) % 100f
                }
                lastFrameTime = frameTimeMillis
            }
            delay(32)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                val gridSize = 80.dp.toPx()
                val offsetPx = gridOffset / 100f * gridSize
                val lineColor = PrimaryAccent.copy(alpha = 0.03f)

                var x = -gridSize + offsetPx
                while (x < size.width + gridSize) {
                    drawLine(
                        color = lineColor,
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 1f
                    )
                    x += gridSize
                }

                var y = -gridSize + offsetPx
                while (y < size.height + gridSize) {
                    drawLine(
                        color = lineColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f
                    )
                    y += gridSize
                }
            }
    )
}
