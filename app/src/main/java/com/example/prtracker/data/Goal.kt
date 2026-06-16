package com.example.prtracker.data

import androidx.compose.runtime.Immutable

@Immutable
data class Goal(
    val id: String,
    val exerciseId: String,
    val exerciseName: String,
    val targetValue: Int,
    val period: String,
    val createdAt: Long,
    val type: String
)
