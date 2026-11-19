# Implementation Plan: Houdini Monitor MVP

**Branch**: `001-houdini-monitor-mvp` | **Date**: 2026-11-19 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-houdini-monitor-mvp/spec.md`

## Summary

Build **Houdini Monitor**, a Windows desktop performance monitor (Task Manager style)
with live CPU/RAM/Disk metrics (~1s sampling), ~60s in-memory history charts, process
list with confirmed kill (extra warning for system-looking processes, no auto UAC),
Light/Dark/Follow-system themes, Spanish+English i18n, Gradle + jpackage `.exe`, and
a Spanish README for DAM portfolio defense.

Technical approach: Java 21+ modular Gradle app, OSHI for metrics/processes, JavaFX +
AtlantaFX for UI, scheduled sampling off the FX thread with immutable snapshots and
`Platform.runLater`, JUnit 5 tests for pure domain logic first.

## Technical Context

**Language/Version**: Java 21+ (Virtual Threads for sampling where beneficial)

**Primary Dependencies**: JavaFX 21+, AtlantaFX, OSHI, Gradle (Java application plugin + jpackage)

**Storage**: N/A (in-memory metric history only; session preferences)

**Testing**: JUnit 5 (unit tests for metrics history, kill confirmation rules, formatting)

**Target Platform**: Windows 10/11 desktop

**Project Type**: desktop-app (single Gradle project)

**Performance Goals**: UI remains responsive; sampling ~1 Hz; charts hold ~60 samples/resource

**Constraints**: No auto UAC elevation; no remote telemetry; MVP excludes Network/GPU/alerts/disk history persistence; sampling MUST NOT block JavaFX thread; clean two-phase executor shutdown

**Scale/Scope**: Single-window app; sidebar (CPU/RAM/Disk/Processes) + detail pane; ~5 process columns

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Gate | Status | Notes |
|------|--------|-------|
| Spec-anchored delivery | PASS | Active spec `001-houdini-monitor-mvp` |
| Layered architecture / FX-thread safety | PASS | Domain vs UI packages; sampler off FX thread |
| Test-first pure logic | PASS | History buffer, kill rules, disk aggregation tested first |
| Safe process control | PASS | Confirm + system warning; no auto elevation |
| MVP scope / agreed stack | PASS | Java/JavaFX/AtlantaFX/OSHI/Gradle only |
| EARS requirements / ES UI + EN i18n | PASS | Spec uses EARS; ResourceBundle ES/EN |
| jpackage + Spanish README | PASS | Build tasks + README.md planned |

Post-design re-check: PASS — structure and contracts align with gates.

## Project Structure

### Documentation (this feature)

```text
specs/001-houdini-monitor-mvp/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── ui-navigation.md
│   └── process-kill-flow.md
└── tasks.md
```

### Source Code (repository root)

```text
app/
├── build.gradle.kts
├── src/main/java/com/houdini/monitor/
│   ├── HoudiniMonitorApp.java
│   ├── domain/
│   │   ├── ResourceKind.java
│   │   ├── ResourceSnapshot.java
│   │   ├── MetricHistory.java
│   │   ├── ProcessInfo.java
│   │   ├── SystemMetricsSnapshot.java
│   │   └── kill/
│   │       ├── KillConfirmationPolicy.java
│   │       └── KillOutcome.java
│   ├── metrics/
│   │   ├── MetricsSampler.java
│   │   ├── OshiMetricsCollector.java
│   │   └── ProcessTerminator.java
│   ├── service/
│   │   ├── MonitoringService.java
│   │   └── PreferencesService.java
│   ├── i18n/
│   │   └── Messages.java
│   └── ui/
│       ├── MainView.java
│       ├── ResourceSidebar.java
│       ├── ResourceDetailPane.java
│       ├── ProcessTableView.java
│       ├── HistoryChartView.java
│       ├── ThemeManager.java
│       └── KillConfirmDialog.java
├── src/main/resources/
│   ├── messages_es.properties
│   ├── messages_en.properties
│   └── styles/
└── src/test/java/com/houdini/monitor/
    ├── domain/
    └── kill/
```

**Structure Decision**: Single Gradle JavaFX application module under `app/` (or repo root
`build.gradle.kts` with standard `src/main/java`). Domain and metrics are UI-free;
UI package only binds snapshots via FX properties / runLater.

## Complexity Tracking

> No constitution violations requiring justification.
