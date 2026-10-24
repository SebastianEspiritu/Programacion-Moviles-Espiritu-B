package com.espiritu.ayudacampus.ui.soporte

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
import com.espiritu.ayudacampus.model.*
import com.espiritu.ayudacampus.ui.components.*
import com.espiritu.ayudacampus.viewmodel.AtenderViewModel
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AtenderTicketScreen(vm: AtenderViewModel, onVolver: () -> Unit) {
    val estado by vm.uiState.collectAsStateWithLifecycle()
    val detalle = estado.ticket
    val avisos = remember { SnackbarHostState() }
    LaunchedEffect(estado.error, estado.mensaje) {
        (estado.error ?: estado.mensaje)?.let { avisos.showSnackbar(it) }
    }
    Scaffold(topBar = {
        TopAppBar(title = { Text("Atender ticket #${vm.ticketId}") }, navigationIcon = {
            IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver a la bandeja") }
        })
    }, snackbarHost = { SnackbarHost(avisos) }) { padding ->
        when {
            estado.cargando -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            estado.errorCarga != null -> Column(Modifier.padding(padding).padding(24.dp)) {
                Text(estado.errorCarga.orEmpty(), color = MaterialTheme.colorScheme.error)
                AyudaButton("Reintentar", vm::cargar)
            }
            estado.inexistente -> Text("El ticket no existe.", Modifier.padding(padding).padding(24.dp))
            detalle != null -> LazyColumn(Modifier.fillMaxSize().padding(padding).imePadding(),
                contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(detalle.ticket.titulo, style = MaterialTheme.typography.headlineSmall)
                        Text("Estudiante: ${detalle.cliente.nombre}")
                        Text(detalle.cliente.correo, style = MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            EstadoChip(detalle.ticket.estado); PrioridadChip(detalle.ticket.prioridad)
                        }
                        Text(detalle.ticket.descripcion)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(iconoCategoria(detalle.categoria.icono), null, tint = MaterialTheme.colorScheme.primary)
                            Text(detalle.categoria.nombre)
                        }
                        if (detalle.ticket.ubicacion.isNotBlank()) Text("Ubicación: ${detalle.ticket.ubicacion}")
                        Text("Creado: ${fechaAtencion(detalle.ticket.fecha)}", style = MaterialTheme.typography.bodySmall)
                        detalle.ticket.fechaCierre?.let { Text("Cerrado: ${fechaAtencion(it)}", style = MaterialTheme.typography.bodySmall) }
                        detalle.ticket.calificacion?.let { Estrellas(it) }
                        StepperTicket(detalle.ticket.estado)
                        if (detalle.ticket.estado == "CERRADO") Text("Ticket cerrado. Las acciones de soporte están bloqueadas.")
                        Text("Estado", style = MaterialTheme.typography.titleMedium)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            EstadoTicket.entries.forEach { valor ->
                                FilterChip(selected = estado.estado == valor, onClick = { vm.cambiarEstado(valor) },
                                    enabled = estado.editable && valor != EstadoTicket.CERRADO,
                                    label = { Text(when (valor) {
                                        EstadoTicket.ABIERTO -> "Abierto"; EstadoTicket.EN_PROCESO -> "En proceso"
                                        EstadoTicket.RESUELTO -> "Resuelto"; EstadoTicket.CERRADO -> "Cerrado"
                                    }) }, colors = coloresSelector())
                            }
                        }
                        Text("Prioridad", style = MaterialTheme.typography.titleMedium)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Prioridad.entries.forEach { valor ->
                                FilterChip(selected = estado.prioridad == valor, onClick = { vm.cambiarPrioridad(valor) }, enabled = estado.editable,
                                    label = { Text(when (valor) { Prioridad.BAJA -> "Baja"; Prioridad.MEDIA -> "Media"; Prioridad.ALTA -> "Alta" }) },
                                    colors = coloresSelector())
                            }
                        }
                        AyudaButton("Guardar cambios", vm::guardar, Modifier.fillMaxWidth(),
                            enabled = estado.editable && (estado.estadoEditado || estado.prioridadEditada))
                    }
                }
                item { Text("Conversación", style = MaterialTheme.typography.titleLarge) }
                if (detalle.comentarios.isEmpty()) item { Text("Todavía no hay comentarios.") }
                items(detalle.comentarios, key = { it.id }) { BurbujaComentario(it, it.autorId == vm.usuario.id) }
                if (detalle.ticket.estado != "CERRADO") item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AyudaTextField(estado.respuesta, vm::cambiarRespuesta, "Respuesta de soporte", Modifier.fillMaxWidth(),
                            enabled = estado.editable, singleLine = false, minLines = 2)
                        AyudaButton("Enviar respuesta", { vm.responder() }, Modifier.fillMaxWidth(),
                            enabled = estado.editable && estado.respuesta.isNotBlank())
                    }
                }
                if (estado.procesando) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            }
        }
    }
}

@Composable
private fun coloresSelector() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer)

private fun fechaAtencion(fecha: Long) = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(fecha))
