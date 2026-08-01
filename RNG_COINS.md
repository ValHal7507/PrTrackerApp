# PRTracker Coin System (RNG / Pet Dice)

## Overview

Coins are a virtual currency earned exclusively through the Pet Dice mini-game (RNG system). They are used to purchase upgrades (pet shop) and special dice (dice shop). Coins are **not** connected to exercise logging, XP, or any other part of the app — they are purely a pet-system currency.

All coin data is persisted in `pets.json` under the `PetStorageData.coins` field (a `Long`, default `0L`). Pet data is stored separately from app data to avoid rewriting `prs.json` on every pet action.

---

## 1. Coin Sources (Earning)

### 1.1 Dice Rolls (Primary Source)

Every time the player rolls the dice via `PRViewModel.rollDice()`, they earn coins based on the pet they rolled. **Burst dice rolls (`burstRoll()`) award no coins** — this only applies to normal `rollDice()` calls.

**Normal pets (COMMON → DIVINE):**
```kotlin
val upgradeMult = 1.0 + getUpgradeLevel(PetUpgrade.COIN_MULTIPLIER) * 0.20
val petCoinMult = petXpMultiplier().toDouble()
_coins.value += (pet.coinValue().toLong() * upgradeMult * petCoinMult).toLong()
```

The formula is:

```
coins_earned = floor(pet.coinValue × coinMultiplierUpgrade × petXpMultiplier)
```

Where:
- **`pet.coinValue()`** = `baseCoins × tierCoinMultiplier × stars`
- **`coinMultiplierUpgrade`** = `1.0 + COIN_MULTIPLIER_level × 0.20`
- **`petXpMultiplier()`** = additive stacking of XP multipliers from all equipped pets

**SUPER pets (both natural 1/100k and SUPER DICE):**

```
coins_earned = 50,000,000,000  (flat 50B, no multipliers)
```

SUPER pets award a flat 50 billion coins regardless of coin multiplier upgrades or equipped pets. This applies to both natural 1/100k SUPER rolls and SUPER DICE rolls.

**EXCLUSIVE pets (natural 1/1M):**

```
coins_earned = 100,000,000,000,000  (flat 100T, no multipliers)
```

**SECRET pets (natural 1/2M):**

```
coins_earned = 5,000,000,000,000,000 × tierMultiplier  (no multipliers)
```

SUPER/EXCLUSIVE/SECRET roll rewards do not use coin multiplier or pet XP multiplier.

### 1.2 Selling Pets

| Method | Description | Coins Earned |
|--------|-------------|-------------|
| `sellPet(petId)` | Sells a single pet by ID | `pet.coinValue()` |
| `sellAllUnfavorited()` | Sells all non-favorited pets at once | Sum of `pet.coinValue()` for all unfavorited pets |
| `sellPets(ids)` | Sells a specific collection of non-favorited pets | Sum of `pet.coinValue()` for the selected pets |

**Normal pets (COMMON → DIVINE):** `pet.coinValue() × coinMultiplierUpgrade`
- `coinMultiplierUpgrade = 1.0 + (COIN_MULTIPLIER level × 0.20)`

**Premium pets (SUPER, EXCLUSIVE, SECRET):** `pet.coinValue()` only (no coin multiplier upgrade applied)

Favorited pets (`isFavorited = true`) are **protected** — they are excluded from sell operations.

**Burst dice rolls:** Burst dice (BURST_100, BURST_1000, BURST_10000, BURST_100000) award **no coins** — neither from rolls nor from auto-sell during the burst. Pets auto-sold during a burst still count toward species tier counts (index tracking) but yield 0 coins.

### 1.3 Fusing Pets

Fusing pets (upgrading tier) does **not** award coins. It consumes the pet and upgrades its tier. No coin transaction occurs during fusion.

---

## 2. Pet Coin Value Calculation

File: `app/src/main/java/com/example/prtracker/data/Pet.kt`

```kotlin
fun Pet.coinValue(): Long {
    val rarity = PetRarity.fromName(rarity)
    if (rarity == PetRarity.SUPER || rarity == PetRarity.EXCLUSIVE || rarity == PetRarity.SECRET) {
        return (rarity.baseCoins * PetTier.fromName(tier).coinMultiplier).toLong()  // no star multiplier for premium pets
    }
    val multiplier = PetTier.fromName(tier).coinMultiplier
    return rarity.baseCoins * multiplier * stars
}
```

SUPER, EXCLUSIVE, and SECRET pets skip the star multiplier — they use `rarity.baseCoins × tierMultiplier` only. This applies to both sell value and roll reward.

### 2.1 Base Coin Values by Rarity

| Rarity | Drop Chance | `baseCoins` |
|--------|------------|-------------|
| COMMON | 54.4% | 100 |
| UNCOMMON | 28.0% | 250 |
| RARE | 13.0% | 600 |
| EPIC | 4.0% | 1,500 |
| LEGENDARY | 0.5% | 5,000 |
| MYTHICAL | 0.1% | 15,000 |
| DIVINE | 0.05% (1/2000 each) | 50,000 |
| SUPER | 0.001% (1/100k — independent) | 100,000,000,000 |
| EXCLUSIVE | 1/1M (independent) | 100,000,000,000,000 |
| SECRET | 1/2M (independent) | 5,000,000,000,000,000 |

### 2.2 Tier Coin Multipliers

| Tier | `coinMultiplier` | `xpMult` |
|------|-----------------|----------|
| NORMAL | 1× | 1.00× |
| SILVER | 2× | 1.10× |
| GOLDEN | 4× | 1.25× |
| RAINBOW | 8× | 1.45× |
| DARK_MATTER | 16× | 1.70× |
| RED_MATTER | 32× | 2.00× |

### 2.3 Star Multiplier

Stars apply as a direct multiplication factor (`stars`). A pet with 5 stars earns 5× the base coin value compared to 1-star. SUPER, EXCLUSIVE, and SECRET pets skip the star multiplier — they use `rarity.baseCoins × tierMultiplier` only.

### 2.4 Example Coin Values (max without upgrades)

| Rarity | Tier | Stars | Coin Value |
|--------|------|-------|-----------|
| COMMON | NORMAL | 1★ | 100 |
| COMMON | NORMAL | 5★ | 500 |
| EPIC | NORMAL | 1★ | 1,500 |
| EPIC | GOLDEN | 1★ | 6,000 |
| EPIC | GOLDEN | 5★ | 30,000 |
| MYTHICAL | RED_MATTER | 5★ | 15,000 × 32 × 5 = **2,400,000** |
| DIVINE | NORMAL | 1★ | 50,000 × 1 = **50,000** |
| DIVINE | RED_MATTER | 5★ | 50,000 × 32 × 5 = **8,000,000** |
| SUPER | NORMAL | — | 100,000,000,000 × 1 = **100B** (no stars) |
| SUPER | RED_MATTER | — | 100,000,000,000 × 32 = **3.2T** |
| EXCLUSIVE | NORMAL | — | 100,000,000,000,000 × 1 = **100T** (no stars) |
| EXCLUSIVE | RED_MATTER | — | 100,000,000,000,000 × 32 = **3.2Q** |
| SECRET | NORMAL | — | 5,000,000,000,000,000 × 1 = **5Q** (no stars) |
| SECRET | RED_MATTER | — | 5,000,000,000,000,000 × 32 = **160Q** |

---

## 3. Pet XP Multiplier (Affects Coin Earnings)

File: `app/src/main/java/com/example/prtracker/data/Pet.kt`

**Standard (non-SUPER) formula:**
```kotlin
fun Pet.xpMultiplier(inventory: List<Pet>? = null): Float {
    val rarity = PetRarity.fromName(rarity)
    if (rarity == PetRarity.SUPER) {
        val list = inventory ?: return 1.0f
        val bestNonSuper = list
            .filter { PetRarity.fromName(it.rarity) != PetRarity.SUPER && PetRarity.fromName(it.rarity) != PetRarity.EXCLUSIVE && PetRarity.fromName(it.rarity) != PetRarity.SECRET }
            .maxOfOrNull { it.xpMultiplier(list) } ?: 1.0f
        val tierMult = PetTier.fromName(tier).xpMult
        return 1.1f * tierMult * bestNonSuper
    }
    if (rarity == PetRarity.EXCLUSIVE) {
        val list = inventory ?: return 1.0f
        val bestNonSuper = list
            .filter { PetRarity.fromName(it.rarity) != PetRarity.SUPER && PetRarity.fromName(it.rarity) != PetRarity.EXCLUSIVE && PetRarity.fromName(it.rarity) != PetRarity.SECRET }
            .maxOfOrNull { it.xpMultiplier(list) } ?: 1.0f
        val tierMult = PetTier.fromName(tier).xpMult
        return 2.0f * tierMult * bestNonSuper
    }
    if (rarity == PetRarity.SECRET) {
        val list = inventory ?: return 1.0f
        val bestNonPremium = list
            .filter { PetRarity.fromName(it.rarity) != PetRarity.SUPER && PetRarity.fromName(it.rarity) != PetRarity.EXCLUSIVE && PetRarity.fromName(it.rarity) != PetRarity.SECRET }
            .maxOfOrNull { it.xpMultiplier(list) } ?: 1.0f
        val tierMult = PetTier.fromName(tier).xpMult
        return 5.0f * tierMult * bestNonPremium
    }
    val species = PetCatalog.allSpecies.find { it.id == speciesId }
    val rarityMult = species?.xpMult ?: rarity.baseXpMult
    val tierMult = PetTier.fromName(tier).xpMult
    val starMult = 1.0f + (stars - 1) * 0.01f
    return rarityMult * tierMult * starMult
}
```

The `inventory` parameter is only needed for SUPER/EXCLUSIVE/SECRET pets (to find the best non-premium multiplier). Non-premium pets ignore it. DIVINE species have per-species XP overrides via `PetSpecies.xpMult`: Aurelion (1.60x), Seraphel (1.75x), Thundius (2.00x).

### 3.1 Equipped Pet XP Multiplier Stacking

File: `app/src/main/java/com/example/prtracker/viewmodel/PRViewModel.kt`

```kotlin
fun petXpMultiplier(): Float {
    val equipped = _equippedPetIds.value
    if (equipped.isEmpty()) return 1.0f
    var mult = 1.0f
    for (id in equipped) {
        val pet = _petInventory.value.find { it.id == id } ?: continue
        mult += pet.xpMultiplier() - 1.0f
    }
    return mult.coerceAtLeast(1.0f)
}
```

Multiple equipped pets stack **additively**: each pet contributes `(its xpMultiplier - 1.0)`. Base is 1.0, plus the surplus from each pet.

Equip slots start at 2 (base) and can be increased with the `EQUIP_SLOTS` upgrade (up to 5 total slots).

### 3.2 Example: Coin Earned from a Roll

Scenario: Player rolls an EPIC pet (GOLDEN tier, 1★) with COIN_MULTIPLIER level 3 and two equipped pets (each providing 1.15× XP mult):

```
pet.coinValue() = 1500 × 4 × 1 = 6,000
coinMultiplierUpgrade = 1.0 + 3 × 0.20 = 1.60
petXpMultiplier = 1.0 + (1.15 - 1.0) + (1.15 - 1.0) = 1.30
coins_earned = floor(6,000 × 1.60 × 1.30) = floor(12,480) = 12,480 coins
```

---

## 4. Coin Spending

### 4.1 Pet Upgrades (PetUpgradesScreen)

File: `app/src/main/java/com/example/prtracker/data/PetUpgrade.kt`

Each upgrade has a cost formula and a level. Players purchase upgrade levels with coins, and can buy multiple levels at once via the quantity selector.

#### Cost Formulas

```kotlin
open fun costForLevel(currentLevel: Int): Long {
    if (fixedCosts != null) {
        return if (currentLevel < fixedCosts.size) fixedCosts[currentLevel] else Long.MAX_VALUE
    }
    return (baseCost * 1.25.pow(currentLevel)).toLong().coerceAtMost(100_000_000L)
}
// ROLL_SPEED override:
override fun costForLevel(currentLevel: Int): Long =
    (baseCost * 1.8.pow(currentLevel)).toLong().coerceAtMost(100_000_000L)
```

**Standard upgrades** use a recursive 25% increase per level: `cost = floor(baseCost × 1.25^level)`. **ROLL_SPEED** uses 80% per level: `cost = floor(300 × 1.8^level)`. All formula-based costs are **capped at 100M coins** — once a level reaches 100M, every subsequent level costs exactly 100M.

| Level | LUCK (500×1.25^n) | COIN_MULTIPLIER (600×1.25^n) | ROLL_SPEED (300×1.8^n) | LUCKY_ROLL (1000×1.25^n) |
|-------|-------------------|-----------------------------|----------------------|-------------------------|
| 0     | 500               | 600                         | 300                  | 1,000                   |
| 1     | 625               | 750                         | 540                  | 1,250                   |
| 2     | 781               | 938                         | 972                  | 1,562                   |
| 5     | 1,221             | 1,465                       | 3,150                | 2,441                   |
| 10    | 4,657             | 5,588                       | 59,520               | 9,313                   |
| 20    | 43,370            | 52,044                      | 21,245,100           | 86,740                  |
| 50    | 35,032,500        | 42,039,000                  | — (max 23)           | 70,065,000              |
| 58    | 100,000,000 (cap) | —                           | —                    | —                       |
| 100   | 100,000,000 (cap) | 100,000,000 (cap)           | —                    | 100,000,000 (cap)       |

**EQUIP_SLOTS** uses fixed costs (no exponential formula) — `fixedCosts = listOf(1_000_000, 10_000_000, 100_000_000)`:

| Level | Cost |
|-------|------|
| 0 → 1 | 1,000,000 |
| 1 → 2 | 10,000,000 |
| 2 → 3 | 100,000,000 |
| 3+ | Long.MAX_VALUE (unpurchasable) |

#### Upgrade Catalog

| Upgrade | ID | Effect | Base Cost | Cost Formula | Max Level |
|---------|---|--------|-----------|-------------|-----------|
| **LUCK** | `"luck"` | +20% per level to all non-COMMON rarity chances | 500 | base × 1.25^level | unlimited |
| **COIN_MULTIPLIER** | `"coin_multiplier"` | +0.20× coins per roll per level | 600 | base × 1.25^level | unlimited |
| **ROLL_SPEED** | `"roll_speed"` | -72ms dice animation per level (reaches 0ms at level 23) | 300 | base × 1.8^level | 23 |
| **LUCKY_ROLL** | `"lucky_roll"` | Every 5th roll is lucky (+0.25× per level rarity boost), higher levels upgrade pet tier | 1,000 | base × 1.25^level | unlimited |
| **EQUIP_SLOTS** | `"equip_slots"` | +1 equip slot per level (base 2, max 5) | 1,000,000 | fixed costs (1M/10M/100M) | 3 |

#### Upgrade Effect Details

**COIN_MULTIPLIER:**
- Each level adds +0.20× to the coin multiplier
- Level 0: 1.0× (no bonus)
- Level 1: 1.20× (+20%)
- Level 5: 2.0× (+100%)
- Level 10: 3.0× (+200%)

This multiplier applies multiplicatively with `pet.coinValue()` and `petXpMultiplier()` when earning coins from rolls.

**LUCK:**
- Each level adds +20% to ALL non-COMMON rarity chances (UNCOMMON, RARE, EPIC, LEGENDARY, MYTHICAL)
- Level 0: 1.0× (no bonus)
- Level 1: 1.20×
- Level 5: 2.0× (rare +100% more likely)

**ROLL_SPEED:**
- Each level reduces dice roll animation delay by 72ms
- Base delay: 1600ms
- Level 0: 1600ms
- Level 10: 880ms
- Level 20: 160ms
- Max level 23: 0ms (instant)
- Uses faster cost scaling: **1.8× per level** instead of 1.25×
- Total cost to max: ~279M coins

**LUCKY_ROLL:**
- When `luckyRollLevel > 0`, every 5th roll (`totalRolls % 5 == 0`) is a lucky roll
- Lucky rolls get a rarity boost of `+0.25× per level` applied to all non-COMMON chances
- Lucky roll tier bonus (determines the tier of the rolled pet):
  - Level 0: no lucky roll
  - Level 1+: SILVER
  - Level 51+: GOLDEN
  - Level 101+: RAINBOW
  - Level 151+: DARK_MATTER
  - Level 201+: RED_MATTER

**EQUIP_SLOTS:**
- Base equip slots: 2
- Each purchased level adds +1 slot
- Level 0: 2 slots
- Level 1: 3 slots
- Level 2: 4 slots
- Level 3: 5 slots (max)

### 4.2 Special Dice Shop (DiceShopScreen)

Players can buy consumable dice that filter rarity ranges for a limited number of rolls.

| Dice Type | Price | Rolls | Effect |
|-----------|-------|-------|--------|
| BANISHING DICE | 100,000 | 10 | Removes COMMON (weighted UNCOMMON+); 1/1000 DIVINE override |
| REFINING DICE | 500,000 | 5 | Weighted: RARE 61.7%, EPIC 25%, LEGENDARY 11.1%, MYTHICAL 2.2%; 1/100 DIVINE override |
| ASCENDANT DICE | 1,000,000 | 5 | Weighted: EPIC 63.9%, LEGENDARY 25%, MYTHICAL 11.1%; 1/20 DIVINE override |
| LEGENDARY DICE | 10,000,000 | 5 | Weighted: LEGENDARY 75%, MYTHICAL 25%; 1/8 DIVINE override |
| MYTHIC DICE | 25,000,000 | 5 | MYTHICAL only; 1/9 DIVINE override; 1/100k EXCLUSIVE override |
| SUPER DICE | 1,000,000,000,000 | 1 | Guarantees SUPER rarity pet (flat 50B coin award); 1/200k SECRET; 1/1k EXCLUSIVE |
| BURST_100 | 100,000,000 | 100 | Instant 100 rolls — no coins rewarded |
| BURST_1000 | 1,000,000,000 | 1,000 | Instant 1,000 rolls — no coins rewarded |
| BURST_10000 | 10,000,000,000 | 10,000 | Instant 10,000 rolls — no coins rewarded |
| BURST_100000 | 100,000,000,000 | 100,000 | Instant 100,000 rolls — no coins rewarded |

Purchasing: `buyDice(typeId, count)` deducts `price × count` from coins.

### 4.3 Export / Data Transfer

Coins are included in:
- **Pet export** (`generatePetExportJson()`) — includes coins in the `PetStorageData` JSON
- **Full data export** (when BOTH is selected)
- **Sync MERGE mode**: coins use `maxOf(local, incoming)` — the higher coin count is kept
- **Sync REPLACE mode**: coins are fully overwritten by the incoming data

---

## 5. Persistence

### Storage Fields

`PetStorageData` (stored in `pets.json`):
```kotlin
val coins: Long = 0L
val petUpgrades: Map<String, Int> = emptyMap()
val petInventory: List<Pet> = emptyList()
val equippedPetIds: List<String> = emptyList()
val diceInventory: List<SpecialDice> = emptyList()
val activeDiceEffects: List<ActiveDiceEffect> = emptyList()
val miniGameSettings: MiniGameSettings = MiniGameSettings()  // autoSellRarities, freezeRarities, selectedRollCount
val speciesTierCounts: Map<String, Int> = emptyMap()
val totalRolls: Int = 0
val rollsSinceEpicOrAbove: Int = 0
val rollsSinceLegendary: Int = 0
val rollsSinceMythical: Int = 0
val rollsSinceDivine: Int = 0
```

`StorageData` (stored in `prs.json`) also contained these fields before the split, but after migration they are cleaned to defaults. The app always reads pet data from `pets.json` via `StorageManager.loadPetData()`.

### Save Triggers

Pet data is saved (`savePetData()`) on every:
- Dice roll (`rollDice()`, `rollDiceMultiple()`, `burstRoll()`)
- Pet sell (`sellPet()`, `sellAllUnfavorited()`, `sellPets()`)
- Pet fuse (`fusePet()`, `fuseAllPets()`, `fusePremiumPets()`)
- Upgrade purchase (`purchaseUpgrade()`, `purchaseUpgradeMultiple()`)
- Dice buy (`buyDice()`)
- Dice use (`useDiceByType()`)
- Dice remove (`removeActiveDiceEffect()`)
- Equip/unequip pet (`equipPet()`, `unequipPet()`)
- Pet favoriting (`toggleFavorite()`)
- Settings change (`setAutoSellRarity()`, `setFreezeRarity()`, `setSelectedRollCount()`)
- Clear pet data (`clearPetData()`)

### Wipe Pet Data

Three-step confirmation in SettingsScreen resets all pet-related state:
```kotlin
_coins.value = 0L
_petUpgrades.value = emptyMap()
_petInventory.value = emptyList()
_equippedPetIds.value = emptyList()
_diceInventory.value = emptyList()
_activeDiceEffects.value = emptyList()
_totalRolls.value = 0
_rollsSinceEpicOrAbove.value = 0
_rollsSinceLegendary.value = 0
_rollsSinceMythical.value = 0
_rollsSinceDivine.value = 0
_miniGameSettings.value = MiniGameSettings()
_speciesTierCounts.value = emptyMap()
savePetData()
```

---

## 6. UI Display

### Coin Counter (DiceRollScreen)

- Shown in the top-right corner of the dice roll screen
- Uses an animated `Animatable` that animates from previous value to new value over 500ms
- Clicking the coin counter navigates to `PetUpgradesScreen`
- Format: compact K/M/B/T/Q short format via `formatCoins()` (e.g., "1.5K", "100K", "2.4M", "1.0B", "1.5T", "1.0Q")

### PetDetailView (Sell Confirmation)

When selling a pet, the confirmation dialog shows:
```
"You will receive X coins."
```
Where X = `pet.coinValue()` (formatted with thousand separators).

### Export Dialog (SettingsScreen)

PET export info shows:
```
"X pets · Y coins · Z rolls · W upgrades"
```

### Import Success (SyncImportScreen)

After importing pet data, shows:
```
"X pets · Y coins · Z rolls · W upgrades"
```

---

## 7. Merge / Sync Behavior

When merging pet data from a backup (both `mergePetData` and `mergePetDataFromStorage`):

```kotlin
_coins.value = maxOf(_coins.value, data.coins)
```

- **Coins**: the higher value between local and incoming is kept
- **Pets**: deduplicated by ID; keeps the higher tier, then higher stars
- **Upgrades**: for each upgrade key, the higher level is kept
- **Roll counters**: `totalRolls`, `rollsSinceEpicOrAbove`, etc. are set from the incoming data (new source of truth)
- **Dice inventory**: merged by ID dedup
- **Active dice effects**: same type → higher rolls remaining; different types → added; then sorted by strength descending

---

## 8. Summary: Coin System Architecture

```
                    ┌────────────────────┐
                    │   pet.coinValue()  │
                    │ base × tier × stars │
                    │ (premium: no stars)│
                    └────────┬───────────┘
                             │
                    ┌────────▼───────────┐
                    │  coinMultiplierUpg │
                    │  1.0 + level × 0.20│
                    │  (non-premium only)│
                    └────────┬───────────┘
                             │
                    ┌────────▼───────────┐
                    │   petXpMultiplier  │
                    │  (additive stack)  │
                    │  (non-premium only)│
                    └────────┬───────────┘
                             │
                    ┌────────▼───────────┐
                    │   coins_earned =   │
                    │ floor(a × b × c)  │
                    └────────┬───────────┘
                             │
              ┌──────────────┼──────────────┐
              ▼              ▼              ▼
        ┌─────────┐   ┌──────────┐   ┌──────────┐
        │ Sell    │   │ Upgrade  │   │ Special  │
        │ Pets    │   │ Shop     │   │ Dice     │
        │ (earn)  │   │ (spend)  │   │ (spend)  │
        └─────────┘   └──────────┘   └──────────┘
```

### Key Files

| File | Purpose |
|------|---------|
| `data/Pet.kt` | `Pet` data class, `PetRarity` (10 tiers incl. EXCLUSIVE, SECRET), `PetSpecies` (25 species with xpMult), `Pet.coinValue()`, `Pet.xpMultiplier()`, `RollResult`, `MiniGameSettings` |
| `data/PetUpgrade.kt` | `PetUpgrade` enum, `costForLevel()`, `totalCostForLevels()`, `maxPurchaseableLevels()`, cost formulas |
| `data/SpecialDice.kt` | `SpecialDiceType`, dice prices, weighted distributions, rarity filters, and DIVINE trigger chances |
| `data/StorageManager.kt` | `PetStorageData` (13 fields), save/load pet data (including coins) |
| `viewmodel/PRViewModel.kt` | `rollDice()`, `rollDiceMultiple()`, `sellPet()`, `sellAllUnfavorited()`, `purchaseUpgrade()`, `purchaseUpgradeMultiple()`, `buyDice()`, `useDiceByType()`, `removeActiveDiceEffect()`, `fusePet()`, `fuseAllPets()`, `fusePremiumPets()`, `petXpMultiplier()`, `coinMultiplier()`, `setAutoSellRarity()`, `setFreezeRarity()`, `setSelectedRollCount()` |
| `ui/screens/DiceRollScreen.kt` | Dice UI, coin counter with animation, pet reveal (with displayOneInX override), premium freeze, auto-roll |
| `ui/screens/DiceShopScreen.kt` | Special dice shop UI |
| `ui/screens/DiceInventoryScreen.kt` | Owned dice display, USE dialog |
| `ui/screens/PetUpgradesScreen.kt` | Upgrade shop UI with quantity selector, purchase buttons |
| `ui/screens/PetInventoryScreen.kt` | Sell pet UI, premium fuse UI, confirmations |
| `ui/screens/PetIndexScreen.kt` | Pet index (pokédex), species grid, star management, premium formulas |
| `ui/screens/MiniGameSettingsScreen.kt` | Auto-sell settings, freeze screen settings, roll count selector |
| `ui/screens/SettingsScreen.kt` | Export dialog, wipe pet data |
