package com.espiritu.ayudacampus.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.espiritu.ayudacampus.model.Rol
import com.espiritu.ayudacampus.ui.cliente.LoginScreen
import com.espiritu.ayudacampus.ui.cliente.SplashScreen
import com.espiritu.ayudacampus.ui.components.AppTopBar
import com.espiritu.ayudacampus.ui.theme.AyudaCampusTheme
import com.espiritu.ayudacampus.viewmodel.SesionViewModel

@Composable
fun NavAyuda(vm: SesionViewModel) {
    val nav = rememberNavController()
    val usuario by vm.usuarioActual.collectAsStateWithLifecycle()
    val restaurando by vm.restaurando.collectAsStateWithLifecycle()
    val errorInicializacion by vm.errorInicializacion.collectAsStateWithLifecycle()
    val oscuro by vm.modoOscuro.collectAsStateWithLifecycle()
    var rolSeleccionado by rememberSaveable { mutableStateOf(Rol.CLIENTE) }
    val rol = usuario?.rol?.let { Rol.valueOf(it) } ?: rolSeleccionado
    LaunchedEffect(restaurando, errorInicializacion, usuario) {
        val destino = when {
            restaurando || errorInicializacion != null -> Rutas.SPLASH
            usuario == null -> Rutas.LOGIN
            usuario?.rol == Rol.ADMIN.name -> Rutas.PANEL
            else -> Rutas.MIS_TICKETS
        }
        nav.navigate(destino) {
            popUpTo(nav.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }
    AyudaCampusTheme(rol = rol, darkTheme = oscuro) {
        NavHost(navController = nav, startDestination = Rutas.SPLASH) {
            composable(Rutas.SPLASH) { SplashScreen(errorInicializacion) }
            composable(Rutas.LOGIN) {
                LoginScreen(vm, rolSeleccionado, { rolSeleccionado = it })
            }
            // Puntos de entrada: las pantallas funcionales pertenecen a los pasos 7 y 10.
            composable(Rutas.MIS_TICKETS) {
                InicioPerfil("Mis tickets", usuario?.nombre.orEmpty(), "Estudiante")
            }
            composable(Rutas.PANEL) {
                InicioPerfil("Panel de soporte", usuario?.nombre.orEmpty(), "Soporte TI")
            }
        }
    }
}

@Composable
private fun InicioPerfil(titulo: String, nombre: String, perfil: String) {
    Scaffold(topBar = { AppTopBar(titulo) }) { padding ->
        Column(Modifier.padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Hola, $nombre", style = MaterialTheme.typography.headlineSmall)
            Text(perfil, color = MaterialTheme.colorScheme.primary)
        }
    }
}
