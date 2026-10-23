package com.espiritu.ayudacampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.espiritu.ayudacampus.data.prefs.PreferenciasSesion
import com.espiritu.ayudacampus.data.repository.AyudaRepository

class AyudaViewModelFactory(
    private val repo: AyudaRepository,
    private val preferencias: PreferenciasSesion,
    private val esperarInicializacion: suspend () -> Unit
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == SesionViewModel::class.java) {
            "ViewModel no registrado: ${modelClass.name}"
        }
        @Suppress("UNCHECKED_CAST")
        return SesionViewModel(repo, preferencias, esperarInicializacion) as T
    }
}
