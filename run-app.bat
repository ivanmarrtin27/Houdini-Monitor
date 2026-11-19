@echo off
setlocal EnableExtensions
title Houdini Monitor
cd /d "%~dp0"

set "HM_VER_FILE=%TEMP%\houdini-monitor-java-ver.txt"

REM --- Comprobar Gradle Wrapper ---
if not exist "%~dp0gradlew.bat" (
  echo [ERROR] No se encuentra gradlew.bat en:
  echo   %~dp0
  echo Asegurate de estar en la raiz del proyecto Houdini Monitor.
  goto :fail
)

REM --- Resolver JAVA_HOME ---
set "BEST_HOME="
set "BEST_MAJOR=0"

if not defined JAVA_HOME goto :try_jdk24

call :is_suitable_jdk "%JAVA_HOME%"
if not errorlevel 1 (
  echo Usando JAVA_HOME existente.
  goto :java_ready
)
echo JAVA_HOME actual no es valido o es ^< 21: %JAVA_HOME%
echo Buscando un JDK 21+ en Program Files\Java...

:try_jdk24
if not exist "C:\Program Files\Java\jdk-24\bin\java.exe" goto :scan_jdks
call :is_suitable_jdk "C:\Program Files\Java\jdk-24"
if errorlevel 1 goto :scan_jdks
set "JAVA_HOME=C:\Program Files\Java\jdk-24"
goto :java_ready

:scan_jdks
for /d %%D in ("C:\Program Files\Java\jdk-*") do call :consider_jdk "%%~fD"
if not defined BEST_HOME goto :no_jdk
set "JAVA_HOME=%BEST_HOME%"
goto :java_ready

:no_jdk
echo [ERROR] No se encontro un JDK 21 o superior.
echo Instala un JDK 21+ ^(recomendado jdk-24^) en:
echo   C:\Program Files\Java\
echo O define JAVA_HOME apuntando a ese JDK.
goto :fail

:java_ready
set "PATH=%JAVA_HOME%\bin;%PATH%"
echo.
echo Houdini Monitor
echo ---------------
echo JAVA_HOME = %JAVA_HOME%
call :print_java_version
echo Gradle   = .\gradlew.bat :app:run
echo.

call "%~dp0gradlew.bat" :app:run
set "EXITCODE=%ERRORLEVEL%"
if not "%EXITCODE%"=="0" (
  echo.
  echo [ERROR] La aplicacion termino con codigo %EXITCODE%.
  goto :fail_with_code
)
call :cleanup
exit /b 0

:fail
call :cleanup
echo.
pause
exit /b 1

:fail_with_code
call :cleanup
echo.
pause
exit /b %EXITCODE%

:cleanup
if exist "%HM_VER_FILE%" del /q "%HM_VER_FILE%" >nul 2>&1
exit /b 0

:consider_jdk
call :is_suitable_jdk "%~1"
if errorlevel 1 exit /b 0
call :read_major "%~1"
if not defined MAJOR exit /b 0
if %MAJOR% GTR %BEST_MAJOR% (
  set "BEST_MAJOR=%MAJOR%"
  set "BEST_HOME=%~1"
)
exit /b 0

:is_suitable_jdk
if not exist "%~1\bin\java.exe" exit /b 1
call :read_major "%~1"
if not defined MAJOR exit /b 1
if %MAJOR% LSS 21 exit /b 1
exit /b 0

REM Fichero temporal: FOR /F rompe rutas con espacios (Program Files).
:read_major
set "MAJOR="
set "VER="
"%~1\bin\java.exe" -version >"%HM_VER_FILE%" 2>&1
if errorlevel 1 exit /b 0
for /f "tokens=3 delims= " %%A in ('findstr /i "version" "%HM_VER_FILE%"') do (
  set "VER=%%~A"
  goto :parse_major
)
exit /b 0

:parse_major
for /f "tokens=1 delims=." %%M in ("%VER%") do set "MAJOR=%%M"
exit /b 0

:print_java_version
"%JAVA_HOME%\bin\java.exe" -version >"%HM_VER_FILE%" 2>&1
for /f "usebackq delims=" %%V in ("%HM_VER_FILE%") do (
  echo Java     = %%V
  exit /b 0
)
exit /b 0
