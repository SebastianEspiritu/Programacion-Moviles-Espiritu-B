package com.espiritu.clinicasalud.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.espiritu.clinicasalud.model.Medico

val fechasDisponibles = listOf("Lun 28 Set", "Mar 29 Set", "Mié 30 Set")
val horasDisponibles = listOf("09:00 am", "11:30 am", "03:00 pm")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendarCitaScreen(
    medico: Medico,
    onBack: () -> Unit,
    onConfirmar: (String, String) -> Unit
) {
    var fechaSeleccionada by remember { mutableStateOf(fechasDisponibles.first()) }
    var horaSeleccionada by remember { mutableStateOf(horasDisponibles.first()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agendar cita") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Cita con ${medico.nombre}", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            Text("Elige una fecha", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(fechasDisponibles) { fecha ->
                    FilterChip(
                        selected = fecha == fechaSeleccionada,
                        onClick = { fechaSeleccionada = fecha },
                        label = { Text(fecha) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Elige una hora", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(horasDisponibles) { hora ->
                    FilterChip(
                        selected = hora == horaSeleccionada,
                        onClick = { horaSeleccionada = hora },
                        label = { Text(hora) }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { onConfirmar(fechaSeleccionada, horaSeleccionada) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirmar cita")
            }
        }
    }
}
