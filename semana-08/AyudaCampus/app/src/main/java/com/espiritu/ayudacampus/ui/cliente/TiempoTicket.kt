package com.espiritu.ayudacampus.ui.cliente

/** Antig?edad desde la fecha de creaci?n persistida; no estima tiempos de atenci?n. */
internal fun tiempoRelativoTicket(fecha: Long, ahora: Long): String {
    val minutos = ((ahora - fecha).coerceAtLeast(0) / 60_000)
    return when {
        minutos < 1 -> "Ahora"
        minutos < 60 -> "Hace $minutos min"
        minutos < 1_440 -> "Hace ${minutos / 60} h"
        else -> "Hace ${minutos / 1_440} d"
    }
}
