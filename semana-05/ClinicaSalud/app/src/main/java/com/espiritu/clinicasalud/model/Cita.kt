package com.espiritu.clinicasalud.model

enum class EstadoCita {
    CONFIRMADA,
    COMPLETADA,
    CANCELADA
}

data class Cita(
    val medico: Medico,
    val fecha: String,
    val hora: String,
    val estado: EstadoCita = EstadoCita.CONFIRMADA
)