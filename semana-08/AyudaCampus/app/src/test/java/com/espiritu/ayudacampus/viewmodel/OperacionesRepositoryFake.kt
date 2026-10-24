package com.espiritu.ayudacampus.viewmodel

import com.espiritu.ayudacampus.data.local.entity.*
import com.espiritu.ayudacampus.data.local.relation.TicketConDetalle
import com.espiritu.ayudacampus.data.repository.*
import com.espiritu.ayudacampus.model.*
import java.io.IOException
import java.lang.reflect.Proxy
import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.resume
import kotlinx.coroutines.flow.*

internal class OperacionesRepositoryFake {
    val admin = Usuario(2, "Soporte", "soporte@campus.test", "admin", "ADMIN")
    val cliente = Usuario(7, "Juan", "juan@campus.test", "1234", "CLIENTE")
    val categorias = MutableStateFlow(listOf(Categoria(1, "Hardware", "pc"), Categoria(2, "Software", "code", false)))
    val base = TicketConDetalle(Ticket(10, 7, 1, "Equipo", "No enciende", "Aula", fecha = 1000),
        emptyList(), categorias.value.first(), cliente)
    val detalle = MutableStateFlow<TicketConDetalle?>(base)
    val escrituras = mutableListOf<String>()
    val fallos = mutableSetOf<String>()
    var pausar = false
    var completar: (() -> Unit)? = null
    var secuenciaComentario = 1
    val repo = Proxy.newProxyInstance(AyudaRepository::class.java.classLoader, arrayOf(AyudaRepository::class.java)) { _, metodo, args ->
        when (metodo.name) {
            "observarTicket" -> flow {
                if (metodo.name in fallos) throw IOException("Lectura")
                emitAll(detalle)
            }
            "observarCategorias" -> flow {
                if (metodo.name in fallos) throw IOException("Lectura")
                emitAll(categorias)
            }
            else -> {
                val mutar: () -> Any? = {
                    if (metodo.name in fallos) throw IOException("Escritura")
                    when (metodo.name) {
                        "guardarAtencion" -> {
                            val actual = requireNotNull(detalle.value)
                            if (actual.ticket.estado == "CERRADO") throw TicketCerradoException()
                            val estado = args[1] as EstadoTicket
                            require(estado != EstadoTicket.CERRADO)
                            detalle.value = actual.copy(ticket = actual.ticket.copy(estado = estado.name, prioridad = (args[2] as Prioridad).name))
                        }
                        "agregarComentario" -> {
                            val actual = requireNotNull(detalle.value)
                            if (actual.ticket.estado == "CERRADO") throw TicketCerradoException()
                            val autor = args[1] as Usuario
                            detalle.value = actual.copy(comentarios = actual.comentarios + Comentario(secuenciaComentario++, 10,
                                autor.id, autor.nombre, autor.rol, args[2] as String, 2000))
                        }
                        "guardarCategoria" -> {
                            val c = args[0] as Categoria
                            if (categorias.value.any { it.id != c.id && it.nombre.trim().equals(c.nombre.trim(), true) }) throw CategoriaDuplicadaException()
                            if (c.id != 0 && categorias.value.none { it.id == c.id }) throw CategoriaNoExisteException()
                            val nueva = c.copy(id = if (c.id == 0) (categorias.value.maxOfOrNull { it.id } ?: 0) + 1 else c.id)
                            categorias.value = categorias.value.filter { it.id != nueva.id } + nueva
                        }
                        "eliminarCategoria" -> {
                            val c = args[0] as Categoria
                            if (detalle.value?.ticket?.categoriaId == c.id) throw CategoriaConTicketsException()
                            categorias.value = categorias.value.filter { it.id != c.id }
                        }
                        "cambiarActiva" -> categorias.value = categorias.value.map {
                            if (it.id == args[0] as Int) it.copy(activa = args[1] as Boolean) else it
                        }
                        else -> error("Operación inesperada: ${metodo.name}")
                    }
                    escrituras += metodo.name
                    Unit
                }
                if (pausar) {
                    @Suppress("UNCHECKED_CAST") val continuacion = args.last() as Continuation<Any?>
                    completar = { continuacion.resume(mutar()) }
                    COROUTINE_SUSPENDED
                } else mutar()
            }
        }
    } as AyudaRepository
}
