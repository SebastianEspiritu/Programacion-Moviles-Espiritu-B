package com.espiritu.campusgps.data

import com.espiritu.campusgps.model.Posicion

interface UbicacionRepository {
    suspend fun obtenerPosicion(): Result<Posicion>
}
