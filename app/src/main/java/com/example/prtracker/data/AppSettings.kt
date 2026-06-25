package com.example.prtracker.data

data class AppSettings(
    val weightUnit: String = "kg",
    val targetWeight: Float? = null,
    val calendarDayViewMode: String = "pr",
    val morningReminderHour: Int = 8,
    val morningReminderMinute: Int = 0,
    val eveningReviewHour: Int = 21,
    val eveningReviewMinute: Int = 30,
    val soundEnabled: Boolean = true,
    val soundVolume: Float = 0.5f,
    val hapticEnabled: Boolean = true,
    val speechesEnabled: Boolean = true,
    val bestRestGameServings: Int = 0,
    val appearance: AppearanceSettings = AppearanceSettings()
)
