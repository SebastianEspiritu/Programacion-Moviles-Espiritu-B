package com.espiritu.avisoscampus.notificacion

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.espiritu.avisoscampus.model.Aviso

object Notificador {
    const val CANAL = "avisos"

    fun crearCanal(context: Context) {
        // Los canales existen desde Android 8 (API 26)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL,
                "Avisos",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones de la app"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal)
        }
    }

    fun tienePermiso(context: Context): Boolean {
        // TODO Paso 2: si SDK_INT < 33 devuelve true; si no, ContextCompat.checkSelfPermission
        return false
    }

    fun mostrar(context: Context, aviso: Aviso): Boolean {
        // TODO 1: si no tienePermiso(context) -> return false
        // TODO 2: construir la notificacion con NotificationCompat.Builder
        // TODO 3: NotificationManagerCompat.from(context).notify(aviso.id, ...)
        return true
    }
}
