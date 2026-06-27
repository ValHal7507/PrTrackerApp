package com.example.prtracker.data

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Immutable
import java.util.UUID

enum class DiceCategory { LUCK, TIER, BURST }

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
    val baseChances: Map<PetRarity, Double>? = null,
    val category: DiceCategory = DiceCategory.LUCK
) {
    BANISHING(
        id = "banishing",
        displayName = "BANISHING DICE",
        description = "Banishes common — 1/1000 chance of divine",
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
        description = "Weighted odds — RARE 61.7%, EPIC 25%, LEGENDARY 11.1%, MYTHICAL 2.2%, DIVINE 1%",
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
        description = "Weighted odds — EPIC 63.9%, LEGENDARY 25%, MYTHICAL 11.1%, DIVINE 5%",
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
        description = "Weighted odds — LEGENDARY 75%, MYTHICAL 25%, DIVINE 12.5%",
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
        description = "Unveils mythic destiny — mythical pets, 1/9 chance of divine, 1/100k chance of exclusive",
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
        description = "Guarantees a SUPER rarity pet — 1/1000 chance of EXCLUSIVE, 1/200k chance of SECRET",
        price = 1_000_000_000_000L,
        rollsCount = 1,
        minRarity = PetRarity.SUPER,
        maxRarity = PetRarity.SUPER,
        colorHex = 0xFF001B3D,
        emoji = "\uD83D\uDCAB"
    ),
    GILDED_DICE(
        id = "gilded",
        displayName = "GILDED DICE",
        description = "Tier modifier — normal rarity, guaranteed GOLDEN+ tier (60% GOLDEN, 25% RAINBOW, 12% DARK_MATTER, 3% RED_MATTER)",
        price = 50_000_000L,
        rollsCount = 3,
        minRarity = PetRarity.COMMON,
        maxRarity = PetRarity.SECRET,
        colorHex = 0xFFFFD700,
        emoji = "\uD83C\uDF1F",
        category = DiceCategory.TIER
    ),
    BURST_100(
        id = "burst_100",
        displayName = "BURST DICE (100)",
        description = "Instant 100 rolls — no coins rewarded",
        price = 100_000_000L,
        rollsCount = 100,
        minRarity = PetRarity.COMMON,
        maxRarity = PetRarity.SECRET,
        colorHex = 0xFFFF8C00,
        emoji = "\u26A1",
        category = DiceCategory.BURST
    ),
    BURST_1000(
        id = "burst_1000",
        displayName = "BURST DICE (1K)",
        description = "Instant 1,000 rolls — no coins rewarded",
        price = 1_000_000_000L,
        rollsCount = 1000,
        minRarity = PetRarity.COMMON,
        maxRarity = PetRarity.SECRET,
        colorHex = 0xFFFF8C00,
        emoji = "\u26A1",
        category = DiceCategory.BURST
    ),
    BURST_10000(
        id = "burst_10000",
        displayName = "BURST DICE (10K)",
        description = "Instant 10,000 rolls — no coins rewarded",
        price = 10_000_000_000L,
        rollsCount = 10000,
        minRarity = PetRarity.COMMON,
        maxRarity = PetRarity.SECRET,
        colorHex = 0xFFFF8C00,
        emoji = "\u26A1",
        category = DiceCategory.BURST
    ),
    BURST_100000(
        id = "burst_100000",
        displayName = "BURST DICE (100K)",
        description = "Instant 100,000 rolls — no coins rewarded",
        price = 100_000_000_000L,
        rollsCount = 100000,
        minRarity = PetRarity.COMMON,
        maxRarity = PetRarity.SECRET,
        colorHex = 0xFFFF8C00,
        emoji = "\u26A1",
        category = DiceCategory.BURST
    );

    fun toColor(): Color = Color(colorHex)

    val isBurstDice: Boolean get() = id.startsWith("burst_")

    companion object {
        fun fromId(id: String): SpecialDiceType? = entries.find { it.id == id }
        val strengthOrder: List<SpecialDiceType> = listOf(SUPER_DICE, MYTHIC, GILDED_DICE, BURST_100000, BURST_10000, BURST_1000, BURST_100, LEGENDARY, ASCENDANT, REFINING, BANISHING)
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
    val rollsTotal: Int = 0,
    val category: String = "luck"
) {
    val diceType: SpecialDiceType? get() = SpecialDiceType.fromId(diceTypeId)
    val diceCategory: DiceCategory get() = try { DiceCategory.valueOf(category.uppercase()) } catch (_: Exception) { DiceCategory.LUCK }
    val progress: Float get() = if (rollsTotal > 0) rollsRemaining.toFloat() / rollsTotal.toFloat() else 0f
}

object SpecialDiceCatalog {
    val allTypes: List<SpecialDiceType> = SpecialDiceType.entries
    fun byId(id: String): SpecialDiceType? = SpecialDiceType.fromId(id)
}
