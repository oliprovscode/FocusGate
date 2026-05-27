# FocusGate

A minimalistic Android screentime app that **blocks Instagram** until you complete a real-world task. No willpower required — just do the task.

## Features

- Blocks Instagram until a task is completed
- Tasks include:
  - **10 Pushups** — detected via front camera + ML pose estimation
  - **Morning Shower** — checkbox task (available 08:00–10:00)
  - **Walk 250 steps** — pedometer-based
  - **Read 5 minutes** — timed focus session
  - **Drink water** — simple confirmation with delay
- Minimalistic dark UI with SVG icons, no emojis
- Accessibility overlay blocks Instagram launch
- Daily task history

## Building

```bash
./gradlew assembleDebug
```

APK will be at `app/build/outputs/apk/debug/app-debug.apk`

## Permissions Required

- `CAMERA` — for pushup detection (ML Kit Pose)
- `ACTIVITY_RECOGNITION` — for step counting
- `BIND_ACCESSIBILITY_SERVICE` — for app blocking overlay
- `SYSTEM_ALERT_WINDOW` — for lock screen overlay

## Tech Stack

- Kotlin
- ML Kit Pose Detection (front camera pushup counter)
- AccessibilityService (app blocker)
- Room (task history)
- WorkManager (background monitoring)
- CameraX
