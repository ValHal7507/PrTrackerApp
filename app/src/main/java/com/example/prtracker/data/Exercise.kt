package com.example.prtracker.data

import androidx.compose.runtime.Immutable

@Immutable
data class Exercise(
    val id: String,
    val name: String,
    val type: String,
    val entries: List<PREntry>,
    val isPinned: Boolean = false,
    val sortOrder: Int = 0,
    val goal: Int? = null,
    val difficulty: String = "MEDIUM"
)
