package com.espiritu.ayudacampus.ui.cliente

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BienvenidaScreen(onIngresar: () -> Unit) {
    val colores = MaterialTheme.colorScheme
    Surface(color = colores.primaryContainer.copy(alpha = .35f).compositeOver(colores.background)) {
        BoxWithConstraints(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars),
            contentAlignment = Alignment.TopCenter) {
            val alto = maxHeight
            val ladoIcono = ((maxWidth - 72.dp) / 3).coerceIn(48.dp, 88.dp)
            Column(Modifier.widthIn(max = 480.dp).fillMaxWidth().verticalScroll(rememberScrollState())
                .heightIn(min = alto).padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height((alto * .15f).coerceAtMost(112.dp)))
                    MarcaAyudaCampus()
                    Spacer(Modifier.height(40.dp))
                    Text("AyudaCampus", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("Reporta y sigue tus incidencias", fontSize = 14.sp, color = colores.onSurfaceVariant)
                }
                Row(Modifier.padding(top = 56.dp, bottom = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf(IconoAcceso.Computadora, IconoAcceso.Wifi, IconoAcceso.Impresora).forEach { icono ->
                        Surface(Modifier.size(ladoIcono), shape = RoundedCornerShape(22.dp),
                            color = colores.primaryContainer, contentColor = colores.primary) {
                            Box(contentAlignment = Alignment.Center) { IconoAccesoFino(icono, Modifier.size(36.dp)) }
                        }
                    }
                }
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    // Registro aún no implementado: ambas acciones abren el Login existente.
                    Button(onClick = onIngresar, modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(10.dp)) { Text("Comenzar", fontWeight = FontWeight.SemiBold) }
                    TextButton(onClick = onIngresar) { Text("Ya tengo una cuenta", fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}

@Composable
private fun MarcaAyudaCampus() {
    Surface(Modifier.size(108.dp), shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary) {
        val tinta = LocalContentColor.current
        Box(contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(66.dp)) {
                val w = size.width
                val h = size.height
                val contorno = Path().apply {
                    moveTo(.18f * w, .12f * h)
                    lineTo(.82f * w, .12f * h)
                    quadraticTo(.9f * w, .12f * h, .9f * w, .2f * h)
                    lineTo(.9f * w, .63f * h)
                    quadraticTo(.9f * w, .71f * h, .82f * w, .71f * h)
                    lineTo(.46f * w, .71f * h)
                    lineTo(.26f * w, .91f * h)
                    lineTo(.26f * w, .71f * h)
                    lineTo(.18f * w, .71f * h)
                    quadraticTo(.1f * w, .71f * h, .1f * w, .63f * h)
                    lineTo(.1f * w, .2f * h)
                    quadraticTo(.1f * w, .12f * h, .18f * w, .12f * h)
                    close()
                }
                drawPath(contorno, tinta, style = Stroke(1.5.dp.toPx()))
                listOf(.35f, .5f, .65f).forEach { x -> drawCircle(tinta, 1.3.dp.toPx(), Offset(x * w, .42f * h)) }
            }
        }
    }
}
