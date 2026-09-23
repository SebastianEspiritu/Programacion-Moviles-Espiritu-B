# Registro Detallado de Prompts - Fase 2 (Mejora con IA)

**Curso:** Programación en Móviles  
**Proyecto:** TECSUP Fit (Opción B)  
**Estudiante:** Sebastián Espíritu  
**Rama de Git:** `con-ia`  

---

## Introducción
En esta segunda fase se utilizó asistencia de Inteligencia Artificial para enriquecer la funcionalidad de la aplicación "TECSUP Fit", enfocándose principalmente en la gestión de estado con Jetpack Compose (sin ViewModel), la incorporación de un flujo interactivo de cancelación de reservas mediante cuadros de diálogo (`AlertDialog`) y la depuración de temas de UI.

---

## 1. Historial de Commits

### Fase 1 (Desarrollo Base)
* `proyecto inicial de TECSUP FIT`
* `cree package model y class Routine`
* `implementa HomeScreen ui ocn workout cards`
* `crear FitrnessClass y Resservation models`
* `implementar HomeScreen con filter chips y class list y añadi dependencia icons`
* `añadir DetailScreen and ConfirmationScreens`
* `agregar pantallas de mis reservas y perfil de usuario`
* `implementa contenedor principal con navegacion por barra inferior`
* `corregir referencias de colores en Theme.kt`

### Fase 2 (Mejora con IA)
* `feat: agregar boton y dialogo AlertDialog para cancelar reservas`
* `style: ajustar navegacion de barra inferior para integracion con reservas`
* `docs: agregar archivo PROMPTS.md con el registro de interacciones con IA`

---

## Prompt 1: Implementación de la cancelación de reserva con AlertDialog

### 1.1. Consulta enviada a la IA (Prompt)
> *"Estoy construyendo una aplicación en Android Studio usando Jetpack Compose y Material3 para un gimnasio llamado TECSUP Fit. No estoy utilizando ViewModel ni arquitectura MVVM, todo el estado se maneja en la vista con `remember` y `mutableStateOf`.*
> 
> *Necesito agregar una mejora funcional a la pantalla de 'Mis Reservas': quiero que cada tarjeta de reserva confirmada tenga un ícono de tacho de basura (Delete) y que, al presionarlo, muestre un cuadro de diálogo de confirmación (`AlertDialog`). El diálogo debe preguntar '¿Estás seguro de que deseas cancelar la reserva de [Nombre de la Clase]?' con botones para 'Sí, cancelar' y 'Volver'. Si confirma, la reserva debe eliminarse de la lista visible."*

### 1.2. Respuesta obtenida de la IA
La IA proporcionó la estructura de un componente `ReservationsScreen` declarando el estado de la lista y el estado del elemento a eliminar:
```kotlin
var reservationsList by remember { mutableStateOf(initialReservations) }
var reservationToCancel by remember { mutableStateOf<Reservation?>(null) }
```

Prompt 2: Manejo de Navegación Secundaria y Visibilidad del BottomBar
2.1. Consulta enviada a la IA (Prompt)
"Tengo un Scaffold en Compose con un NavigationBar (bottomBar) que tiene 4 pestañas: Inicio, Reservas, Rutinas y Perfil. Sin embargo, cuando el usuario selecciona una clase para ver sus detalles o pasa a la pantalla de confirmación, la barra inferior no debería estorbar.

¿Cómo puedo estructurar la navegación condicional dentro de MainScreen usando únicamente variables de estado remember para ocultar la barra inferior durante el flujo secuencial (Detalle -> Confirmación) y volver a mostrarla en las pantallas principales?"
