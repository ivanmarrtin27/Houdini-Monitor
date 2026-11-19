# Quickstart: Houdini Monitor MVP

## Prerequisites

- Windows 10/11
- JDK 21+ (with `jpackage` for `.exe` packaging)
- Git (optional)

## Setup

From repository root:

```powershell
.\gradlew.bat test
.\gradlew.bat run
```

## Validation scenarios

1. **Live metrics (P1)**  
   Run the app → sidebar shows CPU/RAM/Disk updating ~every second → select each resource → chart fills over time (~60s window).

2. **Process list & kill (P1)**  
   Open Processes → columns Name, PID, CPU %, RAM, User → sort default by CPU → End process → confirm dialog → cancel leaves process; confirm on a normal user process terminates it; permission failure shows clear message without UAC.

3. **Theme & i18n (P2)**  
   Switch Light/Dark/Follow system → UI updates → switch Español/English → labels update.

4. **Portfolio deliverable (P2)**  
   Follow root `README.md` (Spanish) → build Windows package:

```powershell
.\gradlew.bat jpackage
```

   Expect an `.exe` / installer under the documented `build` output path; app launches to main UI.

## Tests

```powershell
.\gradlew.bat test
```

Expected: all unit tests green (metric history, kill policy, disk aggregation helpers).
