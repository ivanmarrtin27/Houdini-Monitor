# UI Contract: Navigation & Layout

## Shell

- Window title: localized product name ("Houdini Monitor").
- Layout: left **resource sidebar** + right **detail pane**.
- Sidebar entries: CPU, RAM, Disco/Disk, Procesos/Processes.
- Selecting a sidebar entry updates the detail pane without restarting sampling.

## Resource detail (CPU / RAM / Disk)

- Shows current usage % (and for Disk, optional volume list).
- Shows a simple line chart of the last ~60 seconds of samples for that resource.
- Empty/partial history: chart renders available points without error.

## Processes detail

- Table columns (localized headers): Name, PID, CPU %, RAM, User.
- Default sort: CPU % descending.
- Selection enables a "End process" / "Terminar proceso" action.
- RAM displayed in human-readable units (e.g. MB/GB).

## Theme & language controls

- Theme control: Light / Dark / Follow system (localized labels).
- Language control: Español / English.
- Changing theme or language updates visible strings/styles without requiring app restart.
