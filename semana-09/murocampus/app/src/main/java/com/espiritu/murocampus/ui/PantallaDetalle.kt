package com.espiritu.murocampus.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
fun PantallaDetalle(vm: PostsViewModel, id: Int) {
    val estado by vm.detalle.collectAsStateWithLifecycle()

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
