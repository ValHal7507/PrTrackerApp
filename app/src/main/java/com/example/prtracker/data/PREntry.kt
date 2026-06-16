package com.example.prtracker.data

import androidx.compose.runtime.Immutable

@Immutable
data class PREntry(
    val id: String,
    val value: Int,
    val date: Long,
    val note: String = ""
)
