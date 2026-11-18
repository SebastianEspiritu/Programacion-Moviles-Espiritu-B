package com.espiritu.campusgps.util

import java.util.Locale
import kotlin.math.roundToInt

// Menos de 1000: metros enteros (350 m). Desde 1000: km con un decimal (1.5 km)
fun formatearDistancia(metros: Float): String =
    if (metros < 1000f) {
        "${metros.roundToInt()} m"
    } else {
        String.format(Locale.US, "%.1f km", metros / 1000f)
    }
