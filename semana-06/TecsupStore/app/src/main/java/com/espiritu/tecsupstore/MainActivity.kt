package com.espiritu.tecsupstore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.espiritu.tecsupstore.ui.theme.TecsupStoreTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TecsupStoreTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PantallaTienda()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaTienda() {
    val productos = remember {
        listOf(
            Producto(1, "Audífonos", 89.0),
            Producto(2, "Smartwatch", 199.0),
            Producto(3, "Funda celular", 25.0)
        )
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentRoute by remember { mutableStateOf("Inicio") }
    // Conjunto de IDs seleccionados como favoritos
    var favoritosIds by remember { mutableStateOf(setOf<Int>()) }

    AppDrawer(
        drawerState = drawerState,
        currentRoute = currentRoute,
        favoritosCount = favoritosIds.size, // Pasa el total al Badge del Drawer
        onNavigate = { nuevaRuta ->
            currentRoute = nuevaRuta
            scope.launch { drawerState.close() }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("TECSUP Store") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Abrir menú"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentRoute) {
                    "Inicio" -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            items(productos) { producto ->
                                val esFavorito = producto.id in favoritosIds
                                TarjetaProducto(
                                    producto = producto,
                                    esFavorito = esFavorito,
                                    onToggleFavorito = { p ->
                                        val yaEsFavorito = p.id in favoritosIds
                                        favoritosIds = if (yaEsFavorito) {
                                            favoritosIds - p.id
                                        } else {
                                            favoritosIds + p.id
                                        }
                                        val mensaje = if (yaEsFavorito) {
                                            "Eliminado de Favoritos: ${p.nombre}"
                                        } else {
                                            "Agregado a Favoritos: ${p.nombre}"
                                        }
                                        scope.launch {
                                            snackbarHostState.showSnackbar(mensaje)
                                        }
                                    },
                                    onOptionSelected = { mensaje ->
                                        scope.launch {
                                            snackbarHostState.showSnackbar(mensaje)
                                        }
                                    }
                                )
                            }
                        }
                    }
                    "Favoritos" -> {
                        val listaFavoritos = productos.filter { it.id in favoritosIds }
                        if (listaFavoritos.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No tienes productos en favoritos aún",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                items(listaFavoritos) { producto ->
                                    TarjetaProducto(
                                        producto = producto,
                                        esFavorito = true,
                                        onToggleFavorito = { p ->
                                            favoritosIds = favoritosIds - p.id
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Eliminado de Favoritos: ${p.nombre}")
                                            }
                                        },
                                        onOptionSelected = { mensaje ->
                                            scope.launch {
                                                snackbarHostState.showSnackbar(mensaje)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Pantalla de $currentRoute",
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                }
            }
        }
    }
}