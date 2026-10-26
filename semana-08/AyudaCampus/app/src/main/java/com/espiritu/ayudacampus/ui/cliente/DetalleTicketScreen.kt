package com.espiritu.ayudacampus.ui.cliente

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
import androidx.compose.material.icons.filled.CheckCircle
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
import com.espiritu.ayudacampus.model.EstadoTicket
import com.espiritu.ayudacampus.ui.components.*
import com.espiritu.ayudacampus.ui.theme.colorPrioridad
import com.espiritu.ayudacampus.viewmodel.DetalleTicketViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleTicketScreen(vm: DetalleTicketViewModel, onVolver: () -> Unit) {
    val estado by vm.uiState.collectAsStateWithLifecycle()
    val detalle = estado.ticket
    val colores = MaterialTheme.colorScheme
    val ahora by produceState(System.currentTimeMillis()) {
        while (true) { delay(60_000L); value = System.currentTimeMillis() }
    }
    val ticketActivo = detalle?.ticket
    val esCerrado = ticketActivo?.estado == EstadoTicket.CERRADO.name

    Scaffold(
        containerColor = colores.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Ticket #${vm.ticketId.toString().padStart(4, '0')}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver a mis tickets", Modifier.size(20.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colores.background)
            )
        },
        bottomBar = {
            if (detalle != null && !esCerrado) {
                Surface(
                    color = colores.surface,
                    tonalElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth().imePadding()
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = estado.borrador,
                            onValueChange = vm::cambiarBorrador,
                            placeholder = { Text("Escribe un comentario...", fontSize = 13.sp) },
                            enabled = estado.puedeComentar,
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
                            onClick = { vm.comentar() },
                            shape = CircleShape,
                            color = if (estado.puedeComentar && estado.borrador.isNotBlank()) colores.primary else colores.surfaceVariant,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Enviar comentario",
                                    tint = if (estado.puedeComentar && estado.borrador.isNotBlank()) colores.onPrimary else colores.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        when {
            estado.cargando -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            estado.errorCarga != null -> Column(
                Modifier.padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(estado.errorCarga.orEmpty(), color = colores.error)
                Spacer(Modifier.height(12.dp))
                AyudaButton("Reintentar", vm::cargar)
            }
            estado.sinAcceso -> Text("No tienes acceso a este ticket.", Modifier.padding(padding).padding(24.dp))
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
                                            detalle.categoria.nombre +
                                                if (ticket.ubicacion.isNotBlank()) " \u00b7 ${ticket.ubicacion}" else "" +
                                                    " \u00b7 ${tiempoRelativoTicket(ticket.fecha, ahora)}",
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

                    // Stepper de progreso
                    item {
                        Card(
                            Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = superficieEstudiante()),
                            border = BorderStroke(.75.dp, colores.outlineVariant)
                        ) {
                            Box(Modifier.padding(16.dp)) {
                                StepperTicket(ticket.estado)
                            }
                        }
                    }

                    // Sección Conversación
                    item {
                        Text(
                            "Conversación",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colores.onBackground
                        )
                    }

                    if (detalle.comentarios.isEmpty()) {
                        item {
                            Text(
                                "Todavía no hay comentarios.",
                                fontSize = 13.sp,
                                color = colores.onSurfaceVariant
                            )
                        }
                    }

                    items(detalle.comentarios, key = { it.id }) { comentario ->
                        BurbujaComentario(comentario, comentario.autorId == vm.usuario.id)
                    }

                    // Estado Resuelto (Screen 06)
                    if (ticket.estado == EstadoTicket.RESUELTO.name) {
                        item {
                            Surface(
                                color = Color(0xFFE8F5E9),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        "Soporte marcó este ticket como resuelto. ¿El problema se solucionó?",
                                        fontSize = 13.sp,
                                        color = Color(0xFF1B5E20),
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        item {
                            Card(
                                Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = superficieEstudiante()),
                                border = BorderStroke(.75.dp, colores.outlineVariant)
                            ) {
                                Column(
                                    Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        "Califica la atención",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Estrellas(estado.calificacion, vm::seleccionarCalificacion, estado.puedeResolver)
                                    if (estado.calificacion in 1..5) {
                                        Text(
                                            "${estado.calificacion} de 5 estrellas",
                                            fontSize = 12.sp,
                                            color = colores.primary
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            AyudaTextField(
                                estado.comentarioFinal,
                                vm::cambiarComentarioFinal,
                                "Comentario final (opcional)",
                                Modifier.fillMaxWidth(),
                                enabled = estado.puedeResolver,
                                singleLine = false,
                                minLines = 2
                            )
                        }

                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { vm.cerrar() },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = colores.primary),
                                    enabled = estado.puedeResolver && estado.calificacion in 1..5
                                ) {
                                    Text("Confirmar y cerrar", fontWeight = FontWeight.SemiBold)
                                }
                                OutlinedButton(
                                    onClick = vm::reabrir,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                    border = BorderStroke(1.dp, colores.error),
                                    enabled = estado.puedeResolver
                                ) {
                                    Text("Reabrir ticket", color = colores.error, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    // Ticket Cerrado
                    if (ticket.estado == EstadoTicket.CERRADO.name) {
                        item {
                            Card(
                                Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = superficieEstudiante()),
                                border = BorderStroke(.75.dp, colores.outlineVariant)
                            ) {
                                Column(
                                    Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("Ticket cerrado. Ya no se pueden enviar comentarios.", fontSize = 13.sp)
                                    ticket.calificacion?.let {
                                        Spacer(Modifier.height(4.dp))
                                        Text("Calificación otorgada:", fontSize = 12.sp, color = colores.onSurfaceVariant)
                                        Estrellas(it)
                                    }
                                }
                            }
                        }
                    }

                    // Mensajes de error o proceso
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (estado.procesando) LinearProgressIndicator(Modifier.fillMaxWidth())
                            estado.errorAccion?.let { Text(it, color = colores.error, fontSize = 12.sp) }
                            estado.mensaje?.let { Text(it, color = colores.primary, fontSize = 12.sp) }
                        }
                    }

                    // Espacio final para evitar que el bottom bar cubra el último elemento
                    item {
                        Spacer(Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}
