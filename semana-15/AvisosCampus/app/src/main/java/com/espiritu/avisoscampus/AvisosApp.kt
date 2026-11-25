package com.espiritu.avisoscampus

import android.app.Application
import com.espiritu.avisoscampus.notificacion.Notificador

class AvisosApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Notificador.crearCanal(this)
    }
}
