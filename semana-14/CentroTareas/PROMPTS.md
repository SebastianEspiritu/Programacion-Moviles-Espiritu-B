# PROMPTS.md · CentroTareas

Registro de los prompts importantes que usé con la IA, qué respondió, qué cambié o descarté y por qué.

**Herramienta:** asistente de IA (Claude). El docente permitió usar IA en todas las fases, así que también incluyo los problemas reales de la Fase 1.
**Mejoras de la Fase 2 (rama `con-ia`):** progreso desde el Repository con `Flow`, tiempo máximo con `withTimeout` y primos con porcentaje.
**Lo que revisé en cada respuesta:** la View solo habla con su ViewModel, el ViewModel solo con la interface `TareasRepository` y sin `Context`, nada de `GlobalScope`, `runBlocking` ni `Thread.sleep`, y la `CancellationException` siempre se vuelve a lanzar.

## Fase 2 · Mejoras

| Prompt que escribí | Qué respondió la IA | Qué cambié o descarté | Por qué |
|---|---|---|---|
| «Vamos con: progreso desde el Repository, tiempo máximo y primos con progreso» (elegidas entre las 6 de la guía) | Explicó qué capas cambia cada una: la 1 cambia Repository + ViewModel, la 2 ViewModel + View y la 3 las tres capas. Avisó que con un límite fijo de 2 s la carga «A la vez» (3 s) fallaría siempre | Acepté las tres y que el límite se active con un interruptor | La guía pide mínimo 3 mejoras y al menos una que cambie el Repository o el ViewModel: la 1 y la 3 cumplen. Con el interruptor se puede probar el tiempo máximo sin romper la carga normal |
| «Mueve la tarea con progreso al Repository» | Agregó `fun tareaConProgreso(): Flow<Float>` a la interface. En `TareasRepositoryLocal` la implementó con `flow { emit(i / 100f) }` (100 vueltas de `delay(40)`) y `flowOn(Dispatchers.Default)`. En el ViewModel, `tareaLarga()` solo hace `collect` y actualiza `_progreso` | Lo acepté. Revisé que `trabajo`, `corriendo`, el `finally` y `cancelar()` no cambiaron | Así el trabajo vive en el Repository, como en una app real, y el ViewModel solo lo recolecta. Lo probé en Logcat: `Antes: main`, `Dentro: DefaultDispatcher-worker` (gracias a `flowOn`) y `Despues: main`. Cancelar sigue funcionando porque `collect` se cancela con el Job |
| «Agrega el tiempo máximo de 2 s» | Envolvió la carga en `withTimeout(2000)` dentro de `ejecutarCarga` y agregó `limiteTiempo` con `cambiarLimiteTiempo()`. Atrapa `TimeoutCancellationException` y muestra «Tardó demasiado» | Lo acepté después de revisar el orden de los `catch` | `TimeoutCancellationException` es una subclase de `CancellationException`: si su `catch` fuera después, el tiempo máximo se tomaría como una cancelación y nunca se vería el mensaje. Una cancelación de verdad se sigue relanzando con `throw e` |
| «Agrega el porcentaje al contar primos» | Agregó un parámetro `alAvanzar: (Float) -> Unit` a `contarPrimos` (con `= {}` en la interface) que avisa 100 veces durante el cálculo. En el ViewModel, `progresoPrimos: StateFlow<Float>`; en la pantalla, una barra con «Calculando... 45 %» | Lo acepté. No guardé el porcentaje dentro de `PrimosUi.Calculando` | Avisar en cada número haría el cálculo más lento; 100 avisos bastan. No modifiqué `Estados.kt` porque es un archivo entregado. La barra va más lenta al final porque mide números revisados, no tiempo, y los números grandes cuestan más |
| «Pasa CentroTareas de `main` a mi rama `con-ia`» | Repitió lo que funcionó en la semana 13: cerrar Android Studio, descartar `.idea/workspace.xml` y hacer `git cherry-pick` de los 9 commits | Lo hice desde CMD | `con-ia` tiene su propia versión de semanas anteriores; el cherry-pick copia solo `semana-14` con los mismos 9 mensajes |
| (Error de la IA) «Ejecuto `git cherry-pick beb316e^..7441232`» | Antes de que lo ejecutara, avisó que en CMD el `^` es un carácter especial y se perdería el primer commit. Lo cambió por `4defb61..7441232` | Usé el rango corregido | Con `^` en CMD se habrían copiado solo 8 commits y faltaría el proyecto base |

## Fase 1 · Problemas reales y cómo los corregí

| Prompt que escribí | Qué respondió la IA | Qué cambié o descarté | Por qué |
|---|---|---|---|
| «Ayúdame a hacer este laboratorio, lo importante son los commits» | Armó el proyecto base con la estructura de la guía y un commit por paso. Usó el paquete `com.espiritu.centrotareas` en vez de `com.tecsup.centrotareas` | Mantuve `com.espiritu` | Es la convención de mis laboratorios anteriores (`com.apellido.proyecto`) |
| «¿Y esos errores rojos del MainActivity?» | Explicó que no eran errores reales: la app compilaba, pero el editor no conocía la librería nueva `lifecycle-viewmodel-compose` hasta sincronizar Gradle | Hice File → Sync Project with Gradle Files | El código estaba bien; solo faltaba que Android Studio leyera la dependencia |
| (La IA revisó el commit 2 antes de que lo subiera) | Vio que se habían colado 8 archivos `.idea/*.xml` que Android Studio genera solo. Los quitó con `git commit --amend` y desde ahí agregó los archivos de cada paso uno por uno | Lo acepté | Esos archivos no son parte del Paso 1; la guía quita puntos si un commit mezcla contenido. Como el commit no estaba subido, corregirlo no afectaba a GitHub |
| «Con 2 000 000 tardó 774 ms» (Paso 5) | Recordó que la guía pide entre 2 y 5 s para el valor por defecto y lo subió de 500 000 a 5 000 000 | Lo acepté: con 5 000 000 salen 348 513 primos en ≈ 2 s | Con menos de 1 s no se nota que la pantalla sigue respondiendo mientras calcula |
| «Te paso las capturas» | Detectó que faltaba la de «Dos cargas» con éxito (la 4.ª imagen era el mismo error repetido) | Tomé la captura de «A la vez» en 3001 ms | El README pide 5 capturas distintas: tarea con progreso, cancelada, primos, cargas y error |

## Lo que aprendí revisando a la IA

- `Thread.sleep` bloquea el hilo y `delay` lo libera: la diferencia se ve con el botón Tócame.
- Con `withTimeout`, el orden de los `catch` importa: la excepción más específica va primero.
- `flowOn` cambia el hilo donde se emite el `Flow`, pero quien recolecta sigue en el hilo principal, así que actualizar el `StateFlow` desde el `collect` es seguro.
- La IA también se equivoca (el `^` en CMD): siempre hay que leer los comandos y revisar el resultado antes de seguir.
