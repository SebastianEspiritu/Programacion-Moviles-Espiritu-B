package com.espiritu.campusgps.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Mejora IA: fila de chips. Solo dibuja y avisa; el filtrado lo hace el ViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltroCategorias(
    categorias: List<String>,
    seleccionada: String?,
    onSeleccionar: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = seleccionada == null,
            onClick = { onSeleccionar(null) },
            label = { Text("Todos") }
        )
        categorias.forEach { cat ->
            FilterChip(
                selected = seleccionada == cat,
                // Tocar de nuevo la misma categoría la desactiva
                onClick = { onSeleccionar(if (seleccionada == cat) null else cat) },
                label = { Text(cat) }
            )
        }
    }
}
