package com.example.prtracker.data

import kotlin.math.pow

enum class PetUpgrade(
    val id: String,
    val displayName: String,
    val description: String,
    val baseCost: Long,
    val costMultiplier: Float,
    val fixedCosts: List<Long>? = null
) {
    LUCK(
        id = "luck",
        displayName = "LUCK",
        description = "Improves rare drop chances (+20% per level)",
        baseCost = 500L,
        costMultiplier = 1.12f
    ),
    COIN_MULTIPLIER(
        id = "coin_multiplier",
        displayName = "COIN MULTIPLIER",
        description = "Boosts coins earned per roll (+0.20x per level)",
        baseCost = 600L,
        costMultiplier = 1.13f
    ),
    ROLL_SPEED(
        id = "roll_speed",
        displayName = "ROLL SPEED",
        description = "Faster dice animation (-72ms per level, min 200ms)",
        baseCost = 300L,
        costMultiplier = 1.10f
    ),
    LUCKY_ROLL(
        id = "lucky_roll",
        displayName = "LUCKY ROLL",
        description = "Every 5th roll is lucky — boosted rarity chances",
        baseCost = 1000L,
        costMultiplier = 1.15f
    ),
    EQUIP_SLOTS(
        id = "equip_slots",
        displayName = "EQUIP SLOTS",
        description = "Equip more pets for XP bonuses (max 5)",
        baseCost = 1_000_000L,
        costMultiplier = 1.0f,
        fixedCosts = listOf(1_000_000L, 10_000_000L, 100_000_000L)
    );

    fun costForLevel(currentLevel: Int): Long {
        if (fixedCosts != null) {
            return if (currentLevel < fixedCosts.size) fixedCosts[currentLevel] else Long.MAX_VALUE
        }
        return (baseCost * costMultiplier.pow(currentLevel * 0.82f)).toLong()
    }

    fun nextLevelCost(currentLevel: Int): Long =
        costForLevel(currentLevel)

    fun maxLevel(): Int? = fixedCosts?.size

    companion object {
        fun fromId(id: String): PetUpgrade? = entries.find { it.id == id }
    }
}
