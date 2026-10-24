package com.espiritu.ayudacampus.ui.soporte

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.espiritu.ayudacampus.data.local.entity.Categoria
import com.espiritu.ayudacampus.ui.components.*
import com.espiritu.ayudacampus.viewmodel.CategoriasViewModel

@Composable
fun CategoriasScreen(vm: CategoriasViewModel, onEditar: (Int) -> Unit, onNueva: () -> Unit) {
    val estado by vm.uiState.collectAsStateWithLifecycle()
    var pendiente by remember { mutableStateOf<Categoria?>(null) }
    Scaffold(topBar = { AppTopBar("Categorías") }, floatingActionButton = {
        FloatingActionButton(onClick = onNueva) { Icon(Icons.Default.Add, "Crear categoría") }
    }) { padding ->
        when {
            estado.cargando -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            estado.errorCarga != null -> Column(Modifier.padding(padding).padding(24.dp)) {
                Text(estado.errorCarga.orEmpty(), color = MaterialTheme.colorScheme.error)
                AyudaButton("Reintentar", vm::cargar)
            }
            else -> LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Column {
                        Text("Administra las categorías de incidencias.")
                        estado.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        estado.mensaje?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                        if (estado.procesando) LinearProgressIndicator(Modifier.fillMaxWidth())
                    }
                }
                if (estado.categorias.isEmpty()) item { Text("No hay categorías registradas.") }
                items(estado.categorias, key = { it.id }) { categoria ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(iconoCategoria(categoria.icono), null, tint = MaterialTheme.colorScheme.primary)
                                Text(categoria.nombre, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                                Switch(checked = categoria.activa, onCheckedChange = { vm.alternar(categoria) }, enabled = !estado.procesando,
                                    modifier = Modifier.semantics { contentDescription = "Disponibilidad de ${categoria.nombre}" })
                            }
                            Text(if (categoria.activa) "Activa" else "Inactiva")
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { onEditar(categoria.id) }, enabled = !estado.procesando) { Text("Editar") }
                                TextButton(onClick = { pendiente = categoria }, enabled = !estado.procesando) { Text("Eliminar") }
                            }
                        }
                    }
                }
            }
        }
    }
    pendiente?.let { categoria ->
        AlertDialog(onDismissRequest = { pendiente = null }, title = { Text("Eliminar categoría") },
            text = { Text("¿Eliminar «${categoria.nombre}»? Solo es posible si no tiene tickets asociados.") },
            confirmButton = { TextButton(onClick = { pendiente = null; vm.eliminar(categoria) }) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = { pendiente = null }) { Text("Cancelar") } })
    }
}
