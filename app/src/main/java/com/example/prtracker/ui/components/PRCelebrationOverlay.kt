package com.example.prtracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.prtracker.ui.theme.GoalComplete
import com.example.prtracker.ui.theme.SuccessPurple
import kotlinx.coroutines.delay

@Composable
fun PRCelebrationOverlay(
    visible: Boolean,
    onDismiss: () -> Unit,
    xpEarned: Long = 0L
) {
    var showGreenFlash by remember(visible) { mutableStateOf(false) }

    LaunchedEffect(visible) {
        if (visible) {
            showGreenFlash = true
            delay(300)
            showGreenFlash = false
            delay(2200)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onDismiss() }
        ) {
            if (showGreenFlash) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SuccessPurple.copy(alpha = 0.6f))
                )
            }

            val composition by rememberLottieComposition(
                spec = LottieCompositionSpec.Url(
                    "https://assets.lottiefiles.com/packages/lf20_touohxv0.json"
                )
            )

            LottieAnimation(
                composition = composition,
                iterations = 1,
                modifier = Modifier.fillMaxSize(),
                speed = 1.5f
            )

            Text(
                text = "NEW PERSONAL\nRECORD!",
                style = MaterialTheme.typography.headlineLarge,
                color = SuccessPurple,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .statusBarsPadding()
                    .align(Alignment.Center)
            )
            if (xpEarned > 0L) {
                Text(
                    text = "+$xpEarned XP",
                    style = MaterialTheme.typography.titleLarge,
                    color = GoalComplete,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 64.dp)
                        .align(Alignment.BottomCenter)
                )
            }
        }
    }
}
