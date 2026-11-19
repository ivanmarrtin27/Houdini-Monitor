# Tasks: Houdini Monitor Phase 2 Polish

**Input**: Design documents from `/specs/002-phase2-polish/`
**Prerequisites**: plan.md, spec.md, data-model.md, contracts/, research.md, quickstart.md

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel
- **[Story]**: US1…US5

## Phase 1: Setup

- [x] T001 Update AGENTS.md active feature pointer to `specs/002-phase2-polish/`
- [x] T002 [P] Add i18n keys ES/EN for network, GPU, tray, search, uptime, empty states in `app/src/main/resources/messages*.properties`
- [x] T003 [P] Add `app/src/main/resources/styles/houdini-accent.css` green accent variables/overlays

## Phase 2: Foundational (blocking)

- [x] T004 Extend `ResourceKind` with NETWORK, GPU in `app/src/main/java/com/houdini/monitor/domain/ResourceKind.java`
- [x] T005 [P] Add domain types `CpuCoreSnapshot`, `NetworkInterfaceSnapshot`, `GpuSnapshot` under `domain/`
- [x] T006 Extend `SystemMetricsSnapshot` with cores, network rates/interfaces, gpu, uptime fields (data-model.md)
- [x] T007 [P] Add `ProcessQuery` pure filter/sort + failing tests first `domain/ProcessQuery.java` / `ProcessQueryTest.java`
- [x] T008 [P] Add `NetworkRateCalculator` pure helper + tests `metrics/NetworkRateCalculator.java`
- [x] T009 Persist preferences (theme/locale/alwaysOnTop) via java.util.prefs in `PreferencesService` + tests

## Phase 3: User Story 1 — Richer metrics (P1)

**Goal**: Network, GPU, per-core CPU, per-disk in snapshots and detail UI
**Independent Test**: Sidebar + detail show new resources; GPU N/A safe

- [x] T010 [US1] Extend `OshiMetricsCollector` for per-core, network deltas, GPU best-effort, boot/uptime
- [x] T011 [US1] Extend `MonitoringService` histories for NETWORK (and GPU % when available)
- [x] T012 [US1] Update `ResourceSidebar` / `ResourceDetailPane` / `HistoryChartView` for new kinds and detail lists
- [x] T013 [US1] Show system uptime in chrome (`MainView` / toolbar)

## Phase 4: User Story 2 — Process UX (P1)

**Goal**: Search, filter, sort; keep kill behavior
**Independent Test**: Search+sort work; kill confirm unchanged

- [x] T014 [US2] Wire search field + empty state + column sort in `ProcessTableView` using `ProcessQuery`
- [x] T015 [US2] Ctrl+F focus search when processes view active in `MainView`

## Phase 5: User Story 3 — Visual polish (P2)

**Goal**: Green accent + denser professional layout

- [x] T016 [US3] Apply accent stylesheet in `ThemeManager`; style sidebar active/selection/charts green
- [x] T017 [US3] Tighten spacing/layout density in MainView/sidebar/detail/process table

## Phase 6: User Story 4 — Tray / always-on-top / prefs (P2)

**Goal**: Tray show/hide, always-on-top, persisted prefs

- [x] T018 [US4] Implement `TrayService` (AWT) + integrate close-to-tray / Exit in `HoudiniMonitorApp`
- [x] T019 [US4] Always-on-top toggle wired to Stage + PreferencesService
- [x] T020 [US4] Load persisted prefs on startup (theme/locale/alwaysOnTop)

## Phase 7: User Story 5 — Package & Desktop (P2)

- [x] T021 [US5] Run `.\gradlew.bat :app:test` until green
- [x] T022 [US5] Run `.\gradlew.bat :app:jpackage`; copy to Desktop `HoudiniMonitor`; refresh `.lnk`
- [x] T023 [US5] Verify `:app:run`; update README Phase-2 notes (ES)

## Phase 8: Polish / Converge

- [x] T024 Manual smoke vs quickstart.md; fix gaps
- [x] T025 `/speckit-converge` until Converged; update MEMORY.md

## Dependencies

- Setup (T001–T003) → Foundational (T004–T009) → US1 (T010–T013) → US2 (T014–T015)
- US3 (T016–T017) after US1 sidebar/detail exist
- US4 (T018–T020) after prefs T009 + MainView chrome
- US5 after features implementable
- Converge last

## Parallel examples

- T002 ∥ T003 after T001
- T005 ∥ T007 ∥ T008 after T004
- T014 can start once T007 + process table available

## Implementation strategy

MVP-first incremental: foundational domain/tests → collector → UI metrics → process UX → polish → tray → package → converge.
