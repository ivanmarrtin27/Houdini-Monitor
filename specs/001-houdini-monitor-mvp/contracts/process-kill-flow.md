# UI/Domain Contract: Process Kill Flow

## Preconditions

- A process row is selected in the processes table.
- Sampling may continue in the background during the dialog.

## Flow

1. User triggers **Terminar proceso** / **End process**.
2. System evaluates `KillConfirmationPolicy`:
   - Always show confirmation dialog with process name + PID.
   - If `systemLooking == true`, show **additional warning** text before confirm is accepted.
3. If user cancels at any step → `KillOutcome.CANCELLED`; no terminate call.
4. If user confirms:
   - Attempt terminate without elevation.
   - Map result:
     - Process gone before kill → `NOT_FOUND` + localized message.
     - Access denied / permission error → `ACCESS_DENIED` + clear localized message (**no UAC prompt**).
     - Other failure → `FAILED` + localized message.
     - Success → `SUCCESS`; process disappears on next sample (or immediately removed from table).

## Non-goals

- No automatic UAC elevation.
- No silent kill without confirmation.
