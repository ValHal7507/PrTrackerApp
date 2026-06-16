package com.example.prtracker.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.prtracker.data.AppearanceSettings

val LocalAppearance = compositionLocalOf { AppearanceSettings() }

val AppearanceSettings.exerciseAccentColor: Color get() = Color(exerciseAccent)
val AppearanceSettings.exerciseSecondaryColor: Color get() = Color(exerciseSecondary)
val AppearanceSettings.pinnedAccentColor: Color get() = Color(pinnedAccent)
val AppearanceSettings.pinnedSecondaryColor: Color get() = Color(pinnedSecondary)
val AppearanceSettings.runningAccentColor: Color get() = Color(runningAccent)
val AppearanceSettings.runningSecondaryColor: Color get() = Color(runningSecondary)
val AppearanceSettings.systemAccentColor: Color get() = Color(systemAccent)
val AppearanceSettings.systemSecondaryColor: Color get() = Color(systemSecondary)
