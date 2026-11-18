package com.espiritu.campusgps.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Mejora IA: selector de radio (Sin radio, 100, 200, 500 m). Solo avisa la elección
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorRadio(
    radios: List<Int>,
    seleccionado: Int?,
    onSeleccionar: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Radio:")
        FilterChip(
            selected = seleccionado == null,
            onClick = { onSeleccionar(null) },
            label = { Text("Sin radio") }
        )
        radios.forEach { metros ->
            FilterChip(
                selected = seleccionado == metros,
                onClick = { onSeleccionar(metros) },
                label = { Text("$metros m") }
            )
        }
    }
}
