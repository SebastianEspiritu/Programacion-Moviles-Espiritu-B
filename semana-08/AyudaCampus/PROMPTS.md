# Registro de Prompts y Desarrollo con IA — AyudaCampus (Fase 2)

Este documento registra los prompts, decisiones de arquitectura, correcciones humanas y la reflexión sobre el uso de la Inteligencia Artificial durante la implementación de la **Fase 2 (Con IA)** del proyecto AyudaCampus en la rama `con-ia`.

---

## 1. Bitácora de Mejoras e Historial de Commits

### Mejora A: Tiempo Promedio de Resolución en el Panel de Soporte
* **Commit:** `25d949f` (`ia: agrega tiempo promedio de resolucion al panel`)
* **Prompt Principal:**
  > *"Implementar la mejora de tiempo promedio de resolución en el panel de soporte calculando fechaCierre - fecha usando tickets CERRADOS. Arquitectura obligatoria: TicketDao -> AyudaRepository -> AyudaRepositoryRoom -> PanelSoporteViewModel -> PanelSoporteScreen. Usar lenguaje visual pastel y formato legible (horas y minutos). Sin datos inventados cuando no hay tickets cerrados."*
* **Archivos Modificados:**
  * `app/src/main/java/com/espiritu/ayudacampus/data/local/dao/TicketDao.kt`
  * `app/src/main/java/com/espiritu/ayudacampus/data/repository/AyudaRepository.kt`
  * `app/src/main/java/com/espiritu/ayudacampus/data/repository/AyudaRepositoryRoom.kt`
  * `app/src/main/java/com/espiritu/ayudacampus/viewmodel/PanelSoporteViewModel.kt`
  * `app/src/main/java/com/espiritu/ayudacampus/ui/soporte/PanelSoporteScreen.kt`
  * `app/src/test/java/com/espiritu/ayudacampus/viewmodel/PanelSoporteViewModelTest.kt`
  * `app/src/test/java/com/espiritu/ayudacampus/viewmodel/SoporteRepositoryFake.kt`

---

### Mejora B: Contador de SLA por Prioridad
* **Commit:** `821672f` (`ia: agrega contador de SLA por prioridad`)
* **Prompt Principal:**
  > *"Implementar un contador de SLA por prioridad: ALTA (4h), MEDIA (24h) y BAJA (72h). Calcular tiempo transcurrido y restante desde fecha de creación. Mostrar tiempo restante si está en plazo o estado VENCIDO si supera el plazo. En tickets cerrados detener el contador usando fechaCierre. Usar ticker reactivo eficiente en UI."*
* **Archivos Creados y Modificados:**
  * `app/src/main/java/com/espiritu/ayudacampus/model/CalculadorSla.kt`
  * `app/src/main/java/com/espiritu/ayudacampus/ui/components/SlaChip.kt`
  * `app/src/main/java/com/espiritu/ayudacampus/ui/components/TicketCard.kt`
  * `app/src/main/java/com/espiritu/ayudacampus/ui/soporte/AtenderTicketScreen.kt`
  * `app/src/main/java/com/espiritu/ayudacampus/ui/soporte/BandejaScreen.kt`
  * `app/src/main/java/com/espiritu/ayudacampus/ui/cliente/DetalleTicketScreen.kt`
  * `app/src/test/java/com/espiritu/ayudacampus/model/CalculadorSlaTest.kt`

---

### Mejora C: Búsqueda Avanzada en la Bandeja de Soporte
* **Commit:** `c4ff402` (`ia: agrega busqueda avanzada en la bandeja`)
* **Prompt Principal:**
  > *"Permitir búsqueda combinada por texto, categoría y prioridad usando condición AND. Actualizar en tiempo real. Incluir botón de limpiar texto, chips de filtros y botón de restablecer todos los filtros. Mantener ordenamiento por prioridad y fecha, y pruebas unitarias completas."*
* **Archivos Modificados:**
  * `app/src/main/java/com/espiritu/ayudacampus/viewmodel/BandejaViewModel.kt`
  * `app/src/main/java/com/espiritu/ayudacampus/ui/soporte/BandejaScreen.kt`
  * `app/src/test/java/com/espiritu/ayudacampus/viewmodel/BandejaViewModelTest.kt`
  * `app/src/test/java/com/espiritu/ayudacampus/viewmodel/SoporteRepositoryFake.kt`

---

## 2. Decisiones de Arquitectura y Correcciones Humanas

1. **Revisión del Ticker SLA Reactivo:** Durante la implementación de la Mejora B, la IA inicialmente propuso reevaluar `System.currentTimeMillis()` dentro de cada Composable individual. Se corrigió para utilizar un único ticker compartido por pantalla mediante `produceState(System.currentTimeMillis()) { while(true) { delay(60_000L); value = ... } }`, actualizando toda la pantalla de forma limpia sin fugas de corrutinas ni temporizadores por tarjeta.
2. **Cumplimiento MVVM + Repository:** Se garantizó que la View (`Screen` / `Composable`) no acceda directamente a DAOs ni Repositorios. Todas las evaluaciones de filtrado combinadas (Mejora C) se ejecutan dentro de `BandejaViewModel`, y el cálculo de SLA reside puramente en la capa de modelo (`CalculadorSla.kt`).
3. **Respeto del Diseño Aprobado:** Se conservaron intactas las paletas pastel, bordes, contadores superiores, gráficos de dona/barras y banners de alerta de la Fase Base.
4. **Pruebas y Robustez:** Se mantuvieron las 161 pruebas unitarias anteriores y se añadieron 14 pruebas nuevas (alcanzando 175 pruebas unitarias exitosas), comprobando límites, estados sin datos, duraciones negativas y búsquedas combinadas.

---

## 3. Reflexión Personal sobre el uso de IA

La integración de la Inteligencia Artificial como asistente de desarrollo agilizó significativamente la escritura de código repetitivo y la estructuración de pruebas unitarias. Sin embargo, su eficacia dependió críticamente de la precisión de las instrucciones y de la supervisión humana constante para evitar sobrecomplicaciones técnicas. La IA tiende a sugerir soluciones genéricas que pueden violar restricciones arquitectónicas locales o introducir temporizadores ineficientes si no se acotan sus límites. En conclusión, la IA es un catalizador potente de productividad, pero la validación humana rigorosa sigue siendo indispensable para garantizar código limpio, óptimo y mantenible.

---

## 4. Estructura de Ramas

La guía del laboratorio menciona como denominación genérica la rama `mejora-ia`. En el presente repositorio la rama activa de desarrollo de la Fase 2 es **`con-ia`**, publicada y sincronizada con el remoto `origin/con-ia`.
