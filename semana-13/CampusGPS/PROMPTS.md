# PROMPTS.md · CampusGPS

Registro de los prompts importantes que usé con la IA, qué respondió, qué cambié o descarté y por qué.

**Herramienta:** asistente de IA (Claude). El docente permitió usar IA en todas las fases, así que también incluyo los problemas reales de la Fase 1.
**Mejoras de la Fase 2 (rama `con-ia`):** filtrar por categoría, radio de cercanía y punto azul nativo.
**Lo que revisé en cada respuesta:** la View solo habla con su ViewModel, el ViewModel solo con interfaces de Repository y sin `Context`, y ningún Composable calcula distancias ni filtra datos.

## Fase 2 · Mejoras

| Prompt que escribí | Qué respondió la IA | Qué cambié o descarté | Por qué |
|---|---|---|---|
| «Quiero las mejoras: filtrar por categoría, radio de cercanía y punto azul nativo» (elegidas entre las 6 de la guía) | Explicó qué capas cambia cada una: filtro y radio cambian ViewModel + View; punto azul solo la View | Acepté las tres | La guía pide mínimo 3 mejoras y al menos una que cambie el ViewModel o el Repository: el filtro y el radio cumplen esa regla |
| «Agrega el filtro por categoría» | Agregó en el ViewModel `categoria` (null = todas) y `categorias` sacadas del Repository; `lugares` se arma con `combine(_lugares, _categoria)`. Creó `FiltroCategorias.kt` con `FilterChip` y la función `filtrar()`, que quita la selección si el lugar queda oculto | Lo acepté después de revisar que la vista no filtra nada: solo dibuja los chips y llama a `vm.filtrar()` | Filtrar en el Composable sería lógica en la vista. Con el filtro en el ViewModel, `masCercano` (que se deriva de `lugares`) pasa a ser el más cercano de la categoría elegida sin tocar más código. Lo probé: con «Estudio» solo quedan Biblioteca y Laboratorio |
| «Agrega el radio de cercanía de 100, 200 y 500 m» | En el ViewModel: `radio`, `cambiarRadio()` y `dentroDelRadio: StateFlow<Set<Int>>` con `combine(lugares, _radio)`. En la vista: `SelectorRadio.kt`, un `Circle` en `MapaUbicacion` solo cuando hay ubicación, y la etiqueta «Dentro del radio» en la tarjeta | Lo acepté. Revisé que la comparación `metros <= radio` está en el ViewModel y no en `ListaLugares` | Comparar distancias en la tarjeta sería lógica en la vista. La tarjeta solo recibe el `Set` de ids. Al probar desde la ubicación por defecto del emulador (California) ningún lugar entraba en el radio: hay que poner el emulador en el punto base |
| «Agrega el punto azul nativo» | `MapProperties(isMyLocationEnabled = tienePermiso)` y `MapUiSettings(myLocationButtonEnabled = tienePermiso)`. El permiso se revisa en `PantallaMapa` con `ContextCompat` y se actualiza al responder el cuadro de permiso | Lo acepté. Comprobé que el ViewModel no cambió | Revisar el permiso necesita `Context`, y el ViewModel no debe recibirlo. El punto azul solo se activa con permiso, para no provocar un `SecurityException` |
| «Pasa CampusGPS de `main` a mi rama `con-ia`» | Primero propuso `git merge main` dentro de `con-ia`. Después lo probó en una copia y vio que daba conflictos en `semana-08/AyudaCampus`, así que lo cambió por `git cherry-pick` de los 10 commits de CampusGPS | Usé el cherry-pick | `con-ia` tiene su propia versión de AyudaCampus. El cherry-pick copia solo `semana-13` y conserva los mismos 10 mensajes de commit |

## Fase 1 · Problemas reales y cómo los corregí

| Prompt que escribí | Qué respondió la IA | Qué cambié o descarté | Por qué |
|---|---|---|---|
| «Al ejecutar sale: Class 'kotlin.Unit' was compiled with an incompatible version of Kotlin» | Explicó que una de las librerías de mapas/ubicación trae Kotlin 2.4.10 y el proyecto compilaba con 2.2.10. Subió `kotlin` a 2.4.10 en `libs.versions.toml` | Lo acepté | El proyecto base debe compilar; con la versión anterior no podía leer esa librería |
| «Sale "Unresolved reference 'util'" al sincronizar» (Paso 1, `build.gradle.kts`) | El código que había generado usaba `java.util.Properties()`, que choca con la extensión `java { }` de Gradle. Lo cambió por `import java.util.Properties` arriba del archivo | Lo acepté | Fue un error de la propia IA: su código compilaba en teoría pero no en Gradle Kotlin DSL. Lo detecté porque el Sync falló |
| (La IA revisó mi historial antes del commit 3) | Avisó que el commit 2 se había subido sin esa corrección del `import` | Metí la corrección en el commit 2 con `git commit --amend` y `git push --force-with-lease` | Si la dejaba para el commit 3, ese commit mezclaba dos pasos y la guía quita puntos por eso |
| «Me da error al cambiar a `con-ia`» | Explicó que Android Studio vuelve a escribir `.idea/workspace.xml` aunque se haga `stash`, y que el `cherry-pick` se había aplicado sobre `main` | Hice `git cherry-pick --abort`, cerré Android Studio, descarté `workspace.xml` y repetí los pasos | Ese archivo solo guarda la configuración del editor; no afecta al proyecto |

## Lo que aprendí revisando a la IA

- La IA también se equivoca (el `java.util.Properties` en Gradle): siempre hay que compilar y probar lo que genera antes de hacer commit.
- Mi criterio para cada mejora: **si decide algo sobre los datos, va al ViewModel; si solo dibuja, va a la vista**.
- El permiso de ubicación es responsabilidad de la vista (necesita `Context`); obtener la ubicación es del Repository.
- Nunca le pasé mi clave de Maps a la IA: está solo en `local.properties`, que no se sube a GitHub.
