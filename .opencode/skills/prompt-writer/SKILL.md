---
name: prompt-writer
description: Generates a complete, copy-pastable coding prompt for the PRTracker Android project and writes it to LAST_PROMPT.md. Use this skill any time the user asks to "write a prompt", "make a prompt", "generate a prompt", "create a prompt for", or describes a feature/fix they want implemented. Always triggers when the output is a prompt for an OpenCode agent, not a direct code change.
---

# Prompt Writer

This skill generates structured, self-contained coding prompts for the PRTracker Android project and saves them to `LAST_PROMPT.md`. Every prompt produced by this skill must be immediately executable by a fresh OpenCode agent with zero clarifying questions.

## Output Rule

After generating the prompt content, always write it to `LAST_PROMPT.md` in the project root using a file write tool. Confirm to the user that the file has been written and is ready to paste.

## Mandatory Opening Line

Every prompt must begin with this exact line, verbatim, as the very first line of content:
Before making any changes, read CONTEXT.md in full, then read every file in the project source tree to fully understand the architecture, data flow, and conventions.


No exceptions. This line is non-negotiable and goes above everything else, including the FILES TO MODIFY section.

## Prompt Structure

Build every prompt using this skeleton, in this order:

1. Mandatory opening line (above)
2. FILES TO MODIFY — list every file path that will be touched, one per line, before any steps begin
3. GOAL — one short paragraph describing what the feature or fix achieves
4. STEP 1, STEP 2, STEP 3 … — numbered steps, each targeting exactly one file or one logical unit of work. Each step names the exact file path it modifies. Steps never bleed into each other.
5. CONVENTIONS section (see below — always last)

## Step Writing Rules

- Each step names its target file path explicitly at the top of the step.
- No step touches more than one file unless the two changes are inseparably atomic (e.g., adding a route constant and registering it in NavGraph in the same step is acceptable; anything larger is not).
- Steps are surgical. Never rewrite an entire file to fix a small bug. Identify the root cause first, then touch only the broken lines.
- No placeholders. No "[insert your value here]". No incomplete sentences. The agent must be able to read the step and execute without guessing anything.

## Bug Fix Prompts

When the user describes a bug, the prompt must:
1. State the likely root cause at the top of the GOAL section.
2. Give targeted steps that only touch what is broken.
3. Never rewrite whole files to address a small problem.

## CONVENTIONS Section

Every prompt ends with this section, verbatim, as the final block:
CONVENTIONS:

No XML layouts — all UI is Jetpack Compose
No internet usage
No database — JSON only via StorageManager
All new data class fields must have defaults for Gson compatibility
Cache all Brush objects in remember { }
GridBackground() on every screen
Follow existing updateSettings() pattern for any settings changes
No new dependencies unless absolutely necessary
After implementing, build the project and fix any compilation errors before considering the task done

## Project Color Palette Reference

When writing prompts that involve UI, colors, or visual design, use only these values. Never invent new hex codes:

- Background: #050A18
- Card background: #0D1526
- Primary accent (cyan): #00F5FF
- Secondary accent (purple): #7B2FFF
- Success (green): #00FF85
- Danger (red): #FF003C
- Running / live feature (magenta): #FF2D78
- Text primary: #E8F4FD
- Text secondary: #6B8CAE
- Font: always Monospace

## Design Decision Rule

When the user asks for a color, style, or design choice without specifying a preference, make the decision confidently. Do not ask clarifying questions. Pick the best option for the cyberpunk aesthetic, state the choice and the reasoning in two sentences maximum inside the prompt's GOAL section, then proceed to write the steps.

## Single Block Rule

The entire prompt is one contiguous block of text written to LAST_PROMPT.md. Never split a feature across multiple files or multiple pastes. One feature = one LAST_PROMPT.md write = one agent paste.

## Project Quick Reference

The target project is PRTracker — an offline calisthenics personal record tracker for Android.

Key facts the prompt writer must never contradict:
- Language: Kotlin, Jetpack Compose, Material3
- Single ViewModel: PRViewModel (all state lives here)
- Storage: StorageManager reads/writes prs.json via Gson — no Room, no SQLite
- No internet in any feature
- Navigation: Compose Navigation, routes defined in NavGraph.kt
- Running features use fixed magenta (#FF2D78) regardless of theme
- Every screen must include GridBackground()
- Foreground GPS service: RunTrackingService.kt (FusedLocationProviderClient)
- Existing dependencies are in gradle/libs.versions.toml — do not add new ones unless the feature is impossible without them
