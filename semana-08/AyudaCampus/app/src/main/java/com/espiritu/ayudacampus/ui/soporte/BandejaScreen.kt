package com.espiritu.ayudacampus.ui.soporte

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.espiritu.ayudacampus.model.EstadoTicket
import com.espiritu.ayudacampus.model.Prioridad
import com.espiritu.ayudacampus.ui.components.*
import com.espiritu.ayudacampus.ui.theme.colorPrioridad
import com.espiritu.ayudacampus.viewmodel.BandejaViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BandejaScreen(vm: BandejaViewModel, onAtender: (Int) -> Unit) {
    val estado by vm.uiState.collectAsStateWithLifecycle()
    val filtroEstado by vm.estado.collectAsStateWithLifecycle()
    val filtroPrioridad by vm.prioridad.collectAsStateWithLifecycle()
    val filtroCategoriaId by vm.categoriaId.collectAsStateWithLifecycle()
    val busqueda by vm.busqueda.collectAsStateWithLifecycle()
    val soloAlertas by vm.soloAlertas.collectAsStateWithLifecycle()
    val categorias by vm.categorias.collectAsStateWithLifecycle()
    val filtrosActivos by vm.filtrosActivos.collectAsStateWithLifecycle()
    val colores = MaterialTheme.colorScheme
    val ahora by produceState(System.currentTimeMillis()) {
        while (true) { delay(60_000L); value = System.currentTimeMillis() }
    }

    Scaffold(
        containerColor = colores.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Tickets", fontSize = 16.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { if (soloAlertas) vm.mostrarTodos() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", Modifier.size(20.dp))
                    }
                },
                actions = {
                    if (filtrosActivos) {
                        TextButton(onClick = vm::limpiarFiltros) {
                            Text("Restablecer", fontSize = 12.sp, color = colores.primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colores.background)
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (soloAlertas) {
                Surface(
                    color = colores.errorContainer,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ALTA · Abiertos desde hace más de 1 hora", fontSize = 12.sp, color = colores.onErrorContainer)
                        TextButton(onClick = vm::mostrarTodos) { Text("Ver todos", fontSize = 12.sp) }
                    }
                }
            }

            // Campo de búsqueda por texto con botón de limpiar
            OutlinedTextField(
                value = busqueda,
                onValueChange = vm::buscar,
                placeholder = { Text("Buscar por número o título…", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Outlined.Search, null, Modifier.size(20.dp)) },
                trailingIcon = {
                    if (busqueda.isNotEmpty()) {
                        IconButton(onClick = vm::limpiarBusqueda) {
                            Icon(Icons.Default.Clear, "Limpiar búsqueda", Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                textStyle = MaterialTheme.typography.bodyMedium,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = colores.surface,
                    focusedContainerColor = colores.surface,
                    unfocusedBorderColor = colores.outlineVariant
                )
            )

            // Filtros combinados: Estado, Prioridad, Categoría
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Fila 1: Filtro por Estado
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        null to "Todos",
                        EstadoTicket.ABIERTO to "Abiertos",
                        EstadoTicket.EN_PROCESO to "En proceso",
                        EstadoTicket.RESUELTO to "Resueltos"
                    ).forEach { (valor, etiqueta) ->
                        FilterChip(
                            selected = filtroEstado == valor,
                            onClick = { vm.filtrar(valor) },
                            shape = RoundedCornerShape(20.dp),
                            label = { Text(etiqueta, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = colores.surface,
                                selectedContainerColor = colores.primary,
                                selectedLabelColor = colores.onPrimary
                            )
                        )
                    }
                }

                // Fila 2: Filtro por Prioridad (Mejora C)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = filtroPrioridad == null,
                        onClick = { vm.filtrarPrioridad(null) },
                        shape = RoundedCornerShape(20.dp),
                        label = { Text("Prioridad: Todas", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = colores.surface,
                            selectedContainerColor = colores.primaryContainer,
                            selectedLabelColor = colores.onPrimaryContainer
                        )
                    )
                    Prioridad.entries.forEach { prio ->
                        val seleccionada = filtroPrioridad == prio
                        FilterChip(
                            selected = seleccionada,
                            onClick = { vm.filtrarPrioridad(if (seleccionada) null else prio) },
                            shape = RoundedCornerShape(20.dp),
                            label = { Text(prio.name.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = colores.surface,
                                selectedContainerColor = colorPrioridad(prio.name),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Fila 3: Filtro por Categoría (Mejora C)
                if (categorias.isNotEmpty()) {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = filtroCategoriaId == null,
                            onClick = { vm.filtrarCategoria(null) },
                            shape = RoundedCornerShape(20.dp),
                            label = { Text("Categoría: Todas", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = colores.surface,
                                selectedContainerColor = colores.primaryContainer,
                                selectedLabelColor = colores.onPrimaryContainer
                            )
                        )
                        categorias.forEach { cat ->
                            val seleccionada = filtroCategoriaId == cat.id
                            FilterChip(
                                selected = seleccionada,
                                onClick = { vm.filtrarCategoria(if (seleccionada) null else cat.id) },
                                shape = RoundedCornerShape(20.dp),
                                label = { Text(cat.nombre, fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(
                                        iconoCategoria(cat.icono),
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = colores.surface,
                                    selectedContainerColor = colores.primary,
                                    selectedLabelColor = colores.onPrimary,
                                    selectedLeadingIconColor = colores.onPrimary
                                )
                            )
                        }
                    }
                }
            }

            when {
                estado.cargando -> Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                estado.error != null -> Column(
                    Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(estado.error.orEmpty(), color = colores.error)
                    Spacer(Modifier.height(12.dp))
                    AyudaButton("Reintentar", vm::reintentar)
                }
                estado.tickets.isEmpty() -> Column(
                    Modifier.fillMaxWidth().weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("No hay tickets con estos filtros.", color = colores.onSurfaceVariant, fontSize = 13.sp)
                    if (filtrosActivos) {
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = vm::limpiarFiltros) {
                            Text("Restablecer todos los filtros", fontSize = 12.sp, color = colores.primary)
                        }
                    }
                }
                else -> LazyColumn(
                    Modifier.weight(1f),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(estado.tickets, key = { it.ticket.id }) { detalle ->
                        TicketCard(
                            detalle = detalle,
                            onClick = { onAtender(detalle.ticket.id) },
                            mostrarCliente = true,
                            ahora = ahora
                        )
                    }
                }
            }
        }
    }
}
