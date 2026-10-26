package com.espiritu.ayudacampus.ui.navigation

import com.espiritu.ayudacampus.model.Rol
import org.junit.Assert.assertEquals
import org.junit.Test

class InicioAyudaTest {
    @Test fun arranqueSinSesionMuestraBienvenida() {
        assertEquals(Rutas.BIENVENIDA, destinoInicio(false, false, null, false))
    }

    @Test fun restauracionTienePrioridadSobreLoginYSesion() {
        listOf(null, Rol.CLIENTE, Rol.ADMIN).forEach { rol ->
            assertEquals(Rutas.SPLASH, destinoInicio(true, false, rol, true))
        }
    }

    @Test fun errorInicializacionConservaPantallaDeCargaYError() {
        assertEquals(Rutas.SPLASH, destinoInicio(false, true, null, true))
    }

    @Test fun sesionesRestauradasSaltanBienvenidaYLogin() {
        assertEquals(Rutas.MIS_TICKETS, destinoInicio(false, false, Rol.CLIENTE, false))
        assertEquals(Rutas.PANEL, destinoInicio(false, false, Rol.ADMIN, false))
    }

    @Test fun cerrarOCambiarPerfilSinCuentaAcreditadaAbreLogin() {
        assertEquals(Rutas.PANEL, destinoInicio(false, false, Rol.ADMIN, true))
        assertEquals(Rutas.LOGIN, destinoInicio(false, false, null, true))
    }

    @Test fun solicitarLoginNoAlteraElRolAutenticado() {
        assertEquals(Rutas.MIS_TICKETS, destinoInicio(false, false, Rol.CLIENTE, true))
        assertEquals(Rutas.PANEL, destinoInicio(false, false, Rol.ADMIN, true))
    }

    @Test fun finalizarCargaRespetaLaIntencionDeAcceso() {
        assertEquals(Rutas.SPLASH, destinoInicio(true, false, null, false))
        assertEquals(Rutas.BIENVENIDA, destinoInicio(false, false, null, false))
        assertEquals(Rutas.LOGIN, destinoInicio(false, false, null, true))
    }
}
