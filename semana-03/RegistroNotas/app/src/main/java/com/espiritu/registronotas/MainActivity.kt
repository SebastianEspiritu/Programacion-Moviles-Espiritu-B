package com.espiritu.registronotas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import com.espiritu.registronotas.ui.theme.RegistroNotasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RegistroNotasTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RegistroNotasScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun RegistroNotasScreen(modifier: Modifier = Modifier) {
    var notaFP by remember { mutableFloatStateOf(0f) }
    var notaPOO by remember { mutableFloatStateOf(0f) }
    var notaPM by remember { mutableFloatStateOf(0f) }
    var notaBD by remember { mutableFloatStateOf(0f) }
    // --- ESTADOS DE CONTROLES Y RESULTADO ---
    var redondearPromedio by remember { mutableStateOf(false) }
    var notasConfirmadas by remember { mutableStateOf(false) }
    var mostrarResultado by remember { mutableStateOf(false) }

    // Colores de la interfaz
    val purpleHeader = Color(0xFF5E43A5)
    val purplePrimary = Color(0xFF5E43A5)
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFECE6F8), Color(0xFFF7F4FD))
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(brush = backgroundGradient)
    ) {
        // Barra Superior: Registro de Notas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(purpleHeader)
                .padding(vertical = 18.dp, horizontal = 20.dp)
        ) {
            Text(
                text = "Registro de Notas",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Cuerpos con Scroll
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Sección Título
            Text(
                text = "Notas del ciclo",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1B1F)
            )
            Text(
                text = "Desliza para asignar cada nota (0 a 20)",
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            CursoItem(
                nombre = "Fundamentos de Programación",
                pesoText = "(20%)",
                nota = notaFP,
                onNotaChange = { notaFP = it }
            )

            CursoItem(
                nombre = "Programación Orientada a Objetos",
                pesoText = "(25%)",
                nota = notaPOO,
                onNotaChange = { notaPOO = it }
            )

            CursoItem(
                nombre = "Programación en Móviles",
                pesoText = "(30%)",
                nota = notaPM,
                onNotaChange = { notaPM = it }
            )

            CursoItem(
                nombre = "Base de Datos",
                pesoText = "(25%)",
                nota = notaBD,
                onNotaChange = { notaBD = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Switch: Redondear promedio final
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Redondear promedio final",
                    fontSize = 15.sp,
                    color = Color(0xFF1C1B1F)
                )
                Switch(
                    checked = redondearPromedio,
                    onCheckedChange = { redondearPromedio = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = purplePrimary
                    )
                )
            }

            // Checkbox: Confirmo que las notas son correctas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = notasConfirmadas,
                    onCheckedChange = {
                        notasConfirmadas = it
                        if (!it) mostrarResultado = false
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = purplePrimary
                    )
                )
                Text(
                    text = "Confirmo que las notas son correctas",
                    fontSize = 14.sp,
                    color = Color(0xFF1C1B1F)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón Calcular Promedio (deshabilitado si no confirma)
            Button(
                onClick = { mostrarResultado = true },
                enabled = notasConfirmadas,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = purplePrimary,
                    disabledContainerColor = Color(0xFFC0B8DA),
                    contentColor = Color.White,
                    disabledContentColor = Color.White
                )
            ) {
                Text(
                    text = "CALCULAR PROMEDIO",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            // Mensaje previo al cálculo
            if (!mostrarResultado) {
                Text(
                    text = "Asigna las notas y confirma para calcular",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
            if (mostrarResultado) {
                // 1. Cálculo del promedio ponderado (Pesos: 20%, 25%, 30%, 25%)
                val promedioPonderado = (notaFP.toInt() * 0.20f) +
                        (notaPOO.toInt() * 0.25f) +
                        (notaPM.toInt() * 0.30f) +
                        (notaBD.toInt() * 0.25f)

                // 2. Aplicar redondeo según el Switch
                val promedioFinal = if (redondearPromedio) {
                    promedioPonderado.roundToInt().toFloat()
                } else {
                    promedioPonderado
                }

                // 3. Reglas de negocio para la observación y color del Chip
                val (observacion, chipBgColor, chipTextColor) = when {
                    promedioFinal >= 17f -> Triple("EXCELENTE", Color(0xFFD1E7DD), Color(0xFF0F5132))      // Verde oscuro
                    promedioFinal >= 13f -> Triple("APROBADO", Color(0xFFE2F0D9), Color(0xFF2E7D32))       // Verde
                    promedioFinal >= 10f -> Triple("EN RECUPERACIÓN", Color(0xFFFFF3CD), Color(0xFF664D03))// Ámbar
                    else -> Triple("DESAPROBADO", Color(0xFFF8D7DA), Color(0xFF842029))                    // Rojo
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tarjeta de Resultados (Borde morado claro)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD0C4E8))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Promedio ponderado:  ${String.format(java.util.Locale.US, "%.2f", promedioPonderado)}",
                            fontSize = 15.sp,
                            color = Color(0xFF1C1B1F)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "Promedio final:  ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = purplePrimary
                            )
                            Text(
                                text = if (redondearPromedio) "${promedioFinal.toInt()}" else String.format(java.util.Locale.US, "%.2f", promedioFinal),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = purplePrimary
                            )
                        }

                        if (redondearPromedio) {
                            Text(
                                text = "(redondeado)",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Chip de Observación
                        Surface(
                            color = chipBgColor,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = observacion,
                                color = chipTextColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Mensaje de Confirmación Verde
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✓  Promedio calculado correctamente",
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            // Pie de página fijo al final
            Spacer(modifier = Modifier.height(30.dp))
            Text(
                text = "Desarrollado por: Sebastian Espiritu",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun CursoItem(
    nombre: String,
    pesoText: String,
    nota: Float,
    onNotaChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1C1B1F)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = pesoText,
                    fontSize = 12.sp,
                    color = Color(0xFF6750A4)
                )
            }

            // Badge con la nota en vivo
            Surface(
                color = Color(0xFFECE6F8),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "${nota.toInt()}",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5E43A5),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 14.sp
                )
            }
        }

        // Slider con paso entero (0 a 20)
        Slider(
            value = nota,
            onValueChange = onNotaChange,
            valueRange = 0f..20f,
            steps = 19,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF5E43A5),
                activeTrackColor = Color(0xFF5E43A5),
                inactiveTrackColor = Color(0xFFE2DBEC)
            )
        )
    }
}