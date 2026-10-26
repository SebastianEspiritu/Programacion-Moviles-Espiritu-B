package com.espiritu.ayudacampus.ui.components.graficos

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.espiritu.ayudacampus.model.DatoGrafico

@Composable
fun GraficoBarras(datos: List<DatoGrafico>, modifier: Modifier = Modifier) {
    val maximo = (datos.maxOfOrNull { it.valor } ?: 0).coerceAtLeast(1)
    val colorBarra = MaterialTheme.colorScheme.primary
    val colorFondo = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    val colores = MaterialTheme.colorScheme

    if (datos.isEmpty()) {
        Text("No hay categorías disponibles.", fontSize = 12.sp, color = colores.onSurfaceVariant)
        return
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        datos.forEach { dato ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.width(52.dp)
            ) {
                Text(
                    text = dato.valor.toString(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colores.onSurface
                )
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(100.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Canvas(
                        Modifier
                            .fillMaxSize()
                            .semantics {
                                contentDescription = "${dato.etiqueta}: ${dato.valor} tickets"
                            }
                    ) {
                        drawRoundRect(
                            color = colorFondo,
                            cornerRadius = CornerRadius(6.dp.toPx())
                        )
                        if (dato.valor > 0) {
                            val alturaBarra = size.height * dato.valor / maximo
                            drawRoundRect(
                                color = colorBarra,
                                topLeft = Offset(0f, size.height - alturaBarra),
                                size = Size(size.width, alturaBarra),
                                cornerRadius = CornerRadius(6.dp.toPx())
                            )
                        }
                    }
                }
                Text(
                    text = dato.etiqueta,
                    fontSize = 10.sp,
                    color = colores.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
