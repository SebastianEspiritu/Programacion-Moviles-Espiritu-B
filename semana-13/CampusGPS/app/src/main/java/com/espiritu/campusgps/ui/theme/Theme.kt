package com.espiritu.campusgps.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = AzulCampus,
    onPrimary = Color.White,
    primaryContainer = AzulContenedor,
    onPrimaryContainer = AzulOscuroTexto,
    background = FondoClaro,
    surface = FondoClaro,
    surfaceVariant = SuperficieTarjeta
)

private val DarkColorScheme = darkColorScheme(
    primary = AzulCampusClaro,
    onPrimary = AzulOscuroTexto,
    primaryContainer = AzulCampus,
    onPrimaryContainer = Color.White
)

@Composable
fun CampusGPSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
