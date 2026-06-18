package com.example.prtracker.data

import androidx.compose.runtime.Immutable
import java.util.UUID

@Immutable
data class PresetExercise(
    val exerciseName: String = "",
    val targetReps: Int = 0,
    val targetHoldSeconds: Int = 0,
    val isUntilFailure: Boolean = false,
    val sets: Int = 3,
    val notes: String = ""
)

@Immutable
data class WorkoutPreset(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val description: String = "",
    val exercises: List<PresetExercise> = emptyList(),
    val difficulty: String = "MEDIUM",
    val createdAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val sortOrder: Int = 0
)
