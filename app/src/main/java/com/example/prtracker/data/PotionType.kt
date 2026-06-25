package com.example.prtracker.data

enum class PotionType(
    val id: String,
    val displayName: String,
    val description: String,
    val flaskColor: Long,
    val badgeLabel: String,
    val maxStack: Int
) {
    XP_DOUBLE(
        id = "xp_double",
        displayName = "XP SURGE",
        description = "2x XP on your next logged entry.",
        flaskColor = 0xFF00FFFF,
        badgeLabel = "2x",
        maxStack = 5
    );
}
