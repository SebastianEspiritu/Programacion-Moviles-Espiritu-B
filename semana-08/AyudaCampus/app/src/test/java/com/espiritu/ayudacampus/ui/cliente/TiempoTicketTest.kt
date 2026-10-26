package com.espiritu.ayudacampus.ui.cliente

import org.junit.Assert.assertEquals
import org.junit.Test

class TiempoTicketTest {
    private val ahora = 1_800_000_000_000L

    @Test fun menosDeUnMinutoSeMuestraComoAhora() {
        assertEquals("Ahora", tiempoRelativoTicket(ahora, ahora))
        assertEquals("Ahora", tiempoRelativoTicket(ahora - 59_999, ahora))
    }

    @Test fun fechaFuturaNoProduceAntiguedadNegativa() {
        assertEquals("Ahora", tiempoRelativoTicket(ahora + 60_000, ahora))
    }

    @Test fun minutosSeCalculanDesdeLaFechaReal() {
        assertEquals("Hace 1 min", tiempoRelativoTicket(ahora - 60_000, ahora))
        assertEquals("Hace 59 min", tiempoRelativoTicket(ahora - 3_599_999, ahora))
    }

    @Test fun unaHoraCambiaLaUnidad() {
        assertEquals("Hace 1 h", tiempoRelativoTicket(ahora - 3_600_000, ahora))
        assertEquals("Hace 23 h", tiempoRelativoTicket(ahora - 86_399_999, ahora))
    }

    @Test fun diasRepresentanPeriodosCompletosDeVeinticuatroHoras() {
        assertEquals("Hace 1 d", tiempoRelativoTicket(ahora - 86_400_000, ahora))
        assertEquals("Hace 3 d", tiempoRelativoTicket(ahora - 259_200_000, ahora))
    }

    @Test fun avanzarElRelojActualizaLaAntiguedadSinCambiarLaFecha() {
        val fecha = ahora - 59_000
        assertEquals("Ahora", tiempoRelativoTicket(fecha, ahora))
        assertEquals("Hace 1 min", tiempoRelativoTicket(fecha, ahora + 1_000))
        assertEquals("Hace 1 h", tiempoRelativoTicket(fecha, ahora + 3_541_000))
    }
}
