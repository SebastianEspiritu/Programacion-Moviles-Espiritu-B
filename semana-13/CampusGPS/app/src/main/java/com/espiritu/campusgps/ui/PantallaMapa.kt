package com.espiritu.campusgps.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.espiritu.campusgps.model.UbicacionUi
import com.espiritu.campusgps.util.formatearDistancia
import com.espiritu.campusgps.viewmodel.MapaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMapa(vm: MapaViewModel) {
    val estado by vm.estado.collectAsState()
    val lugares by vm.lugares.collectAsState()
    val seleccionado by vm.seleccionado.collectAsState()
    val masCercano by vm.masCercano.collectAsState()
    val categoria by vm.categoria.collectAsState()
    val radio by vm.radio.collectAsState()
    val dentroDelRadio by vm.dentroDelRadio.collectAsState()

    val contexto = LocalContext.current

    // Mejora IA: la vista recuerda si hay permiso para activar el punto azul del mapa
    fun permisoConcedido(): Boolean =
        ContextCompat.checkSelfPermission(
            contexto, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                contexto, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

    var tienePermiso by remember { mutableStateOf(permisoConcedido()) }

    // Android dibuja el cuadro de permiso; aquí solo recibimos la respuesta
    val lanzador = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        tienePermiso = resultado.values.any { it }
        // Si aceptó FINE o COARSE, buscamos; si no, avisamos al ViewModel
        if (resultado.values.any { it }) vm.buscar() else vm.permisoDenegado()
    }

    fun alPulsar() {
        val tieneFine = ContextCompat.checkSelfPermission(
            contexto, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val tieneCoarse = ContextCompat.checkSelfPermission(
            contexto, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        tienePermiso = tieneFine || tieneCoarse
        if (tieneFine || tieneCoarse) {
            vm.buscar()
        } else {
            lanzador.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CampusGPS") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val error = estado as? UbicacionUi.Error
            if (error != null) {
                // Pantalla de error: botón principal, recuadro con el mensaje y su acción
                Spacer(Modifier.height(24.dp))
                Button(onClick = { alPulsar() }) {
                    Text("Obtener mi ubicación")
                }
                Spacer(Modifier.height(16.dp))
                MensajeError(
                    error = error,
                    onReintentar = { alPulsar() }
                )
            } else {
                // El mapa ocupa todo el espacio libre de arriba
                MapaUbicacion(
                    estado = estado,
                    lugares = lugares,
                    seleccionado = seleccionado,
                    masCercanoId = masCercano?.id,
                    radio = radio,
                    tienePermiso = tienePermiso,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = { alPulsar() },
                    enabled = estado !is UbicacionUi.Buscando
                ) {
                    Text("Obtener mi ubicación")
                }

                Spacer(Modifier.height(8.dp))

                when (val e = estado) {
                    UbicacionUi.Inicial -> Text("Pulsa el botón para empezar")
                    UbicacionUi.Buscando -> Text("Buscando tu ubicación...")
                    is UbicacionUi.Lista -> Text(
                        "Latitud: ${e.posicion.latitud}   Longitud: ${e.posicion.longitud}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    is UbicacionUi.Error -> Unit
                }

                Spacer(Modifier.height(8.dp))

                // Botón secundario: lleva la cámara al lugar más cercano
                OutlinedButton(
                    onClick = { masCercano?.let { vm.seleccionar(it.id) } },
                    enabled = masCercano != null
                ) {
                    Text("Ir al más cercano")
                }

                masCercano?.let { cercano ->
                    val metros = lugares.firstOrNull { it.lugar.id == cercano.id }?.metros
                    Text(
                        buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Más cercano: ") }
                            append(cercano.nombre)
                            if (metros != null) append(" (${formatearDistancia(metros)})")
                        }
                    )
                }

                // Mejora IA: chips para filtrar por categoría
                FiltroCategorias(
                    categorias = vm.categorias,
                    seleccionada = categoria,
                    onSeleccionar = { vm.filtrar(it) }
                )

                // Mejora IA: selector del radio de cercanía
                SelectorRadio(
                    radios = vm.radiosDisponibles,
                    seleccionado = radio,
                    onSeleccionar = { vm.cambiarRadio(it) }
                )

                // Lista de lugares debajo del mapa, con altura fija
                ListaLugares(
                    lugares = lugares,
                    seleccionado = seleccionado,
                    onClick = { id -> vm.seleccionar(id) },
                    masCercanoId = masCercano?.id,
                    dentroDelRadio = dentroDelRadio,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
