package com.espiritu.tecsupfit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.espiritu.tecsupfit.model.Routine
import com.espiritu.tecsupfit.ui.theme.TecsupBlue

val sampleRoutines = listOf(
    Routine(1, "Rutina de Cardio PE", "Cardio", 30, 250),
    Routine(2, "Fuerza y Torso", "Musculación", 45, 380),
    Routine(3, "Piernas & Funcional", "HIIT", 40, 400)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("TECSUP Fit - Sebastián Espíritu", color = Color.White)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TecsupBlue)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sampleRoutines) { routine ->
                RoutineCard(routine)
            }
        }
    }
}

@Composable
fun RoutineCard(routine: Routine) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = routine.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Categoría: ${routine.category} | ${routine.durationMinutes} min | ${routine.calories} kcal",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}