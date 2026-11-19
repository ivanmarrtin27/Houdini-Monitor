# Feature Specification: Houdini Monitor Phase 2 Polish

**Feature Branch**: `002-phase2-polish`

**Created**: 2026-11-19

**Status**: Draft

**Input**: User description: "Upgrade Houdini Monitor from MVP to polished Phase-2 professional desktop monitor: visual polish with green accent; process sort/filter/search; richer resource detail (per-core CPU, per-disk volumes); system tray show/hide + always-on-top; Network AND GPU metrics with graceful fallback; sensible extras (uptime, keyboard shortcuts, preference persistence). No remote telemetry or auto UAC elevation. Refresh Desktop executable."

## Clarifications

### Session 2026-11-19

- Q: ¿Network y GPU están ambos en alcance? → A: Sí; ambos. GPU muestra N/D / no disponible si OSHI/hardware no reporta datos; nunca crashea.
- Q: ¿Acento visual? → A: Color secundario/acento verde para gráficos, selección, highlights y estado activo de sidebar; primary coherente con AtlantaFX.
- Q: ¿Preferencias persistentes? → A: Sí — tema, idioma y always-on-top (y tray-related defaults razonables) entre reinicios; sin telemetría remota.
- Q: ¿Comportamiento bandeja al cerrar ventana? → A: Cerrar (X) oculta a bandeja cuando la bandeja está disponible; salida explícita desde menú bandeja o acción Salir termina la app y el muestreo.
- Q: ¿Métricas de red? → A: Throughput agregado (bytes/s in/out) + lista de interfaces principales; historial ~60 s del agregado.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Recursos enriquecidos en vivo (Priority: P1)

Un usuario abre Houdini Monitor y ve CPU, RAM, Disco, Red y GPU (si disponible) en la barra lateral con valores actuales ~cada segundo. Al seleccionar CPU ve uso por núcleo; al seleccionar Disco ve desglose por volumen/unidad; al seleccionar Red ve throughput e interfaces; al seleccionar GPU ve métricas disponibles o un estado claro de no disponible.

**Why this priority**: Diferencia Phase 2 del MVP y aporta el valor de monitor profesional.

**Independent Test**: Arrancar la app y verificar sidebar con CPU/RAM/Disco/Red/GPU, detalle per-core, per-volume, red y GPU/N-D sin bloqueo de UI.

**Acceptance Scenarios**:

1. **Given** la aplicación arranca en Windows, **When** el usuario mira la barra lateral, **Then** ve entradas para CPU, RAM, Disco, Red y GPU con valores actuales o “N/D” donde aplique.
2. **Given** el usuario selecciona CPU, **When** mira el detalle, **Then** ve uso agregado, historial ~60 s y desglose por núcleo lógico.
3. **Given** el usuario selecciona Disco, **When** mira el detalle, **Then** ve % agregado, historial ~60 s y lista por volumen/unidad.
4. **Given** el usuario selecciona Red, **When** mira el detalle, **Then** ve throughput agregado (entrada/salida), historial ~60 s y interfaces principales.
5. **Given** GPU no es reportable, **When** el usuario selecciona GPU, **Then** ve un mensaje claro de no disponible y la app sigue estable.
6. **Given** la app está en ejecución, **When** pasa ~1 s, **Then** las métricas se actualizan sin bloquear la interfaz.

---

### User Story 2 - Procesos con búsqueda, filtro y ordenación (Priority: P1)

Un usuario abre Procesos, busca por nombre/PID/usuario, filtra la lista y ordena columnas (Nombre, PID, CPU %, RAM, Usuario) con UX clara. El kill con confirmación y advertencia de sistema del MVP se conserva.

**Why this priority**: Esencial para uso tipo Task Manager en demos y trabajo real.

**Independent Test**: Buscar un proceso conocido, ordenar por RAM, filtrar, y verificar que kill/confirmación/permisos siguen igual.

**Acceptance Scenarios**:

1. **Given** hay procesos en ejecución, **When** el usuario escribe en el campo de búsqueda, **Then** la tabla muestra solo coincidencias por nombre, PID o usuario (case-insensitive).
2. **Given** la tabla de procesos es visible, **When** el usuario hace clic en un encabezado de columna, **Then** la lista se ordena por esa columna (asc/desc) con indicador visual.
3. **Given** el usuario solicita terminar un proceso, **When** confirma o cancela, **Then** el comportamiento de confirmación, advertencia de sistema y fallo por permisos del MVP se mantiene.
4. **Given** la búsqueda no tiene coincidencias, **When** la tabla está vacía por filtro, **Then** se muestra un estado vacío claro (no error).

---

### User Story 3 - Pulido visual profesional (Priority: P2)

Un usuario percibe una UI más densa y profesional (estilo Task Manager), con acento verde coherente en gráficos, selección, highlights y sidebar activa, en temas Claro y Oscuro.

**Why this priority**: Calidad de portfolio y diferenciación visual sin cambiar el modelo de navegación.

**Independent Test**: Cambiar tema Claro/Oscuro y verificar acento verde en sidebar activa, selección y series de gráficos.

**Acceptance Scenarios**:

1. **Given** tema Claro u Oscuro, **When** el usuario selecciona un recurso en la sidebar, **Then** el estado activo usa el acento verde del sistema de diseño.
2. **Given** un recurso con historial, **When** el gráfico se renderiza, **Then** las series usan el acento verde de forma coherente.
3. **Given** la ventana principal, **When** el usuario inspecciona layout, **Then** el espacio se usa de forma más densa y profesional que el MVP, sin perder sidebar + detalle.

---

### User Story 4 - Bandeja, siempre encima y preferencias (Priority: P2)

Un usuario puede fijar la ventana siempre encima, minimizar/ocultar a la bandeja del sistema y restaurar desde el icono. Tema, idioma y always-on-top se restauran al reiniciar. También ve uptime del sistema (y/o de la app) y puede enfocar búsqueda con atajo de teclado.

**Why this priority**: Integración de escritorio esperada en un monitor profesional.

**Independent Test**: Activar always-on-top; ocultar a bandeja y restaurar; reiniciar y comprobar preferencias; Ctrl+F (o equivalente) enfoca búsqueda en procesos.

**Acceptance Scenarios**:

1. **Given** la app está abierta, **When** el usuario activa “Siempre encima”, **Then** la ventana permanece por encima de otras ventanas no-topmost.
2. **Given** la bandeja del sistema está disponible, **When** el usuario cierra la ventana con X, **Then** la app se oculta a la bandeja en lugar de terminar.
3. **Given** la app está en bandeja, **When** el usuario elige Mostrar / doble clic en el icono, **Then** la ventana principal se restaura.
4. **Given** la app está en bandeja, **When** el usuario elige Salir, **Then** el muestreo se detiene de forma ordenada y el proceso termina.
5. **Given** el usuario cambió tema, idioma y always-on-top, **When** reinicia la app, **Then** esas preferencias se restauran.
6. **Given** la vista de procesos está visible, **When** el usuario usa el atajo de enfoque de búsqueda, **Then** el campo de búsqueda recibe el foco.
7. **Given** la app está en ejecución, **When** el usuario mira el chrome de la UI, **Then** ve uptime del sistema (y opcionalmente uptime de la sesión de la app).

---

### User Story 5 - Entrega Windows actualizada (Priority: P2)

Un evaluador ejecuta tests, construye el entregable jpackage y usa el ejecutable en el Escritorio actualizado.

**Why this priority**: Criterio de done del brief Phase 2.

**Independent Test**: `gradlew :app:test` verde; jpackage produce `HoudiniMonitor.exe`; carpeta copiada al Escritorio; acceso directo actualizado; `:app:run` arranca.

**Acceptance Scenarios**:

1. **Given** el entorno de build documentado, **When** se ejecutan los tests unitarios, **Then** pasan en verde.
2. **Given** JDK con jpackage, **When** se genera el app-image, **Then** existe un `.exe` usable con runtime embebido.
3. **Given** la carpeta jpackage completa, **When** se copia al Escritorio sobrescribiendo, **Then** el acceso directo apunta a un entregable completo y funcional.

---

### Edge Cases

- GPU no disponible o API vacía → mostrar N/D / mensaje localizado; no crash.
- Red sin interfaces o contadores a cero → estado vacío o ceros coherentes; app estable.
- Bandeja no disponible (política SO / headless) → cerrar X termina la app con shutdown limpio; no crash.
- Proceso desaparece entre selección y kill → mensaje claro (comportamiento MVP).
- Preferencias corruptas o ausentes → defaults seguros (ES, tema sistema, always-on-top off).
- Primer segundo sin historial / sin deltas de red o CPU → gráficos con puntos disponibles; throughput puede ser 0 hasta el segundo sample.
- Búsqueda con caracteres especiales → no falla; puede devolver cero resultados.
- Kill de proceso propio / sistema → misma política MVP (confirmación + warning; sin UAC auto).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: CUANDO la aplicación está en ejecución, EL SISTEMA MUST muestrear CPU, RAM, Disco, Red y GPU (si disponible) ~cada 1 segundo off UI thread sin bloquear la interfaz.
- **FR-002**: CUANDO el usuario selecciona CPU, EL SISTEMA MUST mostrar uso agregado, historial ~60 s y uso por núcleo lógico.
- **FR-003**: CUANDO el usuario selecciona Disco, EL SISTEMA MUST mostrar % usado agregado de volúmenes fijos locales, historial ~60 s y desglose por volumen/unidad.
- **FR-004**: CUANDO el usuario selecciona Red, EL SISTEMA MUST mostrar throughput agregado (entrada y salida), historial ~60 s del agregado, y una lista de interfaces principales.
- **FR-005**: CUANDO el usuario selecciona GPU, EL SISTEMA MUST mostrar métricas disponibles (p. ej. uso % y/o memoria si el proveedor las reporta) O un estado localizado de no disponible; MUST NOT terminar de forma anómala.
- **FR-006**: EL SISTEMA MUST conservar el layout sidebar + panel de detalle y ampliar la sidebar con Red y GPU.
- **FR-007**: EL SISTEMA MUST permitir buscar procesos por nombre, PID o usuario (case-insensitive) y mostrar estado vacío claro sin resultados.
- **FR-008**: EL SISTEMA MUST permitir ordenar la tabla de procesos por Nombre, PID, CPU %, RAM y Usuario con indicador de dirección.
- **FR-009**: EL SISTEMA MUST conservar kill con confirmación siempre, advertencia extra para procesos de sistema, mensaje claro de permisos, y MUST NOT solicitar elevación UAC automática.
- **FR-010**: EL SISTEMA MUST aplicar un sistema de acento verde coherente (gráficos, selección, highlights, sidebar activa) en temas Claro y Oscuro, sobre la base visual AtlantaFX.
- **FR-011**: EL SISTEMA MUST ofrecer toggle “Siempre encima” que fija/desfija la ventana principal.
- **FR-012**: SI la bandeja del sistema está disponible, ENTONCES CUANDO el usuario cierra con X, EL SISTEMA MUST ocultar a bandeja; MUST ofrecer Mostrar/Ocultar y Salir desde el menú de bandeja.
- **FR-013**: CUANDO el usuario elige Salir (bandeja o acción explícita), EL SISTEMA MUST detener el muestreo de forma ordenada y terminar el proceso.
- **FR-014**: EL SISTEMA MUST persistir localmente tema, idioma y always-on-top entre reinicios (defaults seguros si faltan datos).
- **FR-015**: EL SISTEMA MUST mostrar uptime del sistema en la UI; MAY mostrar uptime de la sesión de la aplicación.
- **FR-016**: EL SISTEMA MUST ofrecer atajo de teclado para enfocar el campo de búsqueda de procesos cuando la vista de procesos está activa.
- **FR-017**: EL SISTEMA MUST mantener localización ES+EN de cadenas nuevas y arrancar con preferencia de idioma persistida (default ES si no hay preferencia).
- **FR-018**: EL SISTEMA MUST poder entregarse como app-image Windows (`.exe` + runtime) y documentar ejecución/construcción; el entregable de Escritorio MUST actualizarse como parte de la entrega Phase 2.
- **FR-019**: EL SISTEMA MUST NOT enviar telemetría remota.

### Key Entities

- **SystemMetricsSnapshot**: Instantánea inmutable de CPU (agregado + per-core), RAM, Disco (agregado + volúmenes), Red (agregado + interfaces), GPU (opcional/disponible), procesos, y marcas de tiempo relevantes (p. ej. boot time / uptime).
- **NetworkInterfaceSnapshot**: Nombre/etiqueta, bytes recibidos/enviados (o tasas), estado básico.
- **GpuSnapshot**: Disponibilidad, nombre opcional, uso % opcional, memoria usada/total opcional.
- **CpuCoreSnapshot**: Índice de núcleo y % de uso.
- **VolumeSnapshot**: Etiqueta/mount, % usado, usado/total.
- **ProcessViewState**: Texto de búsqueda, columna/dirección de orden, lista filtrada derivada.
- **UserPreferences**: Tema, locale, always-on-top (persistidos localmente).

### Out of Scope

- Alertas / notificaciones push
- Persistencia de historial de métricas en disco
- Telemetría remota
- Elevación UAC automática
- Instalador MSI (app-image jpackage sigue siendo el entregable)
- Gestión avanzada de red (conexiones TCP por proceso, firewall)
- Overclock / control de ventiladores / paneles vendor-specific GPU

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Un usuario identifica CPU, RAM, Disco, Red y GPU (o N/D) en la sidebar en menos de 10 segundos tras el arranque.
- **SC-002**: Un usuario localiza un proceso conocido vía búsqueda en menos de 5 segundos y puede ordenar por al menos dos columnas distintas.
- **SC-003**: En máquinas sin GPU reportable, la app permanece usable y muestra estado no disponible sin cerrarse.
- **SC-004**: After enable always-on-top and hide-to-tray, restore from tray returns a usable main window; Exit ends the process cleanly (no orphan sampler).
- **SC-005**: Tras reiniciar, tema, idioma y always-on-top coinciden con la sesión anterior en ≥95% de arranques con preferencias válidas.
- **SC-006**: Tests unitarios de lógica pura nueva (agregación red/disco, filtros/orden de procesos, parseo de preferencias, fallback GPU) pasan en verde antes de marcar la feature completa.
- **SC-007**: El entregable Desktop actualizado arranca y muestra la UI Phase 2 sin pasos manuales extra más allá de abrir el `.exe` / acceso directo.

## Assumptions

- El MVP (001) permanece como base; Phase 2 extiende dominio, UI y empaquetado sin romper contratos de kill/confirmación.
- OSHI es suficiente para red y, donde el SO lo permita, información GPU limitada; si no hay datos, UX de fallback.
- AWT SystemTray + JavaFX stage es aceptable en Windows para la bandeja.
- Preferencias locales (archivo o Preferences API del SO) bastan; no hay cuenta de usuario ni sync en la nube.
- Uptime del sistema se deriva del boot time reportado por el proveedor de métricas.
- Atajo de búsqueda: Ctrl+F (Windows estándar).
- Idioma de docs de producto sigue en español; identificadores de código en inglés.
