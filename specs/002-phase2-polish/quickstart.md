# Quickstart: Phase 2 verification

## Dev run

```powershell
.\gradlew.bat :app:run
```

## Tests

```powershell
.\gradlew.bat :app:test
```

## Package

```powershell
# JAVA_HOME must point to JDK 21+ with jpackage
.\gradlew.bat :app:jpackage
```

Output: `app\build\jpackage\HoudiniMonitor\HoudiniMonitor.exe`

Copy folder to Desktop:

```powershell
$src = "app\build\jpackage\HoudiniMonitor"
$dst = "$env:USERPROFILE\OneDrive\Desktop\HoudiniMonitor"
Remove-Item -Recurse -Force $dst -ErrorAction SilentlyContinue
Copy-Item -Recurse $src $dst
```

Refresh shortcut `Houdini Monitor.lnk` → `$dst\HoudiniMonitor.exe`.

## Manual smoke

1. Sidebar shows CPU/RAM/Disk/Network/GPU; GPU may be N/D.
2. CPU detail shows cores; Disk shows volumes; Network shows rates.
3. Processes: search, sort columns, Ctrl+F, kill confirm still works.
4. Green accent visible on active sidebar + charts.
5. Always-on-top toggle; close to tray; Show; Exit cleans up.
6. Restart: theme/language/always-on-top restored.
