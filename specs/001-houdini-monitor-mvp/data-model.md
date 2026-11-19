# Data Model: Houdini Monitor MVP

## ResourceKind

Enum: `CPU`, `RAM`, `DISK`, `PROCESSES`

## ResourceSnapshot

| Field | Type | Rules |
|-------|------|-------|
| kind | ResourceKind | CPU, RAM, or DISK |
| timestampEpochMs | long | monotonic sampling time |
| usagePercent | double | 0.0–100.0 inclusive |
| detailLabel | String | optional human label (e.g. volume path) |

## MetricHistory

| Field | Type | Rules |
|-------|------|-------|
| kind | ResourceKind | one history per CPU/RAM/DISK |
| capacity | int | ~60 (one sample/sec for ~60s) |
| samples | ordered list of ResourceSnapshot | drop oldest when full |

**Validation**: Reject null snapshots; ignore out-of-range % by clamping in collector tests.

## ProcessInfo

| Field | Type | Rules |
|-------|------|-------|
| name | String | non-blank display name |
| pid | long | > 0 |
| cpuPercent | double | ≥ 0 |
| memoryBytes | long | ≥ 0 |
| user | String | may be empty/"?" if unavailable |
| systemLooking | boolean | derived by KillConfirmationPolicy |

**Identity**: `pid` unique within a snapshot; processes may disappear between samples.

## SystemMetricsSnapshot

Immutable aggregate published each tick:

| Field | Type |
|-------|------|
| capturedAtEpochMs | long |
| cpu | ResourceSnapshot |
| ram | ResourceSnapshot |
| disk | ResourceSnapshot |
| diskVolumes | List\<ResourceSnapshot\> (optional per-volume) |
| processes | List\<ProcessInfo\> (sorted CPU % desc by default) |

## KillConfirmationPolicy / KillRequest

| Field | Type | Rules |
|-------|------|-------|
| process | ProcessInfo | required |
| requiresExtraWarning | boolean | true if system-looking |
| userConfirmed | boolean | must be true to proceed |
| userAcceptedExtraWarning | boolean | required if requiresExtraWarning |

## KillOutcome

Enum-like result: `SUCCESS`, `CANCELLED`, `NOT_FOUND`, `ACCESS_DENIED`, `FAILED`

## UserPreferences

| Field | Type | Rules |
|-------|------|-------|
| theme | enum LIGHT, DARK, SYSTEM | default SYSTEM or LIGHT |
| locale | Locale | default `es`; supported `es`, `en` |

Session-scoped for MVP (no persistence required).

## State transitions

```text
Sampling: STOPPED → RUNNING → STOPPING → STOPPED
Kill flow: IDLE → CONFIRM (→ EXTRA_WARN if system) → EXECUTING → RESULT → IDLE
```
