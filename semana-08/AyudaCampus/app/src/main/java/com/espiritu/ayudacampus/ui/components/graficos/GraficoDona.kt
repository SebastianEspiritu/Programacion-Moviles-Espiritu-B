package com.espiritu.ayudacampus.ui.components.graficos

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.espiritu.ayudacampus.model.DatoGrafico

@Composable
fun GraficoDona(datos: List<DatoGrafico>, colores: List<Color>, modifier: Modifier = Modifier) {
    require(colores.isNotEmpty())
    val total = datos.sumOf { it.valor }
    val resumen = datos.joinToString { "${it.etiqueta}: ${it.valor}" }
    val neutro = MaterialTheme.colorScheme.outlineVariant

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(130.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.size(130.dp).semantics { contentDescription = "Tickets por estado. $resumen" }) {
                val ancho = 20.dp.toPx()
                val lado = size.minDimension - ancho
                val origen = Offset((size.width - lado) / 2, (size.height - lado) / 2)
                if (total == 0) {
                    drawArc(neutro, 0f, 360f, false, origen, Size(lado, lado), style = Stroke(ancho))
                } else {
                    var inicio = -90f
                    datos.forEachIndexed { indice, dato ->
                        val angulo = 360f * dato.valor / total
                        if (angulo > 0) {
                            drawArc(
                                colores[indice % colores.size],
                                inicio,
                                angulo,
                                false,
                                origen,
                                Size(lado, lado),
                                style = Stroke(ancho)
                            )
                        }
                        inicio += angulo
                    }
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(total.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Text("tickets", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Column(
            modifier = Modifier.weight(1f).padding(start = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            datos.forEachIndexed { indice, dato ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(Modifier.size(10.dp).background(colores[indice % colores.size], CircleShape))
                    Text(
                        text = dato.etiqueta,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = dato.valor.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
