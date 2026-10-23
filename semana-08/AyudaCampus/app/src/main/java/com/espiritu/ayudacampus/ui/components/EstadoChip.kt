package com.espiritu.ayudacampus.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.espiritu.ayudacampus.ui.theme.*

@Composable
fun EstadoChip(estado: String) {
    val (texto, color) = when (estado) {
        "ABIERTO" -> "Abierto" to EstadoAbierto
        "EN_PROCESO" -> "En proceso" to EstadoEnProceso
        "RESUELTO" -> "Resuelto" to EstadoResuelto
        else -> "Cerrado" to EstadoCerrado
    }
    Surface(color = color.copy(alpha = .12f), shape = RoundedCornerShape(8.dp)) {
        Text(texto, Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color, style = MaterialTheme.typography.labelMedium)
    }
}
