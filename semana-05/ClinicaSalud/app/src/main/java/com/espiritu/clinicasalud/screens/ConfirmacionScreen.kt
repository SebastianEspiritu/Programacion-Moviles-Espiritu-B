package com.espiritu.clinicasalud.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.espiritu.clinicasalud.model.Cita

@Composable
fun ConfirmacionScreen(
    cita: Cita,
    onVolverInicio: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("¡Cita agendada!", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Médico: ${cita.medico.nombre}")
                Text("Especialidad: ${cita.medico.especialidad}")
                Text("Fecha: ${cita.fecha}")
                Text("Hora: ${cita.hora}")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onVolverInicio, modifier = Modifier.fillMaxWidth()) {
            Text("Volver al inicio")
        }
    }
}
