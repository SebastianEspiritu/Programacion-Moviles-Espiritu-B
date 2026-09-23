package com.espiritu.tecsupfit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.espiritu.tecsupfit.model.Reservation

val initialReservations = listOf(
    Reservation(1, "Cross Training", "Hoy, 6:00 pm", "Confirmada"),
    Reservation(2, "Yoga funcional", "Ayer, 7:00 am", "Completada")
)

@Composable
fun ReservationsScreen() {
    var reservationsList by remember { mutableStateOf(initialReservations) }
    var reservationToCancel by remember { mutableStateOf<Reservation?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(20.dp)
    ) {
        Text("Mis reservas", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        if (reservationsList.isEmpty()) {
            Text("No tienes reservas activas.", color = Color.Gray, fontSize = 14.sp)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(reservationsList) { reservation ->
                    ReservationCard(
                        reservation = reservation,
                        onCancelClick = { reservationToCancel = reservation }
                    )
                }
            }
        }
    }

    // Cuadro de diálogo AlertDialog para la cancelación de reserva
    reservationToCancel?.let { reservation ->
        AlertDialog(
            onDismissRequest = { reservationToCancel = null },
            title = { Text("Cancelar Reserva") },
            text = { Text("¿Estás seguro de que deseas cancelar la reserva de ${reservation.className}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        reservationsList = reservationsList.filter { it.id != reservation.id }
                        reservationToCancel = null
                    }
                ) {
                    Text("Sí, cancelar", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { reservationToCancel = null }) {
                    Text("Volver")
                }
            }
        )
    }
}

@Composable
fun ReservationCard(reservation: Reservation, onCancelClick: () -> Unit) {
    val isConfirmed = reservation.status == "Confirmada"
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(40.dp)
                        .background(if (isConfirmed) Color(0xFF005A42) else Color.LightGray)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(reservation.className, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(reservation.timeAndRoom, fontSize = 13.sp, color = Color.Gray)
                    Text(
                        text = reservation.status,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isConfirmed) Color(0xFF005A42) else Color.Gray
                    )
                }
            }

            if (isConfirmed) {
                IconButton(onClick = onCancelClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Cancelar reserva", tint = Color.Red)
                }
            }
        }
    }
}

@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Mi perfil", fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(30.dp))

        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8F5E9)),
            contentAlignment = Alignment.Center
        ) {
            Text("SE", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF005A42))
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("Sebastián Espíritu", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("Plan Premium", fontSize = 13.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(30.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(number = "14", label = "Clases", modifier = Modifier.weight(1f))
            StatCard(number = "3", label = "Rachas", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun StatCard(number: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(number, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(label, fontSize = 12.sp, color = Color.Gray)
        }
    }
}