package com.espiritu.ayudacampus.ui.cliente

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.espiritu.ayudacampus.model.Rol
import com.espiritu.ayudacampus.viewmodel.SesionViewModel

@Composable
fun LoginScreen(vm: SesionViewModel, rol: Rol, onRolChange: (Rol) -> Unit) {
    var correo by rememberSaveable { mutableStateOf("") }
    // La clave y su visibilidad no se persisten al recrear la pantalla.
    var clave by remember { mutableStateOf("") }
    var mostrarClave by remember { mutableStateOf(false) }
    var aviso by remember { mutableStateOf<String?>(null) }
    val error by vm.error.collectAsStateWithLifecycle()
    val ingresando by vm.ingresando.collectAsStateWithLifecycle()
    val colores = MaterialTheme.colorScheme
    val fondo = if (colores.background.luminance() > .5f) Color.White else colores.background
    Surface(color = fondo) {
        Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars).imePadding(),
            contentAlignment = Alignment.TopCenter) {
            Column(Modifier.widthIn(max = 480.dp).fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 36.dp)) {
                Text("Bienvenido", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text("Ingresa para continuar", fontSize = 14.sp, color = colores.onSurfaceVariant)
                Spacer(Modifier.height(24.dp))
                Surface(shape = RoundedCornerShape(12.dp), color = colores.surfaceVariant.copy(alpha = .35f)) {
                    Row(Modifier.fillMaxWidth().padding(3.dp).selectableGroup()) {
                        Rol.entries.forEach { opcion ->
                            val seleccionado = rol == opcion
                            Surface(Modifier.weight(1f).selectable(selected = seleccionado,
                                enabled = !ingresando, role = Role.Tab,
                                onClick = { onRolChange(opcion); vm.limpiarError() }),
                                shape = RoundedCornerShape(9.dp),
                                color = if (seleccionado) colores.primary else Color.Transparent,
                                contentColor = if (seleccionado) colores.onPrimary else colores.onSurfaceVariant) {
                                Box(Modifier.heightIn(min = 36.dp).padding(horizontal = 8.dp, vertical = 7.dp),
                                    contentAlignment = Alignment.Center) {
                                    Text(if (opcion == Rol.CLIENTE) "Estudiante" else "Soporte TI",
                                        fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                CampoLogin(correo, { correo = it; vm.limpiarError() }, "Correo",
                    IconoAcceso.Correo, !ingresando,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
                Spacer(Modifier.height(12.dp))
                CampoLogin(clave, { clave = it; vm.limpiarError() }, "Contrase\u00f1a",
                    IconoAcceso.Candado, !ingresando,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (mostrarClave) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { mostrarClave = !mostrarClave }, enabled = !ingresando) {
                            Icon(if (mostrarClave) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                if (mostrarClave) "Ocultar contrase\u00f1a" else "Mostrar contrase\u00f1a", Modifier.size(18.dp))
                        }
                    })
                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = colores.error, style = MaterialTheme.typography.bodySmall)
                }
                TextButton(onClick = { aviso = "Recuperaci\u00f3n de contrase\u00f1a" }, enabled = !ingresando,
                    modifier = Modifier.align(Alignment.End), contentPadding = PaddingValues(vertical = 8.dp)) {
                    Text("\u00bfOlvidaste tu contrase\u00f1a?", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(12.dp))
                Button(onClick = { vm.login(correo, clave, rol) }, enabled = !ingresando,
                    modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(10.dp)) {
                    if (ingresando) {
                        CircularProgressIndicator(Modifier.size(18.dp), color = colores.onPrimary, strokeWidth = 2.dp)
                        Spacer(Modifier.width(10.dp))
                    }
                    Text(if (ingresando) "Ingresando\u2026" else "Ingresar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(16.dp))
                if (rol == Rol.CLIENTE) {
                    TextButton(onClick = { aviso = "Registro de estudiantes" }, enabled = !ingresando,
                        modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text("\u00bfNo tienes cuenta? Reg\u00edstrate", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Surface(shape = RoundedCornerShape(10.dp), color = colores.primaryContainer.copy(alpha = .35f)) {
                        Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Modo pr\u00e1ctica", color = colores.primary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Puedes cambiar de perfil desde Ajustes para probar ambos roles.",
                                color = colores.onSurfaceVariant, fontSize = 12.sp, lineHeight = 17.sp)
                        }
                    }
                }
            }
        }
    }
    aviso?.let { titulo ->
        AlertDialog(onDismissRequest = { aviso = null }, title = { Text(titulo) },
            text = { Text("Esta funci\u00f3n todav\u00eda no est\u00e1 disponible. No se han enviado correos ni creado cuentas.") },
            confirmButton = { TextButton(onClick = { aviso = null }) { Text("Entendido") } })
    }
}

/** Etiqueta persistente dentro del borde, sin depender de que el campo tenga texto. */
@Composable
private fun CampoLogin(
    valor: String,
    onValueChange: (String) -> Unit,
    etiqueta: String,
    icono: IconoAcceso,
    habilitado: Boolean,
    keyboardOptions: KeyboardOptions,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    val colores = MaterialTheme.colorScheme
    val interaccion = remember { MutableInteractionSource() }
    val enfocado by interaccion.collectIsFocusedAsState()
    val forma = RoundedCornerShape(10.dp)
    BasicTextField(
        value = valor, onValueChange = onValueChange, enabled = habilitado,
        singleLine = true, keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation, interactionSource = interaccion,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = colores.onSurface),
        cursorBrush = androidx.compose.ui.graphics.SolidColor(colores.primary),
        modifier = Modifier.fillMaxWidth().semantics { contentDescription = etiqueta }
            .border(if (enfocado) 1.5.dp else .75.dp,
                if (enfocado) colores.primary else colores.outlineVariant.copy(alpha = .7f), forma),
        decorationBox = { entrada ->
            Row(Modifier.heightIn(min = 52.dp).padding(start = 10.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically) {
                IconoAccesoFino(icono, Modifier.size(18.dp), colores.primary)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f).padding(vertical = 6.dp)) {
                    Text(etiqueta, fontSize = 10.sp, lineHeight = 12.sp,
                        color = colores.onSurfaceVariant)
                    Spacer(Modifier.height(2.dp))
                    entrada()
                }
                trailingIcon?.invoke()
            }
        }
    )
}
