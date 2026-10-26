package com.espiritu.ayudacampus.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
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

data class SesionGuardada(val rol: String, val usuarioId: Int)

interface PreferenciasSesion {
    val sesion: Flow<SesionGuardada>
    val modoOscuro: Flow<Boolean>
    val notificaciones: Flow<Boolean>
    val recibirTickets: Flow<Boolean>
    suspend fun guardarModoOscuro(activado: Boolean)
    suspend fun guardarNotificaciones(activadas: Boolean)
    suspend fun guardarRecibirTickets(activado: Boolean)
    suspend fun guardarSesion(rol: String, usuarioId: Int)
    suspend fun cerrarSesion()
}

class PreferenciasStore internal constructor(
    private val store: DataStore<Preferences>
) : PreferenciasSesion {
    constructor(context: Context) : this(context.applicationContext.dataStore)

    private object Claves {
        val PERFIL_ACTIVO = stringPreferencesKey("perfilActivo")
        val USUARIO_ID = intPreferencesKey("usuarioId")
        val MODO_OSCURO = booleanPreferencesKey("modoOscuro")
        val NOTIFICACIONES = booleanPreferencesKey("notificaciones")
        val RECIBIR_TICKETS = booleanPreferencesKey("recibirTickets")
    }

    // LECTURA DE PREFERENCIAS

    override val sesion: Flow<SesionGuardada> = store.data.map {
        SesionGuardada(it[Claves.PERFIL_ACTIVO] ?: "", it[Claves.USUARIO_ID] ?: 0)
    }

    val perfilActivo: Flow<String> = store.data.map {
            preferencias ->
        preferencias[Claves.PERFIL_ACTIVO] ?: ""
    }

    val usuarioId: Flow<Int> = store.data.map {
            preferencias ->
        preferencias[Claves.USUARIO_ID] ?: 0
    }

    override val modoOscuro: Flow<Boolean> = store.data.map {
            preferencias ->
        preferencias[Claves.MODO_OSCURO] ?: false
    }

    override val notificaciones: Flow<Boolean> = store.data.map {
            preferencias ->
        preferencias[Claves.NOTIFICACIONES] ?: true
    }

    override val recibirTickets: Flow<Boolean> = store.data.map {
            preferencias ->
        preferencias[Claves.RECIBIR_TICKETS] ?: true
    }

    // GUARDAR SESIÓN

    override suspend fun guardarSesion(
        rol: String,
        usuarioId: Int
    ) {
        require(rol == "CLIENTE" || rol == "ADMIN") {
            "El rol debe ser CLIENTE o ADMIN"
        }

        store.edit { preferencias ->
            preferencias[Claves.PERFIL_ACTIVO] = rol
            preferencias[Claves.USUARIO_ID] = usuarioId
        }
    }

    // GUARDAR MODO OSCURO

    override suspend fun guardarModoOscuro(activado: Boolean) {
        store.edit { preferencias ->
            preferencias[Claves.MODO_OSCURO] = activado
        }
    }

    // GUARDAR NOTIFICACIONES

    override suspend fun guardarNotificaciones(activadas: Boolean) {
        store.edit { preferencias ->
            preferencias[Claves.NOTIFICACIONES] = activadas
        }
    }

    // GUARDAR DISPONIBILIDAD DE LA MESA DE AYUDA

    override suspend fun guardarRecibirTickets(activado: Boolean) {
        store.edit { preferencias ->
            preferencias[Claves.RECIBIR_TICKETS] = activado
        }
    }

    // CERRAR SESIÓN SIN BORRAR LOS AJUSTES

    override suspend fun cerrarSesion() {
        store.edit { preferencias ->
            preferencias.remove(Claves.PERFIL_ACTIVO)
            preferencias.remove(Claves.USUARIO_ID)
        }
    }
}
