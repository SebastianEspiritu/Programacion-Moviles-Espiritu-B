package com.espiritu.tecsupfit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.espiritu.tecsupfit.model.FitnessClass

val sampleClasses = listOf(
    FitnessClass(1, "Yoga funcional", "7:00 am · Sala 2", "45 min", "Mejora tu flexibilidad y equilibrio con posturas dinámicas.", 10, 15),
    FitnessClass(2, "Cross Training", "6:00 pm · Sala 1", "45 min", "Entrenamiento funcional de alta intensidad. Cupos limitados.", 8, 12),
    FitnessClass(3, "Spinning", "7:30 pm · Sala 3", "50 min", "Sesión de cardio de alta intensidad sobre bicicleta estática.", 5, 20)
)

@Composable
fun HomeScreen(onClassSelected: (FitnessClass) -> Unit) {
    var selectedFilter by remember { mutableStateOf("Hoy") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        // Cabecera superior
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF005A42))
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column {
                Text("TECSUP Fit", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Hola, Sebastián", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            // Chips de filtro
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val filters = listOf("Hoy", "Esta semana")
                items(filters) { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) Color(0xFF005A42) else Color(0xFFE0E0E0))
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = filter,
                            color = if (isSelected) Color.White else Color.Black,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("Clases disponibles", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(12.dp))

            // Lista de clases
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(sampleClasses) { fitnessClass ->
                    ClassCard(fitnessClass = fitnessClass, onClick = { onClassSelected(fitnessClass) })
                }
            }
        }
    }
}

@Composable
fun ClassCard(fitnessClass: FitnessClass, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = Color(0xFF005A42))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(fitnessClass.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(fitnessClass.timeAndRoom, fontSize = 13.sp, color = Color.Gray)
            }
        }
    }
}