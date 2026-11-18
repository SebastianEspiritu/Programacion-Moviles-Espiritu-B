package com.espiritu.centrotareas.data

import kotlinx.coroutines.flow.Flow

interface TareasRepository {
    suspend fun contarPrimos(hasta: Int): Int
    suspend fun cargarPerfil(): String
    suspend fun cargarPedidos(simularError: Boolean): List<String>

    // Mejora IA: la tarea simulada vive en el Repository y emite su progreso (0f a 1f)
    fun tareaConProgreso(): Flow<Float>
}
