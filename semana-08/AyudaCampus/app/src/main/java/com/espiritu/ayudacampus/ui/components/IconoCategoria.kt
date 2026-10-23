package com.espiritu.ayudacampus.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

fun iconoCategoria(clave: String): ImageVector = when (clave) {
    "pc" -> Icons.Default.Computer
    "code" -> Icons.Default.Code
    "wifi" -> Icons.Default.Wifi
    "key" -> Icons.Default.Key
    "printer" -> Icons.Default.Print
    else -> Icons.AutoMirrored.Filled.Label
}
