package com.example.prtracker.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Paint
import android.graphics.Typeface
import android.media.MediaPlayer
import android.media.audiofx.LoudnessEnhancer
import kotlinx.coroutines.delay
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import com.example.prtracker.R
import com.example.prtracker.navigation.Routes
import com.example.prtracker.ui.components.GlowingCard
import com.example.prtracker.ui.components.GridBackground
import com.example.prtracker.ui.theme.Background
import com.example.prtracker.ui.theme.GoalComplete
import com.example.prtracker.ui.theme.LocalAppearance
import com.example.prtracker.ui.theme.systemAccentColor
import com.example.prtracker.ui.theme.systemSecondaryColor
import com.example.prtracker.ui.theme.Surface
import com.example.prtracker.ui.theme.TextPrimary
import com.example.prtracker.ui.theme.TextSecondary
import com.example.prtracker.data.PetCatalog
import com.example.prtracker.data.XpEngine
import com.example.prtracker.viewmodel.PRViewModel
import kotlin.math.min

private val tabSwitchSpeeches = listOf(
    R.raw.speech_1 to "Systems fully operational. Your iron willpower is the fuel. Time to work!",
    R.raw.speech_2 to "The weights don't stand a chance today. Load the bar, and let's conquer it!",
    R.raw.speech_3 to "Up and at 'em, Champion! Your future self is waiting. Let's make it count!",
    R.raw.speech_4 to "Target locked: New Personal Records. Initiate maximum effort protocol. Let's go!",
    R.raw.speech_5 to "No hesitation. No retreat! Push your limits today, and dominate the iron!"
)

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: PRViewModel
) {
    val currentStreak by viewModel.currentStreak.collectAsState()
    val tierResult by viewModel.tierResult.collectAsState()
    val exercises by viewModel.exercises.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()
    val totalXp by viewModel.totalXp.collectAsState()
    val currentLevel by viewModel.currentLevel.collectAsState()
    val xpInLevel by viewModel.xpInCurrentLevel.collectAsState()
    val xpToNext by viewModel.xpNeededForLevelUp.collectAsState()
    val equippedPetIds by viewModel.equippedPetIds.collectAsState()
    val petInventory by viewModel.petInventory.collectAsState()
    val petMult = viewModel.petXpMultiplier()

    val densityObj = LocalDensity.current
    val text9spPx = with(densityObj) { 9.sp.toPx() }
    val text7spPx = with(densityObj) { 7.sp.toPx() }
    val text8spPx = with(densityObj) { 8.sp.toPx() }

    val appearance = LocalAppearance.current

    val topPRValue = remember(exercises) {
        exercises.maxOfOrNull { ex -> ex.entries.maxOfOrNull { it.value } ?: 0 } ?: 0
    }
    val topPRType = remember(exercises, topPRValue) {
        exercises
            .filter { ex -> ex.entries.maxOfOrNull { it.value } ?: 0 == topPRValue }
            .firstOrNull()?.type
    }
    val topPRString = remember(topPRValue, topPRType) {
        if (topPRValue > 0) {
            if (topPRType == "hold") "${topPRValue}s" else "$topPRValue reps"
        } else "0"
    }

    val cr47Paint = remember(text9spPx) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.MONOSPACE
            color = android.graphics.Color.argb(180, 0, 245, 255)
            textSize = text9spPx
        }
    }
    val prLabelPaint = remember(text7spPx) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.MONOSPACE
            color = android.graphics.Color.argb(200, 123, 47, 255)
            textSize = text7spPx
        }
    }
    val prValuePaint = remember(text8spPx) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.MONOSPACE
            color = android.graphics.Color.argb(220, 0, 245, 255)
            textSize = text8spPx
        }
    }

    val navBrush = remember(appearance) {
        Brush.horizontalGradient(listOf(appearance.systemAccentColor, appearance.systemSecondaryColor))
    }

    val context = LocalContext.current

    var selectedSpeechText by remember { mutableStateOf("") }
    var isSpeaking by remember { mutableStateOf(false) }
    var showBubble by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var currentEnhancer by remember { mutableStateOf<LoudnessEnhancer?>(null) }
    var speechId by remember { mutableStateOf(0) }
    var isFirstComposition by rememberSaveable { mutableStateOf(true) }
    var appWasInBackground by remember { mutableStateOf(false) }

    fun playSpeech(audioRes: Int, text: String) {
        try {
            currentEnhancer?.release()
            currentEnhancer = null
        } catch (_: Exception) {}
        try { mediaPlayer?.stop() } catch (_: Exception) {}
        try { mediaPlayer?.release() } catch (_: Exception) {}
        mediaPlayer = null

        selectedSpeechText = text
        showBubble = true
        speechId++

        try {
            val player = MediaPlayer.create(context, audioRes)
            if (player == null) {
                isSpeaking = false
                return
            }
            mediaPlayer = player

            if (appSettings.speechesEnabled) {
                val gain = (appSettings.soundVolume * 2350).toInt()
                val enhancer = LoudnessEnhancer(player.audioSessionId)
                enhancer.enabled = true
                enhancer.setTargetGain(gain)
                currentEnhancer = enhancer
                player.setOnCompletionListener {
                    isSpeaking = false
                    showBubble = false
                }
                isSpeaking = true
                player.start()
            } else {
                player.setVolume(0f, 0f)
                player.setOnCompletionListener {
                    showBubble = false
                }
                player.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            isSpeaking = false
            showBubble = false
        }
    }

    fun stopSpeech() {
        try { currentEnhancer?.release() } catch (_: Exception) {}
        currentEnhancer = null
        try { mediaPlayer?.stop() } catch (_: Exception) {}
        try { mediaPlayer?.release() } catch (_: Exception) {}
        mediaPlayer = null
        isSpeaking = false
        showBubble = false
    }

    fun playWelcome() {
        playSpeech(R.raw.mascot_welcome, "Welcome back, Champion! Ready to train?")
    }

    // Cleanup when leaving HomeScreen (tab switch away)
    DisposableEffect(Unit) {
        onDispose { stopSpeech() }
    }

    // Activity lifecycle: detect app background/foreground
    val activity = context.findActivity()
    val lifecycleOwner = activity as? LifecycleOwner
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    appWasInBackground = true
                    stopSpeech()
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (appWasInBackground) {
                        appWasInBackground = false
                        playWelcome()
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner?.lifecycle?.addObserver(observer)
        onDispose {
            lifecycleOwner?.lifecycle?.removeObserver(observer)
        }
    }

    // Cold start → welcome; tab switch → random
    LaunchedEffect(Unit) {
        if (isFirstComposition) {
            isFirstComposition = false
            playWelcome()
        } else {
            val roll = tabSwitchSpeeches.random()
            playSpeech(roll.first, roll.second)
        }
    }

    // --- ANIMATION STATES ---
    var animateTrigger by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animateTrigger = true }

    val hudAlpha by animateFloatAsState(
        targetValue = if (animateTrigger) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing),
        label = "hud_alpha"
    )

    val bubbleScale by animateFloatAsState(
        targetValue = if (showBubble) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "bubble_scale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "mascot_engine")

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.06f,
        targetValue = 0.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val mouthScale = remember { Animatable(1f) }

    // Lip-sync mouth animation driven by isSpeaking
    LaunchedEffect(isSpeaking) {
        if (isSpeaking) {
            val totalMs = (mediaPlayer?.duration ?: 3000).toInt()
            val cycleMs = 180
            val numCycles = (totalMs / cycleMs).coerceAtLeast(1)
            repeat(numCycles) {
                mouthScale.animateTo(0.4f, tween(80, easing = LinearEasing))
                mouthScale.animateTo(1.3f, tween(80, easing = LinearEasing))
                mouthScale.animateTo(1f, tween(20, easing = LinearEasing))
            }
            mouthScale.snapTo(1f)
        } else {
            mouthScale.snapTo(1f)
        }
    }

    // Smooth fade-out for every welcome speech
    LaunchedEffect(speechId) {
        if (isSpeaking) {
            val player = mediaPlayer ?: return@LaunchedEffect
            val totalMs = try { player.duration.toLong() } catch (_: Exception) { return@LaunchedEffect }
            val fadeOutMs = 500L
            delay((totalMs - fadeOutMs).coerceAtLeast(0))
            val steps = 10
            for (i in 1..steps) {
                if (mediaPlayer == null || !isSpeaking) return@LaunchedEffect
                val volume = 1f - (i.toFloat() / steps)
                try { player.setVolume(volume, volume) } catch (_: Exception) {}
                delay(fadeOutMs / steps)
            }
        }
    }

    val eyeYScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4000
                1f at 0
                1f at 3800
                0.0f at 3900
                1f at 4000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "eye_blink"
    )

    val scannerProgress by infiniteTransition.animateFloat(
        initialValue = -0.25f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanner_line"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        GridBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth()
            ) {
                // --- ANIMATED SPEECH BUBBLE ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp)
                        .offset(y = 8.dp)
                        .scale(bubbleScale)
                        .alpha(bubbleScale)
                        .zIndex(10f),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFF0A0F1D).copy(alpha = 0.92f),
                                shape = RoundedCornerShape(14.dp, 14.dp, 14.dp, 2.dp)
                            )
                            .drawBehind {
                                drawRoundRect(
                                    color = Color(0xFF5CE1E6),
                                    topLeft = Offset.Zero,
                                    size = size,
                                    cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
                                    style = Stroke(width = 1.5f.dp.toPx())
                                )
                            }
                    ) {
                        Text(
                            text = selectedSpeechText,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(460.dp)
                ) {
                    val cw = size.width
                    val ch = size.height

                    val t18 = 18.dp.toPx()
                    val t30 = 30.dp.toPx()
                    val t10 = 10.dp.toPx()
                    val t4 = 4.dp.toPx()
                    val t20 = 20.dp.toPx()
                    val t1_5 = 1.5f.dp.toPx()

                    val hudW = 110.dp.toPx()
                    val hudH = 70.dp.toPx()
                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx()), 0f)

                    val cx = cw / 2f
                    val cy = ch * 0.52f // Centered composition alignment
                    val r = min(cw, ch) * 0.40f // Proportional scale matching "Zenith logo.png"

                    val hudPad = t20
                    val hudY = ch * 0.12f + 66.dp.toPx() * 0.2f

                    // --- ACCURATE BRAND PALETTE ---
                    val zenithNavy = Color(0xFF0A0F1D)       // Visor screen core
                    val zenithDarkBlue = Color(0xFF12223A)   // Face shadows / respirator base
                    val zenithBlue = Color(0xFF1E3A60)       // Main outer plates
                    val zenithLightBlue = Color(0xFF43B6E6)  // Forehead dome
                    val zenithCyan = Color(0xFF5CE1E6)       // Eyes, cyber brows, trim glow
                    val zenithPurple = Color(0xFF8832D3)     // Inner ears, screen border trim
                    val zenithWhiteSilver = Color(0xFFF2F7FF)// Outer ear frame rims
                    val zenithBlueFlower = Color(0xFF3299D3) // Outer crown flowers
                    val zenithGold = Color(0xFFF1C40F)       // Pistils / flower centers
                    val zenithLeaf = Color(0xFF228B22)       // Crown foliage
                    val zenithOutline = Color(0xFF060911)    // Sticker vector outlines

                    // --- AMBIENT GLOW SYSTEM (BREATHING) ---
                    drawCircle(
                        color = zenithLightBlue.copy(alpha = glowAlpha),
                        radius = r * 1.4f,
                        center = Offset(cx, cy)
                    )

                    // --- LEFT HUD PANEL (BOOT-UP ALPHA) ---
                    val leftHudX = hudPad
                    drawRoundRect(
                        color = Background.copy(alpha = 0.9f * hudAlpha),
                        topLeft = Offset(leftHudX, hudY),
                        size = Size(hudW, hudH),
                        cornerRadius = CornerRadius(t4)
                    )
                    drawRoundRect(
                        color = appearance.systemAccentColor.copy(alpha = 0.7f * hudAlpha),
                        topLeft = Offset(leftHudX, hudY),
                        size = Size(hudW, hudH),
                        cornerRadius = CornerRadius(t4),
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawLine(
                        color = appearance.systemAccentColor.copy(alpha = 0.4f * hudAlpha),
                        start = Offset(leftHudX + t10, hudY + 23.dp.toPx()),
                        end = Offset(leftHudX + 100.dp.toPx(), hudY + 23.dp.toPx()),
                        strokeWidth = 1.dp.toPx()
                    )
                    val tickYs = floatArrayOf(hudY + t18, hudY + t30, hudY + 42.dp.toPx(), hudY + 54.dp.toPx())
                    val tickLengths = floatArrayOf(60.dp.toPx(), 50.dp.toPx(), 60.dp.toPx(), 50.dp.toPx())
                    for (i in tickYs.indices) {
                        drawLine(
                            color = appearance.systemAccentColor.copy(alpha = 0.3f * hudAlpha),
                            start = Offset(leftHudX + t10, tickYs[i]),
                            end = Offset(leftHudX + t10 + tickLengths[i], tickYs[i]),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // --- RIGHT HUD PANEL (BOOT-UP ALPHA) ---
                    val rightHudX = cw - hudPad - hudW
                    drawRoundRect(
                        color = Background.copy(alpha = 0.9f * hudAlpha),
                        topLeft = Offset(rightHudX, hudY),
                        size = Size(hudW, hudH),
                        cornerRadius = CornerRadius(t4)
                    )
                    drawRoundRect(
                        color = appearance.systemSecondaryColor.copy(alpha = 0.7f * hudAlpha),
                        topLeft = Offset(rightHudX, hudY),
                        size = Size(hudW, hudH),
                        cornerRadius = CornerRadius(t4),
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawLine(
                        color = appearance.systemSecondaryColor.copy(alpha = 0.4f * hudAlpha),
                        start = Offset(rightHudX + t10, hudY + 23.dp.toPx()),
                        end = Offset(rightHudX + 100.dp.toPx(), hudY + 23.dp.toPx()),
                        strokeWidth = 1.dp.toPx()
                    )
                    for (i in tickYs.indices) {
                        drawLine(
                            color = appearance.systemSecondaryColor.copy(alpha = 0.3f * hudAlpha),
                            start = Offset(rightHudX + t10, tickYs[i]),
                            end = Offset(rightHudX + t10 + tickLengths[i], tickYs[i]),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // --- CONNECTOR DASHED LINES (BOOT-UP ALPHA) ---
                    drawLine(
                        color = appearance.systemAccentColor.copy(alpha = 0.35f * hudAlpha),
                        start = Offset(leftHudX + hudW, hudY + hudH * 0.5f),
                        end = Offset(cx - r * 0.80f, cy),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = dashEffect
                    )
                    drawLine(
                        color = appearance.systemAccentColor.copy(alpha = 0.35f * hudAlpha),
                        start = Offset(rightHudX, hudY + hudH * 0.5f),
                        end = Offset(cx + r * 0.80f, cy),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = dashEffect
                    )

                    // ==========================================
                    // --- REDESIGNED MASCOT ("Zenith logo.png") ---
                    // ==========================================

                    // 1. EAR BACKINGS & RIMS
                    val leftEarOuter = Path().apply {
                        moveTo(cx - r * 0.52f, cy - r * 0.22f)
                        lineTo(cx - r * 0.46f, cy - r * 1.18f)
                        lineTo(cx - r * 0.14f, cy - r * 0.46f)
                        close()
                    }
                    val rightEarOuter = Path().apply {
                        moveTo(cx + r * 0.52f, cy - r * 0.22f)
                        lineTo(cx + r * 0.46f, cy - r * 1.18f)
                        lineTo(cx + r * 0.14f, cy - r * 0.46f)
                        close()
                    }
                    drawPath(path = leftEarOuter, color = zenithWhiteSilver)
                    drawPath(path = leftEarOuter, color = zenithOutline, style = Stroke(width = 3.5f.dp.toPx()))
                    drawPath(path = rightEarOuter, color = zenithWhiteSilver)
                    drawPath(path = rightEarOuter, color = zenithOutline, style = Stroke(width = 3.5f.dp.toPx()))

                    // 2. PURPLE INNER EARS
                    val leftEarInner = Path().apply {
                        moveTo(cx - r * 0.46f, cy - r * 0.26f)
                        lineTo(cx - r * 0.42f, cy - r * 1.06f)
                        lineTo(cx - r * 0.18f, cy - r * 0.46f)
                        close()
                    }
                    val rightEarInner = Path().apply {
                        moveTo(cx + r * 0.46f, cy - r * 0.26f)
                        lineTo(cx + r * 0.42f, cy - r * 1.06f)
                        lineTo(cx + r * 0.18f, cy - r * 0.46f)
                        close()
                    }
                    drawPath(path = leftEarInner, color = zenithPurple)
                    drawPath(path = leftEarInner, color = zenithOutline, style = Stroke(width = 2.dp.toPx()))
                    drawPath(path = rightEarInner, color = zenithPurple)
                    drawPath(path = rightEarInner, color = zenithOutline, style = Stroke(width = 2.dp.toPx()))

                    // 3. MAIN FACETED FACE SHIELD BASE
                    val faceBaseSilhouette = Path().apply {
                        moveTo(cx, cy + r * 0.72f) // Chin point
                        lineTo(cx - r * 0.42f, cy + r * 0.46f)
                        lineTo(cx - r * 0.98f, cy + r * 0.08f) // Left cheek flare
                        lineTo(cx - r * 0.52f, cy - r * 0.38f) // Upper left forehead
                        quadraticBezierTo(cx, cy - r * 0.48f, cx + r * 0.52f, cy - r * 0.38f)
                        lineTo(cx + r * 0.98f, cy + r * 0.08f) // Right cheek flare
                        lineTo(cx + r * 0.42f, cy + r * 0.46f)
                        close()
                    }
                    drawPath(path = faceBaseSilhouette, color = zenithBlue)

                    // 4. LOWER FACE SHADOW PLATES
                    val lowerShadowPlates = Path().apply {
                        moveTo(cx, cy + r * 0.72f)
                        lineTo(cx - r * 0.42f, cy + r * 0.46f)
                        lineTo(cx - r * 0.98f, cy + r * 0.08f)
                        lineTo(cx - r * 0.46f, cy + r * 0.16f)
                        lineTo(cx, cy + r * 0.34f)
                        lineTo(cx + r * 0.46f, cy + r * 0.16f)
                        lineTo(cx + r * 0.98f, cy + r * 0.08f)
                        lineTo(cx + r * 0.42f, cy + r * 0.46f)
                        close()
                    }
                    drawPath(path = lowerShadowPlates, color = zenithDarkBlue)

                    // 5. HELMET TOP BRIGHT DOME
                    val domeHelmet = Path().apply {
                        moveTo(cx - r * 0.34f, cy - r * 0.36f)
                        lineTo(cx - r * 0.24f, cy - r * 0.68f)
                        quadraticBezierTo(cx, cy - r * 0.78f, cx + r * 0.24f, cy - r * 0.68f)
                        lineTo(cx + r * 0.34f, cy - r * 0.36f)
                        close()
                    }
                    drawPath(path = domeHelmet, color = zenithLightBlue)
                    drawPath(path = domeHelmet, color = zenithOutline, style = Stroke(width = 2.5f.dp.toPx()))

                    // Dome highlight arc
                    drawOval(
                        color = Color.White.copy(alpha = 0.25f),
                        topLeft = Offset(cx - r * 0.14f, cy - r * 0.74f),
                        size = Size(r * 0.24f, r * 0.08f)
                    )

                    // 6. VISOR DISPLAY SCREEN CORE
                    val screenVisor = Path().apply {
                        moveTo(cx, cy + r * 0.42f)
                        lineTo(cx - r * 0.46f, cy + r * 0.22f)
                        lineTo(cx - r * 0.52f, cy - r * 0.24f)
                        lineTo(cx + r * 0.52f, cy - r * 0.24f)
                        lineTo(cx + r * 0.46f, cy + r * 0.22f)
                        close()
                    }
                    drawPath(path = screenVisor, color = zenithNavy)

                    // Visor Frame Accents (Purple flank plates)
                    val leftVisorTrim = Path().apply {
                        moveTo(cx - r * 0.52f, cy - r * 0.24f)
                        lineTo(cx - r * 0.46f, cy + r * 0.22f)
                        lineTo(cx - r * 0.50f, cy + r * 0.22f)
                        lineTo(cx - r * 0.55f, cy - r * 0.24f)
                        close()
                    }
                    val rightVisorTrim = Path().apply {
                        moveTo(cx + r * 0.52f, cy - r * 0.24f)
                        lineTo(cx + r * 0.46f, cy + r * 0.22f)
                        lineTo(cx + r * 0.50f, cy + r * 0.22f)
                        lineTo(cx + r * 0.55f, cy - r * 0.24f)
                        close()
                    }
                    drawPath(path = leftVisorTrim, color = zenithPurple)
                    drawPath(path = rightVisorTrim, color = zenithPurple)
                    drawPath(path = screenVisor, color = zenithOutline, style = Stroke(width = 3.dp.toPx()))

                    // --- FACE SHIELD SCANNER LINE ---
                    val laserY = cy + (r * scannerProgress)
                    drawLine(
                        color = zenithCyan.copy(alpha = 0.4f),
                        start = Offset(cx - r * 0.45f, laserY),
                        end = Offset(cx + r * 0.45f, laserY),
                        strokeWidth = 3.dp.toPx()
                    )

                    // 7. GLOWING CYBER CYAN BROWS & EYES
                    // Thick slanted geometric brows
                    val leftBrow = Path().apply {
                        moveTo(cx - r * 0.36f, cy - r * 0.14f)
                        lineTo(cx - r * 0.14f, cy - r * 0.06f)
                        lineTo(cx - r * 0.16f, cy - r * 0.02f)
                        lineTo(cx - r * 0.38f, cy - r * 0.10f)
                        close()
                    }
                    val rightBrow = Path().apply {
                        moveTo(cx + r * 0.36f, cy - r * 0.14f)
                        lineTo(cx + r * 0.14f, cy - r * 0.06f)
                        lineTo(cx + r * 0.16f, cy - r * 0.02f)
                        lineTo(cx + r * 0.38f, cy - r * 0.10f)
                        close()
                    }
                    drawPath(path = leftBrow, color = zenithCyan)
                    drawPath(path = rightBrow, color = zenithCyan)

                    // Left curved happy eye (with blink)
                    val leftEyePath = Path().apply {
                        moveTo(cx - r * 0.38f, cy + r * 0.10f)
                        quadraticBezierTo(cx - r * 0.25f, cy - r * 0.03f, cx - r * 0.12f, cy + r * 0.10f)
                    }
                    val leftPivotX = cx - r * 0.25f
                    val leftPivotY = cy + r * 0.05f
                    drawContext.canvas.save()
                    drawContext.canvas.translate(leftPivotX, leftPivotY)
                    drawContext.canvas.scale(1f, eyeYScale)
                    drawContext.canvas.translate(-leftPivotX, -leftPivotY)
                    drawPath(path = leftEyePath, color = zenithCyan, style = Stroke(width = 6.5f.dp.toPx(), cap = StrokeCap.Round))
                    drawContext.canvas.restore()

                    // Right structural angular wink chevron (with blink)
                    val rightEyePath = Path().apply {
                        moveTo(cx + r * 0.14f, cy + r * 0.04f)
                        lineTo(cx + r * 0.26f, cy + r * 0.11f)
                        lineTo(cx + r * 0.38f, cy + r * 0.03f)
                    }
                    val rightPivotX = cx + r * 0.26f
                    val rightPivotY = cy + r * 0.07f
                    drawContext.canvas.save()
                    drawContext.canvas.translate(rightPivotX, rightPivotY)
                    drawContext.canvas.scale(1f, eyeYScale)
                    drawContext.canvas.translate(-rightPivotX, -rightPivotY)
                    drawPath(path = rightEyePath, color = zenithCyan, style = Stroke(width = 6.5f.dp.toPx(), cap = StrokeCap.Round))
                    drawContext.canvas.restore()

                    // 8. RESPIRATOR MASK & CHIN GRILL
                    val respiratorBase = Path().apply {
                        moveTo(cx, cy + r * 0.70f)
                        lineTo(cx - r * 0.26f, cy + r * 0.42f)
                        lineTo(cx - r * 0.42f, cy + r * 0.24f)
                        lineTo(cx + r * 0.42f, cy + r * 0.24f)
                        lineTo(cx + r * 0.26f, cy + r * 0.42f)
                        close()
                    }
                    drawPath(path = respiratorBase, color = zenithDarkBlue)
                    drawPath(path = respiratorBase, color = zenithOutline, style = Stroke(width = 3.dp.toPx()))

                    // Cyan wing moldings on the side of the respirator
                    val leftRespWing = Path().apply {
                        moveTo(cx - r * 0.40f, cy + r * 0.25f)
                        lineTo(cx - r * 0.24f, cy + r * 0.25f)
                        lineTo(cx - r * 0.14f, cy + r * 0.33f)
                        close()
                    }
                    val rightRespWing = Path().apply {
                        moveTo(cx + r * 0.40f, cy + r * 0.25f)
                        lineTo(cx + r * 0.24f, cy + r * 0.25f)
                        lineTo(cx + r * 0.14f, cy + r * 0.33f)
                        close()
                    }
                    drawPath(path = leftRespWing, color = zenithCyan)
                    drawPath(path = rightRespWing, color = zenithCyan)
                    drawPath(path = leftRespWing, color = zenithOutline, style = Stroke(width = 1.5f.dp.toPx()))
                    drawPath(path = rightRespWing, color = zenithOutline, style = Stroke(width = 1.5f.dp.toPx()))

                    // Five vertical rounded pills forming the grill (TALKING ANIMATION)
                    val grillXOffsets = floatArrayOf(0f, -0.08f, 0.08f, -0.16f, 0.16f)
                    val grillHeights = floatArrayOf(0.24f, 0.20f, 0.20f, 0.14f, 0.14f)
                    val grillYOffsets = floatArrayOf(0.42f, 0.44f, 0.44f, 0.47f, 0.47f)
                    val barWidth = r * 0.05f

                    for (i in grillXOffsets.indices) {
                        val dynamicHeight = r * grillHeights[i] * mouthScale.value
                        val midY = cy + r * grillYOffsets[i] + (r * grillHeights[i]) / 2f
                        val dynamicTopY = midY - dynamicHeight / 2f

                        drawRoundRect(
                            color = zenithCyan,
                            topLeft = Offset(cx + r * grillXOffsets[i] - barWidth / 2f, dynamicTopY),
                            size = Size(barWidth, dynamicHeight),
                            cornerRadius = CornerRadius(barWidth / 2f)
                        )
                        drawRoundRect(
                            color = zenithOutline,
                            topLeft = Offset(cx + r * grillXOffsets[i] - barWidth / 2f, dynamicTopY),
                            size = Size(barWidth, dynamicHeight),
                            cornerRadius = CornerRadius(barWidth / 2f),
                            style = Stroke(width = 1.5f.dp.toPx())
                        )
                    }

                    // 9. HIGH-QUALITY STICKER STYLE OUTLINE
                    drawPath(path = faceBaseSilhouette, color = zenithOutline, style = Stroke(width = 4.dp.toPx()))

                    // 10. FLOWER CROWN ARRANGEMENT (Precision vector nesting)
                    fun drawDetailedFlower(center: Offset, isBlue: Boolean) {
                        val petalColor = if (isBlue) zenithBlueFlower else zenithPurple
                        // Leaf accents behind petals
                        for (angle in listOf(-45f, 0f, 45f, 135f, 180f, 225f)) {
                            drawContext.canvas.save()
                            drawContext.canvas.translate(center.x, center.y)
                            drawContext.canvas.rotate(angle)
                            drawOval(
                                color = zenithLeaf,
                                topLeft = Offset(r * 0.04f, -r * 0.03f),
                                size = Size(r * 0.10f, r * 0.06f)
                            )
                            drawOval(
                                color = zenithOutline,
                                topLeft = Offset(r * 0.04f, -r * 0.03f),
                                size = Size(r * 0.10f, r * 0.06f),
                                style = Stroke(width = 1.5f.dp.toPx())
                            )
                            drawContext.canvas.restore()
                        }
                        // 5 Rounded vector petals
                        for (i in 0..4) {
                            drawContext.canvas.save()
                            drawContext.canvas.translate(center.x, center.y)
                            drawContext.canvas.rotate(i * 72f)
                            drawOval(
                                color = petalColor,
                                topLeft = Offset(-r * 0.03f, -r * 0.08f),
                                size = Size(r * 0.06f, r * 0.09f)
                            )
                            drawOval(
                                color = zenithOutline,
                                topLeft = Offset(-r * 0.03f, -r * 0.08f),
                                size = Size(r * 0.06f, r * 0.09f),
                                style = Stroke(width = 1.5f.dp.toPx())
                            )
                            drawContext.canvas.restore()
                        }
                        // Golden Core
                        drawCircle(color = zenithGold, radius = r * 0.035f, center = center)
                        drawCircle(color = zenithOutline, radius = r * 0.035f, center = center, style = Stroke(width = 1.5f.dp.toPx()))
                    }

                    // Left Side: Blue (outer), Purple (inner)
                    drawDetailedFlower(center = Offset(cx - r * 0.64f, cy - r * 0.26f), isBlue = true)
                    drawDetailedFlower(center = Offset(cx - r * 0.36f, cy - r * 0.34f), isBlue = false)

                    // Right Side: Purple (inner), Blue (outer)
                    drawDetailedFlower(center = Offset(cx + r * 0.36f, cy - r * 0.34f), isBlue = false)
                    drawDetailedFlower(center = Offset(cx + r * 0.64f, cy - r * 0.26f), isBlue = true)

                    // --- CORNER HUD BRACKETS (BOOT-UP ALPHA) ---
                    val bracketPad = t20
                    val bracketLen = 25.dp.toPx()
                    val bracketStroke = t1_5
                    val bracketColor = appearance.systemAccentColor.copy(alpha = 0.4f * hudAlpha)

                    // Top Left
                    drawLine(color = bracketColor, start = Offset(bracketPad, bracketPad + bracketLen), end = Offset(bracketPad, bracketPad), strokeWidth = bracketStroke)
                    drawLine(color = bracketColor, start = Offset(bracketPad, bracketPad), end = Offset(bracketPad + bracketLen, bracketPad), strokeWidth = bracketStroke)
                    // Top Right
                    drawLine(color = bracketColor, start = Offset(cw - bracketPad, bracketPad + bracketLen), end = Offset(cw - bracketPad, bracketPad), strokeWidth = bracketStroke)
                    drawLine(color = bracketColor, start = Offset(cw - bracketPad, bracketPad), end = Offset(cw - bracketPad - bracketLen, bracketPad), strokeWidth = bracketStroke)
                    // Bottom Left
                    drawLine(color = bracketColor, start = Offset(bracketPad, ch - bracketPad - bracketLen), end = Offset(bracketPad, ch - bracketPad), strokeWidth = bracketStroke)
                    drawLine(color = bracketColor, start = Offset(bracketPad, ch - bracketPad), end = Offset(bracketPad + bracketLen, ch - bracketPad), strokeWidth = bracketStroke)
                    // Bottom Right
                    drawLine(color = bracketColor, start = Offset(cw - bracketPad, ch - bracketPad - bracketLen), end = Offset(cw - bracketPad, ch - bracketPad), strokeWidth = bracketStroke)
                    drawLine(color = bracketColor, start = Offset(cw - bracketPad, ch - bracketPad), end = Offset(cw - bracketPad - bracketLen, ch - bracketPad), strokeWidth = bracketStroke)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- CR-47 TITLE ---
            Text(
                text = "CR-47",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawRoundRect(
                            color = appearance.systemAccentColor.copy(alpha = 0.3f),
                            topLeft = Offset(size.width * 0.1f, size.height - 2.dp.toPx()),
                            size = Size(size.width * 0.8f, 2.dp.toPx()),
                            cornerRadius = CornerRadius(1.dp.toPx())
                        )
                    }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // --- SUBTITLE ---
            Text(
                text = "BIOMECHANICAL UNIT",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // --- STATS CARD ---
            GlowingCard(
                modifier = Modifier.fillMaxWidth(),
                borderBrush = navBrush
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatColumn(
                        value = "$currentStreak DAYS",
                        label = "STREAK"
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(appearance.systemAccentColor.copy(alpha = 0.2f))
                    )
                    StatColumn(
                        value = "${tierResult.currentTier}/10",
                        label = "TIER"
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(appearance.systemAccentColor.copy(alpha = 0.2f))
                    )
                    StatColumn(
                        value = "${exercises.size}",
                        label = "EXERCISES"
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(appearance.systemAccentColor.copy(alpha = 0.2f))
                    )
                    StatColumn(
                        value = "ONLINE",
                        label = "STATUS"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (equippedPetIds.isNotEmpty()) {
                GlowingCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderBrush = navBrush
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        equippedPetIds.forEach { petId ->
                            val pet = petInventory.find { it.id == petId }
                            if (pet != null) {
                                val species = PetCatalog.allSpecies.find { it.id == pet.speciesId }
                                Text(
                                    text = species?.emoji ?: "?",
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }
                        if (petMult > 1.0f) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "+${((petMult - 1.0f) * 100).toInt()}% XP",
                                color = Color(0xFF00FF85),
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // --- LEVEL PROGRESS CARD ---
            LevelProgressCard(
                level = currentLevel,
                xpInLevel = xpInLevel,
                xpToNext = xpToNext
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- ENTER SYSTEM BUTTON ---
            Button(
                onClick = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.HOME) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = appearance.systemAccentColor.copy(alpha = 0.15f)
                )
            ) {
                Text(
                    text = "ENTER SYSTEM",
                    color = appearance.systemAccentColor,
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Enter system",
                    tint = appearance.systemAccentColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- SYSTEM READOUT ---
            Text(
                text = "> ALL SYSTEMS NOMINAL",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary.copy(alpha = 0.5f),
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "> OFFLINE MODE — DATA SECURED",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary.copy(alpha = 0.5f),
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StatColumn(value: String, label: String) {
    val appearance = LocalAppearance.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = appearance.systemAccentColor,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun LevelProgressCard(
    level: Int,
    xpInLevel: Long,
    xpToNext: Long,
    modifier: Modifier = Modifier
) {
    val appearance = LocalAppearance.current
    val accentColor = appearance.systemAccentColor
    val secondaryColor = appearance.systemSecondaryColor
    val isMaxLevel = level >= XpEngine.MAX_LEVEL
    val progress = if (isMaxLevel) 1f else if (xpToNext > 0) (xpInLevel.toFloat() / xpToNext.toFloat()).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "xp_progress"
    )

    val borderBrush = remember(accentColor, secondaryColor) {
        Brush.horizontalGradient(listOf(accentColor, secondaryColor))
    }

    GlowingCard(
        modifier = modifier.fillMaxWidth(),
        borderBrush = borderBrush
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LEVEL",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary,
                    letterSpacing = 2.sp
                )
                if (isMaxLevel) {
                    Text(
                        text = "MAX",
                        style = MaterialTheme.typography.headlineMedium,
                        fontFamily = FontFamily.Monospace,
                        color = GoalComplete,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "$level",
                        style = MaterialTheme.typography.headlineMedium,
                        fontFamily = FontFamily.Monospace,
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(accentColor.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (isMaxLevel) {
                                Brush.horizontalGradient(
                                    colors = listOf(GoalComplete, accentColor)
                                )
                            } else {
                                Brush.horizontalGradient(
                                    colors = listOf(accentColor, secondaryColor)
                                )
                            }
                        )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (isMaxLevel) {
                    Text(
                        text = "+%,d XP BEYOND MAX".format(xpInLevel),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = GoalComplete,
                        fontSize = 10.sp
                    )
                } else {
                    Text(
                        text = "%,d / %,d XP".format(xpInLevel, xpToNext),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}