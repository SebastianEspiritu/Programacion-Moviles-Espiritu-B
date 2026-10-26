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
import com.espiritu.ayudacampus.ui.cliente.BienvenidaScreen
import com.espiritu.ayudacampus.ui.theme.AyudaCampusTheme
import com.espiritu.ayudacampus.viewmodel.SesionViewModel
import com.espiritu.ayudacampus.viewmodel.PanelSoporteViewModel
import com.espiritu.ayudacampus.viewmodel.BandejaViewModel
import com.espiritu.ayudacampus.ui.soporte.PanelSoporteScreen
import com.espiritu.ayudacampus.ui.soporte.BandejaScreen
import com.espiritu.ayudacampus.ui.soporte.AtenderTicketScreen
import com.espiritu.ayudacampus.ui.soporte.CategoriasScreen
import com.espiritu.ayudacampus.ui.soporte.FormularioCategoriaScreen
import com.espiritu.ayudacampus.viewmodel.PerfilViewModel
import com.espiritu.ayudacampus.ui.cliente.PerfilScreen
import com.espiritu.ayudacampus.ui.soporte.AjustesScreen
import com.espiritu.ayudacampus.viewmodel.AtenderViewModel
import com.espiritu.ayudacampus.viewmodel.CategoriasViewModel

@Composable
fun NavAyuda(vm: SesionViewModel, fabricaTickets: (Usuario) -> AyudaViewModelFactory) {
    val usuario by vm.usuarioActual.collectAsStateWithLifecycle()
    val ajustes by vm.ajustes.collectAsStateWithLifecycle()
    val cerrando by vm.cerrandoSesion.collectAsStateWithLifecycle()
    val errorSesion by vm.error.collectAsStateWithLifecycle()
    val restaurando by vm.restaurando.collectAsStateWithLifecycle()
    val errorInicializacion by vm.errorInicializacion.collectAsStateWithLifecycle()
    val oscuro by vm.modoOscuro.collectAsStateWithLifecycle()
    var rolSeleccionado by rememberSaveable { mutableStateOf(Rol.CLIENTE) }
    // Se conserva fuera del grafo por cuenta: salir/cambiar de perfil nunca abre bienvenida.
    var loginSolicitado by rememberSaveable { mutableStateOf(false) }
    val cerrarSesion: () -> Unit = {
        loginSolicitado = true
        vm.cerrarSesion()
    }
    val cambiarPerfil: () -> Unit = {
        loginSolicitado = true
        rolSeleccionado = if (usuario?.rol == Rol.ADMIN.name) Rol.CLIENTE else Rol.ADMIN
        vm.cambiarPerfil()
    }
    val rol = usuario?.rol?.let { Rol.valueOf(it) } ?: rolSeleccionado
    // El controlador y su host comparten identidad y ciclo de vida por cuenta.
    // Al salir o cambiar de usuario se descarta todo el grafo anterior.
    key(usuario?.id, usuario?.rol) {
        val nav = rememberNavController()
        val entrada by nav.currentBackStackEntryAsState()
        val ruta = entrada?.destination?.route
        LaunchedEffect(restaurando, errorInicializacion, usuario, loginSolicitado, nav) {
            // Los errores de credenciales no reinician el destino ni los campos del Login.
            val destino = destinoInicio(restaurando, errorInicializacion != null,
                usuario?.rol?.let(Rol::valueOf), loginSolicitado)
            if (usuario != null) loginSolicitado = true
            if (destino == Rutas.LOGIN) loginSolicitado = true
            if (nav.currentDestination?.route != destino) {
                nav.navigate(destino) {
                    popUpTo(nav.graph.id) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
        AyudaCampusTheme(rol = rol, darkTheme = oscuro) {
            Scaffold(contentWindowInsets = WindowInsets(0, 0, 0, 0), bottomBar = {
                if (usuario?.rol == Rol.CLIENTE.name && ruta in listOf(Rutas.MIS_TICKETS, Rutas.NUEVO, Rutas.PERFIL, Rutas.DETALLE, Rutas.AJUSTES_CLIENTE)) {
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                        listOf(Triple(Rutas.MIS_TICKETS, "Tickets", Icons.AutoMirrored.Filled.List),
                            Triple(Rutas.NUEVO, "Nuevo", Icons.Default.Add),
                            Triple(Rutas.PERFIL, "Perfil", Icons.Default.Person)).forEach { (destino, texto, icono) ->
                            NavigationBarItem(selected = ruta == destino || (destino == Rutas.MIS_TICKETS && ruta == Rutas.DETALLE) || (destino == Rutas.PERFIL && ruta == Rutas.AJUSTES_CLIENTE), onClick = {
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
                if (usuario?.rol == Rol.ADMIN.name && ruta in listOf(Rutas.PANEL, Rutas.BANDEJA_RUTA, Rutas.CATEGORIAS, Rutas.AJUSTES, Rutas.ATENDER, Rutas.FORMULARIO_CATEGORIA, Rutas.PERFIL)) {
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                        listOf(Triple(Rutas.PANEL, "Panel", Icons.Default.Dashboard),
                            Triple(Rutas.BANDEJA, "Tickets", Icons.AutoMirrored.Filled.List),
                            Triple(Rutas.CATEGORIAS, "Categorías", Icons.Default.Category),
                            Triple(Rutas.AJUSTES, "Ajustes", Icons.Default.Settings)).forEach { (destino, texto, icono) ->
                            NavigationBarItem(selected = ruta == destino ||
                                (destino == Rutas.BANDEJA && ruta in listOf(Rutas.BANDEJA_RUTA, Rutas.ATENDER)) ||
                                (destino == Rutas.CATEGORIAS && ruta == Rutas.FORMULARIO_CATEGORIA) || (destino == Rutas.AJUSTES && ruta == Rutas.PERFIL), onClick = {
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
                    composable(Rutas.BIENVENIDA) {
                        BienvenidaScreen(onIngresar = { loginSolicitado = true })
                    }
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
                    composable(Rutas.PERFIL) {
                        usuario?.let { actual ->
                            val perfilVm: PerfilViewModel? = if (actual.rol == Rol.CLIENTE.name) {
                                viewModel(key = "perfil-${actual.id}", factory = fabricaTickets(actual))
                            } else null
                            val contadores = perfilVm?.uiState?.collectAsStateWithLifecycle()?.value
                            PerfilScreen(actual, contadores, cerrando || ajustes.guardando, errorSesion,
                                { perfilVm?.cargar() },
                                { nav.navigate(if (actual.rol == Rol.ADMIN.name) Rutas.AJUSTES else Rutas.AJUSTES_CLIENTE) { launchSingleTop = true } },
                                cambiarPerfil, cerrarSesion)
                        }
                    }
                    composable(Rutas.AJUSTES_CLIENTE) {
                        usuario?.takeIf { it.rol == Rol.CLIENTE.name }?.let { actual ->
                            AjustesScreen(actual, ajustes, cerrando, errorSesion, vm::guardarModoOscuro,
                                vm::guardarNotificaciones, vm::guardarRecibirTickets, vm::cargarAjustes,
                                { nav.navigate(Rutas.PERFIL) { launchSingleTop = true } }, cambiarPerfil, cerrarSesion)
                        }
                    }
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
                        SoloSoporte(usuario) { soporte ->
                            val categoriasVm: CategoriasViewModel = viewModel(factory = fabricaTickets(soporte))
                            CategoriasScreen(categoriasVm, { nav.navigate(Rutas.categoria(it)) }, { nav.navigate(Rutas.categoria()) })
                        }
                    }
                    composable(Rutas.FORMULARIO_CATEGORIA, arguments = listOf(navArgument("categoriaId") { type = NavType.IntType; defaultValue = 0 })) { entradaCategoria ->
                        SoloSoporte(usuario) { soporte ->
                            val id = entradaCategoria.arguments?.getInt("categoriaId")?.takeUnless { it == 0 }
                            val formularioVm: CategoriasViewModel = viewModel(factory = fabricaTickets(soporte).paraCategoria(id))
                            FormularioCategoriaScreen(formularioVm, { nav.popBackStack() }, { nav.popBackStack() })
                        }
                    }
                    composable(Rutas.AJUSTES) {
                        SoloSoporte(usuario) { actual ->
                            AjustesScreen(actual, ajustes, cerrando, errorSesion, vm::guardarModoOscuro,
                                vm::guardarNotificaciones, vm::guardarRecibirTickets, vm::cargarAjustes,
                                { nav.navigate(Rutas.PERFIL) { launchSingleTop = true } }, cambiarPerfil, cerrarSesion)
                        }
                    }
                    composable(Rutas.ATENDER, arguments = listOf(navArgument("ticketId") { type = NavType.IntType })) { entradaAtender ->
                        SoloSoporte(usuario) { soporte ->
                            val id = requireNotNull(entradaAtender.arguments).getInt("ticketId")
                            val atenderVm: AtenderViewModel = viewModel(factory = fabricaTickets(soporte).paraDetalle(id))
                            AtenderTicketScreen(atenderVm) { nav.popBackStack() }
                        }
                    }
                }
            }
        }
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
