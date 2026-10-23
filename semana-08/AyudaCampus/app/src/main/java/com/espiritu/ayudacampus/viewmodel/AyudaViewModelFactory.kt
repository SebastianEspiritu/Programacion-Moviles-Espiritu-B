package com.espiritu.ayudacampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.espiritu.ayudacampus.data.prefs.PreferenciasSesion
import com.espiritu.ayudacampus.data.repository.AyudaRepository
import com.espiritu.ayudacampus.data.local.entity.Usuario

class AyudaViewModelFactory(
    private val repo: AyudaRepository,
    private val preferencias: PreferenciasSesion,
    private val esperarInicializacion: suspend () -> Unit,
    private val usuario: Usuario? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return when (modelClass) {
            SesionViewModel::class.java -> SesionViewModel(repo, preferencias, esperarInicializacion)
            MisTicketsViewModel::class.java -> MisTicketsViewModel(repo, requireNotNull(usuario))
            else -> throw IllegalArgumentException("ViewModel no registrado: ${modelClass.name}")
        } as T
    }
}
