package com.espiritu.tecsupfit.model

data class Routine(
    val id: Int,
    val title: String,
    val category: String,
    val durationMinutes: Int,
    val calories: Int
)