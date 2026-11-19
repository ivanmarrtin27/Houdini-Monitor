# Phase 1 — Foundation (Houdini Monitor / PulseDeck vision)

PulseDeck is the long-term codename/vision. Product name remains **Houdini Monitor**.

## Data / thread flow

```
[OSHI / collectors]                 background daemon thread
        |                           "houdini-metrics-sampler"
        v
 MetricCollector<T>.collect()  --> immutable SystemMetricsSnapshot
        |
        v
   MetricsSampler  -----------> MetricsHub.publish(snapshot)
        |                              |
        |                    CircularBuffer histories (CPU/RAM/DISK/NET/GPU)
        |                              |
        |                              +--> listeners (CopyOnWrite)
        |                                        |
        |                         Platform.runLater(...)  [FX thread]
        |                                        |
        v                                        v
 MonitoringService                    MainView / StatusBar / charts
        |
        +--> MetricHistory (UI-compatible façade over CircularBuffer)

[UI theme / prefs]  --IO-->  JsonSettingsStore (~/.houdini-monitor/settings.json)
[logging]           --IO-->  ~/.houdini-monitor/logs/  (logback; never on FX critical path)
```

## Decisions
1. Keep Gradle — already working and mandated by AGENTS.md.
2. Keep product name Houdini Monitor; PulseDeck only in design docs.
3. Evolve in place under `app/` — no parallel Maven tree.
4. Phase 1 deps: Jackson + SLF4J/logback only (no SQLite).
5. Extend MVP/Phase-2 code; Spec Kit `002` remains historical/converged — foundation documented here.
