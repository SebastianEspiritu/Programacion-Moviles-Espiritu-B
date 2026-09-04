package com.espiritu.registronotas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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

        // Cuerpos con Scroll vertical
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

        }
    }
}