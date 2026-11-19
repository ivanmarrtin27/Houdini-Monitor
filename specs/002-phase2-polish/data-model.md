# Data Model: Houdini Monitor Phase 2

## ResourceKind

Enum: `CPU`, `RAM`, `DISK`, `NETWORK`, `GPU`, `PROCESSES`

## CpuCoreSnapshot

| Field | Type | Rules |
|-------|------|-------|
| index | int | ≥ 0 |
| usagePercent | double | 0–100 (clamped) |

## NetworkInterfaceSnapshot

| Field | Type | Rules |
|-------|------|-------|
| name | String | display name |
| bytesPerSecIn | double | ≥ 0 |
| bytesPerSecOut | double | ≥ 0 |
| ipv4 | String | may be empty |

## GpuSnapshot

| Field | Type | Rules |
|-------|------|-------|
| available | boolean | false → UI shows N/A |
| name | String | may be empty |
| usagePercent | Double | optional; null if unknown |
| memoryUsedBytes | Long | optional |
| memoryTotalBytes | Long | optional |

## SystemMetricsSnapshot (extended)

| Field | Type |
|-------|------|
| capturedAtEpochMs | long |
| cpu | ResourceSnapshot |
| cpuCores | List\<CpuCoreSnapshot\> |
| ram | ResourceSnapshot |
| disk | ResourceSnapshot |
| diskVolumes | List\<ResourceSnapshot\> |
| network | ResourceSnapshot (aggregate “load” proxy or 0; detail uses rates) |
| networkBytesPerSecIn | double |
| networkBytesPerSecOut | double |
| networkInterfaces | List\<NetworkInterfaceSnapshot\> |
| gpu | GpuSnapshot |
| processes | List\<ProcessInfo\> |
| systemBootTimeEpochMs | long |
| systemUptimeSeconds | long |

## ProcessQuery (pure)

| Input | Rules |
|-------|-------|
| processes | immutable list |
| searchText | trim; match name/pid/user contains ignore-case |
| sortColumn | NAME, PID, CPU, RAM, USER |
| ascending | boolean |

**Output**: filtered then sorted list.

## UserPreferences (persisted)

| Field | Type | Default |
|-------|------|---------|
| themeMode | LIGHT/DARK/SYSTEM | SYSTEM |
| locale | es/en | es |
| alwaysOnTop | boolean | false |

## MetricHistory

Unchanged ring buffer; kinds used for charts: CPU, RAM, DISK, NETWORK (aggregate display series), GPU (when available %).

## State transitions

```text
Window: VISIBLE ↔ HIDDEN_TRAY (if tray) → EXITING → STOPPED
Sampling: STOPPED → RUNNING → STOPPING → STOPPED (on Exit / Application.stop)
```
