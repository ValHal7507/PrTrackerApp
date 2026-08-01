---
name: prtracker-json-validator
description: Validate PRTracker JSON serialization integrity after any data class change. Ensures old prtracker_backup.json files always deserialize without crashing. Trigger when the user says "validate json", "check json", "json safe", "verify storage", "json compatibility", "validate storage", "check storage format", "verify json format". Also trigger automatically whenever a data class in app/src/main/java/com/example/prtracker/data/ is modified or a new data class is created.
---

# PRTracker JSON Validator

Verify that every data class change preserves backward compatibility with existing `prtracker_backup.json` files on users' devices. Gson fills missing JSON keys with field defaults — without a default, the app crashes.

## The 7 Rules

| # | Rule | What It Catches |
|---|---|---|
| 1 | New fields must have defaults | Gson crash on old JSON missing the new key |
| 2 | StorageData fields must all have defaults | Root container breakage — affects every user |
| 3 | saveFullData matches StorageData | Silent data loss on save (field exists but isn't written) |
| 4 | Nested classes must have defaults | Crash from deserializing nested objects missing keys |
| 5 | No breaking removals/renames | Data loss from removed fields |
| 6 | Goal risk flag | Goal has zero defaults — any new field = instant crash |
| 7 | ProGuard coverage | Release-only crash from R8 stripping classes |

## Data Classes to Audit

All live in `app/src/main/java/com/example/prtracker/data/`:

| Class | File | All fields have defaults? |
|---|---|---|
| `StorageData` | `StorageManager.kt` | Yes — safest to extend |
| `Exercise` | `Exercise.kt` | No (id, name, type, entries required) |
| `PREntry` | `PREntry.kt` | No (id, value, date required) |
| `Goal` | `Goal.kt` | **No — zero defaults, highest risk** |
| `WeightEntry` | `WeightEntry.kt` | No (id, weight, date required) |
| `RunEntry` | `RunEntry.kt` | No (id, distanceMeters, durationSeconds, date required) |
| `RunningPRs` | `RunningPRs.kt` | Yes — safest to extend |
| `AppSettings` | `AppSettings.kt` | Yes — safest to extend |
| `AppearanceSettings` | `AppearanceSettings.kt` | Yes — safest to extend |
| `PresetExercise` | `WorkoutPreset.kt` | Yes — safest to extend |
| `WorkoutPreset` | `WorkoutPreset.kt` | Yes — safest to extend |
| `SessionSetEntry` | `WorkoutSession.kt` | Yes — safest to extend |
| `SessionExerciseProgress` | `WorkoutSession.kt` | Yes — safest to extend |
| `WorkoutSession` | `WorkoutSession.kt` | Yes — safest to extend |
| `LeverageTelemetry` | `LeverageTelemetry.kt` | **No — zero defaults, highest risk** |

## Workflow

### Step 1: Read Changed Files

Scan `app/src/main/java/com/example/prtracker/data/*.kt` for any data class that was modified. Use `git diff` or read the files directly to see the current state.

### Step 2: Audit Field Defaults (Rules 1-4)

For every data class constructor parameter, verify it has either:
- A default value: `val fieldName: Type = defaultValue`
- A nullable type with null default: `val fieldName: Type? = null`

Flag any field missing both. Pay special attention to:
- `StorageData` — the root container, all fields MUST have defaults
- Nested classes inside `StorageData` (`PresetExercise`, `SessionExerciseProgress`, `SessionSetEntry`, `WorkoutSession`, `WorkoutPreset`, `AppearanceSettings`, `RunningPRs`)

### Step 3: Check save/load Symmetry (Rule 3)

Read `StorageManager.kt` and compare:
- `StorageData` constructor parameters
- `saveFullData()` function parameters

Every field in `StorageData` must have a corresponding parameter in `saveFullData()`. If a field exists in `StorageData` but not in `saveFullData()`, data is silently dropped on save.

### Step 4: Compare Against CONTEXT.md (Rule 5)

Read section 4 of `CONTEXT.md` (Data Models). Compare the documented fields against the actual data class fields. Detect:
- Removed fields (documented but no longer in source)
- Renamed fields (name changed between docs and source)

Both are breaking changes that lose data.

### Step 5: Flag Goal Class (Rule 6)

If any change touched `Goal.kt` or `LeverageTelemetry.kt`, emit a hard warning:

> GOAL/TELEMETRY RISK: This class has zero defaults. Any new field MUST have a default value or the app will crash when loading old prtracker_backup.json files.

Do not proceed until a default is confirmed.

### Step 6: Verify ProGuard (Rule 7)

Read `app/proguard-rules.pro`. Confirm the blanket rule exists:
```
-keep class com.example.prtracker.** { *; }
```

If a new data class is added outside the `com.example.prtracker` package, flag that it needs an explicit keep rule.

### Step 7: Validate Against Backup (Rules 1-4)

Read `prtracker_backup.json` from the project root. Parse the JSON and check:

1. List all top-level keys present in the backup
2. For each data class, list the fields it expects
3. For every field missing from the backup, verify the data class has a default value
4. If any field is missing AND has no default → that's a crash

**Known safe missing fields** (your backup may lack these — they have defaults):
- `runningPRs.best10kmSeconds` — `Int? = null`
- `workoutSession` may be absent — `WorkoutSession? = null`

### Step 8: Report Results

Output a clear verdict:

```
JSON VALIDATION RESULT
======================
Rules 1-4 (field defaults): PASS / FAIL
Rule 5 (no removals): PASS / FAIL
Rule 6 (Goal safety): PASS / WARN
Rule 7 (ProGuard): PASS / FAIL
Backup compatibility: PASS / FAIL

Verdict: GREEN / YELLOW / RED
```

**GREEN** — All 7 rules pass. JSON format is safe. Old backups will deserialize correctly.

**YELLOW** — Warnings found but nothing that will crash. Explain the risk.

**RED** — Breaking change detected. Provide the exact fix:

```
FIX: Add a default value to DataClass.kt:
  Change: val newField: Type
  To:     val newField: Type = defaultValue
```

Do not suggest moving on until the fix is applied and re-validated.

## Common Patterns That Break JSON

| Pattern | Why It Breaks | Fix |
|---|---|---|
| `val newField: String` | Gson sets null on non-nullable → crash | `val newField: String = ""` |
| `val newField: Int` | Gson sets 0 on non-nullable but field name missing from old JSON | `val newField: Int = 0` |
| `val newField: Boolean` | Same as above | `val newField: Boolean = false` |
| Removing a field | Data lost on next save | Keep the field, deprecate it instead |
| Renaming a field | Old JSON key no longer matches → field gets default | Keep old name or add migration |
| Adding to Goal/Telemetry | These classes have zero defaults | MUST add default value |
