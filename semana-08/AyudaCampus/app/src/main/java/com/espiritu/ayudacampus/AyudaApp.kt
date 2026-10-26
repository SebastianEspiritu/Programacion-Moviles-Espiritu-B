package com.espiritu.ayudacampus

import android.app.Application
import com.espiritu.ayudacampus.data.local.AyudaDatabase
import com.espiritu.ayudacampus.data.local.DatosIniciales
import com.espiritu.ayudacampus.data.prefs.PreferenciasStore
import com.espiritu.ayudacampus.data.repository.AyudaRepository
import com.espiritu.ayudacampus.data.repository.AyudaRepositoryRoom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.CancellationException

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

    val preferencias: PreferenciasStore by lazy { PreferenciasStore(applicationContext) }

    // Todos los consumidores esperan el resultado de la misma carga inicial.
    private val inicializacion by lazy {
        applicationScope.async {
            try {
                DatosIniciales.sembrar(database)
                Result.success(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure<Unit>(e)
            }
        }
    }

    suspend fun esperarInicializacion() {
        inicializacion.await().getOrThrow()
    }

    override fun onCreate() {
        super.onCreate()
        inicializacion.start()
    }
}
