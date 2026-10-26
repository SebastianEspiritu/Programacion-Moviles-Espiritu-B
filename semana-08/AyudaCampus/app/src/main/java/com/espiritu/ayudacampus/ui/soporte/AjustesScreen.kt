package com.espiritu.ayudacampus.ui.soporte

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.espiritu.ayudacampus.data.local.entity.Usuario
import com.espiritu.ayudacampus.ui.cliente.superficieEstudiante
import com.espiritu.ayudacampus.viewmodel.AjustesUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(usuario: Usuario, estado: AjustesUiState, cerrandoSesion: Boolean, errorSesion: String?,
                  onModoOscuro: (Boolean) -> Unit, onNotificaciones: (Boolean) -> Unit,
                  onRecibirTickets: (Boolean) -> Unit, onReintentar: () -> Unit,
                  onPerfil: () -> Unit = {}, onCambiarPerfil: () -> Unit, onCerrarSesion: () -> Unit) {
    val habilitado = !estado.cargando && !estado.guardando && !cerrandoSesion
    val colores = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colores.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ajustes", fontSize = 16.sp, fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colores.background)
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tarjeta de perfil Admin
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = superficieEstudiante()),
                border = BorderStroke(.75.dp, colores.outlineVariant)
            ) {
                Row(
                    Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = colores.primaryContainer,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Person, null, Modifier.size(32.dp), tint = colores.primary)
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(usuario.nombre, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colores.onSurface)
                        Text(usuario.correo, fontSize = 12.sp, color = colores.onSurfaceVariant)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = colores.primaryContainer
                        ) {
                            Text(
                                "Perfil activo",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = colores.primary
                            )
                        }
                    }
                }
            }

            // Tarjeta Cambiar de perfil (con icono SwapHoriz a la izquierda)
            Card(
                onClick = onCambiarPerfil,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = colores.primary),
                enabled = habilitado
            ) {
                Row(
                    Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.SwapHoriz, null, tint = colores.onPrimary, modifier = Modifier.size(24.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                        Text("Cambiar de perfil", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colores.onPrimary)
                        Text("Modo práctica: ir a vista Estudiante", fontSize = 11.sp, color = colores.onPrimary.copy(alpha = 0.8f))
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = colores.onPrimary, modifier = Modifier.size(20.dp))
                }
            }

            Text("Preferencias", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colores.onBackground)

            // Tarjeta de preferencias
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = superficieEstudiante()),
                border = BorderStroke(.75.dp, colores.outlineVariant)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Modo oscuro", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Cambia el tema de la app", fontSize = 11.sp, color = colores.onSurfaceVariant)
                        }
                        Switch(
                            checked = estado.modoOscuro,
                            onCheckedChange = onModoOscuro,
                            enabled = habilitado,
                            modifier = Modifier.semantics { contentDescription = "Modo oscuro" }
                        )
                    }
                    HorizontalDivider(color = colores.outlineVariant, thickness = 0.5.dp)
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Notificaciones", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Avisar tickets nuevos", fontSize = 11.sp, color = colores.onSurfaceVariant)
                        }
                        Switch(
                            checked = estado.notificaciones,
                            onCheckedChange = onNotificaciones,
                            enabled = habilitado,
                            modifier = Modifier.semantics { contentDescription = "Notificaciones" }
                        )
                    }
                    if (usuario.rol == "ADMIN") {
                        HorizontalDivider(color = colores.outlineVariant, thickness = 0.5.dp)
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("Recibir tickets", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("Abrir o cerrar la mesa de ayuda", fontSize = 11.sp, color = colores.onSurfaceVariant)
                            }
                            Switch(
                                checked = estado.recibirTickets,
                                onCheckedChange = onRecibirTickets,
                                enabled = habilitado,
                                modifier = Modifier.semantics { contentDescription = "Recibir tickets" }
                            )
                        }
                    }
                }
            }

            Surface(
                color = colores.primaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    "Se guardan en el dispositivo con DataStore",
                    Modifier.padding(12.dp),
                    fontSize = 11.sp,
                    color = colores.primary
                )
            }

            if (estado.cargando) CircularProgressIndicator()

            estado.error?.let {
                Text(it, color = colores.error, fontSize = 12.sp)
                TextButton(onClick = onReintentar, enabled = !estado.guardando) { Text("Reintentar lectura") }
            }

            if (estado.guardando) LinearProgressIndicator(Modifier.fillMaxWidth())

            OutlinedButton(
                onClick = onCerrarSesion,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                border = BorderStroke(1.dp, colores.error),
                enabled = habilitado
            ) {
                Text("Cerrar sesión", color = colores.error, fontWeight = FontWeight.SemiBold)
            }

            errorSesion?.let { Text(it, color = colores.error, fontSize = 12.sp) }
        }
    }
}
