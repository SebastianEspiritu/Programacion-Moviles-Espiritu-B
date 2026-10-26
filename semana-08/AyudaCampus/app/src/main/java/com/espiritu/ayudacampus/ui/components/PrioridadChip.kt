package com.espiritu.ayudacampus.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.espiritu.ayudacampus.ui.theme.*

@Composable
fun PrioridadChip(prioridad: String) {
    val texto = when (prioridad) { "ALTA" -> "Alta"; "MEDIA" -> "Media"; else -> "Baja" }
    val color = colorPrioridad(prioridad)
    Surface(color = color.copy(alpha = .12f), shape = RoundedCornerShape(8.dp)) {
        Text(texto, Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color, style = MaterialTheme.typography.labelMedium)
    }
}
