package com.espiritu.ayudacampus.ui.soporte

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.espiritu.ayudacampus.model.ICONOS_CATEGORIA
import com.espiritu.ayudacampus.ui.components.*
import com.espiritu.ayudacampus.viewmodel.CategoriasViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FormularioCategoriaScreen(vm: CategoriasViewModel, onVolver: () -> Unit, onGuardado: () -> Unit) {
    val estado by vm.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(estado.guardado) { if (estado.guardado) onGuardado() }
    val editable = !estado.procesando && !estado.guardado
    Scaffold(topBar = {
        TopAppBar(title = { Text(if (vm.categoriaId == null) "Nueva categoría" else "Editar categoría") }, navigationIcon = {
            IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver a categorías") }
        })
    }) { padding ->
        when {
            estado.cargando -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            estado.errorCarga != null -> Column(Modifier.padding(padding).padding(24.dp)) {
                Text(estado.errorCarga.orEmpty(), color = MaterialTheme.colorScheme.error)
                AyudaButton("Reintentar", vm::cargar)
            }
            estado.inexistente -> Text("La categoría ya no existe.", Modifier.padding(padding).padding(24.dp))
            else -> Column(Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                AyudaTextField(estado.nombre, vm::cambiarNombre, "Nombre *", Modifier.fillMaxWidth(),
                    enabled = editable, error = estado.errorNombre)
                Text("Icono", style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ICONOS_CATEGORIA.forEach { icono ->
                        FilterChip(selected = estado.icono == icono, onClick = { vm.cambiarIcono(icono) }, enabled = editable,
                            label = { Text(when (icono) { "pc" -> "Equipo"; "code" -> "Software"; "wifi" -> "Red"
                                "key" -> "Cuenta"; "printer" -> "Impresión"; else -> "General" }) },
                            leadingIcon = { Icon(iconoCategoria(icono), null, Modifier.size(18.dp)) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer))
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Activa", Modifier.weight(1f))
                    Switch(checked = estado.activa, onCheckedChange = vm::cambiarActiva, enabled = editable,
                        modifier = Modifier.semantics { contentDescription = "Categoría activa" })
                }
                Text("Vista previa", style = MaterialTheme.typography.titleMedium)
                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.medium) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(iconoCategoria(estado.icono), null)
                        Column { Text(estado.nombre.trim().ifEmpty { "Nombre de la categoría" }); Text(if (estado.activa) "Activa" else "Inactiva") }
                    }
                }
                estado.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                AyudaButton(if (estado.procesando) "Guardando…" else "Guardar categoría", vm::guardarFormulario,
                    Modifier.fillMaxWidth(), enabled = editable)
            }
        }
    }
}
