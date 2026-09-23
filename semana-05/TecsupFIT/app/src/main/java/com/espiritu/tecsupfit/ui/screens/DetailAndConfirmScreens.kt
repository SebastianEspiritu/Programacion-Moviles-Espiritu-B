package com.espiritu.tecsupfit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.espiritu.tecsupfit.model.FitnessClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(fitnessClass: FitnessClass, onBack: () -> Unit, onReserve: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de clase", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color(0xFF005A42))
            }

            Spacer(modifier = Modifier.height(20.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(fitnessClass.title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("${fitnessClass.timeAndRoom} · ${fitnessClass.duration}", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Text(fitnessClass.description, fontSize = 14.sp, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(16.dp))
                Text("${fitnessClass.availableSpots} de ${fitnessClass.totalSpots} cupos disponibles", fontSize = 14.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = onReserve,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005A42)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Reservar cupo", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ConfirmationScreen(fitnessClass: FitnessClass, onViewReservations: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8F5E9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color(0xFF005A42))
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("¡Cupo reservado!", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(fitnessClass.title, fontSize = 16.sp, color = Color.Gray)
        Text("Hoy, ${fitnessClass.timeAndRoom}", fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = onViewReservations,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Ver mis reservas", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}