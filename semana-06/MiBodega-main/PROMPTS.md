# Bitácora de Prompts e Ingeniería de IA (PROMPTS.md)

**Proyecto:** Mi Bodega - Aplicación Android con Jetpack Compose  
**Asignatura / Laboratorio:** Desarrollo de Aplicaciones Móviles  
**Rama de trabajo:** `con-ia`  

---

## Prompts de la Fase 2: Buscador en Tiempo Real y Filtro Combinado

### Commit #1: Estado y Componente Visual del Buscador

> **Prompt:**
> ```text
> Actúa como un desarrollador Senior en Android con Jetpack Compose y Kotlin. Tengo un proyecto de bodega llamado 'Mi Bodega' y quiero que me ayudes a implementar la mejora de búsqueda de productos en la Pantalla 3 (InicioScreen.kt).
> 
> Para el primer commit, solo agrega el estado del texto de búsqueda (var textoBusqueda by remember { mutableStateOf("") }) y la interfaz del OutlinedTextField dentro de la LazyColumn.
> 
> Usa el icono Search como leadingIcon, bordes redondeados de 12.dp y los colores del tema (GrisClaro y VerdeBodega). Todavía no apliques el filtrado combinado ni el botón de limpiar, solo déjalo listo visualmente para el primer commit.
> ```

---

### Commit #2: Lógica de Doble Filtrado en Tiempo Real

> **Prompt:**
> ```text
> Actúa como un desarrollador Senior en Android con Jetpack Compose. Continuando con mi proyecto de bodega, ahora necesito el segundo commit para InicioScreen.kt.
> 
> Quiero que implementes la lógica para que el campo de búsqueda filtre la lista de productos en tiempo real a medida que el usuario escribe, combinándose correctamente con el filtro de categoría ya existente. Ambos filtros deben funcionar juntos usando un AND lógico (coincideCategoria && coincideBusqueda), sin reemplazarse.
> 
> Pasa la lista filtrada a los items de la LazyColumn para que la UI se actualice automáticamente al escribir.
> ```

---

### Commit #3: Botón de Limpiar y Estado Lista Vacía

> **Prompt:**
> ```text
> Actúa como un desarrollador Senior en Android con Jetpack Compose. Para el tercer y último commit de la mejora en mi proyecto de bodega en InicioScreen.kt, quiero mejorar la experiencia de usuario.
> 
> Agrega un trailingIcon en el OutlinedTextField para que, si el campo no está vacío, aparezca un botón con el icono Clear que permita borrar el texto al hacer clic. Además, si la lista de productos filtrados está vacía, muestra un mensaje descriptivo indicando que no se encontraron productos para ese texto.
> ```

---

## Prompts para Preguntas de Reflexión y Análisis Técnico

> **Prompt:**
> ```text
> Actúa como un tutor experto en Android y Jetpack Compose. Tengo un proyecto de bodega y necesito responder estas 5 preguntas de reflexión técnica para mi informe:
> 
> 1. ¿Por qué Producto.kt y MainActivity.kt se entregaron completos, y las pantallas no? ¿Qué tienen en común los archivos que sí se dejaron como esqueleto?
> 2. ¿Cómo lograste que el filtro de categoría (LazyRow) y el cálculo del carrito reaccionen automáticamente sin que tú "actualices" nada a mano?
> 3. ¿Qué diferencia notaste entre navigate() normal (Inicio -> Detalle) y el que usa popUpTo (Datos de entrega -> Confirmación)?
> 4. ¿Qué tuviste que corregir del código que te generó la IA para el buscador en tiempo real?
> 5. Compara el NavigationDrawer del Laboratorio 6 con el NavigationBar de esta tarea: ¿en qué caso usarías cada uno en un proyecto propio?
> 
> Respóndelas de forma concisa, clara y técnica.
> ```

---

## Historial de Commits en Git

| Commit | Mensaje en Git |
| :--- | :--- |
| **1** | `feat(fase-2): agregar estado textoBusqueda y UI del campo de busqueda` |
| **2** | `feat(fase-2): implementar filtrado en tiempo real combinado por categoria y texto` |
| **3** | `feat(fase-2): agregar boton para limpiar texto y mensaje para busqueda vacia` |
