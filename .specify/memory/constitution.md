<!--
Sync Impact Report
- Version change: 1.0.0 → 1.1.0
- Modified principles: V. MVP Scope Discipline → Spec-Scoped Delivery Discipline
  (Network/GPU allowed when active Phase-2+ spec includes them; graceful N/A)
- Modified sections: Technology & Product Constraints (accent color, Network/GPU,
  tray/always-on-top/prefs, process search/filter/sort, denser layout)
- Added sections: none
- Removed sections: none
- Follow-up TODOs: none
-->

# Houdini Monitor Constitution

## Core Principles

### I. Spec-Anchored Delivery
Every feature MUST originate from an active Spec Kit specification. Implementation
MUST NOT invent requirements outside the approved brief or active `spec.md`.
When a desired change is not covered, the agent MUST ask before expanding scope.
Rationale: portfolio work stays auditable, defendable, and aligned with Spec-Driven
Development rather than vibe coding.

### II. Layered Desktop Architecture
Domain and metrics logic MUST be independent of JavaFX UI controls. Sampling and
process operations MUST run off the JavaFX Application Thread; UI updates MUST
use `Platform.runLater` (or equivalent safe marshaling). Metric snapshots exposed
to the UI MUST be immutable and thread-safe. Executors MUST shut down in a clean
two-phase manner on application exit.
Rationale: concurrency concepts from PSP multi-threading apply directly to a
responsive, correct desktop monitor.

### III. Test-First for Pure Logic (NON-NEGOTIABLE)
Pure logic (metric aggregation, history buffers, kill-confirmation rules,
threshold/formatting helpers) MUST have failing unit tests written before
production code. UI wiring MAY rely on manual verification described in the
spec. Tests MUST stay green before a task is marked complete.
Rationale: DAM portfolio quality and regression safety without over-testing
JavaFX chrome.

### IV. Safe Process Control
Killing a process MUST always require explicit user confirmation. Processes that
look system-critical MUST show an extra warning. On permission failure the UI
MUST show a clear message. The application MUST NOT attempt automatic UAC
elevation.
Rationale: user safety and predictable privilege behavior on Windows.

### V. Spec-Scoped Delivery Discipline
Ship only what the active feature specification requires. Prefer the agreed stack
(Java 21+, JavaFX, AtlantaFX, OSHI, Gradle) over new dependencies. Items such as
alerts, disk-persisted metric history, remote telemetry, and auto UAC elevation
MUST remain unimplemented until a new spec explicitly includes them. Network and
GPU metrics MAY be implemented when an active Phase-2+ spec includes them, with
graceful unavailable/fallback UX when hardware APIs cannot report data.
Rationale: YAGNI keeps the portfolio focused while allowing intentional Phase growth.

## Technology & Product Constraints

- Language/runtime: Java 21+ with Virtual Threads where they improve sampling I/O.
- UI: JavaFX + AtlantaFX; themes Light / Dark / Follow system; Phase-2+ MAY define
  a secondary/accent color system (green) over AtlantaFX for charts, selection,
  and sidebar active state without abandoning AtlantaFX as the base.
- Metrics: OSHI for CPU (incl. per-core when specified), RAM, Disk (per-volume),
  process enumeration, and—when specified—Network and GPU with graceful N/A fallback.
- Desktop integration (when specified): system tray show/hide; always-on-top toggle;
  local preference persistence (theme, language, always-on-top). No auto UAC elevation.
- Build: Gradle preferred; deliverable includes Windows `.exe` via `jpackage`.
- Localization: Spanish and English; code identifiers in English;
  product-facing UI/docs default Spanish for defense.
- Requirements style: EARS (CUANDO / SI…ENTONCES / MIENTRAS / EL SISTEMA…).
- Sampling cadence: approximately 1 second; in-memory history charts ~60 seconds
  for charted resources (CPU, RAM, Disk, and when specified Network/GPU aggregates).
- Layout: modern Task Manager style (resource sidebar + detail pane); denser polish OK.
- Process columns: Name, PID, CPU %, RAM, User; Phase-2+ MAY add search/filter/sort UX.
- Privacy: no remote telemetry without explicit user consent; no secrets in repo.

## Development Workflow

1. Follow Spec Kit order: constitution → specify → clarify → plan → tasks →
   (analyze) → implement → converge until Converged.
2. One Spec Kit task at a time; keep local session notes short if you use them
   (max ~50 lines; not part of the public tree).
3. Ask before adding dependencies beyond the agreed stack, before data-model
   changes that affect specs, and before elevated kill behavior.
4. Verification: unit tests green for logic tasks; compile/run app for UI tasks;
   converge checks remaining gaps against the active spec.

## Governance

This constitution supersedes informal practices and ad-hoc scope expansion.
Amendments MUST update this file with semantic versioning:
- MAJOR: remove/redefine principles incompatibly.
- MINOR: add or materially expand a principle/section.
- PATCH: clarifications and non-semantic wording.

Compliance is reviewed at plan, implement, and converge steps. Local working
notes (if any) MUST remain consistent with this constitution and are not
published with the product tree.

**Version**: 1.1.0 | **Ratified**: 2026-11-19 | **Last Amended**: 2026-11-19
