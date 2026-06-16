package com.example.prtracker.data

import androidx.compose.runtime.Immutable

@Immutable
data class RunEntry(
    val id: String,
    val distanceMeters: Float,
    val durationSeconds: Int,
    val date: Long,
    val note: String = ""
) {
    val paceSecondsPerKm: Float
        get() = if (distanceMeters > 0f) durationSeconds / (distanceMeters / 1000f) else 0f

    val caloriesBurned: Int
        get() = (distanceMeters / 1000f * 65f).toInt()

    val speedKmh: Float
        get() = if (durationSeconds > 0) (distanceMeters / 1000f) / (durationSeconds / 3600f) else 0f
}
