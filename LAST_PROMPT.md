Before making any changes, read CONTEXT.md in full, then read every file in the project source tree to fully understand the architecture, data flow, and conventions.

## FILES TO MODIFY

- `app/src/main/java/com/example/prtracker/ui/screens/WorkoutSessionScreen.kt`

## GOAL

Modify the active workout session screen so that next to each exercise, the reps progress shows total reps done out of total reps needed for the entire exercise (across all sets), not just against the per-set target. Currently it displays e.g. "15 / 10" (15 reps done vs 10 per set). It should display e.g. "15 / 30" (15 reps done vs 30 total needed = targetReps × totalSets). The sets progress ("X/Y sets") is already correct and should not change.

## STEP 1

In `app/src/main/java/com/example/prtracker/ui/screens/WorkoutSessionScreen.kt`, find the `SessionExerciseCard` composable function. Inside it, locate lines 296-297 where `doneTotal` and `label` are computed:

```kotlin
val doneTotal = progress.completedSets.sumOf { it.value }
val label = if (progress.isHold) "${doneTotal}s / ${progress.targetValue}s" else "$doneTotal / ${progress.targetValue}"
```

Replace the `label` computation line with:

```kotlin
val totalNeeded = progress.targetValue * progress.totalSets
val label = if (progress.isHold) "${doneTotal}s / ${totalNeeded}s" else "$doneTotal / $totalNeeded"
```

This changes the denominator from `progress.targetValue` (per-set target) to `progress.targetValue * progress.totalSets` (total reps/seconds needed across all sets). The `doneTotal` variable (sum of all completed set values) stays the same. The sets line `"$completedCount/${progress.totalSets} sets ($label)"` on line 299 is already correct and does not need to change.

## CONVENTIONS

No XML layouts — all UI is Jetpack Compose
No internet usage
No database — JSON only via StorageManager
All new data class fields must have defaults for Gson compatibility
Cache all Brush objects in remember { }
GridBackground() on every screen
Follow existing updateSettings() pattern for any settings changes
No new dependencies unless absolutely necessary
After implementing, build the project and fix any compilation errors before considering the task done
