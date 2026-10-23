package com.espiritu.ayudacampus.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Una sola instancia de DataStore para toda la aplicación.
private val Context.dataStore by preferencesDataStore(
    name = "ajustes"
)

class PreferenciasStore(
    private val context: Context
) {

    private object Claves {
        val PERFIL_ACTIVO = stringPreferencesKey("perfilActivo")
        val USUARIO_ID = intPreferencesKey("usuarioId")
        val MODO_OSCURO = booleanPreferencesKey("modoOscuro")
        val NOTIFICACIONES = booleanPreferencesKey("notificaciones")
        val RECIBIR_TICKETS = booleanPreferencesKey("recibirTickets")
    }

    // LECTURA DE PREFERENCIAS

    val perfilActivo: Flow<String> = context.dataStore.data.map {
            preferencias ->
        preferencias[Claves.PERFIL_ACTIVO] ?: ""
    }

    val usuarioId: Flow<Int> = context.dataStore.data.map {
            preferencias ->
        preferencias[Claves.USUARIO_ID] ?: 0
    }

    val modoOscuro: Flow<Boolean> = context.dataStore.data.map {
            preferencias ->
        preferencias[Claves.MODO_OSCURO] ?: false
    }

    val notificaciones: Flow<Boolean> = context.dataStore.data.map {
            preferencias ->
        preferencias[Claves.NOTIFICACIONES] ?: true
    }

    val recibirTickets: Flow<Boolean> = context.dataStore.data.map {
            preferencias ->
        preferencias[Claves.RECIBIR_TICKETS] ?: true
    }

    // GUARDAR SESIÓN

    suspend fun guardarSesion(
        rol: String,
        usuarioId: Int
    ) {
        require(rol == "CLIENTE" || rol == "ADMIN") {
            "El rol debe ser CLIENTE o ADMIN"
        }

        context.dataStore.edit { preferencias ->
            preferencias[Claves.PERFIL_ACTIVO] = rol
            preferencias[Claves.USUARIO_ID] = usuarioId
        }
    }

    // GUARDAR MODO OSCURO

    suspend fun guardarModoOscuro(activado: Boolean) {
        context.dataStore.edit { preferencias ->
            preferencias[Claves.MODO_OSCURO] = activado
        }
    }

    // GUARDAR NOTIFICACIONES

    suspend fun guardarNotificaciones(activadas: Boolean) {
        context.dataStore.edit { preferencias ->
            preferencias[Claves.NOTIFICACIONES] = activadas
        }
    }

    // GUARDAR DISPONIBILIDAD DE LA MESA DE AYUDA

    suspend fun guardarRecibirTickets(activado: Boolean) {
        context.dataStore.edit { preferencias ->
            preferencias[Claves.RECIBIR_TICKETS] = activado
        }
    }

    // CERRAR SESIÓN SIN BORRAR LOS AJUSTES

    suspend fun cerrarSesion() {
        context.dataStore.edit { preferencias ->
            preferencias.remove(Claves.PERFIL_ACTIVO)
            preferencias.remove(Claves.USUARIO_ID)
        }
    }
}
