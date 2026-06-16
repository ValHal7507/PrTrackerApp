---
name: prtracker-context-updater
description: Update CONTEXT.md by scanning all project files. Trigger when the user says "save the data", "update context", "sync docs", "save docs", "regenerate context", or asks you to document recent changes to the project. Run this whenever new screens, data models, components, or features are added or when existing ones change. Also trigger when the user says "save the data" in any form (e.g. "save the data now", "please save the data", "can you save the data"). Do NOT trigger for simple edits to CONTEXT.md that the user explicitly describes — only when they ask you to scan and sync the full project.
---

# PRTracker Context Updater

A skill that scans the entire PRTracker project to detect changes and updates CONTEXT.md accordingly.

## When to Use

The user will say something like **"save the data"**, **"update context"**, **"sync docs"**, **"save docs now"**, or **"regenerate context"**. When you hear any of these, run this skill end-to-end. Do not skip steps.

## Workflow

### Step 1: Scan the Project

Use glob, grep, and read tools to build a complete picture of the current project state.

**1a. Project structure**: Run `glob` with `**/*.kt` and `**/*.kts` and `**/*.xml` and `**/*.toml` and `**/*.properties` and `**/*.pro` to get the full file listing. Focus on `app/src/main/java/com/example/prtracker/` for Kotlin source files.

**1b. Dependencies**: Read `gradle/libs.versions.toml` and `app/build.gradle.kts` to verify the tech stack versions in section 2 of CONTEXT.md.

**1c. Data models**: Read every file in `data/` directory (Exercise.kt, PREntry.kt, Goal.kt, WeightEntry.kt, RunEntry.kt, RunningPRs.kt, AppSettings.kt, LeverageTelemetry.kt, AppearanceSettings.kt, WorkoutPreset.kt, StorageManager.kt, TierEvaluator.kt, RsiEngine.kt, RunningPREngine.kt, QRSyncManager.kt, ColorPalette.kt, ThemeDefinitions.kt) and compare against sections 4 and 5 of CONTEXT.md.

**1d. Navigation**: Read `navigation/NavGraph.kt` and verify the routes table in section 6 matches.

**1e. ViewModel**: Read `viewmodel/PRViewModel.kt` and verify the StateFlow/functions table in section 9 matches.

**1f. UI components**: Read all files in `ui/components/` and compare against section 10.

**1g. UI screens**: Read all files in `ui/screens/` and also check for any `.kt` screen files directly in the `ui/` directory (like AddGoalScreen.kt, WeightScreen.kt, LogWeightScreen.kt, SyncReceiveScreen.kt, RankScreen.kt, LogRunScreen.kt, RunHistoryScreen.kt, WorkoutPresetDetailScreen.kt, PresetAnalysisScreen.kt). Compare against section 8.

**1h. Theme**: Read `ui/theme/` files and compare against section 7.

**1i. Workers**: Read all files in `work/` and compare against section 10.

**1j. Manifest**: Read `AndroidManifest.xml` to check permissions and hardware features.

### Step 2: Identify Changes

For each section of CONTEXT.md, list any changes found:

- **New files**: Any `.kt` file that exists but is not listed in the project structure or documented in any section.
- **Removed files**: Any file documented in CONTEXT.md that no longer exists on disk.
- **Changed data models**: Fields added/removed/renamed in data classes, changed default values, new annotations.
- **Changed ViewModel**: New StateFlows, new functions, changed signatures, removed functions.
- **Changed routes**: New routes, removed routes, changed route patterns.
- **Changed dependencies**: New versions, added/removed libraries.
- **Changed components**: New composables, changed signatures, removed composables.
- **Changed screens**: New screens, removed screens, changed screen descriptions.

### Step 3: Verify Before Writing

For every change you plan to make:
1. Cross-reference the source code at least twice (read the actual file contents)
2. If a data model has a field with a default value in the source, make sure you capture it
3. If a composable function signature changed, update the `@Composable` signature documentation
4. For removed features: mark them explicitly but keep the documentation (add a note like `// REMOVED in version X`). Only fully remove content if it's completely irrelevant and the user has confirmed.
5. Do NOT add comments to source code files. Only update CONTEXT.md.
6. Check for differences in the project structure tree (section 3) — new files need new lines, removed files need removal.

### Step 4: Write to CONTEXT.md

Use the `edit` tool to make targeted edits to CONTEXT.md. The file is at the root of the project: `C:\Users\val3nt_n\AndroidStudioProjects\Prtracker\CONTEXT.md`.

**Rules:**
- **Preserve past content.** Do not delete descriptions of features that still exist. Only remove content if a feature/file has been definitively removed from the project.
- **Add new content at the appropriate location.** Insert new sections/entries following the existing numbering and structure.
- **Update version numbers** in section 2 (Tech Stack) if dependencies changed.
- **Update project structure** in section 3 if files were added or removed.
- **Update data models** in section 4 if any field/class changed.
- **Update routes table** in section 6 if routes changed.
- **Update ViewModel table** in section 9 if StateFlows or functions changed.
- **Add new screen sections** following the existing 8.x numbering pattern.
- **Add new component sections** following the existing numbering in section 10.
- **Fix typos or formatting** you notice, but only if you're already editing that section.

**File path convention:** All file paths in CONTEXT.md use forward slashes relative to the project root. For example: `data/Exercise.kt`, `ui/screens/DashboardScreen.kt`.

### Step 5: Confirm

After writing, do a final verification:
1. Re-read the changed sections of CONTEXT.md to make sure they render correctly
2. Verify no existing content was accidentally removed
3. Tell the user: "CONTEXT.md updated. Scanned [N] files, found [X] changes."
