package com.espiritu.ayudacampus

import android.app.Application
import com.espiritu.ayudacampus.data.local.AyudaDatabase
import com.espiritu.ayudacampus.data.local.DatosIniciales
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AyudaApp : Application() {

    val database: AyudaDatabase by lazy {
        AyudaDatabase.obtener(this)
    }

    private val applicationScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    override fun onCreate() {
        super.onCreate()

        applicationScope.launch {
            DatosIniciales.sembrar(database)
        }
    }
}
