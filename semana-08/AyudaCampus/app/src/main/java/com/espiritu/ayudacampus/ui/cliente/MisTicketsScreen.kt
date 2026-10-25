package com.espiritu.ayudacampus.ui.cliente

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.espiritu.ayudacampus.model.EstadoTicket
import com.espiritu.ayudacampus.R
import com.espiritu.ayudacampus.ui.components.*
import com.espiritu.ayudacampus.viewmodel.MisTicketsViewModel

@Composable
fun MisTicketsScreen(vm: MisTicketsViewModel, onNuevo: () -> Unit, onDetalle: (Int) -> Unit) {
    val estado by vm.uiState.collectAsStateWithLifecycle()
    val busqueda by vm.busqueda.collectAsStateWithLifecycle()
    val filtro by vm.estado.collectAsStateWithLifecycle()
    Scaffold(
        topBar = { AppTopBar("Mis tickets") },
        floatingActionButton = {
            FloatingActionButton(onClick = onNuevo) { Icon(Icons.Default.Add, "Crear ticket") }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            Text("Hola, ${vm.usuario.nombre}", style = MaterialTheme.typography.headlineSmall)
            Text(if (estado.cargando) "Cargando tus tickets…" else if (estado.error != null)
                "Consulta tus incidencias" else pluralStringResource(R.plurals.tickets_abiertos, estado.abiertos, estado.abiertos),
                style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(12.dp))
            AyudaTextField(busqueda, vm::buscar, "Buscar por número o título", Modifier.fillMaxWidth())
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(null to "Todos", EstadoTicket.ABIERTO to "Abiertos",
                    EstadoTicket.EN_PROCESO to "En proceso", EstadoTicket.RESUELTO to "Resueltos").forEach { (valor, etiqueta) ->
                    FilterChip(selected = filtro == valor, onClick = { vm.filtrar(valor) }, label = { Text(etiqueta) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer))
                }
            }
            when {
                estado.cargando -> Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                estado.error != null -> Column(Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(estado.error.orEmpty(), color = MaterialTheme.colorScheme.error)
                    AyudaButton("Reintentar", vm::reintentar)
                }
                estado.tickets.isEmpty() -> Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text("No tienes tickets aquí")
                }
                else -> LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp)) {
                    items(estado.tickets, key = { it.ticket.id }) { ticket ->
                        TicketCard(ticket, { onDetalle(ticket.ticket.id) })
                    }
                }
            }
        }
    }
}
