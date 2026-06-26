package com.example.prtracker.data

import androidx.compose.runtime.Immutable
import java.util.UUID

@Immutable
data class Pet(
    val id: String = UUID.randomUUID().toString(),
    val speciesId: String = "",
    val name: String = "",
    val rarity: String = "COMMON",
    val stars: Int = 1,
    val obtainedAt: Long = System.currentTimeMillis(),
    val rollNumber: Int = 0,
    val tier: String = "NORMAL",
    val isFavorited: Boolean = false
)

fun Pet.coinValue(): Long {
    val rarity = PetRarity.fromName(rarity)
    if (rarity == PetRarity.SUPER || rarity == PetRarity.EXCLUSIVE) {
        return (rarity.baseCoins * PetTier.fromName(tier).coinMultiplier).toLong()
    }
    val multiplier = PetTier.fromName(tier).coinMultiplier
    return rarity.baseCoins * multiplier * stars
}

fun Pet.xpMultiplier(inventory: List<Pet>? = null): Float {
    val rarity = PetRarity.fromName(rarity)
    if (rarity == PetRarity.SUPER) {
        val list = inventory ?: return 1.0f
        val bestNonSuper = list
            .filter { PetRarity.fromName(it.rarity) != PetRarity.SUPER && PetRarity.fromName(it.rarity) != PetRarity.EXCLUSIVE }
            .maxOfOrNull { it.xpMultiplier(list) }
            ?: 1.0f
        val tierMult = PetTier.fromName(tier).xpMult
        return 1.1f * tierMult * bestNonSuper
    }
    if (rarity == PetRarity.EXCLUSIVE) {
        val list = inventory ?: return 1.0f
        val bestNonSuper = list
            .filter { PetRarity.fromName(it.rarity) != PetRarity.SUPER && PetRarity.fromName(it.rarity) != PetRarity.EXCLUSIVE }
            .maxOfOrNull { it.xpMultiplier(list) }
            ?: 1.0f
        val tierMult = PetTier.fromName(tier).xpMult
        return 2.0f * tierMult * bestNonSuper
    }
    val species = PetCatalog.allSpecies.find { it.id == speciesId }
    val rarityMult = species?.xpMult ?: rarity.baseXpMult
    val tierMult = PetTier.fromName(tier).xpMult
    val starMult = 1.0f + (stars - 1) * 0.05f
    return rarityMult * tierMult * starMult
}

data class RollResult(
    val pet: Pet,
    val effectiveChances: Map<PetRarity, Double>,
    val isLuckyRoll: Boolean = false,
    val wasSold: Boolean = false,
    val displayOneInX: Int? = null
)

@Immutable
data class MiniGameSettings(
    val autoSellRarities: Set<String> = emptySet(),
    val selectedRollCount: Int = 0
)

enum class PetTier(val label: String, val colorHex: Long, val order: Int, val coinMultiplier: Int, val xpMult: Float) {
    NORMAL("NORMAL", 0xFFAAAAAA, 0, 1, 1.00f),
    SILVER("SILVER", 0xFFC0C0C0, 1, 2, 1.10f),
    GOLDEN("GOLDEN", 0xFFFFD700, 2, 4, 1.25f),
    RAINBOW("RAINBOW", 0xFFFF44FF, 3, 8, 1.45f),
    DARK_MATTER("DARK MATTER", 0xFF6A0DAD, 4, 16, 1.70f),
    RED_MATTER("RED MATTER", 0xFFDC143C, 5, 32, 2.00f);

    companion object {
        fun fromName(name: String): PetTier = entries.find { it.name == name } ?: NORMAL
        fun nextTier(current: PetTier): PetTier? = entries.getOrNull(current.order + 1)
    }
}

enum class PetRarity(val label: String, val dropChance: Double, val colorHex: Long, val baseCoins: Long, val baseXpMult: Float) {
    COMMON("COMMON", 0.544, 0xFFAAAAAA, 100L, 1.00f),
    UNCOMMON("UNCOMMON", 0.280, 0xFF44FF44, 250L, 1.05f),
    RARE("RARE", 0.130, 0xFF4488FF, 600L, 1.10f),
    EPIC("EPIC", 0.040, 0xFFAA44FF, 1500L, 1.20f),
    LEGENDARY("LEGENDARY", 0.005, 0xFFFFD700, 5000L, 1.35f),
    MYTHICAL("MYTHICAL", 0.001, 0xFFFF4444, 15000L, 1.50f),
    DIVINE("DIVINE", 0.0005, 0xFFFFE44D, 50000L, 1.60f),
    SUPER("SUPER", 0.0, 0xFF001B3D, 100_000_000_000L, 1.0f),
    EXCLUSIVE("EXCLUSIVE", 0.0, 0xFF7B35C1, 100_000_000_000_000L, 2.0f);

    companion object {
        fun fromName(name: String): PetRarity =
            entries.find { it.name == name } ?: COMMON
    }
}

@Immutable
data class PetSpecies(
    val id: String,
    val name: String,
    val rarity: PetRarity,
    val emoji: String,
    val xpMult: Float? = null
)

object PetCatalog {
    val allSpecies: List<PetSpecies> = listOf(
        // COMMON (54.4%)
        PetSpecies("bot_01", "Scrap Bot", PetRarity.COMMON, "\uD83E\uDD16"),
        PetSpecies("slime_01", "Gel Slime", PetRarity.COMMON, "\uD83D\uDFE2"),
        PetSpecies("cat_01", "Alley Cat", PetRarity.COMMON, "\uD83D\uDC31"),
        PetSpecies("mouse_01", "Wire Mouse", PetRarity.COMMON, "\uD83D\uDC2D"),
        PetSpecies("blob_01", "Data Blob", PetRarity.COMMON, "\uD83D\uDD35"),

        // UNCOMMON (28%)
        PetSpecies("fox_01", "Neon Fox", PetRarity.UNCOMMON, "\uD83E\uDD8A"),
        PetSpecies("owl_01", "Circuit Owl", PetRarity.UNCOMMON, "\uD83E\uDD89"),
        PetSpecies("frog_01", "Pixel Frog", PetRarity.UNCOMMON, "\uD83D\uDC38"),
        PetSpecies("bee_01", "Buzz Drone", PetRarity.UNCOMMON, "\uD83D\uDC1D"),

        // RARE (13%)
        PetSpecies("wolf_01", "Cyber Wolf", PetRarity.RARE, "\uD83D\uDC3A"),
        PetSpecies("dragon_01", "Hatchling", PetRarity.RARE, "\uD83D\uDC09"),
        PetSpecies("phoenix_01", "Spark Phoenix", PetRarity.RARE, "\uD83E\uDDA5"),

        // EPIC (4%)
        PetSpecies("titan_01", "Void Titan", PetRarity.EPIC, "\uD83D\uDD2E"),
        PetSpecies("unicorn_01", "Prism Unicorn", PetRarity.EPIC, "\uD83E\uDD84"),

        // LEGENDARY (0.5%)
        PetSpecies("leviathan_01", "Storm Leviathan", PetRarity.LEGENDARY, "\uD83D\uDC0F"),
        PetSpecies("golem_01", "Obsidian Golem", PetRarity.LEGENDARY, "\uD83E\uDDF1"),

        // MYTHICAL (0.1%)
        PetSpecies("void_01", "Void Walker", PetRarity.MYTHICAL, "\u2B50"),
        PetSpecies("cosmos_01", "Cosmos Drake", PetRarity.MYTHICAL, "\uD83C\uDF0C"),

        // DIVINE (0.05% each — 1/2000)
        PetSpecies("divine_01", "Aurelion", PetRarity.DIVINE, "\uD83D\uDC51", 1.60f),
        PetSpecies("divine_02", "Seraphel", PetRarity.DIVINE, "\uD83C\uDF1F", 1.75f),
        PetSpecies("divine_03", "Thundius", PetRarity.DIVINE, "\u26A1", 2.00f),

        // SUPER (1 in 100,000 — handled specially)
        PetSpecies("super_dog_01", "Rex", PetRarity.SUPER, "\uD83D\uDC15"),
        PetSpecies("super_dog_02", "Buddy", PetRarity.SUPER, "\uD83D\uDC36"),

        // EXCLUSIVE (1 in 1,000,000 — handled specially)
        PetSpecies("zenith_01", "Zenith", PetRarity.EXCLUSIVE, "\uD83D\uDC09")
    )

    fun speciesForRarity(rarity: PetRarity): List<PetSpecies> =
        allSpecies.filter { it.rarity == rarity }
}
