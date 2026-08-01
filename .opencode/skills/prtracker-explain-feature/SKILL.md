---
name: prtracker-explain-feature
description: Explain any feature, component, screen, or system in PRTracker by reading actual source files and producing a detailed breakdown. Trigger when the user says "explain", "tell me about", "describe", "how does X work", "walk me through", "what is X", "how is X implemented", "break down X", "how does X function". Do NOT trigger for simple yes/no questions, code generation, or bug fixing.
---

# PRTracker Feature Explainer

Explain any part of the app by reading the actual source code and producing a structured, accurate breakdown.

## Workflow

### Step 1: Identify the Target

The user will name something like "DashboardScreen", "the calendar streak logic", "RSI engine", "how syncing works", "the balance engine", etc.

Clarify if ambiguous — if "the streak" could mean the dashboard streak indicator or the calendar streak, ask which one.

### Step 2: Read the Actual Source Files

**Do not explain from memory.** Do not read only CONTEXT.md. Always open the relevant source files.

Use the project structure to locate files:
- Screens → `app/src/main/java/com/example/prtracker/ui/screens/`
- Components → `app/src/main/java/com/example/prtracker/ui/components/`
- Data models → `app/src/main/java/com/example/prtracker/data/`
- ViewModel → `app/src/main/java/com/example/prtracker/viewmodel/PRViewModel.kt`
- Theme → `app/src/main/java/com/example/prtracker/ui/theme/`
- Navigation → `app/src/main/java/com/example/prtracker/navigation/NavGraph.kt`
- Workers → `app/src/main/java/com/example/prtracker/work/`
- Main entry → `app/src/main/java/com/example/prtracker/MainActivity.kt`

For a screen, at minimum read:
- The screen composable file
- The ViewModel functions/state it consumes
- Any custom components it uses (check imports at top of file)

For a data system (RSI, storage, sync), read:
- All data class files in the chain
- The engine/computation files
- How ViewModel calls them

### Step 3: Structure the Explanation

Cover these sections (omit any that don't apply):

**What it does and why it exists**
- The user-facing purpose
- What problem it solves

**Files involved and their roles**
- File paths relative to project root (e.g. `ui/screens/CalendarScreen.kt`)
- What each file contributes (data definition, computation, UI rendering, orchestration)

**How data flows (input → logic → output)**
- Where the inputs come from (user tap, stored JSON, ViewModel state, nav arguments)
- What transformations happen (computation, filtering, sorting, pairing)
- What the output is (rendered UI, saved JSON, navigated route, notification)

**How it connects to the rest of the app**
- ViewModel: which StateFlows and functions are consumed
- StorageManager: what data is read/written
- NavGraph: routes involved
- Other screens/components: what it calls or is called by

**Non-obvious decisions, edge cases, and gotchas**
- Any workarounds or design tradeoffs visible in the code
- Edge cases handled (empty states, nulls, API level differences)
- Performance considerations (derivedStateOf, remember, drawBehind)
- Things that look wrong but are intentional

### Step 4: Format

- Use clear section headers
- Reference actual class names, function names, field names, and line numbers
- Include short code snippets where they clarify (never more than 10-15 lines)
- No vague summaries — every statement should trace back to something in the code

### Step 5: Validate

After writing the explanation, double-check:
- Are all file paths accurate? (re-read the imports if unsure)
- Do the function signatures match what's actually in the code?
- Is there anything in the source that contradicts what you wrote?
