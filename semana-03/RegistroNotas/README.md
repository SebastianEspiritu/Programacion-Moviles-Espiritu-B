# Registro de Notas - Jetpack Compose

Aplicación móvil Android desarrollada en Kotlin y Jetpack Compose para el cálculo del promedio ponderado de notas del ciclo académico.

**Desarrollador:** Sebastian Espiritu

---

## Características
* **Control interactivo:** Sliders de 0 a 20 con actualización en tiempo real (*Recomposición*).
* **Confirmación de datos:** Botón habilitado dinámicamente mediante `Checkbox`.
* **Cálculo ponderado:** Ponderación exacta por curso (20%, 25%, 30%, 25%).
* **Redondeo opcional:** `Switch` interactivo que aplica `roundToInt()`.
* **Semáforo visual en notas:** Badges dinámicos (Rojo si nota < 13, Verde si nota >= 13).
* **Desglose de aportes:** Visualización del puntaje individual aportado por materia al promedio total.
* **Botón Limpiar:** Reinicio completo de estados y Sliders.
* **Chip de estado:** Clasificación según rango de nota (EXCELENTE, APROBADO, EN RECUPERACIÓN, DESAPROBADO).

---

## Casos de Prueba y Demostración

### 1. Estado Inicial
Pantalla en estado base con Sliders en 0 y botón de cálculo deshabilitado.

<img width="378" height="832" alt="cap vacía" src="https://github.com/user-attachments/assets/08b1bfe4-5db3-41ba-b33b-5f273d19382a" />


### 2. Ingreso y Selección de Notas
Asignación de notas con Sliders y activación de casilla de verificación.

<img width="384" height="836" alt="cap rellenado" src="https://github.com/user-attachments/assets/8c160a5a-33c9-4237-9c1c-198b5a5b0c1b" />


### 3. Resultado Calculado
Tarjeta con desglose de aportes, promedio ponderado final y chip de observación.

<img width="380" height="571" alt="cap ejecutado" src="https://github.com/user-attachments/assets/2b52c06d-544f-468d-890a-b634d83a4514" />

---

## Tecnologías Utilizadas
* **Lenguaje:** Kotlin
* **UI Framework:** Jetpack Compose (Material 3)
* **Control de Estado:** `remember`, `mutableStateOf`, `mutableFloatStateOf`
* **VCS:** Git & GitHub
