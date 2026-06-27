# PR Tracker

A personal record tracking app for exercises. Log reps, hold times, runs, and body weight — track your progress over time.

## Features

- **Exercise Tracking** — Create exercises (reps or hold-based), log entries, and track personal records
- **Workout Presets** — Build reusable workout templates with target reps/hold times, sets, and an "Until Failure" option
- **Live Workout Execution** — Start a preset as a live session with pause/resume, per-set input, and timer
- **Body Weight Log** — Track weight over time with charts, min/max stats, and a target weight indicator
- **Running Tracker** — Log runs manually or use live GPS tracking with real-time distance, pace, and elapsed time
- **Goals** — Set daily, weekly, or monthly targets per exercise and track completion
- **Calendar View** — Monthly overview with workout days, rest days, streak tracking, and movement pattern analysis
- **10-Tier Rank System** — Calisthenics progression evaluator across 10 tiers from RAW_CARBON to SYSTEM_OVERRIDE
- **Exercise History** — Scrollable list of every logged entry across all exercises with PR color coding
- **Workout History** — Review past completed workout sessions with per-exercise breakdowns
- **Pet Dice Roll** — Roll for random pets across 10 rarity tiers (COMMON → SECRET), collect, fuse, sell, and equip pets for XP bonuses
- **Pet Upgrades** — Upgrade luck, coin multiplier, roll speed, lucky rolls, equip slots, and multi-roll with earned coins
- **Appearance customization** — 6 preset themes or create your own with per-system accent colors
- **Notifications** — Morning training reminders, goal progress alerts, evening daily reviews, and weekly summaries
- **Data Export/Import** — Share your data as a JSON file between devices via Android's share sheet
- **Offline** — Fully offline, no account or internet required

## Tech Stack

| Category   | Choice                        |
| ---------- | ----------------------------- |
| Language   | Kotlin                        |
| UI         | Jetpack Compose (Material3)   |
| Navigation | Compose Navigation            |
| Charts     | MPAndroidChart                |
| Animations | Lottie Compose                |
| JSON       | Gson                          |
| Background | WorkManager                   |
| GPS        | Google Play Services Location |
| Min SDK    | Android 8.0 (API 26)          |

## Data Storage

All data is stored as JSON files on the device — no database, no server. App data is in `prs.json` and pet data is in `pets.json`. The app supports file-based export and import for backing up or transferring data between devices.

## Building

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle
4. Run on a device or emulator (API 26+)

```bash
./gradlew assembleDebug
```

## License

# This project is for personal use.
