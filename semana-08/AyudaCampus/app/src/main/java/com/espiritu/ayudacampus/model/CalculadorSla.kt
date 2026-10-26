package com.espiritu.ayudacampus.model

enum class EstadoSla {
    EN_PLAZO,
    VENCIDO,
    CUMPLIDO
}

data class InfoSla(
    val estadoSla: EstadoSla,
    val plazoMs: Long,
    val transcurridoMs: Long,
    val restanteMs: Long,
    val texto: String
)

object CalculadorSla {
    const val SLA_ALTA_MS = 4 * 3_600_000L  // 4 horas
    const val SLA_MEDIA_MS = 24 * 3_600_000L // 24 horas
    const val SLA_BAJA_MS = 72 * 3_600_000L  // 72 horas

    fun obtenerPlazoMs(prioridad: String?): Long = when (prioridad?.uppercase()) {
        "ALTA" -> SLA_ALTA_MS
        "BAJA" -> SLA_BAJA_MS
        else -> SLA_MEDIA_MS
    }

    fun calcularSla(
        prioridad: String?,
        fechaCreacion: Long,
        fechaCierre: Long?,
        estadoTicket: String?,
        ahora: Long = System.currentTimeMillis()
    ): InfoSla {
        val plazoMs = obtenerPlazoMs(prioridad)
        val esCerrado = estadoTicket == EstadoTicket.CERRADO.name

        val referencia = if (esCerrado && fechaCierre != null && fechaCierre >= fechaCreacion) {
            fechaCierre
        } else {
            ahora.coerceAtLeast(fechaCreacion)
        }

        val transcurridoMs = (referencia - fechaCreacion).coerceAtLeast(0)
        val restanteMs = plazoMs - transcurridoMs

        val (estadoSla, texto) = when {
            esCerrado -> {
                if (transcurridoMs <= plazoMs) {
                    EstadoSla.CUMPLIDO to "SLA cumplido (${formatearDuracionSla(transcurridoMs)})"
                } else {
                    EstadoSla.VENCIDO to "Fuera de SLA (+${formatearDuracionSla(transcurridoMs - plazoMs)})"
                }
            }
            restanteMs > 0 -> {
                EstadoSla.EN_PLAZO to "SLA: ${formatearDuracionSla(restanteMs)} restantes"
            }
            else -> {
                EstadoSla.VENCIDO to "Vencido hace ${formatearDuracionSla(-restanteMs)}"
            }
        }

        return InfoSla(
            estadoSla = estadoSla,
            plazoMs = plazoMs,
            transcurridoMs = transcurridoMs,
            restanteMs = restanteMs,
            texto = texto
        )
    }

    fun formatearDuracionSla(ms: Long): String {
        val minTotales = (ms.coerceAtLeast(0)) / 60_000
        if (minTotales < 1) return "< 1 min"
        val horas = minTotales / 60
        val min = minTotales % 60
        val dias = horas / 24
        val horasRestantes = horas % 24

        return when {
            dias > 0 && horasRestantes > 0 -> "$dias d $horasRestantes h"
            dias > 0 -> "$dias d"
            horas > 0 && min > 0 -> "$horas h $min min"
            horas > 0 -> "$horas h"
            else -> "$min min"
        }
    }
}
