package com.espiritu.ayudacampus.ui.soporte

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import com.espiritu.ayudacampus.data.local.entity.Usuario
import com.espiritu.ayudacampus.ui.components.AppTopBar
import com.espiritu.ayudacampus.ui.components.AyudaButton
import com.espiritu.ayudacampus.viewmodel.AjustesUiState

@Composable
fun AjustesScreen(usuario: Usuario, estado: AjustesUiState, cerrandoSesion: Boolean, errorSesion: String?,
                  onModoOscuro: (Boolean) -> Unit, onNotificaciones: (Boolean) -> Unit,
                  onRecibirTickets: (Boolean) -> Unit, onReintentar: () -> Unit,
                  onPerfil: () -> Unit, onCambiarPerfil: () -> Unit, onCerrarSesion: () -> Unit) {
    val habilitado = !estado.cargando && !estado.guardando && !cerrandoSesion
    Scaffold(topBar = { AppTopBar("Ajustes") }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(usuario.nombre, style = MaterialTheme.typography.titleLarge)
            Text(usuario.correo)
            if (estado.cargando) CircularProgressIndicator()
            SelectorAjuste("Modo oscuro", estado.modoOscuro, habilitado, onModoOscuro)
            SelectorAjuste("Notificaciones", estado.notificaciones, habilitado, onNotificaciones)
            if (usuario.rol == "ADMIN") {
                SelectorAjuste("Recibir tickets", estado.recibirTickets, habilitado, onRecibirTickets)
                Text(if (estado.recibirTickets) "La mesa de ayuda recibe nuevos tickets." else "Mesa de ayuda cerrada")
            }
            Text("Tus preferencias se conservan al cerrar la aplicación.",
                style = MaterialTheme.typography.bodySmall)
            estado.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                TextButton(onClick = onReintentar, enabled = !estado.guardando) { Text("Reintentar lectura") }
            }
            if (estado.guardando) LinearProgressIndicator(Modifier.fillMaxWidth())
            AyudaButton("Ver perfil", onPerfil, Modifier.fillMaxWidth(), enabled = !cerrandoSesion && !estado.guardando)
            AyudaButton("Cambiar de perfil", onCambiarPerfil, Modifier.fillMaxWidth(), enabled = !cerrandoSesion && !estado.guardando)
            OutlinedButton(onClick = onCerrarSesion, modifier = Modifier.fillMaxWidth(), enabled = !cerrandoSesion && !estado.guardando) {
                Text("Cerrar sesión")
            }
            errorSesion?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun SelectorAjuste(titulo: String, valor: Boolean, habilitado: Boolean, onCambio: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Text(titulo, Modifier.weight(1f))
        Switch(checked = valor, onCheckedChange = onCambio, enabled = habilitado,
            modifier = Modifier.semantics { contentDescription = titulo })
    }
}
