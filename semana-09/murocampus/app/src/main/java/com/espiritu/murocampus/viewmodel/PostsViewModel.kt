package com.espiritu.murocampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.espiritu.murocampus.data.PostRepository
import com.espiritu.murocampus.model.Comentario
import com.espiritu.murocampus.model.NuevoPost
import com.espiritu.murocampus.model.Post
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Lo que muestra la pantalla de detalle: la publicación y sus comentarios. */
data class DetalleUi(
    val post: Post,
    val comentarios: List<Comentario>
)

class PostsViewModel(private val repo: PostRepository) : ViewModel() {

    // ---------- Lista ----------
    private val _lista = MutableStateFlow<UiState<List<Post>>>(UiState.Cargando)
    val lista: StateFlow<UiState<List<Post>>> = _lista.asStateFlow()

    // ---------- Filtro ----------
    // null significa "Todos"
    private val _usuarioFiltro = MutableStateFlow<Int?>(null)
    val usuarioFiltro: StateFlow<Int?> = _usuarioFiltro.asStateFlow()

    private var trabajoLista: Job? = null

    // ---------- Detalle ----------
    private val _detalle = MutableStateFlow<UiState<DetalleUi>>(UiState.Cargando)
    val detalle: StateFlow<UiState<DetalleUi>> = _detalle.asStateFlow()

    private var trabajoDetalle: Job? = null

    // ---------- Crear ----------
    // null = aún no se envió
    private val _creado = MutableStateFlow<UiState<Post>?>(null)
    val creado: StateFlow<UiState<Post>?> = _creado.asStateFlow()

    init { cargar() }

    /** Carga la lista respetando el filtro actual (también lo usa Reintentar). */
    fun cargar() {
        trabajoLista?.cancel()            // si cambias de chip rápido, solo vale la última carga
        trabajoLista = viewModelScope.launch {
            _lista.value = UiState.Cargando
            val userId = _usuarioFiltro.value
            val resultado = if (userId == null) repo.listar() else repo.porUsuario(userId)
            resultado
                .onSuccess { _lista.value = UiState.Exito(it) }
                .onFailure { _lista.value = UiState.Error(mensajeDe(it)) }
        }
    }

    /** Guarda el filtro y recarga: null -> repo.listar(), si no -> repo.porUsuario(userId). */
    fun filtrar(userId: Int?) {
        _usuarioFiltro.value = userId
        cargar()
    }

    fun abrir(id: Int) {
        trabajoDetalle?.cancel()          // si abres otra publicación, se cancela la anterior
        trabajoDetalle = viewModelScope.launch {
            _detalle.value = UiState.Cargando

            // Las dos llamadas se hacen a la vez
            val (resultadoPost, resultadoComentarios) = coroutineScope {
                val post = async { repo.detalle(id) }
                val comentarios = async { repo.comentarios(id) }
                post.await() to comentarios.await()
            }

            // Si cualquiera de las dos falla, todo el detalle muestra el error
            val error = resultadoPost.exceptionOrNull() ?: resultadoComentarios.exceptionOrNull()
            _detalle.value = if (error != null) {
                UiState.Error(mensajeDe(error))
            } else {
                UiState.Exito(
                    DetalleUi(
                        post = resultadoPost.getOrThrow(),
                        comentarios = resultadoComentarios.getOrThrow()
                    )
                )
            }
        }
    }

    fun publicar(titulo: String, texto: String) {
        viewModelScope.launch {
            _creado.value = UiState.Cargando
            repo.crear(NuevoPost(userId = 1, title = titulo, body = texto))
                .onSuccess { _creado.value = UiState.Exito(it) }
                .onFailure { _creado.value = UiState.Error(mensajeDe(it)) }
        }
    }

    /** Al entrar a la pantalla Crear, se borra el mensaje del envío anterior. */
    fun nuevaPublicacion() {
        _creado.value = null
    }
}
