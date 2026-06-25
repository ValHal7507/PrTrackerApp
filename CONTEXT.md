# PRTracker — Complete Project Context

## 1. What Is This App

PRTracker is a **personal record tracking app for exercises**. Users can create exercises (e.g. "Push-ups", "Plank"), log how many reps they did (or how many seconds they held a position), and track their personal records (PRs) over time.

It is a fully **offline** Android app — no login, no database, no internet requirement. Everything is stored as a JSON file on the device.

---

## 2. Tech Stack

| Category           | Choice                                    | Version                          |
| ------------------ | ----------------------------------------- | -------------------------------- |
| Language           | Kotlin                                    | 2.2.10                           |
| Minimum SDK        | Android 8.0 (API 26)                      | —                                |
| Target SDK         | Android 16 (API 36)                       | —                                |
| UI Toolkit         | Jetpack Compose (Material3)               | BOM 2026.02.01                   |
| Navigation         | Compose Navigation                        | 2.7.5                            |
| JSON Serialization | Gson                                      | 2.10.1                           |
| Charts             | MPAndroidChart                            | 3.1.0 (via JitPack)              |
| Animations         | Lottie Compose                            | 6.1.0                            |
| Background Work    | WorkManager                               | 2.9.0                            |
| Build System       | Gradle with Kotlin DSL                    | AGP 9.2.1                        |
| Icons              | Material Icons Extended                   | via BOM                          |
| ViewModel          | AndroidViewModel + StateFlow              | via lifecycle-runtime-ktx 2.10.0 |
| AOT Profiles       | profileinstaller + baselineprofile plugin | 1.3.1 / 1.5.0-alpha06            |
| GPS / Location     | Google Play Services Location             | 21.3.0                           |
| Benchmark Testing  | benchmark-macro-junit4                    | 1.5.0-alpha06                    |

Key dependencies in `gradle/libs.versions.toml`:

```toml
[versions]
agp = "9.2.1"
coreKtx = "1.18.0"
junit = "4.13.2"
junitVersion = "1.3.0"
espressoCore = "3.7.0"
lifecycleRuntimeKtx = "2.10.0"
activityCompose = "1.13.0"
kotlin = "2.2.10"
composeBom = "2026.02.01"
gson = "2.10.1"
lottie = "6.1.0"
navigationCompose = "2.7.5"
mpandroidchart = "3.1.0"
workmanager = "2.9.0"
profileinstaller = "1.3.1"
benchmark = "1.5.0-alpha06"
baselineprofile = "1.5.0-alpha06"
playServicesLocation = "21.3.0"
```

JitPack is added in `settings.gradle.kts` for MPAndroidChart:

```kotlin
maven { url = uri("https://jitpack.io") }
```

---

## 3. Project Structure

```
Prtracker/
├── build.gradle.kts                          # Root build file (plugin declarations)
├── settings.gradle.kts                       # Repository config (Google, Maven Central, JitPack)
├── gradle/
│   ├── libs.versions.toml                    # Version catalog (all dependencies)
│   └── wrapper/
├── gradlew / gradlew.bat                     # Gradle wrapper scripts
├── local.properties                          # Local SDK path (user-specific)
├── CONTEXT.md                                # THIS FILE
├── app.md                                    # AI agent reference for the codebase
├── prtracker_backup.json                     # Reference backup file for JSON validation
├── .opencode/
│   └── skills/
│       ├── prtracker-json-validator/         # Auto-validates JSON serialization after data class changes
│       │   └── SKILL.md
│       ├── prtracker-builder/                # Builds app and fixes compilation errors
│       │   └── SKILL.md
│       ├── prtracker-context-updater/        # Syncs CONTEXT.md with project state
│       │   └── SKILL.md
│       ├── prtracker-storage-field/          # Adds fields to data classes with Gson safety
│       │   └── SKILL.md
│       ├── prtracker-proguard-rule/          # Adds ProGuard/R8 keep rules
│       │   └── SKILL.md
│       ├── prtracker-bottom-nav-tab/         # Adds new bottom navigation tabs
│       │   └── SKILL.md
│       ├── prtracker-explain-feature/        # Explains features and components
│       │   └── SKILL.md
│       └── prompt-writer/                    # Generates coding prompts for PRTracker
│           └── SKILL.md
├── baselineprofile/                          # Baseline Profile module for AOT compilation
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── java/com/example/prtracker/
│           └── BaselineProfileGenerator.kt
│
└── app/
    ├── build.gradle.kts                      # App module build config (SDK versions, dependencies)
    ├── proguard-rules.pro                    # ProGuard/R8 rules for release builds
    └── src/main/
        ├── AndroidManifest.xml               # App manifest (permissions: INTERNET, VIBRATE, POST_NOTIFICATIONS, RECEIVE_BOOT_COMPLETED, ACCESS_FINE_LOCATION, FOREGROUND_SERVICE, FOREGROUND_SERVICE_LOCATION, WRITE_EXTERNAL_STORAGE (maxSdkVersion 28); FileProvider)
        ├── res/
        │   ├── values/
        │   │   ├── strings.xml               # app_name = "Pr tracker"
        │   │   ├── colors.xml                # Default template colors (unused by Compose)
        │   │   └── themes.xml                # Base Android theme (dark, no action bar)
        │   ├── drawable/
        │   │   ├── ic_launcher_background.xml # Green adaptive icon background
        │   │   ├── ic_launcher_foreground.xml # Adaptive icon foreground (bugdroid)
        │   │   └── ic_goal_notification.xml  # Target icon for goal notifications
        │   ├── mipmap-anydpi-v26/
        │   │   ├── ic_launcher.xml           # Adaptive icon definition
        │   │   └── ic_launcher_round.xml     # Adaptive round icon definition
        │   ├── raw/                           # Audio assets for mascot voice lines
        │   │   ├── mascot_welcome.mp3        # Welcome back voice line
        │   │   ├── speech_1.mp3              # "Systems fully operational..."
        │   │   ├── speech_2.mp3              # "The weights don't stand a chance..."
        │   │   ├── speech_3.mp3              # "Up and at 'em, Champion!..."
        │   │   ├── speech_4.mp3              # "Target locked: New Personal Records..."
        │   │   └── speech_5.mp3              # "No hesitation. No retreat!..."
        │   └── xml/
        │       ├── backup_rules.xml          # Auto-generated backup config
        │       ├── data_extraction_rules.xml # Auto-generated data extraction config
        │       └── file_paths.xml            # FileProvider paths (cache/share/)
        │
        └── java/com/example/prtracker/
            ├── MainActivity.kt               # Entry point: Scaffold, bottom nav, FAB, NavHost, WorkManager scheduling
            ├── data/
            │   ├── Exercise.kt               # Exercise data class (with isPinned, sortOrder, difficulty)
            │   ├── ExerciseDifficulty.kt     # ExerciseDifficulty enum, parsedDifficulty(), ExerciseClassifier
            │   ├── XpEngine.kt               # XP/level system: XP rates, level formula, computeTotalXp
            │   ├── PREntry.kt                # PREntry data class
            │   ├── Goal.kt                   # Goal data class (exercise-linked, period-based targets)
            │   ├── WeightEntry.kt            # WeightEntry data class (weight, date, note)
            │   ├── AppSettings.kt            # AppSettings data class (weightUnit, targetWeight, calendarDayViewMode)
            │   ├── LeverageTelemetry.kt      # RSI telemetry data class (entryId, dateString, rawValue, pairedWeight, rsiScore)
            │   ├── RsiEngine.kt              # RSI computation engine: closest-weight pairing, buildTelemetry
            │   ├── SoundEngine.kt            # Programmatic audio synthesis for UI sound effects (tap, PR, delete, timer)
            │   ├── TierEvaluator.kt          # 10-tier calisthenics progression evaluator (alias matching, Levenshtein, evaluate)
            │   ├── AppearanceSettings.kt    # AppearanceSettings data class (8 accent color longs, active theme ID)
            │   ├── ColorPalette.kt          # NEON_PALETTE — 20 color swatches for custom color picker
            │   ├── ThemeDefinitions.kt      # AppTheme data class + ALL_THEMES preset list (6 themes)
            │   ├── RunEntry.kt              # RunEntry data model (distance, duration, date, note)
            │   ├── RunningPRs.kt            # RunningPRs data model (best pace, splits, totals)
            │   ├── RunningPREngine.kt       # Running PR computation engine
            │   ├── WorkoutPreset.kt         # PresetExercise and WorkoutPreset data classes (preset templates)
            │   ├── WorkoutSession.kt        # WorkoutSession, SessionExerciseProgress, SessionSetEntry data classes
            │   ├── Pet.kt                   # Pet data class, PetRarity (6 tiers), PetTier (6 tiers), PetSpecies, PetCatalog
            │   ├── PetUpgrade.kt            # PetUpgrade enum (LUCK, ROLL_SPEED, LUCKY_ROLL, COIN_MULTIPLIER) with cost formulas
            │   └── StorageManager.kt         # Gson-based JSON file read/write (exercises + goals + weight + settings + presets + sessions + history)
            ├── viewmodel/
            │   └── PRViewModel.kt            # Shared ViewModel (exercises, goals, weight, settings, haptic state, workout history)
            ├── navigation/
            │   └── NavGraph.kt               # Route definitions + NavHost setup
            ├── service/
            │   └── RunTrackingService.kt     # Foreground service for live GPS run tracking (FusedLocationProviderClient)
            ├── work/
            │   ├── MorningReminderWorker.kt  # WorkManager worker for daily 8:00 AM training reminder
            │   ├── GoalNotificationWorker.kt # WorkManager worker for daily 8:30 AM goal reminders
            │   ├── EveningReviewWorker.kt    # WorkManager worker for daily 9:30 PM daily review summary
            │   └── WeeklySummaryWorker.kt    # WorkManager worker for Sunday 10 PM weekly summary
            └── ui/
                ├── theme/
                │   ├── Color.kt              # Cyberpunk color palette + pinned/complete colors
                │   ├── Type.kt               # Typography (Monospace for titles)
                │   ├── Theme.kt              # Dark-only MaterialTheme
                │   └── DynamicColors.kt      # LocalAppearance CompositionLocal + extension color properties
                ├── components/
                │   ├── GlowingCard.kt         # Glassmorphism card with neon gradient border (supports custom brush)
                │   ├── NeonButton.kt         # Full-width glowing cyan button
                │   ├── AnimatedRing.kt       # Canvas-drawn circular progress ring (supports custom colors)
                │   ├── GridBackground.kt     # Animated moving grid lines (drawBehind, no recomposition, 30fps cap)
                │   ├── SetGoalDialog.kt      # Dialog for setting/editing/removing per-exercise goals
                │   ├── PRCelebrationOverlay.kt # Lottie confetti + purple flash + text
                │   ├── RunningCard.kt        # Running summary card for DashboardScreen
                │   └── BalanceEngine.kt      # Movement classification engine (mapExercisesToVectors, computeAsymmetryIndex) + BiomechanicalRadarCard pentagonal radar chart
                └── screens/
                    ├── HomeScreen.kt         # Entry point: quick overview / entry picker
                    ├── DashboardScreen.kt     # Main screen: exercise grid with pinned sections, drag-and-drop reorder, deferred first-frame visibility
                    ├── CalendarScreen.kt      # Monthly calendar with 5 cell types (WORKOUT/REST/MISSED/FUTURE/EMPTY), streak tracking, rest day toggle, BiomechanicalRadarCard, MovementAnalysisSection
                    ├── AddExerciseScreen.kt   # Create new exercise form
                    ├── LogEntryScreen.kt      # Log reps/seconds for an exercise
                    ├── ExerciseDetailScreen.kt # Chart + entry history + delete
                    ├── ExerciseHistoryScreen.kt # Scrollable list of all logged entries with PR color coding
                    ├── WorkoutHistoryScreen.kt  # Past completed workout sessions with double-confirm delete
                    ├── SettingsScreen.kt      # Settings: haptics, weight unit, export, clear, version
                    ├── GoalsScreen.kt         # Goals list with GoalCards + FAB
                    ├── HoldTimerScreen.kt    # Fullscreen countdown/stopwatch for timing hold exercises
                    ├── AddGoalScreen.kt       # Create new goal form
                    ├── WeightScreen.kt        # Body weight tracker: stats row + chart + history list with swipe-to-delete
                    ├── LogWeightScreen.kt     # Log a body weight entry with increment/decrement controls
                    ├── AppearanceScreen.kt    # Color customization: preset themes, per-system accent sliders, live preview
                    ├── SyncExportScreen.kt    # File-based data export via Android share sheet
                    ├── SyncImportScreen.kt    # Import confirmation screen for received JSON data
                    ├── NotificationSettingsScreen.kt # Notification preferences: toggle morning/evening/weekly, set times
                    ├── RankScreen.kt          # 10-tier progression rank screen (terminal header, active tier card, next unlock, full matrix)
                    ├── LiveRunScreen.kt       # Live GPS run tracking with real-time distance, pace, and elapsed time
                    ├── LogRunScreen.kt        # Log a run entry (distance, duration, pace/speed/calories preview, NEW PR badge)
                    ├── RunHistoryScreen.kt    # Run history with all-time records, entry list with delete, and LIVE RUN button
                    ├── WorkoutPresetsScreen.kt # Workout preset list with reorder mode, create/edit sheet, pin/delete actions
                    ├── WorkoutPresetDetailScreen.kt # Full-screen preset detail view with exercises, edit/delete actions
                    ├── WorkoutSessionScreen.kt # Live workout execution with timer, per-set input, pause/resume/finish
                    ├── PresetAnalysisScreen.kt # Movement pattern analysis with pentagonal radar chart and exercise classification (VP/HP/CL/VPush/HPush)
                    ├── RestGameScreen.kt        # Protein Catch mini-game: catch falling scoops to fill cup, servings score, chaos bursts
                    ├── DiceRollScreen.kt        # Pet dice roll mini-game: rarity/pity system, fusion, selling, tap-dice-to-roll
                    ├── DiceShopScreen.kt         # Special dice shop: buy consumable dice that filter rarity chances
                    ├── DiceInventoryScreen.kt    # Owned special dice: activate consumable dice for filtered rolls
                    ├── PetInventoryScreen.kt     # Pet inventory full-screen: search, sort, grid, inline pet detail, bulk actions
                    └── PetUpgradesScreen.kt     # Pet upgrade shop: LUCK, ROLL_SPEED, LUCKY_ROLL, COIN_MULTIPLIER, EQUIP_SLOTS upgrades
```

---

## 4. Data Models

### `Exercise` (`data/Exercise.kt`)

```kotlin
@Immutable
data class Exercise(
    val id: String,           // UUID.randomUUID().toString()
    val name: String,         // User-given name, e.g. "Push-ups"
    val type: String,         // "reps" or "hold" (seconds)
    val entries: List<PREntry>, // All logged entries for this exercise
    val isPinned: Boolean = false,  // Whether exercise is pinned to top of dashboard
    val sortOrder: Int = 0,   // Order within pinned/unpinned section
    val goal: Int? = null,    // Optional per-exercise target value (null = no goal set)
    val difficulty: String = "MEDIUM"  // Auto-classified difficulty: EASY/MEDIUM/HARD/EXTREME
)
```

### `ExerciseDifficulty` (`data/ExerciseDifficulty.kt`)

```kotlin
enum class ExerciseDifficulty(val label: String) {
    EASY("EASY"), MEDIUM("MEDIUM"), HARD("HARD"), EXTREME("EXTREME")
}
```

`ExerciseClassifier` object provides `classify(exerciseName: String): ExerciseDifficulty` using a two-pass approach:

1. **Known-exercise map** — longest substring match against a hardcoded map (case-insensitive)
2. **Keyword fallback** — scans EXTREME → HARD → MEDIUM → EASY keyword lists

Helper extension: `Exercise.parsedDifficulty(): ExerciseDifficulty` converts the stored `difficulty` string to enum, defaulting to `MEDIUM`.

### `XpEngine` (`data/XpEngine.kt`)

XP/level system with Pokémon GO–style exponential scaling (`xpNeededForNextLevel(n) = floor(1000 × n^1.5)`, max level 200).

| Difficulty | XP per rep | XP per second (hold) |
| ---------- | ---------- | -------------------- |
| EASY       | 20         | 8                    |
| MEDIUM     | 50         | 20                   |
| HARD       | 120        | 50                   |
| EXTREME    | 300        | 120                  |

Key functions: `xpForEntry()`, `levelFromTotalXp()`, `xpInCurrentLevel()`, `xpNeededForCurrentLevelUp()`, `computeTotalXpFromExercises()`, `predictWorkoutXp()`.

### `PREntry` (`data/PREntry.kt`)

```kotlin
@Immutable
data class PREntry(
    val id: String,     // UUID.randomUUID().toString()
    val value: Int,     // Number of reps, or seconds held
    val date: Long,     // System.currentTimeMillis() at time of logging
    val note: String,   // Optional user note, defaults to ""
    val xpEarned: Long = 0L  // Multiplied XP at log time (pet + potion), 0L for backward compat
)
```

### `Goal` (`data/Goal.kt`)

```kotlin
@Immutable
data class Goal(
    val id: String,              // UUID.randomUUID().toString()
    val exerciseId: String,      // Links to existing Exercise
    val exerciseName: String,    // Stored for display without lookup
    val targetValue: Int,        // e.g. 200
    val period: String,          // "daily", "weekly", or "monthly"
    val createdAt: Long,         // Timestamp of when goal was created
    val type: String             // "reps" or "hold" — copied from exercise
)
```

### `WeightEntry` (`data/WeightEntry.kt`)

```kotlin
@Immutable
data class WeightEntry(
    val id: String,        // UUID.randomUUID().toString()
    val weight: Float,    // Weight in kg (always stored in kg internally)
    val date: Long,        // Timestamp of when weight was logged
    val note: String       // Optional note, defaults to ""
)
```

Weight is always stored in kilograms internally. Conversion to/from pounds happens only at the UI layer via the `weightUnit` setting.

### `RunEntry` (`data/RunEntry.kt`)

```kotlin
@Immutable
data class RunEntry(
    val id: String,             // UUID.randomUUID().toString()
    val distanceMeters: Float,  // Distance in meters (always stored in meters)
    val durationSeconds: Int,   // Total duration in seconds
    val date: Long,             // Timestamp of when run was logged
    val note: String = ""       // Optional note
)
```

Computed properties: `paceSecondsPerKm` (duration / distance in km), `caloriesBurned` (distance in km × 65), `speedKmh` (km / hours).

### `RunningPRs` (`data/RunningPRs.kt`)

```kotlin
@Immutable
data class RunningPRs(
    val bestPaceSecondsPerKm: Float? = null,
    val bestDistanceMeters: Float? = null,
    val bestDurationSeconds: Int? = null,
    val best500mSeconds: Int? = null,
    val best1kmSeconds: Int? = null,
    val best2kmSeconds: Int? = null,
    val best5kmSeconds: Int? = null,
    val best10kmSeconds: Int? = null,
    val bestCaloriesInRun: Int? = null,
    val totalDistanceMeters: Float = 0f,
    val totalRuns: Int = 0
)
```

Computed by `RunningPREngine.computePRs()`. Split times are estimated from qualifying entries (distance >= split distance). All null by default for Gson backward compat.

### `AppSettings` (`data/AppSettings.kt`)

```kotlin
data class AppSettings(
    val weightUnit: String = "kg",            // "kg" or "lbs"
    val targetWeight: Float? = null,          // Optional target body weight in kg
    val calendarDayViewMode: String = "pr",   // "pr" or "sum" — how to aggregate daily entry values
    val morningReminderHour: Int = 8,         // Hour for morning training reminder (0-23)
    val morningReminderMinute: Int = 0,       // Minute for morning training reminder (0-59)
    val eveningReviewHour: Int = 21,          // Hour for evening daily review (0-23)
    val eveningReviewMinute: Int = 30,        // Minute for evening daily review (0-59)
    val soundEnabled: Boolean = true,         // Whether UI sound effects are enabled
    val soundVolume: Float = 0.5f,            // Sound effects volume (0.0 - 1.0)
    val hapticEnabled: Boolean = true,         // Whether haptic feedback is enabled
    val speechesEnabled: Boolean = true,      // Whether mascot voice lines are enabled
    val appearance: AppearanceSettings = AppearanceSettings()  // Color customization settings
)
```

Weight is always stored in kilograms internally. Conversion to/from pounds happens only at the UI layer via the `weightUnit` setting.

### `Pet` (`data/Pet.kt`)

```kotlin
@Immutable
data class Pet(
    val id: String = UUID.randomUUID().toString(),
    val speciesId: String = "",
    val name: String = "",
    val rarity: String = "COMMON",    // COMMON, UNCOMMON, RARE, EPIC, LEGENDARY, MYTHICAL
    val stars: Int = 1,               // 1-5 stars
    val obtainedAt: Long = System.currentTimeMillis(),
    val rollNumber: Int = 0,
    val tier: String = "NORMAL",      // NORMAL, SILVER, GOLDEN, RAINBOW, DARK_MATTER, RED_MATTER
    val isFavorited: Boolean = false  // FAVORITED pets are protected from Sell All
)
```

`PetRarity` enum defines 7 rarity tiers with drop chances, base coin values, and base XP multipliers: COMMON (54.4%, 100 coins, 1.00x), UNCOMMON (28%, 250 coins, 1.05x), RARE (13%, 600 coins, 1.10x), EPIC (4%, 1500 coins, 1.20x), LEGENDARY (0.5%, 5000 coins, 1.35x), MYTHICAL (0.1%, 15000 coins, 1.50x), SUPER (0.01% 1/10k independent, 1T coins, 1.00x).

`PetTier` enum defines 6 evolution tiers with coin multipliers and XP multipliers: NORMAL (1x, 1.00x), SILVER (2x, 1.10x), GOLDEN (4x, 1.25x), RAINBOW (8x, 1.40x), DARK_MATTER (16x, 1.60x), RED_MATTER (32x, 2.00x). A pet's coin value = `PetRarity.baseCoins × PetTier.coinMultiplier × stars`. A pet's XP multiplier = `PetRarity.baseXpMult × PetTier.xpMult × (1 + (stars-1) × 0.01)`.

`PetSpecies` holds 18 placeholder species (6 per rarity). `PetCatalog.allSpecies` is the registry. Species are code-only — only `Pet` instances are persisted to JSON.

`fun Pet.coinValue()` extension function computes the sell value. Fusion consumes a 5★ pet and creates the next tier with 1★.

`data class RollResult(pet, effectiveChances, isLuckyRoll)` — returned by `rollDice()`. `effectiveChances` contains the actual boosted chances used for the roll (including lucky roll boost), used for the "1 in X CHANCE" display on pet reveal.

### `PetUpgrade` (`data/PetUpgrade.kt`)

```kotlin
enum class PetUpgrade(
    val id: String,
    val displayName: String,
    val description: String,
    val baseCost: Long,
    val costMultiplier: Float,
    val fixedCosts: List<Long>? = null  // null = exponential scaling, non-null = fixed per-level costs
) {
    LUCK(id = "luck", displayName = "LUCK", description = "Improves rare drop chances (+20% per level)", baseCost = 500L, costMultiplier = 1.12f),
    COIN_MULTIPLIER(id = "coin_multiplier", displayName = "COIN MULTIPLIER", description = "Boosts coins earned per roll (+0.20x per level)", baseCost = 600L, costMultiplier = 1.13f),
    ROLL_SPEED(id = "roll_speed", displayName = "ROLL SPEED", description = "Faster dice animation (-72ms per level)", baseCost = 300L, costMultiplier = 1.10f),
    LUCKY_ROLL(id = "lucky_roll", displayName = "LUCKY ROLL", description = "Every 5th roll is lucky — boosted rarity chances", baseCost = 1000L, costMultiplier = 1.15f),
    EQUIP_SLOTS(id = "equip_slots", displayName = "EQUIP SLOTS", description = "More active pet equip slots", baseCost = 0L, costMultiplier = 1f, fixedCosts = listOf(1_000_000L, 10_000_000L, 100_000_000L));

    open fun costForLevel(currentLevel: Int): Long {
        if (fixedCosts != null) return if (currentLevel < fixedCosts.size) fixedCosts[currentLevel] else Long.MAX_VALUE
        return (baseCost * 1.25.pow(currentLevel)).toLong().coerceAtMost(100_000_000L)
    }
    // ROLL_SPEED override:
    override fun costForLevel(currentLevel: Int): Long =
        (baseCost * 1.8.pow(currentLevel)).toLong().coerceAtMost(100_000_000L)
    fun nextLevelCost(currentLevel: Int): Long = costForLevel(currentLevel)
    fun maxLevel(): Int? = fixedCosts?.size
}
```

5 upgradeable stats stored as `Map<String, Int>` in `PetStorageData.petUpgrades` (upgrade ID → level). Cost scaling uses a **recursive formula**: standard upgrades ×1.25 per level (`baseCost × 1.25^level`), ROLL_SPEED uses ×1.8 per level (`baseCost × 1.8^level`, max 23 at 0ms). All costs are capped at 100M coins (`coerceAtMost(100_000_000L)`) so upgrades never cost more than 100M per level. EQUIP_SLOTS uses fixed costs (1M/10M/100M) with max 3 levels. **Luck**: each level adds +20% to ALL non-COMMON rarity chances (Lv1=1.2x, Lv5=2.0x). **Coin Multiplier**: each level adds +0.20x to all coin earnings (Lv1=1.2x, Lv5=2.0x); coin earnings are also multiplied by the pet XP multiplier from equipped pets. **Roll Speed**: each level reduces dice animation delay by 72ms (base 1600ms, reaches 0ms at L23). **Lucky Roll**: every 5th roll when upgrade > 0, boosted rarity chances scaling with level (+0.25x per level), and tier of rolled pets is based on lucky roll level: Lv1+=SILVER, Lv51+=GOLDEN, Lv101+=RAINBOW, Lv151+=DARK_MATTER, Lv201+=RED_MATTER. **Equip Slots**: adds starting equip slots (base 2, each level adds 1, max 5). Accessible via the coin counter on DiceRollScreen.

### `LeverageTelemetry` (`data/LeverageTelemetry.kt`)

```kotlin
@Immutable
data class LeverageTelemetry(
    val entryId: String,
    val dateString: String,
    val rawValue: Int,
    val pairedWeight: Float,
    val rsiScore: Int
)
```

RSI (Relative Strength Index) telemetry computed by pairing each exercise entry with the closest body-weight entry in time. `rsiScore = rawValue × pairedWeight` for both reps and hold type exercises.

### `TierResult` / `CriterionStatus` (`data/TierEvaluator.kt`)

```kotlin
@Immutable
data class TierResult(
    val currentTier: Int,
    val tierName: String,
    val tierColor: Color,
    val overallProgress: Float,
    val nextTierNumber: Int,
    val nextTierName: String,
    val nextTierCriteria: List<CriterionStatus>
)

@Immutable
data class CriterionStatus(
    val label: String,
    val userBest: Int,
    val required: Int,
    val met: Boolean
)
```

`TierEvaluator` defines 10 calisthenics progression tiers (RAW_CARBON through SYSTEM_OVERRIDE), each with 3 criteria. A tier is unlocked when ≥2 criteria are met. Exercise names are matched to categories via alias tables with `contains()` and Levenshtein distance fallback.

Tier names: RAW_CARBON, BOOT_SEQUENCE, STRUCTURAL_PATCH, OVERCLOCK_V1, NEURAL_BRIDGE, KINETIC_FLOW, CHASSIS_APEX, GRAVITY_REBEL, QUANTUM_LEVER, SYSTEM_OVERRIDE.

### `AppearanceSettings` (`data/AppearanceSettings.kt`)

```kotlin
@Immutable
data class AppearanceSettings(
    val exerciseAccent: Long = 0xFF00F5FF,
    val exerciseSecondary: Long = 0xFF7B2FFF,
    val pinnedAccent: Long = 0xFF00FF85,
    val pinnedSecondary: Long = 0xFFFF003C,
    val runningAccent: Long = 0xFFFF2D78,
    val runningSecondary: Long = 0xFFC4005A,
    val systemAccent: Long = 0xFF00F5FF,
    val systemSecondary: Long = 0xFF7B2FFF,
    val activeThemeId: String = "default"
)
```

8 accent colors stored as `Long` (ARGB hex) for Gson serialization. Converted to Compose `Color` via extension properties in `DynamicColors.kt`. `activeThemeId` tracks which preset theme is active (`"default"`, `"blood_iron"`, `"toxic_green"`, `"void_purple"`, `"solar_storm"`, `"ice_protocol"`, or `"custom"`).

### `WorkoutPreset` / `PresetExercise` (`data/WorkoutPreset.kt`)

```kotlin
@Immutable
data class PresetExercise(
    val exerciseName: String = "",
    val targetReps: Int = 0,
    val targetHoldSeconds: Int = 0,
    val sets: Int = 3,
    val notes: String = ""
)

@Immutable
data class WorkoutPreset(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val description: String = "",
    val exercises: List<PresetExercise> = emptyList(),
    val difficulty: String = "MEDIUM",
    val createdAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val sortOrder: Int = 0
)
```

`PresetExercise` holds a single exercise within a preset — exercise name references an existing tracked exercise (not a UUID link), target reps/hold seconds, set count, and optional notes. `WorkoutPreset` is the container with name, description, difficulty (EASY/MEDIUM/HARD/MAX EFFORT), created-at timestamp, pin state, and `sortOrder` for manual reorder. All fields have defaults for Gson backward compat. No `tags` or `estimatedDurationMinutes` fields — removed to keep the model lean. `restBetweenSetsSeconds` was removed from `PresetExercise` — rest timer was deemed unnecessary for the preset workflow.

### `WorkoutSession` / `SessionExerciseProgress` / `SessionSetEntry` (`data/WorkoutSession.kt`)

```kotlin
@Immutable
data class SessionSetEntry(
    val value: Int = 0
)

@Immutable
data class SessionExerciseProgress(
    val exerciseName: String = "",
    val targetValue: Int = 0,
    val isHold: Boolean = false,
    val totalSets: Int = 0,
    val completedSets: List<SessionSetEntry> = emptyList()
)

@Immutable
data class WorkoutSession(
    val id: String = "",
    val presetId: String = "",
    val presetName: String = "",
    val startedAt: Long = 0L,
    val pausedDurationMs: Long = 0L,
    val pausedSinceMs: Long = 0L,
    val isPaused: Boolean = false,
    val isCompleted: Boolean = false,
    val exercises: List<SessionExerciseProgress> = emptyList()
) {
    fun elapsedMs(now: Long): Long {
        if (startedAt == 0L) return 0L
        val totalPaused = if (isPaused) pausedDurationMs + (now - pausedSinceMs).coerceAtLeast(0L) else pausedDurationMs
        return (now - startedAt - totalPaused).coerceAtLeast(0L)
    }
}
```

`WorkoutSession` tracks a live workout execution: started timestamp, accumulated pause duration (`pausedDurationMs`), current pause start time (`pausedSinceMs`), pause flag, completion flag, and per-exercise progress. `elapsedMs(now)` correctly excludes all paused time (including the current pause if `isPaused` is true). All fields have Gson-safe defaults for backward compatibility.

---

## 5. Data Storage

**No database.** All data is persisted as two JSON files on the device.

- **App data file:** `context.filesDir/prs.json`
- **Pet data file:** `context.filesDir/pets.json`
- **Format:** Gson-serialized `StorageData` object (app) and `PetStorageData` object (pets)
- **Backward compatibility:** Old format (plain `List<Exercise>` array) is detected and migrated on load; old `prs.json` with embedded pet fields is auto-migrated to `pets.json`
- **Load timing:** Called once in `PRViewModel.init{}`
- **Save timing:** App data saved after every exercise/goal/weight/setting mutation. Pet data saved independently after every dice roll, fuse, sell, upgrade, equip, or favorite operation (avoids rewriting app JSON on every pet action).

### `StorageData` wrapper (app data — `prs.json`)

```kotlin
@Immutable
data class StorageData(
    val exercises: List<Exercise> = emptyList(),
    val goals: List<Goal> = emptyList(),
    val weightEntries: List<WeightEntry> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val restDays: List<String> = emptyList(),  // Format: "YYYY-MM-DD"
    val runEntries: List<RunEntry> = emptyList(),
    val runningPRs: RunningPRs = RunningPRs(),
    val workoutPresets: List<WorkoutPreset> = emptyList(),
    val workoutSession: WorkoutSession? = null,
    val workoutHistory: List<WorkoutSession> = emptyList(),
    val totalXp: Long = 0L,             // Accumulated XP across all time
    val xpBootstrapped: Boolean = false, // True once retroactive XP migration has run
    val petInventory: List<Pet> = emptyList(),       // All collected pets (with tier, stars)
    val totalRolls: Long = 0L,                       // Lifetime dice rolls
    val rollsSinceEpicOrAbove: Long = 0L,            // Pity counter for epic+ (soft pity at 150)
    val rollsSinceLegendary: Long = 0L,              // Hard pity: guaranteed legendary at 401
    val rollsSinceMythical: Long = 0L,               // Hard pity: guaranteed mythical at 2001
    val lastDiceRollTimestamp: Long = 0L,            // Timestamp of last roll
    val coins: Long = 0L,                             // Pet dice coins (earned on every roll)
    val petUpgrades: Map<String, Int> = emptyMap()    // Upgrade ID → level (e.g. "luck" → 3)
)
```

`restDays` is a list of date strings where the user explicitly marked a rest day. Defaults to empty list for backward compatibility with old JSON files — no migration needed. `runEntries` and `runningPRs` both have defaults for Gson backward compat. `workoutPresets` defaults to empty list for backward compatibility. `workoutSession` defaults to null for backward compatibility. `workoutHistory` defaults to empty list for backward compatibility with older JSON files. `totalXp` and `xpBootstrapped` default to 0L and false for Gson backward compat. All pet fields (`petInventory`, `totalRolls`, `rollsSinceEpicOrAbove`, `rollsSinceLegendary`, `rollsSinceMythical`, `lastDiceRollTimestamp`, `coins`, `petUpgrades`) default to empty/0L for Gson backward compat.

### `PetStorageData` wrapper (pet data — `pets.json`)

```kotlin
@Immutable
data class PetStorageData(
    val petInventory: List<Pet> = emptyList(),
    val totalRolls: Long = 0L,
    val rollsSinceEpicOrAbove: Long = 0L,
    val rollsSinceLegendary: Long = 0L,
    val rollsSinceMythical: Long = 0L,
    val lastDiceRollTimestamp: Long = 0L,
    val coins: Long = 0L,
    val petUpgrades: Map<String, Int> = emptyMap(),
    val equippedPetIds: List<String> = emptyList(),  // IDs of equipped pets (max 2-5 slots)
    val diceInventory: List<SpecialDice> = emptyList(),     // Owned special dice (consumable)
    val activeDiceEffects: List<ActiveDiceEffect> = emptyList()  // Active dice queue (strongest first)
)
```

Separate from `StorageData` to avoid rewriting the full app JSON on every pet action. Auto-migrated from old `prs.json` on first launch via `StorageManager.migrateIfNeeded()`.

### `StorageManager` (`data/StorageManager.kt`)

| Method                                                                                                                                                               | Description                                                                                                                                                                                                                     |
| -------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `loadData(): Pair<List<Exercise>, List<Goal>>`                                                                                                                       | Reads `prs.json`, parses as `StorageData` (falls back to old array format). Returns sorted exercises + goals.                                                                                                                   |
| `saveData(exercises, goals)`                                                                                                                                         | Serializes `StorageData` to JSON and writes to `prs.json`. Preserves existing weight entries, settings, and restDays.                                                                                                           |
| `loadExercises(): List<Exercise>`                                                                                                                                    | Convenience wrapper — calls `loadData()` and returns only exercises.                                                                                                                                                            |
| `saveExercises(List<Exercise>)`                                                                                                                                      | Preserves existing goals, weight entries, settings, and restDays; saves updated exercises.                                                                                                                                      |
| `loadFullData(): StorageData`                                                                                                                                        | Reads `prs.json` and returns the full `StorageData` object (for the settings screen).                                                                                                                                           |
| `saveFullData(...)` | Saves the full `StorageData` object (all nineteen fields) to `prs.json`.                                                                                         |
| `loadPetData(): PetStorageData`                                                                                                                                      | Reads `pets.json` and returns the full `PetStorageData` object. Falls back to migrating pet fields from old `prs.json` via `migrateIfNeeded()`.                                                                                 |
| `savePetData(...)` | Saves the full `PetStorageData` object (11 fields) to `pets.json`. Only called for pet-specific operations (dice roll, fuse, sell, upgrade, equip, favorite).                                                                    |
| `migrateIfNeeded()` | One-time migration: moves pet fields from old `prs.json` to `pets.json`. Deletes pet fields from `prs.json` after migration.                                                                                                     |

---

## 6. Navigation Structure

### Routes (defined in `navigation/NavGraph.kt`)

| Route                          | Screen                       | Arguments            |
| ------------------------------ | ---------------------------- | -------------------- |
| `"home"`                       | `HomeScreen`                 | None                 |
| `"dashboard"`                  | `DashboardScreen`            | None                 |
| `"goals"`                      | `GoalsScreen`                | None                 |
| `"add_goal"`                   | `AddGoalScreen`              | None                 |
| `"add_exercise"`               | `AddExerciseScreen`          | None                 |
| `"log_entry/{exerciseId}"`     | `LogEntryScreen`             | `exerciseId: String` |
| `"detail/{exerciseId}"`        | `ExerciseDetailScreen`       | `exerciseId: String` |
| `"calendar"`                   | `CalendarScreen`             | None                 |
| `"settings"`                   | `SettingsScreen`             | None                 |
| `"weight"`                     | `WeightScreen`               | None                 |
| `"log_weight"`                 | `LogWeightScreen`            | None                 |
| `"appearance"`                 | `AppearanceScreen`           | None                 |
| `"sync_export"`                | `SyncExportScreen`           | None                 |
| `"sync_import"`                | `SyncImportScreen`           | None                 |
| `"rank"`                       | `RankScreen`                 | None                 |
| `"hold_timer/{exerciseId}"`    | `HoldTimerScreen`            | `exerciseId: String` |
| `"notification_settings"`      | `NotificationSettingsScreen` | None                 |
| `"log_run"`                    | `LogRunScreen`               | None                 |
| `"live_run"`                   | `LiveRunScreen`              | None                 |
| `"run_history"`                | `RunHistoryScreen`           | None                 |
| `"presets"`                    | `WorkoutPresetsScreen`       | None                 |
| `"presets?editId={editId}"`    | `WorkoutPresetsScreen`       | `editId: String?`    |
| `"preset_detail/{presetId}"`   | `WorkoutPresetDetailScreen`  | `presetId: String`   |
| `"preset_analysis/{presetId}"` | `PresetAnalysisScreen`       | `presetId: String`   |
| `"workout_session/{presetId}"` | `WorkoutSessionScreen`       | `presetId: String`   |
| `"exercise_history"`           | `ExerciseHistoryScreen`      | None                 |
| `"workout_history"`            | `WorkoutHistoryScreen`       | None                 |
| `"rest_game"`                  | `RestGameScreen`             | None                 |
| `"dice_roll"`                  | `DiceRollScreen`             | None                 |
| `"dice_shop"`                  | `DiceShopScreen`             | None                 |
| `"dice_inventory"`             | `DiceInventoryScreen`        | None                 |
| `"pet_inventory"`              | `PetInventoryScreen`         | None                 |
| `"pet_upgrades"`               | `PetUpgradesScreen`          | None                 |

Helper functions in the `Routes` object:

```kotlin
Routes.logEntry(id)     // → "log_entry/<id>"
Routes.detail(id)       // → "detail/<id>"
Routes.holdTimer(id)    // → "hold_timer/<id>"
Routes.logRun()         // → "log_run"
Routes.runHistory()     // → "run_history"
Routes.presetDetail(id) // → "preset_detail/<id>"
Routes.presetAnalysis(id) // → "preset_analysis/<id>"
Routes.workoutSession(id) // → "workout_session/<id>"
```

Routes without helper functions (accessed via const vals directly):

```kotlin
Routes.LIVE_RUN         // = "live_run"
Routes.APPEARANCE       // = "appearance"
Routes.DICE_SHOP        // = "dice_shop"
Routes.DICE_INVENTORY   // = "dice_inventory"
```

### Navigation Flow

```
Dashboard ──tap card──→ ExerciseDetailScreen
Dashboard ──FAB────────→ AddExerciseScreen
Dashboard ──bottom nav──→ CalendarScreen
Dashboard ──bottom nav──→ WeightScreen
Dashboard ──bottom nav──→ GoalsScreen
Dashboard ──bottom nav──→ SettingsScreen
Dashboard ──Running card──→ RunHistoryScreen
Dashboard ──Running card──→ LogRunScreen

RunHistoryScreen ──"LIVE RUN" button──→ LiveRunScreen

LiveRunScreen ──save/discard──→ pops back to RunHistoryScreen

GoalsScreen ──FAB──────→ AddGoalScreen

AddGoalScreen ──confirm──→ pops back to GoalsScreen
AddGoalScreen ──back────→ pops back to GoalsScreen

ExerciseDetailScreen ──"LOG ENTRY" button──→ LogEntryScreen
ExerciseDetailScreen ──"TIMER" button (hold exercises only)──→ HoldTimerScreen

LogEntryScreen ──log entry──→ pops back to ExerciseDetailScreen
LogEntryScreen ──if PR──→ celebration overlay ──auto-dismiss──→ pops back to ExerciseDetailScreen
LogEntryScreen ──timer icon button (hold exercises only)──→ HoldTimerScreen

HoldTimerScreen ──log entry (no PR)──→ pops back to ExerciseDetailScreen (or LogEntryScreen)
HoldTimerScreen ──log entry (new PR)──→ celebration overlay ──auto-dismiss──→ pops back

AddExerciseScreen ──confirm──→ pops back to Dashboard
SettingsScreen ──various actions──→ stays on SettingsScreen
SettingsScreen ──"APPEARANCE"──────→ AppearanceScreen
SettingsScreen ──"NOTIFICATIONS"──→ NotificationSettingsScreen
SettingsScreen ──"SHARE DATA"──→ SyncExportScreen

SyncExportScreen ──back/complete──→ pops back to SettingsScreen
MainActivity ──(intent filter: ACTION_VIEW + application/json)──→ SyncImportScreen
SyncImportScreen ──REPLACE/MERGE──→ pops back or navigates to Dashboard

WeightScreen ──FAB──────→ LogWeightScreen

LogWeightScreen ──back────→ pops back to WeightScreen

GoalsScreen ──"RANK EVALUATOR" button──→ RankScreen

RankScreen ──back──→ pops back to GoalsScreen

GoalsScreen ──"REST GAME" button──→ RestGameScreen

RestGameScreen ──back──→ pops back to GoalsScreen

GoalsScreen ──"PET DICE" button──→ DiceRollScreen

DiceRollScreen ──back──→ pops back to GoalsScreen

DiceRollScreen ──"INVENTORY" button──→ PetInventoryScreen
PetInventoryScreen ──back──→ pops back to DiceRollScreen

DiceRollScreen ──"DICE" button──→ DiceInventoryScreen
DiceInventoryScreen ──back──→ pops back to DiceRollScreen
DiceInventoryScreen ──"USE" dialog──→ activates dice, pops back to DiceRollScreen

DiceRollScreen ──"SHOP" button──→ DiceShopScreen
DiceShopScreen ──back──→ pops back to DiceRollScreen
DiceShopScreen ──"BUY" button──→ coins deducted, dice added to inventory

CalendarScreen ──bottom nav──→ any other bottom nav route
CalendarScreen ──tap WORKOUT day──→ shows popup with exercises logged that day
CalendarScreen ──tap REST day──→ shows popup confirming rest day
CalendarScreen ──"MARK TODAY AS REST DAY"──→ toggles rest day status with haptic feedback
CalendarScreen ──"ANALYZE MOVEMENT →" button──→ PresetAnalysisScreen

PresetAnalysisScreen ──back──→ pops back to CalendarScreen

Dashboard ──bottom nav──→ PresetsScreen
PresetsScreen ──FAB────────→ creates new preset
PresetsScreen ──tap preset card──→ PresetDetailScreen
PresetsScreen ──tap preset card edit──→ opens edit sheet (or from detail via editId param)
PresetsScreen ──pin/delete──→ toggles pin or deletes preset
PresetsScreen ──reorder mode──→ up/down buttons on each card, calls reorderPresets()

PresetDetailScreen ──back──→ pops back to PresetsScreen
PresetDetailScreen ──edit button──→ navigates to PresetsScreen?editId={id}, opens edit sheet on arrival
PresetDetailScreen ──"START WORKOUT" button (no active session)──→ creates session, navigates to WorkoutSessionScreen
PresetDetailScreen ──"RESUME WORKOUT" button (active session)──→ navigates to WorkoutSessionScreen

PresetDetailScreen ──"FINISH WORKOUT"──→ logs all sets as PREntries, marks session complete, pops back to PresetDetailScreen
PresetDetailScreen ──back button──→ auto-pauses workout, pops back
Dashboard ──"EXERCISE HISTORY" button──→ ExerciseHistoryScreen
Dashboard ──"WORKOUT HISTORY" button──→ WorkoutHistoryScreen

ExerciseHistoryScreen ──back──→ pops back to Dashboard
WorkoutHistoryScreen ──back──→ pops back to Dashboard
WorkoutHistoryScreen ──delete (double confirm)──→ removes from history

WorkoutSessionScreen ──PAUSE/RESUME──→ toggles isPaused, accumulates pausedDurationMs on resume
WorkoutSessionScreen ──FINISH──→ logs all completed sets as individual PREntries, marks session complete
WorkoutSessionScreen ──back/leave──→ auto-pauses workout (DisposableEffect onDispose), preserves session for resume
```

Bottom navigation bar is visible on `home`, `dashboard`, `presets`, `goals`, `weight`, `calendar`, `settings`, and `preset_detail/{presetId}` routes. It is hidden on `live_run`, `workout_session`, and other detail routes. FAB is visible on `dashboard` and `weight` screens.

---

## 7. Design System

### Color Palette (`ui/theme/Color.kt`)

```kotlin
val Background           = Color(0xFF050A18)  // Deep dark navy
val Surface              = Color(0xFF0D1526)  // Slightly lighter navy
val CardBackground       = Color(0x990F1C35)  // Glassmorphism (60% alpha)
val PrimaryAccent        = Color(0xFF00F5FF)  // Neon cyan (default accent)
val SecondaryAccent      = Color(0xFF7B2FFF)  // Electric purple (default secondary)
val SuccessPurple        = Color(0xFFB026FF)  // Neon purple (PRs, celebrations)
val TextPrimary          = Color(0xFFE8F4FD)  // Near-white
val TextSecondary        = Color(0xFF6B8CAE)  // Muted blue-gray
val GoalComplete         = Color(0xFF00FF85)  // Neon green (completed goals, pinned accent)
val GoalReachedColor     = Color(0xFFFF6B00)  // Neon orange (exercise PR meets or exceeds goal)
val GoalWarningColor     = Color(0xFFFFD700)  // Yellow (near-goal indicator)
val PinnedAccent         = Color(0xFF00FF85)  // Neon green (pinned card primary accent)
val PinnedAccentSecondary = Color(0xFFFF003C) // Neon red (pinned card secondary accent)

val Magenta = Color(0xFFFF2D78)         // Running feature accent (fixed)
val MagentaDim = Color(0xFFC4005A)      // Running feature secondary (fixed)

val TierRawCarbon = Color(0xFF6B8CAE)       // Muted steel gray
val TierBootSequence = Color(0xFF4A6FA5)    // Muted blue
val TierStructuralPatch = Color(0xFF3D5A73) // Slate teal
val TierOverclockV1 = Color(0xFF5C3D8F)     // Dark purple
val TierNeuralBridge = Color(0xFF7B2FFF)    // Electric purple (same as SecondaryAccent)
val TierKineticFlow = Color(0xFF3B3B8F)     // Deep indigo
val TierGravityRebel = Color(0xFF00F5FF)    // Neon cyan (same as PrimaryAccent)
val TierQuantumLever = Color(0xFF00FFFF)    // Bright cyan
val TierSystemOverride = Color(0xFFB026FF)  // Neon purple (same as SuccessPurple)
```

### Typography (`ui/theme/Type.kt`)

- **Titles & PR numbers:** `FontFamily.Monospace` (stand-in for Orbitron)
- **Body text:** `FontFamily.Default` (system font)
- The original spec called for Google Fonts "Orbitron", but it was changed to `Monospace` because the downloadable fonts approach required complex certificate resource files. To use real Orbitron, add `.ttf` files to `res/font/` and change `FontFamily.Monospace` to `FontFamily(Font(R.font.orbitron))`.

### Visual Style

- **Cards:** Semi-transparent background (`CardBackground`) with 16dp rounded corners and a 1dp gradient border (cyan → purple by default, custom border brushes supported). Border colors can be customized per-system via `AppearanceSettings` and are read from `LocalAppearance.current`. Applied via `GlowingCard` composable.
- **Buttons:** Dark translucent background with a gradient border overlay. 16dp rounded corners. Accent color is read from `LocalAppearance.current.systemAccentColor`. Applied via `NeonButton` composable.
- **Background:** Deep navy (`Background`) with a slowly moving grid of ultra-faint cyan lines (`GridBackground` composable, drawn entirely in `drawBehind` to avoid recomposition).
- **Progress rings:** Canvas-drawn arcs with sweep gradient (customizable colors). Default colors are read from `LocalAppearance.current.exerciseAccentColor`/`exerciseSecondaryColor`. Animated on mount via `Animatable`. Applied via `AnimatedRing`.
- **Bottom nav:** No labels, just icons. Seven items: Home (home icon), Dashboard (fitness center icon), Presets (list icon), Goals (track changes icon), Weight (monitor weight icon), Calendar (calendar month icon), Settings (settings icon). Selected item has a 3dp gradient underline using `LocalAppearance.current` colors.
- **Status bar / nav bar:** Dark colors set in `themes.xml`.
- **Dynamic colors:** All accent colors are driven by `AppearanceSettings` stored in `AppSettings.appearance`. A `CompositionLocal` named `LocalAppearance` provides the current settings to every composable tree via `CompositionLocalProvider` in `MainActivity`. Eight extension properties on `AppearanceSettings` (e.g. `exerciseAccentColor`, `systemAccentColor`) convert `Long` ARGB values to Compose `Color`.

---

## 8. Screens — Detailed Description

### 8.1 DashboardScreen

- **Animated background:** `GridBackground()` composable with slowly drifting grid lines.
- **Title:** "PR TRACKER" in `displayLarge` Monospace with a glowing cyan underline drawn via `drawBehind`. Below the title, shows equipped pet emojis with "+X% XP" text when pets are equipped.
- **Search bar:** `OutlinedTextField` at top with search icon, filters exercises by name in real time.
- **Exercise grid:** `LazyVerticalGrid` with 2 columns. Exercises are split into two sections:

  **PINNED section** (only visible if any exercises are pinned):
  - "PINNED" section header in neon green (`PinnedAccent`)
  - Pinned exercise cards with green/red color scheme
  - A glowing divider line with gradient from neon green to neon red

  **ALL EXERCISES section:**
  - "ALL EXERCISES" section header in muted blue-gray
  - Unpinned exercise cards with standard cyan/purple color scheme

  **ExerciseCard** layout:
  - Pin/unpin icon (filled pin when pinned, outlined when unpinned)
  - Goal icon (flag) — opens `SetGoalDialog` for setting/editing/removing per-exercise target
  - Delete icon (trash) — opens confirmation dialog, calls `viewModel.deleteExercise(id)`
  - Streak indicator (purple star) if entries exist in the current week
  - Exercise name (truncated with ellipsis)
  - Current PR value in large Monospace (`displayLarge`), suffixed with `s` for hold type; turns neon orange (`GoalReachedColor`) when PR >= goal
  - Type label: "REPS" or "HOLD"
  - Small `AnimatedRing` showing `(goalProgress)` — previously` (PR % 100) / 100` — now capped with a tick mark at 100% and displays percentage text; colors dynamically switch: cyan/purple (no goal), green/red (goal set, not reached), green/green (goal reached); a pulse glow animation runs on the card border when the goal is reached

- **Color scheme switching:** When `exercise.isPinned` is true, the card uses `PinnedAccent` (neon green) for the card border, pin icon, PR number, and progress ring, and `PinnedAccentSecondary` (neon red) for the border's second gradient color. Unpinned cards use the original `PrimaryAccent` (cyan) and `SecondaryAccent` (purple). A single `ExerciseCard` composable handles both states via a conditional `pinnedAccent` / `pinnedAccentSecondary` pattern — no duplicate composables.

- **Drag-and-drop reordering:** Long-press on a card enters drag mode. The dragged card scales up (1.05x) with a `graphicsLayer` transform and follows the finger. A semitransparent ghost placeholder remains in the original grid slot. On drop, the nearest card of the same pin status is found by distance (using `LazyGridState.layoutInfo.visibleItemsInfo` for live content-space coordinates), and `viewModel.swapExercises()` is called to swap sort orders. Boundary enforcement: if the user tries to drag a pinned card into the unpinned area (or vice versa), the card shakes using an `Animatable` offset animation triggered by `shakeTrigger`, and the swap is rejected. Only cards with the same `isPinned` status can be reordered together.

- **Running section:** A full-width `RunningCard` composable sits below the exercise grid showing best distance, best pace, total runs, total distance, and calories. It has "VIEW ALL" (navigates to `RunHistoryScreen`) and "LOG A RUN" (navigates to `LogRunScreen`) interactive elements.
- **Empty state:** "NO EXERCISES YET\n— ADD ONE —" centered text.
- **No matches state:** "NO MATCHES" centered text when search query yields no results.
- **Fixed header:** The "PR TRACKER" title and search bar are always visible in a fixed Column above the grid (no animated hide-on-scroll). The grid uses `Modifier.weight(1f)` to fill remaining space.
- **Bottom buttons:** Two side-by-side `NeonButton`s below the grid: "EXERCISE HISTORY" (navigates to `ExerciseHistoryScreen`) and "WORKOUT HISTORY" (navigates to `WorkoutHistoryScreen`).
- **FAB:** Bottom-right, glowing cyan `+` button, navigates to `AddExerciseScreen`.

### 8.2 AddExerciseScreen

- **Back button** (arrow icon) in top-left.
- **Title:** "NEW EXERCISE" in `headlineLarge`.
- **Name field:** `OutlinedTextField` with cyan cursor, cyan focus border, dark container. Triggers auto-classification via `ExerciseClassifier.classify()` after 150ms debounce.
- **Type toggle:** Two pill-shaped buttons side by side: "REPS" and "HOLD (seconds)". Selected one fills with cyan tint, unselected is dim.
- **Difficulty grid:** Four pill buttons for EASY/MEDIUM/HARD/EXTREME. The `LaunchedEffect` auto-classifies the difficulty from the exercise name (shown as "AUTO" label). Tapping a pill manually overrides the auto-classified value.
- **Goal field:** Numeric `OutlinedTextField` for optional target. Label: "Set a target to track progress (optional)".
- **Confirm button:** `NeonButton` with text "CONFIRM". Disabled if name is blank.
  - On click: generates UUID, creates `Exercise(name, type, emptyList(), difficulty = picked value, sortOrder = maxOrder + 1)`, calls `viewModel.addExercise()`, pops back to Dashboard.

### 8.3 LogEntryScreen

- **Back button** (arrow icon).
- **Exercise name** as headline.
- **"NEW PR!" badge:** Shows in purple when the current input value exceeds the existing max. Has a soft purple background glow drawn via `drawBehind`.
- **Large animated value:** Shows `value` or `${value}s` in 64sp font. Color animates between cyan and purple depending on PR status using `animateColorAsState`.
- **Controls:**
  - Down arrow button (decrements value, minimum 0)
  - Center text field (tap-to-type, numeric keyboard, max 100dp wide)
  - Up arrow button (increments value)
  - Timer icon button (only for "hold" type exercises) — navigates to HoldTimerScreen so the user can time their hold and auto-fill the result
- **Current PR display:** "CURRENT PR: X reps" or "CURRENT PR: Xs" in body text.
- **Note field:** Optional text field.
- **"LOG IT" button:** `NeonButton`, disabled if value is 0.
  - On click:
    1. Triggers haptic feedback if enabled — long vibration (300ms) for new PR, short (50ms) otherwise. Uses `VibratorManager` on API 31+, falls back to `VIBRATOR_SERVICE` on older versions via `@Suppress("DEPRECATION")`
    2. Creates `PREntry(UUID, value, currentTimeMillis, note)` and calls `viewModel.logEntry()`
    3. If new PR and value > 0: shows `PRCelebrationOverlay` (blur + dim + Lottie + text)
    4. Otherwise: pops back to ExerciseDetailScreen
- **PR overlay:** Blurs the background content (12dp Gaussian blur on API 31+, gracefully ignored on older devices), adds a 40% black dim overlay, then shows the `PRCelebrationOverlay` with purple flash + confetti Lottie animation.

### 8.4 ExerciseDetailScreen

- Collects exercises as state from ViewModel so it recomposes when data changes.
- **Title row:** Back arrow, exercise name, difficulty badge, pencil (rename) icon, delete (trash) icon.
  - **Rename:** Pencil icon opens an `AlertDialog` with an `OutlinedTextField` pre-filled with the current name. Rename button disabled if blank or unchanged. Calls `viewModel.renameExercise(exerciseId, newName)` which cascades the name change to goals, workout presets, active session, and workout history.
- **Line chart** (MPAndroidChart via `AndroidView`):
  - Dark transparent background
  - Neon cyan line, cubic bezier mode, filled with low-alpha cyan
  - Glowing dots at data points
  - X axis: date labels (MM/dd format), Y axis: values (always starts at 0, no upper limit)
  - Pinch-to-zoom, drag, and touch enabled
  - Animate on mount (800ms)
  - Shows "No entries yet" placeholder if empty
- **History list:** `Column` + `forEachIndexed` of `EntryRow` items inside a `verticalScroll` container (newest first). Each row:
  - Date formatted as "MMM dd, yyyy HH:mm"
  - Optional note text
  - Value with unit suffix; if that entry was a PR, the value text and trophy icon use `pinnedAccentColor` (green), otherwise `exerciseAccentColor` (cyan)
  - Green trophy icon (`EmojiEvents`) with green tint if that entry was a PR
  - Small ✕ delete button — calls `viewModel.deleteEntry(exerciseId, entry.id)` and UI updates immediately because it collects state
- **Sticky bottom buttons:** The screen layout uses a two-layer Column structure: a `weight(1f)` scrollable section containing the header, chart, history label, and entry list, plus a fixed bottom section outside the scroll for the action buttons. For "reps" exercises, a single full-width `NeonButton` labeled "LOG ENTRY" navigates to `LogEntryScreen`. For "hold" exercises, two side-by-side `NeonButton`s appear: "LOG ENTRY" (navigates to `LogEntryScreen`) and "TIMER" with a timer icon (navigates to `HoldTimerScreen`). The buttons never scroll away regardless of entry count.
- **Delete exercise:** Trash icon in header opens confirmation dialog. On confirm, calls `viewModel.deleteExercise(id)` and pops back to Dashboard.

### 8.5 SettingsScreen

- **Scrollable layout:** The entire content Column uses `.verticalScroll(rememberScrollState())` so all sections (including the data transfer section at the bottom) are reachable.
- **Title:** "SETTINGS" in `displayLarge`.
- **"APPEARANCE" button:** Clickable `GlowingCard` with palette icon that navigates to `AppearanceScreen` for color customization. Uses `appearance.systemAccentColor` for the icon tint.
- **"NOTIFICATIONS" button:** Clickable `GlowingCard` with bell icon that navigates to `NotificationSettingsScreen`. Positioned immediately below the APPEARANCE button.
- **Haptic feedback toggle:** Switch inside a `GlowingCard`. Reads/writes `appSettings.hapticEnabled` field through `viewModel.toggleHaptic()`. Persisted to JSON via `AppSettings` — survives app restarts.
- **Mascot speech toggle:** Switch inside a `GlowingCard`. Reads/writes `appSettings.speechesEnabled` field through `viewModel.toggleSpeeches()`. Controls whether the HomeScreen robot mascot plays voice lines on tab entry.
- **Weight unit toggle:** Two pill buttons ("KG" / "LBS") inside a `GlowingCard`. Reads/writes the `weightUnit` field in `AppSettings` via `viewModel.setWeightUnit()`. When changing to "LBS", all weight values in WeightScreen are multiplied by 2.20462; the stored JSON always uses kilograms.
- **Target weight field:** Optional `OutlinedTextField` to set a target body weight. When set, a horizontal dashed green line appears on the WeightScreen chart at the target value (auto-converted to current display unit).
- **"EXPORT DATA" button:** Opens a type picker dialog with APP / PET / BOTH pills showing data summary per type. Exports to Downloads with type-specific filenames (APP → `prtracker_app_backup.json`, PET → `prtracker_pets_backup.json`, BOTH → `prtracker_backup.json`). Uses `MediaStore.Downloads` (API 29+) or direct file copy (older). Shows success dialog.
- **"CLEAR ALL DATA" button:** Confirmation dialog, then calls `viewModel.clearAllData()` (clears both exercises and goals).
- **"WIPE PET DATA" button:** Red button with 3x confirmation dialog (Step 1: list what's wiped, Step 2: are you sure, Step 3: final confirmation). Calls `viewModel.clearPetData()`.
- **"DEVICE SYNC" section:** Two buttons inside a `GlowingCard`:
  - **"SHARE DATA"** — navigates to `SyncExportScreen` which writes `prs.json` to cache and opens Android's share sheet to send it to another device
  - Import info text: "To import: open a prtracker_backup.json file from your file manager"
- **Version text:** "PR TRACKER v1.0" at the bottom.

### 8.6 GoalsScreen

- **Animated background:** `GridBackground()` composable.
- **Title:** "GOALS" in `displayLarge` Monospace with a glowing cyan underline drawn via `drawBehind`.
- **Goal list:** `LazyColumn` of `GoalCard` composables stacked vertically with 12dp spacing.
- **GoalCard** (horizontal rectangle, ~110dp height, full width):
  - LEFT: Circular progress ring (72dp, Canvas-drawn, cyan for in-progress, neon green when complete) with percentage text ("74%") in Monospace
  - CENTER: Exercise name in Monospace cyan, progress text ("143 / 200 reps"), period badge pill (DAILY/WEEKLY/MONTHLY in electric purple), time-remaining text ("Resets in Xh Ym" / "X days left this week" / "X days left in [Month]")
  - RIGHT: Delete icon button (trash, with confirmation dialog)
  - When 100% complete: neon green glowing border (`GoalComplete`), "COMPLETED" badge in top-right corner, ring turns green
  - Progress and time-remaining values are computed via `derivedStateOf` in the LazyColumn's `items` block for performance
- **Empty state:** "NO GOALS YET\n— ADD ONE —" centered text.
- **FAB:** Bottom-right, glowing cyan `+` button, navigates to `AddGoalScreen`.

- **Rank Evaluator button:** Full-width styled button between the goals list and the Leverage Index section. Uses the same style as `NeonButton` (cyan accent 15% alpha fill, 16dp rounded corners, gradient border overlay) with `Icons.Default.Stars` on the left and "RANK EVALUATOR" in monospace uppercase. On click, navigates to `RankScreen` via `navController.navigate(Routes.RANK)`. The gradient border `Brush` is cached in `remember { }`. Vertical spacing of 12dp on both sides is provided by the `LazyColumn`'s `Arrangement.spacedBy(12.dp)`.

- **Pet Dice button:** Full-width styled button below the Rank Evaluator button. Uses `DiceIcon` (or `Casino` icon) with "PET DICE" label. On click, navigates to `DiceRollScreen` via `navController.navigate(Routes.DICE_ROLL)`. Shows a coin balance badge with the player's current coins.

#### RSI Section (below goals list)

- **Section header:** Divider line + "⚡ LEVERAGE INDEX" title + "TRUE STRENGTH RELATIVE TO BODY WEIGHT" subtitle
- **Loading state:** `RsiLoadingDots` — three pulsing cyan dots (infinite cross-fade animation)
- **Empty state:** `RsiEmptyState` — GlowingCard with "RSI TRACKING UNAVAILABLE" message and "GO TO WEIGHT LOG" NeonButton
- **RsiCard** (horizontal, ~120dp, full width):
  - LEFT: `MiniSparkChart` (Canvas line chart, last 5 telemetry points, purple dots + lines)
  - CENTER: Exercise name in cyan, "CURRENT RSI: X pts" in monospace, delta change (green/red) + status badge pill (STABLE/OVERLOAD/DELOAD with colored backgrounds), "LAST LOGGED AT: X.X kg"
  - RIGHT: Type badge ("REPS" / "HOLD") in purple
- **Data lifecycle:** `LaunchedEffect(Unit)` at screen entry calls `viewModel.loadAllTelemetry()` on Dispatchers.Default; `DisposableEffect` calls `viewModel.clearAllTelemetry()` when leaving
- **`MiniSparkChart`** composable: Canvas-drawn line chart (SecondaryAccent), 5-point window, min/max normalization, round caps, dot markers at each point

### 8.7 AddGoalScreen

- **Back button** (arrow icon) in top-left.
- **Title:** "NEW GOAL" in `headlineLarge`.
- **Exercise selector:** Clickable dropdown box that expands an inline `LazyColumn` of all exercises (showing name + type). Selected exercise is highlighted in cyan.
- **Target field:** Numeric `OutlinedTextField` (disabled until exercise is selected). Label changes dynamically: "Target Reps" for reps exercises, "Target Seconds" for hold exercises.
- **Period selector:** Three pill toggle buttons side by side: "DAILY" / "WEEKLY" / "MONTHLY". Selected one fills with cyan tint.
- **Preview line:** Displays "Complete X reps every [period]" inside a `GlowingCard` when both exercise and target are filled.
- **"SET GOAL" button:** `NeonButton`, disabled until all fields are valid (exercise selected, target > 0). On click: generates UUID, creates `Goal`, calls `viewModel.addGoal()`, pops back to GoalsScreen.

### 8.8 WeightScreen

- **Animated background:** `GridBackground()` composable.
- **Title:** "WEIGHT" in `displayLarge` Monospace with a glowing cyan underline drawn via `drawBehind`.
- **Stats row:** Three metric cards in a horizontal row inside `GlowingCard`:
  - **Current weight:** Most recent entry value, or "---"
  - **Change:** Difference between latest and previous entry (green up/down arrow, purple if 0)
  - **Min/Max:** Lowest and highest weight recorded
- **Line chart** (MPAndroidChart via `AndroidView`):
  - Dark transparent background
  - Neon cyan line with cubic bezier mode and filled area
  - X axis: date labels (MM/dd), Y axis: weight values in current unit
  - If `targetWeight` is set in settings, a horizontal dashed green line is drawn at the target
  - Pinch-to-zoom, drag, and touch enabled
- **History list:** `LazyColumn` of weight entries (newest first). Each row:
  - Date formatted as "MMM dd, yyyy"
  - Weight value with unit suffix (kg or lbs)
  - Optional note text
  - Swipe-to-delete with red background and trash icon
- **Empty state:** "NO WEIGHT ENTRIES YET\n— LOG ONE —" centered text.
- **FAB:** Bottom-right, glowing cyan `+` button, navigates to `LogWeightScreen`.

### 8.9 LogWeightScreen

- **Back button** (arrow icon) in top-left.
- **Title:** "LOG WEIGHT" in `headlineLarge`.
- **Date picker:** Clickable date text that opens a `DatePickerDialog` to set the date for the entry (defaults to today).
- **Weight controls:**
  - Down arrow button (decrements by 0.1)
  - Large weight value display in 64sp Monospace font with unit suffix
  - Up arrow button (increments by 0.1)
- **Note field:** Optional text field.
- **"LOG WEIGHT" button:** `NeonButton`, disabled if weight is 0.0.
  - On click: Triggers short 50ms haptic feedback if enabled (uses `VibratorManager` on API 31+, `VIBRATOR_SERVICE` fallback), then creates `WeightEntry(UUID, weight, date, note)`, calls `viewModel.addWeightEntry()`, pops back to WeightScreen.

### 8.10 SyncExportScreen

- **Back button** (arrow icon) in top-left.
- **Title:** "SYNC EXPORT" in `headlineLarge`.
- **Type selection:** Three pill buttons at top: APP DATA, PET DATA, BOTH. Shows data summary per selected type with counts.
- **Export flow:** On screen entry or type selection, the selected data is serialized to JSON and written to the app's cache directory. Android's share sheet (`Intent.ACTION_SEND` with `application/json` mime type) is then opened, allowing the user to send the file to another device via Bluetooth, email, messaging apps, cloud storage, etc. The shared file is named per type: `prtracker_app_backup.json` (APP), `prtracker_pets_backup.json` (PET), or `prtracker_backup.json` (BOTH).
- **FileProvider:** The cache file is shared via the app's `FileProvider` (configured in `file_paths.xml` for `cache/share/` path) to comply with Android's `FLAG_GRANT_READ_URI_PERMISSION` requirement.
- **Success state:** After the share sheet completes, shows a brief confirmation toast or snackbar.
- **Complete/back:** Pops back to SettingsScreen on back press or after share completes.

### 8.11 SyncImportScreen

- **Back button** (arrow icon) in top-left.
- **Title:** "SYNC IMPORT" in `headlineLarge`.
- **Entry point:** Launched via `ACTION_VIEW` intent filter for `application/json` mime type. Users can open `.json` backup files from any file manager, and the app will open directly to this screen.
- **Data parsing:** The incoming JSON file is read and deserialized into a `StorageData` object. A loading indicator is shown while parsing.
- **Confirmation dialog:** After successful parsing, a confirmation dialog appears showing all 6 data type counts (exercises, goals, weight, presets, runs, workouts):
  - **"Merge (Recommended)"** — merges incoming exercises/goals/weight/presets/runs/workouts with existing data by ID deduplication. For matching IDs: keeps higher goal value, keeps local `isPinned`/`sortOrder`. Exercises not present locally are appended. Run entries deduped by ID, merged sorted by date, running PRs recomputed.
  - **"Replace"** — completely replaces all local data with the incoming data (all 6 types + settings + restDays).
  - "Cancel" — discards received data and returns to dashboard.
- **Success state:** After import, shows "IMPORT COMPLETE" green text with all 6 data type counts, then navigates to Dashboard.
- **Error handling:** If the JSON file is malformed or cannot be parsed, an error message is shown with a "GO BACK" button.

### 8.12 RankScreen

- **Animated background:** `GridBackground()` composable.
- **First-frame deferred alpha:** Content fades in (500ms tween) after one `withFrameNanos` delay.
- **Terminal header:** `"> RANK EVALUATOR"` in small Monospace with a blinking cursor (`|`) animated via `rememberInfiniteTransition` (600ms reverse alpha).
- **Active tier card:** `GlowingCard` with:
  - Large tier number in Monospace (`%02d`), with elevated shadow (`graphicsLayer` shadowElevation) in the tier color
  - Tier name below in Monospace
  - When `currentTier == 0`: shows `"--"` / `"UNRANKED"` / `"// awaiting input data"` in secondary color, no border brush
  - Animated progress bar (6dp rounded, 1000ms tween): background track in dim secondary, fill bar with gradient from `SecondaryAccent` to tier color (cyan for tier 7, which uses `PrimaryAccent`)
  - "TIER X / 10" label
- **Next unlock section:** Visible when `currentTier < 10`:
  - `"> NEXT: {tierName}"` header
  - GlowingCard with `CriterionRow` composables separated by dim dividers:
    - CheckCircle icon (green) or Lock icon (red) per criterion
    - Criterion label + "your best: X" text
    - Right-aligned pill badge showing "userBest / required" in green/red/gray
- **Full progression matrix:** GlowingCard listing all 10 tiers in reverse order (10 → 1):
  - Each `TierMatrixRow` has a left border: solid line for unlocked tiers, dashed for next tier, none for locked
  - Tier number in Monospace, tier name, CheckCircle (green) or Lock icon (dimmed)
  - Opacity: 100% unlocked, 80% next, 30% locked
  - Colors mapped via `tierNumberToColor()` (7→PrimaryAccent, others→their tier color)
  - Names mapped via `tierNumberToName()` (e.g. "RAW CARBON", "SYSTEM OVERRIDE")
- **Footer:** `"// criteria: achieve any 2 of 3 per tier to unlock"` in small Monospace, centered.
- **Tier evaluation:** `viewModel.tierResult` derived via `TierEvaluator.evaluate()` from exercises. 10 tiers (RAW_CARBON → SYSTEM_OVERRIDE), each with 3 criteria. Tier unlocked when ≥2 criteria met. Exercise name matching via alias `contains()` then Levenshtein distance fallback.
- **Data flow:** `viewModel.tierResult` is a `StateFlow` derived via `_exercises.map { TierEvaluator.evaluate(it) }` with `WhileSubscribed(5000)` sharing. No save/load needed — computed on the fly.
- **Navigation:** Accessed via the "RANK EVALUATOR" button on the GoalsScreen. The bottom nav no longer has a Rank icon — it was moved to declutter the nav and give remaining icons more space.

### 8.13 CalendarScreen

- **Animated background:** `GridBackground()` composable.
- **Title:** "SYSTEM CHRONO // OVERVIEW" in `displayLarge` Monospace with a glowing cyan underline drawn via `drawBehind`.
- **Streak banner card:** Full-width `GlowingCard` with:
  - Flame emoji + "CURRENT STREAK: X DAYS" in neon cyan Monospace, prominent size
  - Below it in smaller secondary text: "(X rest days this month)" — count of `restDays` entries in the currently displayed month
  - If streak is 0: shows "NO ACTIVE STREAK" in secondary color
  - The streak number animates in with `animateIntAsState` (count up from 0 to X over 800ms on first composition)
- **Biomechanical balance engine card:** Full-width `BiomechanicalRadarCard` (from `ui/components/BalanceEngine.kt`) placed between the streak banner and the movement analysis section. Displays a pentagonal radar chart (5 axes: VP/HP/VPush/HPush/CL) showing the user's movement pattern balance computed from logged exercise PRs. Shows a "Structural Deviation" percentage and asymmetry warning when push/pull imbalance exceeds 25%. See Section 10 component description for details.
- **Movement analysis section:** A private `MovementAnalysisSection` composable placed between the balance engine card and the month navigation row. Contains:
  - "MOVEMENT ANALYSIS" label in small Monospace
  - `ExposedDropdownMenuBox` to select a workout preset (shows "NO PRESETS — CREATE ONE FIRST" when empty)
  - "ANALYZE MOVEMENT →" button that navigates to `PresetAnalysisScreen` with the selected preset's ID (disabled when no preset is selected, with gradient border overlay when enabled)
- **Month navigation:** A row with:
  - Left arrow button (`<`) — go to previous month
  - Current month + year in Monospace, centered with `AnimatedContent` horizontal slide animation: "MAY 2026"
  - Right arrow button (`>`) — go to next month, disabled and dimmed when viewing current month or future
  - Future months beyond the current month are blocked
- **Day of week header row:** A fixed row of 7 labels: `SU · MO · TU · WE · TH · FR · SA` in small Monospace, secondary color (#6B8CAE)
- **Calendar grid:** A 7-column `LazyVerticalGrid` with:
  - `key = { dateString }` on every item for performance
  - Padding cells at the start to align day 1 to the correct weekday, computed using `java.util.Calendar`
  - Trailing padding cells to fill the last incomplete row
  - `DayCell` composable — one per calendar day, receiving: `dateString`, `dayNumber`, `cellType`, `isToday`

  **CellType enum** with 5 visual states:

  | Type        | Visual                                                                                                                                             |
  | ----------- | -------------------------------------------------------------------------------------------------------------------------------------------------- |
  | `WORKOUT`   | Dark card background (#0F1C35), 1.5dp neon cyan (#00F5FF) glowing border, day number in white Monospace, small filled neon cyan dot centered below |
  | `REST`      | Filled container with electric purple (#7B2FFF) at 40% alpha, day number in electric purple Monospace, pause symbol (⏸) below                      |
  | `MISSED`    | No border/no background, day number in dim tech gray (#444444), ultra-faint neon red (#FF003C) 1dp underline below the number                      |
  | `FUTURE`    | No border/no background, day number in very low opacity white (20% alpha), not tappable                                                            |
  | `EMPTY_PAD` | Invisible `Box()` composable used as spacer                                                                                                        |

  **TODAY** cells get an extra neon cyan ring/outline (2dp, slightly larger than the card) regardless of type; if today is `MISSED`, text color is overridden to white.

  **Tapping a day cell:**
  - `WORKOUT` day: shows a dark-themed `AlertDialog` popup listing the exercises logged that day with their max value
  - `REST` day: shows popup confirming "Rest day — chain preserved" in purple
  - `MISSED` and `FUTURE` days: no interaction

- **Bottom action button:** A full-width button:
  - If today is NOT a rest day: label "MARK TODAY AS REST DAY" with filled neon cyan styling
  - If today IS already a rest day: label "UNMARK REST DAY" with outlined neon red styling
  - Tapping triggers `viewModel.toggleTodayAsRestDay()` which toggles today's rest day status in `_restDays`, saves to JSON, and emits a haptic event via `_hapticEvent` SharedFlow
  - Haptic feedback is consumed in the screen via `LaunchedEffect` collecting `viewModel.hapticEvent`, using `VibratorManager` on API 31+ (fallback to `VIBRATOR_SERVICE`), and respects `appSettings.hapticEnabled`

- **Streak animation:** Uses `animateIntAsState` with 800ms tween for the count-up animation on first composition.

- **Performance:** `workoutDays` set is computed once per recomposition via `remember(exercises)`. `restDaySet` is computed via `remember(restDays)`. The day cells grid uses explicit `key = { dateString }` for Compose identity tracking.

### 8.14 HoldTimerScreen

- **Animated background:** `GridBackground()` composable.
- **Title row:** Back arrow (arrow icon) in top-left, exercise name in `headlineMedium` Monospace centered, "HOLD TIMER" subtitle in `labelLarge` TextSecondary.
- **Timer ring:** 240dp `Canvas` circle with a full 360-degree track arc (`PrimaryAccent` at 15% alpha) and a progress arc sweeping clockwise from -90 degrees. The progress arc uses a sweep gradient from `PrimaryAccent` to `SecondaryAccent` (cached in `remember { }`). Sweep angle is calculated as `(elapsedSeconds / targetSeconds).coerceAtMost(1f) * 360f` when a target is set, or `(elapsedSeconds % 60) / 60f * 360f` (cosmetic 60-second loop) when no target is set. Inside the ring, elapsed time is displayed as `"MM:SS"` in 56sp Monospace TextPrimary, with "SECONDS" in `labelSmall` TextSecondary below.
- **Control buttons:** A centered `Row` with 24dp spacing:
  - **START/PAUSE** — 64dp circular `Box` with `PrimaryAccent` at 20% alpha and 2dp PrimaryAccent border. Shows `PlayArrow` when stopped, `Pause` when running. Toggles `isRunning` state.
  - **RESET** — 48dp circular `Box` with `TextSecondary` at 10% alpha, `Refresh` icon. Resets elapsed time to 0, stops timer, clears target-hit state.
  - **LOG IT** — 48dp circular `Box` with `GoalComplete` at 20% alpha and 2dp GoalComplete border (only when `elapsedSeconds > 0`, otherwise invisible styling). Shows `Check` icon. Triggers haptic feedback — long 300ms vibration for new PR, short 50ms otherwise (uses `VibratorManager` on API 31+, `VIBRATOR_SERVICE` fallback). Calls `viewModel.logEntry(exerciseId, PREntry(...))` with the elapsed seconds. If the value is a new PR (checked via `viewModel.isNewPR` before logging), shows `PRCelebrationOverlay` before popping back; otherwise pops back directly.
- **Target time row:** "TARGET" label in `labelSmall`, then `Remove`/`Add` IconButtons with a center `OutlinedTextField` showing the target value (placeholder `"0"` when no target is set). The text field allows direct numeric input for a custom target value. `targetSeconds` is synced bidirectionally with the field. Increments/decrements by 5 seconds (min 0, max 300) via the buttons.
- **Timer logic:** A single `elapsedMillis: Long` state variable. When `isRunning` is true, a `LaunchedEffect(isRunning)` loops with `delay(100L)` and `elapsedMillis += 100L`. Displayed as `elapsedSeconds = (elapsedMillis / 1000L).toInt()`.
- **Target reached behavior:** When running, target > 0, and elapsed >= target, triggers a one-shot 50ms vibration (only if `appSettings.hapticEnabled`), sets `showTargetFlash = true` for 500ms to swap the progress arc color to `GoalComplete` (solid green sweep gradient), then resets.
- **Screen wake lock:** `DisposableEffect(Unit)` sets `WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON` on the Activity's window on entry and clears it on dispose.
- **PR overlay:** Same pattern as LogEntryScreen — `PRCelebrationOverlay` with auto-dismiss after 2.5s, pop back on dismiss.

### 8.15 AppearanceScreen

- **Animated background:** `GridBackground()` composable.
- **Back button** (arrow icon) in top-left, tinted with `appearance.systemAccentColor`.
- **Title:** "APPEARANCE" in `displayLarge` with `systemAccentColor`.
- **Preset themes section:** `LazyRow` of `ThemeCard` composables showing each of the 6 presets (SYSTEM DEFAULT, BLOOD & IRON, TOXIC OVERRIDE, VOID PROTOCOL, SOLAR STORM, ICE PROTOCOL) as small swatches with name and description. Selected theme has a gradient border and brighter background. Tapping a theme calls `viewModel.applyTheme(theme)`.
- **Custom colors section:** Four `ColorSectionCard` composables, one per accent system:
  - **EXERCISES** — cards, charts, PR numbers; primary/secondary sliders
  - **PINNED EXERCISES** — pinned card borders & accents; primary/secondary sliders
  - **RUNNING** — running cards, badges & stats; primary/secondary sliders
  - **SYSTEM / UI** — buttons, nav bar, icons, borders; primary/secondary sliders
  - Each `ColorSectionCard` shows current color swatches (primary/secondary) and a `LazyRow` of 20 `NEON_PALETTE` swatches (from `ColorPalette.kt`). Tapping a swatch assigns the color to the primary slot, or cycles to secondary if already primary. Changes set `activeThemeId` to `"custom"`.
- **Preview card:** `PreviewCard` composable at the bottom showing a live preview row with BUTTON, REPS, PIN, RUN labels colored with the current accent settings. Uses a `GlowingCard` with a gradient border matching system accent colors.
- **Data flow:** All color changes go through `viewModel.updateAppearance(updated)` which updates `_appSettings.value.appearance` and persists to JSON via `saveData()`. The `LocalAppearance` CompositionLocal is provided in `MainActivity` from `appSettings.appearance`, so all composables consuming it react immediately.

### 8.16 WorkoutPresetsScreen

- **Animated background:** `GridBackground()` composable.
- **Title:** "WORKOUT PRESETS" in `displayLarge` Monospace with system accent color.
- **Count badge:** Small pill showing `"X PRESETS"` in secondary color below the title.
- **Reorder mode toggle:** `SwapVert` icon button next to count badge. When active, shows "REORDERING" label in system accent color, and each preset card gets up/down arrow buttons for manual reorder.
- **Empty state:** `GlowingCard` with `FitnessCenter` icon (50% opacity) and "NO PRESETS\n— TAP + TO CREATE ONE —" in secondary color.
- **Preset list:** `LazyColumn` of sorted presets (pinned first, then by sortOrder, then by createdAt descending). Each card uses `WorkoutPresetCard` composable with:
  - In reorder mode: up/down `IconButton`s on the left edge (cyan arrows, dimmed at boundaries)
  - Name (titleLarge, system accent) with `ChevronRight` icon, difficulty badge (color-coded per level: EASY=green, MEDIUM=cyan, HARD=orange, MAX EFFORT=red), tap navigates to full-screen detail
  - Description text (if present, max 2 lines)
  - Exercise list with index, name, set x target detail
  - Action buttons row: Pin toggle (star icon), Edit (pencil icon), Delete (trash icon, with confirmation dialog)
- **FAB:** Bottom-right with `Add` icon, opens create sheet.
- **Create/Edit sheet:** `ModalBottomSheet` with `PresetFormSheet` composable containing:
  - Name field, Description field (optional)
  - Difficulty picker — 4 pill buttons (EASY / MEDIUM / HARD / MAX EFFORT), each color-coded
  - Exercises section — `EditableExerciseCard` composables for each exercise (editable dropdown, target field, sets, notes, remove button with confirmation dialog), plus "ADD EXERCISE" button
  - **Add exercise sub-form:** `ExposedDropdownMenuBox` with `DropdownMenuItem` selection from `viewModel.exercises` (alphabetically sorted, restricted to existing tracked exercises). Exercise type auto-detected from `Exercise.type` field — no manual type toggle. Target reps or hold seconds field (auto-preserves matching target type when changing exercise selection). Sets field. Notes field. "CONFIRM EXERCISE" button (disabled if no exercises exist or name is blank).
  - Input validation on SAVE PRESET: all exercises must have name, sets ≥ 1, valid target > 0
  - CANCEL / SAVE PRESET buttons at bottom.
- **Delete confirmation:** `AlertDialog` with preset name, Delete/Cancel buttons.
- **Data flow:** Reads/writes `viewModel.workoutPresets` via `addPreset()`, `updatePreset()`, `deletePreset()`, `togglePresetPin()`, `reorderPresets(from, to)`. Persisted to JSON via `StorageData.workoutPresets`.

### 8.17 WorkoutPresetDetailScreen

- **Animated background:** `GridBackground()` composable.
- **Back button:** ArrowBack icon in top-left, pops nav back to presets screen.
- **Title:** Preset name in `displayLarge` Monospace with system accent color.
- **Meta card:** `GlowingCard` showing description, difficulty badge (color-coded), and predicted XP reward ("ESTIMATED XP: X,XXX" in green monospace). For "until failure" exercises, the prediction uses the exercise's current PR as the value per set; for normal exercises, it uses the target reps/hold seconds.
- **Exercise list:** Vertical list of exercises inside a `GlowingCard`. Each exercise shows:
  - Index number and name in system accent
  - Stat chips row: "S X" (sets count), target reps or hold seconds in green (`GoalComplete`), optional notes text
  - Chips separated by small dividers
- **Action buttons:** Two buttons at the bottom inside a fixed row:
  - **"EDIT"** — navigates to `PresetsScreen?editId={preset.id}`, which opens the edit sheet on arrival (calls `navController.navigate("${Routes.PRESETS}?editId=${preset.id}")`). Styled as a `NeonButton` with system accent color.
  - **"DELETE"** — opens confirmation `AlertDialog` with "Delete Preset" title and preset name, two buttons: "CANCEL" (dismiss) and "DELETE" (calls `viewModel.deletePreset(presetId)` then pops back to presets screen). Styled with red accent.
- **Empty/loading state:** If preset is not found by ID, shows a centered "Preset not found" text.
- **Navigation:** Accessed via preset card tap or from the preset detail route directly. The bottom nav bar remains visible on this screen.

### 8.18 PresetAnalysisScreen

- **Animated background:** `GridBackground()` composable.
- **Back button:** ArrowBack icon in top-left, pops back to CalendarScreen.
- **Title:** Preset name in `headlineMedium` Monospace with bold weight, then "MOVEMENT PATTERN ANALYSIS" subtitle in `bodySmall` Monospace.
- **MovementCategory enum:** `VP` (Vertical Pull), `HP` (Horizontal Pull), `CL` (Core/Legs), `VPush` (Vertical Push), `HPush` (Horizontal Push) — 5 categories for classifying exercises.
- **Classification engine (`classifyExercise()`):** Keyword-based exercise name matching with preset keyword lists for each category, falling back to `CL` as a catch-all. "Dip" is classified as VPush (Vertical Push).
- **Scoring:** Each preset exercise is scored using either the actual logged PR value (from matched `Exercise` entries via `exercise.entries.maxOf { it.value }`) or the preset target value (`maxOf(pe.targetReps, pe.targetHoldSeconds)`) if no matching exercise exists. Total raw scores per category are normalized to 0–100 based on the highest category score.
- **Balance Profile card:** `GlowingCard` containing:
  - "BALANCE PROFILE" label
  - A 220dp pentagonal radar chart drawn via `Canvas` with 10 concentric grid levels, 5 spokes, a filled data polygon (primary accent with 20% alpha fill and 80% alpha outline), and axis labels (VP/HP/CL/VPush/HPush)
  - Animation: radar data fills in over 1000ms via `animateFloatAsState` with `FastOutSlowInEasing` on first composition (controlled by a `started` flag)
  - Below the chart: a color-coded legend row for each category showing a colored dot, abbreviation (e.g. "VP"), and raw score value
- **Exercise Breakdown card:** `GlowingCard` listing each preset exercise with its classified category (color-coded dot indicator) and abbreviation badge. Rows separated by dim horizontal dividers. Each row shows exercise name and category abbreviation badge.
- **Coverage indicator:** "COVERAGE: X/5 MOVEMENT PATTERNS" text in `bodySmall` centered below the breakdown card.
- **Empty state:** If preset ID doesn't match any preset, shows "PRESET NOT FOUND" with a back button and centered text.
- **Navigation:** Accessed via the "ANALYZE MOVEMENT →" button on CalendarScreen's `MovementAnalysisSection`.

### 8.19 LiveRunScreen

- **Animated background:** `GridBackground()` composable.
- **Full-screen layout:** No bottom nav bar (hidden via `showBottomBar` logic).
- **States:** Four states managed by `RunState` enum — `IDLE`, `RUNNING`, `PAUSED`, `FINISHED`.
- **Permission handling:** On entry, `ACCESS_FINE_LOCATION` is requested via `rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission())`. If permanently denied, a centered `GlowingCard` shows "LOCATION PERMISSION REQUIRED" with an "OPEN SETTINGS" button that fires `Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)`.
- **Color scheme:** All numbers and borders use hardcoded `Color(0xFFFF2D78)` (magenta), not `LocalAppearance`. This is the fixed running accent color.
- **IDLE state:** Shows "LIVE RUN" title, "GPS TRACKING WITH FOREGROUND SERVICE" subtitle, and a full-width "START RUN" magenta `NeonButton`-style button. On click: starts `RunTrackingService` with `ACTION_START` via `startForegroundService`.
- **RUNNING state:** Collects four `StateFlow`s from `RunTrackingService.companion`:
  - `distanceMeters` — accumulated GPS distance
  - `elapsedSeconds` — ticker-based elapsed time
  - `currentPaceSecPerKm` — latest GPS-derived pace (null until >100m)
  - `isTracking` / `isPaused` — state flags
- **Live metrics display:**
  - Elapsed time as `HH:MM:SS` in 56sp Monospace magenta
  - Distance formatted as `X.XX km` or `XXX m` in 40sp Monospace white
  - Two side-by-side `GlowingCard`s: "CURRENT PACE" and "AVG PACE" showing `MM:SS /km` format, `--:-- /km` when not yet computable
- **Control buttons:**
  - RUNNING: "PAUSE" (outlined) and "FINISH" (magenta) side by side
  - PAUSED: "RESUME" (magenta) and "FINISH" (red outlined, `Color(0xFFFF003C)`) side by side
- **FINISH flow:** When tapped, sends `ACTION_STOP` to the service. An `AnimatedVisibility` card slides up from the bottom showing:
  - Final stats: distance, time, computed pace
  - Optional note `OutlinedTextField`
  - "SAVE RUN" magenta button: calls `viewModel.addRunEntry(RunEntry(...))`, then pops back to `RunHistoryScreen`
  - "DISCARD" text button: pops back without saving
  - On save/discard, calls `RunTrackingService.reset()` to clear state
- **Data sources:** All live state flows are consumed from `RunTrackingService` companion object via `collectAsState()`.
- **Navigation:** Accessed from the "LIVE RUN" button on `RunHistoryScreen`.

### 8.20 WorkoutSessionScreen

- **Animated background:** `GridBackground()` composable.
- **Full-screen layout:** No bottom nav bar (hidden via `showBottomBar` logic).
- **Color scheme:** All interactive elements use hardcoded `Color(0xFFFF2D78)` (magenta), not `LocalAppearance`.
- **Title row:** Back arrow (auto-pauses workout), preset name in `headlineMedium` Monospace magenta.
- **Live timer:** Elapsed time displayed as `HH:MM:SS` in 48sp Monospace magenta, updated every 100ms via a `tick` counter that drives recomposition. Actual elapsed time computed via `session.elapsedMs(System.currentTimeMillis())` which correctly excludes all paused time.
- **Sets remaining counter:** Label below timer showing total sets left across all exercises.
- **Live XP earned:** Label below sets remaining showing actual XP earned so far from completed sets, recomputed on each set completion via `XpEngine.xpForEntry()` using each exercise's difficulty.
- **Exercise list:** `LazyColumn` of `SessionExerciseCard` composables. Each card shows:
  - Exercise name with index number, completed/total sets count, and total reps/seconds summary
  - Per-set rows: "Set 1", "Set 2", etc. Completed sets show value in green (`GoalComplete`) with a checkmark icon. Incomplete sets show an `OutlinedTextField` (56dp height, `titleMedium`) for numeric input and a checkmark `IconButton` to submit.
  - Card border turns green when all sets are complete.
- **Control buttons:** Two side-by-side buttons at the bottom:
  - **PAUSE/RESUME** — Outlined magenta border, toggles `viewModel.togglePauseWorkout()` which sets `isPaused` and records `pausedSinceMs`; on resume, accumulated time is added to `pausedDurationMs`
  - **FINISH** — Solid magenta border, calls `viewModel.finishWorkout()` which logs each completed set as an individual `PREntry` (with note "From workout: {presetName}") to the matching exercise, marks session as completed, and saves
- **Auto-pause on leave:** `DisposableEffect(Unit)` calls `viewModel.autoPauseWorkout()` on dispose (back button, bottom nav, app background). This preserves the session for resume later.
- **No active workout state:** If `session == null` or `isCompleted == true`, shows "NO ACTIVE WORKOUT" / "WORKOUT COMPLETE" centered text with "GO BACK" button.
- **Active workout banner on WorkoutPresetsScreen:** Shows "ACTIVE WORKOUT" with status ("PAUSED — tap to resume" or "IN PROGRESS — tap to continue"), tappable to navigate to the active session.
- **Navigation:** Accessed via START/RESUME WORKOUT button on `WorkoutPresetDetailScreen`. Back button auto-pauses and returns to preset detail.

### 8.21 ExerciseHistoryScreen

- **Animated background:** `GridBackground()` composable.
- **Back button** (arrow icon) in top-left, tinted with `exerciseAccentColor`.
- **Title:** "EXERCISE HISTORY" in `displayLarge` Monospace with glowing underline.
- **Search bar:** `OutlinedTextField` between title and list with search icon, filters entries by exercise name (case-insensitive `contains`). Styled with `exerciseAccentColor` cursor/focus, `CardBackground` fill. Shows "NO MATCHES" centered text when search active but no results.
- **Data:** All `PREntry` objects across all exercises are collected into a flat list sorted newest-first. Each entry is analyzed to determine if it was a personal record at the time it was logged (its value > running max of all prior entries for that exercise).
- **Entry rows:** Each row has a 4dp colored left border strip and value text:
  - `pinnedAccentColor` (green) if the entry was a PR when logged
  - `exerciseAccentColor` (cyan) otherwise
- **Row details:** Exercise name, value with reps/s suffix, formatted date ("MMM dd, yyyy HH:mm"), optional note, XP earned inline. PR entries show a green trophy icon and "PR" label.
- **Delete entry:** Each row has a trash icon button in the right column. Deleting requires a 3-step confirmation dialog (same pattern as DashboardScreen exercise deletion): Step 1 "Delete Entry", Step 2 "Are You Sure?", Step 3 "Final Confirmation" with red "Delete Permanently" — calls `viewModel.deleteEntry(exerciseId, entryId)`.
- **Empty state:** "NO ENTRIES YET" centered text.
- **Navigation:** Accessed via "EXERCISE HISTORY" button on DashboardScreen.

### 8.22 WorkoutHistoryScreen

- **Animated background:** `GridBackground()` composable.
- **Back button** (arrow icon) in top-left, tinted with `systemAccentColor`.
- **Title:** "WORKOUT HISTORY" in `displayLarge` Monospace with glowing underline.
- **Search bar:** `OutlinedTextField` between title and list with search icon, filters workouts by preset name (case-insensitive `contains`). Styled with `systemAccentColor` cursor/focus, `CardBackground` fill. Shows "NO MATCHES" centered text when search active but no results.
- **Data:** List of past completed `WorkoutSession` objects persisted in `StorageData.workoutHistory`. Sessions are prepended to history on `finishWorkout()`.
- **Workout cards:** Each `GlowingCard` shows:
  - Preset name in `systemAccentColor` Monospace
  - Formatted start date, elapsed duration (using `elapsedMs()` helper), and exercise count
  - Per-exercise breakdown: exercise name, completed/total sets count, and total reps/seconds
  - Delete icon button (trash) on the right
- **Double-confirm delete:** First dialog: "Delete this workout?" with Cancel/Delete. Second dialog: "Are you sure? This cannot be undone." with Cancel/Permanently Delete (in red).
- **Empty state:** "NO WORKOUTS YET" centered text.
- **Navigation:** Accessed via "WORKOUT HISTORY" button on DashboardScreen.

### 8.23 HomeScreen

- **Animated background:** `GridBackground()` composable.
- **Robot mascot:** A detailed "CR-47 BIOMECHANICAL UNIT" robot character drawn via Canvas, featuring a flower crown, animated visor, animated mouth (lip-sync effect), and a scanner line sweeping down the figure. The mascot occupies the center of the screen.
- **Voice lines:** On entry, the mascot speaks a random voice line via `MediaPlayer` from 5 raw audio resources (`speech_1.mp3` through `speech_5.mp3`). A welcome-back voice line (`mascot_welcome.mp3`) plays when the app returns to foreground. Voice lines only play when `appSettings.speechesEnabled` is true. An animated speech bubble appears above the mascot during voice playback. Lifecycle-aware: stops playback on `ON_PAUSE`/`ON_STOP`.
- **Stats card:** A `GlowingCard` below the mascot displaying:
  - Current streak from `viewModel.currentStreak`
  - Current tier from `viewModel.tierResult` (tier name + number)
  - Current level + XP progress bar from `LevelProgressCard` (`viewModel.currentLevel`, `viewModel.xpInCurrentLevel`, `viewModel.xpNeededForLevelUp`)
  - Total exercise count
  - Equipped pet emojis with "+X% XP" text when pets are equipped
  - "ONLINE" status indicator
- **System readout text:** Below the stats card, shows two text lines: "ALL SYSTEMS NOMINAL" and "OFFLINE MODE -- DATA SECURED" in small Monospace.
- **Navigation button:** A single styled "ENTER SYSTEM" button that navigates to `DashboardScreen`.
- **Visual styling:** Uses `LocalAppearance.current` colors throughout for the mascot, stats card, and button. All rendering uses native Canvas APIs with `Paint` objects cached in `remember {}`.
- **Behavior:** Acts as an entry point / landing screen. No data collection happens here — reads state passively. Accessible via bottom nav "Home" icon.

### 8.24 DiceRollScreen

- **Animated background:** `GridBackground()` composable.
- **Full-screen layout:** No bottom nav bar (hidden via `showBottomBar` logic).
- **States:** Four states managed by `DiceRollState` enum — `IDLE`, `ROLLING`, `REVEAL`, `PET_DETAIL`.
- **Color scheme:** All interactive elements use hardcoded `Color(0xFFFFD700)` (gold) as accent.
- **Title row:** Back arrow (arrow icon) in top-left, "PET DICE" title in `headlineMedium` Monospace centered.
- **Combined stats + controls + coins row:** Below the title, a single horizontal `Row` with `SpaceBetween` arrangement containing:
  - Left: Roll count and pity counters (EPIC+, LEGENDARY, MYTHICAL) in small Monospace
  - Center: AUTO toggle button (`Icons.Default.Autorenew`) — toggles auto-roll on/off via `viewModel.toggleAutoRoll()`, uses `autoRoll` StateFlow from ViewModel (persists across navigation)
  - Right: Coin balance with `Coins` icon and animated count-up via `Animatable`
- **Lucky countdown:** Below the stats row, shows "LUCKY IN X" text (X = rollsUntilLucky - 1) in gold, or "LUCKY ROLL!" when a lucky roll is ready (rollsUntilLucky == 1). Only visible when lucky_roll upgrade > 0.
- **Equipped pets row:** If any pets are equipped, shows a row of equipped pet emojis with a "+X% XP" text in green monospace. Tap an emoji to unequip that pet. Below the inventory button.
- **Inventory button:** Centered row showing "📦 INVENTORY (count)" with accent color pill. Navigates to `PetInventoryScreen` via `navController.navigate(Routes.PET_INVENTORY)`.
- **IDLE state:** `IdleDiceView` — 160dp gold `Casino` icon button that triggers roll. "TAP TO ROLL" label.
- **ROLLING state:** `RollingDiceView` — 3D dice rotation via `graphicsLayer { rotationX/rotationY }`, gold border, sparkle particles around the dice, "ROLLING..." label with pulsing animation.
- **REVEAL state:** Split layout — top area (weight 1f) shows `RevealView` (pet emoji, rarity badge, name, stars, "1 in X CHANCE" display floored at 2, "TAP TO DISMISS" text). Bottom area (220dp) shows idle dice — **tappable to roll again** without dismissing the reveal first. RevealView is scrollable via `verticalScroll` so all content fits.
- **PET_DETAIL state:** Full-screen `PetDetailView` overlay (bottom dice area hidden) showing:
  - Large pet emoji in center with tier-colored border glow
  - Pet name in Monospace
  - Rarity badge pill (color-coded)
  - Tier badge pill (color-coded with tier label)
  - Stars display (★/☆, gold when >= 3 stars)
  - **Favorite toggle** — star icon button, toggles `isFavorited` with haptic feedback
  - **FUSE button** (visible when 5★ and not max tier) — upgrades pet to next tier, consumes old pet, resets stars to 1. Calls `viewModel.fusePet(petId)`.
  - **SELL button** — sells pet for coin value (baseCoins × tierMultiplier × stars). Calls `viewModel.sellPet(petId)`. Shows confirmation dialog before selling.
  - "TAP TO DISMISS" text below
- **Auto-roll loop:** A separate `LaunchedEffect(autoRoll)` runs a continuous `while` loop checking `rollState`. On IDLE: triggers roll + dice animation. On REVEAL: waits rollDelay then triggers next roll. On PET_DETAIL: silently calls `viewModel.rollDice()` every 2s without changing state. On ROLLING: waits. Auto-roll only stops via explicit toggle — navigating away, fusing, viewing pets don't stop it.
- **Merge import:** MERGE mode deduplicates pets by ID, keeps higher tier then higher stars, `maxOf` for coins.
- **Coin counter:** Clickable, navigates to `PetUpgradesScreen` via `navController.navigate(Routes.PET_UPGRADES)`.
- **Roll speed:** Dice animation delay scales with `roll_speed` upgrade level (`maxOf(200, 1600 - level * 72)` ms).
- **Navigation:** Accessed via "PET DICE" button on GoalsScreen.

### 8.25 PetInventoryScreen

- **Animated background:** `GridBackground()` composable.
- **Full-screen layout:** No bottom nav bar (hidden via `showBottomBar` logic).
- **Back button:** ArrowBack icon in top-left, pops back to DiceRollScreen via `navController.popBackStack()`.
- **Title:** "INVENTORY (N)" in `headlineMedium` Monospace gold, where N is total pet count.
- **Search bar:** `OutlinedTextField` below title with search icon, filters pets by name, rarity, or tier (case-insensitive `contains`).
- **Sort controls:** Row with sort mode dropdown (`ExposedDropdownMenuBox`: TYPE / RARITY / VALUE / XP) and ascending/descending toggle arrow button. Default: VALUE descending. Favorites always pinned to top regardless of sort.
- **Pet grid:** `LazyVerticalGrid` with 4 columns showing all owned pets sorted by favorites first, then by selected sort mode. Each `PetCollectionCard` shows emoji + pet name (truncated) with tier-specific visual effects:
  - NORMAL: rarity border color
  - SILVER: gradient border + animated shimmer overlay
  - GOLDEN: gold gradient fill
  - RAINBOW: animated cycling hue rotation
  - DARK_MATTER: pulsing purple glow with scale pulse
  - RED_MATTER: pulsing red glow with heat shimmer translation
  - Starred pets (>= 3★) have a gold border glow
  - 5★ NORMAL pets show a pulsing gold border indicating upgrade-ready
  - Green checkmark badge when equipped
- **Tap opens PET_DETAIL:** Tapping a pet card replaces the grid with `PetDetailView` inline (not a separate route). Shows all pet details including equip/unequip button.
- **Long-press for quick favorite toggle** with haptic feedback.
- **Action buttons row:** FUSE ALL and SELL ALL buttons at the bottom (outside grid, fixed). Each has a confirmation dialog.
- **Merge import:** MERGE mode deduplicates pets by ID, keeps higher tier then higher stars, `maxOf` for coins.
- **Navigation:** Accessed via "INVENTORY" button on DiceRollScreen.

### 8.26 PetUpgradesScreen

- **Animated background:** `GridBackground()` composable.
- **Back button:** ArrowBack icon in top-left, tinted with `systemAccentColor`.
- **Title:** "PET UPGRADES" in `displayLarge` Monospace with `systemAccentColor`.
- **Coin balance:** Top-right showing current coins with coin emoji.
- **Upgrade cards:** `LazyColumn` of `UpgradeCard` composables, one per `PetUpgrade` entry:
  - Upgrade name in accent color, current level / max level in secondary text
  - Purchase button (gold coin styling) showing cost, disabled when can't afford
  - "MAX" badge when at max level (neon green)
  - Description text in secondary color
  - 10-segment progress bar showing purchased levels in accent color
  - Effect preview text showing next-level benefit
- **Navigation:** Accessed via coin counter tap on `DiceRollScreen`.

---

## 9. ViewModel Structure

### `PRViewModel` (`viewmodel/PRViewModel.kt`)

Extends `AndroidViewModel(application)` for app context access.

| StateFlow            | Type                                              | Description                                                                                                                                                                    |
| -------------------- | ------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `exercises`          | `StateFlow<List<Exercise>>`                       | All exercises, single source of truth                                                                                                                                          |
| `goals`              | `StateFlow<List<Goal>>`                           | All goals                                                                                                                                                                      |
| `pinnedExercises`    | `StateFlow<List<Exercise>>`                       | Derived: exercises where `isPinned == true`, sorted by `sortOrder`                                                                                                             |
| `unpinnedExercises`  | `StateFlow<List<Exercise>>`                       | Derived: exercises where `isPinned == false`, sorted by `sortOrder`                                                                                                            |
| `weightEntries`      | `StateFlow<List<WeightEntry>>`                    | All body weight entries, sorted by date descending                                                                                                                             |
| `appSettings`        | `StateFlow<AppSettings>`                          | Singleton settings (weightUnit, targetWeight, calendarDayViewMode, morningReminderHour/Minute, eveningReviewHour/Minute, soundEnabled, soundVolume, hapticEnabled, appearance) |
| `restDays`           | `StateFlow<List<String>>`                         | List of rest day date strings ("YYYY-MM-DD")                                                                                                                                   |
| `currentStreak`      | `StateFlow<Int>`                                  | Derived: computed from workout days + rest days via `combine(_exercises, _restDays)`                                                                                           |
| `hapticEvent`        | `SharedFlow<Unit>`                                | One-shot event consumed by CalendarScreen to trigger vibration                                                                                                                 |
| `allTelemetry`       | `StateFlow<Map<String, List<LeverageTelemetry>>>` | Per-exercise RSI telemetry map, loaded on demand                                                                                                                               |
| `isLoadingTelemetry` | `StateFlow<Boolean>`                              | Loading flag for telemetry computation                                                                                                                                         |
| `runEntries`         | `StateFlow<List<RunEntry>>`                       | All run entries, sorted by date descending                                                                                                                                     |
| `runningPRs`         | `StateFlow<RunningPRs>`                           | Derived: running PRs computed from `RunEntry` list via `RunningPREngine.computePRs()`                                                                                          |
| `tierResult`         | `StateFlow<TierResult>`                           | Derived: 10-tier progression result from `TierEvaluator.evaluate(exercises)`                                                                                                   |
| `workoutPresets`     | `StateFlow<List<WorkoutPreset>>`                  | All workout presets, single source of truth                                                                                                                                    |
| `activeSession`      | `StateFlow<WorkoutSession?>`                      | Active workout session (null if none), persisted to JSON for app restart survival                                                                                              |
| `workoutHistory`     | `StateFlow<List<WorkoutSession>>`                 | Past completed workout sessions, persisted for history display                                                                                                                 |
| `pendingImportJson`  | `StateFlow<String?>`                              | Temporarily holds incoming JSON for the import screen (one-shot handoff)                                                                                                       |
| `totalXp`            | `StateFlow<Long>`                                 | Accumulated XP across all time                                                                                                                                                 |
| `currentLevel`       | `StateFlow<Int>`                                  | Derived: current level (1–200) from `totalXp` via `XpEngine.levelFromTotalXp()`                                                                                                |
| `xpInCurrentLevel`   | `StateFlow<Long>`                                 | Derived: XP accumulated within the current level                                                                                                                               |
| `xpNeededForLevelUp` | `StateFlow<Long>`                                 | Derived: XP required to advance to the next level                                                                                                                              |
| `petInventory`       | `StateFlow<List<Pet>>`                            | All collected pets, sorted by tier then stars                                                                                                                                  |
| `totalRolls`         | `StateFlow<Long>`                                 | Lifetime dice roll count                                                                                                                                                       |
| `rollsSinceEpicOrAbove` | `StateFlow<Long>`                              | Soft pity counter: Epic+ chance +1% per roll after 150 rolls                                                                                                                  |
| `rollsSinceLegendary` | `StateFlow<Long>`                               | Hard pity: guaranteed legendary at 401 rolls                                                                                                                                   |
| `rollsSinceMythical` | `StateFlow<Long>`                                | Hard pity: guaranteed mythical at 2001 rolls                                                                                                                                   |
| `lastDiceRollTimestamp` | `StateFlow<Long>`                              | Timestamp of last dice roll                                                                                                                                                    |
| `coins`              | `StateFlow<Long>`                                 | Pet dice coins (earned on every roll)                                                                                                                                          |
| `petUpgrades`        | `StateFlow<Map<String, Int>>`                     | Upgrade ID → level (e.g. "luck" → 3)                                                                                                                                         |
| `autoRoll`           | `StateFlow<Boolean>`                              | Auto-roll toggle state, persists across navigation                                                                                                                             |
| `equippedPetIds`     | `StateFlow<List<String>>`                         | IDs of equipped pets (max 2-5 slots), persisted to pets.json                                                                                                                  |

| Function                                            | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| --------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `loadData()`                                        | Reads full `StorageData` via `StorageManager.loadFullData()`, loads exercises as-is (no reclassification), runs XP bootstrap on first load (computes total XP from all entries and persists), migrates old workout history sessions (`xpEarned == 0L`) by matching session exercises to exercises and computing XP per completed set, updates all 19 state flows (exercises, goals, weightEntries, appSettings, restDays, runEntries, runningPRs, workoutPresets, activeSession, workoutHistory, totalXp, petInventory, totalRolls, rollsSinceEpicOrAbove, rollsSinceLegendary, rollsSinceMythical, lastDiceRollTimestamp, coins, petUpgrades, equippedPetIds) + syncs `SoundEngine.volume` |
| `saveData()`                                        | Writes exercises + goals + weight entries to `StorageManager.saveData()` (private)                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| `addExercise(exercise)`                             | Appends with auto-incremented `sortOrder` within the same pin group, then saves. Uses the difficulty from the Exercise constructor (set by AddExerciseScreen). |
| `renameExercise(exerciseId, newName)`               | Updates exercise name and cascades to goals (exerciseId match), workout presets (exerciseName string match), active session, and workout history. Saves all changes. |
| `logEntry(exerciseId, entry)`                       | Appends entry to matching exercise, awards XP via `XpEngine.xpForEntry()` multiplied by equipped pet XP multiplier and active potion multiplier, bakes `xpEarned` into entry, moves exercise to front of list, calls `recalculateTotalXp()` + saves                                                                                                                                                                                                                                                                                                                                                                                                               |
| `deleteExercise(exerciseId)`                        | Removes exercise by ID + removes all associated goals + saves                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| `deleteEntry(exerciseId, entryId)`                  | Removes specific entry from exercise, calls `recalculateTotalXp()` + saves                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| `clearAllData()`                                    | Resets all state flows to defaults (exercises, goals, weightEntries, appSettings, restDays, runEntries, runningPRs, workoutPresets, activeSession, workoutHistory, allTelemetry, petInventory, totalRolls, rollsSinceEpicOrAbove, rollsSinceLegendary, rollsSinceMythical, lastDiceRollTimestamp, coins, petUpgrades, equippedPetIds) + resets XP + saves                                                                                                                                                                                                                               |
| `isNewPR(exerciseId, value): Boolean`               | True if value > max existing entry value, or if no entries exist                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| `getCurrentPR(exerciseId): Int`                     | Max entry value, or 0                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| `getExerciseById(exerciseId): Exercise?`            | Finds exercise by ID                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| `toggleHaptic(enabled)`                             | Sets haptic preference via `_appSettings.hapticEnabled`, persists to JSON                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| `togglePin(exerciseId)`                             | Flips `isPinned`, recalculates sort orders + saves                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| `swapExercises(id1, id2)`                           | Swaps `sortOrder` values between two exercises + saves                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| `addGoal(goal: Goal)`                               | Appends to goals list + saves                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| `deleteGoal(goalId: String)`                        | Removes goal by ID + saves                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| `getProgressForGoal(goal: Goal): Int`               | Sums PREntry values for the linked exercise within the current period window                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| `getProgressPercent(goal: Goal): Float`             | Returns 0f to 1f                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| `getTimeRemaining(goal: Goal): String`              | Returns human-readable string based on period (e.g. "Resets in 5h 30m", "3 days left this week", "12 days left in March")                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| `getCalendarDayValue(exercise, dateString): Int`    | Returns aggregated value for a given exercise on a specific date string ("YYYY-MM-DD") using the current `calendarDayViewMode` ("pr" = max, "sum" = sum)                                                                                                                                                                                                                                                                                                                                                                                                       |
| `setWeightUnit(unit)`                               | Converts all weight entries to new unit (kg/lbs), updates settings + saves                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| `setTargetWeight(weight)`                           | Updates target body weight in settings + saves                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| `setCalendarDayViewMode(mode)`                      | Sets how daily values are aggregated ("pr"/"sum"), saves                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| `setSoundEnabled(enabled)`                          | Toggles UI sound effects on/off, saves                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| `setSoundVolume(volume)`                            | Sets sound effects volume (0.0–1.0), updates `SoundEngine.volume`, saves                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| `toggleSpeeches(enabled)`                           | Toggles mascot voice lines on/off, saves                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| `setMorningReminderTime(hour, minute)`              | Changes morning training reminder time, saves                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| `setEveningReviewTime(hour, minute)`                | Changes evening review time, saves                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| `addWeightEntry(entry)`                             | Appends to weight entries list + saves                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| `deleteWeightEntry(entryId: String)`                | Removes entry by ID + saves                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| `getCurrentWeight(): Float?`                        | Returns the most recent weight entry value, or null if no entries exist                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| `getLowestWeight(): Float?`                         | Returns the lowest weight value across all entries, or null                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| `getHighestWeight(): Float?`                        | Returns the highest weight value across all entries, or null                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| `getAverageWeight(): Float?`                        | Returns the average weight across all entries, or null                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| `updateAppearance(appearance)`                      | Updates `_appSettings.value.appearance` with new `AppearanceSettings`, persists to JSON                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| `applyTheme(theme: AppTheme)`                       | Applies a preset theme by setting `_appSettings.value.appearance = theme.settings`, persists to JSON                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| `setGoal(exerciseId, goalValue)`                    | Updates `exercise.goal` on matching exercise + saves                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| `getGoalProgress(exerciseId): Float`                | Returns `currentPR / goal` as Float (can exceed 1.0), or 0f if no goal set                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| `isGoalReached(exerciseId): Boolean`                | True if exercise has a goal set and current PR >= goal                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| `generateExportJson(): String`                      | Serializes full `StorageData` to JSON string for file export                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| `importSyncData(data: StorageData, mode: SyncMode)` | Merges or replaces local data with incoming `StorageData`. REPLACE mode overwrites all 9 state flows (exercises, goals, weight, settings, restDays, runEntries, runningPRs, presets, history) + totalXp + pet inventory + coins + equippedPetIds. MERGE mode deduplicates exercises by ID/name, merges goals/weight/restDays/presets/history by ID, deduplicates run entries by ID and recomputes running PRs, recomputes totalXp from merged exercises, deduplicates pets by ID (keeps higher tier then higher stars), maxOf for coins. Both modes preserve exercise difficulty as-is (no reclassification) and recompute XP via `recalculateTotalXp()`.                                                                   |
| `SyncMode` enum                                     | `REPLACE` — overwrites all local data; `MERGE` — deduplicates by ID, keeps higher goal value, preserves `isPinned`/`sortOrder`                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| `toggleTodayAsRestDay()`                            | Toggles today's date in `_restDays` list, saves to JSON, emits on `_hapticEvent`                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| `currentStreak`                                     | Derived via `combine(_exercises, _restDays)` — walks backwards from today counting workout days, preserving chain through rest days, breaking on missed days                                                                                                                                                                                                                                                                                                                                                                                                   |
| `calculateCurrentStreak(): Int`                     | Private: collects workout day set from all entries, walks offset from today, counts consecutive workout days (rest days preserve chain)                                                                                                                                                                                                                                                                                                                                                                                                                        |
| `loadAllTelemetry()`                                | Computes RSI telemetry for all exercises on `Dispatchers.Default`, stores in `_allTelemetry`                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| `clearAllTelemetry()`                               | Clears the telemetry state and resets loading flag                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| `getCurrentRsi(exerciseId): Int?`                   | Returns the latest RSI score for an exercise, or null                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| `getRsiDelta(exerciseId): Int?`                     | Returns 30-day RSI delta (latest vs ~30 days ago), or null                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| `getRsiStatusLabel(delta): String`                  | Returns "STABLE", "OVERLOAD", or "DELOAD" based on delta sign                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| `addRunEntry(entry: RunEntry)`                      | Prepends run entry, recomputes running PRs, saves                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| `deleteRunEntry(id: String)`                        | Removes run entry, recomputes running PRs, saves                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| `isNewRunPR(entry: RunEntry): Boolean`              | True if entry beats any existing running PR                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| `savePresets()`                                     | Persists current workout presets to JSON                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| `startWorkout(preset: WorkoutPreset)`               | Creates a new `WorkoutSession` from preset exercises (maps `PresetExercise` to `SessionExerciseProgress` with target value, isHold, totalSets), sets `activeSession`, persists to JSON                                                                                                                                                                                                                                                                                                                                                                         |
| `completeSetInSession(exerciseIndex, value)`        | Adds a `SessionSetEntry` to the specified exercise's completed sets + saves                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| `togglePauseWorkout()`                              | Toggles `isPaused`; on pause records `pausedSinceMs = now`; on resume accumulates `pausedDurationMs += (now - pausedSinceMs)`                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| `finishWorkout()`                                   | Logs each completed set as a separate `PREntry` (note: "From workout: {presetName}") to the matching exercise, computes `xpEarned` per set (with equipped pet multiplier and active potion multiplier), moves exercise to front, marks session completed, saves completed session to `_workoutHistory`, calls `recalculateTotalXp()` + saves                                                                                                                                                                                                                                                                               |
| `discardWorkout()`                                  | Sets `activeSession = null`, discards all progress                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| `autoPauseWorkout()`                                | If session is active and not already paused/completed, sets `isPaused = true` and records `pausedSinceMs` (called on screen leave)                                                                                                                                                                                                                                                                                                                                                                                                                             |
| `deleteWorkoutHistoryEntry(sessionId: String)`      | Removes a session from `_workoutHistory` by ID + saves                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| `setPendingImportJson(json: String?)`               | Stores incoming JSON string for the import screen to consume                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| `consumePendingImportJson(): String?`               | Returns and clears the pending import JSON (one-shot handoff)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| `addPreset(preset: WorkoutPreset)`                  | Appends preset to list + saves                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| `updatePreset(updated: WorkoutPreset)`              | Replaces matching preset by ID + saves                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| `deletePreset(id: String)`                          | Removes preset by ID + saves                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| `togglePresetPin(id: String)`                       | Toggles `isPinned` on matching preset + saves                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| `reorderPresets(from: Int, to: Int)`                | Removes item at `from` index from sorted list, inserts at `to`, rewrites all `sortOrder` values + saves                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| `rollDice(): Pet`                                    | Weighted random rarity selection with pity system (soft pity at 150 rolls for Epic+, hard pity at 401 for Legendary, 2001 for Mythical). Luck upgrade adds +20%/level to ALL non-COMMON rarity chances. Lucky Roll: every 5th roll when lucky_roll > 0, boosted rarity chances scaling with level (+0.25x per level). Active dice effects with `baseChances` (REFINING/ASCENDANT/LEGENDARY) replace base drop chances with custom weighted distributions; soft pity is skipped for those dice. Hard pity still overrides. Old-style filter (BANISHING/MYTHIC) uses `minRarity`/`maxRarity` zeroing as before. SUPER pre-check: flat `Math.random() < 0.0001` (1/10k independent), or guaranteed if SUPER DICE is active (`isSuperDiceActive`). SUPER DICE forces the check to always trigger. SUPER pet coin award: **flat 500B** (no multipliers). Natural SUPER and SUPER DICE both give 500B flat. Finds existing pet by `speciesId + tier` for star upgrade; if already 5★ NORMAL, creates a new copy. Awards coins = `pet.coinValue()` × coin_multiplier upgrade × pet XP multiplier from equipped pets on every roll (coin value includes stars multiplier). Returns `RollResult(pet, effectiveChances, isLuckyRoll)` where `effectiveChances` are the actual boosted chances used for the roll. Saves to JSON. |
| `toggleAutoRoll()`                                   | Toggles `_autoRoll` StateFlow (persists across navigation). Does NOT save to JSON — transient toggle.                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| `purchaseUpgrade(upgrade)`                           | Buys the next level of a PetUpgrade if affordable. Deducts coins, increments level in `_petUpgrades`, saves to JSON.                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| `fusePet(petId: String)`                             | Finds pet by ID, requires 5★ and not max tier. Removes old pet, creates new pet with `tier = nextTier`, `stars = 1`, new UUID. Removes old pet ID from equipped list. Saves to JSON.                                                                                                                                                                                                                                                                                   |
| `sellPet(petId: String)`                             | Removes pet by ID, adds `pet.coinValue()` to coins. Removes pet ID from equipped list. Saves to JSON.                                                                                                                                                                                                                                                                                                                                                              |
| `equipPet(petId: String)`                            | Adds pet ID to `_equippedPetIds` (up to max slots). Saves to pets.json.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| `unequipPet(petId: String)`                          | Removes pet ID from `_equippedPetIds`. Saves to pets.json.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| `petXpMultiplier(): Double`                          | Returns 1.0 + sum of (pet.xpMultiplier() - 1.0) for each equipped pet (additive stacking). Used as multiplicand in logEntry(), finishWorkout(), and rollDice() coin awards. |
| `maxEquipSlots(): Int`                               | Returns 2 + upgrade level of EQUIP_SLOTS (max 5).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| `clearPetData()`                                     | Resets pet inventory, coins, upgrades, rolls, pity counters, equipped pets to defaults. Saves to pets.json.                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| `generateAppExportJson(): String`                    | Serializes app-only StorageData (without pet fields) to JSON for APP export.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| `generatePetExportJson(): String`                    | Serializes PetStorageData to JSON for PET export.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| `importAppData(json: String)`                        | Parses JSON as StorageData, applies REPLACE or MERGE to app state flows, saves to prs.json.                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| `importPetData(json: String)`                        | Parses JSON as PetStorageData, applies REPLACE or MERGE to pet state flows, saves to pets.json.                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| `mergePetData(incoming: List<Pet>, newCoins: Long, ...)` | Deduplicates pets by ID (keeps higher tier then higher stars), maxOf for coins. Saves to pets.json.                                                                                                                                                                                                                                                                                                                                                                                                                                                             |

**Period window logic** (used by both `getProgressForGoal` and `GoalNotificationWorker`):

| Period    | Window                                                                         |
| --------- | ------------------------------------------------------------------------------ |
| `daily`   | Today midnight to midnight (same calendar day)                                 |
| `weekly`  | Current Monday to Sunday (Monday start via `firstDayOfWeek = Calendar.MONDAY`) |
| `monthly` | 1st to last day of current month                                               |

All period math uses `java.util.Calendar` (not `java.time`) for API 26 compatibility.

---

## 10. Custom Components / Composables

### `GlowingCard` (`ui/components/GlowingCard.kt`)

```kotlin
@Composable
fun GlowingCard(
    modifier: Modifier = Modifier,
    borderBrush: Brush? = null,
    content: @Composable BoxScope.() -> Unit
)
```

A `Box` with:

- 16dp rounded corners
- `CardBackground` fill (semi-transparent navy)
- 1dp gradient border from `PrimaryAccent` to `SecondaryAccent` (default), or a custom `borderBrush` if provided
- The default border brush is created once in `remember { }` to avoid reallocation on recomposition
- Default border colors are read dynamically from `LocalAppearance.current.systemAccentColor` and `systemSecondaryColor`

Used as the base container for all cards throughout the app.

### `NeonButton` (`ui/components/NeonButton.kt`)

```kotlin
@Composable
fun NeonButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
)
```

Full-width button (56dp height, 16dp corners) with:

- Color read from `LocalAppearance.current.systemAccentColor` at 15% alpha for fill
- Text color matches the accent color
- Disabled state: dimmer fill, gray text
- Overlay gradient border (accent → transparent) for a glowing edge effect

### `AnimatedRing` (`ui/components/AnimatedRing.kt`)

```kotlin
@Composable
fun AnimatedRing(
    progress: Float,
    modifier: Modifier = Modifier,
    colorStart: Color = LocalAppearance.current.exerciseAccentColor,
    colorEnd: Color = LocalAppearance.current.exerciseSecondaryColor,
    hasGoal: Boolean = false,
    isGoalReached: Boolean = false,
    isPinned: Boolean = false
)
```

A 60dp Canvas circle with:

- Track arc: dim `Surface` color
- Progress arc: sweep gradient from `colorStart` to `colorEnd`, animated with `Animatable` over 1 second
- Round stroke caps
- Customizable colors allow the dashboard to switch between cyan/purple (unpinned) and green/red (pinned)
- `hasGoal`/`isGoalReached`/`isPinned` params dynamically switch colors and show percentage text or a tick mark at 100%
- Default colors are read from `LocalAppearance.current` for dynamic theming

### `SetGoalDialog` (`ui/components/SetGoalDialog.kt`)

```kotlin
@Composable
fun SetGoalDialog(
    visible: Boolean,
    exerciseName: String,
    exerciseType: String,
    currentGoal: Int?,
    onDismiss: () -> Unit,
    onSetGoal: (Int) -> Unit,
    onRemoveGoal: () -> Unit
)
```

A dialog for setting, editing, or removing a per-exercise goal value:

- **SET mode** when `currentGoal` is null: numeric text field with unit label ("reps" or "seconds"), confirm button
- **EDIT mode** when `currentGoal` is set: pre-filled field, update button
- **REMOVE goal** option always visible when a goal is set
- Styled with the app's dark theme (cyan accents, `GlowingCard` container)

### `GridBackground` (`ui/components/GridBackground.kt`)

```kotlin
@Composable
fun GridBackground(modifier: Modifier = Modifier)
```

Full-screen animated grid using `Box` + `Modifier.drawBehind`. Grid lines (80dp spacing) in ultra-faint cyan (3% alpha) slowly scroll diagonally. The animation runs via `withFrameMillis` in a `LaunchedEffect`, updating a `mutableFloatStateOf` offset. Drawing happens entirely in the draw phase — no composable recomposition occurs on animation frames (no `Canvas` composable, only `drawBehind`).

### `PRCelebrationOverlay` (`ui/components/PRCelebrationOverlay.kt`)

```kotlin
@Composable
fun PRCelebrationOverlay(
    visible: Boolean,
    onDismiss: () -> Unit
)
```

Full-screen overlay:

1. **Purple flash:** 300ms full-screen `SuccessPurple` at 60% alpha
2. **Lottie confetti:** Loads from `https://assets.lottiefiles.com/packages/lf20_touohxv0.json`, plays once at 1.5x speed
3. **"NEW PERSONAL RECORD!"** text in `headlineLarge`, purple, Monospace, fade+scale animation via `AnimatedVisibility`
4. **Auto-dismiss** after 2.5 seconds total, or on tap anywhere

### `BiomechanicalRadarCard` (`ui/components/BalanceEngine.kt`)

```kotlin
@Composable
fun BiomechanicalRadarCard(exercises: List<Exercise>)
```

A `GlowingCard` containing a Canvas-drawn pentagonal radar chart (5 axes: VP, HP, VPush, HPush, CL) that visualizes the user's movement pattern balance. Each axis score is computed by `mapExercisesToVectors()` which matches exercise names to keyword lists and calculates a 0-10 score based on PR values. Displays a "Structural Deviation" percentage calculated by `computeAsymmetryIndex()` — if push/pull imbalance exceeds 25%, the chart color switches from cyan to purple, an animated scan-line aberration effect plays over the chart, and a "CRITICAL_ASYMMETRY" warning text appears with a hint to increase the weaker chain. Keyword mapping: VP (pull-up, chin-up, muscle-up), HP (front lever, row), VPush (handstand, hspu, dip), HPush (push-up, pushup, planche), CL (l-sit, lsit, leg raise, dragon flag).

### `MovementAnalysisSection` (`ui/screens/CalendarScreen.kt`)

```kotlin
@Composable
private fun MovementAnalysisSection(
    presets: List<WorkoutPreset>,
    navController: NavHostController
)
```

A private composable inside CalendarScreen.kt. Shows a "MOVEMENT ANALYSIS" label in small Monospace, an `ExposedDropdownMenuBox` for selecting a workout preset, and an "ANALYZE MOVEMENT →" button that navigates to `PresetAnalysisScreen` with the selected preset's ID. The button is disabled when no preset is selected, with a gradient border overlay when enabled. The dropdown shows "NO PRESETS — CREATE ONE FIRST" when the presets list is empty.

### `MorningReminderWorker` (`work/MorningReminderWorker.kt`)

```kotlin
class MorningReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params)
```

Scheduled by `WorkManager` at 8:00 AM daily. On each run:

1. Reads exercises and rest days from `prs.json` directly via `StorageManager.loadFullData()`
2. Checks whether the user has already logged any entries today (any `PREntry` with today's date)
3. If already trained today: returns silently — no notification sent
4. If NOT trained today: sends `IMPORTANCE_HIGH` notification with:
   - Title: "TIME TO TRAIN 💪"
   - Body: one of 5 rotating motivational lines selected by `(dayOfWeek % 5)`:
     "Your PRs won't break themselves.", "Yesterday you said tomorrow. That's today.",
     "The grind doesn't stop — neither do you.", "One session away from a new personal record.",
     "Your future self is counting on you right now."
   - Expanded via `BigTextStyle`: motivational line, current streak with flame emoji,
     then up to 3 pinned exercises with their PR values (or top 3 by most recent entry
     date if no pinned exercises exist)
   - Footer: "Open PRTracker to log today's session."
5. Notification ID: 8001, channel `pr_tracker_morning`, opens DashboardScreen on tap
6. Silently skips if `POST_NOTIFICATIONS` permission is denied (Android 13+)

### `GoalNotificationWorker` (`work/GoalNotificationWorker.kt`)

```kotlin
class GoalNotificationWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params)
```

Scheduled by `WorkManager` at **8:30 AM** daily (changed from original 9:00 AM). On each run:

1. Reads exercises and goals from `prs.json` directly via `StorageManager`
2. Creates the `pr_tracker_goals` notification channel if it doesn't exist
3. For each goal where `progress < targetValue`: fires a separate notification tagged with `goal.id.hashCode()` as the notification ID
4. Each notification shows the exercise name + period as title, current progress with remaining count, uses `BigTextStyle` for expandability, and includes a `setProgress` bar
5. Uses `ic_goal_notification` (target icon) as the small icon
6. Silently skips if `POST_NOTIFICATIONS` permission is denied (Android 13+)

### `EveningReviewWorker` (`work/EveningReviewWorker.kt`)

```kotlin
class EveningReviewWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params)
```

Scheduled by `WorkManager` at 9:30 PM daily. On each run:

1. Reads exercises, rest days from `prs.json` directly via `StorageManager.loadFullData()`
2. Determines scenario and sends the appropriate notification:

   **Scenario A — Trained today:**
   - Title: "TODAY'S SESSION RECAP ✅"
   - Collapsed body: "You logged X exercises today. [best achievement line]"
     Best achievement line: new PR name, streak extended if >3, or total volume
   - Expanded body via `BigTextStyle`: date line, per-exercise breakdown with crown
     emoji (👑) on new PRs, total exercises count, new PR count, current streak,
     bonus "incredible consistency" line when streak ≥ 7

   **Scenario B — Missed day (no entries, not a rest day):**
   - Title: "DID YOU TRAIN TODAY? 🤔"
   - Body: "No entries logged yet. Still time to get a session in."
   - Expanded: suggests marking a rest day, shows current streak as motivation

   **Scenario C — Rest day:**
   - Title: "REST DAY — RECOVERY MODE 🔋"
   - Body: "Smart training includes smart recovery. See you tomorrow."
   - Expanded: confirms streak is safe, encourages recovery

3. Notification ID: 2130, channel `pr_tracker_evening`, `IMPORTANCE_DEFAULT`,
   opens DashboardScreen on tap, no vibration
4. Uses `isNewPRToday()` helper: checks if today's best entry value for an exercise
   equals or exceeds the all-time max AND there were previous entries before today
5. Silently skips if `POST_NOTIFICATIONS` permission is denied (Android 13+)

### `WeeklySummaryWorker` (`work/WeeklySummaryWorker.kt`)

```kotlin
class WeeklySummaryWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params)
```

Scheduled by `WorkManager` every Sunday at 22:00. On each run:

1. Reads exercises, goals, and weight entries from `prs.json` directly via `StorageManager`
2. Computes weekly summary stats from the past 7 days:
   - New PRs set this week (counts exercises where the latest entry value > previous max)
   - Goals completed this week (goals that reached 100% progress within the current period)
   - Total volume (sum of all rep values logged this week)
   - Weight change (difference between first and last weight entry this week)
3. Creates the `pr_tracker_weekly` notification channel if needed
4. Fires a single notification summarizing the week's stats using `BigTextStyle`
5. Uses `ic_goal_notification` as the small icon
6. Silently skips if `POST_NOTIFICATIONS` permission is denied

### `RunTrackingService` (`service/RunTrackingService.kt`)

```kotlin
class RunTrackingService : Service()
```

A foreground service that drives live GPS run tracking. Uses `FusedLocationProviderClient` with `PRIORITY_HIGH_ACCURACY`, 1-second update interval, 500ms fastest interval, 20m accuracy filter, 8-second warmup period (silently discards all points), 2m minimum distance threshold, and an internal `KalmanFilter2D` (constant-velocity 4-state model with `Q=0.0001`, `R=0.00003` degrees) for smoothed coordinate estimation. Companion object exposes five `MutableStateFlow`s consumed by `LiveRunScreen`:

| StateFlow             | Type                        | Description                                |
| --------------------- | --------------------------- | ------------------------------------------ |
| `distanceMeters`      | `MutableStateFlow<Float>`   | Accumulated GPS distance in meters         |
| `elapsedSeconds`      | `MutableStateFlow<Long>`    | Running ticker-based elapsed time          |
| `currentPaceSecPerKm` | `MutableStateFlow<Float?>`  | Latest GPS-derived pace (null until >100m) |
| `isTracking`          | `MutableStateFlow<Boolean>` | Whether service is actively tracking       |
| `isPaused`            | `MutableStateFlow<Boolean>` | Whether tracking is paused                 |

Actions (static methods): `start(context)`, `pause(context)`, `resume(context)`, `stop(context)`. Foreground notification channel `pr_tracker_running` with ongoing notification showing distance and time, updated every 5 seconds. Companion `reset()` clears all state flows. Notification includes a STOP action `PendingIntent`.

### ~~`QRSyncManager` (`data/QRSyncManager.kt`)~~ — REMOVED

Deleted in v1.1 (File-Based Import/Export feature). Replaced by `SyncExportScreen` and `SyncImportScreen` which use Android's share sheet and file-based JSON import instead of QR codes. ZXing and CameraX dependencies were also removed.

---

## 11. Feature Changelog

1. **File-Based Import/Export** — Replaced QR code sync system with file-based import/export. Deleted `QRSyncManager.kt`, `SyncSendScreen.kt`, and `SyncReceiveScreen.kt`. Created `SyncExportScreen.kt` (writes JSON to cache, shares via Android's share sheet using FileProvider) and `SyncImportScreen.kt` (import confirmation screen showing data summary with REPLACE/MERGE options). Added `ACTION_VIEW` intent filter for `application/json` mime type to `AndroidManifest.xml` so users can open `.json` backup files directly in the app from any file manager. Added `pendingImportJson` state to `PRViewModel` for passing JSON data from intent handler to the import screen. Added `generateExportJson()` method to `PRViewModel`. Removed ZXing (~3.5MB) and CameraX (~8MB) dependencies, and removed `CAMERA` permission from manifest.

2. **Preset Movement Pattern Analyzer** — Added `PresetAnalysisScreen` with movement classification engine (VP/HP/CL/VPush/HPush), scoring from preset exercises and actual logged PR values, and a `BiomechanicalRadarCard`-style pentagonal radar chart showing the balance profile. Added `PRESET_ANALYSIS` route to NavGraph. Added `MovementAnalysisSection` (preset dropdown + "ANALYZE MOVEMENT →" button) and `BiomechanicalRadarCard` to `CalendarScreen` above the calendar grid. Dips reclassified from HPush to VPush in both `mapExercisesToVectors` (BalanceEngine.kt) and `classifyExercise()` (PresetAnalysisScreen.kt).

3. **Live GPS Run Tracker** — Added `RunTrackingService` foreground service with `FusedLocationProviderClient` for real-time GPS distance and pace tracking. Added `LiveRunScreen` composable with IDLE/RUNNING/PAUSED/FINISHED states, permission handling, live metrics display (elapsed time, distance, current/avg pace), and animated save/discard flow on finish. Added `play-services-location:21.3.0` dependency. Added `LIVE_RUN` route to NavGraph. Added "LIVE RUN" button with `DirectionsRun` icon to `RunHistoryScreen`. Bottom bar hidden on `live_run` route. All running UI uses fixed magenta (`#FF2D78`) accent color.

4. **GPS Accuracy Improvements** — Enhanced `RunTrackingService` with four layered accuracy improvements: tighter location request settings (1s interval, 500ms fastest), 8-second warmup period that silently discards all initial GPS points, 20m accuracy filter that rejects noisy fixes, and 2m minimum distance threshold to prevent micro-drift accumulation. Added a `KalmanFilter2D` inner class (constant-velocity 4-state model, `Q=0.0001`, `R=0.00003` degrees) that smooths incoming raw coordinates before computing distance deltas. All changes are internal to `RunTrackingService.kt`.

5. **Exercise History Screen** — Added `ExerciseHistoryScreen` with a scrollable LazyColumn showing every PREntry across all exercises, sorted newest-first. Each entry is color-coded: `pinnedAccentColor` (green) if it was a PR when logged, `exerciseAccentColor` (cyan) otherwise. Added "EXERCISE HISTORY" button to `DashboardScreen` and `EXERCISE_HISTORY` route to NavGraph. PR detection runs chronologically per-exercise with a running max.

6. **Workout History Screen** — Added `workoutHistory: List<WorkoutSession>` field to `StorageData` (Gson-safe default `emptyList()`). Completed workouts are prepended to history on `finishWorkout()`. Added `WorkoutHistoryScreen` with `GlowingCard`s showing preset name, date, elapsed duration, exercise breakdown. Deletion uses double-confirmation dialogs. Added "WORKOUT HISTORY" button to `DashboardScreen` and `WORKOUT_HISTORY` route to NavGraph. Added `deleteWorkoutHistoryEntry()` to `PRViewModel`. Wired through all save/load/sync paths.

7. **PR Color Change** — Changed PR entry value text and trophy icon in `ExerciseDetailScreen` from `SuccessPurple` (purple) to `pinnedAccentColor` (green) for consistency with the exercise history screen color scheme.

8. **QR Sync Crash Fix + Complete Data Transfer** — Moved QR code generation off the main thread in `SyncSendScreen` (now async via `LaunchedEffect` on `Dispatchers.Default` with loading spinner and error/retry UI). Added `workoutSession` to the QR payload in `generateSyncQRCodes()`. Added `runEntries` and `runningPRs` import to both REPLACE and MERGE modes in `importSyncData()`. MERGE mode now deduplicates run entries by ID and recomputes running PRs. Updated confirm dialog and success screen in `SyncReceiveScreen` to show all 6 data types (exercises, goals, weight, presets, runs, workouts).

9. **JSON Validator Skill** — Added `prtracker-json-validator` OpenCode skill that auto-validates JSON serialization integrity after any data class change. Ensures old `prtracker_backup.json` files always deserialize without crashing. 7 validation rules: field defaults, StorageData safety, save/load symmetry, nested class defaults, no breaking removals, Goal risk flag, ProGuard coverage. Auto-triggers on data class modifications and validates against the real backup file.

10. **Old Workout XP Migration** — Added `loadData()` migration that scans all `WorkoutSession` entries in `workoutHistory` where `xpEarned == 0L` (sessions from before the XP feature was added). For each, retroactively computes XP by matching session exercises to the current exercise list (by name) and summing `XpEngine.xpForEntry()` per completed set. Computed values are persisted to JSON so migration only runs once per session. Runs every app load but is idempotent — once saved, future loads skip already-migrated sessions.

11. **History Screen Search Bars** — Added `OutlinedTextField` search bars to both `ExerciseHistoryScreen` and `WorkoutHistoryScreen`. Exercise history filters entries by exercise name; workout history filters by preset name. Both use case-insensitive `contains` matching. Styled with the respective accent color for cursor/focus and `CardBackground` fill. Shows "NO MATCHES" centered text when search is active but no results match.

12. **Drag-and-Drop Reorder Fix** — Fixed dashboard exercise reorder by replacing `LayoutCoordinates`/`onGloballyPositioned`/`positionInRoot()` tracking with `LazyGridState.layoutInfo.visibleItemsInfo` content-space coordinates. The old approach caused stale coordinates when items were scrolled off-screen, making the last item in odd-count grids snap to the first position. New approach uses `LazyGridState.layoutInfo` to find nearest cards in content-space on both `onDragStart` and `onDragEnd`, with safe content-space arithmetic for `findClosestKey`. Removed `itemPositions` map and `onGloballyPositioned` modifier; added `dragStartPosInContent` and `dragItemSize` state vars.

13. **Exercise History Triple-Confirm Delete** — Added trash icon button per entry row in `ExerciseHistoryScreen`. Deleting requires a 3-step confirmation dialog (same pattern as DashboardScreen exercise deletion): Step 1 "Delete Entry", Step 2 "Are You Sure?", Step 3 "Final Confirmation" with red "Delete Permanently" — calls `viewModel.deleteEntry(exerciseId, entryId)`. Added `exerciseId: String` field to `ExerciseHistoryItem` data class to support the delete call.

14. **Workout XP Prediction** — Added `XpEngine.predictWorkoutXp(preset, exercises)` that estimates XP reward for completing a workout preset. For "until failure" exercises, uses the exercise's current PR as the value per set; for normal exercises, uses target reps/hold seconds. Shows "ESTIMATED XP: X,XXX" in the meta card on `WorkoutPresetDetailScreen`. During live workouts, `WorkoutSessionScreen` shows actual "XP EARNED: X" computed from completed sets using `XpEngine.xpForEntry()`.

15. **Locked Exercise Difficulty** — Removed automatic difficulty reclassification from `addExercise()`, `loadData()`, and `importSyncData()`. Exercise difficulty is now set once at creation (auto-classified by `ExerciseClassifier.classify()` or manually overridden by the user) and never changed afterward. The `ExerciseClassifier` import was removed from `PRViewModel`.

16. **Exercise Rename** — Added `renameExercise(exerciseId, newName)` to `PRViewModel` that updates the exercise name and cascades to goals, workout presets, active session, and workout history. Added pencil icon button in the `ExerciseDetailScreen` header row that opens a rename dialog with `OutlinedTextField`. Added chart Y-axis fix: `axisMinimum = 0f` forces zero at the bottom with no upper limit.

17. **Pet Dice Roll Mini-game** — Added `Pet` data model with 6 rarity tiers (COMMON→MYTHICAL, 45%→0.1% drop rate), 6 evolution tiers (NORMAL→RED_MATTER, 1x→32x coin multiplier), and 18 placeholder species. Added `DiceRollScreen` with IDLE/ROLLING/REVEAL/PET_DETAIL states, 3D dice animation, pity system (soft pity at 150 for Epic+, hard pity at 401 for Legendary, 2001 for Mythical), auto-roll toggle that persists across navigation, scrollable collection grid with tier-specific visual effects, pet fusion (5★ → next tier, consumes old pet), pet selling for coins, and animated coin counter. Added `petInventory`, `totalRolls`, `rollsSinceEpicOrAbove`, `rollsSinceLegendary`, `rollsSinceMythical`, `lastDiceRollTimestamp`, `coins` fields to `StorageData` with Gson-safe defaults. Added `rollDice()`, `fusePet()`, `sellPet()`, `toggleAutoRoll()` to `PRViewModel`. MERGE import deduplicates pets by ID (keeps higher tier then higher stars), maxOf for coins.

18. **Pet Upgrade Shop** — Added `PetUpgrade` enum with 3 upgrades: LUCK (+5%/level Epic+ pity bonus, Lv1=+5%, Lv10=+50%), ROLL_SPEED (dice animation delay −100ms/level, base 1600ms, min 600ms), LUCKY_ROLL (every 5th roll guaranteed Silver with coin multiplier, Lv1=2.5x, Lv10=11.5x). Each upgrade has 10 levels with exponential cost scaling (LUCK base 500 × 1.8x, ROLL_SPEED base 300 × 1.7x, LUCKY_ROLL base 1000 × 2.0x). Added `petUpgrades: Map<String, Int>` field to `StorageData` (Gson-safe default `emptyMap()`). Added `PetUpgradesScreen` with upgrade cards, progress bars, purchase buttons, coin balance. Added `purchaseUpgrade()` and `getUpgradeLevel()` to `PRViewModel`. DiceRollScreen coin counter is clickable and navigates to `PetUpgradesScreen`. Dice animation delay reads `roll_speed` upgrade level.

19. **Dice Tap-to-Roll** — During REVEAL state, the bottom dice is now tappable to trigger a new roll directly (sets ROLLING with dice animation). Previously required tapping the pet showcase to dismiss then tapping dice. Removes the need for a two-step dismiss+roll flow.

20. **Lucky Countdown Display** — Lucky roll countdown text now shows "LUCKY IN X" where X = rollsUntilLucky - 1 (e.g. "LUCKY IN 4" → "LUCKY IN 3" → "LUCKY IN 2" → "LUCKY IN 1"). When rollsUntilLucky == 1, displays "LUCKY ROLL!" in gold. Previously showed the raw counter value.

21. **Roll Chance Floor** — "1 in X CHANCE" display on pet reveal now floors at 2 (coerceAtLeast(2)). Never shows "1 in 1 CHANCE" which was misleading.

22. **RevealView Scrollable** — Pet reveal content (RevealView) is now scrollable via `verticalScroll` so all content ("TAP TO DISMISS" text, rarety badge, stars) is fully visible on all screen sizes. Removed "NEW!" text and unused `isNewPet` state variable. Later removed "+coins" animation from reveal.

23. **Coin Multiplier Upgrade** — Added `COIN_MULTIPLIER` to `PetUpgrade` enum (baseCost=600, costMultiplier=1.16f). Each level adds +0.20x to all coin earnings (rolls, fusing, selling). Infinite levels with exponential cost scaling. Applied in `rollDice()` via `getUpgradeLevel(PetUpgrade.COIN_MULTIPLIER)`. Effect text: "Coins: X.Xx per roll (+0.20x per level)".

24. **Inventory Modal Bottom Sheet** — Pet collection grid moved from always-on-screen to a `ModalBottomSheet` triggered by an inventory button ("📦 INVENTORY (count)"). Sheet contains the same LazyVerticalGrid (4-column, sorted favorites-first), FUSE ALL, SELL ALL buttons, and close button. Sheet dismisses when tapping a pet card (which then opens PET_DETAIL). Long-press for quick favorite toggle with haptic feedback. Note: later replaced by dedicated `PetInventoryScreen` full-screen route (see item 32).

25. **Dice Layout Restructure** — DiceRollScreen split into two zones: top area (weight 1f) for REVEAL/PET_DETAIL content, bottom area (220dp fixed) for dice. Dice shown during IDLE/ROLLING/REVEAL, hidden during PET_DETAIL. PetDetailView expands via weight(1f) during PET_DETAIL state.

26. **Pet Equip System** — Added `equipPet()`, `unequipPet()`, `petXpMultiplier()`, `maxEquipSlots()` to PRViewModel. Pets give XP multiplier based on rarity × tier × stars (additive stacking). Added `EQUIP_SLOTS` upgrade to PetUpgrade (fixed costs: 1M/10M/100M for 3/4/5 slots). Added `equippedPetIds` to `PetStorageData`, persisted to `pets.json`. Equipped pets shown on DiceRollScreen (emoji row + XP mult text), DashboardScreen (badges + XP mult), and HomeScreen (badges + XP mult). Equip/Unequip button in PetDetailView. Green checkmark badge on equipped PetCollectionCard. Cleaned equipped IDs on sell/fuse to prevent ghost slots.

27. **Split Storage** — App data (`prs.json`) and pet data (`pets.json`) now stored in separate JSON files. `PetStorageData` data class holds 9 pet-specific fields. Auto-migration from old `prs.json` on first launch via `StorageManager.migrateIfNeeded()`. Pet-only operations (roll, fuse, sell, upgrade, equip, favorite) write only `pets.json` for performance. `loadFullData()` reads both files; `saveFullData()` writes both files.

28. **Wipe Pet Data** — Added red "WIPE PET DATA" button in Settings with 3x confirmation (Step 1: list what's wiped, Step 2: are you sure, Step 3: final confirmation). Calls `viewModel.clearPetData()`.

29. **Export Data Type Picker** — SettingsScreen EXPORT DATA button opens dialog with APP / PET / BOTH pills. Shows data summary per type. Exports to Downloads with type-specific filenames: APP → `prtracker_app_backup.json`, PET → `prtracker_pets_backup.json`, BOTH → `prtracker_backup.json`.

30. **Coin Value × Stars** — `Pet.coinValue()` now returns `base × tierMultiplier × stars` (was `base × tierMultiplier`). Max single pet value: MYTHICAL RED_MATTER 5★ = 2,400,000 coins.

31. **Upgrade Cost** — Formula changed multiple times: exponential `costMultiplier^level` → dampened `costMultiplier^(level × 0.82f)` → power `baseCost × (level+1)^1.7` → recursive `baseCost × 1.10^level` → `baseCost × 1.25^level` (standard) and `baseCost × 1.8^level` (ROLL_SPEED). ROLL_SPEED max level set to 23 (0ms delay) with `maxLevel()` and `costForLevel()` overrides. All formula costs capped at 100M via `coerceAtMost(100_000_000L)` so upgrades never exceed 100M per level. Multipliers: LUCK 1.12x, COIN_MULTIPLIER 1.13x, ROLL_SPEED 1.10x, LUCKY_ROLL 1.15x.

32. **Inventory Full-Screen Route** — Pet collection grid moved from `ModalBottomSheet` to dedicated `PetInventoryScreen` route (`PET_INVENTORY`). Full-screen with back button, search, sort dropdown, 4-column grid, inline pet detail, and FUSE ALL / SELL ALL buttons. Removed ModalBottomSheet and all associated inventory state from DiceRollScreen.

33. **XP Earned Stored on Entries** — `PREntry` now includes `xpEarned: Long = 0L` field storing multiplied XP (pet + potion) at log time. `logEntry()` bakes XP into entry; `finishWorkout()` stores per-set XP. History screens read stored value. Old entries backfilled via migration on first load. `recalculateTotalXp()` now sums `entry.xpEarned` instead of recomputing from scratch.

34. **Custom Dice Weighted Distributions** — REFINING, ASCENDANT, and LEGENDARY dice now use `baseChances` (custom weighted rarity distributions) instead of simple min/max filtering. REFINING: RARE 61.7%/EPIC 25%/LEGENDARY 11.1%/MYTHICAL 2.2%. ASCENDANT: EPIC 63.9%/LEGENDARY 25%/MYTHICAL 11.1%. LEGENDARY: LEGENDARY 75%/MYTHICAL 25%. Luck multiplier applies on top; soft pity is skipped for custom-distribution dice; hard pity still overrides. BANISHING and MYTHIC continue using the old `minRarity`/`maxRarity` filter behavior.

35. **Upgrade Cost Cap** — Added `.coerceAtMost(100_000_000L)` to both formula branches in `PetUpgrade.costForLevel()`. All formula-based upgrades (LUCK, COIN_MULTIPLIER, ROLL_SPEED, LUCKY_ROLL) cap at 100M coins per level. Once a level would cost ≥ 100M, every subsequent level costs exactly 100M. EQUIP_SLOTS unchanged (already maxes at 100M for the 3rd slot).

---

## 12. OpenCode Skills

Skills live in `.opencode/skills/` and are auto-discovered by OpenCode. Each skill has a `SKILL.md` with YAML frontmatter (`name`, `description`) and a workflow body.

### `prtracker-json-validator`

**Trigger:** Auto on data class changes, or manual via "validate json", "check json", "json safe", "verify storage".

**Purpose:** Prevents JSON serialization breakage. Validates that every data class change preserves backward compatibility with existing `prtracker_backup.json` files.

**7 Rules:**

| #   | Rule                                      | What It Catches                                |
| --- | ----------------------------------------- | ---------------------------------------------- |
| 1   | New fields must have defaults             | Gson crash on old JSON missing the new key     |
| 2   | StorageData fields must all have defaults | Root container breakage — affects every user   |
| 3   | saveFullData matches StorageData          | Silent data loss on save                       |
| 4   | Nested classes must have defaults         | Crash from deserializing nested objects        |
| 5   | No breaking removals/renames              | Data loss from removed fields                  |
| 6   | Goal risk flag                            | Goal has zero defaults — any new field = crash |
| 7   | ProGuard coverage                         | Release-only crash from R8 stripping           |

**Workflow:** Reads changed `data/*.ct` files, audits field defaults, checks save/load symmetry, compares against CONTEXT.md, validates against `prtracker_backup.json`, and reports GREEN/YELLOW/RED verdict with exact fix instructions.

**Output:** GREEN (all safe) / YELLOW (warnings, no crash) / RED (breaking change + fix command).

### Other Skills

| Skill                       | Trigger                                | Purpose                                      |
| --------------------------- | -------------------------------------- | -------------------------------------------- |
| `prtracker-builder`         | "debug", "fix errors", "build the app" | Builds app and fixes compilation errors      |
| `prtracker-context-updater` | "save the data", "update context"      | Syncs CONTEXT.md with project state          |
| `prtracker-storage-field`   | "add field", "new field"               | Adds fields to data classes with Gson safety |
| `prtracker-proguard-rule`   | "add proguard", "keep rule"            | Adds ProGuard/R8 keep rules                  |
| `prtracker-bottom-nav-tab`  | "add tab", "new tab"                   | Adds new bottom navigation tabs              |
| `prtracker-explain-feature` | "explain", "how does X work"           | Explains features and components             |
| `prompt-writer`             | "write a prompt", "make a prompt"      | Generates coding prompts for PRTracker       |
