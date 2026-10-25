package com.espiritu.ayudacampus.ui.cliente

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.espiritu.ayudacampus.data.local.entity.Usuario
import com.espiritu.ayudacampus.ui.components.AppTopBar
import com.espiritu.ayudacampus.ui.components.AyudaButton
import com.espiritu.ayudacampus.viewmodel.PerfilUiState

@Composable
fun PerfilScreen(usuario: Usuario, contadores: PerfilUiState?, ocupado: Boolean, error: String?,
                 onReintentar: () -> Unit, onAjustes: () -> Unit,
                 onCambiarPerfil: () -> Unit, onCerrarSesion: () -> Unit) {
    Scaffold(topBar = { AppTopBar("Perfil") }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(usuario.nombre, style = MaterialTheme.typography.headlineSmall)
                    Text(usuario.correo)
                    Text(if (usuario.rol == "ADMIN") "Soporte TI" else "Estudiante")
                }
            }
            contadores?.let { datos ->
                when {
                    datos.cargando -> CircularProgressIndicator()
                    datos.error != null -> {
                        Text(datos.error, color = MaterialTheme.colorScheme.error)
                        AyudaButton("Reintentar", onReintentar)
                    }
                    else -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        listOf("Abiertos" to datos.abiertos, "Resueltos" to datos.resueltos, "Totales" to datos.total).forEach { (titulo, valor) ->
                            Column { Text(valor.toString(), style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary); Text(titulo) }
                        }
                    }
                }
            }
            AyudaButton("Ajustes", onAjustes, Modifier.fillMaxWidth(), enabled = !ocupado)
            Text("Para cambiar de perfil, inicia sesión con una cuenta del otro rol.")
            AyudaButton("Cambiar de perfil", onCambiarPerfil, Modifier.fillMaxWidth(), enabled = !ocupado)
            OutlinedButton(onClick = onCerrarSesion, modifier = Modifier.fillMaxWidth(), enabled = !ocupado) { Text("Cerrar sesión") }
            if (ocupado) LinearProgressIndicator(Modifier.fillMaxWidth())
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}
