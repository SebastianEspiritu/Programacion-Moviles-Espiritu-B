package com.espiritu.campusgps.util

import android.location.Location
import com.espiritu.campusgps.model.Lugar
import com.espiritu.campusgps.model.Posicion

// Función pura: distancia en metros desde mi posición hasta un lugar
fun distanciaMetros(desde: Posicion, hasta: Lugar): Float {
    val resultado = FloatArray(1)
    // El orden es (lat1, lon1, lat2, lon2, resultado)
    Location.distanceBetween(
        desde.latitud, desde.longitud,
        hasta.latitud, hasta.longitud,
        resultado
    )
    return resultado[0]
}
