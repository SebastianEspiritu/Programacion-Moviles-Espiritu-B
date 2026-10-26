package com.espiritu.ayudacampus.ui.cliente

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.espiritu.ayudacampus.model.Prioridad
import com.espiritu.ayudacampus.ui.components.AyudaButton
import com.espiritu.ayudacampus.ui.theme.colorPrioridad
import com.espiritu.ayudacampus.viewmodel.NuevoTicketViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NuevoTicketScreen(vm: NuevoTicketViewModel, onVolver: () -> Unit = {}, onCreado: (Long) -> Unit) {
    val estado by vm.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(estado.ticketCreado) { estado.ticketCreado?.let(onCreado) }
    val editable = !estado.enviando && estado.ticketCreado == null
    val colores = MaterialTheme.colorScheme
    Scaffold(containerColor = colores.background, topBar = {
        CenterAlignedTopAppBar(title = { Text("Nuevo ticket", fontSize = 16.sp, fontWeight = FontWeight.SemiBold) },
            navigationIcon = { IconButton(onClick = onVolver, enabled = !estado.enviando) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Volver a Mis tickets", Modifier.size(20.dp))
            } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = colores.background))
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding()
            .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when {
                estado.cargando -> { LinearProgressIndicator(Modifier.fillMaxWidth()); Text("Cargando categor\u00edas y disponibilidad\u2026") }
                estado.errorCarga != null -> {
                    Text(estado.errorCarga.orEmpty(), color = colores.error)
                    AyudaButton("Reintentar carga", vm::cargarDatos, enabled = editable)
                }
                !estado.recibirTickets -> Text("Mesa de ayuda cerrada", color = colores.error)
            }
            Text("Categor\u00eda", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                estado.categorias.forEach { categoria ->
                    FilterChip(selected = estado.categoriaId == categoria.id,
                        onClick = { vm.seleccionarCategoria(categoria.id) }, enabled = editable && !estado.cargando,
                        shape = RoundedCornerShape(18.dp),
                        label = { Text(categoria.nombre, fontSize = 12.sp) },
                        leadingIcon = { Icon(iconoCategoriaEstudiante(categoria.icono), null, Modifier.size(17.dp)) },
                        colors = FilterChipDefaults.filterChipColors(containerColor = superficieEstudiante(),
                            selectedContainerColor = colores.primary, selectedLabelColor = colores.onPrimary,
                            selectedLeadingIconColor = colores.onPrimary))
                }
            }
            if (!estado.cargando && estado.errorCarga == null && estado.categorias.isEmpty()) {
                Text("No hay categor\u00edas activas disponibles.")
            }
            estado.errorCategoria?.let { Text(it, color = colores.error, style = MaterialTheme.typography.bodySmall) }
            CampoIncidencia(estado.titulo, vm::cambiarTitulo, "T\u00edtulo *", editable, estado.errorTitulo)
            CampoIncidencia(estado.ubicacion, vm::cambiarUbicacion, "Ubicaci\u00f3n (opcional)", editable,
                icono = Icons.Outlined.LocationOn)
            Text("Prioridad", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Prioridad.entries.forEach { prioridad ->
                    FilterChip(selected = estado.prioridad == prioridad, onClick = { vm.cambiarPrioridad(prioridad) },
                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp),
                        enabled = editable, label = {
                            Box(Modifier.fillMaxWidth().heightIn(min = 26.dp), contentAlignment = Alignment.Center) {
                                Text(when (prioridad) {
                                    Prioridad.BAJA -> "Baja"; Prioridad.MEDIA -> "Media"; Prioridad.ALTA -> "Alta"
                                }, fontSize = 12.sp)
                            }
                        }, colors = FilterChipDefaults.filterChipColors(
                            containerColor = superficieEstudiante(),
                            selectedContainerColor = colorPrioridad(prioridad.name),
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White))
                }
            }
            CampoIncidencia(estado.descripcion, vm::cambiarDescripcion, "Descripci\u00f3n *", editable,
                estado.errorDescripcion, multilinea = true)
            // No hay contrato de tiempo estimado de respuesta: no se muestra un plazo ficticio.
            estado.error?.let { Text(it, color = colores.error) }
            Button(onClick = vm::enviar, shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                enabled = editable && !estado.cargando && estado.errorCarga == null && estado.recibirTickets) {
                if (estado.enviando) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Text(if (estado.enviando) "Enviando\u2026" else "Enviar ticket", fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
