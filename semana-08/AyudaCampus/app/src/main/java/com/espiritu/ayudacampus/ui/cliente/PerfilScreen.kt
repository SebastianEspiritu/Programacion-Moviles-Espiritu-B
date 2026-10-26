package com.espiritu.ayudacampus.ui.cliente

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.espiritu.ayudacampus.data.local.entity.Usuario
import com.espiritu.ayudacampus.ui.components.AppTopBar
import com.espiritu.ayudacampus.ui.components.AyudaButton
import com.espiritu.ayudacampus.viewmodel.PerfilUiState

@Composable
fun PerfilScreen(usuario: Usuario, contadores: PerfilUiState?, ocupado: Boolean, error: String?,
                 onReintentar: () -> Unit, onAjustes: () -> Unit,
                 onCambiarPerfil: () -> Unit, onCerrarSesion: () -> Unit) {
    val colores = MaterialTheme.colorScheme
    Scaffold(
        containerColor = colores.background,
        topBar = { AppTopBar("Perfil") }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tarjeta de usuario
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
                                if (usuario.rol == "ADMIN") "Soporte TI" else "Estudiante",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = colores.primary
                            )
                        }
                    }
                }
            }

            // Contadores
            contadores?.let { datos ->
                when {
                    datos.cargando -> Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(Modifier.size(24.dp))
                    }
                    datos.error != null -> Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(datos.error, color = colores.error, fontSize = 12.sp)
                        AyudaButton("Reintentar", onReintentar)
                    }
                    else -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val estadisticas = listOf(
                            Triple("Abiertos", datos.abiertos, Triple(Color(0xFFFFFAED), Color(0xFFE65100), Color(0xFFFFCC80))),
                            Triple("Resueltos", datos.resueltos, Triple(Color(0xFFF1F8F5), Color(0xFF2E7D32), Color(0xFFA5D6A7))),
                            Triple("Total", datos.total, Triple(Color(0xFFE0F7FA), Color(0xFF00838F), Color(0xFF80DEEA)))
                        )
                        estadisticas.forEach { (titulo, valor, estilo) ->
                            val (bgColor, textColor, borderColor) = estilo
                            Card(
                                Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = bgColor),
                                border = BorderStroke(1.dp, borderColor)
                            ) {
                                Column(
                                    Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(valor.toString(), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = textColor)
                                    Text(titulo, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = textColor)
                                }
                            }
                        }
                    }
                }
            }

            // Tarjeta Cambiar de perfil (con icono SwapHoriz a la izquierda)
            Card(
                onClick = onCambiarPerfil,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = colores.primary)
            ) {
                Row(
                    Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.SwapHoriz, null, tint = colores.onPrimary, modifier = Modifier.size(24.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                        Text("Cambiar de perfil", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colores.onPrimary)
                        Text("Modo práctica: ir a vista Soporte TI", fontSize = 11.sp, color = colores.onPrimary.copy(alpha = 0.8f))
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = colores.onPrimary, modifier = Modifier.size(20.dp))
                }
            }

            Text("Preferencias", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colores.onBackground)

            // Ajustes / Preferencias tarjeta
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
                            checked = false,
                            onCheckedChange = { onAjustes() },
                            enabled = !ocupado
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
                            Text("Avisar cambios de estado", fontSize = 11.sp, color = colores.onSurfaceVariant)
                        }
                        Switch(
                            checked = true,
                            onCheckedChange = { onAjustes() },
                            enabled = !ocupado
                        )
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

            OutlinedButton(
                onClick = onCerrarSesion,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                border = BorderStroke(1.dp, colores.error),
                enabled = !ocupado
            ) {
                Text("Cerrar sesión", color = colores.error, fontWeight = FontWeight.SemiBold)
            }

            if (ocupado) LinearProgressIndicator(Modifier.fillMaxWidth())
            error?.let { Text(it, color = colores.error, fontSize = 12.sp) }
        }
    }
}
