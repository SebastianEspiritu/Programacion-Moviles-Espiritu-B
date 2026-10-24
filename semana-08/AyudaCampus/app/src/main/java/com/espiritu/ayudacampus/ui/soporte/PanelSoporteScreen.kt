package com.espiritu.ayudacampus.ui.soporte

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.espiritu.ayudacampus.model.EstadoTicket
import com.espiritu.ayudacampus.ui.components.*
import com.espiritu.ayudacampus.ui.components.graficos.*
import com.espiritu.ayudacampus.ui.theme.*
import com.espiritu.ayudacampus.viewmodel.PanelSoporteViewModel

@Composable
fun PanelSoporteScreen(vm: PanelSoporteViewModel, onVerAlertas: () -> Unit) {
    val estado by vm.uiState.collectAsStateWithLifecycle()
    Scaffold(topBar = { AppTopBar("Panel de soporte") }) { padding ->
        when {
            estado.cargando -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            estado.error != null -> Column(Modifier.padding(padding).padding(24.dp)) {
                Text(estado.error.orEmpty(), color = MaterialTheme.colorScheme.error)
                AyudaButton("Reintentar", vm::reintentar)
            }
            else -> LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    Text("Hola, ${vm.usuario.nombre}", style = MaterialTheme.typography.headlineSmall)
                    Text("Resumen de la mesa de ayuda")
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Contador("Abiertos", estado.conteos[EstadoTicket.ABIERTO] ?: 0, EstadoAbierto, Modifier.weight(1f))
                        Contador("En proceso", estado.conteos[EstadoTicket.EN_PROCESO] ?: 0, EstadoEnProceso, Modifier.weight(1f))
                        Contador("Resueltos", estado.conteos[EstadoTicket.RESUELTO] ?: 0, EstadoResuelto, Modifier.weight(1f))
                    }
                }
                item { Text("Cerrados: ${estado.conteos[EstadoTicket.CERRADO] ?: 0} · Total: ${estado.total}") }
                if (estado.altasSinAtender > 0) item {
                    Surface(color = MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.medium) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("${estado.altasSinAtender} tickets de prioridad ALTA sin atención", style = MaterialTheme.typography.titleMedium)
                            Text("Abiertos desde hace más de 1 hora.")
                            AyudaButton("Ver", onVerAlertas)
                        }
                    }
                }
                if (estado.total == 0) item { Text("No hay tickets registrados.") }
                item {
                    SeccionGrafico("Tickets por estado") {
                        GraficoDona(estado.porEstado, listOf(EstadoAbierto, EstadoEnProceso, EstadoResuelto, EstadoCerrado))
                    }
                }
                item { SeccionGrafico("Tickets por categoría") { GraficoBarras(estado.porCategoria) } }
            }
        }
    }
}

@Composable
private fun Contador(titulo: String, valor: Int, color: Color, modifier: Modifier) {
    Surface(modifier, color = color.copy(alpha = .12f), shape = MaterialTheme.shapes.medium) {
        Column(Modifier.padding(10.dp)) {
            Text(valor.toString(), color = color, style = MaterialTheme.typography.headlineMedium)
            Text(titulo, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun SeccionGrafico(titulo: String, contenido: @Composable () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleLarge)
            contenido()
        }
    }
}
