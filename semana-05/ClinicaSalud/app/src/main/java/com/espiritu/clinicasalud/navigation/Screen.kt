package com.espiritu.clinicasalud.navigation

sealed class Screen(val route: String) {
    object Inicio : Screen("inicio")
    object PerfilMedico : Screen("perfil_medico/{medicoId}") {
        fun createRoute(medicoId: Int) = "perfil_medico/$medicoId"
    }
    object AgendarCita : Screen("agendar_cita/{medicoId}") {
        fun createRoute(medicoId: Int) = "agendar_cita/$medicoId"
    }
    object Confirmacion : Screen("confirmacion")
    object MisCitas : Screen("mis_citas")
    object HistorialMedico : Screen("historial_medico")
}