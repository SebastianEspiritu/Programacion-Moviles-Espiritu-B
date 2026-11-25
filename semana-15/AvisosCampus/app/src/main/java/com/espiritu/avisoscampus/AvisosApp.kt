package com.espiritu.avisoscampus

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.espiritu.avisoscampus.data.AvisosRepository
import com.espiritu.avisoscampus.data.AvisosRepositoryDataStore
import com.espiritu.avisoscampus.notificacion.Notificador
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

// Un solo DataStore por archivo en toda la app (crear dos sobre el mismo archivo da error)
private val Context.almacenAvisos: DataStore<Preferences> by preferencesDataStore(name = "avisos")

class AvisosApp : Application() {

    // Alcance que vive lo mismo que la app: mantiene el historial cargado
    private val alcance = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var repositorio: AvisosRepository
        private set

    override fun onCreate() {
        super.onCreate()
        Notificador.crearCanal(this)
        repositorio = AvisosRepositoryDataStore(almacenAvisos, alcance)
    }
}
