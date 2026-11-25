package com.espiritu.murocampus.data.remote

import com.espiritu.murocampus.model.CambioTitulo
import com.espiritu.murocampus.model.Comentario
import com.espiritu.murocampus.model.NuevoPost
import com.espiritu.murocampus.model.Post
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PostApi {

    @GET("posts")
    suspend fun listar(): List<Post>

    @GET("posts/{id}")
    suspend fun detalle(@Path("id") id: Int): Post

    @GET("posts/{id}/comments")
    suspend fun comentarios(@Path("id") id: Int): List<Comentario>

    @GET("posts")
    suspend fun porUsuario(@Query("userId") userId: Int): List<Post>

    @POST("posts")
    suspend fun crear(@Body nuevo: NuevoPost): Post

    // Fase 2: la respuesta viene vacía; Response<Unit> permite revisar el código HTTP
    @DELETE("posts/{id}")
    suspend fun borrar(@Path("id") id: Int): Response<Unit>

    // Fase 2: PATCH modifica solo los campos enviados (PUT reemplazaría la publicación completa)
    @PATCH("posts/{id}")
    suspend fun editarTitulo(@Path("id") id: Int, @Body cambio: CambioTitulo): Post
}
