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
