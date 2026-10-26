package com.espiritu.ayudacampus.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.espiritu.ayudacampus.data.local.entity.Comentario
import java.text.DateFormat
import java.util.Date

@Composable
fun BurbujaComentario(comentario: Comentario, propio: Boolean) {
    val colores = MaterialTheme.colorScheme
    val hora = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(comentario.fecha))
    val etiquetaAutor = if (propio) "Tú · $hora" else "${if (comentario.autorRol == "ADMIN") "Soporte TI" else comentario.autorNombre} · $hora"
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (propio) Arrangement.End else Arrangement.Start) {
        Surface(
            Modifier.fillMaxWidth(.88f),
            shape = MaterialTheme.shapes.medium,
            color = if (propio) colores.primaryContainer else colores.surfaceVariant
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    etiquetaAutor,
                    style = MaterialTheme.typography.labelMedium,
                    fontSize = 11.sp,
                    color = if (propio) colores.onPrimaryContainer else colores.onSurfaceVariant
                )
                Text(
                    comentario.texto,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (propio) colores.onPrimaryContainer else colores.onSurface
                )
            }
        }
    }
}