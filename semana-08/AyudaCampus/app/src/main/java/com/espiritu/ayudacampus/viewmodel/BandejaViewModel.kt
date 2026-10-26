package com.espiritu.ayudacampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.espiritu.ayudacampus.data.local.entity.Categoria
import com.espiritu.ayudacampus.data.local.entity.Usuario
import com.espiritu.ayudacampus.data.local.relation.TicketConDetalle
import com.espiritu.ayudacampus.data.repository.AyudaRepository
import com.espiritu.ayudacampus.model.EstadoTicket
import com.espiritu.ayudacampus.model.Prioridad
import com.espiritu.ayudacampus.model.Rol
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

data class BandejaUiState(
    val tickets: List<TicketConDetalle> = emptyList(),
    val cargando: Boolean = true,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class BandejaViewModel(
    private val repo: AyudaRepository,
    usuario: Usuario,
    soloAlertasInicial: Boolean = false,
    private val ahora: () -> Long = System::currentTimeMillis
) : ViewModel() {
    init { require(usuario.id > 0 && usuario.rol == Rol.ADMIN.name) { "Solo Soporte TI puede acceder a la bandeja." } }

    private val _estado = MutableStateFlow<EstadoTicket?>(null)
    val estado = _estado.asStateFlow()

    private val _busqueda = MutableStateFlow("")
    val busqueda = _busqueda.asStateFlow()

    private val _prioridad = MutableStateFlow<Prioridad?>(null)
    val prioridad = _prioridad.asStateFlow()

    private val _categoriaId = MutableStateFlow<Int?>(null)
    val categoriaId = _categoriaId.asStateFlow()

    private val _soloAlertas = MutableStateFlow(soloAlertasInicial)
    val soloAlertas = _soloAlertas.asStateFlow()

    val categorias: StateFlow<List<Categoria>> = repo.observarCategorias()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val recarga = MutableStateFlow(0)
    private val datos = recarga.flatMapLatest {
        flow {
            emit(BandejaUiState())
            repo.observarTodosLosTickets().collect { emit(BandejaUiState(tickets = it, cargando = false)) }
        }.catch { emit(BandejaUiState(cargando = false, error = "No se pudo cargar la bandeja de tickets.")) }
    }

    private val limite = _soloAlertas.flatMapLatest { if (it) limitesDeAlerta(ahora) else flowOf(Long.MIN_VALUE) }

    private data class Filtros(
        val estado: EstadoTicket?,
        val texto: String,
        val prioridad: Prioridad?,
        val categoriaId: Int?,
        val alertas: Boolean
    )

    private val filtros = combine(_estado, _busqueda, _prioridad, _categoriaId, _soloAlertas) { estado, texto, prioridad, catId, alertas ->
        Filtros(estado, texto.trim(), prioridad, catId, alertas)
    }

    val uiState = combine(datos, filtros, limite) { datos, filtros, limite ->
        datos.copy(tickets = datos.tickets.filter { detalle ->
            val t = detalle.ticket
            val coincideEstado = (filtros.estado == null || t.estado == filtros.estado.name)
            val coincideTexto = (filtros.texto.isEmpty() ||
                t.titulo.contains(filtros.texto, ignoreCase = true) ||
                t.id.toString().contains(filtros.texto.removePrefix("#")))
            val coincidePrioridad = (filtros.prioridad == null || t.prioridad.equals(filtros.prioridad.name, ignoreCase = true))
            val coincideCategoria = (filtros.categoriaId == null || t.categoriaId == filtros.categoriaId)
            val coincideAlertas = (!filtros.alertas || (t.prioridad == "ALTA" && t.estado == "ABIERTO" && t.fecha < limite))

            coincideEstado && coincideTexto && coincidePrioridad && coincideCategoria && coincideAlertas
        })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BandejaUiState())

    val tickets: StateFlow<List<TicketConDetalle>> = uiState.map { it.tickets }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filtrosActivos: StateFlow<Boolean> = combine(_busqueda, _estado, _prioridad, _categoriaId, _soloAlertas) { txt, est, prio, cat, alr ->
        txt.isNotBlank() || est != null || prio != null || cat != null || alr
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun buscar(texto: String) { _busqueda.value = texto }
    fun limpiarBusqueda() { _busqueda.value = "" }
    fun filtrar(estado: EstadoTicket?) { _estado.value = estado }
    fun filtrarPrioridad(prioridad: Prioridad?) { _prioridad.value = prioridad }
    fun filtrarCategoria(categoriaId: Int?) { _categoriaId.value = categoriaId }

    fun limpiarFiltros() {
        _busqueda.value = ""
        _estado.value = null
        _prioridad.value = null
        _categoriaId.value = null
        _soloAlertas.value = false
    }

    fun mostrarTodos() {
        limpiarFiltros()
    }

    fun reintentar() { recarga.value++ }
}
