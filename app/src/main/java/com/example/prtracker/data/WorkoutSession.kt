package com.example.prtracker.data

import androidx.compose.runtime.Immutable

@Immutable
data class SessionSetEntry(
    val value: Int = 0
)

@Immutable
data class SessionExerciseProgress(
    val exerciseName: String = "",
    val targetValue: Int = 0,
    val isHold: Boolean = false,
    val totalSets: Int = 0,
    val completedSets: List<SessionSetEntry> = emptyList()
)

@Immutable
data class WorkoutSession(
    val id: String = "",
    val presetId: String = "",
    val presetName: String = "",
    val startedAt: Long = 0L,
    val pausedDurationMs: Long = 0L,
    val pausedSinceMs: Long = 0L,
    val isPaused: Boolean = false,
    val isCompleted: Boolean = false,
    val exercises: List<SessionExerciseProgress> = emptyList()
) {
    fun elapsedMs(now: Long): Long {
        if (startedAt == 0L) return 0L
        val totalPaused = if (isPaused) pausedDurationMs + (now - pausedSinceMs).coerceAtLeast(0L) else pausedDurationMs
        return (now - startedAt - totalPaused).coerceAtLeast(0L)
    }
}
