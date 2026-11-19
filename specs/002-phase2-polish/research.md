# Research: Houdini Monitor Phase 2

## Decision: Network metrics via OSHI NetworkIF deltas

**Rationale**: OSHI exposes `NetworkIF` with bytes recv/sent; rates require previous sample deltas / elapsed seconds. Aggregate all non-loopback interfaces for sidebar + chart; list top interfaces in detail.

**Alternatives considered**: SNMP/WMI raw — rejected (complexity, new deps).

## Decision: GPU via OSHI GraphicsCard / best-effort sensors

**Rationale**: OSHI graphics enumeration may give name/VRAM without utilization on many Windows machines. Spec allows N/A. Collector wraps attempts in try/catch; `GpuSnapshot.available=false` when no useful data.

**Alternatives considered**: NVIDIA NVML native — rejected (vendor lock-in, deps outside constitution).

## Decision: Per-core CPU via `getProcessorCpuLoadBetweenTicks`

**Rationale**: Standard OSHI pattern alongside system ticks; store `List<CpuCoreSnapshot>`.

## Decision: AWT SystemTray + Platform.runLater

**Rationale**: JavaFX has no first-class tray on Windows; AWT tray is the common pattern. Close request consumes event and hides stage when tray supported; Exit calls Application exit / stop.

**Alternatives considered**: Third-party tray libs — rejected (YAGNI).

## Decision: java.util.prefs for persistence

**Rationale**: Zero new deps; sufficient for theme/locale/alwaysOnTop. Corrupt values → defaults (ES, SYSTEM, false).

## Decision: Green accent CSS overlay

**Rationale**: Keep AtlantaFX PrimerLight/PrimerDark (or current) as base; load `houdini-accent.css` with `-houdini-accent` variables for charts, selection, sidebar `.active`.

## Decision: Process filter/sort as pure domain helper

**Rationale**: `ProcessQuery` filters + sorts immutable lists for testability; TableView columns bind sort but domain helper ensures consistent default + search.

## Decision: Network history uses ResourceKind.NETWORK with synthetic % or dual chart

**Rationale**: MetricHistory today stores usagePercent. For network, chart bytes/s by mapping a normalized display series OR store percent-of-peak within window. Prefer charting Mbps-like values via `usagePercent` field reused as “primary series value” with detail label units — cleaner: extend chart to accept a `double` series with unit label. Minimal change: store network aggregate as two ResourceSnapshots in dedicated histories (`networkIn`, `networkOut`) using ResourceKind.NETWORK and detailLabel in/out; chart primary series = combined or inbound. Spec asks aggregate history — chart sum of in+out rates scaled for display, with labels showing real KB/s or MB/s in detail pane.
