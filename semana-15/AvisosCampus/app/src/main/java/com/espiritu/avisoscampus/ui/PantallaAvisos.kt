package com.espiritu.avisoscampus.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.espiritu.avisoscampus.notificacion.Notificador
import com.espiritu.avisoscampus.viewmodel.AvisosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAvisos(vm: AvisosViewModel) {
    val contexto = LocalContext.current

    // El ViewModel decide QUÉ avisar; la pantalla (que sí tiene Context) lo muestra
    LaunchedEffect(Unit) {
        vm.eventos.collect { aviso ->
            val mostrado = Notificador.mostrar(contexto, aviso)
            // TODO Paso 8: si no se mostró, avisar al ViewModel
        }
    }

    val lanzador = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        // TODO: vm.permisoRespondido(concedido)
    }

    LaunchedEffect(Unit) {
        // TODO: si Notificador.tienePermiso(contexto), vm.permisoRespondido(true)
    }

    // TODO Paso 6: DisposableEffect(escuchando) que registra y quita el ModoAvionReceiver

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AvisosCampus", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Tarjeta(titulo = "Permiso de notificaciones") {
                // TODO Paso 2: etiqueta del estado y botón "Permitir notificaciones"
            }

            Tarjeta(titulo = "Avisos") {
                // TODO Paso 3: botón "Mostrar aviso" que llama a vm.avisarAhora()
            }

            Tarjeta(titulo = "Recordatorio") {
                // TODO Paso 5: "Recordar en 10 s", "Cancelar" y el texto "Faltan N s"
            }

            Tarjeta(titulo = "Receptor de anuncios") {
                // TODO Paso 6: Switch "Escuchar el modo avión"
            }

            Tarjeta(titulo = "Historial") {
                // TODO Paso 7: reemplazar por HistorialAvisos(avisos)
            }
        }
    }
}

// Una Card por función, con su título en azul
@Composable
fun Tarjeta(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = titulo,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall
            )
            contenido()
        }
    }
}
