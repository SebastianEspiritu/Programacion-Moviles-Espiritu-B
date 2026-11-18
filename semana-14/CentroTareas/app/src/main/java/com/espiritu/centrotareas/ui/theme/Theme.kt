package com.espiritu.centrotareas.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaClaro = lightColorScheme(
    primary = Azul,
    onPrimary = Color.White,
    primaryContainer = AzulContenedor,
    onPrimaryContainer = AzulOscuro,
    secondary = Azul,
    onSecondary = Color.White,
    background = FondoApp,
    onBackground = Color(0xFF1B1B1F),
    surface = Color.White,
    onSurface = Color(0xFF1B1B1F),
    outlineVariant = Borde,
    error = Color(0xFFB3261E),
    errorContainer = Color(0xFFFBE4E3),
    onErrorContainer = Color(0xFF8C1D18)
)

@Composable
fun CentroTareasTheme(content: @Composable () -> Unit) {
    // Siempre el esquema claro con azul (sin colores dinámicos) para que se vea como el diseño
    MaterialTheme(
        colorScheme = EsquemaClaro,
        typography = Typography,
        content = content
    )
}
