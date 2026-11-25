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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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

    // ---------- Búsqueda (Fase 2) ----------
    // Filtra por título las publicaciones YA descargadas: no hace otra llamada a la API
    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    /** Lo que dibuja la lista: el estado de la descarga con la búsqueda aplicada. */
    val listaVisible: StateFlow<UiState<List<Post>>> =
        combine(_lista, _busqueda) { estado, texto -> aplicarBusqueda(estado, texto) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Cargando)

    // ---------- Detalle ----------
    private val _detalle = MutableStateFlow<UiState<DetalleUi>>(UiState.Cargando)
    val detalle: StateFlow<UiState<DetalleUi>> = _detalle.asStateFlow()

    private var trabajoDetalle: Job? = null

    // ---------- Crear ----------
    // null = aún no se envió
    private val _creado = MutableStateFlow<UiState<Post>?>(null)
    val creado: StateFlow<UiState<Post>?> = _creado.asStateFlow()

    // ---------- Borrar (Fase 2) ----------
    // null = no se pidió borrar; Exito lleva el id que se borró
    private val _borrado = MutableStateFlow<UiState<Int>?>(null)
    val borrado: StateFlow<UiState<Int>?> = _borrado.asStateFlow()

    // ---------- Editar (Fase 2) ----------
    // null = aún no se envió; Exito lleva la publicación que respondió el servidor
    private val _editado = MutableStateFlow<UiState<Post>?>(null)
    val editado: StateFlow<UiState<Post>?> = _editado.asStateFlow()

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

    fun buscar(texto: String) {
        _busqueda.value = texto
    }

    private fun aplicarBusqueda(estado: UiState<List<Post>>, texto: String): UiState<List<Post>> {
        val buscado = texto.trim()
        if (estado !is UiState.Exito || buscado.isEmpty()) return estado
        return UiState.Exito(estado.datos.filter { it.title.contains(buscado, ignoreCase = true) })
    }

    fun abrir(id: Int) {
        trabajoDetalle?.cancel()          // si abres otra publicación, se cancela la anterior
        _borrado.value = null             // el aviso de borrado es de la publicación anterior
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

    fun borrar(id: Int) {
        viewModelScope.launch {
            _borrado.value = UiState.Cargando
            repo.borrar(id)
                .onSuccess {
                    // JSONPlaceholder no borra de verdad: la quitamos de la lista ya descargada
                    quitarDeLista(id)
                    _borrado.value = UiState.Exito(id)
                }
                .onFailure { _borrado.value = UiState.Error(mensajeDe(it)) }
        }
    }

    /** Se llama cuando la pantalla ya mostró el aviso de borrado. */
    fun avisoBorradoMostrado() {
        _borrado.value = null
    }

    private fun quitarDeLista(id: Int) {
        val estado = _lista.value
        if (estado is UiState.Exito) {
            _lista.value = UiState.Exito(estado.datos.filterNot { it.id == id })
        }
    }

    /** Al entrar a la pantalla Editar, se borra el mensaje de la edición anterior. */
    fun nuevaEdicion() {
        _editado.value = null
    }

    fun editarTitulo(id: Int, titulo: String) {
        viewModelScope.launch {
            _editado.value = UiState.Cargando
            repo.editarTitulo(id, titulo)
                .onSuccess { post ->
                    actualizarPost(post)
                    _editado.value = UiState.Exito(post)
                }
                .onFailure { _editado.value = UiState.Error(mensajeDe(it)) }
        }
    }

    /** Reemplaza la publicación editada en la lista y en el detalle (JSONPlaceholder no la guarda). */
    private fun actualizarPost(post: Post) {
        val lista = _lista.value
        if (lista is UiState.Exito) {
            _lista.value = UiState.Exito(lista.datos.map { if (it.id == post.id) post else it })
        }
        val detalle = _detalle.value
        if (detalle is UiState.Exito && detalle.datos.post.id == post.id) {
            _detalle.value = UiState.Exito(detalle.datos.copy(post = post))
        }
    }
}
