package com.espiritu.ayudacampus

import android.app.Application
import com.espiritu.ayudacampus.data.local.AyudaDatabase
import com.espiritu.ayudacampus.data.local.DatosIniciales
import com.espiritu.ayudacampus.data.repository.AyudaRepository
import com.espiritu.ayudacampus.data.repository.AyudaRepositoryRoom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AyudaApp : Application() {

    // Una sola instancia de la base de datos.
    val database: AyudaDatabase by lazy {
        AyudaDatabase.obtener(this)
    }

    // Repositorio que utilizarán los ViewModels.
    val repository: AyudaRepository by lazy {
        AyudaRepositoryRoom(database)
    }

    private val applicationScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    override fun onCreate() {
        super.onCreate()

        // Carga los datos iniciales sin bloquear la interfaz.
        applicationScope.launch {
            DatosIniciales.sembrar(database)
        }
    }
}
