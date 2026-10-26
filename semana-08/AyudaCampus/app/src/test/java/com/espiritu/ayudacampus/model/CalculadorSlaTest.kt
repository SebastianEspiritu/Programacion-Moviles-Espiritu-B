package com.espiritu.ayudacampus.model

import org.junit.Assert.*
import org.junit.Test

class CalculadorSlaTest {

    private val base = 10_000_000L

    @Test
    fun plazosSlaPorPrioridadCorrectos() {
        assertEquals(4 * 3_600_000L, CalculadorSla.obtenerPlazoMs("ALTA"))
        assertEquals(24 * 3_600_000L, CalculadorSla.obtenerPlazoMs("MEDIA"))
        assertEquals(72 * 3_600_000L, CalculadorSla.obtenerPlazoMs("BAJA"))
        assertEquals(24 * 3_600_000L, CalculadorSla.obtenerPlazoMs(null)) // Fallback a MEDIA
        assertEquals(24 * 3_600_000L, CalculadorSla.obtenerPlazoMs("DESCONOCIDO"))
    }

    @Test
    fun ticketAltaEnPlazoMuestraTiempoRestante() {
        // 2 horas después de creación -> quedan 2 horas (de 4h)
        val ahora = base + 2 * 3_600_000L
        val info = CalculadorSla.calcularSla(
            prioridad = "ALTA",
            fechaCreacion = base,
            fechaCierre = null,
            estadoTicket = "ABIERTO",
            ahora = ahora
        )

        assertEquals(EstadoSla.EN_PLAZO, info.estadoSla)
        assertEquals(2 * 3_600_000L, info.restanteMs)
        assertEquals("SLA: 2 h restantes", info.texto)
    }

    @Test
    fun ticketMediaVencidoMuestraTiempoVencido() {
        // 25 horas después de creación -> vencido hace 1 hora (de 24h)
        val ahora = base + 25 * 3_600_000L
        val info = CalculadorSla.calcularSla(
            prioridad = "MEDIA",
            fechaCreacion = base,
            fechaCierre = null,
            estadoTicket = "EN_PROCESO",
            ahora = ahora
        )

        assertEquals(EstadoSla.VENCIDO, info.estadoSla)
        assertTrue(info.restanteMs < 0)
        assertEquals("Vencido hace 1 h", info.texto)
    }

    @Test
    fun ticketBajaCerradoEnPlazoConservaFechaCierreYCumplido() {
        // Creado en base, cerrado a las 50 horas (plazo 72h) -> Cumplido
        val fechaCierre = base + 50 * 3_600_000L
        val ahoraPosterior = base + 200 * 3_600_000L // Reloj actual mucho después
        val info = CalculadorSla.calcularSla(
            prioridad = "BAJA",
            fechaCreacion = base,
            fechaCierre = fechaCierre,
            estadoTicket = "CERRADO",
            ahora = ahoraPosterior
        )

        assertEquals(EstadoSla.CUMPLIDO, info.estadoSla)
        assertEquals(50 * 3_600_000L, info.transcurridoMs)
        assertEquals("SLA cumplido (2 d 2 h)", info.texto)
    }

    @Test
    fun ticketCerradoFueraDeSlaMuestraExceso() {
        // Creado en base, cerrado a las 5 horas (plazo 4h) -> Vencido al cerrar por 1h
        val fechaCierre = base + 5 * 3_600_000L
        val info = CalculadorSla.calcularSla(
            prioridad = "ALTA",
            fechaCreacion = base,
            fechaCierre = fechaCierre,
            estadoTicket = "CERRADO",
            ahora = base + 10 * 3_600_000L
        )

        assertEquals(EstadoSla.VENCIDO, info.estadoSla)
        assertEquals("Fuera de SLA (+1 h)", info.texto)
    }

    @Test
    fun formateaDuracionesCorrectamente() {
        assertEquals("< 1 min", CalculadorSla.formatearDuracionSla(0L))
        assertEquals("30 min", CalculadorSla.formatearDuracionSla(30 * 60_000L))
        assertEquals("1 h 15 min", CalculadorSla.formatearDuracionSla(1 * 3_600_000L + 15 * 60_000L))
        assertEquals("3 d 2 h", CalculadorSla.formatearDuracionSla(74 * 3_600_000L))
    }
}
