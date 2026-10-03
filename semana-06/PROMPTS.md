# Registro de Prompts de IA - Fase 2 (Mejora con IA)
Este documento registra la secuencia de prompts utilizados para interactuar con la IA durante el desarrollo de la Fase 2 del Laboratorio en la rama `mejora-ia`.

---

## Prompt 1: Creación de la rama y análisis de arquitectura
> **Prompt enviado a la IA:**  
> "Actúa como un desarrollador Senior Android en Kotlin y Jetpack Compose. Necesitamos iniciar la Fase 2 de nuestro proyecto 'TecsupStore'. Indícame los comandos de Git necesarios para situarme en la rama `main` y crear una nueva rama de trabajo llamada `mejora-ia`. Asimismo, analiza la estructura actual de `TarjetaProducto`, `AppDrawer` y `MainActivity` para preparar la integración reactiva de un contador de productos favoritos."

**Resultado / Impacto:**
- Se creó la rama de trabajo `mejora-ia` de manera limpia desde `main`.
- Se definió el flujo de datos necesario para comunicar las acciones del `DropdownMenu` con el `Badge` del `AppDrawer`.

---

## Prompt 2: Refactorización y sincronización de estado (DropdownMenu ↔ Drawer)
> **Prompt enviado a la IA:**  
> "En el proyecto Jetpack Compose, necesitamos conectar el DropdownMenu de cada producto con el Drawer de la aplicación. Implementa los siguientes requerimientos:  
> 1. Modifica `TarjetaProducto.kt` para soportar un callback `onToggleFavorito` y alternar dinámicamente el texto ('Agregar a Favoritos' / 'Quitar de Favoritos') y el ícono del corazón.  
> 2. En `MainActivity.kt`, gestiona el estado reactivo de favoritos usando un conjunto de IDs (`favoritosIds: Set<Int>`) para garantizar consistencia y evitar duplicados.  
> 3. Pasa el total de favoritos al parámetro `favoritosCount` de `AppDrawer.kt` para que el `Badge` del ítem 'Favoritos' se actualice en tiempo real.  
> 4. Habilita una vista filtrada para mostrar solo los favoritos cuando el usuario navegue a esa pantalla.  
> Entrégame el código completo y optimizado para `TarjetaProducto.kt` y `MainActivity.kt`."

**Resultado / Impacto:**
- Sincronización completa del estado entre componentes UI.
- Actualización dinámica e instantánea del `Badge` del Drawer al interactuar con el `DropdownMenu` de cada producto.
