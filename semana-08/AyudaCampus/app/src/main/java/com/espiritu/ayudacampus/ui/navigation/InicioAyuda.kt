package com.espiritu.ayudacampus.ui.navigation

import com.espiritu.ayudacampus.model.Rol

internal fun destinoInicio(
    restaurando: Boolean,
    errorInicializacion: Boolean,
    rol: Rol?,
    loginSolicitado: Boolean
): String = when {
    restaurando || errorInicializacion -> Rutas.SPLASH
    rol == Rol.ADMIN -> Rutas.PANEL
    rol == Rol.CLIENTE -> Rutas.MIS_TICKETS
    loginSolicitado -> Rutas.LOGIN
    else -> Rutas.BIENVENIDA
}
