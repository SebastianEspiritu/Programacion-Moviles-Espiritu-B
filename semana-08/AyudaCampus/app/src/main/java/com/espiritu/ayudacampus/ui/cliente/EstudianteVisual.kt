package com.espiritu.ayudacampus.ui.cliente

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun superficieEstudiante(): Color =
    if (MaterialTheme.colorScheme.background.luminance() > .5f) Color.White
    else MaterialTheme.colorScheme.surface

internal fun iconoCategoriaEstudiante(clave: String): ImageVector = when (clave.lowercase()) {
    "pc" -> Icons.Outlined.Computer
    "code" -> Icons.Outlined.Code
    "wifi" -> Icons.Outlined.Wifi
    "key" -> Icons.Outlined.Key
    "printer" -> Icons.Outlined.Print
    else -> Icons.Outlined.Category
}

/** Campos de esta etapa: etiqueta permanente y errores, sin cambiar formularios compartidos. */
@Composable
internal fun CampoIncidencia(
    valor: String,
    onValueChange: (String) -> Unit,
    etiqueta: String,
    enabled: Boolean,
    error: String? = null,
    multilinea: Boolean = false,
    icono: ImageVector? = null
) {
    val colores = MaterialTheme.colorScheme
    val interaccion = remember { MutableInteractionSource() }
    val enfocado by interaccion.collectIsFocusedAsState()
    val borde = if (error != null) colores.error else if (enfocado) colores.primary else colores.outlineVariant
    Column {
        Surface(color = superficieEstudiante(), shape = RoundedCornerShape(10.dp)) {
            BasicTextField(value = valor, onValueChange = onValueChange, enabled = enabled,
                singleLine = !multilinea, minLines = if (multilinea) 3 else 1,
                keyboardOptions = KeyboardOptions.Default,
                interactionSource = interaccion,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = colores.onSurface),
                cursorBrush = SolidColor(colores.primary),
                modifier = Modifier.fillMaxWidth().semantics {
                    contentDescription = etiqueta
                    error?.let { error(it) }
                }.border(if (enfocado) 1.5.dp else .75.dp, borde, RoundedCornerShape(10.dp)),
                decorationBox = { entrada ->
                    Row(Modifier.heightIn(min = if (multilinea) 104.dp else 52.dp)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        icono?.let {
                            Icon(it, null, Modifier.size(20.dp), tint = colores.primary)
                            Spacer(Modifier.width(10.dp))
                        }
                        Column(Modifier.weight(1f)) {
                            Text(etiqueta, fontSize = 10.sp, lineHeight = 12.sp, color = colores.onSurfaceVariant)
                            Spacer(Modifier.height(3.dp))
                            entrada()
                        }
                    }
                })
        }
        error?.let { Text(it, Modifier.padding(top = 4.dp), color = colores.error,
            style = MaterialTheme.typography.bodySmall) }
    }
}
