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
    val emoji: String
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
        description = "Refines fate, removing common and uncommon pets",
        price = 500_000L,
        rollsCount = 5,
        minRarity = PetRarity.RARE,
        maxRarity = PetRarity.MYTHICAL,
        colorHex = 0xFFFF8800,
        emoji = "\u26A1"
    ),
    ASCENDANT(
        id = "ascendant",
        displayName = "ASCENDANT DICE",
        description = "Ascends beyond the ordinary — only epic and above",
        price = 1_000_000L,
        rollsCount = 5,
        minRarity = PetRarity.EPIC,
        maxRarity = PetRarity.MYTHICAL,
        colorHex = 0xFFAA44FF,
        emoji = "\uD83C\uDF0C"
    ),
    LEGENDARY(
        id = "legendary",
        displayName = "LEGENDARY DICE",
        description = "Commands legendary destiny — only legendary and above",
        price = 10_000_000L,
        rollsCount = 5,
        minRarity = PetRarity.LEGENDARY,
        maxRarity = PetRarity.MYTHICAL,
        colorHex = 0xFFFFD700,
        emoji = "\uD83D\uDC51"
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
    );

    fun toColor(): Color = Color(colorHex)

    companion object {
        fun fromId(id: String): SpecialDiceType? = entries.find { it.id == id }
        val strengthOrder: List<SpecialDiceType> = listOf(MYTHIC, LEGENDARY, ASCENDANT, REFINING, BANISHING)
    }
}

@Immutable
data class SpecialDice(
    val id: String = UUID.randomUUID().toString(),
    val typeId: String = "",
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
