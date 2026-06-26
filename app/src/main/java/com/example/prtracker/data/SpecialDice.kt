package com.example.prtracker.data

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Immutable
import java.util.UUID

enum class SpecialDiceType(
    val id: String,
    val displayName: String,
    val description: String,
    val price: Long,
    val rollsCount: Int,
    val minRarity: PetRarity,
    val maxRarity: PetRarity,
    val colorHex: Long,
    val emoji: String,
    val baseChances: Map<PetRarity, Double>? = null
) {
    BANISHING(
        id = "banishing",
        displayName = "BANISHING DICE",
        description = "Banishes all common pets for the next rolls",
        price = 100_000L,
        rollsCount = 10,
        minRarity = PetRarity.UNCOMMON,
        maxRarity = PetRarity.MYTHICAL,
        colorHex = 0xFFFF4444,
        emoji = "\uD83D\uDD25"
    ),
    REFINING(
        id = "refining",
        displayName = "REFINING DICE",
        description = "Weighted odds — RARE 61.7%, EPIC 25%, LEGENDARY 11.1%, MYTHICAL 2.2%",
        price = 500_000L,
        rollsCount = 5,
        minRarity = PetRarity.RARE,
        maxRarity = PetRarity.MYTHICAL,
        colorHex = 0xFFFF8800,
        emoji = "\u26A1",
        baseChances = mapOf(
            PetRarity.RARE to 0.6171497584541063,
            PetRarity.EPIC to 0.25,
            PetRarity.LEGENDARY to 1.0 / 9.0,
            PetRarity.MYTHICAL to 1.0 / 46.0
        )
    ),
    ASCENDANT(
        id = "ascendant",
        displayName = "ASCENDANT DICE",
        description = "Weighted odds — EPIC 63.9%, LEGENDARY 25%, MYTHICAL 11.1%",
        price = 1_000_000L,
        rollsCount = 5,
        minRarity = PetRarity.EPIC,
        maxRarity = PetRarity.MYTHICAL,
        colorHex = 0xFFAA44FF,
        emoji = "\uD83C\uDF0C",
        baseChances = mapOf(
            PetRarity.EPIC to 0.6388888888888889,
            PetRarity.LEGENDARY to 0.25,
            PetRarity.MYTHICAL to 1.0 / 9.0
        )
    ),
    LEGENDARY(
        id = "legendary",
        displayName = "LEGENDARY DICE",
        description = "Weighted odds — LEGENDARY 75%, MYTHICAL 25%",
        price = 10_000_000L,
        rollsCount = 5,
        minRarity = PetRarity.LEGENDARY,
        maxRarity = PetRarity.MYTHICAL,
        colorHex = 0xFFFFD700,
        emoji = "\uD83D\uDC51",
        baseChances = mapOf(
            PetRarity.LEGENDARY to 0.75,
            PetRarity.MYTHICAL to 0.25
        )
    ),
    MYTHIC(
        id = "mythic",
        displayName = "MYTHIC DICE",
        description = "Unveils pure mythic destiny — only mythical pets",
        price = 25_000_000L,
        rollsCount = 5,
        minRarity = PetRarity.MYTHICAL,
        maxRarity = PetRarity.MYTHICAL,
        colorHex = 0xFF00F5FF,
        emoji = "\uD83D\uDC8E"
    ),
    SUPER_DICE(
        id = "super_dice",
        displayName = "SUPER DICE",
        description = "Guarantees a SUPER rarity pet — cosmic tier, 1 in 100,000 on demand",
        price = 1_000_000_000_000L,
        rollsCount = 1,
        minRarity = PetRarity.SUPER,
        maxRarity = PetRarity.SUPER,
        colorHex = 0xFF001B3D,
        emoji = "\uD83D\uDCAB"
    );

    fun toColor(): Color = Color(colorHex)

    companion object {
        fun fromId(id: String): SpecialDiceType? = entries.find { it.id == id }
        val strengthOrder: List<SpecialDiceType> = listOf(SUPER_DICE, MYTHIC, LEGENDARY, ASCENDANT, REFINING, BANISHING)
    }
}

@Immutable
data class SpecialDice(
    val id: String = UUID.randomUUID().toString(),
    val typeId: String = "",
    val quantity: Int = 1,
    val obtainedAt: Long = System.currentTimeMillis()
) {
    val diceType: SpecialDiceType? get() = SpecialDiceType.fromId(typeId)
}

@Immutable
data class ActiveDiceEffect(
    val diceTypeId: String = "",
    val rollsRemaining: Int = 0,
    val rollsTotal: Int = 0
) {
    val diceType: SpecialDiceType? get() = SpecialDiceType.fromId(diceTypeId)
    val progress: Float get() = if (rollsTotal > 0) rollsRemaining.toFloat() / rollsTotal.toFloat() else 0f
}

object SpecialDiceCatalog {
    val allTypes: List<SpecialDiceType> = SpecialDiceType.entries
    fun byId(id: String): SpecialDiceType? = SpecialDiceType.fromId(id)
}
