---
name: prtracker-bottom-nav-tab
description: Add a new tab to the bottom navigation bar in PRTracker. Requires editing MainActivity.kt (nav items list + FAB visibility), NavGraph.kt (new route + composable entry), creating a new screen file, and updating CONTEXT.md. Trigger when the user says "add tab", "new tab", "add bottom nav tab", "add navigation tab", or any request to add a new item to the bottom navigation bar. Do NOT trigger for adding non-tab routes or modifying existing tabs.
---

# PRTracker Bottom Nav Tab Adder

Add a new tab to the bottom navigation. There are **3 files to edit** (sometimes 4) and one file to create. Missing any step will break the build or leave the nav non-functional.

## Architecture Overview

The bottom nav has 3 touch points:

1. **`navigation/NavGraph.kt`** — Route constant in `Routes` object + composable entry in `NavHost`
2. **`MainActivity.kt`** — Icon import + nav item `Triple` in `PRBottomNavigationBar.items` + route in `showBottomBar` list
3. **New screen file** — `ui/screens/YourNewScreen.kt`
4. (Optional) **CONTEXT.md** — Update routes table, navigation flow, bottom nav description, and add new screen section

## Workflow

### Step 1: Ask the User

1. **Tab name** — display label (e.g. "Runs", "Analysis", "Progress")
2. **Route name** — kebab-case identifier (e.g. "runs", "analysis", "progress")
3. **Icon** — which Material icon to use (e.g. `Icons.Default.DirectionsRun`, `Icons.Default.Analytics`, `Icons.Default.BarChart`)
4. **New screen or existing?** — does this tab navigate to a new screen or an existing route?
5. **FAB visibility** — should the FAB show on this tab? (default: no)

### Step 2: Determine the Exact Edit Points

Look up the current state of each file before editing:

- **`NavGraph.kt`** — `Routes` object for existing constants + `PrTrackerNavGraph` composable for existing composable entries
- **`MainActivity.kt`** — the `PRBottomNavigationBar` composable (around line 405), the `showBottomBar` list (around line 101), and the `showFab` expression (around line 102)
- **New screen** — read an existing bottom-nav tab screen like `GoalsScreen.kt` or `CalendarScreen.kt` for reference on signature pattern

### Step 3: Edit `navigation/NavGraph.kt`

**3a. Add a route constant in the `Routes` object:**

```kotlin
const val YOUR_ROUTE = "your_route"
```

If the route needs a helper function (for parameterized navigation), add it too:
```kotlin
fun yourRoute(param: String) = "your_route/$param"
```

**3b. Add an import** for the new screen at the top of the file:
```kotlin
import com.example.prtracker.ui.screens.YourNewScreen
```

**3c. Add a composable entry inside `NavHost`:**

```kotlin
composable(Routes.YOUR_ROUTE) {
    YourNewScreen(
        viewModel = viewModel,
        navController = navController
    )
}
```

Match the existing screen composable signature pattern. Most bottom-nav screens receive `viewModel` and `navController`.

### Step 4: Edit `MainActivity.kt`

**4a. Import the icon:**

Add to the existing `Icons.Default.*` imports block:
```kotlin
import androidx.compose.material.icons.filled.YourIcon
```

**4b. Add the nav item to the `items` list in `PRBottomNavigationBar`:**

Find the `items` list (around line 417) and add a new `Triple`:
```kotlin
Triple(Routes.YOUR_ROUTE, Icons.Default.YourIcon, "Label"),
```

Place it in the desired order within the list. The current order is: HOME, DASHBOARD, PRESETS, GOALS, WEIGHT, CALENDAR, SETTINGS.

**4c. Add the route to `showBottomBar`:**

Find the `showBottomBar` expression (around line 101):
```kotlin
val showBottomBar = currentRoute in listOf(Routes.HOME, Routes.DASHBOARD, Routes.PRESETS, Routes.GOALS, Routes.WEIGHT, Routes.CALENDAR, Routes.SETTINGS, "${Routes.PRESETS}?editId={editId}", "${Routes.PRESET_DETAIL}/{presetId}")
```

Add your new route to this list:
```kotlin
Routes.YOUR_ROUTE,
```

**Important:** If the route has a parameterized variant (like PRESETS with `?editId={editId}`), add that variant to `showBottomBar` too.

**4d. (Optional) Update FAB visibility:**

If the user requested FAB visibility on this tab:
```kotlin
val showFab = currentRoute == Routes.DASHBOARD || currentRoute == Routes.YOUR_ROUTE
```

If the tab also needs WeightScreen-style FAB, use:
```kotlin
val showFab = currentRoute in listOf(Routes.DASHBOARD, Routes.WEIGHT, Routes.YOUR_ROUTE)
```

### Step 5: Create the New Screen File

Create `app/src/main/java/com/example/prtracker/ui/screens/YourNewScreen.kt`.

Follow the standard bottom-nav screen template:

```kotlin
package com.example.prtracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.prtracker.ui.components.GridBackground
import com.example.prtracker.ui.theme.TextSecondary
import com.example.prtracker.viewmodel.PRViewModel

@Composable
fun YourNewScreen(
    viewModel: PRViewModel,
    navController: NavHostController
) {
    // Screen content
}
```

**Patterns to follow from existing screens:**
- Use `GridBackground()` as the root animated background
- Use the same imports pattern as other bottom-nav screens (e.g. `GoalsScreen.kt`)
- Pass `viewModel` and `navController` as parameters
- Collect ViewModel state with `val something by viewModel.something.collectAsState()`

### Step 6: Update CONTEXT.md

Read CONTEXT.md and make targeted edits:

**6a. Routes table** (section 6) — add the new route:
```
| `"your_route"` | `YourNewScreen` | None |
```

If the route has a helper function, add it to the helper functions block:
```kotlin
Routes.yourRoute()    // → "your_route"
```

**6b. Navigation Flow** (section 6) — add entry for the tab:
```
Dashboard ──bottom nav──→ YourNewScreen
```

**6c. Bottom nav description** (section 7, Visual Style) — update the nav item count and list:
```
- **Bottom nav:** No labels, just icons. Eight items: Home (...), Dashboard (...), Presets (...), Goals (...), Weight (...), Calendar (...), Settings (...), YourTab (...).
```

**6d. Add new screen section** (section 8) — append a new `### 8.X YourNewScreen` subsection following the existing pattern with descriptive bullet points of what the screen does.

### Step 7: Verify

**Double-check these common pitfalls:**

| Mistake | How to catch it |
|---|---|
| Icon import missing | Verify the icon class is imported in MainActivity.kt |
| Route not in `showBottomBar` | Nav bar won't show on the new tab — verify the route is in the list |
| Route typo in `Routes` object | Verify the const val name matches where it's used |
| Screen file missing package | Verify `package com.example.prtracker.ui.screens` is at the top |
| Import missing in NavGraph.kt | Verify `import com.example.prtracker.ui.screens.YourNewScreen` is present |
| Wrong composable signature | Verify the screen composable receives `(viewModel: PRViewModel, navController: NavHostController)` |
| Trailing comma missing on new field | Verify comma before the new `Triple(...)` item in the items list |
| `selected` logic not updated | If the route has query params, add special handling in the `selected` check inside the `forEach` block, mirroring the PRESETS pattern |
