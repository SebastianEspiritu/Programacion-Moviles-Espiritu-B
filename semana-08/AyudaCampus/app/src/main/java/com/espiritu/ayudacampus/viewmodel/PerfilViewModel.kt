package com.espiritu.ayudacampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.espiritu.ayudacampus.data.local.entity.Usuario
import com.espiritu.ayudacampus.data.repository.AyudaRepository
import com.espiritu.ayudacampus.model.Rol
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PerfilUiState(val abiertos: Int = 0, val resueltos: Int = 0, val total: Int = 0,
                         val cargando: Boolean = true, val error: String? = null)

class PerfilViewModel(private val repo: AyudaRepository, val usuario: Usuario) : ViewModel() {
    init { require(usuario.id > 0 && usuario.rol == Rol.CLIENTE.name) }
    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState = _uiState.asStateFlow()
    private var carga: Job? = null
    init { cargar() }
    fun cargar() {
        carga?.cancel()
        _uiState.value = PerfilUiState()
        carga = viewModelScope.launch {
            try {
                repo.observarTicketsCliente(usuario.id).collect { lista ->
                    _uiState.value = PerfilUiState(
                        abiertos = lista.count { it.ticket.estado == "ABIERTO" },
                        resueltos = lista.count { it.ticket.estado == "RESUELTO" },
                        total = lista.size, cargando = false)
                }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _uiState.value = PerfilUiState(cargando = false, error = "No se pudieron cargar tus contadores.") }
        }
    }
}
