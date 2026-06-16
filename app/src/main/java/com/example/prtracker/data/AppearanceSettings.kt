package com.example.prtracker.data

import androidx.compose.runtime.Immutable

@Immutable
data class AppearanceSettings(
    val exerciseAccent: Long = 0xFF00F5FF,
    val exerciseSecondary: Long = 0xFF7B2FFF,
    val pinnedAccent: Long = 0xFF00FF85,
    val pinnedSecondary: Long = 0xFFFF003C,
    val runningAccent: Long = 0xFFFF2D78,
    val runningSecondary: Long = 0xFFC4005A,
    val systemAccent: Long = 0xFF00F5FF,
    val systemSecondary: Long = 0xFF7B2FFF,
    val activeThemeId: String = "default"
)
