# Cardioo (Blood Pressure + Pulse + Weight Tracker)

Manual-entry health tracking app built with **Kotlin**, **Jetpack Compose (Material 3)**, **Room**, **Hilt**, and an **MVVM + Clean Architecture**-style layering.

## What it does
- **Readings** tab
  - Combined card per entry: timestamp, BP + category badge, pulse, weight, notes
  - Dashboard summary: latest + average BP
  - Pull-to-refresh (local DB)
  - **CSV export** using Android’s system file picker
- **Chart** tab
  - Simple trend chart with toggles (BP / Pulse / Weight)
  - Weekly / Monthly range filter
- **Measurement Entry**
  - Single form: systolic/diastolic, pulse, weight with kg/lb toggle, date+time picker, notes
  - Input validation (realistic ranges)
  - BP category (Normal/Elevated/HTN Stage 1/2)
  - BMI calculated from user height (set in profile)
- **Onboarding / Profile**
  - First launch asks for height + units (needed for BMI)
  - Settings screen to edit height, units, optional DOB / gender

## Project structure (high-level)
- `app/src/main/java/com/cardioo/`
  - `data/`
    - `db/` Room database (`AppDatabase`), entities, DAOs, type converters
    - `mapper/` entity ↔ domain mapping
    - `repository/` repository implementations (Room-backed)
  - `domain/`
    - `model/` pure Kotlin models + BP category + unit conversions
    - `repository/` repository interfaces
    - `usecase/` small use-cases used by ViewModels
  - `di/` Hilt modules (Room DB + repository bindings)
  - `presentation/`
    - `app/` Compose navigation (`CardiooRoot`, `Routes`)
    - `main/` bottom navigation scaffold (exactly 2 tabs)
    - `readings/` list + dashboard + CSV export
    - `entry/` add/edit combined measurement form
    - `chart/` simple Compose canvas chart
    - `onboarding/` first-launch profile setup
    - `settings/` profile/unit preferences
    - `theme/` Material 3 theme (white + pink `#FF6B8B`)

## How to run
1. Open the folder `d:\Projects\cardioo` in **Android Studio** (Giraffe+ recommended).
2. Let Gradle sync finish.
3. Run the **app** configuration on an emulator/device (minSdk **24**).

If you see a Gradle/JDK error:
- Use **JDK 17** (Android Studio → Settings → Build Tools → Gradle → Gradle JDK).

## Notes
- No runtime permissions are required (manual entry only).
- CSV export uses the system document picker, so the user chooses the destination.

