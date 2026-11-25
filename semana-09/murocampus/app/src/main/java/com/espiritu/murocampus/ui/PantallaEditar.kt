package com.espiritu.murocampus.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.espiritu.murocampus.ui.theme.BordeExito
import com.espiritu.murocampus.ui.theme.FondoExito
import com.espiritu.murocampus.ui.theme.TextoSecundario
import com.espiritu.murocampus.ui.theme.VerdeExito
import com.espiritu.murocampus.viewmodel.PostsViewModel
import com.espiritu.murocampus.viewmodel.UiState

/** Fase 2: modifica el título de una publicación con PATCH y muestra la respuesta. */
@Composable
fun PantallaEditar(vm: PostsViewModel, id: Int) {
    val detalle by vm.detalle.collectAsStateWithLifecycle()
    val estado by vm.editado.collectAsStateWithLifecycle()

    // El título actual viene del detalle que ya está descargado
    val tituloActual = (detalle as? UiState.Exito)?.datos?.post?.title.orEmpty()
    var titulo by rememberSaveable { mutableStateOf(tituloActual) }

    val enviando = estado is UiState.Cargando
    val cambio = titulo.trim()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Editar publicación $id",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = titulo,
            onValueChange = { titulo = it },
            label = { Text("Título") },
            minLines = 2,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFF9AA5B8),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = Color(0xFF5B6475),
                focusedLabelColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Deshabilitado si está vacío o si no cambió nada
        Button(
            onClick = { vm.editarTitulo(id, cambio) },
            enabled = cambio.isNotEmpty() && cambio != tituloActual && !enviando
        ) {
            Text("Guardar", fontWeight = FontWeight.Bold)
        }

        when (val e = estado) {
            null -> Unit                                   // aún no se envió
            UiState.Cargando -> CircularProgressIndicator(Modifier.size(28.dp))
            is UiState.Exito -> RespuestaEdicion(
                idPost = e.datos.id,
                tituloNuevo = e.datos.title
            )
            is UiState.Error -> Text(
                text = e.mensaje,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(14.dp)
            )
        }
    }
}

/** Recuadro verde punteado con lo que respondió el servidor. */
@Composable
private fun RespuestaEdicion(idPost: Int, tituloNuevo: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(FondoExito)
            .drawBehind {
                drawRoundRect(
                    color = BordeExito,
                    cornerRadius = CornerRadius(12.dp.toPx()),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                    )
                )
            }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Actualizado con PATCH (id $idPost)",
            color = VerdeExito,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Text(
            text = "Respuesta: «$tituloNuevo»",
            color = TextoSecundario,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}
