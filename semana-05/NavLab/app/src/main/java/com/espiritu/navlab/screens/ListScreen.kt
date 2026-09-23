package com.espiritu.navlab.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.espiritu.navlab.navigation.Screen

private val PurplePrimary = Color(0xFF673AB7)
private val LilacLight = Color(0xFFF3E5F5)

private data class StudentMock(val name: String, val career: String, val initials: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(navController: NavController) {
    val students = listOf(
        StudentMock("Sebastián Espíritu", "Diseño & Desarrollo de Software", "SE"),
        StudentMock("María García López", "Ingeniería de Software", "MG"),
        StudentMock("Carlos Mendoza Vega", "Redes & Ciberseguridad", "CM"),
        StudentMock("Ana Torres Ríos", "Diseño & Desarrollo de Software", "AT"),
        StudentMock("Luis Paredes Ruiz", "Big Data e IA", "LP"),
        StudentMock("Sofía Castro Ramos", "Ingeniería de Software", "SC"),
        StudentMock("Diego Flores Huamán", "Diseño & Desarrollo de Software", "DF"),
        StudentMock("Lucía Morales Soto", "Redes & Ciberseguridad", "LM")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Directorio de Alumnos",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PurplePrimary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 12.dp,
                bottom = 16.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(students.size) { index ->
                val student = students[index]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate(Screen.Detail.createRoute(index + 1))
                        },
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(LilacLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = student.initials,
                                fontWeight = FontWeight.Bold,
                                color = PurplePrimary,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = student.name,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = student.career,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Ver detalle",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}