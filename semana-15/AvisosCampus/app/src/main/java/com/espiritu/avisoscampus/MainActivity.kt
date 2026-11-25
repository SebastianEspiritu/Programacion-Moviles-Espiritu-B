package com.espiritu.avisoscampus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.espiritu.avisoscampus.data.AvisosRepositoryMemoria
import com.espiritu.avisoscampus.ui.PantallaAvisos
import com.espiritu.avisoscampus.ui.theme.AvisosCampusTheme
import com.espiritu.avisoscampus.viewmodel.AvisosViewModel
import com.espiritu.avisoscampus.viewmodel.AvisosViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AvisosCampusTheme {
                val vm: AvisosViewModel = viewModel(
                    factory = AvisosViewModelFactory(AvisosRepositoryMemoria())
                )
                PantallaAvisos(vm = vm)
            }
        }
    }
}
