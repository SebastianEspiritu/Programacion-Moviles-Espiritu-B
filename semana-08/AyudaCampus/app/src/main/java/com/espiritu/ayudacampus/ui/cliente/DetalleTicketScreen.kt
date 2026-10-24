package com.espiritu.ayudacampus.ui.cliente

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.espiritu.ayudacampus.model.EstadoTicket
import com.espiritu.ayudacampus.ui.components.*
import com.espiritu.ayudacampus.ui.theme.EstadoResuelto
import com.espiritu.ayudacampus.viewmodel.DetalleTicketViewModel
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleTicketScreen(vm: DetalleTicketViewModel, onVolver: () -> Unit) {
    val estado by vm.uiState.collectAsStateWithLifecycle()
    val detalle = estado.ticket
    Scaffold(topBar = {
        TopAppBar(title = { Text("Ticket #${vm.ticketId}") }, navigationIcon = {
            IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver a mis tickets") }
        })
    }) { padding ->
        when {
            estado.cargando -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            estado.errorCarga != null -> Column(Modifier.padding(padding).padding(24.dp)) {
                Text(estado.errorCarga.orEmpty(), color = MaterialTheme.colorScheme.error)
                AyudaButton("Reintentar", vm::cargar)
            }
            estado.sinAcceso -> Text("No tienes acceso a este ticket.", Modifier.padding(padding).padding(24.dp))
            estado.inexistente -> Text("El ticket no existe.", Modifier.padding(padding).padding(24.dp))
            detalle != null -> LazyColumn(Modifier.fillMaxSize().padding(padding).imePadding(),
                contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(detalle.ticket.titulo, style = MaterialTheme.typography.headlineSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            EstadoChip(detalle.ticket.estado); PrioridadChip(detalle.ticket.prioridad)
                        }
                        Text(detalle.ticket.descripcion)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(iconoCategoria(detalle.categoria.icono), null, tint = MaterialTheme.colorScheme.primary)
                            Text(detalle.categoria.nombre)
                        }
                        if (detalle.ticket.ubicacion.isNotBlank()) Text("Ubicación: ${detalle.ticket.ubicacion}")
                        Text("Creado: ${fechaDetalle(detalle.ticket.fecha)}", style = MaterialTheme.typography.bodySmall)
                        detalle.ticket.fechaCierre?.let { Text("Cerrado: ${fechaDetalle(it)}", style = MaterialTheme.typography.bodySmall) }
                        StepperTicket(detalle.ticket.estado)
                    }
                }
                item { Text("Conversación", style = MaterialTheme.typography.titleLarge) }
                if (detalle.comentarios.isEmpty()) item { Text("Todavía no hay comentarios.") }
                items(detalle.comentarios, key = { it.id }) { comentario ->
                    BurbujaComentario(comentario, comentario.autorId == vm.usuario.id)
                }
                if (detalle.ticket.estado != EstadoTicket.CERRADO.name) item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AyudaTextField(estado.borrador, vm::cambiarBorrador, "Escribe un comentario",
                            Modifier.fillMaxWidth(), enabled = estado.puedeComentar, singleLine = false, minLines = 2)
                        AyudaButton("Enviar comentario", { vm.comentar() }, Modifier.fillMaxWidth(),
                            enabled = estado.puedeComentar && estado.borrador.isNotBlank())
                    }
                }
                if (detalle.ticket.estado == EstadoTicket.RESUELTO.name) item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(color = EstadoResuelto.copy(alpha = .12f), shape = MaterialTheme.shapes.medium) {
                            Text("Tu ticket está resuelto. Confirma la atención o solicita su reapertura.",
                                Modifier.padding(12.dp), color = EstadoResuelto)
                        }
                        Text("Califica la atención", style = MaterialTheme.typography.titleMedium)
                        Estrellas(estado.calificacion, vm::seleccionarCalificacion, estado.puedeResolver)
                        AyudaTextField(estado.comentarioFinal, vm::cambiarComentarioFinal, "Comentario final (opcional)",
                            Modifier.fillMaxWidth(), enabled = estado.puedeResolver, singleLine = false, minLines = 2)
                        AyudaButton("Confirmar y cerrar", { vm.cerrar() }, Modifier.fillMaxWidth(),
                            enabled = estado.puedeResolver && estado.calificacion in 1..5)
                        OutlinedButton(onClick = vm::reabrir, modifier = Modifier.fillMaxWidth(), enabled = estado.puedeResolver) {
                            Text("Reabrir ticket")
                        }
                    }
                }
                if (detalle.ticket.estado == EstadoTicket.CERRADO.name) item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Ticket cerrado. Ya no se pueden enviar comentarios.")
                        detalle.ticket.calificacion?.let { Estrellas(it) }
                    }
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (estado.procesando) LinearProgressIndicator(Modifier.fillMaxWidth())
                        estado.errorAccion?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        estado.mensaje?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                    }
                }
            }
        }
    }
}

private fun fechaDetalle(fecha: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(fecha))
