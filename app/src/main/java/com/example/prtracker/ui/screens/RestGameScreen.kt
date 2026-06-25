package com.example.prtracker.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.prtracker.data.PotionType
import com.example.prtracker.ui.components.GridBackground
import com.example.prtracker.ui.theme.Background
import com.example.prtracker.ui.theme.GoalComplete
import com.example.prtracker.ui.theme.GoalReachedColor
import com.example.prtracker.ui.theme.LocalAppearance
import com.example.prtracker.ui.theme.PrimaryAccent
import com.example.prtracker.ui.theme.SecondaryAccent
import com.example.prtracker.ui.theme.TextPrimary
import com.example.prtracker.ui.theme.TextSecondary
import com.example.prtracker.ui.theme.systemAccentColor
import com.example.prtracker.viewmodel.PRViewModel
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

private enum class ObjectType {
    REGULAR, MEGA, BOMB, SPILL
}

private data class FallingObject(
    val id: Long,
    val x: Float,
    var y: Float,
    val type: ObjectType,
    val speed: Float
)

private const val TARGET_GRAMS = 100
private const val CUP_WIDTH_RATIO = 0.18f
private const val CUP_HEIGHT_RATIO = 0.06f
private const val CUP_Y_RATIO = 0.88f
private const val OBJECT_SIZE_RATIO = 0.04f

@Composable
fun RestGameScreen(
    navController: NavHostController,
    viewModel: PRViewModel
) {
    val appearance = LocalAppearance.current

    var servings by remember { mutableIntStateOf(0) }
    var bestServings by remember { mutableIntStateOf(0) }
    var currentGrams by remember { mutableIntStateOf(0) }
    var cupCenterX by remember { mutableFloatStateOf(0.5f) }
    var isPaused by remember { mutableStateOf(false) }
    var showBestFlash by remember { mutableStateOf(false) }
    var showPotionEarned by remember { mutableStateOf(false) }
    var potionAwardedThisSession by remember { mutableStateOf(false) }
    var chaosBurstActive by remember { mutableStateOf(false) }
    var chaosBurstEnd by remember { mutableLongStateOf(0L) }

    val objects = remember { mutableStateListOf<FallingObject>() }
    var nextObjectId by remember { mutableLongStateOf(0L) }
    var lastSpawnTime by remember { mutableLongStateOf(0L) }

    val canPlayMiniGame by viewModel.canPlayMiniGame.collectAsState()

    val cupFillFraction = (currentGrams.toFloat() / TARGET_GRAMS).coerceIn(0f, 1f)
    val fillLerped by animateFloatAsState(
        targetValue = cupFillFraction,
        animationSpec = tween(200, easing = LinearEasing),
        label = "fill"
    )

    // Load saved best score on entry
    LaunchedEffect(Unit) {
        bestServings = viewModel.miniGameHighScore.value
    }

    // Game loop — handles spawning, collisions
    LaunchedEffect(Unit) {
        while (true) {
            delay(16L)
            if (isPaused) continue

            val now = System.currentTimeMillis()

            // Chaos burst logic
            if (chaosBurstActive && now > chaosBurstEnd) {
                chaosBurstActive = false
            }

            // Spawn logic
            val baseInterval = when {
                servings < 3 -> 900L
                servings < 7 -> 650L
                else -> 400L
            }
            val spawnInterval = if (chaosBurstActive) (baseInterval / 3).coerceAtLeast(120L) else baseInterval

            if (now - lastSpawnTime > spawnInterval) {
                val r = Random.nextFloat()
                val spillChance = when {
                    servings < 3 -> 0.03f
                    servings < 7 -> 0.10f
                    else -> 0.18f
                }
                val type = when {
                    r < 0.02f -> ObjectType.BOMB
                    r < 0.05f -> ObjectType.MEGA
                    r < 0.05f + spillChance -> ObjectType.SPILL
                    else -> ObjectType.REGULAR
                }

                val baseSpeed = when {
                    servings < 3 -> 0.0018f
                    servings < 7 -> 0.0028f
                    else -> 0.0040f
                }
                val speed = baseSpeed * when (type) {
                    ObjectType.MEGA -> 0.65f
                    ObjectType.BOMB -> 1.5f
                    ObjectType.SPILL -> 1.1f
                    ObjectType.REGULAR -> 1.0f
                }

                objects.add(
                    FallingObject(
                        id = nextObjectId++,
                        x = Random.nextFloat() * 0.85f + 0.075f,
                        y = -0.06f,
                        type = type,
                        speed = speed
                    )
                )
                lastSpawnTime = now

                // In chaos burst, spawn extras
                if (chaosBurstActive && Random.nextFloat() < 0.5f) {
                    val extraType = when {
                        Random.nextFloat() < 0.15f -> ObjectType.SPILL
                        Random.nextFloat() < 0.05f -> ObjectType.BOMB
                        Random.nextFloat() < 0.1f -> ObjectType.MEGA
                        else -> ObjectType.REGULAR
                    }
                    objects.add(
                        FallingObject(
                            id = nextObjectId++,
                            x = Random.nextFloat() * 0.85f + 0.075f,
                            y = -0.06f,
                            type = extraType,
                            speed = baseSpeed * 1.2f
                        )
                    )
                }
            }

            // Update positions and check collisions
            val toRemove = mutableListOf<Long>()
            var gramsDelta = 0

            for (obj in objects) {
                obj.y += obj.speed

                // Collision check: object overlaps cup vertically and is within cup horizontally
                val cupTop = CUP_Y_RATIO - CUP_HEIGHT_RATIO / 2f
                val cupBottom = CUP_Y_RATIO + CUP_HEIGHT_RATIO / 2f
                if (obj.y in (cupTop - 0.01f)..(cupBottom + 0.01f)) {
                    val halfCup = CUP_WIDTH_RATIO * 0.48f
                    if (abs(obj.x - cupCenterX) < halfCup) {
                        when (obj.type) {
                            ObjectType.REGULAR -> gramsDelta += 5
                            ObjectType.MEGA -> gramsDelta += 15
                            ObjectType.BOMB -> gramsDelta += 25
                            ObjectType.SPILL -> gramsDelta -= 10
                        }
                        toRemove.add(obj.id)
                    }
                }

                // Remove if off screen
                if (obj.y > 1.15f) {
                    toRemove.add(obj.id)
                }
            }

            objects.removeAll { it.id in toRemove }

            if (gramsDelta != 0) {
                currentGrams = (currentGrams + gramsDelta).coerceIn(0, TARGET_GRAMS)
            }

            // Serving complete check
            if (currentGrams >= TARGET_GRAMS) {
                servings++
                if (servings > bestServings) {
                    bestServings = servings
                    showBestFlash = true
                    viewModel.updateBestRestGameServings(servings)
                }
                currentGrams = 0
                objects.clear()

                // Award potion at 5 servings if cooldown is done and not already awarded this session
                if (servings >= 5 && canPlayMiniGame && !potionAwardedThisSession) {
                    viewModel.awardPotion(PotionType.XP_DOUBLE)
                    potionAwardedThisSession = true
                    showPotionEarned = true
                }

                // Trigger chaos burst every 5 servings
                if (servings % 5 == 0) {
                    chaosBurstActive = true
                    chaosBurstEnd = System.currentTimeMillis() + 3000L
                }
            }
        }
    }

    // Reset best flash
    LaunchedEffect(showBestFlash) {
        if (showBestFlash) {
            delay(1200L)
            showBestFlash = false
        }
    }

    // Reset potion earned overlay
    LaunchedEffect(showPotionEarned) {
        if (showPotionEarned) {
            delay(2000L)
            showPotionEarned = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GridBackground()

        // Game canvas (drawn first, below all UI)
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            down.consume()
                            cupCenterX = (down.position.x / size.width).coerceIn(
                                CUP_WIDTH_RATIO / 2f,
                                1f - CUP_WIDTH_RATIO / 2f
                            )

                            do {
                                val event = awaitPointerEvent()
                                for (change in event.changes) {
                                    if (change.pressed) {
                                        cupCenterX = (change.position.x / size.width).coerceIn(
                                            CUP_WIDTH_RATIO / 2f,
                                            1f - CUP_WIDTH_RATIO / 2f
                                        )
                                    }
                                }
                            } while (event.changes.any { it.pressed })
                        }
                    }
            ) {
            val w = size.width
            val h = size.height

            // Draw falling objects
            for (obj in objects) {
                val cx = obj.x * w
                val cy = obj.y * h
                val radius = OBJECT_SIZE_RATIO * w

                when (obj.type) {
                    ObjectType.REGULAR -> {
                        // White/beige scoop
                        drawCircle(
                            color = Color(0xFFF5F0E8),
                            radius = radius,
                            center = Offset(cx, cy)
                        )
                        drawCircle(
                            color = Color(0xFFD4C9B8),
                            radius = radius * 0.6f,
                            center = Offset(cx, cy - radius * 0.2f)
                        )
                    }
                    ObjectType.MEGA -> {
                        // Glowing cyan mega scoop
                        drawCircle(
                            color = PrimaryAccent.copy(alpha = 0.3f),
                            radius = radius * 1.5f,
                            center = Offset(cx, cy)
                        )
                        drawCircle(
                            color = PrimaryAccent,
                            radius = radius,
                            center = Offset(cx, cy)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = radius * 0.4f,
                            center = Offset(cx - radius * 0.2f, cy - radius * 0.2f)
                        )
                    }
                    ObjectType.BOMB -> {
                        // Purple capsule
                        drawCircle(
                            color = SecondaryAccent.copy(alpha = 0.4f),
                            radius = radius * 1.3f,
                            center = Offset(cx, cy)
                        )
                        drawCircle(
                            color = SecondaryAccent,
                            radius = radius,
                            center = Offset(cx, cy)
                        )
                        // Lightning bolt mark
                        val path = Path().apply {
                            moveTo(cx - radius * 0.2f, cy - radius * 0.5f)
                            lineTo(cx + radius * 0.1f, cy - radius * 0.1f)
                            lineTo(cx - radius * 0.1f, cy - radius * 0.1f)
                            lineTo(cx + radius * 0.2f, cy + radius * 0.5f)
                            lineTo(cx - radius * 0.1f, cy + radius * 0.1f)
                            lineTo(cx + radius * 0.1f, cy + radius * 0.1f)
                            close()
                        }
                        drawPath(path, color = Color.White)
                    }
                    ObjectType.SPILL -> {
                        // Red/orange blob - distinct and warning-like
                        drawCircle(
                            color = Color(0xFFFF3B30).copy(alpha = 0.3f),
                            radius = radius * 1.4f,
                            center = Offset(cx, cy)
                        )
                        drawCircle(
                            color = Color(0xFFFF6B00),
                            radius = radius,
                            center = Offset(cx, cy)
                        )
                        drawCircle(
                            color = Color(0xFFFF3B30),
                            radius = radius * 0.6f,
                            center = Offset(cx, cy + radius * 0.15f)
                        )
                        // X mark
                        drawLine(
                            color = Color.White,
                            start = Offset(cx - radius * 0.3f, cy - radius * 0.3f),
                            end = Offset(cx + radius * 0.3f, cy + radius * 0.3f),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = Color.White,
                            start = Offset(cx + radius * 0.3f, cy - radius * 0.3f),
                            end = Offset(cx - radius * 0.3f, cy + radius * 0.3f),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            // Draw cup
            drawCup(
                centerX = cupCenterX * w,
                y = CUP_Y_RATIO * h,
                cupWidth = CUP_WIDTH_RATIO * w,
                cupHeight = CUP_HEIGHT_RATIO * h,
                fillFraction = fillLerped
            )
        }

        // Grams display — positioned just above the cup
        val cupTopPadding = maxHeight * CUP_Y_RATIO - 48.dp
        Text(
            text = "$currentGrams / ${TARGET_GRAMS}g",
            style = MaterialTheme.typography.titleMedium,
            color = if (currentGrams > 0) appearance.systemAccentColor else TextSecondary,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = cupTopPadding)
        )
        }

        // Back button (drawn on top of canvas, receives touches)
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 40.dp, start = 8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = appearance.systemAccentColor
            )
        }

        // Title
        Text(
            text = "PROTEIN CATCH",
            style = MaterialTheme.typography.displayLarge,
            color = appearance.systemAccentColor,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp)
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

        // Score display
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 100.dp, end = 16.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "SERVINGS: $servings",
                style = MaterialTheme.typography.titleLarge,
                color = if (showBestFlash) GoalComplete else appearance.systemAccentColor,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "BEST: $bestServings",
                style = MaterialTheme.typography.bodyMedium,
                color = if (showBestFlash) GoalComplete else TextSecondary,
                fontFamily = FontFamily.Monospace
            )
        }

        // Chaos burst indicator
        if (chaosBurstActive) {
            Text(
                text = "// CHAOS BURST //",
                style = MaterialTheme.typography.titleMedium,
                color = SecondaryAccent,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 100.dp)
            )
        }

        // Potion progress dots (only when cooldown done and no potion earned this session)
        if (canPlayMiniGame && !potionAwardedThisSession) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 140.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(5) { index ->
                    val filled = servings > index
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (filled) GoalComplete
                                else TextSecondary.copy(alpha = 0.3f)
                            )
                    )
                }
            }
            Text(
                text = "5 SERVINGS = POTION",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 156.dp)
            )
        }

        // Pause button
        IconButton(
            onClick = { isPaused = !isPaused },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 8.dp)
        ) {
            Text(
                text = if (isPaused) "▶" else "⏸",
                fontSize = 20.sp,
                color = appearance.systemAccentColor
            )
        }

        // Pause overlay
        if (isPaused) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Background.copy(alpha = 0.7f))
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            isPaused = false
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PAUSED",
                        style = MaterialTheme.typography.displayLarge,
                        color = appearance.systemAccentColor,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "TAP TO RESUME",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
                }
            }
        }

        // Potion earned overlay
        if (showPotionEarned) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Background.copy(alpha = 0.9f))
                        .padding(32.dp)
                ) {
                    Text(
                        text = "YOU RECEIVED A 2X POTION",
                        style = MaterialTheme.typography.headlineMedium,
                        color = GoalComplete,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "2x XP on your next entry",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }

private fun DrawScope.drawCup(
    centerX: Float,
    y: Float,
    cupWidth: Float,
    cupHeight: Float,
    fillFraction: Float
) {
    val left = centerX - cupWidth / 2f
    val top = y - cupHeight / 2f
    val cornerRadius = cupWidth * 0.12f

    // Cup body (dark)
    drawRoundRect(
        color = Color(0xFF0D1526),
        topLeft = Offset(left, top),
        size = Size(cupWidth, cupHeight),
        cornerRadius = CornerRadius(cornerRadius)
    )

    // Fill liquid
    if (fillFraction > 0f) {
        val liquidHeight = cupHeight * fillFraction * 0.85f
        val liquidTop = top + cupHeight - liquidHeight - cupHeight * 0.05f

        // Color transitions: white -> milky cyan -> bright cyan at 100%
        val liquidColor = when {
            fillFraction < 0.3f -> Color(0xFFF5F0E8).copy(alpha = 0.7f)
            fillFraction < 0.7f -> {
                val t = (fillFraction - 0.3f) / 0.4f
                lerpColor(Color(0xFFF5F0E8), PrimaryAccent.copy(alpha = 0.8f), t)
            }
            else -> {
                val t = (fillFraction - 0.7f) / 0.3f
                lerpColor(PrimaryAccent.copy(alpha = 0.8f), PrimaryAccent, t)
            }
        }

        drawRoundRect(
            color = liquidColor,
            topLeft = Offset(left + cupWidth * 0.05f, liquidTop),
            size = Size(cupWidth * 0.9f, liquidHeight),
            cornerRadius = CornerRadius(cornerRadius * 0.5f)
        )

        // Glow when near full
        if (fillFraction > 0.85f) {
            drawRoundRect(
                color = PrimaryAccent.copy(alpha = 0.2f * (fillFraction - 0.85f) / 0.15f),
                topLeft = Offset(left - 4.dp.toPx(), top - 4.dp.toPx()),
                size = Size(cupWidth + 8.dp.toPx(), cupHeight + 8.dp.toPx()),
                cornerRadius = CornerRadius(cornerRadius + 2.dp.toPx())
            )
        }
    }

    // Cup rim (glowing cyan)
    drawRoundRect(
        color = PrimaryAccent,
        topLeft = Offset(left, top),
        size = Size(cupWidth, cupHeight * 0.12f),
        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
    )

    // Cup outline
    drawRoundRect(
        color = PrimaryAccent.copy(alpha = 0.6f),
        topLeft = Offset(left, top),
        size = Size(cupWidth, cupHeight),
        cornerRadius = CornerRadius(cornerRadius),
        style = Stroke(width = 2.dp.toPx())
    )
}

private fun lerpColor(a: Color, b: Color, t: Float): Color {
    val tCoerced = t.coerceIn(0f, 1f)
    return Color(
        red = a.red + (b.red - a.red) * tCoerced,
        green = a.green + (b.green - a.green) * tCoerced,
        blue = a.blue + (b.blue - a.blue) * tCoerced,
        alpha = a.alpha + (b.alpha - a.alpha) * tCoerced
    )
}
