package com.espiritu.murocampus.data

import com.espiritu.murocampus.data.remote.PostApi
import com.espiritu.murocampus.model.Comentario
import com.espiritu.murocampus.model.NuevoPost
import com.espiritu.murocampus.model.Post
import retrofit2.HttpException
import kotlin.coroutines.cancellation.CancellationException

interface PostRepository {
    suspend fun listar(): Result<List<Post>>
    suspend fun detalle(id: Int): Result<Post>
    suspend fun comentarios(id: Int): Result<List<Comentario>>
    suspend fun porUsuario(userId: Int): Result<List<Post>>
    suspend fun crear(nuevo: NuevoPost): Result<Post>
    suspend fun borrar(id: Int): Result<Unit>
}

class PostRepositoryRemote(private val api: PostApi) : PostRepository {

    override suspend fun listar(): Result<List<Post>> = llamar { api.listar() }

    override suspend fun detalle(id: Int): Result<Post> = llamar { api.detalle(id) }

    override suspend fun comentarios(id: Int): Result<List<Comentario>> =
        llamar { api.comentarios(id) }

    override suspend fun porUsuario(userId: Int): Result<List<Post>> =
        llamar { api.porUsuario(userId) }

    override suspend fun crear(nuevo: NuevoPost): Result<Post> = llamar { api.crear(nuevo) }

    override suspend fun borrar(id: Int): Result<Unit> = llamar {
        val respuesta = api.borrar(id)
        // Con Response<Unit> Retrofit no lanza excepción si el código es 4xx/5xx: lo revisamos aquí
        if (!respuesta.isSuccessful) throw HttpException(respuesta)
    }

    // Un solo try/catch para todas las llamadas: ninguna excepción sale del Repository
    private suspend fun <T> llamar(bloque: suspend () -> T): Result<T> =
        try {
            Result.success(bloque())
        } catch (e: CancellationException) {
            throw e                      // nunca se traga la cancelación
        } catch (e: Exception) {
            Result.failure(e)            // IOException, HttpException, SerializationException...
        }
}
