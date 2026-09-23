package com.espiritu.clinicasalud.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.espiritu.clinicasalud.model.Medico

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilMedicoScreen(
    medico: Medico,
    onBack: () -> Unit,
    onAgendarCita: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil del médico") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(medico.nombre, style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(medico.especialidad, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${medico.calificacion} de calificación")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = medico.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onAgendarCita,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Agendar cita")
            }
        }
    }
}
