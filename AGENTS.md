# AGENTS.md — Houdini Monitor

Monitor de rendimiento de escritorio (estilo Task Manager / btop) para Windows.
Proyecto portfolio DAM: Spec-Driven Development con Spec Kit + Java.

## Stack y estructura

- Java 21+ (Virtual Threads), JavaFX, AtlantaFX, OSHI.
- Spec Kit: `.specify/` (plantillas, scripts y workflows).
- Specs: `specs/002-phase2-polish/` (MVP histórico: `specs/001-houdini-monitor-mvp/`).
- Código de la app: módulo Gradle `app/` (`com.houdini.monitor`).

## Comandos Spec Kit

- Skills / comandos: `/speckit-constitution`, `/speckit-specify`, `/speckit-clarify`, `/speckit-plan`, `/speckit-checklist`, `/speckit-tasks`, `/speckit-analyze`, `/speckit-implement`, `/speckit-converge`.
- CLI: `specify version`, `specify check` (PATH debe incluir `%USERPROFILE%\.local\bin`).

## Convenciones

- Código e identificadores en inglés; UI y docs de producto en español.
- Requisitos en EARS (CUANDO / SI…ENTONCES / MIENTRAS / EL SISTEMA…).
- Una tarea Spec Kit a la vez; tests primero en lógica pura (métricas, umbrales).
- La spec manda: nada fuera de la spec activa sin preguntar.

## Forma de trabajar

- Planificar (`/speckit-*` de especificación) antes de implementar features grandes.
- Cambios pequeños y enfocados; no reescribir lo que ya funciona.
- Notas locales de progreso (si existen) no forman parte del entregable público.

## Límites

- Siempre: respetar constitución Spec Kit; no inventar requisitos.
- Pregunta antes: dependencias nuevas, cambios de modelo de datos, matar procesos con privilegios elevados.
- Nunca: secretos en repo; telemetría remota sin consentimiento; implementar “por gusto” fuera de la spec.

## Verificación

- Tras cada tarea de lógica: tests unitarios en verde (`.\gradlew.bat :app:test`).
- Tras UI: arrancar la app (`.\run-app.bat` o `.\gradlew.bat :app:run`) y comprobar el flujo manual de la spec.
- `/speckit-converge` hasta veredicto Converged.
