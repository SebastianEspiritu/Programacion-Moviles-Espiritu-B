package com.espiritu.avisoscampus.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.espiritu.avisoscampus.model.Aviso
import com.espiritu.avisoscampus.model.Origen
import com.espiritu.avisoscampus.ui.theme.FilaFondo
import com.espiritu.avisoscampus.ui.theme.OrigenBoton
import com.espiritu.avisoscampus.ui.theme.OrigenModoAvion
import com.espiritu.avisoscampus.ui.theme.OrigenRecordatorio

// Tarjeta "Historial". No conoce el ViewModel: solo recibe la lista.
@Composable
fun HistorialAvisos(avisos: List<Aviso>) {
    Tarjeta(titulo = "Historial") {
        if (avisos.isEmpty()) {
            Text(
                text = "Sin avisos todavía",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            // Column + forEach (no LazyColumn): la pantalla ya se desplaza
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                avisos.take(20).forEach { aviso ->
                    FilaAviso(aviso)
                }
            }
        }
    }
}

@Composable
private fun FilaAviso(aviso: Aviso) {
    Surface(
        color = FilaFondo,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            EtiquetaOrigen(aviso.origen)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = aviso.titulo,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = aviso.texto,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = aviso.hora,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EtiquetaOrigen(origen: Origen) {
    val (fondo, texto) = when (origen) {
        Origen.BOTON -> OrigenBoton to Color.White
        Origen.RECORDATORIO -> OrigenRecordatorio to Color(0xFF3A2C00)
        Origen.MODO_AVION -> OrigenModoAvion to Color.White
    }
    Surface(color = fondo, shape = RoundedCornerShape(50)) {
        Text(
            text = origen.texto,
            color = texto,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
