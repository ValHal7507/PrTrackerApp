package com.example.prtracker.data

import androidx.compose.runtime.Immutable

@Immutable
data class RunningPRs(
    val bestPaceSecondsPerKm: Float? = null,
    val bestDistanceMeters: Float? = null,
    val bestDurationSeconds: Int? = null,
    val best500mSeconds: Int? = null,
    val best1kmSeconds: Int? = null,
    val best2kmSeconds: Int? = null,
    val best5kmSeconds: Int? = null,
    val best10kmSeconds: Int? = null,
    val bestCaloriesInRun: Int? = null,
    val totalDistanceMeters: Float = 0f,
    val totalRuns: Int = 0
)
