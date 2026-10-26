package com.espiritu.ayudacampus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.espiritu.ayudacampus.data.local.relation.TicketConDetalle
import com.espiritu.ayudacampus.ui.theme.colorPrioridad
import java.text.DateFormat
import java.util.Date

@Composable
fun TicketCard(detalle: TicketConDetalle, onClick: () -> Unit, modifier: Modifier = Modifier, mostrarCliente: Boolean = false) {
    val ticket = detalle.ticket
    val icono = iconoCategoria(detalle.categoria.icono)
    Card(onClick = onClick, modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(Modifier.width(5.dp).fillMaxHeight().background(colorPrioridad(ticket.prioridad)))
            Column(Modifier.weight(1f).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("#${ticket.id}", style = MaterialTheme.typography.labelMedium)
                if (mostrarCliente) Text("Estudiante: ${detalle.cliente.nombre}", style = MaterialTheme.typography.bodyMedium)
                Text(ticket.titulo, style = MaterialTheme.typography.titleMedium,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(icono, contentDescription = null, modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary)
                    Text(detalle.categoria.nombre, style = MaterialTheme.typography.bodyMedium)
                }
                if (ticket.ubicacion.isNotBlank()) Text(ticket.ubicacion, style = MaterialTheme.typography.bodySmall)
                Text(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(ticket.fecha)),
                    style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EstadoChip(ticket.estado)
                    PrioridadChip(ticket.prioridad)
                }
            }
        }
    }
}
