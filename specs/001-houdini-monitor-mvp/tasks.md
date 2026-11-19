# Tasks: Houdini Monitor MVP

**Input**: Design documents from `/specs/001-houdini-monitor-mvp/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: Included for pure domain logic (constitution III â€” test-first).

**Organization**: Phases by user story for incremental delivery.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.: US1, US2, US3, US4)
- Include exact file paths in descriptions

## Path Conventions

- Single Gradle module: `app/src/main/java/com/houdini/monitor/`, `app/src/test/java/com/houdini/monitor/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Gradle JavaFX project skeleton

- [x] T001 Create Gradle project layout `settings.gradle.kts`, root/`app` module dirs per `plan.md`
- [x] T002 Configure `app/build.gradle.kts` with Java 21, JavaFX, AtlantaFX, OSHI, JUnit 5, application plugin
- [x] T003 [P] Add `.gitignore` entries for Gradle/JavaFX build outputs at repository root
- [x] T004 [P] Create package skeleton directories under `app/src/main/java/com/houdini/monitor/` (`domain`, `metrics`, `service`, `i18n`, `ui`)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Shared domain types and sampling infrastructure before UI stories

**âš ï¸ CRITICAL**: No user story UI work until this phase completes

- [x] T005 [P] Implement `ResourceKind` in `app/src/main/java/com/houdini/monitor/domain/ResourceKind.java` citing `data-model.md` ResourceKind
- [x] T006 [P] Implement immutable `ResourceSnapshot` in `app/src/main/java/com/houdini/monitor/domain/ResourceSnapshot.java` citing `data-model.md` ResourceSnapshot.kind, .timestampEpochMs, .usagePercent, .detailLabel
- [x] T007 Write failing unit tests for `MetricHistory` ring buffer in `app/src/test/java/com/houdini/monitor/domain/MetricHistoryTest.java` (capacity ~60, drop oldest)
- [x] T008 Implement `MetricHistory` in `app/src/main/java/com/houdini/monitor/domain/MetricHistory.java` citing `data-model.md` MetricHistory.kind, .capacity, .samples until T007 passes
- [x] T009 [P] Implement `ProcessInfo` record in `app/src/main/java/com/houdini/monitor/domain/ProcessInfo.java` citing `data-model.md` ProcessInfo.name, .pid, .cpuPercent, .memoryBytes, .user, .systemLooking
- [x] T010 Implement immutable `SystemMetricsSnapshot` in `app/src/main/java/com/houdini/monitor/domain/SystemMetricsSnapshot.java` citing `data-model.md` SystemMetricsSnapshot fields
- [x] T011 Implement `OshiMetricsCollector` in `app/src/main/java/com/houdini/monitor/metrics/OshiMetricsCollector.java` (CPU, RAM, aggregate local fixed disk %, process list sorted CPU desc)
- [x] T012 Implement `MetricsSampler` scheduled ~1s off FX thread with two-phase shutdown in `app/src/main/java/com/houdini/monitor/metrics/MetricsSampler.java`
- [x] T013 Implement `MonitoringService` publishing snapshots to listeners in `app/src/main/java/com/houdini/monitor/service/MonitoringService.java`
- [x] T014 Create `HoudiniMonitorApp` JavaFX entry + empty `MainView` shell in `app/src/main/java/com/houdini/monitor/HoudiniMonitorApp.java` and `ui/MainView.java`

**Checkpoint**: Foundation ready â€” domain + sampler compile; tests for history green

---

## Phase 3: User Story 1 - Ver recursos del sistema en vivo (Priority: P1) ðŸŽ¯ MVP

**Goal**: Sidebar + detail with live CPU/RAM/Disk and ~60s charts

**Independent Test**: App shows updating CPU/RAM/Disk; charts fill over time

### Tests for User Story 1

- [x] T015 [P] [US1] Write failing tests for disk aggregate % helper in `app/src/test/java/com/houdini/monitor/metrics/DiskUsageAggregatorTest.java`

### Implementation for User Story 1

- [x] T016 [US1] Implement disk aggregate helper used by collector in `app/src/main/java/com/houdini/monitor/metrics/DiskUsageAggregator.java` until T015 passes
- [x] T017 [US1] Implement `ResourceSidebar` in `app/src/main/java/com/houdini/monitor/ui/ResourceSidebar.java` (CPU, RAM, Disk, later Processes slot)
- [x] T018 [US1] Implement `HistoryChartView` in `app/src/main/java/com/houdini/monitor/ui/HistoryChartView.java` for ~60s series
- [x] T019 [US1] Implement `ResourceDetailPane` in `app/src/main/java/com/houdini/monitor/ui/ResourceDetailPane.java` binding current % + chart per `contracts/ui-navigation.md`
- [x] T020 [US1] Wire `MonitoringService` â†’ UI via `Platform.runLater` in `app/src/main/java/com/houdini/monitor/ui/MainView.java` and start/stop sampler on lifecycle
- [x] T021 [US1] Maintain three `MetricHistory` instances (CPU/RAM/DISK) and feed charts on each snapshot in `app/src/main/java/com/houdini/monitor/service/MonitoringService.java` or UI controller

**Checkpoint**: US1 independently demoable

---

## Phase 4: User Story 2 - Inspeccionar y terminar procesos (Priority: P1)

**Goal**: Process table + confirmed kill with system warning and permission message

**Independent Test**: List processes; confirm/cancel kill; access-denied message without UAC

### Tests for User Story 2

- [x] T022 [P] [US2] Write failing tests for `KillConfirmationPolicy` in `app/src/test/java/com/houdini/monitor/domain/kill/KillConfirmationPolicyTest.java` citing `data-model.md` KillConfirmationPolicy / ProcessInfo.systemLooking

### Implementation for User Story 2

- [x] T023 [US2] Implement `KillConfirmationPolicy` and `KillOutcome` in `app/src/main/java/com/houdini/monitor/domain/kill/` until T022 passes
- [x] T024 [US2] Implement `ProcessTerminator` (no elevation) mapping errors in `app/src/main/java/com/houdini/monitor/metrics/ProcessTerminator.java`
- [x] T025 [US2] Implement `ProcessTableView` columns Name/PID/CPU%/RAM/User default CPU sort in `app/src/main/java/com/houdini/monitor/ui/ProcessTableView.java` per `contracts/ui-navigation.md`
- [x] T026 [US2] Implement `KillConfirmDialog` extra warning path in `app/src/main/java/com/houdini/monitor/ui/KillConfirmDialog.java` per `contracts/process-kill-flow.md`
- [x] T027 [US2] Wire Processes sidebar entry + kill action + outcome alerts in `app/src/main/java/com/houdini/monitor/ui/MainView.java`

**Checkpoint**: US1 + US2 work independently

---

## Phase 5: User Story 3 - Temas e idioma (Priority: P2)

**Goal**: Light/Dark/Follow system + ES/EN i18n (default ES)

**Independent Test**: Switch theme and language; UI updates without restart

### Implementation for User Story 3

- [x] T028 [P] [US3] Add `messages_es.properties` and `messages_en.properties` under `app/src/main/resources/`
- [x] T029 [P] [US3] Implement `Messages` / locale switch helper in `app/src/main/java/com/houdini/monitor/i18n/Messages.java`
- [x] T030 [US3] Implement `PreferencesService` session theme+locale in `app/src/main/java/com/houdini/monitor/service/PreferencesService.java` citing `data-model.md` UserPreferences.theme, .locale
- [x] T031 [US3] Implement `ThemeManager` AtlantaFX Light/Dark/System in `app/src/main/java/com/houdini/monitor/ui/ThemeManager.java`
- [x] T032 [US3] Add theme + language controls and re-localize visible strings in `app/src/main/java/com/houdini/monitor/ui/MainView.java`

**Checkpoint**: Themes and i18n work on live metrics UI

---

## Phase 6: User Story 4 - Entrega portfolio (Priority: P2)

**Goal**: Spanish README + Windows jpackage `.exe`

**Independent Test**: Follow README to run/build; `.exe` launches main UI

- [x] T033 [US4] Configure jpackage Gradle task in `app/build.gradle.kts` (or root build) outputting Windows package
- [x] T034 [US4] Write Spanish portfolio `README.md` at repository root (producto, alcance MVP, ejecuciÃ³n, build exe, defensa)
- [x] T035 [US4] Verify `.\gradlew.bat test` and document run/jpackage commands match `specs/001-houdini-monitor-mvp/quickstart.md`

**Checkpoint**: Done criteria for portfolio deliverable met

---

## Phase 7: Polish & Cross-Cutting Concerns

- [x] T036 [P] Ensure orderly two-phase shutdown on window close in `HoudiniMonitorApp.java` / `MetricsSampler.java`
- [x] T037 [P] Manual pass of `quickstart.md` validation scenarios
- [x] T038 Update `MEMORY.md` with final state, decisions, and next steps

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: start immediately
- **Foundational (Phase 2)**: after Setup â€” BLOCKS stories
- **US1 (Phase 3)**: after Foundational â€” MVP core
- **US2 (Phase 4)**: after Foundational; ideally after US1 sidebar exists
- **US3 (Phase 5)**: after US1 UI shell
- **US4 (Phase 6)**: after app runnable
- **Polish**: after desired stories

### User Story Dependencies

- **US1**: no dependency on other stories
- **US2**: shares MainView/sidebar with US1
- **US3**: overlays preferences on existing UI
- **US4**: packaging/docs after runnable app

### Parallel Opportunities

- T003/T004; T005/T006/T009; T015; T022; T028/T029; T036/T037

---

## Parallel Example: User Story 1

```text
Task: "DiskUsageAggregatorTest.java"
Task: "ResourceSidebar.java" (after foundation)
Task: "HistoryChartView.java"
```

---

## Implementation Strategy

### MVP First (User Story 1)

1. Phase 1â€“2 â†’ Phase 3 (US1) â†’ validate live metrics
2. Add US2 kill flow â†’ US3 themes/i18n â†’ US4 jpackage/README â†’ polish

### Notes

- Tests for MetricHistory and KillConfirmationPolicy MUST fail before implementation
- One Spec Kit task at a time when executing `/speckit-implement`
- Do not invent Network/GPU/alerts/persistence/auto-elevation
