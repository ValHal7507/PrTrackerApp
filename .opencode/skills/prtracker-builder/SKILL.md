---
name: prtracker-builder
description: Build the PRTracker Android app and fix all compilation errors until it compiles successfully. Trigger when the user says "debug", "fix errors", "build the app", "fix build", "make it compile", "build now", or any phrase indicating they want the project to compile without errors. Also trigger on "debug this", "fix the errors", "fix compilation", "build and fix errors". Do NOT trigger for general coding or feature work that does not involve compiling or error-fixing.
---

# PRTracker Builder

Build the Android app and fix every compilation error until `gradlew assembleDebug` succeeds with zero errors. You are only done when the build passes.

## Build Command

```
./gradlew assembleDebug --no-daemon -Pandroid.injected.invoked.from.ide=false 2>&1
```

Use `workdir` set to `C:\Users\val3nt_n\AndroidStudioProjects\Prtracker`. On Windows, use `.\gradlew assembleDebug --no-daemon -Pandroid.injected.invoked.from.ide=false 2>&1`.

## Workflow

### Step 1: Initial Build

Run the build command. If it succeeds immediately, report "Build successful — no errors" and stop.

### Step 2: Parse Errors

If the build fails, read the full error output. Identify every distinct compilation error. For each error you need:

- The file path and line number
- The error message
- The surrounding code context

### Step 3: Fix Errors One at a Time

For each error:

1. **Read the file** around the reported line to understand the code
2. **Read related files** — imports, dependencies, parent classes, related composables
3. **Apply the fix** using the `edit` tool
4. **Rebuild** to verify the fix resolved the error and didn't introduce new ones

**Common error patterns in this project:**

| Error | Likely Fix |
|---|---|
| Unresolved reference `X` | Missing import, renamed symbol, or wrong package |
| Type mismatch | Wrong parameter type, missing `.toX()` conversion |
| Missing argument | Composable or function signature changed — add the parameter |
| Cannot infer type | Add explicit type annotation |
| `@Composable` invocations can only happen from context | Wrap in composable function or provide `@Composable` scope |
| `Modifier.X` not found | Wrong modifier chain, use `Modifier.then()` or import extension |
| `X is not an annotation class` | Wrong import path |
| `Unresolved reference: R` | Resource not found — check res/ directory or use string literal |
| `Unresolved reference: drawBehind` | Missing `import androidx.compose.ui.draw.drawBehind` |
| Package name doesn't match file path | Fix package declaration or move file |
| `None of the following functions can be called with the arguments supplied` | Signatures changed — check parameter list |
| `Val cannot be reassigned` | Using `val` instead of `var`, or using wrong collection type |
| `Unresolved reference: LocalAppearance` | Missing `@Composable` annotation on function |
| `'public open fun onPlaybackParamsChange(params: PlaybackParams!)'` | Audio/Media API issue — rarely relevant here |

### Step 4: Iterate Until Clean

Keep fixing and rebuilding until `assembleDebug` exits with code 0. Do not skip errors. Do not assume a fix worked — always verify with a rebuild.

### Step 5: Report

When the build passes (exit code 0), report: "Build successful after [N] attempts. [X] errors fixed."

List the errors that were fixed with file paths and a brief description.

## Important Rules

1. **Do not add comments** to source files — only fix actual errors
2. **Do not change code behavior** unless required for compilation
3. **Prefer minimal fixes** — import the missing symbol, add the missing parameter, fix the type
4. **Do not remove features** — if something doesn't compile, fix it, don't delete it
5. **If a file is legitimately broken** (e.g. references an API that doesn't exist), find the equivalent API in the project's dependency versions
6. **Check imports** — many errors are just missing imports in this project
7. **If you upgrade or downgrade a dependency** to fix an error, update `gradle/libs.versions.toml` accordingly
8. **Do not modify `gradlew`, `gradlew.bat`, `gradle-wrapper.jar`, or `gradle-wrapper.properties`** — these are build infrastructure files
