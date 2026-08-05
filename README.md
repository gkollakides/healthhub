# George Health Hub — Android MVP

A native Jetpack Compose Android project based on the approved centralized health and nutrition design.

## Included in this checkpoint

- Today dashboard with calorie and macro progress
- Seven-day meal plan and recipe ingredient requirements
- Kitchen inventory with a generated weekly grocery shortfall list
- Health/body cards, trend charts, period averages and reading history
- Imported cycling, running and strength workout summaries
- Health Connect permission flow for activity, sleep, heart rate, weight, body fat and exercise
- Nutrition write permission ready for meal logging sync
- Persistent app accent-colour selection in Settings
- Source-neutral UI: Samsung Health, RENPHO, Strava and Hevy remain behind the scenes

Detailed strength sets, reps and weights are deliberately deferred to the later phase.

## Run it

1. Open this folder in Android Studio (Ladybug or newer is recommended).
2. Let Android Studio install Android SDK 35 and sync Gradle.
3. Select an Android 8+ emulator or device and press **Run**.
4. On a real device, open Settings in the app and choose **Connect health data**.

Health Connect data access only becomes meaningful on a device where Samsung Health and the other source apps are already writing compatible record types. The MVP currently presents sample records while the adapters are connected; the permission boundary and record types are already defined in `HealthConnectManager.kt`.

## Project layout

- `MainActivity.kt` — Compose screens and interactions
- `Models.kt` — meal, pantry, health and workout models plus seeded MVP content
- `AppViewModel.kt` — persistent appearance settings
- `HealthConnectManager.kt` — Health Connect availability and permissions

## Next implementation checkpoint

Replace seeded health cards with Health Connect reads, persist kitchen/meal state in Room, and write logged nutrition records back to Health Connect.
