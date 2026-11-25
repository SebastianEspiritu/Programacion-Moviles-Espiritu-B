package com.espiritu.murocampus.model

import kotlinx.serialization.Serializable

// Ejemplo resuelto [E]
@Serializable
data class Usuario(
    val id: Int,
    val name: String,
    val email: String
)

// GET posts y GET posts/{id}
@Serializable
data class Post(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)

// GET posts/{id}/comments
@Serializable
data class Comentario(
    val postId: Int,
    val id: Int,
    val name: String,
    val email: String,
    val body: String
)

// Lo que enviamos en el POST: no lleva id (lo asigna el servidor)
@Serializable
data class NuevoPost(
    val userId: Int,
    val title: String,
    val body: String
)
