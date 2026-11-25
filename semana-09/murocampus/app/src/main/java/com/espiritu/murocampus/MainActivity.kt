package com.espiritu.murocampus

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.espiritu.murocampus.data.PostRepositoryRemote
import com.espiritu.murocampus.data.remote.RetrofitClient
import com.espiritu.murocampus.ui.BarraMuroCampus
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
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = { BarraMuroCampus() }
                ) { innerPadding ->
                    Box(Modifier.padding(innerPadding).fillMaxSize()) {
                        PantallaLista(vm)
                    }
                }
            }
        }
    }
}
