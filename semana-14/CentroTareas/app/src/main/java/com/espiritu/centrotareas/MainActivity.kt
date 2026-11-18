package com.espiritu.centrotareas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.espiritu.centrotareas.data.TareasRepositoryLocal
import com.espiritu.centrotareas.ui.PantallaTareas
import com.espiritu.centrotareas.ui.theme.CentroTareasTheme
import com.espiritu.centrotareas.viewmodel.TareasViewModel
import com.espiritu.centrotareas.viewmodel.TareasViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CentroTareasTheme {
                val viewModel: TareasViewModel = viewModel(
                    factory = TareasViewModelFactory(TareasRepositoryLocal())
                )
                PantallaTareas(viewModel = viewModel)
            }
        }
    }
}
