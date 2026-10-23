package com.espiritu.ayudacampus.ui.cliente

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.espiritu.ayudacampus.model.Prioridad
import com.espiritu.ayudacampus.ui.components.*
import com.espiritu.ayudacampus.viewmodel.NuevoTicketViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NuevoTicketScreen(vm: NuevoTicketViewModel, onCreado: (Long) -> Unit) {
    val estado by vm.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(estado.ticketCreado) { estado.ticketCreado?.let(onCreado) }
    val editable = !estado.enviando && estado.ticketCreado == null
    Scaffold(topBar = { AppTopBar("Nuevo ticket") }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding()
            .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Reporta una incidencia", style = MaterialTheme.typography.headlineSmall)
            Text("Cuéntanos qué ocurre para que podamos ayudarte.")
            when {
                estado.cargando -> { LinearProgressIndicator(Modifier.fillMaxWidth()); Text("Cargando categorías y disponibilidad…") }
                estado.errorCarga != null -> {
                    Text(estado.errorCarga.orEmpty(), color = MaterialTheme.colorScheme.error)
                    AyudaButton("Reintentar carga", vm::cargarDatos, enabled = editable)
                }
                !estado.recibirTickets -> Text("Mesa de ayuda cerrada", color = MaterialTheme.colorScheme.error)
            }
            Text("Categoría del problema *", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                estado.categorias.forEach { categoria ->
                    FilterChip(selected = estado.categoriaId == categoria.id,
                        onClick = { vm.seleccionarCategoria(categoria.id) }, enabled = editable && !estado.cargando,
                        label = { Text(categoria.nombre) },
                        leadingIcon = { Icon(iconoCategoria(categoria.icono), null, Modifier.size(18.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer))
                }
            }
            if (!estado.cargando && estado.errorCarga == null && estado.categorias.isEmpty()) {
                Text("No hay categorías activas disponibles.")
            }
            estado.errorCategoria?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            AyudaTextField(estado.titulo, vm::cambiarTitulo, "Título del problema *",
                Modifier.fillMaxWidth(), enabled = editable, error = estado.errorTitulo)
            AyudaTextField(estado.descripcion, vm::cambiarDescripcion, "Descripción detallada *",
                Modifier.fillMaxWidth(), enabled = editable, singleLine = false, minLines = 3,
                error = estado.errorDescripcion)
            AyudaTextField(estado.ubicacion, vm::cambiarUbicacion, "Ubicación (opcional)",
                Modifier.fillMaxWidth(), enabled = editable)
            Text("Prioridad", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Prioridad.entries.forEach { prioridad ->
                    FilterChip(selected = estado.prioridad == prioridad, onClick = { vm.cambiarPrioridad(prioridad) },
                        enabled = editable, label = { Text(when (prioridad) {
                            Prioridad.BAJA -> "Baja"; Prioridad.MEDIA -> "Media"; Prioridad.ALTA -> "Alta"
                        }) }, colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer))
                }
            }
            estado.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            AyudaButton(if (estado.enviando) "Enviando…" else "Enviar ticket", vm::enviar,
                Modifier.fillMaxWidth(), enabled = editable && !estado.cargando && estado.errorCarga == null && estado.recibirTickets)
        }
    }
}
