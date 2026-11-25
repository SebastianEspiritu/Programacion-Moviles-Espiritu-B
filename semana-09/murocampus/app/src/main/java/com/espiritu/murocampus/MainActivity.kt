package com.espiritu.murocampus

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.espiritu.murocampus.data.PostRepositoryRemote
import com.espiritu.murocampus.data.remote.RetrofitClient
import com.espiritu.murocampus.ui.BarraMuroCampus
import com.espiritu.murocampus.ui.PantallaCrear
import com.espiritu.murocampus.ui.PantallaDetalle
import com.espiritu.murocampus.ui.PantallaLista
import com.espiritu.murocampus.ui.theme.MuroCampusTheme
import com.espiritu.murocampus.viewmodel.PostsViewModel
import com.espiritu.murocampus.viewmodel.PostsViewModelFactory

class MainActivity : ComponentActivity() {

    // El ViewModel sobrevive a la rotación: no se vuelve a descargar de cero
    private val vm: PostsViewModel by viewModels {
        PostsViewModelFactory(PostRepositoryRemote(RetrofitClient.api))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Iconos blancos en la barra de estado (sobre el azul)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        setContent {
            MuroCampusTheme {
                // null = se ve la lista; un id = se ve el detalle de esa publicación
                var idSeleccionado by rememberSaveable { mutableStateOf<Int?>(null) }
                // true = se ve la pantalla Crear
                var creando by rememberSaveable { mutableStateOf(false) }

                val enLista = idSeleccionado == null && !creando

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = { BarraMuroCampus() },
                    floatingActionButton = {
                        // El botón + solo aparece en la lista y abre la pantalla Crear
                        if (enLista) {
                            FloatingActionButton(
                                onClick = {
                                    vm.nuevaPublicacion()
                                    creando = true
                                },
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ) {
                                Text("+", fontSize = 28.sp)
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(Modifier.padding(innerPadding).fillMaxSize()) {
                        val id = idSeleccionado
                        when {
                            creando -> {
                                // El botón Atrás del sistema vuelve a la lista
                                BackHandler { creando = false }
                                PantallaCrear(vm)
                            }

                            id != null -> {
                                // El botón Atrás del sistema vuelve a la lista (sin recargarla)
                                BackHandler { idSeleccionado = null }
                                PantallaDetalle(vm, id)
                            }

                            else -> PantallaLista(
                                vm = vm,
                                onAbrir = { postId ->
                                    idSeleccionado = postId
                                    vm.abrir(postId)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
