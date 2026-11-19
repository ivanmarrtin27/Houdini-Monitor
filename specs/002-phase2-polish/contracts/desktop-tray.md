# Contract: Desktop Tray & Window

## Always on top

Toggle in toolbar; sets `Stage.setAlwaysOnTop`; persisted in preferences.

## Tray (when SystemTray supported)

- Icon + tooltip “Houdini Monitor”
- Menu: Show, Hide, Exit
- Window close (X): hide to tray (consume close); do not stop sampler
- Exit: stop sampler (two-phase), remove tray icon, `Platform.exit`

## Tray unavailable

Close (X) performs full shutdown as MVP.

## Preferences load

On start: apply theme, locale, alwaysOnTop before showing stage.
