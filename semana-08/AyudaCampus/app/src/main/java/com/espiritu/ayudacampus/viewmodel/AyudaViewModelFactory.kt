package com.espiritu.ayudacampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.espiritu.ayudacampus.data.prefs.PreferenciasSesion
import com.espiritu.ayudacampus.data.repository.AyudaRepository
import com.espiritu.ayudacampus.data.local.entity.Usuario
import kotlinx.coroutines.flow.Flow

class AyudaViewModelFactory(
    private val repo: AyudaRepository,
    private val preferencias: PreferenciasSesion,
    private val recibirTickets: Flow<Boolean>,
    private val esperarInicializacion: suspend () -> Unit,
    private val usuario: Usuario? = null,
    private val ticketId: Int? = null,
    private val soloAlertas: Boolean = false,
    private val categoriaId: Int? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return when (modelClass) {
            SesionViewModel::class.java -> SesionViewModel(repo, preferencias, esperarInicializacion)
            PerfilViewModel::class.java -> PerfilViewModel(repo, requireNotNull(usuario))
            MisTicketsViewModel::class.java -> MisTicketsViewModel(repo, requireNotNull(usuario))
            NuevoTicketViewModel::class.java -> NuevoTicketViewModel(repo, requireNotNull(usuario), recibirTickets)
            DetalleTicketViewModel::class.java -> DetalleTicketViewModel(repo, requireNotNull(usuario), requireNotNull(ticketId))
            PanelSoporteViewModel::class.java -> PanelSoporteViewModel(repo, requireNotNull(usuario))
            BandejaViewModel::class.java -> BandejaViewModel(repo, requireNotNull(usuario), soloAlertas)
            AtenderViewModel::class.java -> AtenderViewModel(repo, requireNotNull(usuario), requireNotNull(ticketId))
            CategoriasViewModel::class.java -> CategoriasViewModel(repo, requireNotNull(usuario), categoriaId)
            else -> throw IllegalArgumentException("ViewModel no registrado: ${modelClass.name}")
        } as T
    }

    fun paraDetalle(id: Int) = AyudaViewModelFactory(repo, preferencias, recibirTickets, esperarInicializacion, usuario, id)
    fun paraBandeja(alertas: Boolean) = AyudaViewModelFactory(repo, preferencias, recibirTickets, esperarInicializacion, usuario, soloAlertas = alertas)
    fun paraCategoria(id: Int?) = AyudaViewModelFactory(repo, preferencias, recibirTickets, esperarInicializacion, usuario, categoriaId = id)
}
