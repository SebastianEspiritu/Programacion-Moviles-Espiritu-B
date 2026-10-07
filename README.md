## VI. Preguntas de reflexión

* **¿Por qué `Producto.kt` y `MainActivity.kt` se entregaron completos, y las pantallas no? ¿Qué tienen en común los archivos que sí se dejaron como esqueleto?**
  * `Producto.kt` (modelo de datos y *mock data*) y `MainActivity.kt` (contenedor principal con `setContent` y la configuración del tema) representan la infraestructura de soporte y la arquitectura base del proyecto. Se entregaron completos porque no formaban parte de los objetivos de evaluación.
  * Los archivos entregados como esqueleto (`InicioScreen`, `CarritoScreen`, etc.) tienen en común ser **componentes UI composables (`@Composable`)**. Se dejaron vacíos o parciales para que el estudiante desarrolle la maquetación visual, el manejo de estados locales (`remember`, `mutableStateOf`), las listas dinámicas y la interactividad del usuario en Jetpack Compose.

* **¿Cómo lograste que el filtro de categoría (`LazyRow`) y el cálculo del carrito reaccionen automáticamente sin que tú "actualices" nada a mano?**
  * Se logró mediante el paradigma declarativo y el sistema de **Estado y Recomposición** de Jetpack Compose.
  * Al declarar variables de estado con `remember { mutableStateOf(...) }` (como `categoriaSeleccionada` o `cantidadCarrito`), Compose rastrea automáticamente qué funciones composables leen dichos valores. Cuando el valor cambia, Compose detecta la modificación y dispara la **recomposición** únicamente de las partes de la UI afectadas, recalculando las listas o textos sin necesidad de manipular manualmente la vista ni usar adaptadores.

* **¿Qué diferencia notaste entre `navigate()` normal (`Inicio` → `Detalle`) y el que usa `popUpTo` (`Datos de entrega` → `Confirmación`)?**
  * **`navigate()` normal:** Agrega la nueva pantalla en la parte superior de la pila de navegación (*backstack*). Al presionar el botón físico o de software "Atrás", el usuario regresa exactamente a la pantalla anterior (`Detalle` → `Inicio`).
  * **`navigate()` con `popUpTo` (e `inclusive = true`):** Remueve de la pila de navegación las pantallas intermedias especificadas (como `Carrito` y `DatosDeEntrega`). Esto evita que si el usuario presiona "Atrás" desde la pantalla de `Confirmación`, regrese al formulario de pago o al carrito ya procesado, canalizando la navegación de vuelta al inicio.

* **¿Qué tuviste que corregir del código que te generó la IA para el buscador en tiempo real?**
  * **Integración de filtros (AND lógico):** Se corrigió la condición de filtrado para garantizar que la búsqueda por texto no sobrescribiera el filtro de categoría activa, combinando ambos criterios mediante la expresión `coincideCategoria && coincideBusqueda`.
  * **Estructura dentro de `LazyColumn`:** Se colocó el `OutlinedTextField` dentro de un bloque `item { }` dentro del contenedor principal para evitar conflictos de scroll.
  * **Detalles de UX:** Se añadió el icono de borrado rápido (`Clear`) en el `trailingIcon` del campo de texto y un bloque condicional `if (productosFiltrados.isEmpty())` para notificar al usuario cuando no existan coincidencias.

* **Compara el `NavigationDrawer` del Laboratorio 6 con el `NavigationBar` de esta tarea: ¿en qué caso usarías cada uno en un proyecto propio?**
  * **`NavigationBar` (Barra inferior):** Lo utilizaría para los **destinos principales de acceso frecuente** (entre 3 y 5 secciones como *Inicio*, *Buscar*, *Pedidos*, *Perfil*). Es ideal en entornos móviles porque permite una navegación cómoda y rápida con una sola mano.
  * **`NavigationDrawer` (Menú lateral):** Lo utilizaría en aplicaciones complejas que contengan **más de 5 secciones**, o para opciones secundarias de configuración, soporte, políticas de privacidad y cambio de cuenta. También es muy útil en adaptaciones para tablets o pantallas de gran formato.

---

## VII. Observaciones y conclusiones

### Observaciones
1. **Manejo de State Hoisting:** Durante el desarrollo surgió la duda sobre dónde debía residir el estado del carrito de compras. Se observó que al elevar el estado (*State Hoisting*) hacia el componente de navegación principal, las distintas pantallas pudieron compartir el contador de productos del carrito de forma consistente.
2. **Anidamiento de elementos desplazables:** Al integrar el buscador (`OutlinedTextField`) y el listado de categorías (`LazyRow`) dentro de la pantalla principal, fue indispensable envolverlos correctamente en bloques `item { ... }` de la `LazyColumn` principal para mantener la fluidez del desplazamiento y evitar errores de maquetación.

### Conclusiones
1. **Eficiencia de trabajar con esqueletos:** Desarrollar a partir de una estructura predefinida permite concentrar el aprendizaje y el esfuerzo en la resolución de la lógica de negocio y los componentes interactivos de la UI, evitando perder tiempo en la configuración repetitiva de carpetas, Gradle o modelos de datos base.
2. **Evolución del desarrollo (Fase 1 vs. Fase 2):** Mientras que la Fase 1 estuvo orientada a la maquetación estática y navegación entre vistas, la Fase 2 demostró el verdadero potencial reactivo de Jetpack Compose. La implementación del buscador combinado en tiempo real requirió pocas líneas de código lógico gracias a que la UI responde automáticamente a los cambios de estado.
