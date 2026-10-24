package com.espiritu.ayudacampus.ui.soporte

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.espiritu.ayudacampus.model.EstadoTicket
import com.espiritu.ayudacampus.ui.components.*
import com.espiritu.ayudacampus.viewmodel.BandejaViewModel

@Composable
fun BandejaScreen(vm: BandejaViewModel, onAtender: (Int) -> Unit) {
    val estado by vm.uiState.collectAsStateWithLifecycle()
    val filtro by vm.estado.collectAsStateWithLifecycle()
    val busqueda by vm.busqueda.collectAsStateWithLifecycle()
    val soloAlertas by vm.soloAlertas.collectAsStateWithLifecycle()
    Scaffold(topBar = { AppTopBar("Bandeja de tickets") }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            if (soloAlertas) {
                Text("ALTA · Abiertos desde hace más de 1 hora", style = MaterialTheme.typography.titleSmall)
                TextButton(onClick = vm::mostrarTodos) { Text("Mostrar todos los tickets") }
            }
            AyudaTextField(busqueda, vm::buscar, "Buscar por número o título", Modifier.fillMaxWidth())
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(null to "Todos", EstadoTicket.ABIERTO to "Abiertos", EstadoTicket.EN_PROCESO to "En proceso",
                    EstadoTicket.RESUELTO to "Resueltos").forEach { (valor, texto) ->
                    FilterChip(selected = filtro == valor, onClick = { vm.filtrar(valor) }, label = { Text(texto) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer))
                }
            }
            when {
                estado.cargando -> Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                estado.error != null -> Column(Modifier.padding(24.dp)) {
                    Text(estado.error.orEmpty(), color = MaterialTheme.colorScheme.error)
                    AyudaButton("Reintentar", vm::reintentar)
                }
                estado.tickets.isEmpty() -> Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text("No hay tickets con estos filtros.")
                }
                else -> LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(estado.tickets, key = { it.ticket.id }) { detalle ->
                        TicketCard(detalle, { onAtender(detalle.ticket.id) }, mostrarCliente = true)
                    }
                }
            }
        }
    }
}
