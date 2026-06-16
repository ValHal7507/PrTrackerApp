package com.example.prtracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.example.prtracker.ui.theme.CardBackground
import com.example.prtracker.ui.theme.LocalAppearance
import com.example.prtracker.ui.theme.systemAccentColor
import com.example.prtracker.ui.theme.systemSecondaryColor

@Composable
fun GlowingCard(
    modifier: Modifier = Modifier,
    borderBrush: Brush? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val appearance = LocalAppearance.current
    val defaultBorderBrush = remember(appearance.systemAccent, appearance.systemSecondary) {
        Brush.linearGradient(listOf(appearance.systemAccentColor, appearance.systemSecondaryColor))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .border(
                BorderStroke(1.dp, borderBrush ?: defaultBorderBrush),
                RoundedCornerShape(16.dp)
            ),
        content = content
    )
}
