package com.espiritu.ayudacampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.espiritu.ayudacampus.data.local.entity.Usuario
import com.espiritu.ayudacampus.data.prefs.PreferenciasSesion
import com.espiritu.ayudacampus.data.repository.AyudaRepository
import com.espiritu.ayudacampus.model.Rol
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AjustesUiState(
    val modoOscuro: Boolean = false,
    val notificaciones: Boolean = true,
    val recibirTickets: Boolean = true,
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    val error: String? = null
)

class SesionViewModel(
    private val repo: AyudaRepository,
    private val preferencias: PreferenciasSesion,
    private val esperarInicializacion: suspend () -> Unit
) : ViewModel() {
    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual = _usuarioActual.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    private val _modoOscuro = MutableStateFlow(false)
    val modoOscuro = _modoOscuro.asStateFlow()
    private val _restaurando = MutableStateFlow(true)
    val restaurando = _restaurando.asStateFlow()
    private val _ingresando = MutableStateFlow(false)
    val ingresando = _ingresando.asStateFlow()
    private val _errorInicializacion = MutableStateFlow<String?>(null)
    val errorInicializacion = _errorInicializacion.asStateFlow()
    private var restauracion: Job? = null
    private var lecturaAjustes: Job? = null
    private val _ajustes = MutableStateFlow(AjustesUiState())
    val ajustes = _ajustes.asStateFlow()
    private val _cerrandoSesion = MutableStateFlow(false)
    val cerrandoSesion = _cerrandoSesion.asStateFlow()

    init {
        cargarAjustes()
        restaurarSesion()
    }

    fun cargarAjustes() {
        lecturaAjustes?.cancel()
        _ajustes.update { it.copy(cargando = true, error = null) }
        lecturaAjustes = viewModelScope.launch {
            try {
                combine(preferencias.modoOscuro, preferencias.notificaciones, preferencias.recibirTickets) { oscuro, avisos, recibe ->
                    Triple(oscuro, avisos, recibe)
                }.collect { (oscuro, avisos, recibe) ->
                    _modoOscuro.value = oscuro
                    _ajustes.update { it.copy(modoOscuro = oscuro, notificaciones = avisos,
                        recibirTickets = recibe, cargando = false) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _ajustes.update { it.copy(cargando = false, error = "No se pudieron leer los ajustes. Reintenta la carga.") }
            }
        }
    }

    fun guardarModoOscuro(valor: Boolean) = guardarAjuste { preferencias.guardarModoOscuro(valor) }
    fun guardarNotificaciones(valor: Boolean) = guardarAjuste { preferencias.guardarNotificaciones(valor) }
    fun guardarRecibirTickets(valor: Boolean) {
        if (_usuarioActual.value?.rol != Rol.ADMIN.name) {
            _ajustes.update { it.copy(error = "Solo Soporte TI puede cambiar la recepción de tickets.") }
            return
        }
        guardarAjuste { preferencias.guardarRecibirTickets(valor) }
    }

    private fun guardarAjuste(guardar: suspend () -> Unit) {
        if (_usuarioActual.value == null || _cerrandoSesion.value || _ajustes.value.guardando || _ajustes.value.cargando) return
        _ajustes.update { it.copy(guardando = true, error = null) }
        viewModelScope.launch {
            try { guardar() }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { _ajustes.update { it.copy(error = "No se pudo guardar el ajuste. Inténtalo nuevamente.") } }
            finally { _ajustes.update { it.copy(guardando = false) } }
        }
    }

    // Cambiar de cuenta también exige volver a autenticarse con su rol real.
    fun cerrarSesion() {
        if (_usuarioActual.value == null || _cerrandoSesion.value || _ajustes.value.guardando) return
        _cerrandoSesion.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                preferencias.cerrarSesion()
                _usuarioActual.value = null
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _error.value = "No se pudo cerrar sesión. Inténtalo nuevamente." }
            finally { _cerrandoSesion.value = false }
        }
    }

    fun restaurarSesion() {
        // Mientras se restaura no se aceptan intentos de acceso.
        if (_ingresando.value || restauracion?.isActive == true || _usuarioActual.value != null) return
        _restaurando.value = true
        _errorInicializacion.value = null
        restauracion = viewModelScope.launch {
            try {
                esperarInicializacion()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _errorInicializacion.value = "No se pudo preparar la base de datos. Cierra y vuelve a abrir la aplicación."
                _restaurando.value = false
                return@launch
            }
            try {
                val sesion = preferencias.sesion.first()
                if (sesion.rol.isEmpty() && sesion.usuarioId == 0) return@launch
                val usuario = if (sesion.usuarioId > 0 && Rol.entries.any { it.name == sesion.rol }) {
                    repo.obtenerUsuarioPorId(sesion.usuarioId)
                } else null
                if (usuario != null && usuario.rol == sesion.rol) {
                    _usuarioActual.value = usuario
                } else {
                    preferencias.cerrarSesion()
                    _error.value = "La sesión guardada ya no es válida. Ingresa nuevamente."
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = "No se pudo restaurar la sesión. Intenta ingresar nuevamente."
            } finally {
                _restaurando.value = false
            }
        }
    }

    fun limpiarError() { _error.value = null }

    fun login(correo: String, clave: String, rolSeleccionado: Rol) {
        if (_restaurando.value || _ingresando.value || _errorInicializacion.value != null || _usuarioActual.value != null) return
        _error.value = null
        if (correo.isBlank() || clave.isBlank()) {
            _error.value = "Completa el correo y la contraseña."
            return
        }
        _ingresando.value = true
        viewModelScope.launch {
            try {
                esperarInicializacion()
                val usuario = repo.login(correo.trim(), clave)
                when {
                    usuario == null -> _error.value = "Correo o contraseña incorrectos"
                    usuario.rol != rolSeleccionado.name -> _error.value = "El usuario no corresponde al perfil seleccionado."
                    else -> {
                        preferencias.guardarSesion(usuario.rol, usuario.id)
                        // Publicar el usuario solo después de persistir la sesión.
                        _usuarioActual.value = usuario
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = "No se pudo iniciar sesión. Inténtalo nuevamente."
            } finally {
                _ingresando.value = false
            }
        }
    }
}
