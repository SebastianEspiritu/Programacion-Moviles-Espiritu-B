package com.espiritu.ayudacampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.espiritu.ayudacampus.data.local.entity.Comentario
import com.espiritu.ayudacampus.data.local.entity.Usuario
import com.espiritu.ayudacampus.data.local.relation.TicketConDetalle
import com.espiritu.ayudacampus.data.repository.AyudaRepository
import com.espiritu.ayudacampus.data.repository.TicketCerradoException
import com.espiritu.ayudacampus.model.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AtenderUiState(
    val ticket: TicketConDetalle? = null,
    val cargando: Boolean = true,
    val inexistente: Boolean = false,
    val procesando: Boolean = false,
    val errorCarga: String? = null,
    val error: String? = null,
    val mensaje: String? = null,
    val respuesta: String = "",
    val estado: EstadoTicket = EstadoTicket.ABIERTO,
    val prioridad: Prioridad = Prioridad.MEDIA,
    val estadoEditado: Boolean = false,
    val prioridadEditada: Boolean = false
) {
    val editable get() = ticket != null && ticket.ticket.estado != EstadoTicket.CERRADO.name &&
        !cargando && !procesando && errorCarga == null
}

class AtenderViewModel(private val repo: AyudaRepository, val usuario: Usuario, val ticketId: Int) : ViewModel() {
    init { require(usuario.id > 0 && usuario.rol == Rol.ADMIN.name) { "Acceso exclusivo para Soporte TI." } }
    private val _uiState = MutableStateFlow(AtenderUiState())
    val uiState = _uiState.asStateFlow()
    val ticket = uiState.map { it.ticket }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    private var carga: Job? = null
    init { cargar() }

    private fun publicar(detalle: TicketConDetalle?) {
        _uiState.update { anterior ->
            val cerrado = detalle?.ticket?.estado == EstadoTicket.CERRADO.name
            anterior.copy(ticket = detalle?.copy(comentarios = detalle.comentarios.sortedWith(
                compareBy<Comentario> { it.fecha }.thenBy { it.id })), cargando = false, inexistente = detalle == null,
                errorCarga = null,
                estado = if (detalle != null && (!anterior.estadoEditado || detalle.ticket.estado == "CERRADO"))
                    EstadoTicket.valueOf(detalle.ticket.estado) else anterior.estado,
                prioridad = if (detalle != null && (!anterior.prioridadEditada || cerrado)) Prioridad.valueOf(detalle.ticket.prioridad) else anterior.prioridad,
                estadoEditado = anterior.estadoEditado && !cerrado, prioridadEditada = anterior.prioridadEditada && !cerrado)
        }
    }

    fun cargar() {
        if (_uiState.value.procesando) return
        carga?.cancel()
        _uiState.update { it.copy(cargando = true, errorCarga = null) }
        if (ticketId <= 0) { publicar(null); return }
        carga = viewModelScope.launch {
            try { repo.observarTicket(ticketId).collect(::publicar) }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { _uiState.update { it.copy(ticket = null, cargando = false, errorCarga = "No se pudo cargar el ticket.") } }
        }
    }

    fun cambiarEstado(estado: EstadoTicket) {
        if (!_uiState.value.editable) return
        if (estado == EstadoTicket.CERRADO) {
            _uiState.update { it.copy(error = "Solo el estudiante puede cerrar el ticket.") }; return
        }
        _uiState.update { it.copy(estado = estado, estadoEditado = true, error = null, mensaje = null) }
    }
    fun cambiarPrioridad(prioridad: Prioridad) {
        if (_uiState.value.editable) _uiState.update { it.copy(prioridad = prioridad, prioridadEditada = true, error = null, mensaje = null) }
    }
    fun cambiarRespuesta(texto: String) {
        if (_uiState.value.editable) _uiState.update { it.copy(respuesta = texto, error = null, mensaje = null) }
    }

    private fun ejecutar(accion: suspend (TicketConDetalle) -> Unit) {
        if (_uiState.value.procesando) return
        if (!_uiState.value.editable) {
            _uiState.update { it.copy(error = "El ticket no está disponible o está cerrado.") }; return
        }
        _uiState.update { it.copy(procesando = true, error = null, mensaje = null) }
        viewModelScope.launch {
            try {
                val actual = repo.observarTicket(ticketId).first()
                publicar(actual)
                if (actual == null) { _uiState.update { it.copy(error = "El ticket ya no existe.") }; return@launch }
                if (actual.ticket.estado == "CERRADO") throw TicketCerradoException()
                accion(actual)
                publicar(repo.observarTicket(ticketId).first())
            } catch (e: CancellationException) { throw e }
            catch (e: TicketCerradoException) { _uiState.update { it.copy(error = "El ticket está cerrado. Soporte no puede modificarlo.") } }
            catch (e: Exception) { _uiState.update { it.copy(error = "No se pudo completar la operación. Inténtalo nuevamente.") } }
            finally { _uiState.update { it.copy(procesando = false) } }
        }
    }

    fun responder(texto: String = _uiState.value.respuesta) {
        if (_uiState.value.procesando) return
        if (texto.isBlank()) { _uiState.update { it.copy(error = "Escribe una respuesta antes de enviarla.") }; return }
        ejecutar {
            repo.agregarComentario(ticketId, usuario, texto.trim())
            _uiState.update { it.copy(respuesta = "", mensaje = "Respuesta enviada.") }
        }
    }

    fun guardar() {
        val formulario = _uiState.value
        if (formulario.procesando) return
        if (formulario.estado == EstadoTicket.CERRADO) {
            _uiState.update { it.copy(error = "Solo el estudiante puede cerrar el ticket.") }; return
        }
        if (formulario.editable && !formulario.estadoEditado && !formulario.prioridadEditada) {
            _uiState.update { it.copy(mensaje = "No hay cambios pendientes.") }; return
        }
        ejecutar { actual ->
            val estado = if (formulario.estadoEditado) formulario.estado else EstadoTicket.valueOf(actual.ticket.estado)
            val prioridad = if (formulario.prioridadEditada) formulario.prioridad else Prioridad.valueOf(actual.ticket.prioridad)
            repo.guardarAtencion(ticketId, estado, prioridad)
            _uiState.update { it.copy(estadoEditado = false, prioridadEditada = false, mensaje = "Cambios guardados.") }
        }
    }
}
