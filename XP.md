# PRTracker — XP & Level System

## Overview

Every exercise entry you log awards XP based on the exercise's **difficulty tier** and **type** (reps vs hold). XP accumulates over time and drives a 1–200 level progression system displayed on the HomeScreen.

---

## 1. XP Rates

| Difficulty | XP per rep | XP per second (hold) |
|---|---|---|
| EASY | 20 | 8 |
| MEDIUM | 50 | 20 |
| HARD | 120 | 50 |
| EXTREME | 300 | 120 |

### Formula

```
entryXp = entryValue × rate(difficulty, type)
```

Where:
- For **reps** exercises: `rate = xpPerRep(difficulty)`
- For **hold** exercises: `rate = xpPerSecond(difficulty)`

### Examples

| Exercise | Entry | Difficulty | XP Awarded |
|---|---|---|---|
| Push-ups | 50 reps | EASY | 50 × 20 = 1,000 XP |
| Dips | 15 reps | HARD | 15 × 120 = 1,800 XP |
| Hollow hold | 60s | MEDIUM | 60 × 20 = 1,200 XP |
| Pull-ups | 10 reps | MEDIUM | 10 × 50 = 500 XP |
| Muscle-ups | 5 reps | HARD | 5 × 120 = 600 XP |
| Planche lean | 30s | EXTREME | 30 × 120 = 3,600 XP |

---

## 2. Difficulty Classification

Exercises are auto-classified by name using `ExerciseClassifier` (data/ExerciseDifficulty.kt) with a two-pass approach:

### Pass 1 — Known-exercise map
Longest substring match against a hardcoded table (case-insensitive).

| Exercise | Difficulty |
|---|---|
| Decline Push-Ups | MEDIUM |
| Pike Push-Ups | HARD |
| Hollow hold / Hollow body hold | MEDIUM |
| Pull-ups, Chin-ups, Push-ups | MEDIUM |
| Dips | HARD |
| Muscle-ups, HSPU, L-sit, Levers | HARD |
| Pseudo planche (all variants) | HARD |
| Planche, Full planche, Straddle planche, Tuck planche, Planche lean | EXTREME |
| One-arm push/pull, Human flag, Iron cross | EXTREME |

### Pass 2 — Keyword fallback
Scanned in order: EXTREME → HARD → MEDIUM → EASY.

### Default
If nothing matches, difficulty defaults to **MEDIUM**.

### Manual override
In the Add Exercise screen, the auto-classified difficulty shows an "AUTO" label. Tapping a different difficulty pill overrides the classification for that exercise.

---

## 3. Level Formula

```
xpNeededForNextLevel(level) = floor(450 × level^0.87)
```

This produces a gentle exponential curve — early levels are quick, later levels require consistent training. Cumulative XP to reach max level 200 is ~4.8M.

- Formula source: `XpEngine.xpNeededForNextLevel()`
- Max level: **200**
- At level 200: `xpNeededForNextLevel` returns `Long.MAX_VALUE`

### Level Table

| Level | XP to next level | Cumulative XP to reach this level |
|---|---|---|
| 1 → 2 | 450 | 0 |
| 2 → 3 | 822 | 450 |
| 3 → 4 | 1,176 | 1,272 |
| 4 → 5 | 1,518 | 2,448 |
| 5 → 6 | 1,851 | 3,966 |
| 6 → 7 | 2,177 | 5,817 |
| 7 → 8 | 2,497 | 7,994 |
| 8 → 9 | 2,812 | 10,491 |
| 9 → 10 | 3,122 | 13,303 |
| 10 → 11 | 3,429 | 16,425 |
| 15 → 16 | 4,774 | 34,455 |
| 20 → 21 | 6,053 | 57,661 |
| 25 → 26 | 7,288 | 85,070 |
| 50 → 51 | 13,025 | 316,768 |
| 75 → 76 | 18,255 | 666,922 |
| 100 → 101 | 23,141 | 1,128,187 |
| 150 → 151 | 32,466 | 2,375,662 |
| 199 → 200 | 41,147 | 3,987,706 |
| 200 (max) | ∞ | ≈ 4,833,015 |

---

## 4. Helper Functions

All in `XpEngine` (`data/XpEngine.kt`):

### `levelFromTotalXp(totalXp: Long): Int`
Walks the cumulative XP table from level 1 upward and returns the highest level whose cumulative threshold does not exceed `totalXp`.

### `xpInCurrentLevel(totalXp: Long): Long`
Returns the XP earned within the current level: `totalXp - cumulativeXpToReach(currentLevel)`.

### `xpNeededForCurrentLevelUp(totalXp: Long): Long`
Returns the XP still needed to advance: `xpNeededForNextLevel(currentLevel)`.

### `cumulativeXpToReach(targetLevel: Int): Long`
Sums `xpNeededForNextLevel(lvl)` for all levels from 1 up to `targetLevel - 1`.

### `computeTotalXpFromExercises(exercises: List<Exercise>): Long`
Retroactively computes total XP from all existing entries using their stored difficulty.

---

## 5. When XP is Awarded

| Action | XP Awarded |
|---|---|
| Logging a new PREntry | Yes, immediately via `logEntry()` |
| Completing a workout session | Yes, per completed set via `finishWorkout()` — stored in `WorkoutSession.xpEarned` |
| Deleting an entry | XP is recomputed from scratch by `recalculateTotalXp()` — drift-proof |
| Importing data (MERGE mode) | Re-computed from merged exercises via `recalculateTotalXp()` |
| Importing data (REPLACE mode) | Re-computed from imported exercises via `recalculateTotalXp()` |
| Clearing all data | Total XP reset to 0 |

### Migration: Old Workout History

On every app load, `loadData()` scans all `WorkoutSession` entries in `workoutHistory`. Any session where `xpEarned == 0L` (i.e. sessions from before the XP feature was added) gets its XP retroactively computed by matching each session exercise to the current exercise list (by name) and summing `XpEngine.xpForEntry()` per completed set. The computed values are persisted to JSON.

---

## 6. UI Display

| Screen | What Shows |
|---|---|
| **HomeScreen** | `LevelProgressCard` — level number (or "MAX" at 200), animated progress bar with gradient fill, XP text in "X,XXX / X,XXX" format, "+X XP BEYOND MAX" when at max level |
| **SettingsScreen** | TOTAL XP card showing formatted XP and current level |
| **LogEntryScreen** | "+X XP" inline preview below the value counter; passes `xpEarned` to PRCelebrationOverlay |
| **HoldTimerScreen** | "+X XP" inline preview below the timer ring; passes `xpEarned` to PRCelebrationOverlay |
| **PRCelebrationOverlay** | "+X XP" displayed at the bottom of the overlay |
| **ExerciseHistoryScreen** | "+X XP" per entry on its own line next to the value |
| **WorkoutHistoryScreen** | "+X XP" badge on its own dedicated line per card, read from `WorkoutSession.xpEarned` |
| **WorkoutSessionScreen** | "+X XP" shown on completion screen between "WORKOUT COMPLETE" and "GO BACK" |

### XP Recalculation

XP is always recomputed from scratch after every mutation via `recalculateTotalXp()`:
- Called after `logEntry()`, `deleteEntry()`, `finishWorkout()`, `clearAllData()`, `importSyncData()`
- Uses `computeTotalXpFromExercises()` which iterates all entries across all exercises
- Drift-proof — no manual addition or subtraction anywhere
