package com.espiritu.ayudacampus.ui.soporte

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.espiritu.ayudacampus.model.ICONOS_CATEGORIA
import com.espiritu.ayudacampus.ui.cliente.superficieEstudiante
import com.espiritu.ayudacampus.ui.components.*
import com.espiritu.ayudacampus.viewmodel.CategoriasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioCategoriaScreen(vm: CategoriasViewModel, onVolver: () -> Unit, onGuardado: () -> Unit) {
    val estado by vm.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(estado.guardado) { if (estado.guardado) onGuardado() }
    val editable = !estado.procesando && !estado.guardado
    val colores = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colores.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (vm.categoriaId == null) "Nueva categoría" else "Editar categoría", fontSize = 16.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onVolver, enabled = editable) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver a categorías", Modifier.size(20.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colores.background)
            )
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
            estado.inexistente -> Text("La categoría ya no existe.", Modifier.padding(padding).padding(24.dp))
            else -> Column(
                Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Campo de nombre compacto
                AyudaTextField(
                    estado.nombre,
                    vm::cambiarNombre,
                    "Nombre de la categoría",
                    Modifier.fillMaxWidth(),
                    enabled = editable,
                    error = estado.errorNombre
                )

                Text("Ícono", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colores.onBackground)

                // Cuadrícula de 3 columnas x 2 filas para los 6 iconos
                val filasICONOS = ICONOS_CATEGORIA.chunked(3)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    filasICONOS.forEach { fila ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            fila.forEach { icono ->
                                val seleccionado = estado.icono == icono
                                Surface(
                                    onClick = { if (editable) vm.cambiarIcono(icono) },
                                    enabled = editable,
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (seleccionado) colores.primaryContainer else superficieEstudiante(),
                                    border = BorderStroke(
                                        if (seleccionado) 1.5.dp else 0.75.dp,
                                        if (seleccionado) colores.primary else colores.outlineVariant
                                    ),
                                    modifier = Modifier.weight(1f).height(64.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            iconoCategoria(icono),
                                            contentDescription = null,
                                            Modifier.size(24.dp),
                                            tint = if (seleccionado) colores.primary else colores.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            // Si la última fila tiene menos de 3 elementos, rellenar con spacers para mantener alineación
                            repeat(3 - fila.size) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }

                // Tarjeta Categoría activa
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = superficieEstudiante()),
                    border = BorderStroke(.75.dp, colores.outlineVariant)
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Categoría activa", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colores.onSurface)
                            Text("Los estudiantes podrán elegirla", fontSize = 11.sp, color = colores.onSurfaceVariant)
                        }
                        Switch(
                            checked = estado.activa,
                            onCheckedChange = vm::cambiarActiva,
                            enabled = editable,
                            modifier = Modifier.semantics { contentDescription = "Categoría activa" }
                        )
                    }
                }

                Text("Vista previa", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colores.onBackground)

                // Tarjeta Vista previa
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = superficieEstudiante()),
                    border = BorderStroke(.75.dp, colores.outlineVariant)
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = colores.primaryContainer
                        ) {
                            Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                                Icon(
                                    iconoCategoria(estado.icono),
                                    contentDescription = null,
                                    Modifier.size(22.dp),
                                    tint = colores.primary
                                )
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = estado.nombre.trim().ifEmpty { "Nombre de la categoría" },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colores.onSurface
                            )
                            Text(
                                text = "0 tickets",
                                fontSize = 11.sp,
                                color = colores.onSurfaceVariant
                            )
                        }
                    }
                }

                estado.error?.let { Text(it, color = colores.error, fontSize = 12.sp) }

                Spacer(Modifier.height(8.dp))

                // Botones Guardar y Cancelar
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = vm::guardarFormulario,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colores.primary),
                        enabled = editable
                    ) {
                        Text(if (estado.procesando) "Guardando…" else "Guardar categoría", fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(
                        onClick = onVolver,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        border = BorderStroke(1.dp, colores.primary),
                        enabled = editable
                    ) {
                        Text("Cancelar", color = colores.primary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
