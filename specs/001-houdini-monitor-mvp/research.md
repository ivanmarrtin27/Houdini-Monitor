# Research: Houdini Monitor MVP

## Decision: Java 21 + Gradle + JavaFX + AtlantaFX + OSHI

- **Rationale**: Agreed stack in brief and constitution; Java 21 Virtual Threads fit
  periodic sampling; AtlantaFX provides polished Light/Dark theming; OSHI is the
  standard cross-platform (Windows-capable) metrics/process library for Java.
- **Alternatives considered**: Maven (Gradle preferred by brief); raw Win32 JNI
  (more fragile, harder to test); Swing (weaker modern theming vs JavaFX/AtlantaFX).

## Decision: Sampling off JavaFX thread with immutable snapshots

- **Rationale**: Professor PSP multi-thread notes — schedule ~1s sampling on a
  dedicated scheduler/virtual-thread executor; publish immutable
  `SystemMetricsSnapshot`; marshal to UI with `Platform.runLater`. Two-phase
  shutdown: cancel sampling, await termination, then exit.
- **Alternatives considered**: Sampling on FX AnimationTimer (blocks/janks UI under
  OSHI cost); mutable shared state without snapshots (race-prone).

## Decision: In-memory ring buffer (~60 samples) for charts

- **Rationale**: Spec requires ~60s history at ~1 Hz → ~60 points; no disk
  persistence (out of scope). Simple circular buffer / deque in domain layer,
  unit-testable.
- **Alternatives considered**: Chart library with built-in history (heavier dep);
  file-backed time series (explicitly out of scope).

## Decision: Kill confirmation policy as pure domain rules

- **Rationale**: Extra warning for SYSTEM/NT AUTHORITY or well-known system names
  is testable without OS. Actual terminate via OSHI/`ProcessHandle` with clear
  failure mapping; never launch elevation helper.
- **Alternatives considered**: Always treat all processes equally (fails FR-006);
  auto-elevate via UAC (forbidden by brief).

## Decision: Disk metric = aggregate used % of local fixed disks

- **Rationale**: Encoded clarification; OSHI file store enumeration filtered to
  local fixed volumes; detail pane may list per-volume %.
- **Alternatives considered**: Only C: drive (too narrow); I/O throughput (out of
  MVP “space usage” framing for Task Manager-like disk).

## Decision: i18n via ResourceBundle (es default, en)

- **Rationale**: Spec FR-009/FR-009a; standard Java approach; UI strings only in
  properties files.
- **Alternatives considered**: Hardcoded Spanish only (fails EN requirement).

## Decision: Themes via AtlantaFX + Follow system

- **Rationale**: AtlantaFX PrimerLight/PrimerDark (or similar) switched at runtime;
  Follow system uses Windows preference / `os.name` theme detection API available
  to JavaFX/AtlantaFX.
- **Alternatives considered**: Custom CSS only (more work, less polished).

## Decision: jpackage for Windows `.exe`

- **Rationale**: Done criterion; Gradle `org.beryx.jlink`/`jpackage` task or
  `application` + exec jpackage; document JDK requirements in README.
- **Alternatives considered**: Launch4j-only wrappers (less integrated runtime);
  fat jar alone (not the requested `.exe` deliverable).
