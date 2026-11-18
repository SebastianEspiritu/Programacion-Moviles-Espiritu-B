package com.espiritu.centrotareas.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.espiritu.centrotareas.model.PrimosUi
import com.espiritu.centrotareas.ui.theme.ExitoFondo
import com.espiritu.centrotareas.ui.theme.ExitoTexto
import com.espiritu.centrotareas.ui.theme.ExitoTitulo
import com.espiritu.centrotareas.viewmodel.TareasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaTareas(viewModel: TareasViewModel) {
    val mensaje by viewModel.mensaje.collectAsState()
    val progreso by viewModel.progreso.collectAsState()
    val corriendo by viewModel.corriendo.collectAsState()
    val primos by viewModel.primos.collectAsState()

    // Prueba de vida: si deja de sumar, la pantalla está congelada
    var toques by rememberSaveable { mutableIntStateOf(0) }
    var textoNumero by rememberSaveable { mutableStateOf("500000") }
    var ultimoNumero by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CentroTareas", fontWeight = FontWeight.Bold) },
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
            TarjetaTarea(titulo = "Tarea con progreso") {
                Button(onClick = { toques++ }) {
                    Text("Tócame: $toques")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { viewModel.tareaLarga() },
                        enabled = !corriendo
                    ) {
                        Text("Iniciar")
                    }
                    OutlinedButton(
                        onClick = { viewModel.cancelar() },
                        enabled = corriendo
                    ) {
                        Text("Cancelar")
                    }
                }
                LinearProgressIndicator(
                    progress = { progreso },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("${(progreso * 100).toInt()} %")
                Text(mensaje)
            }
            TarjetaTarea(titulo = "Contar números primos") {
                val calculando = primos is PrimosUi.Calculando
                OutlinedTextField(
                    value = textoNumero,
                    onValueChange = { textoNumero = it },
                    label = { Text("Hasta el número") },
                    singleLine = true,
                    enabled = !calculando,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            // Ignora textos no numéricos o menores que 2
                            val n = textoNumero.trim().toIntOrNull()
                            if (n != null && n >= 2) {
                                ultimoNumero = n
                                viewModel.contar(n)
                            }
                        },
                        enabled = !calculando
                    ) {
                        Text("Contar")
                    }
                    OutlinedButton(
                        onClick = { viewModel.cancelarPrimos() },
                        enabled = calculando
                    ) {
                        Text("Cancelar")
                    }
                }
                when (val estado = primos) {
                    PrimosUi.Inicial -> Unit
                    PrimosUi.Calculando -> {
                        CircularProgressIndicator()
                        Text("Calculando...")
                    }
                    is PrimosUi.Listo -> RecuadroExito {
                        Text(
                            "Primos hasta $ultimoNumero: ${estado.cantidad}",
                            color = ExitoTitulo,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Tiempo: ${estado.milisegundos} ms")
                    }
                    PrimosUi.Cancelado -> Text("Cancelado")
                }
            }
            TarjetaTarea(titulo = "Dos cargas") {
                // TODO Pasos 6 y 7: Una por una / A la vez, Simular error, resultado y Reintentar
            }
        }
    }
}

@Composable
private fun TarjetaTarea(
    titulo: String,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = titulo,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )
            contenido()
        }
    }
}

@Composable
private fun RecuadroExito(contenido: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ExitoFondo,
        contentColor = ExitoTexto,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            content = contenido
        )
    }
}
