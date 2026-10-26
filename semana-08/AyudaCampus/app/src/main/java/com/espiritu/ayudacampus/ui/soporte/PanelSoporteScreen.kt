package com.espiritu.ayudacampus.ui.soporte

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.espiritu.ayudacampus.model.EstadoTicket
import com.espiritu.ayudacampus.ui.components.*
import com.espiritu.ayudacampus.ui.components.graficos.*
import com.espiritu.ayudacampus.viewmodel.PanelSoporteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanelSoporteScreen(vm: PanelSoporteViewModel, onVerAlertas: () -> Unit) {
    val estado by vm.uiState.collectAsStateWithLifecycle()
    val colores = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colores.background,
        topBar = {
            Row(
                Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars)
                    .padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Panel de soporte", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colores.onBackground)
                    Spacer(Modifier.height(4.dp))
                    Text("Mesa de ayuda TI \u00b7 Hoy", fontSize = 12.sp, color = colores.onSurfaceVariant)
                }
                IconButton(onClick = onVerAlertas) {
                    Icon(Icons.Default.Notifications, "Notificaciones", Modifier.size(22.dp), tint = colores.onBackground)
                }
            }
        }
    ) { padding ->
        when {
            estado.cargando -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            estado.error != null -> Column(Modifier.padding(padding).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(estado.error.orEmpty(), color = colores.error)
                Spacer(Modifier.height(12.dp))
                AyudaButton("Reintentar", vm::reintentar)
            }
            else -> LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tarjetas de contadores (Abiertos, En proceso, Resueltos)
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Contador("Abiertos", estado.conteos[EstadoTicket.ABIERTO] ?: 0, Color(0xFFFFFAED), Color(0xFFE65100), Color(0xFFFFCC80), Modifier.weight(1f))
                        Contador("En proceso", estado.conteos[EstadoTicket.EN_PROCESO] ?: 0, Color(0xFFE3F2FD), Color(0xFF1976D2), Color(0xFF90CAF9), Modifier.weight(1f))
                        Contador("Resueltos", estado.conteos[EstadoTicket.RESUELTO] ?: 0, Color(0xFFF1F8F5), Color(0xFF2E7D32), Color(0xFFA5D6A7), Modifier.weight(1f))
                    }
                }

                // Tarjeta Tiempo Promedio de Resolución (Mejora A)
                item {
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                        border = BorderStroke(1.dp, Color(0xFFE1BEE7))
                    ) {
                        Row(
                            Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFBA68C8)
                            ) {
                                Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Outlined.Schedule,
                                        contentDescription = null,
                                        modifier = Modifier.size(22.dp),
                                        tint = Color.White
                                    )
                                }
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    "Tiempo promedio de resolución",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF4A148C)
                                )
                                Text(
                                    estado.tiempoPromedioFormateado,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6A1B9A)
                                )
                                Text(
                                    "Calculado sobre tickets cerrados (fechaCierre - fecha)",
                                    fontSize = 10.sp,
                                    color = Color(0xFF7B1FA2)
                                )
                            }
                        }
                    }
                }

                if (estado.total == 0) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No hay tickets registrados.", color = colores.onSurfaceVariant)
                        }
                    }
                }

                // Gráfico de dona (Tickets por estado)
                item {
                    SeccionGrafico("Tickets por estado") {
                        GraficoDona(
                            estado.porEstado,
                            listOf(Color(0xFFFFB300), Color(0xFF1976D2), Color(0xFF2E7D32), Color(0xFF78909C))
                        )
                    }
                }

                // Gráfico de barras (Tickets por categoría)
                item {
                    SeccionGrafico("Tickets por categoría") {
                        GraficoBarras(estado.porCategoria)
                    }
                }

                // Alerta de prioridad alta sin atender (ubicada DEBAJO de los dos gráficos, como en la guía)
                if (estado.altasSinAtender > 0) {
                    item {
                        Card(
                            Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                            border = BorderStroke(1.dp, Color(0xFFEF9A9A))
                        ) {
                            Row(
                                Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.Warning, null, tint = Color(0xFFC62828), modifier = Modifier.size(28.dp))
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        "${estado.altasSinAtender} tickets de prioridad Alta",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFC62828)
                                    )
                                    Text(
                                        "sin atender hace más de 1 hora",
                                        fontSize = 11.sp,
                                        color = Color(0xFFB71C1C)
                                    )
                                }
                                Button(
                                    onClick = onVerAlertas,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text("Ver", fontSize = 12.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
private fun Contador(titulo: String, valor: Int, bgColor: Color, textColor: Color, borderColor: Color, modifier: Modifier) {
    Card(
        modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(valor.toString(), color = textColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(titulo, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = textColor)
        }
    }
}

@Composable
private fun SeccionGrafico(titulo: String, contenido: @Composable () -> Unit) {
    val colores = MaterialTheme.colorScheme
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colores.surface),
        border = BorderStroke(.75.dp, colores.outlineVariant)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(titulo, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colores.onSurface)
            contenido()
        }
    }
}
