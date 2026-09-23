package com.espiritu.tecsupfit.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.espiritu.tecsupfit.model.FitnessClass
import com.espiritu.tecsupfit.ui.screens.*

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedClass by remember { mutableStateOf<FitnessClass?>(null) }
    var isConfirming by remember { mutableStateOf(false) }

    val showBottomBar = selectedClass == null && !isConfirming

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = Color.White) {
                    val items = listOf("Inicio", "Reservas", "Rutinas", "Perfil")
                    val icons = listOf(Icons.Default.Home, Icons.Default.DateRange, Icons.Default.FitnessCenter, Icons.Default.Person)

                    items.forEachIndexed { index, item ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = { Icon(icons[index], contentDescription = item) },
                            label = { Text(item) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF005A42),
                                selectedTextColor = Color(0xFF005A42),
                                indicatorColor = Color(0xFFE8F5E9)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding)) {
            when {
                isConfirming && selectedClass != null -> {
                    ConfirmationScreen(
                        fitnessClass = selectedClass!!,
                        onViewReservations = {
                            isConfirming = false
                            selectedClass = null
                            selectedTab = 1
                        }
                    )
                }
                selectedClass != null -> {
                    DetailScreen(
                        fitnessClass = selectedClass!!,
                        onBack = { selectedClass = null },
                        onReserve = { isConfirming = true }
                    )
                }
                else -> {
                    when (selectedTab) {
                        0 -> HomeScreen(onClassSelected = { selectedClass = it })
                        1 -> ReservationsScreen()
                        2 -> HomeScreen(onClassSelected = { selectedClass = it })
                        3 -> ProfileScreen()
                    }
                }
            }
        }
    }
}