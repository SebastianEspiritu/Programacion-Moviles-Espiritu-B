package com.espiritu.clinicasalud.model
data class Medico(
    val id: Int,
    val nombre: String,
    val especialidad: String,
    val calificacion: Double,
    val descripcion: String
)

val listaMedicos = listOf(
    Medico(1, "Dra. Carmen Ríos", "Cardiología", 4.8, "Especialista en arritmias e hipertensión, con más de 10 años de experiencia clínica."),
    Medico(2, "Dr. Luis Fernández", "Pediatría", 4.6, "Enfocado en el cuidado integral de recién nacidos y niños hasta los 12 años."),
    Medico(3, "Dra. Andrea Salas", "Dermatología", 4.9, "Experta en dermatología clínica y estética, formación en la Clínica Mayo."),
    Medico(4, "Dr. Jorge Paredes", "Cardiología", 4.5, "Cardiólogo intervencionista, especializado en procedimientos mínimamente invasivos."),
    Medico(5, "Dra. Melissa Ortiz", "Pediatría", 4.7, "Pediatra con subespecialidad en nutrición infantil y desarrollo temprano.")
)

val listaEspecialidades = listOf("Todas", "Cardiología", "Pediatría", "Dermatología")