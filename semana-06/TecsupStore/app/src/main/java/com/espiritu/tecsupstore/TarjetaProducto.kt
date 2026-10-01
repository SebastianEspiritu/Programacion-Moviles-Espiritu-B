package com.espiritu.tecsupstore

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TarjetaProducto(
    producto: Producto,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "S/ ${String.format("%.2f", producto.precio)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Opciones del producto"
                    )
                }

                // DropdownMenu personalizado con íconos y divisores (Hito 3)
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Favoritos") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.FavoriteBorder,
                                contentDescription = "Favoritos"
                            )
                        },
                        onClick = { expanded = false }
                    )

                    HorizontalDivider()

                    DropdownMenuItem(
                        text = { Text("Compartir") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Compartir"
                            )
                        },
                        onClick = { expanded = false }
                    )

                    HorizontalDivider()

                    DropdownMenuItem(
                        text = { Text("Reportar") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Reportar"
                            )
                        },
                        onClick = { expanded = false }
                    )
                }
            }
        }
    }
}