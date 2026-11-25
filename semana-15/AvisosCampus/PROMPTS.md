# PROMPTS.md · AvisosCampus

Registro de los prompts importantes que usé con la IA, qué respondió, qué cambié o descarté y por qué.

**Herramienta:** asistente de IA (Claude). El docente permitió usar IA en todas las fases, así que también incluyo los problemas reales de la Fase 1.
**Mejoras de la Fase 2 (rama `con-ia`):** dos canales (Urgentes y Avisos), texto largo con `BigTextStyle` e historial persistente con DataStore.
**Lo que revisé en cada respuesta:** nada de `Context`, `NotificationManager` ni `BroadcastReceiver` en el ViewModel; el Repository tampoco usa `Context`; todo receptor se quita en `onDispose`; el `PendingIntent` sigue con `FLAG_IMMUTABLE`.

## Fase 2 · Mejoras

| Prompt que escribí | Qué respondió la IA | Qué cambié o descarté | Por qué |
|---|---|---|---|
| «Haz la fase de mejora con IA en mi rama con-ia» | Propuso 3 de las 6 mejoras de la guía: dos canales (Notificador + ViewModel), texto largo (Notificador) e historial persistente (Repository). Antes de empezar, copió a `con-ia` los 10 commits de `main` con `git cherry-pick`, como en la semana 14 | Acepté las tres | La guía pide mínimo 3 mejoras y al menos una que cambie el Repository o el ViewModel: la de canales cambia el ViewModel y la de DataStore cambia el Repository |
| «Agrega un canal Urgentes para el recordatorio» | Creó el canal `urgentes` con `IMPORTANCE_HIGH` junto al canal `avisos` (`createNotificationChannels`). En `model/Evento.kt` agregó `enum class Canal` y `data class EventoAviso(aviso, canal)`. El ViewModel ahora emite `EventoAviso` y el recordatorio usa `Canal.URGENTES`; la pantalla pasa el canal a `Notificador.mostrar` | Lo acepté. No toqué `Modelos.kt` y puse lo nuevo en otro archivo | `Modelos.kt` es un archivo entregado. Así el ViewModel **decide** qué es urgente sin conocer ids de canal ni `Context`, y el Notificador solo traduce `Canal` a su id y prioridad |
| (La IA lo advirtió) «¿Y si solo subo la importancia del canal Avisos?» | Explicó que la importancia de un canal **no se puede cambiar por código** después de creado: Android respeta lo que ya tiene el usuario. Por eso se crea un canal nuevo | Mantuve dos canales | Cambiar `IMPORTANCE_DEFAULT` a `HIGH` en el mismo id no haría nada en un celular donde la app ya estaba instalada |
| «Muestra el texto completo al expandir la notificación» | Agregó `setStyle(NotificationCompat.BigTextStyle().bigText(...))`. Como los textos son cortos, al expandir se ve el mensaje y debajo `Origen: … · Hora: …` | Lo acepté | Sin datos extra, la notificación expandida se vería igual que la cerrada y no se notaría la mejora |
| «Guarda el historial con DataStore» | Primera idea: que el Repository reciba `Context` para crear el DataStore | **La descarté** | La tabla de la guía dice que el Repository no debe usar `Context` |
| «Sin Context en el Repository» | El DataStore se crea una sola vez en `AvisosApp` (`preferencesDataStore`) y llega ya armado a `AvisosRepositoryDataStore(almacen, alcance)`. El historial se guarda como JSON (`org.json`), máximo 50 avisos | Lo acepté | Además, solo puede existir **un** DataStore por archivo; crearlo en la `Application` lo garantiza |
| (Revisión del código) «¿`registrar` puede seguir igual?» | Lo cambió a `suspend fun registrar(...)` en la interface. Guardar en disco no debe bloquear el hilo principal | Lo acepté. El ViewModel no necesitó cambios porque ya llamaba a `registrar` dentro de `viewModelScope.launch` | `AvisosRepositoryMemoria` también quedó `suspend` y se puede seguir usando para pruebas |
| (Revisión del código) «¿Qué pasa con los ids al reabrir la app?» | Con un contador en memoria, al reabrir volvería a empezar en 1 y repetiría ids. Ahora el id sale de `max(id guardados) + 1`, dentro de la transacción `edit` | Lo acepté | Si dos notificaciones tienen el mismo id, la segunda reemplaza a la primera (pregunta 3 de «Para pensar») |

## Fase 1 · Problemas reales y cómo los corregí

| Prompt que escribí | Qué respondió la IA | Qué cambié o descarté | Por qué |
|---|---|---|---|
| «Prepárate para este laboratorio… ¿cómo lo tengo que crear?» | Indicó cómo llenar *New Project*: `AvisosCampus`, paquete `com.espiritu.avisoscampus`, guardado en `semana-15/AvisosCampus`. Armó el código de cada paso por separado para respetar un commit por paso | Mantuve `com.espiritu` en vez de `com.tecsup` | Es la convención de mis laboratorios anteriores (`com.apellido.proyecto`) |
| «Ya creé el proyecto, ¿no lo puedes hacer tú? Los commits a mi nombre» | Vio que mi carpeta no era un repositorio git (solo tenía `semana-15`). Unió el historial de GitHub con mi proyecto y creó los 9 commits con mi nombre y correo, configurados solo para este repo | Lo acepté | La Mac del salón es compartida: el usuario de git no debe quedar global |
| «`Permission denied to Jamestecsup`» (error 403 al hacer push) | Explicó que el llavero de la Mac tenía guardada la cuenta de otro alumno. Usé `git -c credential.helper= push origin main` con mi token | Lo hice así | Ese comando no usa ni guarda credenciales en la Mac del salón |
| «No veo Developer settings» | Estaba en la configuración del **repositorio**; los tokens están en la de la **cuenta** | Creé el token con expiración de 7 días | Un token corto es más seguro en una Mac compartida |
| «Te paso las capturas en un Word» | Sacó las imágenes del Word, eligió las 5 que pide la guía y agregó 3 de apoyo. Avisó que en *Permiso denegado* falta el recuadro rojo y que el historial tenía una sola fila | Lo tomé en cuenta | El recuadro rojo solo aparece después de pulsar **Mostrar aviso** con el permiso denegado |

## Cómo probar las mejoras

1. **Dos canales:** Ajustes → Apps → AvisosCampus → Notificaciones muestra *Avisos* y *Urgentes*. **Recordar en 10 s** → el recordatorio aparece emergente arriba de la pantalla.
2. **Texto largo:** baja el panel y expande una notificación (flecha ⌄): se ve el texto y la línea `Origen: … · Hora: …`.
3. **Historial persistente:** genera avisos, cierra la app desde Recientes y vuelve a abrirla: el historial sigue ahí.

## Lo que aprendí revisando a la IA

- La IA propuso primero algo que rompía la arquitectura (`Context` en el Repository): hay que revisar cada respuesta contra la tabla de capas.
- La importancia de un canal la controla el usuario después de crearlo; para cambiarla hay que crear otro canal.
- `DataStore` necesita una sola instancia por archivo y funciones `suspend`, por eso se crea en la `Application` y se inyecta.
