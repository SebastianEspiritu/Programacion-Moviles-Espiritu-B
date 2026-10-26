package com.espiritu.ayudacampus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.espiritu.ayudacampus.model.EstadoTicket

@Composable
fun StepperTicket(estado: String) {
    val pasos = listOf("Abierto", "En proceso", "Resuelto", "Cerrado")
    val actual = EstadoTicket.entries.indexOfFirst { it.name == estado }.coerceAtLeast(0)
    val colores = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        pasos.forEachIndexed { indice, texto ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (indice > 0) {
                            Box(
                                Modifier.weight(1f).height(2.dp)
                                    .background(if (indice <= actual) colores.primary else colores.outlineVariant)
                            )
                        } else {
                            Spacer(Modifier.weight(1f))
                        }
                        Spacer(Modifier.size(28.dp))
                        if (indice < pasos.size - 1) {
                            Box(
                                Modifier.weight(1f).height(2.dp)
                                    .background(if (indice < actual) colores.primary else colores.outlineVariant)
                            )
                        } else {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                    Surface(
                        shape = CircleShape,
                        color = when {
                            indice < actual -> colores.primary
                            indice == actual -> colores.primary
                            else -> colores.surfaceVariant
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (indice < actual) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = colores.onPrimary
                                )
                            } else if (indice == actual) {
                                Box(
                                    Modifier.size(10.dp)
                                        .background(colores.onPrimary, CircleShape)
                                )
                            } else {
                                Text(
                                    "${indice + 1}",
                                    fontSize = 11.sp,
                                    color = colores.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    texto,
                    fontSize = 10.sp,
                    color = if (indice <= actual) colores.primary else colores.onSurfaceVariant
                )
            }
        }
    }
}