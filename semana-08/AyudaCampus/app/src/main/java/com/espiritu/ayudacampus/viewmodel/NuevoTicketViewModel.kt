package com.espiritu.ayudacampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.espiritu.ayudacampus.data.local.entity.Categoria
import com.espiritu.ayudacampus.data.local.entity.Usuario
import com.espiritu.ayudacampus.data.repository.AyudaRepository
import com.espiritu.ayudacampus.model.Prioridad
import com.espiritu.ayudacampus.model.Rol
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class NuevoTicketUiState(
    val categorias: List<Categoria> = emptyList(),
    val categoriaId: Int? = null,
    val titulo: String = "",
    val descripcion: String = "",
    val ubicacion: String = "",
    val prioridad: Prioridad = Prioridad.MEDIA,
    val cargando: Boolean = true,
    val recibirTickets: Boolean = false,
    val enviando: Boolean = false,
    val ticketCreado: Long? = null,
    val errorCategoria: String? = null,
    val errorTitulo: String? = null,
    val errorDescripcion: String? = null,
    val error: String? = null,
    val errorCarga: String? = null
)

class NuevoTicketViewModel(
    private val repo: AyudaRepository,
    private val usuario: Usuario,
    private val recibirTickets: Flow<Boolean>
) : ViewModel() {
    init { require(usuario.id > 0 && usuario.rol == Rol.CLIENTE.name) }
    private val _uiState = MutableStateFlow(NuevoTicketUiState())
    val uiState = _uiState.asStateFlow()
    private var carga: Job? = null

    init { cargarDatos() }

    fun cargarDatos() {
        carga?.cancel()
        _uiState.update { it.copy(cargando = true, errorCarga = null, recibirTickets = false) }
        carga = viewModelScope.launch {
            try {
                combine(repo.observarCategoriasActivas(), recibirTickets) { categorias, recibe ->
                    categorias to recibe
                }.collect { (categorias, recibe) ->
                    _uiState.update { it.copy(categorias = categorias, recibirTickets = recibe, cargando = false) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, recibirTickets = false,
                    errorCarga = "No se pudieron cargar las categorías o la disponibilidad. Inténtalo nuevamente.") }
            }
        }
    }

    private fun editar(cambio: (NuevoTicketUiState) -> NuevoTicketUiState) {
        if (!_uiState.value.enviando && _uiState.value.ticketCreado == null) _uiState.update(cambio)
    }
    fun seleccionarCategoria(id: Int) = editar { it.copy(categoriaId = id, errorCategoria = null, error = null) }
    fun cambiarTitulo(valor: String) = editar { it.copy(titulo = valor, errorTitulo = null, error = null) }
    fun cambiarDescripcion(valor: String) = editar { it.copy(descripcion = valor, errorDescripcion = null, error = null) }
    fun cambiarUbicacion(valor: String) = editar { it.copy(ubicacion = valor, error = null) }
    fun cambiarPrioridad(valor: Prioridad) = editar { it.copy(prioridad = valor, error = null) }

    fun enviar() {
        val formulario = _uiState.value
        if (formulario.enviando || formulario.ticketCreado != null) return
        val titulo = formulario.titulo.trim()
        val descripcion = formulario.descripcion.trim()
        val categoriaValida = formulario.categorias.any { it.id == formulario.categoriaId && it.activa }
        val errorTitulo = if (titulo.length < 5) "El título debe tener al menos 5 caracteres." else null
        val errorDescripcion = if (descripcion.length < 10) "La descripción debe tener al menos 10 caracteres." else null
        val errorCategoria = if (!categoriaValida) "Selecciona una categoría activa." else null
        val error = when {
            formulario.cargando -> "Espera a que termine la carga."
            formulario.errorCarga != null -> "No se pudo comprobar la disponibilidad. Reintenta la carga."
            !formulario.recibirTickets -> "Mesa de ayuda cerrada"
            else -> null
        }
        _uiState.update { it.copy(errorTitulo = errorTitulo, errorDescripcion = errorDescripcion,
            errorCategoria = errorCategoria, error = error) }
        if (errorTitulo != null || errorDescripcion != null || errorCategoria != null || error != null) return
        // Bloqueo inmediato, antes de lanzar la corrutina, para rechazar dobles pulsaciones.
        _uiState.update { it.copy(enviando = true) }
        viewModelScope.launch {
            try {
                // Releer las fuentes al enviar evita usar una categoría o disponibilidad obsoleta.
                if (!recibirTickets.first()) {
                    _uiState.update { it.copy(recibirTickets = false, error = "Mesa de ayuda cerrada") }
                    return@launch
                }
                if (repo.observarCategoriasActivas().first().none { it.id == formulario.categoriaId && it.activa }) {
                    _uiState.update { it.copy(errorCategoria = "La categoría ya no está activa. Elige otra.") }
                    return@launch
                }
                val id = repo.crearTicket(usuario.id, requireNotNull(formulario.categoriaId),
                    titulo, descripcion, formulario.ubicacion.trim(), formulario.prioridad)
                _uiState.update { it.copy(ticketCreado = id) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "No se pudo registrar el ticket. Inténtalo nuevamente.") }
            } finally {
                _uiState.update { it.copy(enviando = false) }
            }
        }
    }
}
