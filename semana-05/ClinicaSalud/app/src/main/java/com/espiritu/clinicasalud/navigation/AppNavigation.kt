package com.espiritu.clinicasalud.navigation


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.espiritu.clinicasalud.model.Cita
import com.espiritu.clinicasalud.model.listaMedicos
import com.espiritu.clinicasalud.model.EstadoCita
import com.espiritu.clinicasalud.screens.*
import kotlinx.coroutines.launch

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Estado hoisteado (sin ViewModel): lista de citas compartida entre pantallas
    val citas = remember { mutableStateListOf<Cita>() }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(20.dp)
                ) {
                    Text(
                        "Sebastián Espíritu",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        "Clínica Salud+",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text("Inicio") },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    selected = currentRoute == Screen.Inicio.route,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Inicio.route) {
                            popUpTo(Screen.Inicio.route) { inclusive = true }
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Mis citas") },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    selected = currentRoute == Screen.MisCitas.route,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.MisCitas.route)
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Historial médico") },
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                    selected = currentRoute == Screen.HistorialMedico.route,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.HistorialMedico.route)
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    ) {
        NavHost(navController = navController, startDestination = Screen.Inicio.route) {

            composable(Screen.Inicio.route) {
                InicioScreen(
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onMedicoClick = { medicoId ->
                        navController.navigate(Screen.PerfilMedico.createRoute(medicoId))
                    }
                )
            }

            composable(Screen.PerfilMedico.route) { backStackEntry ->
                val medicoId = backStackEntry.arguments?.getString("medicoId")?.toIntOrNull()
                val medico = listaMedicos.find { it.id == medicoId }
                if (medico != null) {
                    PerfilMedicoScreen(
                        medico = medico,
                        onBack = { navController.popBackStack() },
                        onAgendarCita = {
                            navController.navigate(Screen.AgendarCita.createRoute(medico.id))
                        }
                    )
                }
            }

            composable(Screen.AgendarCita.route) { backStackEntry ->
                val medicoId = backStackEntry.arguments?.getString("medicoId")?.toIntOrNull()
                val medico = listaMedicos.find { it.id == medicoId }
                if (medico != null) {
                    AgendarCitaScreen(
                        medico = medico,
                        onBack = { navController.popBackStack() },
                        onConfirmar = { fecha, hora ->
                            citas.add(Cita(medico = medico, fecha = fecha, hora = hora))
                            navController.navigate(Screen.Confirmacion.route)
                        }
                    )
                }
            }

            composable(Screen.Confirmacion.route) {
                val ultimaCita = citas.lastOrNull()
                if (ultimaCita != null) {
                    ConfirmacionScreen(
                        cita = ultimaCita,
                        onVolverInicio = {
                            navController.navigate(Screen.Inicio.route) {
                                popUpTo(Screen.Inicio.route) { inclusive = true }
                            }
                        }
                    )
                }
            }

            composable(Screen.MisCitas.route) {
                MisCitasScreen(
                    citas = citas,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onCancelarCita = { citaACancelar ->
                        val index = citas.indexOf(citaACancelar)
                        if (index != -1) {
                            citas[index] = citaACancelar.copy(estado = EstadoCita.CANCELADA)
                        }
                    }
                )
            }

            composable(Screen.HistorialMedico.route) {
                HistorialMedicoScreen(
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            }
        }
    }
}