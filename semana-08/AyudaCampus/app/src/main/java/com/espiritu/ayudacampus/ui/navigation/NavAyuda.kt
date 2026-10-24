package com.espiritu.ayudacampus.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.espiritu.ayudacampus.data.local.entity.Usuario
import com.espiritu.ayudacampus.viewmodel.AyudaViewModelFactory
import com.espiritu.ayudacampus.viewmodel.MisTicketsViewModel
import com.espiritu.ayudacampus.ui.cliente.MisTicketsScreen
import com.espiritu.ayudacampus.ui.cliente.NuevoTicketScreen
import com.espiritu.ayudacampus.viewmodel.NuevoTicketViewModel
import com.espiritu.ayudacampus.viewmodel.DetalleTicketViewModel
import com.espiritu.ayudacampus.ui.cliente.DetalleTicketScreen
import com.espiritu.ayudacampus.model.Rol
import com.espiritu.ayudacampus.ui.cliente.LoginScreen
import com.espiritu.ayudacampus.ui.cliente.SplashScreen
import com.espiritu.ayudacampus.ui.components.AppTopBar
import com.espiritu.ayudacampus.ui.theme.AyudaCampusTheme
import com.espiritu.ayudacampus.viewmodel.SesionViewModel
import com.espiritu.ayudacampus.viewmodel.PanelSoporteViewModel
import com.espiritu.ayudacampus.viewmodel.BandejaViewModel
import com.espiritu.ayudacampus.ui.soporte.PanelSoporteScreen
import com.espiritu.ayudacampus.ui.soporte.BandejaScreen

@Composable
fun NavAyuda(vm: SesionViewModel, fabricaTickets: (Usuario) -> AyudaViewModelFactory) {
    val nav = rememberNavController()
    val usuario by vm.usuarioActual.collectAsStateWithLifecycle()
    val restaurando by vm.restaurando.collectAsStateWithLifecycle()
    val errorInicializacion by vm.errorInicializacion.collectAsStateWithLifecycle()
    val oscuro by vm.modoOscuro.collectAsStateWithLifecycle()
    var rolSeleccionado by rememberSaveable { mutableStateOf(Rol.CLIENTE) }
    val rol = usuario?.rol?.let { Rol.valueOf(it) } ?: rolSeleccionado
    val entrada by nav.currentBackStackEntryAsState()
    val ruta = entrada?.destination?.route
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
        Scaffold(contentWindowInsets = WindowInsets(0, 0, 0, 0), bottomBar = {
            if (usuario?.rol == Rol.CLIENTE.name && ruta in listOf(Rutas.MIS_TICKETS, Rutas.NUEVO, Rutas.PERFIL, Rutas.DETALLE)) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    listOf(Triple(Rutas.MIS_TICKETS, "Tickets", Icons.AutoMirrored.Filled.List),
                        Triple(Rutas.NUEVO, "Nuevo", Icons.Default.Add),
                        Triple(Rutas.PERFIL, "Perfil", Icons.Default.Person)).forEach { (destino, texto, icono) ->
                        NavigationBarItem(selected = ruta == destino || (destino == Rutas.MIS_TICKETS && ruta == Rutas.DETALLE), onClick = {
                            nav.navigate(destino) {
                                popUpTo(Rutas.MIS_TICKETS) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }, icon = { Icon(icono, contentDescription = null) }, label = { Text(texto) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary))
                    }
                }
            }
            if (usuario?.rol == Rol.ADMIN.name && ruta in listOf(Rutas.PANEL, Rutas.BANDEJA_RUTA, Rutas.CATEGORIAS, Rutas.AJUSTES, Rutas.ATENDER)) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    listOf(Triple(Rutas.PANEL, "Panel", Icons.Default.Dashboard),
                        Triple(Rutas.BANDEJA, "Tickets", Icons.AutoMirrored.Filled.List),
                        Triple(Rutas.CATEGORIAS, "Categorías", Icons.Default.Category),
                        Triple(Rutas.AJUSTES, "Ajustes", Icons.Default.Settings)).forEach { (destino, texto, icono) ->
                        NavigationBarItem(selected = ruta == destino ||
                            (destino == Rutas.BANDEJA && ruta in listOf(Rutas.BANDEJA_RUTA, Rutas.ATENDER)), onClick = {
                            nav.navigate(destino) {
                                popUpTo(Rutas.PANEL) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }, icon = { Icon(icono, null) }, label = { Text(texto) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary))
                    }
                }
            }
        }) { padding ->
            NavHost(navController = nav, startDestination = Rutas.SPLASH, modifier = Modifier.padding(padding)) {
                composable(Rutas.SPLASH) { SplashScreen(errorInicializacion) }
                composable(Rutas.LOGIN) {
                    LoginScreen(vm, rolSeleccionado, { rolSeleccionado = it })
                }
                composable(Rutas.MIS_TICKETS) {
                    usuario?.takeIf { it.rol == Rol.CLIENTE.name }?.let { estudiante ->
                        val ticketsVm: MisTicketsViewModel = viewModel(
                            key = "misTickets-${estudiante.id}", factory = fabricaTickets(estudiante))
                        MisTicketsScreen(ticketsVm, { nav.navigate(Rutas.NUEVO) { launchSingleTop = true } },
                            { nav.navigate(Rutas.detalle(it)) })
                    }
                }
                composable(Rutas.NUEVO) {
                    usuario?.takeIf { it.rol == Rol.CLIENTE.name }?.let { estudiante ->
                        val nuevoVm: NuevoTicketViewModel = viewModel(
                            key = "nuevoTicket-${estudiante.id}", factory = fabricaTickets(estudiante))
                        NuevoTicketScreen(nuevoVm) {
                            // Recrear la lista limpia filtros y posición: el ticket nuevo aparece primero.
                            nav.navigate(Rutas.MIS_TICKETS) {
                                popUpTo(Rutas.MIS_TICKETS) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                }
                composable(Rutas.PERFIL) { DestinoPendiente("Perfil", "La pantalla de perfil estará disponible más adelante.") }
                composable(Rutas.DETALLE, arguments = listOf(navArgument("ticketId") { type = NavType.IntType })) { entradaDetalle ->
                    usuario?.takeIf { it.rol == Rol.CLIENTE.name }?.let { estudiante ->
                        val id = requireNotNull(entradaDetalle.arguments).getInt("ticketId")
                        val detalleVm: DetalleTicketViewModel = viewModel(
                            key = "detalle-${estudiante.id}-$id", factory = fabricaTickets(estudiante).paraDetalle(id))
                        DetalleTicketScreen(detalleVm) { nav.popBackStack() }
                    }
                }
                composable(Rutas.PANEL) {
                    SoloSoporte(usuario) { soporte ->
                        val panelVm: PanelSoporteViewModel = viewModel(factory = fabricaTickets(soporte))
                        PanelSoporteScreen(panelVm) { nav.navigate(Rutas.bandeja(alertas = true)) { launchSingleTop = true } }
                    }
                }
                composable(Rutas.BANDEJA_RUTA, arguments = listOf(navArgument("alertas") { type = NavType.BoolType; defaultValue = false })) { entradaBandeja ->
                    SoloSoporte(usuario) { soporte ->
                        val alertas = entradaBandeja.arguments?.getBoolean("alertas") ?: false
                        val bandejaVm: BandejaViewModel = viewModel(factory = fabricaTickets(soporte).paraBandeja(alertas))
                        BandejaScreen(bandejaVm) { nav.navigate(Rutas.atender(it)) }
                    }
                }
                composable(Rutas.CATEGORIAS) {
                    SoloSoporte(usuario) { DestinoPendiente("Categorías", "La gestión de categorías estará disponible en el siguiente paso.") }
                }
                composable(Rutas.AJUSTES) {
                    SoloSoporte(usuario) { DestinoPendiente("Ajustes", "Los ajustes estarán disponibles más adelante.") }
                }
                composable(Rutas.ATENDER, arguments = listOf(navArgument("ticketId") { type = NavType.IntType })) { entradaAtender ->
                    SoloSoporte(usuario) {
                        DestinoPendiente("Atender ticket #${entradaAtender.arguments?.getInt("ticketId")}",
                            "La atención de tickets estará disponible en el siguiente paso.")
                    }
                }
            }
        }
    }
}

@Composable
private fun DestinoPendiente(titulo: String, mensaje: String) {
    Scaffold(topBar = { AppTopBar(titulo) }) { padding ->
        Text(mensaje, Modifier.padding(padding).padding(24.dp))
    }
}

@Composable
private fun SoloSoporte(usuario: Usuario?, contenido: @Composable (Usuario) -> Unit) {
    if (usuario != null && usuario.id > 0 && usuario.rol == Rol.ADMIN.name) {
        contenido(usuario)
    } else {
        Text("Acceso exclusivo para Soporte TI.", Modifier.padding(24.dp))
    }
}
