package com.espiritu.ayudacampus.ui.cliente

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.espiritu.ayudacampus.data.local.relation.TicketConDetalle
import com.espiritu.ayudacampus.model.EstadoTicket
import com.espiritu.ayudacampus.R
import com.espiritu.ayudacampus.ui.components.*
import com.espiritu.ayudacampus.ui.theme.colorPrioridad
import com.espiritu.ayudacampus.viewmodel.MisTicketsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MisTicketsScreen(vm: MisTicketsViewModel, onNuevo: () -> Unit, onDetalle: (Int) -> Unit) {
    val estado by vm.uiState.collectAsStateWithLifecycle()
    val busqueda by vm.busqueda.collectAsStateWithLifecycle()
    val filtro by vm.estado.collectAsStateWithLifecycle()
    val colores = MaterialTheme.colorScheme
    val avisos = remember { SnackbarHostState() }
    val alcance = rememberCoroutineScope()
    val ahora by produceState(System.currentTimeMillis()) {
        while (true) { delay(60_000); value = System.currentTimeMillis() }
    }
    val resumen = when {
        estado.cargando -> "Cargando tus tickets\u2026"
        estado.error != null -> "Consulta tus incidencias"
        else -> pluralStringResource(R.plurals.tickets_abiertos, estado.abiertos, estado.abiertos)
    }
    Scaffold(containerColor = colores.background,
        snackbarHost = { SnackbarHost(avisos) },
        topBar = {
            Row(Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars)
                .padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Mis tickets", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("Hola, ${vm.usuario.nombre.substringBefore(' ')} \u00b7 $resumen",
                        fontSize = 12.sp, color = colores.onSurfaceVariant)
                }
                IconButton(onClick = { alcance.launch {
                    avisos.showSnackbar("Los avisos de notificaciones todav\u00eda no est\u00e1n disponibles.")
                } }) { Icon(Icons.Default.Notifications, "Notificaciones", Modifier.size(22.dp)) }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNuevo, shape = RoundedCornerShape(22.dp),
                containerColor = colores.primary, contentColor = colores.onPrimary) {
                Icon(Icons.Default.Add, "Crear ticket", Modifier.size(28.dp))
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
            OutlinedTextField(value = busqueda, onValueChange = vm::buscar,
                placeholder = { Text("Buscar por t\u00edtulo o n\u00famero\u2026", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Outlined.Search, null, Modifier.size(20.dp)) },
                singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                textStyle = MaterialTheme.typography.bodyMedium,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = superficieEstudiante(), focusedContainerColor = superficieEstudiante(),
                    unfocusedBorderColor = colores.outlineVariant))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(null to "Todos", EstadoTicket.ABIERTO to "Abiertos",
                    EstadoTicket.EN_PROCESO to "En proceso", EstadoTicket.RESUELTO to "Resueltos").forEach { (valor, etiqueta) ->
                    FilterChip(selected = filtro == valor, onClick = { vm.filtrar(valor) },
                        shape = RoundedCornerShape(20.dp), label = { Text(etiqueta, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = superficieEstudiante(), selectedContainerColor = colores.primary,
                            selectedLabelColor = colores.onPrimary))
                }
            }
            when {
                estado.cargando -> Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                estado.error != null -> Column(Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(estado.error.orEmpty(), color = colores.error)
                    AyudaButton("Reintentar", vm::reintentar)
                }
                estado.tickets.isEmpty() -> Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text("No tienes tickets aqu\u00ed")
                }
                else -> LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp)) {
                    items(estado.tickets, key = { it.ticket.id }) { ticket ->
                        TarjetaTicketEstudiante(ticket, ahora) { onDetalle(ticket.ticket.id) }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TarjetaTicketEstudiante(detalle: TicketConDetalle, ahora: Long, onClick: () -> Unit) {
    val ticket = detalle.ticket
    val colores = MaterialTheme.colorScheme
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = superficieEstudiante()),
        border = BorderStroke(.75.dp, colores.outlineVariant)) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(Modifier.width(5.dp).fillMaxHeight().background(colorPrioridad(ticket.prioridad)))
            Column(Modifier.weight(1f).padding(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(color = colores.primaryContainer, shape = RoundedCornerShape(10.dp)) {
                        Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                            Icon(iconoCategoriaEstudiante(detalle.categoria.icono), detalle.categoria.nombre,
                                Modifier.size(22.dp), tint = colores.primary)
                        }
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(ticket.titulo, fontSize = 13.sp, lineHeight = 17.sp, fontWeight = FontWeight.SemiBold,
                            maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text("#${ticket.id.toString().padStart(4, '0')} \u00b7 ${detalle.categoria.nombre}" +
                            if (ticket.ubicacion.isNotBlank()) " \u00b7 ${ticket.ubicacion}" else "",
                            fontSize = 10.sp, lineHeight = 14.sp, color = colores.onSurfaceVariant,
                            maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    FlowRow(Modifier.weight(1f).padding(start = 46.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        EstadoChip(ticket.estado)
                        PrioridadChip(ticket.prioridad)
                    }
                    Text(tiempoRelativoTicket(ticket.fecha, ahora), Modifier.padding(start = 6.dp, bottom = 4.dp),
                        fontSize = 10.sp, color = colores.onSurfaceVariant)
                }
            }
        }
    }
}
