# PROMPTS.md · MuroCampus

Registro de los prompts importantes que usé con la IA, qué respondió, qué cambié o descarté y por qué.

**Herramienta:** asistente de IA (Claude). El docente permitió usar IA en todas las fases, así que también incluyo los problemas reales de la Fase 1.
**Mejoras de la Fase 2 (rama `con-ia`):** búsqueda por título, borrar publicación con `@DELETE` y `Response<Unit>`, y editar el título con `@PATCH`.
**Lo que revisé en cada respuesta:** la pantalla nunca llama a la API, el ViewModel no conoce Retrofit, `PostApi`, `HttpException` ni `Context`, el Repository no deja escapar excepciones (devuelve `Result`) y relanza `CancellationException`, nada de `GlobalScope` ni `catch` vacíos, y la URL sigue siendo `https://`.

## Fase 2 · Mejoras

| Prompt que escribí | Qué respondió la IA | Qué cambié o descarté | Por qué |
|---|---|---|---|
| «¿Cuáles mejoras hacemos?» (elegí entre las 6 de la guía) | Explicó qué capas cambia cada una y recordó la regla: mínimo 3 mejoras y al menos una debe cambiar el Repository o la interface de la API | Elegí **Borrar**, **Búsqueda** y **Editar con PATCH** | Borrar y Editar cambian las cuatro capas (API, Repository, ViewModel y View), así que cumplen la regla de sobra. La búsqueda es útil y solo toca ViewModel + View |
| «Agrega la búsqueda por título» | Agregó `busqueda: StateFlow<String>` y `listaVisible`, que junta `_lista` y el texto con `combine(...).stateIn(viewModelScope, ...)`. La pantalla recolecta `listaVisible` y tiene un `OutlinedTextField` con una ✕ para limpiar | Lo acepté. Revisé que `_lista` siga guardando la descarga completa y que el filtro se aplique encima | La búsqueda trabaja sobre las publicaciones **ya descargadas**: no hace otra llamada a la API en cada letra. También funciona junto con el chip de usuario (Usuario 2 + «et» = 4 resultados) |
| «Agrega el botón Eliminar con DELETE» | `@DELETE("posts/{id}") suspend fun borrar(...): Response<Unit>` en la API. En el Repository revisa `respuesta.isSuccessful` y, si no es 2xx, lanza `HttpException(respuesta)` dentro de `llamar`, que lo vuelve `Result.failure`. El ViewModel quita la publicación de la lista; la pantalla pide confirmación con un `AlertDialog` y vuelve a la lista con un `Snackbar` | Lo acepté y agregué el diálogo de confirmación antes de borrar | Con `Response<Unit>` Retrofit **no** lanza excepción por un 404 o 500: hay que revisar el código a mano, si no un error del servidor se tomaría como éxito. La publicación se quita de la lista localmente porque JSONPlaceholder no borra de verdad |
| (Error de la IA que se corrigió al revisar) El `Snackbar` se lanzaba dentro del mismo `LaunchedEffect(borrado)` que después limpiaba el aviso | Al revisar su propio código vio que limpiar `borrado` cambia la clave del `LaunchedEffect`, eso **cancela** su corrutina y el Snackbar desaparecía antes de verse. Lo movió a `rememberCoroutineScope()` | Acepté la corrección | El aviso «Publicación 1 eliminada» ahora se ve completo. Es un error que compila y no se nota leyendo rápido: solo se ve probando |
| «Agrega editar con PUT o PATCH» | Usó `@PATCH("posts/{id}")` con `@Body CambioTitulo(title)`, que manda solo el campo que cambia. El ViewModel reemplaza la publicación en la lista y en el detalle con lo que respondió el servidor; `PantallaEditar` muestra «Actualizado con PATCH (id N)» y la respuesta | Elegí PATCH en vez de PUT. El botón Guardar queda deshabilitado si el título está vacío **o no cambió** | PUT reemplaza la publicación completa (habría que mandar `userId`, `title` y `body`); PATCH modifica solo lo enviado. Deshabilitar cuando no hay cambios evita peticiones inútiles |
| «Pasa MuroCampus de `main` a mi rama `con-ia`» | Guardó con `git stash` los cambios de `.idea` que Android Studio escribe solo, cambió a `con-ia` y copió los 11 commits con `git cherry-pick main~11..main` | Lo acepté | `con-ia` tiene sus propias versiones de semanas anteriores; el cherry-pick copia solo `semana-09` con los mismos 11 mensajes, y luego van los 3 commits `ia:` |

## Fase 1 · Problemas reales y cómo los corregí

| Prompt que escribí | Qué respondió la IA | Qué cambié o descarté | Por qué |
|---|---|---|---|
| «Ayúdame a hacer este laboratorio, en el package tiene que ir mi apellido» | Armó la estructura de la guía con `com.espiritu.murocampus` en vez de `com.tecsup.murocampus`. Como no tenía el proyecto base, creó los archivos [E] (tema azul, `Componentes.kt`, `UiState.kt`) | Mantuve `com.espiritu` | Es la convención de mis laboratorios (`com.apellido.proyecto`) |
| «¿Estás respetando la estructura de la guía?» | Avisó de una contradicción: el árbol de la sección 6 no muestra `UiState.kt`, pero la sección 8 dice `viewmodel/UiState.kt [E]`, y ese archivo usa `HttpException` en `mensajeDe()` | Dejé `UiState.kt` como archivo aparte (sección 8) y lo puse en el commit 2 | Si lo metía dentro de `PostsViewModel.kt`, el ViewModel tendría `HttpException`, que la regla de oro prohíbe. Y no podía ir en el commit 1 porque `HttpException` no existe hasta agregar Retrofit |
| (La IA revisó el commit 7 antes de subirlo) | Vio que se habían colado `.idea/misc.xml` y `.idea/deviceManager.xml`, que Android Studio había agregado solo. Los sacó con `git commit --amend` y desde ahí hizo cada commit indicando los archivos exactos | Lo acepté | La guía quita puntos si un commit mezcla contenido de otro paso |
| «¿Por qué no se ve como en el PDF?» (tarjeta y formulario) | El `body` trae saltos de línea `\n` y la tarjeta cortaba el texto; los reemplazó por espacios. También oscureció el borde de los campos de texto, que casi no se veía | Lo acepté | En el diseño el resumen va corrido y los campos tienen un borde gris azulado visible |
| «En el PDF el formulario sigue escrito junto al mensaje verde» | Explicó que el contrato del Paso 9 dice «muestra Publicado con id 101 **y limpia el formulario**», y que la guía pide los mismos elementos y orden, no una copia exacta | Seguí el contrato | Lo que se califica es el contrato; el diseño es de referencia |
| Prueba «Recurso inexistente» del Paso 10 | La guía dice cambiar `posts/{id}` por `posts/9999`, pero con `@Path("id")` en la función Retrofit lanza `IllegalArgumentException` (la URL ya no tiene `{id}`) y saldría «Error desconocido», no 404. Lo cambió temporalmente en el Repository: `api.detalle(9999)` | Lo acepté y lo deshice antes del commit | Así la petición sí llega al servidor y responde 404, que es lo que la prueba quiere demostrar: «Error del servidor (404)» |
| Prueba «JSON inesperado» | Agregó temporalmente `val autor: String` a `Post`, un campo que el servidor no envía | Lo deshice antes del commit | Sale «Respuesta inesperada» por `SerializationException`. Quitar un campo que sí llega no rompe nada gracias a `ignoreUnknownKeys = true` |

## Lo que aprendí revisando a la IA

- `@Path` cambia una parte del camino, `@Query` agrega `?clave=valor` a la URL y `@Body` manda un objeto como JSON en el cuerpo.
- Con `Response<T>` Retrofit no lanza `HttpException` por sí solo: hay que revisar `isSuccessful` o el código.
- PATCH modifica solo los campos enviados; PUT reemplaza el recurso completo.
- Una corrutina lanzada dentro de un `LaunchedEffect` se cancela cuando cambia su clave: para mostrar un Snackbar que debe sobrevivir se usa `rememberCoroutineScope()`.
- La guía también puede contradecirse (árbol vs. sección 8, ruta `posts/9999`): hay que entender la intención de cada regla y documentar la decisión.
