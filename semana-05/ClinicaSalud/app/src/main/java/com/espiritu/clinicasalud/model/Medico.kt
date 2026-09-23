package com.espiritu.clinicasalud.model
data class Medico(
    val id: Int,
    val nombre: String,
    val especialidad: String,
    val calificacion: Double
)

val listaMedicos = listOf(
    Medico(1, "Dra. Adriana Salte", "Cardiología", 4.8),
    Medico(2, "Dr. Luis Fernández", "Pediatría", 4.6),
    Medico(3, "Dra. Andrea Salas", "Dermatología", 4.9),
    Medico(4, "Dr. Jorge Paredes", "Cardiología", 4.5),
    Medico(5, "Dra. Melissa Ortiz", "Pediatría", 4.7)
)

val listaEspecialidades = listOf("Todas", "Cardiología", "Pediatría", "Dermatología")