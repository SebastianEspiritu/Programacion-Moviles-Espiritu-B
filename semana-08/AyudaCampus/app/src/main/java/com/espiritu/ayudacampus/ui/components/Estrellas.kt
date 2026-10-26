package com.espiritu.ayudacampus.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected

@Composable
fun Estrellas(valor: Int, onSeleccionar: ((Int) -> Unit)? = null, enabled: Boolean = true) {
    val colorEstrellaSeleccionada = Color(0xFFF5B301)
    val colorEstrellaNoSeleccionada = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    Row {
        (1..5).forEach { numero ->
            val seleccionada = numero <= valor
            val tintColor = if (seleccionada) colorEstrellaSeleccionada else colorEstrellaNoSeleccionada
            if (onSeleccionar != null) {
                IconButton(onClick = { onSeleccionar(numero) }, enabled = enabled,
                    modifier = Modifier.semantics { selected = valor == numero }) {
                    Icon(if (seleccionada) Icons.Default.Star else Icons.Default.StarBorder,
                        "Calificar con $numero estrellas", tint = tintColor)
                }
            } else {
                Icon(if (seleccionada) Icons.Default.Star else Icons.Default.StarBorder,
                    if (numero == 1) "Calificación: $valor de 5 estrellas" else null,
                    tint = tintColor)
            }
        }
    }
}
