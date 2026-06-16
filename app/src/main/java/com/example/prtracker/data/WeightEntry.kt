package com.example.prtracker.data

import androidx.compose.runtime.Immutable

@Immutable
data class WeightEntry(
    val id: String,
    val weight: Float,
    val date: Long,
    val note: String = ""
)
