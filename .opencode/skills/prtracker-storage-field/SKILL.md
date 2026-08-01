---
name: prtracker-storage-field
description: Add a new field to a data class in PRTracker with safe Gson backward compatibility. Trigger when the user says "add field", "add a field", "new field", "extend data class", "add column", "add a field to", or any request to modify a data class with a new property. Do NOT trigger for editing existing fields, renaming fields, or removing fields.
---

# PRTracker Storage-Safe Field Adder

Add a new field to an existing data class and ensure it survives Gson deserialization without crashing on old `prs.json` files already on users' devices.

## Data Classes and Their Field Styles

All data classes live in `app/src/main/java/com/example/prtracker/data/`.

| Class | File | Pattern | @Immutable | All fields have defaults? |
|---|---|---|---|---|
| `Exercise` | `data/Exercise.kt` | Required fields + optional with defaults | Yes | No (id, name, type, entries required) |
| `PREntry` | `data/PREntry.kt` | Required fields + optional with defaults | Yes | No (id, value, date required) |
| `Goal` | `data/Goal.kt` | All fields required, **no defaults** | Yes | **No** — be careful! |
| `WeightEntry` | `data/WeightEntry.kt` | Required fields + optional with defaults | Yes | No (id, weight, date required) |
| `RunEntry` | `data/RunEntry.kt` | Required fields + optional with defaults + computed vals | Yes | No (id, distanceMeters, durationSeconds, date required) |
| `RunningPRs` | `data/RunningPRs.kt` | All fields have defaults (nullable or literal) | Yes | **Yes** — safest to extend |
| `AppSettings` | `data/AppSettings.kt` | All fields have defaults | No | **Yes** — safest to extend |
| `AppearanceSettings` | `data/AppearanceSettings.kt` | All fields have defaults | Yes | **Yes** — safest to extend |
| `LeverageTelemetry` | `data/LeverageTelemetry.kt` | All fields required, **no defaults** | Yes | **No** — be careful! |
| `StorageData` | `data/StorageManager.kt` | All fields have defaults | Yes | **Yes** — safest to extend |
| `PresetExercise` | `data/WorkoutPreset.kt` | All fields have defaults | Yes | **Yes** — safest to extend |
| `WorkoutPreset` | `data/WorkoutPreset.kt` | All fields have defaults | Yes | **Yes** — safest to extend |

## Workflow

### Step 1: Ask the User for Input

Before doing anything, ask for:

1. **Which data class?** — e.g. `Exercise`, `PREntry`, `Goal`, `WeightEntry`, `AppSettings`, `RunEntry`, `RunningPRs`, `AppearanceSettings`, `PresetExercise`, `WorkoutPreset`, `LeverageTelemetry`, `StorageData`
2. **Field name, type, and default value** — e.g. `val isPinned: Boolean = false`
3. **UI/ViewModel scope?** (optional) — does the user also want the field exposed in the ViewModel, or surfaced in any UI screen?

### Step 2: Validate the Default Value

**CRITICAL RULE:** Every new field MUST have a default value. Gson will set the field to its default when the key is missing from old JSON. Without a default, deserialization of old `prs.json` files will crash.

If the user specifies a non-nullable field **without** a default (e.g. `val newField: String` with no `= value`), warn immediately:

> ⚠️ Adding a non-nullable field without a default value will cause Gson to crash when loading existing `prs.json` files. Use `val newField: String = ""` or `val newField: String? = null` instead.

Do not proceed until a default is provided.

### Step 3: Add the Field to the Data Class

1. Read the data class file
2. Use the `edit` tool to add the new field. Add it **after existing fields** but **before the closing parenthesis** of the data class constructor
3. Follow the existing formatting style (continued lines, trailing commas)

**Existing formatting examples:**

```kotlin
// Exercise — fields on separate lines with trailing comma
data class Exercise(
    val id: String,
    val name: String,
    val type: String,
    val entries: List<PREntry>,
    val isPinned: Boolean = false,
    val sortOrder: Int = 0,
    val goal: Int? = null
)

// AppSettings — compact, trailing comma on last field
data class AppSettings(
    val weightUnit: String = "kg",
    val targetWeight: Float? = null,
    val calendarDayViewMode: String = "pr",
    val morningReminderHour: Int = 8,
    val morningReminderMinute: Int = 0,
    val eveningReviewHour: Int = 21,
    val eveningReviewMinute: Int = 30,
    val soundEnabled: Boolean = true,
    val soundVolume: Float = 0.5f,
    val hapticEnabled: Boolean = true,
    val appearance: AppearanceSettings = AppearanceSettings()
)
```

### Step 4: Check StorageManager.kt for Migration Need

Read `StorageManager.kt` and determine:

- **Default-only field (safe):** If every existing load path (both `StorageData` path and old array fallback) creates instances with defaults, Gson will fill the missing key with the default. No migration needed.
- **Explicit `copy()` usage:** If `loadFullData()` uses `data.copy(...)`, any field NOT in the `copy()` call is preserved from the deserialized object — which means Gson assigns the default. This is safe.
- **Computed migration needed:** If the new field's value needs to be *derived* from existing fields (e.g. combining two values), add migration logic in `loadFullData()` after the Gson deserialization line but before returning.

**Typical migration pattern (rarely needed):**

```kotlin
// In loadFullData(), after gson.fromJson() succeeds:
val migrated = data.exercises.map { exercise ->
    if (exercise.newField == null) {  // detect old data
        exercise.copy(newField = deriveFrom(exercise))
    } else {
        exercise
    }
}
data.copy(exercises = migrated)
```

If the field is on `StorageData` itself, check the old array fallback path too (lines 41-47). The fallback creates `StorageData(exercises = sorted)` — since `StorageData` fields all have defaults, this is automatically safe.

### Step 5: Update CONTEXT.md

1. Read the relevant section of CONTEXT.md that documents the modified data class (section 4)
2. Add the new field to the Kotlin code block in the same position it appears in the source file
3. Add a comment explaining the field (same style as surrounding fields)

**Example edit — adding to Exercise:**

The `Exercise` code block in CONTEXT.md section 4.1 currently lists:
```
    val sortOrder: Int = 0,   // Order within pinned/unpinned section
    val goal: Int? = null     // Optional per-exercise target value (null = no goal set)
```

If you added `val newField: String = ""` after `goal`, update the block to:
```
    val sortOrder: Int = 0,   // Order within pinned/unpinned section
    val goal: Int? = null,    // Optional per-exercise target value (null = no goal set)
    val newField: String = "" // Description of the new field
```

### Step 6: (Optional) Expose in ViewModel

If the user asked for ViewModel/UI exposure:

1. **ViewModel (`PRViewModel.kt`)**: Check if a new `StateFlow` is needed. If the field is on `Exercise`, existing flows `exercises`, `pinnedExercises`, `unpinnedExercises` already include it automatically since they derive from the full list. If the field needs a dedicated flow or a new mutation function, add it following the existing pattern.
2. **UI**: Update relevant screens to read or display the new field. Use `edit` to make minimal, targeted changes.

### Step 7: Confirm

Summarize what was done:

> Added `fieldName: Type = defaultValue` to `DataClass`.
> StorageManager: no migration needed (Gson default handles it) / [migration details].
> CONTEXT.md: updated section 4.
> [ViewModel/UI: updated if applicable.]
