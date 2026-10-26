package com.espiritu.ayudacampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.espiritu.ayudacampus.data.local.entity.Categoria
import com.espiritu.ayudacampus.data.local.entity.Usuario
import com.espiritu.ayudacampus.data.repository.*
import com.espiritu.ayudacampus.model.ICONOS_CATEGORIA
import com.espiritu.ayudacampus.model.Rol
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CategoriasUiState(
    val categorias: List<Categoria> = emptyList(),
    val cargando: Boolean = true,
    val procesando: Boolean = false,
    val errorCarga: String? = null,
    val error: String? = null,
    val mensaje: String? = null,
    val nombre: String = "",
    val icono: String = "pc",
    val activa: Boolean = true,
    val errorNombre: String? = null,
    val inexistente: Boolean = false,
    val guardado: Boolean = false
)

class CategoriasViewModel(private val repo: AyudaRepository, usuario: Usuario, val categoriaId: Int? = null) : ViewModel() {
    init { require(usuario.id > 0 && usuario.rol == Rol.ADMIN.name) { "Acceso exclusivo para Soporte TI." } }
    private val _uiState = MutableStateFlow(CategoriasUiState())
    val uiState = _uiState.asStateFlow()
    val categorias = uiState.map { it.categorias }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private var formularioInicializado = categoriaId == null
    private var carga: Job? = null
    init { cargar() }

    fun cargar() {
        if (_uiState.value.procesando) return
        carga?.cancel(); _uiState.update { it.copy(cargando = true, errorCarga = null) }
        carga = viewModelScope.launch {
            try {
                repo.observarCategorias().collect { lista ->
                    _uiState.update { anterior ->
                        val categoria = lista.find { it.id == categoriaId }
                        val cargarFormulario = !formularioInicializado && categoria != null
                        if (cargarFormulario) formularioInicializado = true
                        anterior.copy(categorias = lista, cargando = false, errorCarga = null,
                            inexistente = categoriaId != null && categoria == null,
                            nombre = if (cargarFormulario) categoria!!.nombre else anterior.nombre,
                            icono = if (cargarFormulario) categoria!!.icono else anterior.icono,
                            activa = if (cargarFormulario) categoria!!.activa else anterior.activa)
                    }
                }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _uiState.update { it.copy(cargando = false, errorCarga = "No se pudieron cargar las categorías.") } }
        }
    }

    private fun editar(cambio: (CategoriasUiState) -> CategoriasUiState) {
        if (!_uiState.value.procesando && !_uiState.value.guardado) _uiState.update(cambio)
    }
    fun cambiarNombre(nombre: String) = editar { it.copy(nombre = nombre, errorNombre = null, error = null) }
    fun cambiarIcono(icono: String) = editar { it.copy(icono = icono, error = null) }
    fun cambiarActiva(activa: Boolean) = editar { it.copy(activa = activa, error = null) }

    private fun ejecutar(accion: suspend () -> Unit) {
        val estado = _uiState.value
        if (estado.procesando || estado.guardado) return
        if (estado.cargando || estado.errorCarga != null || estado.inexistente) {
            _uiState.update { it.copy(error = "Espera a que las categorías estén disponibles.") }; return
        }
        _uiState.update { it.copy(procesando = true, error = null, mensaje = null) }
        viewModelScope.launch {
            try { accion() }
            catch (e: CancellationException) { throw e }
            catch (e: CategoriaDuplicadaException) { _uiState.update { it.copy(errorNombre = "Ya existe una categoría con ese nombre.") } }
            catch (e: CategoriaConTicketsException) { _uiState.update { it.copy(error = "No se puede eliminar: la categoría tiene tickets asociados.") } }
            catch (e: CategoriaNoExisteException) { _uiState.update { it.copy(error = "La categoría ya no existe.") } }
            catch (e: Exception) { _uiState.update { it.copy(error = "No se pudo completar la operación. Inténtalo nuevamente.") } }
            finally { _uiState.update { it.copy(procesando = false) } }
        }
    }

    fun guardarFormulario() {
        val estado = _uiState.value
        guardar(Categoria(categoriaId ?: 0, estado.nombre, estado.icono, estado.activa))
    }
    fun guardar(categoria: Categoria) {
        if (_uiState.value.procesando || _uiState.value.guardado) return
        val nombre = categoria.nombre.trim()
        val errorNombre = when {
            nombre.isEmpty() -> "El nombre es obligatorio."
            _uiState.value.categorias.any { it.id != categoria.id && it.nombre.trim().equals(nombre, ignoreCase = true) } ->
                "Ya existe una categoría con ese nombre."
            else -> null
        }
        _uiState.update { it.copy(errorNombre = errorNombre) }
        if (errorNombre != null) return
        if (categoria.icono !in ICONOS_CATEGORIA) { _uiState.update { it.copy(error = "Selecciona un icono válido.") }; return }
        ejecutar {
            repo.guardarCategoria(categoria.copy(nombre = nombre))
            _uiState.update { it.copy(guardado = true, mensaje = "Categoría guardada.") }
        }
    }
    fun alternar(categoria: Categoria) = ejecutar {
        repo.cambiarActiva(categoria.id, !categoria.activa)
        _uiState.update { it.copy(mensaje = "Disponibilidad actualizada.") }
    }
    fun eliminar(categoria: Categoria) = ejecutar {
        repo.eliminarCategoria(categoria)
        _uiState.update { it.copy(mensaje = "Categoría eliminada.") }
    }
}
