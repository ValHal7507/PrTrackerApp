package com.example.prtracker.data

import androidx.compose.runtime.Immutable

@Immutable
data class LeverageTelemetry(
    val entryId: String,
    val dateString: String,
    val rawValue: Int,
    val pairedWeight: Float,
    val rsiScore: Int
)
