package com.espiritu.avisoscampus.notificacion

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.espiritu.avisoscampus.MainActivity
import com.espiritu.avisoscampus.R
import com.espiritu.avisoscampus.model.Aviso
import com.espiritu.avisoscampus.model.Canal

object Notificador {
    const val CANAL = "avisos"
    const val CANAL_URGENTE = "urgentes"

    fun crearCanal(context: Context) {
        // Los canales existen desde Android 8 (API 26)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val avisos = NotificationChannel(
                CANAL,
                "Avisos",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones de la app"
            }
            // IMPORTANCE_HIGH: la notificación aparece emergente (heads-up) y suena
            val urgentes = NotificationChannel(
                CANAL_URGENTE,
                "Urgentes",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Recordatorios que no puedes dejar pasar"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannels(listOf(avisos, urgentes))
        }
    }

    fun tienePermiso(context: Context): Boolean {
        // Antes de Android 13 (API 33) el permiso no se pide: se concede al instalar
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission") // el permiso ya se revisa con tienePermiso
    fun mostrar(context: Context, aviso: Aviso, canal: Canal = Canal.AVISOS): Boolean {
        // 1. Sin permiso no se muestra nada, pero tampoco se lanza un error
        if (!tienePermiso(context)) return false

        // Al tocar la notificación se abre MainActivity
        val intent = Intent(context, MainActivity::class.java)
        val pendiente = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // El ViewModel eligió el canal; aquí solo se traduce a su id y prioridad
        val urgente = canal == Canal.URGENTES
        val idCanal = if (urgente) CANAL_URGENTE else CANAL
        val prioridad =
            if (urgente) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT

        // 2. Construir la notificación
        val notificacion = NotificationCompat.Builder(context, idCanal)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle(aviso.titulo)
            .setContentText(aviso.texto)
            .setPriority(prioridad)            // para Android 7 (sin canales)
            .setContentIntent(pendiente)
            .setAutoCancel(true)
            .build()

        // 3. Mostrarla con el id del aviso para que cada una sea distinta
        NotificationManagerCompat.from(context).notify(aviso.id, notificacion)
        return true
    }
}
