package com.espiritu.ayudacampus.ui.components.graficos

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.espiritu.ayudacampus.model.DatoGrafico

@Composable
fun GraficoBarras(datos: List<DatoGrafico>, modifier: Modifier = Modifier) {
    val maximo = (datos.maxOfOrNull { it.valor } ?: 0).coerceAtLeast(1)
    val color = MaterialTheme.colorScheme.primary
    val fondo = MaterialTheme.colorScheme.primaryContainer
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (datos.isEmpty()) Text("No hay categorías disponibles.")
        datos.forEach { dato ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(dato.etiqueta, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Text(dato.valor.toString(), style = MaterialTheme.typography.labelLarge)
                }
                Canvas(Modifier.fillMaxWidth().height(16.dp).semantics {
                    contentDescription = "${dato.etiqueta}: ${dato.valor} tickets"
                }) {
                    drawRoundRect(fondo, cornerRadius = CornerRadius(8.dp.toPx()))
                    if (dato.valor > 0) drawRoundRect(color,
                        size = Size(size.width * dato.valor / maximo, size.height), cornerRadius = CornerRadius(8.dp.toPx()))
                }
            }
        }
    }
}
