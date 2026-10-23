package com.espiritu.ayudacampus.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.espiritu.ayudacampus.model.Rol

@Composable
fun AyudaCampusTheme(
    rol: Rol = Rol.CLIENTE,
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val estudiante = rol == Rol.CLIENTE
    val colores = if (darkTheme) {
        darkColorScheme(
            primary = if (estudiante) Color(0xFF67E8F9) else Color(0xFFC4B5FD),
            onPrimary = if (estudiante) Color(0xFF003744) else Color(0xFF2E1065),
            primaryContainer = if (estudiante) Color(0xFF164E63) else Color(0xFF4C1D95),
            onPrimaryContainer = if (estudiante) EstudianteContenedor else SoporteContenedor
        )
    } else {
        lightColorScheme(
            primary = if (estudiante) EstudiantePrimario else SoportePrimario,
            onPrimary = Color.White,
            primaryContainer = if (estudiante) EstudianteContenedor else SoporteContenedor,
            onPrimaryContainer = if (estudiante) Color(0xFF164E63) else Color(0xFF4C1D95),
            background = if (estudiante) EstudianteFondo else SoporteFondo,
            surface = if (estudiante) EstudianteFondo else SoporteFondo
        )
    }
    MaterialTheme(colorScheme = colores, typography = Typography, content = content)
}
