package com.espiritu.ayudacampus.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.espiritu.ayudacampus.data.local.entity.Comentario
import java.text.DateFormat
import java.util.Date

@Composable
fun BurbujaComentario(comentario: Comentario, propio: Boolean) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (propio) Arrangement.End else Arrangement.Start) {
        Surface(Modifier.fillMaxWidth(.88f), shape = MaterialTheme.shapes.medium,
            color = if (propio) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${comentario.autorNombre} · ${if (comentario.autorRol == "ADMIN") "Soporte TI" else "Estudiante"}",
                    style = MaterialTheme.typography.labelMedium)
                Text(comentario.texto)
                Text(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(comentario.fecha)),
                    style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
