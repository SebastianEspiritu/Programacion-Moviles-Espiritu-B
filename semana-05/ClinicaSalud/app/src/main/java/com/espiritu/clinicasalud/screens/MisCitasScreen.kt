package com.espiritu.clinicasalud.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.espiritu.clinicasalud.model.Cita
import com.espiritu.clinicasalud.model.EstadoCita

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisCitasScreen(
    citas: List<Cita>,
    onMenuClick: () -> Unit,
    onCancelarCita: (Cita) -> Unit
) {
    // Estados locales para el AlertDialog de cancelación
    var citaACancelar by remember { mutableStateOf<Cita?>(null) }
    var mostrarDialogo by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis citas") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (citas.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aún no tienes citas agendadas")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(citas) { cita ->
                        TarjetaCitaItem(
                            cita = cita,
                            onCancelarClick = {
                                citaACancelar = cita
                                mostrarDialogo = true
                            }
                        )
                    }
                }
            }

            // Diálogo de confirmación para cancelar cita
            if (mostrarDialogo && citaACancelar != null) {
                AlertDialog(
                    onDismissRequest = {
                        mostrarDialogo = false
                        citaACancelar = null
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    title = {
                        Text(text = "Cancelar Cita Médica", fontWeight = FontWeight.Bold)
                    },
                    text = {
                        Text(
                            "¿Estás seguro de que deseas cancelar la cita con el/la ${citaACancelar?.medico?.nombre} " +
                                    "programada para el ${citaACancelar?.fecha} a las ${citaACancelar?.hora}?"
                        )
                    },
                    confirmButton = {
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            onClick = {
                                citaACancelar?.let { onCancelarCita(it) }
                                mostrarDialogo = false
                                citaACancelar = null
                            }
                        ) {
                            Text("Sí, cancelar")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = {
                                mostrarDialogo = false
                                citaACancelar = null
                            }
                        ) {
                            Text("Mantener cita")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun TarjetaCitaItem(
    cita: Cita,
    onCancelarClick: () -> Unit
) {
    val (colorFondo, colorTexto, textoEstado) = when (cita.estado) {
        EstadoCita.CONFIRMADA -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Confirmada")
        EstadoCita.COMPLETADA -> Triple(Color(0xFFE3F2FD), Color(0xFF1565C0), "Completada")
        EstadoCita.CANCELADA -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "Cancelada")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cita.medico.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${cita.medico.especialidad} • ${cita.fecha}, ${cita.hora}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = colorFondo,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = textoEstado,
                        color = colorTexto,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Mostrar botón únicamente si la cita está en estado CONFIRMADA
            if (cita.estado == EstadoCita.CONFIRMADA) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onCancelarClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cancelar Cita")
                }
            }
        }
    }
}