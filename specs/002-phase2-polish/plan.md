# Implementation Plan: Houdini Monitor Phase 2 Polish

**Branch**: `002-phase2-polish` | **Date**: 2026-11-19 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/002-phase2-polish/spec.md`

## Summary

Upgrade the MVP desktop monitor into a polished Phase-2 professional app: green accent
theming, process search/filter/sort, per-core CPU and per-volume disk detail, Network and
GPU metrics (graceful N/A), system tray + always-on-top, local preference persistence,
uptime display, keyboard shortcut for process search, and refreshed Desktop jpackage
deliverable. Preserve MVP kill/confirm/permissions behavior and off-FX sampling.

Technical approach: extend immutable snapshots and MetricHistory kinds; pure-logic
helpers + tests first; AWT SystemTray + JavaFX stage; CSS overlay on AtlantaFX; Java
Preferences API for persistence; no new third-party dependencies beyond agreed stack.

## Technical Context

**Language/Version**: Java 21+ (Virtual Threads for sampling)

**Primary Dependencies**: JavaFX 21+, AtlantaFX, OSHI, Gradle + jpackage (unchanged stack)

**Storage**: Local preference store (java.util.prefs) for theme/locale/always-on-top; in-memory metric history only

**Testing**: JUnit 5 for process filter/sort, network rate math, preference parse, GPU availability helpers, disk aggregation regressions

**Target Platform**: Windows 10/11 desktop

**Project Type**: desktop-app (Gradle module `app`)

**Performance Goals**: UI responsive; ~1 Hz sampling; ~60 samples for charted resources including NETWORK (and GPU aggregate when available)

**Constraints**: No auto UAC; no remote telemetry; GPU/network must not crash when unavailable; tray hide-on-close when tray available; clean two-phase sampler shutdown on Exit

**Scale/Scope**: Single window + tray; sidebar CPU/RAM/DISK/NETWORK/GPU/PROCESSES; denser Task Manager layout

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Gate | Status | Notes |
|------|--------|-------|
| Spec-anchored delivery | PASS | Active `002-phase2-polish` |
| Layered architecture / FX-thread safety | PASS | Domain/metrics vs UI; AWT tray callbacks marshal to FX |
| Test-first pure logic | PASS | Filter/sort, rates, prefs, GPU availability planned first |
| Safe process control | PASS | MVP kill contracts unchanged |
| Spec-scoped stack discipline | PASS | Constitution 1.1.0 allows Network/GPU when specified; no new deps |
| EARS / ES+EN | PASS | Spec EARS; i18n keys extended |
| jpackage + README | PASS | Desktop refresh + README update in tasks |

Post-design re-check: PASS — data model and contracts align with gates.

## Project Structure

### Documentation (this feature)

```text
specs/002-phase2-polish/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── ui-navigation.md
│   ├── process-table-ux.md
│   └── desktop-tray.md
└── tasks.md
```

### Source Code (repository root)

```text
app/src/main/java/com/houdini/monitor/
├── domain/          # extend snapshots, ResourceKind, ProcessQuery, NetworkThroughput
├── metrics/         # OshiMetricsCollector network/GPU/per-core; rate helpers
├── service/         # PreferencesService persistence; MonitoringService histories
├── ui/              # sidebar, detail, process table, tray, accent CSS
└── ...
app/src/main/resources/
├── messages*.properties
└── styles/houdini-accent.css
```

## Complexity Tracking

No unjustified complexity. Tray uses standard AWT+FX pattern; GPU fallback is a null-object style availability flag.
