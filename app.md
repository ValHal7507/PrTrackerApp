# PRTracker — Complete App Reference

## 1. What Is This App

PRTracker is a fully offline Android app for tracking personal records (PRs) in exercises. Users create exercises (e.g. "Push-ups"), log reps or hold-seconds, and track PRs over time. No login, no database — everything is a JSON file on the device.

---

## 2. Tech Stack

| Category | Choice |
|---|---|
| Language | Kotlin 2.2.10 |
| Min SDK | Android 8.0 (API 26) |
| Target SDK | Android 16 (API 36) |
| UI | Jetpack Compose + Material3 |
| Navigation | Compose Navigation 2.7.5 |
| JSON | Gson 2.10.1 |
| Charts | MPAndroidChart 3.1.0 (via JitPack) |
| Animations | Lottie Compose 6.1.0 |
| Background | WorkManager 2.9.0 |
| AOT Profiles | profileinstaller 1.3.1 + baselineprofile plugin 1.5.0-alpha06 |
| Build | AGP 9.2.1, Gradle 9.4.1, Kotlin DSL |
| Icons | Material Icons Extended |

---

## 3. Data Models (The "Database")

This app has **no SQL database**. All data is persisted as a single JSON file (`context.filesDir/prs.json`) serialized by Gson.

### Top-level wrapper

```kotlin
data class StorageData(
    val exercises: List<Exercise> = emptyList(),
    val goals: List<Goal> = emptyList(),
    val weightEntries: List<WeightEntry> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val restDays: List<String> = emptyList()  // "YYYY-MM-DD"
)
```

### Exercise

```kotlin
data class Exercise(
    val id: String,           // UUID.randomUUID().toString()
    val name: String,         // User-given name
    val type: String,         // "reps" or "hold" (seconds)
    val entries: List<PREntry>,
    val isPinned: Boolean = false,
    val sortOrder: Int = 0,   // Within pinned/unpinned sections
    val goal: Int? = null     // Optional per-exercise target
)
```

### PREntry

```kotlin
data class PREntry(
    val id: String,           // UUID.randomUUID().toString()
    val value: Int,           // Rep count or seconds held
    val date: Long,           // System.currentTimeMillis()
    val note: String = ""
)
```

### Goal

```kotlin
data class Goal(
    val id: String,
    val exerciseId: String,
    val exerciseName: String,  // Denormalized for display
    val targetValue: Int,
    val period: String,        // "daily" | "weekly" | "monthly"
    val createdAt: Long,
    val type: String           // "reps" | "hold" (from exercise)
)
```

### WeightEntry

```kotlin
data class WeightEntry(
    val id: String,
    val weight: Float,
    val date: Long,
    val note: String = ""
)
```

### AppSettings

```kotlin
data class AppSettings(
    val weightUnit: String = "kg",  // "kg" | "lbs"
    val targetWeight: Float? = null
)
```

---

## 4. Storage Mechanism

- **File:** `context.filesDir/prs.json`
- **Library:** Gson serialization/deserialization
- **Class:** `StorageManager` in `com.example.prtracker.data`
- **Load timing:** Called once in `PRViewModel.init{}` via `loadData()`
- **Save timing:** After every mutation (add/delete exercise, log/delete entry, add/delete goal, clear all, pin/unpin, reorder, add/delete weight entry, change settings)
- **Backward compatibility:** Old format (plain `List<Exercise>` array) is auto-detected and migrated on load
- **Sorting:** Exercises are sorted by `isPinned` (descending), then `sortOrder` (ascending)

### StorageManager API

```kotlin
fun loadData(): Pair<List<Exercise>, List<Goal>>
fun loadFullData(): StorageData
fun saveData(exercises: List<Exercise>, goals: List<Goal>)
fun saveFullData(exercises, goals, weightEntries, settings, restDays = emptyList())
fun loadExercises(): List<Exercise>
fun saveExercises(exercises: List<Exercise>)
```

---

## 5. Architecture

### ViewModel: PRViewModel

Extends `AndroidViewModel(application)`. Single source of truth for all app state.

**StateFlows:**
| Flow | Type | Description |
|---|---|---|
| `exercises` | `StateFlow<List<Exercise>>` | All exercises |
| `goals` | `StateFlow<List<Goal>>` | All goals |
| `weightEntries` | `StateFlow<List<WeightEntry>>` | All weight entries |
| `appSettings` | `StateFlow<AppSettings>` | User settings (weightUnit, targetWeight) |
| `hapticEnabled` | `StateFlow<Boolean>` | Haptic feedback toggle (in-memory only, not persisted) |
| `restDays` | `StateFlow<List<String>>` | Rest day date strings ("YYYY-MM-DD") |
| `currentStreak` | `StateFlow<Int>` | Derived — consecutive workout days, rest days preserve chain |
| `hapticEvent` | `SharedFlow<Unit>` | One-shot haptic trigger consumed by CalendarScreen |
| `pinnedExercises` | Derived, sorted by `sortOrder` | Exercises where `isPinned == true` |
| `unpinnedExercises` | Derived, sorted by `sortOrder` | Exercises where `isPinned == false` |

**Key functions:**
| Function | Description |
|---|---|
| `addExercise(exercise)` | Appends with auto-incremented `sortOrder` |
| `logEntry(exerciseId, entry)` | Appends entry, moves exercise to front |
| `deleteExercise(exerciseId)` | Removes exercise + all linked goals |
| `deleteEntry(exerciseId, entryId)` | Removes specific entry |
| `clearAllData()` | Clears exercises, goals, weight entries, settings |
| `isNewPR(exerciseId, value)` | True if value > existing max (or no entries) |
| `togglePin(exerciseId)` | Flips `isPinned`, recalculates sort orders |
| `swapExercises(id1, id2)` | Swaps `sortOrder` values (for drag reorder) |
| `addGoal(goal)` / `deleteGoal(goalId)` | CRUD for goals |
| `getProgressForGoal(goal)` | Sums entry values within current period window |
| `getProgressPercent(goal)` | Returns 0f..1f |
| `getTimeRemaining(goal)` | Human-readable string ("Resets in 5h 30m") |
| `addWeightEntry(entry)` / `deleteWeightEntry(id)` | CRUD for weight |
| `getCurrentWeight()` / `getLowestWeight()` / `getHighestWeight()` / `getAverageWeight()` | Weight stats |
| `setWeightUnit(unit)` | Converts all entries to new unit |
| `setTargetWeight(weight)` | Sets weight goal |
| `toggleTodayAsRestDay()` | Toggles today's rest day status, saves, emits haptic |
| `calculateCurrentStreak()` | Private: walks backwards, counts consecutive workout days |

**Period window logic:**
| Period | Window |
|---|---|
| `daily` | Today midnight to midnight |
| `weekly` | Current Monday 00:00 to Sunday 23:59 |
| `monthly` | 1st to last day of month |

All period math uses `java.util.Calendar` (not `java.time`) for API 26 compatibility.

---

## 6. Navigation

### Routes (defined in `com.example.prtracker.navigation.Routes`)

| Route | Screen | Back button |
|---|---|---|
| `"dashboard"` | DashboardScreen | None (start) |
| `"goals"` | GoalsScreen | Via nav bar |
| `"add_goal"` | AddGoalScreen | Arrow icon |
| `"add_exercise"` | AddExerciseScreen | Arrow icon |
| `"log_entry/{exerciseId}"` | LogEntryScreen | Arrow icon |
| `"detail/{exerciseId}"` | ExerciseDetailScreen | Arrow icon |
| `"calendar"` | CalendarScreen | Via nav bar |
| `"settings"` | SettingsScreen | Via nav bar |
| `"weight"` | WeightScreen | Via nav bar |
| `"log_weight"` | LogWeightScreen | Arrow icon |

Helper functions: `Routes.logEntry(id)`, `Routes.detail(id)`

### Navigation Flow

```
Dashboard ──tap card──→ ExerciseDetailScreen
Dashboard ──FAB────────→ AddExerciseScreen
Dashboard ──bottom nav──→ Calendar / Settings / Goals / Weight

GoalsScreen ──FAB──────→ AddGoalScreen
WeightScreen ──FAB─────→ LogWeightScreen

ExerciseDetailScreen ──"LOG ENTRY"──→ LogEntryScreen

LogEntryScreen ──log (PR)──→ PRCelebrationOverlay ──auto-dismiss──→ pop back
LogEntryScreen ──log (no PR)──→ pop back

AddExerciseScreen ──confirm──→ pop to Dashboard
AddGoalScreen ──confirm──→ pop to GoalsScreen
```

Bottom nav visible on: `dashboard`, `goals`, `calendar`, `weight`, `settings`.
FAB visible only on: `dashboard` (add exercise), `goals` (add goal), `weight` (log weight).

---

## 7. Screen Details

### DashboardScreen
- GridBackground animated background
- Title "PR TRACKER" with glowing underline
- Search bar filters exercises by name
- LazyVerticalGrid 2 columns, split into PINNED + ALL EXERCISES sections
- Pinned cards: neon green/red color scheme
- Unpinned cards: cyan/purple color scheme
- Drag-and-drop reorder with long-press, boundary enforcement between pinned/unpinned
- ExerciseCard: pin icon, delete icon, streak indicator, name, PR value, type label, AnimatedRing
- Empty state: "NO EXERCISES YET"
- First-frame deferred visibility via `animateFloatAsState` (300ms fade-in)

### AddExerciseScreen
- Name field, type toggle (REPS / HOLD), goal field (not functional), confirm button
- Confirmed exercise gets `sortOrder = max + 1`

### LogEntryScreen
- Shows exercise name, current PR, large animated value
- Up/down increment buttons + center text field
- Optional note field
- "NEW PR!" badge when value > max
- "LOG IT" button: creates PREntry, haptic feedback if enabled
- If new PR: shows `PRCelebrationOverlay` with Lottie confetti + purple flash

### ExerciseDetailScreen
- Line chart (MPAndroidChart wrapped in AndroidView)
- Entry history list (newest first), each row shows trophy icon if it was a PR
- Delete entry button (X) on each row
- Delete exercise button in header with confirmation dialog
- Collects `viewModel.exercises` as state for instant UI updates

### GoalsScreen
- LazyColumn of GoalCards with progress ring, percentage, period badge, time remaining
- Green completion state when 100%
- FAB to add goal

### AddGoalScreen
- Exercise dropdown selector, target value field, period toggle (DAILY/WEEKLY/MONTHLY)
- Preview line in GlowingCard
- "SET GOAL" button enabled when all fields valid

### SettingsScreen
- Haptic toggle (not persisted — resets on restart)
- Export data to Downloads folder
- Clear all data (with confirmation)
- Version text

### WeightScreen
- Weight chart, stats cards, weight entry history, FAB to log weight

### LogWeightScreen
- Weight input with up/down buttons, unit display (from settings), note field

### CalendarScreen
- GridBackground, title "SYSTEM CHRONO // OVERVIEW" with glowing underline
- Streak banner with flame emoji, "CURRENT STREAK: X DAYS" with animateIntAsState count-up (800ms)
- Monthly rest day count in secondary text
- Month nav with left/right arrows, AnimatedContent horizontal slide, future months blocked
- 7-column LazyVerticalGrid calendar with 5 cell types:
  - WORKOUT: dark card, neon cyan border, filled dot
  - REST: electric purple 40% alpha fill, pause symbol
  - MISSED: dim gray text, faint neon red underline
  - FUTURE: 20% alpha white, not tappable
  - EMPTY_PAD: invisible filler
  - TODAY: extra neon cyan ring outline around all types
- Tapping WORKOUT day: AlertDialog listing exercises with max values
- Tapping REST day: AlertDialog with "Rest day — chain preserved"
- Bottom button: "MARK TODAY AS REST DAY" (cyan) or "UNMARK REST DAY" (red outline)
- Haptic feedback on rest day toggle via SharedFlow

---

## 8. Custom UI Components

| Component | File | Description |
|---|---|---|
| `GlowingCard` | `ui/components/GlowingCard.kt` | Semi-transparent card with 1dp gradient border, 16dp corners. Accepts optional `borderBrush`. |
| `NeonButton` | `ui/components/NeonButton.kt` | Full-width cyan button (56dp height) with gradient border overlay. |
| `AnimatedRing` | `ui/components/AnimatedRing.kt` | Canvas-drawn circular progress ring (60dp), sweep gradient, animated 1s mount. Accepts custom colors. |
| `GridBackground` | `ui/components/GridBackground.kt` | Full-screen animated grid (80dp spacing, 3% alpha cyan), 30fps via `delay(32)`, drawn in `drawBehind`. |
| `PRCelebrationOverlay` | `ui/components/PRCelebrationOverlay.kt` | Full-screen overlay: purple flash → Lottie confetti → "NEW PERSONAL RECORD!" text. Auto-dismisses after 2.5s. |

---

## 9. Theme & Design

### Color Palette (Color.kt)
```kotlin
Background           = #050A18  (deep navy)
Surface              = #0D1526  (lighter navy)
CardBackground       = #0F1C35 at 60% alpha
PrimaryAccent        = #00F5FF  (neon cyan)
SecondaryAccent      = #7B2FFF  (electric purple)
SuccessPurple        = #B026FF  (neon purple)
TextPrimary          = #E8F4FD  (near-white)
TextSecondary        = #6B8CAE  (muted blue-gray)
GoalComplete         = #00FF85  (neon green)
PinnedAccent         = #00FF85  (neon green — same as GoalComplete)
PinnedAccentSecondary = #FF003C (neon red)
```

### Typography
- Titles & PR numbers: `FontFamily.Monospace` (stand-in for Orbitron)
- Body text: `FontFamily.Default` (system font)

### Dark-Only Theme
- `darkColorScheme` with custom colors, no light mode
- Material3 `MaterialTheme` wrapper

---

## 10. Background Workers

### GoalNotificationWorker (daily 9:00 AM)
- Reads `prs.json` directly via `StorageManager`
- For each goal where `progress < targetValue`: posts a separate notification
- Uses `ic_goal_notification` drawable as small icon
- `BigTextStyle` with `setProgress` bar
- Skips if `POST_NOTIFICATIONS` denied on Android 13+

### WeeklySummaryWorker (Sundays 10:00 PM)
- Reads all data including weight entries and settings
- Computes: new PRs this week, goals completed, total volume, weight change, most improved
- Posts one expanded notification with emoji-formatted summary

Both schedules are set up in `MainActivity.onCreate()`.

---

## 11. Key Conventions for Development

1. **No XML layouts** — all UI is Compose
2. **No database** — JSON file with Gson, single file `prs.json`
3. **No internet required** — only the Lottie animation URL is fetched (optional)
4. **One ViewModel** — `PRViewModel` is the single source of truth; use `collectAsState()` not snapshot functions
5. **Colors from theme** — never hardcode colors; use values from `Color.kt`
6. **All screens need `GridBackground`** — include as base layer in every screen's `Box`
7. **Cache Brushes** — wrap `Brush.*` in `remember { }` to avoid reallocation
8. **`@Immutable` on data classes** — enables Compose smart recomposition
9. **Item keys** — `LazyColumn`/`LazyVerticalGrid` items must use `key = { it.id }`
10. **`derivedStateOf`** — wrap expensive ViewModel reads in lazy list lambdas
11. **Android API 26+** — use `java.util.Calendar` not `java.time`
12. **Haptic preference** is in-memory only — not persisted

---

## 12. Performance Optimizations Applied

- **Baseline Profile module** (`:baselineprofile`) — generates AOT compilation profile for instant startup
- **profileinstaller** library reads the baseline profile at install time
- **Deferred visibility** on DashboardScreen — grid content fades in 300ms after first frame
- **30fps cap** on GridBackground animation — `delay(32)` after each `withFrameMillis`
- `@Immutable` annotations on all data classes
- Cached `Brush` objects in `remember { }`
- `drawBehind`-based grid background (no composable recomposition)

---

## 13. Build Variants

| Variant | Minification | Shrinking | Debuggable | Signing |
|---|---|---|---|---|
| `debug` | no | no | yes | debug |
| `release` | no | no | no | release (default) |
| `releaseDebuggable` | no | no | yes | debug |

Note: `isMinifyEnabled` is set to `false` for all variants because R8 stripping causes runtime crashes. The performance improvements come from the baseline profile + deferred rendering + 30fps cap instead.
