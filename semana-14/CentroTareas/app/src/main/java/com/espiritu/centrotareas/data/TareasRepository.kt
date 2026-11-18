package com.espiritu.centrotareas.data

import kotlinx.coroutines.flow.Flow

interface TareasRepository {
    // Mejora IA: alAvanzar recibe el porcentaje del cálculo (0f a 1f)
    suspend fun contarPrimos(hasta: Int, alAvanzar: (Float) -> Unit = {}): Int
    suspend fun cargarPerfil(): String
    suspend fun cargarPedidos(simularError: Boolean): List<String>

    // Mejora IA: la tarea simulada vive en el Repository y emite su progreso (0f a 1f)
    fun tareaConProgreso(): Flow<Float>
}
