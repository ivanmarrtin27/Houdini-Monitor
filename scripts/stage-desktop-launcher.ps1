# Copies Monitor jar to Desktop and writes a reliable Desktop .bat launcher.
# Prefer jpackage .exe when present; else fall back to run-app.bat.

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$Desktop = [Environment]::GetFolderPath("Desktop")
$DesktopJarName = "Houdini Monitor.jar"

$candidates = @(
    (Join-Path $Root "dist\HoudiniMonitor\app\HoudiniMonitor.jar"),
    (Join-Path $Root "app\build\jpackage\HoudiniMonitor\app\HoudiniMonitor.jar"),
    (Join-Path $Root "app\build\libs\app-1.0.0.jar")
)

$srcJar = $candidates | Where-Object { Test-Path -LiteralPath $_ } | Select-Object -First 1
if (-not $srcJar) {
    throw "No Monitor jar found. Build with: .\gradlew.bat :app:jpackage   or   .\gradlew.bat :app:jar"
}

Copy-Item -LiteralPath $srcJar -Destination (Join-Path $Desktop $DesktopJarName) -Force

$exeCandidates = @(
    (Join-Path $Root "dist\HoudiniMonitor\HoudiniMonitor.exe"),
    (Join-Path $Root "app\build\jpackage\HoudiniMonitor\HoudiniMonitor.exe")
)
$exe = $exeCandidates | Where-Object { Test-Path -LiteralPath $_ } | Select-Object -First 1
$runBat = Join-Path $Root "run-app.bat"

$batLines = @(
    "@echo off"
    "setlocal EnableExtensions"
    "title Houdini Monitor"
    ""
)

if ($exe) {
    $batLines += @(
        "set `"EXE=$exe`""
        "if exist `"%EXE%`" ("
        "  start `"`" `"%EXE%`""
        "  exit /b 0"
        ")"
        ""
    )
}

$batLines += @(
    "REM Fallback: Gradle run"
    "set `"PROJECT_BAT=$runBat`""
    "if not exist `"%PROJECT_BAT%`" ("
    "  echo ERROR: No se encuentra Houdini Monitor.exe ni run-app.bat"
    "  pause"
    "  exit /b 1"
    ")"
    "call `"%PROJECT_BAT%`""
    "exit /b %ERRORLEVEL%"
)

$batPath = Join-Path $Desktop "Houdini Monitor.bat"
Set-Content -LiteralPath $batPath -Value ($batLines -join "`r`n") -Encoding ASCII

Write-Host "Desktop jar: $(Join-Path $Desktop $DesktopJarName)"
Write-Host "Desktop bat: $batPath"
if ($exe) { Write-Host "Uses exe: $exe" } else { Write-Host "Uses fallback: $runBat" }
