package com.espiritu.murocampus.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.espiritu.murocampus.model.Comentario
import com.espiritu.murocampus.model.Post
import com.espiritu.murocampus.ui.theme.BordeTarjeta
import com.espiritu.murocampus.ui.theme.FondoTarjeta
import com.espiritu.murocampus.ui.theme.TextoSecundario
import com.espiritu.murocampus.viewmodel.PostsViewModel
import com.espiritu.murocampus.viewmodel.UiState

@Composable
fun PantallaDetalle(
    vm: PostsViewModel,
    id: Int,
    onEditar: () -> Unit = {}
) {
    val estado by vm.detalle.collectAsStateWithLifecycle()
    val borrado by vm.borrado.collectAsStateWithLifecycle()
    var confirmarBorrado by remember { mutableStateOf(false) }

    when (val e = estado) {
        UiState.Cargando -> Cargando(texto = "Cargando publicación…")

        is UiState.Error -> ErrorConReintento(
            mensaje = e.mensaje,
            onReintentar = { vm.abrir(id) }
        )

        is UiState.Exito -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { TarjetaDetalle(e.datos.post) }
            item {
                AccionesDetalle(
                    borrando = borrado is UiState.Cargando,
                    onEditar = onEditar,
                    onEliminar = { confirmarBorrado = true }
                )
            }
            (borrado as? UiState.Error)?.let { error ->
                item { MensajeError(error.mensaje) }
            }
            item {
                Text(
                    text = "Comentarios (${e.datos.comentarios.size})",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            items(e.datos.comentarios, key = { it.id }) { comentario ->
                TarjetaComentario(comentario)
            }
        }
    }

    // Confirmación antes de borrar
    if (confirmarBorrado) {
        AlertDialog(
            onDismissRequest = { confirmarBorrado = false },
            title = { Text("¿Eliminar publicación?") },
            text = { Text("Se enviará DELETE posts/$id al servidor.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarBorrado = false
                    vm.borrar(id)
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmarBorrado = false }) { Text("Cancelar") }
            }
        )
    }
}

/** Botones del detalle (Fase 2). */
@Composable
private fun AccionesDetalle(borrando: Boolean, onEditar: () -> Unit, onEliminar: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (borrando) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
        OutlinedButton(onClick = onEditar, enabled = !borrando) {
            Text("Editar título", fontWeight = FontWeight.Bold)
        }
        Button(
            onClick = onEliminar,
            enabled = !borrando,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            )
        ) {
            Text("Eliminar", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MensajeError(mensaje: String) {
    Text(
        text = mensaje,
        color = MaterialTheme.colorScheme.onErrorContainer,
        fontSize = 14.sp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(12.dp)
    )
}

/** Título y texto completos de la publicación. */
@Composable
private fun TarjetaDetalle(post: Post) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, BordeTarjeta, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Text(post.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(10.dp))
        Text(post.body.replace("\n", " "), fontSize = 14.sp, color = TextoSecundario)
    }
}

/** Un comentario: correo en azul y el texto. */
@Composable
private fun TarjetaComentario(comentario: Comentario) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(FondoTarjeta)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text = comentario.email,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )
        Spacer(Modifier.height(2.dp))
        Text(comentario.body.replace("\n", " "), fontSize = 13.sp)
    }
}
