package com.tecsup.mibodega.ui.cliente.modelo

import androidx.annotation.DrawableRes
import com.tecsup.mibodega.R

data class Producto(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val categoria: String,
    @DrawableRes val imagenRes: Int = R.drawable.ic_launcher_foreground
)
val listaCategorias = listOf(
    "Todos",
    "Abarrotes",
    "Lácteos",
    "Bebidas",
    "Limpieza"
)