package com.espiritu.ayudacampus

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.espiritu.ayudacampus.data.local.AyudaDatabase
import com.espiritu.ayudacampus.data.local.entity.*
import com.espiritu.ayudacampus.data.repository.*
import com.espiritu.ayudacampus.model.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RepositorySoporteRoomTest {
    private lateinit var db: AyudaDatabase
    private lateinit var repo: AyudaRepositoryRoom
    private var categoriaId = 0
    private var usuarioId = 0
    private var ticketId = 0
    @Before fun preparar() = runBlocking {
        db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, AyudaDatabase::class.java).build()
        repo = AyudaRepositoryRoom(db)
        categoriaId = db.categoriaDao().insertar(Categoria(nombre = "Hardware", icono = "pc")).toInt()
        usuarioId = db.usuarioDao().insertarTodos(listOf(Usuario(nombre = "Prueba", correo = "test@campus.test", clave = "test", rol = "CLIENTE"))).single().toInt()
        ticketId = repo.crearTicket(usuarioId, categoriaId, "Equipo", "No enciende", "Aula", Prioridad.MEDIA).toInt()
    }
    @After fun cerrar() { db.close() }

    @Test fun guardaEstadoYPrioridadSinModificarOtrosDatos() = runBlocking {
        val antes = db.ticketDao().obtenerPorId(ticketId)!!
        repo.guardarAtencion(ticketId, EstadoTicket.RESUELTO, Prioridad.ALTA)
        assertEquals(antes.copy(estado = "RESUELTO", prioridad = "ALTA"), db.ticketDao().obtenerPorId(ticketId))
    }

    @Test fun cerradoRechazaModificacionesYComentarios() = runBlocking {
        repo.cerrarConCalificacion(ticketId, 4)
        val antes = repo.observarTicket(ticketId).first()!!
        try { repo.guardarAtencion(ticketId, EstadoTicket.ABIERTO, Prioridad.BAJA); fail("Debió rechazar ticket cerrado") }
        catch (_: TicketCerradoException) { }
        try { repo.agregarComentario(ticketId, Usuario(usuarioId, "Prueba", "test", "test", "ADMIN"), "Mensaje"); fail("Debió rechazar comentario") }
        catch (_: TicketCerradoException) { }
        assertEquals(antes, repo.observarTicket(ticketId).first())
    }

    @Test fun soporteNoPuedeGuardarEstadoCerrado() = runBlocking {
        try { repo.guardarAtencion(ticketId, EstadoTicket.CERRADO, Prioridad.ALTA); fail("Debió rechazar cierre") }
        catch (_: IllegalArgumentException) { }
        assertEquals("ABIERTO", db.ticketDao().obtenerPorId(ticketId)!!.estado)
    }

    @Test fun impideDuplicadosInclusoEnGuardadosConcurrentes() = runBlocking {
        val resultados = listOf("Red", " red ").map { nombre -> async {
            try { repo.guardarCategoria(Categoria(nombre = nombre, icono = "wifi")); true }
            catch (_: CategoriaDuplicadaException) { false }
        } }.awaitAll()
        assertEquals(1, resultados.count { it })
        assertEquals(1, db.categoriaDao().obtenerTodas().count { it.nombre.equals("Red", true) })
    }

    @Test fun noEliminaCategoriasReferenciadasNiSusTickets() = runBlocking {
        val categoria = db.categoriaDao().obtenerTodas().single()
        try { repo.eliminarCategoria(categoria); fail("Debió rechazar eliminación") }
        catch (_: CategoriaConTicketsException) { }
        assertEquals(categoria, db.categoriaDao().obtenerTodas().single())
        assertNotNull(db.ticketDao().obtenerPorId(ticketId))
    }

    @Test fun creaEditaDesactivaYEliminaCategoriaSinReferencias() = runBlocking {
        repo.guardarCategoria(Categoria(nombre = "  Cuenta  ", icono = "key"))
        val creada = repo.observarCategorias().first().single { it.nombre == "Cuenta" }
        repo.guardarCategoria(creada.copy(nombre = "Acceso", activa = false))
        assertFalse(repo.observarCategoriasActivas().first().any { it.id == creada.id })
        assertEquals("Acceso", repo.observarCantidadPorCategoria().first().single { it.categoriaId == creada.id }.nombre)
        repo.eliminarCategoria(creada)
        assertFalse(repo.observarCategorias().first().any { it.id == creada.id })
        assertNotNull(db.ticketDao().obtenerPorId(ticketId))
    }

    @Test fun guardaRespuestaConAutorYRol() = runBlocking {
        val admin = Usuario(50, "Soporte", "soporte@campus.test", "admin", "ADMIN")
        repo.agregarComentario(ticketId, admin, "  Respuesta  ")
        val comentario = repo.observarTicket(ticketId).first()!!.comentarios.single()
        assertEquals("Soporte", comentario.autorNombre); assertEquals("ADMIN", comentario.autorRol)
        assertEquals("Respuesta", comentario.texto)
    }
}
