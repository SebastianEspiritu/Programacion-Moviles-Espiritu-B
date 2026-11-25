package com.espiritu.murocampus.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// MuroCampus usa siempre el esquema claro con la barra azul del diseño
private val EsquemaMuroCampus = lightColorScheme(
    primary = AzulMuro,
    onPrimary = Color.White,
    primaryContainer = AzulClaro,
    onPrimaryContainer = AzulMuroOscuro,
    secondary = AzulMuroOscuro,
    onSecondary = Color.White,
    background = FondoApp,
    onBackground = Color(0xFF1D1B20),
    surface = FondoApp,
    onSurface = Color(0xFF1D1B20),
    surfaceVariant = FondoTarjeta,
    onSurfaceVariant = TextoSecundario,
    outline = BordeTarjeta,
    error = RojoError,
    onError = Color.White,
    errorContainer = FondoError,
    onErrorContainer = Color(0xFF8C1D18)
)

@Composable
fun MuroCampusTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaMuroCampus,
        typography = Typography,
        content = content
    )
}
