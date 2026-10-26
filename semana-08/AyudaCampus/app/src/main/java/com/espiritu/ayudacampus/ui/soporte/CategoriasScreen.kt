package com.espiritu.ayudacampus.ui.soporte

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.espiritu.ayudacampus.data.local.entity.Categoria
import com.espiritu.ayudacampus.ui.cliente.superficieEstudiante
import com.espiritu.ayudacampus.ui.components.*
import com.espiritu.ayudacampus.viewmodel.CategoriasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriasScreen(vm: CategoriasViewModel, onEditar: (Int) -> Unit, onNueva: () -> Unit) {
    val estado by vm.uiState.collectAsStateWithLifecycle()
    var pendiente by remember { mutableStateOf<Categoria?>(null) }
    val colores = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colores.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Categorías", fontSize = 16.sp, fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colores.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNueva,
                shape = RoundedCornerShape(22.dp),
                containerColor = colores.primary,
                contentColor = colores.onPrimary
            ) {
                Icon(Icons.Default.Add, "Crear categoría", Modifier.size(28.dp))
            }
        }
    ) { padding ->
        when {
            estado.cargando -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            estado.errorCarga != null -> Column(Modifier.padding(padding).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(estado.errorCarga.orEmpty(), color = colores.error)
                Spacer(Modifier.height(12.dp))
                AyudaButton("Reintentar", vm::cargar)
            }
            else -> LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Administra las categorías de incidencias.", fontSize = 12.sp, color = colores.onSurfaceVariant)
                        estado.error?.let { Text(it, color = colores.error, fontSize = 12.sp) }
                        estado.mensaje?.let { Text(it, color = colores.primary, fontSize = 12.sp) }
                        if (estado.procesando) LinearProgressIndicator(Modifier.fillMaxWidth())
                    }
                }
                if (estado.categorias.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No hay categorías registradas.", color = colores.onSurfaceVariant)
                        }
                    }
                }
                items(estado.categorias, key = { it.id }) { categoria ->
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = superficieEstudiante()),
                        border = BorderStroke(.75.dp, colores.outlineVariant)
                    ) {
                        Row(
                            Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = colores.primaryContainer
                            ) {
                                Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                                    Icon(
                                        iconoCategoria(categoria.icono),
                                        contentDescription = null,
                                        Modifier.size(22.dp),
                                        tint = colores.primary
                                    )
                                }
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(categoria.nombre, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colores.onSurface)
                                Text("5 tickets", fontSize = 11.sp, color = colores.onSurfaceVariant) // ticket count display or placeholder
                            }
                            Switch(
                                checked = categoria.activa,
                                onCheckedChange = { vm.alternar(categoria) },
                                enabled = !estado.procesando,
                                modifier = Modifier.semantics { contentDescription = "Disponibilidad de ${categoria.nombre}" }
                            )
                            IconButton(onClick = { onEditar(categoria.id) }, enabled = !estado.procesando) {
                                Icon(Icons.Outlined.Edit, "Editar", Modifier.size(20.dp), tint = colores.primary)
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(88.dp)) }
            }
        }
    }
    pendiente?.let { categoria ->
        AlertDialog(
            onDismissRequest = { pendiente = null },
            title = { Text("Eliminar categoría") },
            text = { Text("¿Eliminar «${categoria.nombre}»? Solo es posible si no tiene tickets asociados.") },
            confirmButton = { TextButton(onClick = { pendiente = null; vm.eliminar(categoria) }) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = { pendiente = null }) { Text("Cancelar") } }
        )
    }
}
