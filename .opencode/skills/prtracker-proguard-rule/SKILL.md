---
name: prtracker-proguard-rule
description: Add a ProGuard/R8 keep rule for a new library or to protect data classes from being stripped in release builds. Trigger when the user says "add proguard", "add keep rule", "proguard rule", "keep rule", "r8 rule", "protect from shrinking", or when adding a new library dependency that uses reflection, serialization, or callbacks that R8 cannot see statically. Also trigger when the user reports a release-build crash with ClassNotFoundException, NoSuchFieldException, or "method not found" that works in debug but not release.
---

# PRTracker ProGuard/R8 Rule Adder

Add keep rules to `app/proguard-rules.pro` so that libraries and data classes survive R8 shrinking, obfuscation, and optimization in release builds.

## The Current Setup

The project already has these rules in `proguard-rules.pro`:

```
-keep class com.example.prtracker.** { *; }   // Keeps ALL app classes (data classes + ViewModel + composables)
-keepclassmembers class com.example.prtracker.** { *; }  // Keeps all members too
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
```

**Important:** The blanket `-keep class com.example.prtracker.** { *; }` already covers every data class in `com.example.prtracker` package (Exercise, PREntry, Goal, WeightEntry, RunEntry, RunningPRs, AppSettings, AppearanceSettings, PresetExercise, WorkoutPreset, LeverageTelemetry, StorageData). **New data classes in this package do NOT need additional rules** unless they're in a different package.

## Workflow

### Step 1: Ask the User

1. **What's the target?** — A new third-party library, or a specific class that crashes?
2. **What package?** — If it's inside `com.example.prtracker.**`, the existing blanket rule covers it — explain this and skip the rest
3. **What reflection/serialization does it use?** — Gson, kotlinx.serialization, dynamic feature loading, service loaders, callbacks?

### Step 2: Determine the Right Rule

Use this table to decide:

| Scenario | Rule |
|---|---|
| **Entire library package** (safe but coarser) | `-keep class com.some.library.** { *; }` |
| **Specific class with all members** | `-keep class com.some.library.SomeClass { *; }` |
| **Specific class, keep class name only** | `-keepnames class com.some.library.SomeClass` |
| **Gson-serialized class** (outside app package) | `-keep class com.other.pkg.DataClass { *; }` |
| **Class that extends a serializable type** | `-keep class * extends com.some.Type` |
| **Enum (Gson needs enum names)** | `-keep enum com.some.library.** { *; }` |
| **Interface with callbacks** (R8 removes unused interfaces) | `-keep interface com.some.library.Callback { *; }` |
| **Class with native methods** | `-keep class com.some.library.NativeClass { native <methods>; }` |
| **Suppress a warning** (when a dependency triggers a harmless warning) | `-dontwarn com.some.library.obsolete.**` |
| **ServiceLoader / META-INF services** | `-keep class com.some.library.ServiceImpl { *; }` + `-keep class * implements com.some.library.ServiceInterface` |

### Step 3: Add the Rule

Read the current `proguard-rules.pro` first. Append the new rule at the end with a comment explaining what it's for:

```
# ---- Library Name: what it does ----
-keep class com.some.library.SomeClass { *; }
```

Follow the existing comment style: `# ---- Section header ----` on its own line, then the rule.

### Step 4: Warn About Obfuscation

If the user needs to **debug** release builds (e.g. they want readable stack traces from the new library):

- Add `-keepattributes SourceFile,LineNumberTable` (already present)
- For better stack traces from the library itself: `-keepnames class com.some.library.**`

### Step 5: Test

Tell the user to run a release build to verify:

```
.\gradlew assembleRelease --no-daemon 2>&1
```

If the build fails with new warnings, those warnings indicate additional rules needed — typically `-dontwarn` entries for optional dependencies the library references but doesn't require.

### Step 6: Common Library Patterns

| Library | Likely needed |
|---|---|
| **New Gson type adapter** | Already covered by blanket `-keep class com.example.prtracker.**` |
| **MPAndroidChart** | Already bundled — no extra rules needed |
| **Lottie** | Already bundled — no extra rules needed |
| **CameraX** | Already bundled — no extra rules needed |
| **WorkManager** | Coroutine rules already present |
| **ZXing** | Already bundled — no extra rules needed |
| **OkHttp / Retrofit** (if added) | `-keep class retrofit2.** { *; }` + `-keepclassmembers,allowobfuscation interface * { @retrofit2.http.* <methods>; }` |
| **Glide / Coil** (if added) | `-keep class com.bumptech.glide.** { *; }` |
| **Room** (if added) | Full Room ProGuard rules from documentation |
| **Firebase** (if added) | `-keep class com.google.firebase.** { *; }` |
