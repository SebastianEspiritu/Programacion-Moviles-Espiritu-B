package com.espiritu.tecsupfit.model

data class FitnessClass(
    val id: Int,
    val title: String,
    val timeAndRoom: String,
    val duration: String,
    val description: String,
    val availableSpots: Int,
    val totalSpots: Int
)

data class Reservation(
    val id: Int,
    val className: String,
    val timeAndRoom: String,
    val status: String // "Confirmada" o "Completada"
)