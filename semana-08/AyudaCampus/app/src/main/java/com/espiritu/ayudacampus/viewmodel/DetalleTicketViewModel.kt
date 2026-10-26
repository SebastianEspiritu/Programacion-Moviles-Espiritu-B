package com.espiritu.ayudacampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.espiritu.ayudacampus.data.local.entity.Comentario
import com.espiritu.ayudacampus.data.local.entity.Usuario
import com.espiritu.ayudacampus.data.local.relation.TicketConDetalle
import com.espiritu.ayudacampus.data.repository.AyudaRepository
import com.espiritu.ayudacampus.model.EstadoTicket
import com.espiritu.ayudacampus.model.Rol
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DetalleTicketUiState(
    val ticket: TicketConDetalle? = null,
    val cargando: Boolean = true,
    val inexistente: Boolean = false,
    val sinAcceso: Boolean = false,
    val errorCarga: String? = null,
    val errorAccion: String? = null,
    val mensaje: String? = null,
    val procesando: Boolean = false,
    val borrador: String = "",
    val comentarioFinal: String = "",
    val calificacion: Int = 0
) {
    val puedeComentar get() = ticket != null && ticket.ticket.estado != EstadoTicket.CERRADO.name &&
        !cargando && errorCarga == null && !procesando
    val puedeResolver get() = ticket?.ticket?.estado == EstadoTicket.RESUELTO.name &&
        !cargando && errorCarga == null && !procesando
}

class DetalleTicketViewModel(
    private val repo: AyudaRepository,
    val usuario: Usuario,
    val ticketId: Int
) : ViewModel() {
    init { require(usuario.id > 0 && usuario.rol == Rol.CLIENTE.name) }
    private val _uiState = MutableStateFlow(DetalleTicketUiState())
    val uiState = _uiState.asStateFlow()
    val ticket: StateFlow<TicketConDetalle?> = uiState.map { it.ticket }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    private var carga: Job? = null

    init { cargar() }

    fun cargar() {
        if (_uiState.value.procesando) return
        carga?.cancel()
        _uiState.update { it.copy(cargando = true, errorCarga = null, ticket = null, inexistente = false, sinAcceso = false) }
        if (ticketId <= 0) { publicar(null); return }
        carga = viewModelScope.launch {
            try {
                repo.observarTicket(ticketId).collect(::publicar)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, ticket = null, errorCarga = "No se pudo cargar el ticket. Inténtalo nuevamente.") }
            }
        }
    }

    private fun publicar(detalle: TicketConDetalle?) {
        val autorizado = detalle != null && detalle.ticket.id == ticketId && detalle.ticket.clienteId == usuario.id
        _uiState.update { anterior ->
            anterior.copy(
                ticket = if (autorizado) detalle?.copy(comentarios = detalle.comentarios.sortedWith(
                    compareBy<Comentario> { it.fecha }.thenBy { it.id })) else null,
                cargando = false, errorCarga = null, inexistente = detalle == null,
                sinAcceso = detalle != null && !autorizado,
                calificacion = if (detalle?.ticket?.estado == EstadoTicket.RESUELTO.name) anterior.calificacion else 0
            )
        }
    }

    private fun editar(cambio: (DetalleTicketUiState) -> DetalleTicketUiState) {
        if (!_uiState.value.procesando) _uiState.update(cambio)
    }
    fun cambiarBorrador(texto: String) = editar { it.copy(borrador = texto, errorAccion = null, mensaje = null) }
    fun cambiarComentarioFinal(texto: String) = editar { it.copy(comentarioFinal = texto, errorAccion = null, mensaje = null) }
    fun seleccionarCalificacion(valor: Int) = editar { it.copy(calificacion = valor, errorAccion = null) }

    // Revalidar contra Room, además del estado visible, antes de cada escritura.
    private suspend fun obtenerAutorizado(soloResuelto: Boolean): TicketConDetalle? {
        val actual = repo.observarTicket(ticketId).first()
        publicar(actual)
        val error = when {
            actual == null -> "El ticket ya no existe."
            actual.ticket.id != ticketId || actual.ticket.clienteId != usuario.id -> "No tienes acceso a este ticket."
            soloResuelto && actual.ticket.estado != EstadoTicket.RESUELTO.name -> "Esta acción solo está disponible para tickets resueltos."
            !soloResuelto && actual.ticket.estado == EstadoTicket.CERRADO.name -> "No puedes comentar un ticket cerrado."
            else -> null
        }
        if (error != null) { _uiState.update { it.copy(errorAccion = error) }; return null }
        return actual
    }

    private fun ejecutar(soloResuelto: Boolean, accion: suspend () -> Unit) {
        val estado = _uiState.value
        if (estado.procesando) return
        if (estado.cargando || estado.ticket == null || estado.errorCarga != null) {
            _uiState.update { it.copy(errorAccion = "El ticket no está disponible para esta acción.") }
            return
        }
        _uiState.update { it.copy(procesando = true, errorAccion = null, mensaje = null) }
        viewModelScope.launch {
            try {
                if (obtenerAutorizado(soloResuelto) == null) return@launch
                accion()
                // Actualizar antes de liberar el bloqueo; el Flow conserva la actualización continua.
                publicar(repo.observarTicket(ticketId).first())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(errorAccion = "No se pudo completar la acción. Inténtalo nuevamente.") }
            } finally {
                _uiState.update { it.copy(procesando = false) }
            }
        }
    }

    fun comentar(texto: String = _uiState.value.borrador) {
        if (_uiState.value.procesando) return
        if (texto.isBlank()) {
            _uiState.update { it.copy(errorAccion = "Escribe un comentario antes de enviarlo.") }
            return
        }
        ejecutar(soloResuelto = false) {
            repo.agregarComentario(ticketId, usuario, texto.trim())
            _uiState.update { it.copy(borrador = "", mensaje = "Comentario enviado.") }
        }
    }

    fun cerrar(calificacion: Int = _uiState.value.calificacion) {
        if (_uiState.value.procesando) return
        if (calificacion !in 1..5) {
            _uiState.update { it.copy(errorAccion = "Selecciona una calificación de 1 a 5 estrellas.") }
            return
        }
        val final = _uiState.value.comentarioFinal.trim()
        ejecutar(soloResuelto = true) {
            if (final.isNotEmpty()) {
                repo.agregarComentario(ticketId, usuario, final)
                // Si el cierre falla, reintentarlo no vuelve a insertar el comentario ya guardado.
                _uiState.update { it.copy(comentarioFinal = "") }
                if (obtenerAutorizado(soloResuelto = true) == null) return@ejecutar
            }
            repo.cerrarConCalificacion(ticketId, calificacion)
            _uiState.update { it.copy(mensaje = "Ticket cerrado. Gracias por tu calificación.") }
        }
    }

    fun reabrir() = ejecutar(soloResuelto = true) {
        repo.reabrir(ticketId)
        _uiState.update { it.copy(comentarioFinal = "", mensaje = "Ticket reabierto.") }
    }
}
