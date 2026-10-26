package com.espiritu.ayudacampus.ui.cliente

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SplashScreen(errorInicializacion: String?) {
    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text("AyudaCampus", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(24.dp))
            if (errorInicializacion == null) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text("Preparando tu sesión…")
            } else {
                Text(errorInicializacion, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
