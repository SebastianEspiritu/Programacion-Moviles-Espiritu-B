package com.espiritu.ayudacampus.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.espiritu.ayudacampus.model.EstadoTicket

@Composable
fun StepperTicket(estado: String) {
    val pasos = listOf("Abierto", "En proceso", "Resuelto", "Cerrado")
    val actual = EstadoTicket.entries.indexOfFirst { it.name == estado }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        pasos.forEachIndexed { indice, texto ->
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(color = if (indice <= actual) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small) {
                    Text("${indice + 1}", Modifier.padding(8.dp), color = if (indice <= actual)
                        MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(texto, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
