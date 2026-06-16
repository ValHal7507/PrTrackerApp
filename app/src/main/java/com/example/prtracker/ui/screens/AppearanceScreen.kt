package com.example.prtracker.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.prtracker.data.ALL_THEMES
import com.example.prtracker.data.AppearanceSettings
import com.example.prtracker.data.AppTheme
import com.example.prtracker.data.NEON_PALETTE
import com.example.prtracker.ui.components.GlowingCard
import com.example.prtracker.ui.components.GridBackground
import com.example.prtracker.ui.theme.CardBackground
import com.example.prtracker.ui.theme.LocalAppearance
import com.example.prtracker.ui.theme.TextPrimary
import com.example.prtracker.ui.theme.TextSecondary
import com.example.prtracker.ui.theme.exerciseAccentColor
import com.example.prtracker.ui.theme.exerciseSecondaryColor
import com.example.prtracker.ui.theme.pinnedAccentColor
import com.example.prtracker.ui.theme.pinnedSecondaryColor
import com.example.prtracker.ui.theme.runningAccentColor
import com.example.prtracker.ui.theme.runningSecondaryColor
import com.example.prtracker.ui.theme.systemAccentColor
import com.example.prtracker.ui.theme.systemSecondaryColor
import com.example.prtracker.viewmodel.PRViewModel

@Composable
fun AppearanceScreen(
    viewModel: PRViewModel,
    navController: NavHostController
) {
    val appSettings by viewModel.appSettings.collectAsState()
    val currentAppearance = appSettings.appearance

    Box(modifier = Modifier.fillMaxSize()) {
        GridBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = currentAppearance.systemAccentColor
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "APPEARANCE",
                    style = MaterialTheme.typography.displayLarge,
                    color = currentAppearance.systemAccentColor
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "PRESET THEMES",
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(ALL_THEMES) { theme ->
                    ThemeCard(
                        theme = theme,
                        isSelected = currentAppearance.activeThemeId == theme.id,
                        onClick = { viewModel.applyTheme(theme) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "CUSTOM COLORS",
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Individually recolor each accent system",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))

            ColorSectionCard(
                label = "EXERCISES",
                description = "Cards, charts, PR numbers",
                currentColors = listOf(
                    currentAppearance.exerciseAccentColor,
                    currentAppearance.exerciseSecondaryColor
                ),
                onColorChange = { index, colorLong ->
                    val updated = when (index) {
                        0 -> currentAppearance.copy(
                            exerciseAccent = colorLong,
                            activeThemeId = "custom"
                        )
                        else -> currentAppearance.copy(
                            exerciseSecondary = colorLong,
                            activeThemeId = "custom"
                        )
                    }
                    viewModel.updateAppearance(updated)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ColorSectionCard(
                label = "PINNED EXERCISES",
                description = "Pinned card borders & accents",
                currentColors = listOf(
                    currentAppearance.pinnedAccentColor,
                    currentAppearance.pinnedSecondaryColor
                ),
                onColorChange = { index, colorLong ->
                    val updated = when (index) {
                        0 -> currentAppearance.copy(
                            pinnedAccent = colorLong,
                            activeThemeId = "custom"
                        )
                        else -> currentAppearance.copy(
                            pinnedSecondary = colorLong,
                            activeThemeId = "custom"
                        )
                    }
                    viewModel.updateAppearance(updated)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ColorSectionCard(
                label = "RUNNING",
                description = "Running cards, badges & stats",
                currentColors = listOf(
                    currentAppearance.runningAccentColor,
                    currentAppearance.runningSecondaryColor
                ),
                onColorChange = { index, colorLong ->
                    val updated = when (index) {
                        0 -> currentAppearance.copy(
                            runningAccent = colorLong,
                            activeThemeId = "custom"
                        )
                        else -> currentAppearance.copy(
                            runningSecondary = colorLong,
                            activeThemeId = "custom"
                        )
                    }
                    viewModel.updateAppearance(updated)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ColorSectionCard(
                label = "SYSTEM / UI",
                description = "Buttons, nav bar, icons, borders",
                currentColors = listOf(
                    currentAppearance.systemAccentColor,
                    currentAppearance.systemSecondaryColor
                ),
                onColorChange = { index, colorLong ->
                    val updated = when (index) {
                        0 -> currentAppearance.copy(
                            systemAccent = colorLong,
                            activeThemeId = "custom"
                        )
                        else -> currentAppearance.copy(
                            systemSecondary = colorLong,
                            activeThemeId = "custom"
                        )
                    }
                    viewModel.updateAppearance(updated)
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Live preview
            Text(
                text = "PREVIEW",
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(12.dp))

            PreviewCard(appearance = currentAppearance)

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ThemeCard(
    theme: AppTheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderBrush = remember(theme.settings.systemAccent, theme.settings.systemSecondary) {
        Brush.linearGradient(
            listOf(
                Color(theme.settings.systemAccent),
                Color(theme.settings.systemSecondary)
            )
        )
    }

    val borderStrokeColor = if (isSelected) Color.Transparent else TextSecondary.copy(alpha = 0.3f)
    val borderStrokeWidth = if (isSelected) 2.dp else 1.dp
    val borderStroke = if (isSelected) {
        BorderStroke(borderStrokeWidth, borderBrush)
    } else {
        BorderStroke(borderStrokeWidth, borderStrokeColor)
    }

    Box(
        modifier = Modifier
            .width(130.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) CardBackground
                else CardBackground.copy(alpha = 0.5f)
            )
            .border(borderStroke, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color(theme.settings.systemAccent))
                )
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color(theme.settings.systemSecondary))
                )
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color(theme.settings.runningAccent))
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = theme.name,
                style = MaterialTheme.typography.labelMedium,
                color = TextPrimary,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = theme.description,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun ColorSectionCard(
    label: String,
    description: String,
    currentColors: List<Color>,
    onColorChange: (Int, Long) -> Unit
) {
    GlowingCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                currentColors.forEachIndexed { index, color ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (index == 0) "Primary" else "Secondary",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(1.dp, TextSecondary.copy(alpha = 0.3f), CircleShape)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Pick from palette:",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(NEON_PALETTE) { (name, colorLong) ->
                    val color = Color(colorLong)
                    val isActive = color in currentColors
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                BorderStroke(
                                    if (isActive) 2.dp else 0.dp,
                                    TextPrimary
                                ),
                                CircleShape
                            )
                            .clickable {
                                val activeIndex = currentColors.indexOfFirst { it == color }
                                if (activeIndex >= 0) {
                                    val nextIndex = (activeIndex + 1) % currentColors.size
                                    onColorChange(nextIndex, colorLong)
                                } else {
                                    val targetIndex = 0
                                    onColorChange(targetIndex, colorLong)
                                }
                            }
                    )
                }
            }
        }
    }
}

@Composable
private fun PreviewCard(appearance: AppearanceSettings) {
    val gradientBrush = remember(appearance.systemAccent, appearance.systemSecondary) {
        Brush.linearGradient(
            listOf(appearance.systemAccentColor, appearance.systemSecondaryColor)
        )
    }

    GlowingCard(
        modifier = Modifier.fillMaxWidth(),
        borderBrush = gradientBrush
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = "SYSTEM PREVIEW",
                style = MaterialTheme.typography.labelLarge,
                color = appearance.systemAccentColor,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(appearance.systemAccentColor.copy(alpha = 0.15f))
                        .border(
                            BorderStroke(1.dp, gradientBrush),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "BUTTON",
                        color = appearance.systemAccentColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(appearance.exerciseAccentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "REPS",
                        color = appearance.exerciseAccentColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(appearance.pinnedAccentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "PIN",
                        color = appearance.pinnedAccentColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(appearance.runningAccentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "RUN",
                        color = appearance.runningAccentColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
