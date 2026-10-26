package com.espiritu.ayudacampus.ui.soporte

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.espiritu.ayudacampus.model.*
import com.espiritu.ayudacampus.ui.cliente.iconoCategoriaEstudiante
import com.espiritu.ayudacampus.ui.cliente.superficieEstudiante
import com.espiritu.ayudacampus.ui.components.*
import com.espiritu.ayudacampus.ui.theme.colorPrioridad
import com.espiritu.ayudacampus.viewmodel.AtenderViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AtenderTicketScreen(vm: AtenderViewModel, onVolver: () -> Unit) {
    val estado by vm.uiState.collectAsStateWithLifecycle()
    val detalle = estado.ticket
    val colores = MaterialTheme.colorScheme
    val avisos = remember { SnackbarHostState() }
    val ahora by produceState(System.currentTimeMillis()) {
        while (true) { delay(60_000L); value = System.currentTimeMillis() }
    }

    LaunchedEffect(estado.error, estado.mensaje) {
        (estado.error ?: estado.mensaje)?.let { avisos.showSnackbar(it) }
    }

    Scaffold(
        containerColor = colores.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Atender ticket #${vm.ticketId.toString().padStart(4, '0')}", fontSize = 16.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver a la bandeja", Modifier.size(20.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colores.background)
            )
        },
        snackbarHost = { SnackbarHost(avisos) },
        bottomBar = {
            if (detalle != null && detalle.ticket.estado != "CERRADO") {
                Surface(
                    color = colores.surface,
                    tonalElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth().imePadding()
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = estado.respuesta,
                                onValueChange = vm::cambiarRespuesta,
                                placeholder = { Text("Responder al cliente...", fontSize = 13.sp) },
                                enabled = estado.editable,
                                singleLine = false,
                                minLines = 1,
                                maxLines = 4,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(24.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = superficieEstudiante(),
                                    focusedContainerColor = superficieEstudiante(),
                                    unfocusedBorderColor = colores.outlineVariant
                                )
                            )
                            Surface(
                                onClick = { vm.responder() },
                                shape = CircleShape,
                                color = if (estado.editable && estado.respuesta.isNotBlank()) colores.primary else colores.surfaceVariant,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Enviar respuesta",
                                        tint = if (estado.editable && estado.respuesta.isNotBlank()) colores.onPrimary else colores.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
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
            estado.inexistente -> Text("El ticket no existe.", Modifier.padding(padding).padding(24.dp))
            detalle != null -> {
                val ticket = detalle.ticket
                LazyColumn(
                    Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Tarjeta de resumen
                    item {
                        Card(
                            Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = superficieEstudiante()),
                            border = BorderStroke(.75.dp, colores.outlineVariant)
                        ) {
                            Row(Modifier.height(IntrinsicSize.Min)) {
                                Box(
                                    Modifier.width(5.dp).fillMaxHeight()
                                        .background(colorPrioridad(ticket.prioridad))
                                )
                                Column(
                                    Modifier.weight(1f).padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        ticket.titulo,
                                        fontSize = 15.sp,
                                        lineHeight = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colores.onSurface
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        EstadoChip(ticket.estado)
                                        PrioridadChip(ticket.prioridad)
                                        SlaChip(detalle, ahora)
                                    }
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            iconoCategoriaEstudiante(detalle.categoria.icono),
                                            contentDescription = null,
                                            Modifier.size(16.dp),
                                            tint = colores.primary
                                        )
                                        Text(
                                            "Estudiante: ${detalle.cliente.nombre} \u00b7 ${detalle.categoria.nombre}" +
                                                if (ticket.ubicacion.isNotBlank()) " \u00b7 ${ticket.ubicacion}" else "",
                                            fontSize = 11.sp,
                                            color = colores.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    if (ticket.descripcion.isNotBlank()) {
                                        Text(
                                            ticket.descripcion,
                                            fontSize = 13.sp,
                                            color = colores.onSurface,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Selector de Estado
                    item {
                        Card(
                            Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = superficieEstudiante()),
                            border = BorderStroke(.75.dp, colores.outlineVariant)
                        ) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Estado del ticket", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(
                                        EstadoTicket.ABIERTO to "Abierto",
                                        EstadoTicket.EN_PROCESO to "En proceso",
                                        EstadoTicket.RESUELTO to "Resuelto",
                                        EstadoTicket.CERRADO to "Cerrado"
                                    ).forEach { (valor, etiqueta) ->
                                        FilterChip(
                                            selected = estado.estado == valor,
                                            onClick = { vm.cambiarEstado(valor) },
                                            enabled = estado.editable && valor != EstadoTicket.CERRADO,
                                            shape = RoundedCornerShape(16.dp),
                                            label = { Text(etiqueta, fontSize = 12.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                containerColor = colores.surface,
                                                selectedContainerColor = colores.primaryContainer,
                                                selectedLabelColor = colores.onPrimaryContainer
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Selector de Prioridad
                    item {
                        Card(
                            Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = superficieEstudiante()),
                            border = BorderStroke(.75.dp, colores.outlineVariant)
                        ) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Prioridad", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    Prioridad.entries.forEach { valor ->
                                        FilterChip(
                                            selected = estado.prioridad == valor,
                                            onClick = { vm.cambiarPrioridad(valor) },
                                            enabled = estado.editable,
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(14.dp),
                                            label = {
                                                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                                    Text(when (valor) {
                                                        Prioridad.BAJA -> "Baja"; Prioridad.MEDIA -> "Media"; Prioridad.ALTA -> "Alta"
                                                    }, fontSize = 12.sp)
                                                }
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                containerColor = colores.surface,
                                                selectedContainerColor = colorPrioridad(valor.name),
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Botón Guardar cambios
                    item {
                        Button(
                            onClick = vm::guardar,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colores.primary),
                            enabled = estado.editable && (estado.estadoEditado || estado.prioridadEditada)
                        ) {
                            Text("Guardar cambios", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Conversación
                    item {
                        Text("Conversación", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colores.onBackground)
                    }

                    if (detalle.comentarios.isEmpty()) {
                        item { Text("Todavía no hay comentarios.", fontSize = 13.sp, color = colores.onSurfaceVariant) }
                    }

                    items(detalle.comentarios, key = { it.id }) { comentario ->
                        BurbujaComentario(comentario, comentario.autorId == vm.usuario.id)
                    }

                    item { Spacer(Modifier.height(32.dp)) }
                }
            }
        }
    }
}
