package com.espiritu.ayudacampus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.espiritu.ayudacampus.ui.navigation.NavAyuda
import com.espiritu.ayudacampus.viewmodel.AyudaViewModelFactory
import com.espiritu.ayudacampus.viewmodel.SesionViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as AyudaApp
        val factory = AyudaViewModelFactory(app.repository, app.preferencias, app::esperarInicializacion)
        val sesion = ViewModelProvider(this, factory)[SesionViewModel::class.java]
        setContent { NavAyuda(sesion) }
    }
}
