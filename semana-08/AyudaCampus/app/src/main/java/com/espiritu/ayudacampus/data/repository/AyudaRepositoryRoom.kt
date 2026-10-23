package com.espiritu.ayudacampus.data.repository

import com.espiritu.ayudacampus.data.local.AyudaDatabase
import com.espiritu.ayudacampus.data.local.entity.Categoria
import com.espiritu.ayudacampus.data.local.entity.Comentario
import com.espiritu.ayudacampus.data.local.entity.Ticket
import com.espiritu.ayudacampus.data.local.entity.Usuario
import com.espiritu.ayudacampus.data.local.relation.TicketConDetalle
import com.espiritu.ayudacampus.model.ConteoCategoria
import com.espiritu.ayudacampus.model.EstadoTicket
import com.espiritu.ayudacampus.model.Prioridad
import kotlinx.coroutines.flow.Flow

class AyudaRepositoryRoom(
    private val db: AyudaDatabase
) : AyudaRepository {

    private val usuarioDao = db.usuarioDao()
    private val categoriaDao = db.categoriaDao()
    private val ticketDao = db.ticketDao()

    // USUARIOS

    override suspend fun login(
        correo: String,
        clave: String
    ): Usuario? {
        return usuarioDao.login(correo, clave)
    }

    override suspend fun obtenerUsuarioPorId(id: Int): Usuario? = usuarioDao.obtenerPorId(id)

    // CATEGORÍAS

    override fun observarCategorias(): Flow<List<Categoria>> {
        return categoriaDao.observarTodas()
    }

    override fun observarCategoriasActivas(): Flow<List<Categoria>> {
        return categoriaDao.observarActivas()
    }

    override suspend fun guardarCategoria(c: Categoria) {
        if (c.id == 0) {
            categoriaDao.insertar(c)
        } else {
            categoriaDao.actualizar(c)
        }
    }

    override suspend fun eliminarCategoria(c: Categoria) {
        categoriaDao.eliminar(c)
    }

    override suspend fun cambiarActiva(
        id: Int,
        activa: Boolean
    ) {
        categoriaDao.cambiarActiva(id, activa)
    }

    // TICKETS

    override suspend fun crearTicket(
        clienteId: Int,
        categoriaId: Int,
        titulo: String,
        descripcion: String,
        ubicacion: String,
        prioridad: Prioridad
    ): Long {
        val ticket = Ticket(
            clienteId = clienteId,
            categoriaId = categoriaId,
            titulo = titulo,
            descripcion = descripcion,
            ubicacion = ubicacion,
            prioridad = prioridad.name,
            estado = EstadoTicket.ABIERTO.name,
            fecha = System.currentTimeMillis()
        )

        return ticketDao.insertarTicket(ticket)
    }

    override fun observarTicketsCliente(
        clienteId: Int
    ): Flow<List<TicketConDetalle>> {
        return ticketDao.observarPorCliente(clienteId)
    }

    override fun observarTodosLosTickets(): Flow<List<TicketConDetalle>> {
        return ticketDao.observarTodos()
    }

    override fun observarTicket(
        id: Int
    ): Flow<TicketConDetalle?> {
        return ticketDao.observarUno(id)
    }

    override suspend fun cambiarEstado(
        ticketId: Int,
        estado: EstadoTicket
    ) {
        ticketDao.actualizarEstado(ticketId, estado.name)
    }

    override suspend fun cambiarPrioridad(
        ticketId: Int,
        prioridad: Prioridad
    ) {
        ticketDao.actualizarPrioridad(ticketId, prioridad.name)
    }

    override suspend fun cerrarConCalificacion(
        ticketId: Int,
        calificacion: Int
    ) {
        require(calificacion in 1..5) {
            "La calificación debe estar entre 1 y 5"
        }

        ticketDao.cerrar(
            id = ticketId,
            calificacion = calificacion,
            fechaCierre = System.currentTimeMillis()
        )
    }

    override suspend fun reabrir(ticketId: Int) {
        ticketDao.reabrir(ticketId)
    }

    override suspend fun agregarComentario(
        ticketId: Int,
        autor: Usuario,
        texto: String
    ) {
        require(texto.isNotBlank()) {
            "El comentario no puede estar vacío"
        }

        val comentario = Comentario(
            ticketId = ticketId,
            autorId = autor.id,
            autorNombre = autor.nombre,
            autorRol = autor.rol,
            texto = texto.trim(),
            fecha = System.currentTimeMillis()
        )

        ticketDao.insertarComentario(comentario)
    }

    // PANEL DE SOPORTE

    override fun observarCantidadPorEstado(
        estado: EstadoTicket
    ): Flow<Int> {
        return ticketDao.contarPorEstado(estado.name)
    }

    override fun observarCantidadPorCategoria(): Flow<List<ConteoCategoria>> {
        return ticketDao.contarPorCategoria()
    }

    override fun observarAltasSinAtender(
        limite: Long
    ): Flow<Int> {
        return ticketDao.contarAltasSinAtender(limite)
    }
}
