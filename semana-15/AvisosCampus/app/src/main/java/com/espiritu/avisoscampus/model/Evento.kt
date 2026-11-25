package com.espiritu.avisoscampus.model

// Canal por el que debe salir la notificación (lo decide el ViewModel)
enum class Canal { AVISOS, URGENTES }

// Evento de una sola vez que el ViewModel envía a la pantalla
data class EventoAviso(
    val aviso: Aviso,
    val canal: Canal = Canal.AVISOS
)
