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
import kotlinx.coroutines.launch

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

    init {
        viewModelScope.launch {
            try {
                preferencias.modoOscuro.collect { _modoOscuro.value = it }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = "No se pudo leer la preferencia de apariencia."
            }
        }
        restaurarSesion()
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
