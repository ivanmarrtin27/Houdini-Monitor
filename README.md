# Houdini Monitor

Monitor de rendimiento de escritorio para **Windows** (estilo Administrador de tareas / btop). Proyecto portfolio DAM desarrollado con **Spec-Driven Development** (Spec Kit) y Java.

> Enfoque actual: Windows 10/11. No se promete soporte multiplataforma.

## Qué es

Houdini Monitor muestra en tiempo real:

- **CPU** (uso agregado y por núcleo)
- **RAM**
- **Disco** (por volumen)
- **Red** (throughput e interfaces)
- **GPU** (best-effort; muestra N/D si no hay datos)
- **Procesos**: búsqueda, filtrado, ordenación y terminar proceso con confirmación

También incluye bandeja del sistema (mostrar / ocultar / salir), opción «siempre encima», temas Claro / Oscuro / Seguir sistema, uptime de sistema/sesión, atajo Ctrl+F e interfaz en **español** e **inglés**.

## Capturas

*(Pendiente: añade aquí capturas de la ventana principal, detalle de CPU/red y la tabla de procesos.)*

```text
docs/screenshots/
  ├── overview.png
  ├── processes.png
  └── gpu-or-network.png
```

## Stack

| Capa | Tecnología |
|------|------------|
| Lenguaje | Java 21+ (toolchain Gradle) |
| UI | JavaFX 21 + AtlantaFX |
| Métricas | OSHI |
| Persistencia local | JSON en `%USERPROFILE%\.houdini-monitor\` |
| Logging | SLF4J + Logback |
| Build | Gradle Wrapper 8.x |
| Empaquetado | `jpackage` (app-image `.exe`) |
| Método de trabajo | Spec Kit (constitución + specs EARS) |

## Requisitos

- Windows 10/11
- **JDK 21+** (recomendado JDK 24; `jpackage` necesita un JDK completo, no solo JRE)
- Conexión a Internet la primera vez (Gradle descarga dependencias)

## Cómo ejecutar (desarrollo)

No uses el botón *Run* de extensiones Java genéricas con JavaFX: puede fallar por el classpath/módulos. Usa Gradle.

### Opción rápida

Doble clic en `run-app.bat`, o desde PowerShell en la raíz del repo:

```powershell
.\run-app.bat
```

El script sitúa el directorio de trabajo, busca un JDK 21+ (prefiere `jdk-24` y respeta un `JAVA_HOME` válido) y lanza `gradlew :app:run`.

### Manual

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-24"   # ajusta a tu JDK 21+
.\gradlew.bat :app:run
```

## Cómo probar

```powershell
.\gradlew.bat :app:test
```

Los tests cubren lógica pura: historial de métricas, kill, disco, filtro/orden de procesos, tasas de red, preferencias/settings y fallback de GPU.

## Cómo generar el ejecutable (jpackage)

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-24"   # JDK completo con jpackage
.\gradlew.bat :app:jpackage
```

Salida esperada:

```text
app\build\jpackage\HoudiniMonitor\HoudiniMonitor.exe
```

Los artefactos `build/`, `dist/` y el `.exe` no se versionan (regenerables). Tras empaquetar, puedes copiar jar/launcher al Escritorio:

```powershell
powershell -File .\scripts\stage-desktop-launcher.ps1
```

El entrypoint empaquetado es `com.houdini.monitor.Launcher`.

## Alcance (Phase 2 + foundation)

| Incluido | Fuera de alcance (por ahora) |
|----------|------------------------------|
| CPU, RAM, Disco, Red, GPU best-effort | Alertas / notificaciones |
| Procesos con búsqueda/orden + kill confirmado | Historial de métricas en disco / SQLite |
| Bandeja + siempre encima | Telemetría remota |
| Preferencias persistentes (JSON) | Auto-elevación UAC |
| Uptime + Ctrl+F | Instalador MSI (se entrega app-image) |
| Empaquetado Windows `.exe` (jpackage) | |

Specs históricas / activas:

- MVP: `specs/001-houdini-monitor-mvp/`
- Phase 2 polish: `specs/002-phase2-polish/`
- Foundation (capas collector/model/service/storage): `docs/architecture/phase1-foundation.md`

## Arquitectura (resumen)

- **Dominio** (`domain`): snapshots inmutables (cores, red, GPU), `ProcessQuery`, historial en memoria (~60 s).
- **Métricas** (`metrics` / `collector`): OSHI + muestreo ~1 s **fuera** del hilo JavaFX; apagado en dos fases.
- **Servicio** (`service`): `MonitoringService`, `MetricsHub`, preferencias.
- **Storage** (`storage`): `JsonSettingsStore` → `%USERPROFILE%\.houdini-monitor\settings.json`.
- **UI** (`ui`): JavaFX + AtlantaFX + CSS de acento; bandeja AWT + stage JavaFX.
- **i18n**: `ResourceBundle` ES/EN.
- **Spec Kit**: constitución + feature `002-phase2-polish` (Converged).

Más detalle: [`docs/architecture/phase1-foundation.md`](docs/architecture/phase1-foundation.md).

## Notas para defensa / portfolio (DAM)

1. Requisitos en formato EARS dentro de las specs Spec Kit.
2. Concurrencia: muestreo off-FX, snapshots inmutables, callbacks de bandeja con `Platform.runLater`, shutdown limpio.
3. Kill de procesos: confirmación + aviso de procesos de sistema; sin UAC automático.
4. GPU/red degradan a N/D o ceros sin tumbar la aplicación.
5. Sin telemetría remota.

## Spec Kit

Artefactos relevantes:

- `.specify/memory/constitution.md`
- `specs/002-phase2-polish/spec.md`
- `specs/002-phase2-polish/plan.md`
- `specs/002-phase2-polish/tasks.md`

## Licencia

Uso educativo / portfolio. Sin telemetría remota. Si necesitas una licencia formal (MIT, Apache-2.0, etc.), añádela en un fichero `LICENSE` en la raíz.
