# Contract: Process Table UX

## Search

- Text field above table; Ctrl+F focuses it when Processes view active.
- Filter: case-insensitive contains on name, stringified PID, or user.
- Empty filter → all processes (subject to sort).
- No matches → placeholder / empty state message (localized).

## Sort

- Clickable column headers for Name, PID, CPU %, RAM, User.
- Visual indicator of active column + direction.
- Default when no user sort: CPU % descending (MVP).

## Kill

Unchanged from MVP `process-kill-flow`: confirm → optional system warning → execute → map outcomes; no UAC elevation.
