package com.espiritu.murocampus.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.espiritu.murocampus.viewmodel.PostsViewModel
import com.espiritu.murocampus.viewmodel.UiState

@Composable
fun PantallaLista(
    vm: PostsViewModel,
    onAbrir: (Int) -> Unit = {}
) {
    val estado by vm.lista.collectAsStateWithLifecycle()

    when (val e = estado) {
        UiState.Cargando -> Cargando()

        is UiState.Error -> ErrorConReintento(
            mensaje = e.mensaje,
            onReintentar = { vm.cargar() }
        )

        is UiState.Exito -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(e.datos, key = { it.id }) { post ->
                TarjetaPost(
                    userId = post.userId,
                    titulo = post.title,
                    texto = post.body.replace("\n", " "),
                    onClick = { onAbrir(post.id) }
                )
            }
        }
    }
}
