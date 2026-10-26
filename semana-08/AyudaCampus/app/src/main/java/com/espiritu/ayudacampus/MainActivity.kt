package com.espiritu.ayudacampus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.ViewModelProvider
import com.espiritu.ayudacampus.ui.navigation.NavAyuda
import com.espiritu.ayudacampus.viewmodel.AyudaViewModelFactory
import com.espiritu.ayudacampus.viewmodel.SesionViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as AyudaApp
        val factory = AyudaViewModelFactory(app.repository, app.preferencias, app.preferencias.recibirTickets, app::esperarInicializacion)
        val sesion = ViewModelProvider(this, factory)[SesionViewModel::class.java]
        setContent {
            val oscuro by sesion.modoOscuro.collectAsStateWithLifecycle()
            LaunchedEffect(oscuro) {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !oscuro
                    isAppearanceLightNavigationBars = !oscuro
                }
            }
            NavAyuda(sesion) { usuario ->
                AyudaViewModelFactory(app.repository, app.preferencias, app.preferencias.recibirTickets, app::esperarInicializacion, usuario)
            }
        }
    }
}
