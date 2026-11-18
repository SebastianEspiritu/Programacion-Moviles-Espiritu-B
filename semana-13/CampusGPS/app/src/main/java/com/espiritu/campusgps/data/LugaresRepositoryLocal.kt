package com.espiritu.campusgps.data

import com.espiritu.campusgps.model.Lugar

// Datos ficticios: 5 lugares cerca del punto base -12.0464, -77.0428 (Lima)
class LugaresRepositoryLocal : LugaresRepository {

    private val lugares = listOf(
        Lugar(1, "Biblioteca Central", "Estudio", -12.0460, -77.0435),
        Lugar(2, "Cafetería Campus", "Comida", -12.0470, -77.0420),
        Lugar(3, "Laboratorio de Cómputo", "Estudio", -12.0455, -77.0415),
        Lugar(4, "Auditorio Principal", "Eventos", -12.0480, -77.0440),
        Lugar(5, "Estacionamiento Norte", "Servicios", -12.0440, -77.0430)
    )

    override fun obtenerLugares(): List<Lugar> = lugares
}
