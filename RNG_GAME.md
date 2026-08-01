# Pet Dice Roll — Complete Game Design Document

## Overview

The Pet Dice Roll is a minigame within PRTracker where players spend rolls to receive random pets with varying rarities and tiers. Pets can be upgraded via fusion, sold for coins, and enhanced through a persistent upgrade system.

---

## 1. Core Loop

1. Player taps the dice → roll is consumed
2. Random rarity is selected via weighted probabilities
3. Random species of that rarity is assigned
4. If a pet of the same species+tier exists with <5 stars → star upgrade; otherwise → new pet
5. Coins are awarded based on pet value × coin multiplier upgrade
6. Repeat

---

## 2. Pet Rarities

| Rarity     | Drop Chance | Base Coins      | Base XP Mult | Color Hex |
|------------|-------------|-----------------|--------------|-----------|
| COMMON     | 54.4%       | 100             | 1.00x        | 0xFFAAAAAA |
| UNCOMMON   | 28.0%       | 250             | 1.05x        | 0xFF44FF44 |
| RARE       | 13.0%       | 600             | 1.10x        | 0xFF4488FF |
| EPIC       | 4.0%        | 1,500           | 1.20x        | 0xFFAA44FF |
| LEGENDARY  | 0.5%        | 5,000           | 1.35x        | 0xFFFFD700 |
| MYTHICAL   | 0.1%        | 15,000          | 1.50x        | 0xFFFF4444 |
| SUPER      | 0.001% (1/100k) | 100,000,000,000 | 1.00x (dynamic) | 0xFF001B3D |

---

## 3. Pet Tiers (Evolution)

| Tier        | Order | Coin Multiplier | XP Multiplier | Color Hex |
|-------------|-------|-----------------|---------------|-----------|
| NORMAL      | 0     | 1x              | 1.00x         | 0xFFAAAAAA |
| SILVER      | 1     | 2x              | 1.10x         | 0xFFC0C0C0 |
| GOLDEN      | 2     | 4x              | 1.25x         | 0xFFFFD700 |
| RAINBOW     | 3     | 8x              | 1.45x         | 0xFFFF44FF |
| DARK_MATTER | 4     | 16x             | 1.70x         | 0xFF6A0DAD |
| RED_MATTER  | 5     | 32x             | 2.00x         | 0xFFDC143C |

---

## 4. Coin Value Formula

```
coinValue = PetRarity.baseCoins × PetTier.coinMultiplier × stars
```

**Examples:**
- COMMON NORMAL 1★ = 100 × 1 × 1 = 100 coins
- LEGENDARY GOLDEN 5★ = 5,000 × 4 × 5 = 100,000 coins
- MYTHICAL RED_MATTER 5★ = 15,000 × 32 × 5 = 2,400,000 coins
- SUPER NORMAL = 100,000,000,000 × 1 = 100B coins (sell value only; roll awards flat 50B, no multipliers)

---

## 4.5. XP Multiplier Formula

**Standard (non-SUPER) formula:**
```
xpMultiplier = PetRarity.baseXpMult × PetTier.xpMult × (1 + (stars - 1) × 0.05)
```

**SUPER formula (dynamic, depends on best non-SUPER pet):**
```
xpMultiplier = 1.1 × tier.xpMult × best_non_super_mult
```

Where `best_non_super_mult` is the highest `xpMultiplier()` among non-SUPER pets in the inventory (falls back to 1.0x if none exist).

**Examples:**
- COMMON NORMAL 1★ = 1.00 × 1.00 × 1.00 = 1.00x
- LEGENDARY GOLDEN 5★ = 1.35 × 1.25 × 1.20 = 2.025x
- MYTHICAL RED_MATTER 5★ = 1.50 × 2.00 × 1.20 = 3.60x
- SUPER NORMAL (with MYTHICAL RED_MATTER in inventory) = 1.1 × 1.0 × 3.60 = 3.96x

### Combined Equipped Multiplier

When multiple pets are equipped, their multipliers stack additively:

```
totalXpMult = 1.0 + Σ(pet.xpMultiplier - 1.0) for each equipped pet
```

**Examples:**
- 1x MYTHICAL RED_MATTER 5★: 1.0 + (3.60 - 1.0) = 3.60x
- 2x MYTHICAL RED_MATTER 5★: 1.0 + (3.60 - 1.0) × 2 = 6.20x

XP multiplier is applied at log time and baked into `PREntry.xpEarned`.

---

Coins are awarded on every roll, sell, and fuse operation.

### 5a. Roll Award

**Normal pets (non-SUPER):**
```
coinsAwarded = floor(pet.coinValue() × upgradeMult × petCoinMult)
```

Where:
- `upgradeMult = 1.0 + coinMultiplierLevel × 0.20` (from COIN_MULTIPLIER upgrade)
- `petCoinMult = 1.0 + Σ(pet.xpMultiplier - 1.0)` for each equipped pet (same as Combined Equipped XP formula)

**SUPER pets (both natural 1/100k and SUPER DICE):**
```
coinsAwarded = 50,000,000,000  (flat 50B, no multipliers)
```

### 5b. Sell Award

**Non-SUPER pets:**
```
coinsAwarded = floor(pet.coinValue() × upgradeMult)
```

Where:
- `upgradeMult = 1.0 + coinMultiplierLevel × 0.20` (from COIN_MULTIPLIER upgrade)

**SUPER pets:**
```
coinsAwarded = pet.coinValue()  (100B flat, no multipliers)
```

### 5c. Sell All (Unfavorited)

```
totalCoins = sum(pet.sellValue() for each unfavorited pet)
```

Favorited pets are excluded. Each pet's sell value uses the coin multiplier for non-SUPER, flat 100B for SUPER.

---

## 6. Rarity Selection Algorithm

### 6a. Base Chances

Start with the base drop chances from `PetRarity`:

```
COMMON:    0.544
UNCOMMON:  0.280
RARE:      0.130
EPIC:      0.040
LEGENDARY: 0.005
MYTHICAL:  0.001
SUPER:     0.000 (handled independently — 1/100k flat pre-check)
```

SUPER rarity is NOT part of the weighted selection. It uses an independent `Math.random() < 0.00001` pre-check at the top of `rollDice()`, before any dice/luck/pity logic. SUPER DICE overrides this to always trigger.

### 6b. Luck Upgrade Multiplier

If luck level > 0, multiply ALL non-COMMON chances:

```
luckMultiplier = 1.0 + luckLevel × 0.20

COMMON:    0.544                          (unchanged)
UNCOMMON:  0.280 × luckMultiplier
RARE:      0.130 × luckMultiplier
EPIC:      0.040 × luckMultiplier
LEGENDARY: 0.005 × luckMultiplier
MYTHICAL:  0.001 × luckMultiplier
SUPER:     0.000 (never affected by luck)
```

**Luck levels and their multipliers:**

| Level | Multiplier | UNCOMMON | RARE   | EPIC   | LEGENDARY | MYTHICAL |
|-------|------------|----------|--------|--------|-----------|----------|
| 0     | 1.0x       | 28.0%    | 13.0%  | 4.00%  | 0.50%     | 0.10%    |
| 1     | 1.2x       | 33.6%    | 15.6%  | 4.80%  | 0.60%     | 0.12%    |
| 5     | 2.0x       | 56.0%    | 26.0%  | 8.00%  | 1.00%     | 0.20%    |
| 10    | 3.0x       | 84.0%    | 39.0%  | 12.0%  | 1.50%     | 0.30%    |

### 6c. Soft Pity (Epic+)

After 150 rolls without Epic or higher:

```
bonus = (rollsSinceEpicOrAbove - 150) × 0.01 × luckMultiplier
EPIC chance += bonus
```

**Example:** At roll 170 with luck level 0:
```
bonus = (170 - 150) × 0.01 × 1.0 = 0.20
EPIC chance = 0.040 + 0.20 = 0.240 (24%)
```

### 6d. Hard Pity (Legendary)

At exactly 401 rolls without Legendary:
```
LEGENDARY chance = 1.0 (100%)
```

### 6e. Hard Pity (Mythical)

At exactly 2001 rolls without Mythical:
```
MYTHICAL chance = 1.0 (100%)
```

### 6f. Pity Reset

Pity counters reset independently:
- `rollsSinceEpicOrAbove` resets on EPIC, LEGENDARY, or MYTHICAL
- `rollsSinceLegendary` resets only on LEGENDARY
- `rollsSinceMythical` resets only on MYTHICAL

### 6g. Active Dice Effect — Custom Weighted Distributions

When a special dice effect is active (REFINING, ASCENDANT, or LEGENDARY), the base chances are **replaced entirely** by a custom distribution instead of being filtered by `minRarity`/`maxRarity`:

**REFINING dice:**
| Rarity | Chance |
|--------|--------|
| RARE | 61.7% |
| EPIC | 25.0% |
| LEGENDARY | 11.1% (1/9) |
| MYTHICAL | 2.2% (1/46) |

**ASCENDANT dice:**
| Rarity | Chance |
|--------|--------|
| EPIC | 63.9% |
| LEGENDARY | 25.0% (1/4) |
| MYTHICAL | 11.1% (1/9) |

**LEGENDARY dice:**
| Rarity | Chance |
|--------|--------|
| LEGENDARY | 75.0% |
| MYTHICAL | 25.0% (1/4) |

These custom distributions are set via `SpecialDiceType.baseChances`. When active:
- The luck upgrade multiplier applies on top (multiplies all non-COMMON chances)
- **Soft pity is skipped** — the custom distribution already provides generous Epic+ rates
- Hard pity still overrides (401 → Legendary, 2001 → Mythical)
- Lucky roll boost still applies

BANISHING and MYTHIC dice continue using the old `minRarity`/`maxRarity` filter behavior (no custom distribution).

### 6h. Burst Dice

Burst dice are a special category (`DiceCategory.BURST`) that execute rolls instantly in a batch with no coin rewards. They activate from inventory via `useDiceByType()`, creating an `ActiveDiceEffect(category=BURST)`. When the user taps the roll button, `rollDiceMultiple()` detects the BURST effect and calls `burstRoll(count)`.

```
Burst dice:
- No coins awarded from rolls or auto-sell
- All standard rarity odds apply (no min/max filter)
- Lucky Roll, LUCK upgrade, pity, and active LUCK/TIER dice all apply
- Best pet tracked by rarity → tier → stars
- Returns single RollResult with best pet
```

**Burst dice types:**

| Dice Type | Price | Rolls | Effect |
|-----------|-------|-------|--------|
| BURST_100 | 100,000,000 | 100 | Instant 100 rolls — no coins |
| BURST_1000 | 1,000,000,000 | 1,000 | Instant 1,000 rolls — no coins |
| BURST_10000 | 10,000,000,000 | 10,000 | Instant 10,000 rolls — no coins |
| BURST_100000 | 100,000,000,000 | 100,000 | Instant 100,000 rolls — no coins |

**Burst queue:** Multiple burst dice stack in the BURST queue (fire in sequence). The BURST category is independent from LUCK and TIER — all three can be active simultaneously.

**100k batch cap:** `rollDiceMultiple()` caps burst rolls at 100,000 per tap. If more rolls remain after a batch, the BURST effect's `rollsRemaining` is decremented by the batch size instead of being removed, so subsequent taps continue where the last batch left off. Only removes the effect when `rollsRemaining` hits 0.

**Proportional LUCK/TIER consumption:** `burstRoll()` uses local counters (`luckRollsRemaining`, `tierRollsRemaining`) to track LUCK and TIER dice consumption proportionally. Effects stop mid-burst when their counter hits 0. The write-back subtracts the actual consumed amount from each effect's `rollsRemaining`.

### 6i. SUPER DICE

A new dice type that **guarantees a SUPER rarity pet** on each roll. It is NOT part of the weighted selection — instead it sets `isSuperDiceActive = true` in `rollDice()`, which forces the SUPER pre-check (normally `Math.random() < 0.00001`) to always trigger.

```
SUPER DICE: 1,000,000,000,000 coins → 1 roll → guaranteed SUPER pet
```

When SUPER DICE is active:
- The SUPER pre-check always fires (no random check)
- Pet tier follows the same lucky-roll tier logic (NORMAL or lucky-roll tier)
- Coin award: flat 50B (no multipliers)
- Active dice effect is consumed (decremented) normally
- Pity counters reset (same as natural SUPER)

---

## 7. Lucky Roll System

### 7a. Trigger

Every 5th roll when LUCKY_ROLL upgrade level > 0:

```
isLuckyRoll = (luckyRollLevel > 0) AND (totalRolls % 5 == 0)
```

### 7b. Rarity Boost

During a lucky roll, all non-COMMON chances are boosted:

```
luckyRollRarityBoost = 1.0 + luckyRollLevel × 0.25

boostedChance[rarity] = chance[rarity] × luckyRollRarityBoost   (for rarity ≠ COMMON)
boostedChance[COMMON] = chance[COMMON]                           (unchanged)
```

**Lucky Roll levels and their boosts:**

| Level | Boost  | Description |
|-------|--------|-------------|
| 0     | N/A    | No lucky rolls |
| 1     | 1.25x  | +25% to all non-COMMON |
| 5     | 2.25x  | +125% to all non-COMMON |
| 10    | 3.50x  | +250% to all non-COMMON |

### 7c. Selection During Lucky Roll

The boosted chances are normalized and used for rarity selection (same weighted random algorithm as normal rolls, but with boosted weights).

### 7d. Lucky Countdown Display

```
displayValue = rollsUntilLucky - 1
```

- When `rollsUntilLucky > 1`: Shows "LUCKY IN {displayValue}"
- When `rollsUntilLucky == 1`: Shows "LUCKY ROLL!" in gold

---

## 8. Roll Chance Display

The "1 in X CHANCE" displayed on pet reveal:

```
oneInX = floor(totalWeight / rarityChance)
oneInX = max(oneInX, 2)    // floors at 2, never shows "1 in 1"
```

Where `totalWeight` is the sum of all effective chances used for the roll, and `rarityChance` is the chance of the rolled rarity.

---

## 9. Star System

### 9a. Star Upgrades (Same Species + Tier)

When rolling a pet that matches an existing pet's `speciesId + tier`:

```
If existing pet has <5 stars:
    existing.stars += 1    (star upgrade, no new pet created)
If existing pet has 5 stars:
    Create new pet with 1 star (duplicate allowed)
```

### 9b. Star Display

- 1-2 stars: ★☆☆☆☆ (rarity color)
- 3-5 stars: ★★★★★ (gold color)

---

## 10. Fusion System

### 10a. Requirements

- Pet must be 5★ (5 stars)
- Pet must not be at max tier (RED_MATTER)

### 10b. Fusion Formula

```
Old pet (5★ NORMAL) → New pet (1★ SILVER)
Old pet (5★ SILVER) → New pet (1★ GOLDEN)
Old pet (5★ GOLDEN) → New pet (1★ RAINBOW)
Old pet (5★ RAINBOW) → New pet (1★ DARK_MATTER)
Old pet (5★ DARK_MATTER) → New pet (1★ RED_MATTER)
```

### 10c. Deduplication During Fusion

If a pet of the same species+tier already exists at the target tier with <5 stars:
- The existing pet gets +1 star (instead of creating a new pet)
- The fused pet is consumed

### 10d. FUSE ALL

Fuses all 5★ pets that are not at max tier. Returns count of fused pets.

---

## 11. Upgrade System

### 11a. Upgrade Definitions

| Upgrade          | ID               | Base Cost | Fixed Costs          | Max Level | Effect Per Level                |
|------------------|------------------|-----------|----------------------|-----------|---------------------------------|
| LUCK             | `luck`           | 500       | —                    | ∞         | +20% to all non-COMMON chances  |
| COIN_MULTIPLIER  | `coin_multiplier`| 600       | —                    | ∞         | +0.20x to all coin earnings     |
| ROLL_SPEED       | `roll_speed`     | 300       | —                    | 23        | -72ms dice animation delay      |
| LUCKY_ROLL       | `lucky_roll`     | 1,000     | —                    | ∞         | +0.25x lucky roll rarity boost  |
| EQUIP_SLOTS      | `equip_slots`    | 0         | [1M, 10M, 100M]      | 3         | +1 equip slot per level         |
| MULTI_ROLL       | `multi_roll`     | 0         | [1M, 100M, 10B]      | 3         | Roll multiple dice at once      |

### 11b. Cost Formula

All infinite upgrades use a **recursive formula** — each level costs 25% more than the previous. ROLL_SPEED uses a separate **×1.8** multiplier (80% more per level). All formula-based costs are **capped at 100M coins**:

```
// Standard upgrades (LUCK, COIN_MULTIPLIER, LUCKY_ROLL)
costForLevel(0) = baseCost
costForLevel(n) = min(costForLevel(n-1) × 1.25, 100_000_000)

// ROLL_SPEED (faster scaling)
costForLevel(0) = 300
costForLevel(n) = min(costForLevel(n-1) × 1.8, 100_000_000)
```

Equivalently:
- **Standard**: `costForLevel(n) = min(floor(baseCost × 1.25^n), 100_000_000)`
- **ROLL_SPEED**: `costForLevel(n) = min(floor(300 × 1.8^n), 100_000_000)`

For EQUIP_SLOTS and MULTI_ROLL, costs are fixed per level.

**MULTI_ROLL fixed costs:**

| Level | Cost        | Dice Count |
|-------|-------------|------------|
| 1     | 1,000,000   | 2x         |
| 2     | 100,000,000 | 3x         |
| 3     | 10,000,000,000 | 5x      |

### 11c. Cost Scaling Examples

**Standard upgrades (×1.25): LUCK, COIN_MULTIPLIER, LUCKY_ROLL**

**LUCK (base 500):**

| Level | Cost        | Cumulative |
|-------|-------------|------------|
| 1     | 500         | 500        |
| 5     | 1,221       | 4,104      |
| 10    | 4,657       | 16,625     |
| 20    | 43,370      | 171,500    |
| 50    | 35,032,500  | 140,128,000 |
| 58    | 100,000,000 (cap) | —    |
| 100   | 100,000,000 (cap) | 4,811,000,000 |

**COIN_MULTIPLIER (base 600):**

| Level | Cost        | Cumulative |
|-------|-------------|------------|
| 1     | 600         | 600        |
| 5     | 1,465       | 4,925      |
| 10    | 5,588       | 19,950     |
| 20    | 52,044      | 205,800    |
| 50    | 42,039,000  | 168,154,000 |
| 54    | 100,000,000 (cap) | —    |
| 100   | 100,000,000 (cap) | 4,811,000,000 |

**LUCKY_ROLL (base 1,000):**

| Level | Cost        | Cumulative |
|-------|-------------|------------|
| 1     | 1,000       | 1,000      |
| 5     | 2,441       | 8,208      |
| 10    | 9,313       | 33,250     |
| 20    | 86,740      | 343,000    |
| 50    | 70,065,000  | 280,256,000 |
| 52    | 100,000,000 (cap) | —    |
| 100   | 100,000,000 (cap) | 4,811,000,000 |

**ROLL_SPEED (×1.8, base 300, max level 23):**

| Level | Cost      | Cumulative |
|-------|-----------|------------|
| 1     | 300       | 300        |
| 5     | 3,150     | 5,370      |
| 10    | 59,520    | 137,010    |
| 15    | 1,124,400 | 2,620,600  |
| 20    | 21,245,100 | 49,654,900 |
| 23    | 22,233,600 | 52,710,200 |

**EQUIP_SLOTS (fixed costs):**

| Level | Cost    | Cumulative | Slots |
|-------|---------|------------|-------|
| 1     | 1,000,000 | 1,000,000 | 3     |
| 2     | 10,000,000 | 11,000,000 | 4   |
| 3     | 100,000,000 | 111,000,000 | 5  |

**Total cost: Standard upgrades have unlimited levels but never exceed 100M per level. ROLL_SPEED caps at ~52.7M total by L23. EQUIP_SLOTS costs 111M to max all 5 slots.**

### 11d. Level Limits

| Upgrade          | Max Level | Notes                     |
|------------------|-----------|---------------------------|
| LUCK             | ∞         | 1.25× per level           |
| COIN_MULTIPLIER  | ∞         | 1.25× per level           |
| ROLL_SPEED       | 23        | 1.8× per level; 0ms at 23 |
| LUCKY_ROLL       | ∞         | 1.25× per level           |
| EQUIP_SLOTS      | 3         | Fixed costs               |
| MULTI_ROLL       | 3         | Fixed costs: 2x/3x/5x    |

### 11e. Roll Speed Effect

```
diceAnimationDelay = max(0ms, 1600ms - rollSpeedLevel × 72ms)
```

| Level | Delay   |
|-------|---------|
| 0     | 1600ms  |
| 5     | 1240ms  |
| 10    | 880ms   |
| 15    | 520ms   |
| 20    | 160ms   |
| 22    | 16ms    |
| 23    | 0ms (max) |

### 11f. Pet Coin Multiplier

Equipped pets that boost XP also boost coin earnings from dice rolls:

```
coinsAwarded = floor(pet.coinValue × upgradeMult × petCoinMult)
```

Where:
- `upgradeMult = 1.0 + coinMultiplierLevel × 0.20` (from COIN_MULTIPLIER upgrade)
- `petCoinMult = 1.0 + Σ(pet.xpMultiplier - 1.0)` for each equipped pet (same formula as XP)

This means the same equipped pets that increase XP gain also accelerate coin income. A full set of max-tier pets can give up to ~3.76× coin multiplier, significantly speeding up progression as costs balloon exponentially at high levels.

### 11g. Multi-Level Purchase

Players can buy multiple upgrade levels at once via the quantity selector in the Pet Upgrades screen:

- [-] / [+] buttons adjust quantity, or tap the number to type a specific value
- Total cost preview shows the combined cost for the selected quantity
- "BUY N" button displays total cost and is disabled when coins are insufficient

The quantity is **persisted** after each purchase (does not reset to 1). Maximum quantity is limited by both the upgrade's level cap and available coins. `purchaseUpgradeMultiple(upgrade, count)` handles the batch purchase, deducting coins per level and returning the actual count purchased if coins run out mid-purchase.

---

## 12. Pet Species (Placeholder)

### COMMON (54.4%)
| ID      | Name      | Emoji |
|---------|-----------|-------|
| bot_01  | Scrap Bot | 🤖    |
| slime_01| Gel Slime | 🟢    |
| cat_01  | Alley Cat | 🐱    |
| mouse_01| Wire Mouse| 🐭    |
| blob_01 | Data Blob | 🔵    |

### UNCOMMON (28%)
| ID      | Name      | Emoji |
|---------|-----------|-------|
| fox_01  | Neon Fox  | 🦊    |
| owl_01  | Circuit Owl| 🦉   |
| frog_01 | Pixel Frog| 🐸    |
| bee_01  | Buzz Drone| 🐝    |

### RARE (13%)
| ID      | Name        | Emoji |
|---------|-------------|-------|
| wolf_01 | Cyber Wolf  | 🐺    |
| dragon_01| Hatchling  | 🐉    |
| phoenix_01| Spark Phoenix| 🧙  |

### EPIC (4%)
| ID        | Name          | Emoji |
|-----------|---------------|-------|
| titan_01  | Void Titan    | 🔮    |
| unicorn_01| Prism Unicorn | 🦄    |

### LEGENDARY (0.5%)
| ID          | Name            | Emoji |
|-------------|-----------------|-------|
| leviathan_01| Storm Leviathan | 🐎    |
| golem_01    | Obsidian Golem  | 🧊    |

### MYTHICAL (0.1%)
| ID        | Name          | Emoji |
|-----------|---------------|-------|
| void_01   | Void Walker   | ⭐    |
| cosmos_01  | Cosmos Drake  | 🌌    |

### SUPER (1/100k — handled independently)
| ID             | Name  | Emoji |
|----------------|-------|-------|
| super_dog_01   | Rex   | 🐕    |
| super_dog_02   | Buddy | 🐶    |

SUPER species are dog-themed. Only 2 species exist. Each SUPER roll creates an independent pet instance — no stacking or star upgrades across same species.

---

## 13. Multi-Roll System

### 13a. Mechanics

The MULTI_ROLL upgrade allows rolling multiple dice at once. Each roll in a batch is independent — same probabilities, same pity system, same SUPER pre-check.

```
multiRollCount = 1 + getUpgradeLevel(PetUpgrade.MULTI_ROLL)
```

| Level | Dice Per Roll |
|-------|---------------|
| 0     | 1 (default)   |
| 1     | 2             |
| 2     | 3             |
| 3     | 5             |

### 13b. Roll Count Selector (MiniGameSettings)

Players can choose to roll fewer dice than the maximum via the MiniGameSettings screen (gear icon on DiceRollScreen):

```
effectiveRollCount = min(selectedRollCount, maxUnlocked)
```

- `selectedRollCount = 0` → use max unlocked (default)
- `selectedRollCount > 0` → capped by MULTI_ROLL level

Available options: 1x, 2x, 3x, 5x (locked behind MULTI_ROLL levels).

### 13c. Multi-Reveal Display

When multiple pets are rolled:
- **Single roll (1 pet):** Standard `RevealView` with pet emoji, rarity badge, name, stars, "1 in X CHANCE"
- **Multi-roll (2+ pets):** `MultiRevealView` grid showing all pets:
  - Per cell: emoji in rarity-bordered box + pet name in rarity color + single tier pill
  - Not tappable — display only
  - All rows centered horizontally
  - Bottom dice remains visible and tappable to roll again

### 13d. Multi-Roll Pet Detail

During multi-roll, tapping the dice again triggers another batch. To view a specific pet's detail, the user can tap a pet card in the inventory after the roll.

---

## 14. Auto-Sell System

### 14a. Mechanic

Players can configure automatic selling of pets by rarity via the MiniGameSettings screen. When auto-sell is enabled for a rarity, pets of that rarity are sold immediately during `rollDice()` — they never enter the inventory.

```
if (pet.rarity in autoSellRarities AND pet.rarity != "SUPER"):
    coins += pet.sellValue()    // coin multiplier applied for non-SUPER
    pet is NOT added to inventory
```

### 14b. Rules

- **SECRET is excluded** from auto-sell options (always kept). All other rarities including EXCLUSIVE and SUPER are auto-sellable.
- Auto-sold pets are **still displayed in the roll reveal** — `RollResult.pet` is a snapshot taken before auto-sell
- Auto-sold pets still count toward the **pet index** (speciesTierCounts is incremented before auto-sell)
- The coin award from auto-sell uses the same formula as manual sell: `floor(pet.coinValue() × upgradeMult)` for non-SUPER

### 14c. Configuration

- Per-rarity checkboxes in MiniGameSettingsScreen
- Checked rarities stored in `MiniGameSettings.autoSellRarities: Set<String>`
- Persisted to `pets.json`

---

## 15. Dice Quantity Stack

### 15a. Design

SpecialDice uses a `quantity: Int` field. Multiple dice of the same type bought from the shop merge into a single inventory entry with combined quantity.

```
// Buying 3 REFINING dice:
// Before: [] → After: [REFINING ×3]

// Using 1 REFINING dice:
// Before: [REFINING ×3] → After: [REFINING ×2]

// Using last REFINING dice:
// Before: [REFINING ×1] → After: [] (entry removed)
```

### 15b. Migration

On every app load, `collapseDiceInventory()` runs:
- Groups dice entries by typeId
- Sums quantity for each type
- Replaces the list with collapsed entries
- Reduces potential 230k entries to ~6 entries
- O(1) operations instead of O(n)

---

## 16. Pet Index (Pokédex)

### 16a. Tracking

A `speciesTierCounts: Map<String, Int>` field in `PetStorageData` tracks how many of each species+tier combination have ever been obtained.

```
key = "${speciesId}_${tier}"   // e.g. "dragon_01_GOLDEN"
value = count obtained
```

Counts include:
- Rolled pets (before auto-sell)
- Fused pets
- Auto-sold pets (counted before being sold)

Counts never decrement on sell — they are lifetime totals.

### 16b. Bootstrap

On first load for existing users, if `speciesTierCounts` is empty but `petInventory` is non-empty, the index is bootstrapped by scanning the current inventory.

### 16c. Index Screen

The PetIndexScreen shows:
- **Tier filter pills:** NORMAL, SILVER, GOLDEN, RAINBOW, DARK_MATTER, RED_MATTER
- **Species grid:** 4-column grid of all 25 PetCatalog species for the selected tier
  - Unlocked: pet emoji with rarity border, species name, "×N" count badge
  - Locked: "❓" placeholder, dimmed
- **Pet detail overlay:** Tapping an unlocked species opens a full-screen overlay:
  - Large emoji with rarity-colored border
  - Pet name and rarity badge
  - Total obtained count
  - Star management (non-SUPER only): interactive ★/☆ selector, sell button, equip/unequip button
  - SUPER pets: tier pills and formula display ("1.1 × TIER × BEST_NON_SUPER")

### 16d. Access

- 🖥️ button in DiceRollScreen button row navigates to PetIndexScreen
- Back button returns to DiceRollScreen

---

## 17. UI Layout

### DiceRollScreen Split Layout

```
┌─────────────────────────────┐
│  ←  "PET DICE"  ⚙️ (gear)  │
│  Stats | AUTO | Coins       │
│  LUCKY IN X / LUCKY ROLL!  │
│  Equipped: 🐱 🦊 🐺 +X% XP  │
│  📦 INVENTORY (count)       │
│  🖥️ Index  🎲 Dice          │
├─────────────────────────────┤
│                             │
│     Top Area (weight 1f)    │
│   REVEAL / PET_DETAIL       │
│   (scrollable)              │
│                             │
├─────────────────────────────┤
│   Bottom Area (140dp)       │
│   Dice (IDLE/ROLLING/REVEAL)│
│   Hidden during PET_DETAIL  │
└─────────────────────────────┘
```

### DiceView (Unified)

Single composable for IDLE and ROLLING states:
- 93dp gold `Casino` icon button
- "TAP TO ROLL" label (IDLE) / "ROLL (X LEFT)" label (multi-roll ROLLING)
- SUPER dice: dual-pulse glow circles + horizontal shake VFX

### State Transitions

```
IDLE ──tap dice──→ ROLLING ──delay──→ REVEAL ──tap dice──→ ROLLING (next batch)
                     ↑                                      │
                     └──────────────────────────────────────┘

REVEAL ──tap dice──→ ROLLING (new batch without dismissing)

REVEAL ──tap showcase──→ IDLE

IDLE ──tap showcase──→ PET_DETAIL ──tap dismiss──→ IDLE
```

### Multi-Reveal Layout (2+ pets)

```
┌─────────────────────────────┐
│     Top Area (weight 1f)    │
│  ┌─────┐ ┌─────┐ ┌─────┐  │
│  │emoji│ │emoji│ │emoji│  │  ← row 1 (centered)
│  │name │ │name │ │name │  │
│  │tier │ │tier │ │tier │  │
│  └─────┘ └─────┘ └─────┘  │
│  ┌─────┐ ┌─────┐          │
│  │emoji│ │emoji│          │  ← row 2 (centered)
│  │name │ │name │          │
│  │tier │ │tier │          │
│  └─────┘ └─────┘          │
├─────────────────────────────┤
│   Bottom Area (140dp)       │
│   Dice — tappable to roll   │
└─────────────────────────────┘
```

### PetInventoryScreen (Separate Route)

- Triggered by inventory button → navigates to `PET_INVENTORY` route
- Full-screen layout with back button
- Search bar (filters by name, rarity, tier)
- Sort controls: TYPE / RARITY / VALUE / XP dropdown + direction toggle
- 4-column `LazyVerticalGrid`
- Sorted: favorites first, then by selected sort mode
- Long-press for quick favorite toggle
- Tap opens inline `PetDetailView` (replaces grid, not a separate route)
- Equip/Unequip button in PetDetailView
- Green checkmark badge on equipped PetCollectionCards
- FUSE ALL and SELL ALL buttons at bottom

### MiniGameSettingsScreen (Separate Route)

- Triggered by gear icon (⚙️) on DiceRollScreen title bar
- Full-screen layout with back button
- **Auto-Sell section:** Per-rarity checkboxes (SECRET excluded), description "Sold on roll — still shown in showcase"
- **Roll Count section:** 1x/2x/3x/5x pill buttons locked by MULTI_ROLL upgrade level

### PetIndexScreen (Separate Route)

- Triggered by 🖥️ button on DiceRollScreen button row
- Full-screen layout with back button
- **Tier filter pills:** Horizontal LazyRow of tier buttons
- **Species grid:** 4-column grid of all 25 PetCatalog species
  - Unlocked: emoji + rarity border + name + ×N badge
  - Locked: ❓ placeholder, dimmed
- **Pet detail overlay:** Full-screen overlay with star management or SUPER formula display

---

## 18. Persistence

Pet data is stored in a separate `pets.json` file (independent from app data `prs.json`):

```kotlin
data class PetStorageData(
    val petInventory: List<Pet> = emptyList(),
    val totalRolls: Long = 0L,
    val rollsSinceEpicOrAbove: Long = 0L,
    val rollsSinceLegendary: Long = 0L,
    val rollsSinceMythical: Long = 0L,
    val lastDiceRollTimestamp: Long = 0L,
    val coins: Long = 0L,
    val petUpgrades: Map<String, Int> = emptyMap(),
    val equippedPetIds: List<String> = emptyList(),
    val diceInventory: List<SpecialDice> = emptyList(),
    val activeDiceEffects: List<ActiveDiceEffect> = emptyList(),
    val miniGameSettings: MiniGameSettings = MiniGameSettings(),
    val speciesTierCounts: Map<String, Int> = emptyMap()
)
```

Auto-migrated from old `prs.json` on first launch via `StorageManager.migrateIfNeeded()`. Pet-only operations (roll, fuse, sell, upgrade, equip, favorite) write only `pets.json` for performance.

### MERGE Import Behavior

- Pets deduplicated by ID
- Higher tier wins; if same tier, higher stars wins
- Coins: `maxOf(local, incoming)`
- Upgrades: per-ID merge (higher level wins)
- Dice inventory: collapsed by type after merge
- Species tier counts: summed per key
- MiniGame settings: incoming replaces local

---

## 19. Summary of All Formulas

| Formula | Expression |
|---------|------------|
| Coin Value | `baseCoins × tierMultiplier × stars` (SUPER: skips star multiplier) |
| Pet XP Multiplier (standard) | `baseXpMult × tierXpMult × (1 + (stars-1) × 0.05)` |
| Pet XP Multiplier (SUPER) | `1.1 × tier.xpMult × best_non_super_mult` |
| Combined Equipped XP | `1.0 + Σ(petMultiplier - 1.0)` |
| Roll Coin Award | `floor(coinValue × (1.0 + coinMultiplierLevel × 0.20) × petCoinMult)` |
| SUPER Coin Award | `50,000,000,000` (flat 50B, no multipliers) |
| Sell Coin Award (non-SUPER) | `floor(coinValue × (1.0 + coinMultiplierLevel × 0.20))` |
| Sell Coin Award (SUPER) | `100,000,000,000` (flat 100B) |
| Luck Multiplier | `1.0 + luckLevel × 0.20` |
| Soft Pity Bonus | `(rollsSinceEpicOrAbove - 150) × 0.01 × luckMultiplier` |
| Soft Pity (custom dice) | Skipped for dice with `baseChances` (REFINING/ASCENDANT/LEGENDARY) |
| Hard Pity (Legendary) | `rollsSinceLegendary >= 401 → 100%` (overrides dice) |
| Hard Pity (Mythical) | `rollsSinceMythical >= 2001 → 100%` (overrides dice) |
| Lucky Roll Boost | `1.0 + luckyRollLevel × 0.25` |
| Lucky Roll Trigger | `totalRolls % 5 == 0 AND luckyRollLevel > 0` |
| Roll Speed | `max(200, 1600 - rollSpeedLevel × 72)` ms |
| Multi-Roll Count | `1 + multiRollLevel` (Lv0=1, Lv1=2, Lv2=3, Lv3=5) |
| Effective Roll Count | `min(selectedRollCount, maxUnlocked)` (0 = use max) |
| Upgrade Cost (recursive) | `min(floor(baseCost × 1.25^level), 100M)`, ROLL_SPEED: `min(floor(300 × 1.8^level), 100M)` |
| Upgrade Cost (fixed) | `fixedCosts[level]` (EQUIP_SLOTS, MULTI_ROLL) |
| Pet Coin Multiplier | `1.0 + Σ(pet.xpMultiplier - 1.0)` (same as XP) |
| Roll Chance Display | `max(floor(totalWeight / rarityChance), 2)` |
| Dice Weighted Dist. | REFINING: RARE 61.7%/EPIC 25%/LEGENDARY 11.1%/MYTHICAL 2.2% |
| Dice Weighted Dist. | ASCENDANT: EPIC 63.9%/LEGENDARY 25%/MYTHICAL 11.1% |
| Dice Weighted Dist. | LEGENDARY: LEGENDARY 75%/MYTHICAL 25% |
| SUPER DICE | 1T coins → 1 roll → guaranteed SUPER pet, flat 50B award |
| Auto-Sell | `if rarity in autoSellRarities AND rarity != "SECRET": sell on roll` |
| Pet Index Key | `"${speciesId}_${tier}"` → lifetime count |
| Burst Dice | N rolls instantly, no coins, best pet returned. 100k batch cap. |
| Burst Roll LUCK/TIER | Proportional consumption via local counters |
