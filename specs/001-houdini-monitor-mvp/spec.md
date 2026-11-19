# Feature Specification: Houdini Monitor MVP

**Feature Branch**: `001-houdini-monitor-mvp`

**Created**: 2026-11-19

**Status**: Draft

**Input**: User description: "Houdini Monitor — Windows desktop performance monitor (Task Manager / btop style) for DAM portfolio. MVP: CPU, RAM, Disk, process list, kill with confirmation, ~1s refresh, ~60s history charts, Light/Dark/Follow system themes, Spanish+English i18n, installable Windows app + Spanish README."

## Clarifications

### Session 2026-11-19

- Q: ¿Cómo se representa el uso de Disco en el MVP? → A: Porcentaje de espacio usado agregado de discos fijos locales; detalle puede listar volúmenes principales.
- Q: ¿Idioma por defecto al primer arranque? → A: Español (con cambio a inglés en sesión).
- Q: ¿Orden por defecto de la lista de procesos? → A: CPU % descendente.
- Q: ¿Qué cuenta como proceso “de sistema” para advertencia extra? → A: Heurística conservadora: usuario SYSTEM/NT AUTHORITY o nombres bien conocidos del SO.
- Q: ¿Se persisten tema e idioma entre reinicios en el MVP? → A: No obligatorio; preferencias de sesión bastan.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Ver recursos del sistema en vivo (Priority: P1)

Un usuario abre Houdini Monitor y ve de inmediato el uso actual de CPU, RAM y Disco, actualizado aproximadamente cada segundo, en un layout tipo Administrador de tareas moderno (barra lateral de recursos + panel de detalle).

**Why this priority**: Es el valor central del producto; sin métricas en vivo no hay monitor.

**Independent Test**: Arrancar la app y comprobar que CPU, RAM y Disco muestran valores coherentes y se refrescan ~cada segundo.

**Acceptance Scenarios**:

1. **Given** la aplicación arranca en Windows, **When** el usuario mira la barra lateral de recursos, **Then** ve entradas para CPU, RAM y Disco con valores actuales.
2. **Given** la aplicación está en ejecución, **When** pasa aproximadamente un segundo, **Then** los valores de CPU, RAM y Disco se actualizan sin bloquear la interfaz.
3. **Given** el usuario selecciona un recurso en la barra lateral, **When** mira el panel de detalle, **Then** ve el detalle de ese recurso incluyendo un gráfico de historial reciente (~60 s) para CPU, RAM o Disco.

---

### User Story 2 - Inspeccionar y terminar procesos (Priority: P1)

Un usuario consulta la lista de procesos (columnas: Nombre, PID, CPU %, RAM, Usuario), selecciona uno y solicita terminarlo. El sistema siempre pide confirmación; si el proceso parece del sistema, muestra una advertencia extra. Si el kill falla por permisos, muestra un mensaje claro sin intentar elevación automática.

**Why this priority**: Capacidad distintiva frente a “solo mirar”; necesaria para demo de portfolio y uso real tipo Task Manager.

**Independent Test**: Abrir la lista de procesos, intentar matar un proceso de usuario con confirmación, y verificar mensaje claro ante denegación de permisos (sin elevación automática).

**Acceptance Scenarios**:

1. **Given** hay procesos en ejecución, **When** el usuario abre la vista de procesos, **Then** ve una tabla con Nombre, PID, CPU %, RAM y Usuario.
2. **Given** el usuario selecciona un proceso, **When** solicita terminarlo, **Then** el sistema muestra un diálogo de confirmación antes de actuar.
3. **Given** el proceso seleccionado parece crítico/del sistema, **When** el usuario solicita terminarlo, **Then** el diálogo incluye una advertencia adicional.
4. **Given** el usuario confirma el kill, **When** el sistema no tiene permisos suficientes, **Then** muestra un mensaje claro de fallo por permisos y NO solicita elevación UAC automáticamente.
5. **Given** el usuario cancela el diálogo de confirmación, **When** cierra el diálogo, **Then** el proceso sigue en ejecución.

---

### User Story 3 - Temas e idioma (Priority: P2)

Un usuario elige tema Claro, Oscuro o Seguir sistema, y puede usar la interfaz en español o inglés.

**Why this priority**: Mejora profesionalismo del portfolio y usabilidad; no bloquea el núcleo de métricas.

**Independent Test**: Cambiar tema entre Claro/Oscuro/Seguir sistema y cambiar idioma ES/EN comprobando que la UI refleja el cambio.

**Acceptance Scenarios**:

1. **Given** la app está abierta, **When** el usuario elige tema Oscuro (o Claro), **Then** la interfaz aplica ese tema de inmediato.
2. **Given** el usuario elige Seguir sistema, **When** el tema del SO es claro u oscuro, **Then** la app sigue el tema del sistema.
3. **Given** la app está abierta, **When** el usuario cambia el idioma entre español e inglés, **Then** los textos de la interfaz se muestran en el idioma elegido.

---

### User Story 4 - Entrega portfolio (Priority: P2)

Un evaluador o el propio alumno puede construir/ejecutar una aplicación instalable para Windows y leer un README en español que explique qué es, cómo ejecutarla/construirla y cómo defender el proyecto.

**Why this priority**: Criterio de “done” del brief para defensa DAM.

**Independent Test**: Seguir el README en español para ejecutar o construir el entregable Windows y verificar que la app usable arranca.

**Acceptance Scenarios**:

1. **Given** el repositorio del proyecto, **When** un evaluador lee el README en español, **Then** encuentra descripción del producto, alcance MVP, instrucciones de ejecución/construcción y notas de defensa.
2. **Given** el entorno de build documentado, **When** se genera el entregable Windows, **Then** se obtiene un ejecutable/instalable usable en Windows.

---

### Edge Cases

- ¿Qué ocurre si un proceso desaparece entre la selección y la confirmación de kill? El sistema informa que el proceso ya no existe y no falla de forma silenciosa.
- ¿Qué ocurre si no hay discos visibles o un disco no reporta datos? Se muestra un estado vacío o “no disponible” sin cerrar la app.
- ¿Qué ocurre en el primer segundo de vida (aún sin historial)? Los gráficos muestran los puntos disponibles (puede ser vacío o un solo punto) y se van llenando hasta ~60 s.
- ¿Qué ocurre si el usuario intenta matar el propio proceso de Houdini Monitor? Se trata como cualquier proceso con confirmación (y advertencia si aplica por reglas de “sistema”); no hay elevación automática.
- ¿Qué ocurre si el muestreo es lento bajo carga? La UI permanece responsiva; los valores pueden actualizarse con ligero retraso respecto al intervalo nominal de ~1 s.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: CUANDO la aplicación está en ejecución, EL SISTEMA MUST muestrear y mostrar el uso de CPU, RAM y Disco aproximadamente cada 1 segundo sin bloquear la interfaz de usuario.
- **FR-001a**: EL SISTEMA MUST representar el uso de Disco como porcentaje de espacio usado agregado de discos fijos locales (el detalle MAY listar volúmenes principales).
- **FR-002**: CUANDO el usuario selecciona CPU, RAM o Disco, EL SISTEMA MUST mostrar un panel de detalle con un gráfico de historial en memoria de aproximadamente los últimos 60 segundos para ese recurso.
- **FR-003**: EL SISTEMA MUST presentar un layout moderno tipo Administrador de tareas: barra lateral de recursos y panel de detalle.
- **FR-004**: EL SISTEMA MUST mostrar una lista de procesos con las columnas Nombre, PID, CPU %, RAM y Usuario, ordenada por defecto por CPU % descendente.
- **FR-005**: CUANDO el usuario solicita terminar un proceso, EL SISTEMA MUST pedir confirmación explícita antes de ejecutar el kill.
- **FR-006**: SI el proceso seleccionado parece del sistema o crítico (heurística: usuario SYSTEM/NT AUTHORITY o nombre bien conocido del SO), ENTONCES EL SISTEMA MUST mostrar una advertencia adicional en la confirmación.
- **FR-009a**: EL SISTEMA MUST arrancar con idioma español por defecto; el usuario MAY cambiar a inglés en la misma sesión.
- **FR-007**: SI el kill falla por falta de permisos, ENTONCES EL SISTEMA MUST mostrar un mensaje claro al usuario y MUST NOT intentar elevación UAC automática.
- **FR-008**: EL SISTEMA MUST ofrecer temas Claro, Oscuro y Seguir sistema.
- **FR-009**: EL SISTEMA MUST ofrecer localización de la interfaz en español e inglés desde el MVP.
- **FR-010**: EL SISTEMA MUST proporcionar un README en español orientado a defensa de portfolio (qué es, alcance, cómo ejecutar/construir, notas relevantes).
- **FR-011**: EL SISTEMA MUST poder entregarse como aplicación usable en Windows mediante un empaquetado ejecutable (`.exe` / instalable) documentado en el README.
- **FR-012**: MIENTRAS la aplicación se cierra, EL SISTEMA MUST detener el muestreo de forma ordenada sin dejar trabajo de fondo colgado de forma indefinida.

### Out of Scope (MVP)

- Red (interfaces, tráfico, conexiones)
- GPU
- Alertas / notificaciones
- Persistencia de historial en disco
- Elevación UAC automática o asistente de privilegios elevados

### Key Entities

- **ResourceSnapshot**: Uso puntual de un recurso (CPU, RAM o Disco) en un instante, con valor(es) normalizados para visualización.
- **MetricHistory**: Serie temporal en memoria (~60 s) de snapshots para un recurso.
- **ProcessInfo**: Proceso en ejecución con Nombre, PID, CPU %, RAM y Usuario.
- **KillConfirmationDecision**: Resultado de las reglas de confirmación (confirmación estándar vs advertencia extra de proceso de sistema; cancelar vs confirmar).
- **UserPreferences**: Tema (Claro/Oscuro/Seguir sistema) e idioma (ES/EN) de la sesión/UI.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Un usuario puede abrir la app y, en menos de 10 segundos, ver valores actuales de CPU, RAM y Disco.
- **SC-002**: Los indicadores de recursos se actualizan de forma perceptible al menos una vez por segundo en condiciones normales (máquina de desarrollo típica).
- **SC-003**: Un usuario puede localizar un proceso por nombre o PID y completar (o cancelar) el flujo de terminación con confirmación en menos de 30 segundos.
- **SC-004**: En un intento de kill denegado por permisos, el usuario ve un mensaje comprensible y la app no solicita elevación automática.
- **SC-005**: El usuario puede cambiar tema e idioma y ver el cambio reflejado en la UI sin reiniciar la aplicación (salvo limitación explícita documentada).
- **SC-006**: Existe un README en español que permite a un evaluador ejecutar o construir el entregable Windows y entender el alcance MVP en una lectura de ≤15 minutos.
- **SC-007**: El entregable Windows documentado arranca y muestra la UI principal del monitor en un entorno Windows de prueba.

## Assumptions

- Audiencia: portfolio DAM + usuarios normales que conocen el Administrador de tareas de Windows.
- Plataforma objetivo del MVP: Windows de escritorio.
- “Proceso de sistema / crítico” se determina con heurísticas conservadoras (p. ej. nombres/usuarios típicos del sistema); falsos positivos son preferibles a matar sin advertencia.
- El historial de ~60 s vive solo en memoria durante la sesión.
- Preferencias de tema e idioma bastan en sesión; persistencia entre reinicios no es requisito MVP.
- Identificadores de código en inglés; textos de producto/UI y README de defensa en español (con i18n inglés disponible en la UI).
- Red, GPU, alertas, historial en disco y auto-elevación quedan explícitamente fuera del MVP.
