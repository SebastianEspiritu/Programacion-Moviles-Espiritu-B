package com.espiritu.avisoscampus.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.espiritu.avisoscampus.model.Aviso
import com.espiritu.avisoscampus.model.Origen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Guarda el historial en disco con DataStore: no se pierde al cerrar la app.
// No recibe Context: el DataStore se crea en AvisosApp y llega ya armado.
class AvisosRepositoryDataStore(
    private val almacen: DataStore<Preferences>,
    alcance: CoroutineScope
) : AvisosRepository {

    private val claveHistorial = stringPreferencesKey("historial")

    override val avisos: StateFlow<List<Aviso>> = almacen.data
        .catch { error ->
            // Si el archivo no se puede leer, se empieza con el historial vacío
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { prefs -> desdeJson(prefs[claveHistorial]) }
        .stateIn(alcance, SharingStarted.Eagerly, emptyList())

    override suspend fun registrar(titulo: String, texto: String, origen: Origen): Aviso {
        val hora = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        lateinit var nuevo: Aviso
        // edit es una transacción: lee la lista guardada, agrega el aviso y la vuelve a guardar
        almacen.edit { prefs ->
            val lista = desdeJson(prefs[claveHistorial])
            // El id sigue después del mayor guardado: así las notificaciones nunca repiten id
            val id = (lista.maxOfOrNull { it.id } ?: 0) + 1
            nuevo = Aviso(id, titulo, texto, origen, hora)
            prefs[claveHistorial] = aJson((listOf(nuevo) + lista).take(MAXIMO))
        }
        return nuevo
    }

    private fun aJson(avisos: List<Aviso>): String {
        val arreglo = JSONArray()
        avisos.forEach { aviso ->
            arreglo.put(
                JSONObject()
                    .put("id", aviso.id)
                    .put("titulo", aviso.titulo)
                    .put("texto", aviso.texto)
                    .put("origen", aviso.origen.name)
                    .put("hora", aviso.hora)
            )
        }
        return arreglo.toString()
    }

    private fun desdeJson(texto: String?): List<Aviso> {
        if (texto.isNullOrBlank()) return emptyList()
        return runCatching {
            val arreglo = JSONArray(texto)
            List(arreglo.length()) { i ->
                val o = arreglo.getJSONObject(i)
                Aviso(
                    id = o.getInt("id"),
                    titulo = o.getString("titulo"),
                    texto = o.getString("texto"),
                    origen = Origen.valueOf(o.getString("origen")),
                    hora = o.getString("hora")
                )
            }
        }.getOrDefault(emptyList())
    }

    private companion object {
        const val MAXIMO = 50       // se guardan los 50 más recientes; la pantalla muestra 20
    }
}
