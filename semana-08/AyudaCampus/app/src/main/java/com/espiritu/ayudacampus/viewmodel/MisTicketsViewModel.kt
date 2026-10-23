package com.espiritu.ayudacampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.espiritu.ayudacampus.data.local.entity.Usuario
import com.espiritu.ayudacampus.data.local.relation.TicketConDetalle
import com.espiritu.ayudacampus.data.repository.AyudaRepository
import com.espiritu.ayudacampus.model.EstadoTicket
import com.espiritu.ayudacampus.model.Rol
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

data class MisTicketsUiState(
    val tickets: List<TicketConDetalle> = emptyList(),
    val abiertos: Int = 0,
    val cargando: Boolean = true,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class MisTicketsViewModel(private val repo: AyudaRepository, val usuario: Usuario) : ViewModel() {
    init { require(usuario.id > 0 && usuario.rol == Rol.CLIENTE.name) }

    private val _busqueda = MutableStateFlow("")
    val busqueda = _busqueda.asStateFlow()
    private val _estado = MutableStateFlow<EstadoTicket?>(null)
    val estado = _estado.asStateFlow()
    private val recarga = MutableStateFlow(0)

    private val datos = recarga.flatMapLatest {
        flow {
            emit(MisTicketsUiState())
            repo.observarTicketsCliente(usuario.id).collect { lista ->
                emit(MisTicketsUiState(lista, lista.count { it.ticket.estado == EstadoTicket.ABIERTO.name }, false))
            }
        }.catch { emit(MisTicketsUiState(cargando = false, error = "No se pudieron cargar tus tickets.")) }
    }

    val uiState: StateFlow<MisTicketsUiState> = combine(datos, _estado, _busqueda) { datos, estado, texto ->
        val consulta = texto.trim()
        datos.copy(tickets = datos.tickets.filter { detalle ->
            (estado == null || detalle.ticket.estado == estado.name) &&
                (consulta.isEmpty() || detalle.ticket.titulo.contains(consulta, ignoreCase = true) ||
                    detalle.ticket.id.toString().contains(consulta.removePrefix("#")))
        })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MisTicketsUiState())

    val tickets: StateFlow<List<TicketConDetalle>> = uiState.map { it.tickets }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun buscar(texto: String) { _busqueda.value = texto }
    fun filtrar(estado: EstadoTicket?) { _estado.value = estado }
    fun reintentar() { recarga.value++ }
}
