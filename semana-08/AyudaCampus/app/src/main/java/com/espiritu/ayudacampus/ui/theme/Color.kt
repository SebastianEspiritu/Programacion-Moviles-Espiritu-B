package com.espiritu.ayudacampus.ui.theme

import androidx.compose.ui.graphics.Color

val EstudiantePrimario = Color(0xFF0891B2)
val EstudianteContenedor = Color(0xFFCFFAFE)
val EstudianteFondo = Color(0xFFF7FDFE)
val SoportePrimario = Color(0xFF7C3AED)
val SoporteContenedor = Color(0xFFEDE4FF)
val SoporteFondo = Color(0xFFFAF8FF)

val EstadoAbierto = Color(0xFFD97706)
val EstadoEnProceso = Color(0xFF2563EB)
val EstadoResuelto = Color(0xFF16A34A)
val EstadoCerrado = Color(0xFF6B7280)
val PrioridadAlta = Color(0xFFDC2626)
val PrioridadMedia = Color(0xFFB45309)
val PrioridadBaja = Color(0xFF6B7280)

fun colorPrioridad(prioridad: String): Color = when (prioridad) {
    "ALTA" -> PrioridadAlta
    "MEDIA" -> PrioridadMedia
    else -> PrioridadBaja
}
