package com.espiritu.tecsupfit.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF005A42),
    secondary = Color(0xFFE8F5E9),
    background = Color(0xFFF5F7FA),
    surface = Color.White
)

@Composable
fun TecsupFITTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}