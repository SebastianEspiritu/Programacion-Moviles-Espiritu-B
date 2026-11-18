package com.espiritu.campusgps.data

import com.espiritu.campusgps.model.Posicion

interface UbicacionRepository {
    suspend fun obtenerPosicion(): Result<Posicion>
    fun playServicesDisponible(): Boolean
}

// getCurrentLocation devolvió null: GPS apagado o ubicación no disponible
class UbicacionNoDisponibleException : Exception("No se pudo obtener la ubicación")
