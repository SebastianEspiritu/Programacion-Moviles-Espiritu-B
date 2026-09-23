# Documentación de Prompts - Fase 2 (Mejora con IA)

**Proyecto:** Clínica Salud+  
**Rama Git:** `con-ia`  
**Alumno:** Sebastián Espíritu  
**Curso:** Desarrollo de Aplicaciones Móviles  

---

## 1. Descripción de la Funcionalidad Implementada

Se implementó el flujo completo para la **cancelación de citas médicas** dentro de la aplicación Jetpack Compose sin hacer uso de arquitectura MVVM (usando estado hoisteado con `mutableStateListOf`):

1. **Modelo de Datos (`Cita.kt`):** Se extendió el `enum class EstadoCita` agregando el estado `CANCELADA`.
2. **Interfaz de Usuario (`MisCitasScreen.kt`):** 
   - Muestra las citas agendadas.
   - Si la cita está en estado `CONFIRMADA`, se habilita el botón "Cancelar Cita".
   - Al presionar el botón, se despliega un `AlertDialog` de confirmación con diseño Material 3.
   - Las citas canceladas cambian visualmente su etiqueta/badge a color rojo (`#C62828`).
3. **Navegación (`AppNavigation.kt`):** Se ajustó la lambda `onCancelarCita` para buscar el índice de la cita y actualizar su estado a `CANCELADA` mediante la función `.copy()`.

---

## 2. Historial de Prompts e Iteraciones con IA

### Prompt 1: Generación del diálogo y lógica de cancelación
* **Objetivo:** Obtener la estructura de la pantalla `MisCitasScreen` con la tarjeta de cita y el diálogo de confirmación.
* **Prompt enviado:**
  > "Necesito implementar la opción de cancelar citas en MisCitasScreen usando Jetpack Compose y Material 3. Cada tarjeta de cita confirmada debe mostrar un botón para cancelar. Al hacer clic, debe abrirse un AlertDialog preguntando si está seguro. Si confirma, la cita no debe eliminarse de la lista, sino cambiar su estado a CANCELADA. El paquete del proyecto es `com.espiritu.clinicasalud`."

* **Respuesta de la IA:**  
  Generó el código base de `MisCitasScreen` utilizando `AlertDialog`, `LazyColumn` y variaciones de color según el estado de la cita.

* **Ajustes y correcciones aplicados:**  
  Se adaptaron los nombres de las propiedades (`cita.medico.nombre`, `cita.fecha`, `cita.hora`) ya que la propuesta inicial utilizaba un modelo genérico con campos como `doctorName` o `date`.

---

### Prompt 2: Manejo de estado en `AppNavigation.kt` y `EstadoCita`
* **Objetivo:** Resolver el cambio de estado dentro de la lista mutable compartida y el enum.
* **Prompt enviado:**
  > "¿Cómo debo agregar el estado CANCELADA en el enum EstadoCita y cómo actualizo ese elemento dentro del `mutableStateListOf<Cita>` en AppNavigation.kt al ejecutar la cancelación?"

* **Respuesta de la IA:**  
  Explicó cómo agregar `CANCELADA` en `EstadoCita` y la lógica para reemplazar el elemento en la lista usando `indexOf` y `copy(estado = EstadoCita.CANCELADA)`.

* **Ajustes y correcciones aplicados:**  
  Se corrigió un error de compilación importando explícitamente `com.espiritu.clinicasalud.model.EstadoCita` en `AppNavigation.kt`.

---

## 3. Comandos Git Utilizados

```powershell
# Commit 1: Cambios en el modelo de datos
git add app/src/main/java/com/espiritu/clinicasalud/model/Cita.kt
git commit -m "agregar estado CANCELADA en enum EstadoCita"

# Commit 2: Interfaz de usuario y navegación
git add app/src/main/java/com/espiritu/clinicasalud/screens/MisCitasScreen.kt app/src/main/java/com/espiritu/clinicasalud/navigation/AppNavigation.kt
git commit -m "integrar AlertDialog y logica de cancelacion en MisCitasScreen"

# Commit 3: Documentación de prompts
git add PROMPTS.md
git commit -m "agregar archivo PROMPTS.md para documentar los prompts de la Fase 2"

# Subir cambios al repositorio remoto
git push origin con-ia
