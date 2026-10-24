package com.espiritu.ayudacampus.ui.navigation

object Rutas {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val MIS_TICKETS = "cliente/tickets"
    const val PANEL = "soporte/panel"
    const val NUEVO = "cliente/nuevo"
    const val PERFIL = "cliente/perfil"
    const val DETALLE = "cliente/ticket/{ticketId}"
    fun detalle(ticketId: Int) = "cliente/ticket/$ticketId"
    const val BANDEJA = "soporte/tickets"
    const val BANDEJA_RUTA = "soporte/tickets?alertas={alertas}"
    const val CATEGORIAS = "soporte/categorias"
    const val AJUSTES = "soporte/ajustes"
    const val ATENDER = "soporte/ticket/{ticketId}"
    fun bandeja(alertas: Boolean = false) = "soporte/tickets?alertas=$alertas"
    fun atender(ticketId: Int) = "soporte/ticket/$ticketId"
}
