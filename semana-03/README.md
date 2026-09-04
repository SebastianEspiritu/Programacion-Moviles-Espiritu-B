# Lab 03: Registro de Producto
**Estudiante:** Sebastián Espíritu Canteño
**Curso:** Desarrollo de Aplicaciones Móviles  
**Tecnologías:** Android, Kotlin, Jetpack Compose, Material 3

## Descripción:
Aplicación móvil desarrollada en Android Studio para el registro interactivo de productos. La interfaz implementa componentes de Material Design 3, gestión de estado en Compose (`remember` y `mutableStateOf`), validación mediante conversión segura de datos (`toDoubleOrNull` / `toIntOrNull`) y cálculo automático del importe total en una tarjeta de resumen.

---
## 1ra Captura de Pantalla vacía:
<img width="352" height="729" alt="image" src="https://github.com/user-attachments/assets/b2bf4993-1e79-4faa-9c52-3c447b6bce82" />

## 2da Captura de Pantalla rellenada:
<img width="348" height="708" alt="image" src="https://github.com/user-attachments/assets/0e33e03d-5e6c-497c-b75f-291ccf9eadc5" />

### ¿Qué pasaría si declaras las variables de los campos SIN `remember`?
Si se declaran las variables con `mutableStateOf("")` pero sin `remember`, cada vez que Jetpack Compose realice una recomposición (redibuje la pantalla al detectar un cambio de estado), las variables se reiniciarán a su valor inicial (vacío `""` o `false`). En la práctica, el usuario teclearía en los campos de texto pero las letras desaparecerían inmediatamente. `remember` permite conservar el valor a través de las recomposiciones del ciclo de vida del composable.

## Mejora con IA

En esta rama (`con-ia`) se usó Gemini para agregar validación de campos vacíos y un botón para limpiar el formulario, siguiendo la Parte B de la guía del Lab03.

### Prompt que usé

> Tengo esta pantalla de Jetpack Compose (código de PantallaRegistro adjunto). Necesito que agregues una mejora específica, sin tocar el resto de la estructura ni los estilos existentes:
> 1. Cuando se presione el botón AGREGAR PRODUCTO, si nombre, precio o cantidad están vacíos, en vez de mostrar la Card de resumen, muestra un mensaje de error en color rojo.
> 2. Agrega un botón "Limpiar" que vacíe los 3 campos y oculte la Card de resumen.
> No cambies nada más de la pantalla.

### Tabla de decisiones

| Prompt que usé | Qué generó Gemini | Qué acepté o corregí (y por qué) |
|---|---|---|
| Ver prompt completo | Agregó el estado `mensajeError`, un `Text` en rojo (`MaterialTheme.colorScheme.error`) que se muestra cuando hay error, validación con `isBlank()` en los 3 campos dentro del `onClick` del botón AGREGAR, y un `OutlinedButton` "LIMPIAR FORMULARIO" que resetea nombre, precio, cantidad y el resumen. También agregó `mensajeError = ""` en cada `onValueChange`, sin que se lo pidiera explícitamente. | **Acepté**: la estructura general de la validación de vacíos y el botón Limpiar, tal como se pidió. También acepté el `mensajeError = ""` automático al escribir, porque mejora la experiencia de uso aunque no lo pedí. **Corregí**: la validación de Gemini solo revisaba con `isBlank()` si los campos tenían texto, pero no si ese texto era un número válido. Por ejemplo, escribir "abc" en Precio no está vacío, así que pasaba la validación, y luego `precio.toDoubleOrNull() ?: 0.0` lo convertía en 0.0 en silencio, mostrando una Card con datos falsos. Agregué una segunda validación con `toDoubleOrNull()`/`toIntOrNull()` para exigir que precio y cantidad sean números válidos mayores a cero, reutilizando el mismo patrón del Lab02. |

### Capturas de la mejora

**Captura 1 — Validación de campos vacíos:**

<img width="343" height="895" alt="Captura de pantalla 2026-09-03 203437" src="https://github.com/user-attachments/assets/92a6e8af-5cce-46ba-b572-53a59ef15ae1" />

**Captura 2 — Corrección: validación de números inválidos:**

<img width="335" height="440" alt="Captura de pantalla 2026-09-03 204941" src="https://github.com/user-attachments/assets/bfcfbeab-5638-435a-9c70-1b68fd52e7b2" />

