package com.espiritu.ayudacampus.viewmodel

import com.espiritu.ayudacampus.data.local.entity.*
import com.espiritu.ayudacampus.data.local.relation.TicketConDetalle
import com.espiritu.ayudacampus.data.repository.AyudaRepository
import com.espiritu.ayudacampus.model.ConteoCategoria
import com.espiritu.ayudacampus.model.EstadoTicket
import java.io.IOException
import java.lang.reflect.Proxy
import kotlinx.coroutines.flow.*

internal class SoporteRepositoryFake {
    val soporte = Usuario(2, "Soporte", "soporte@campus.test", "admin", "ADMIN")
    val cliente = Usuario(7, "Juan", "juan@campus.test", "1234", "CLIENTE")
    val categorias = MutableStateFlow(listOf(Categoria(1, "Hardware", "pc"), Categoria(2, "Software", "code")))
    val lista = MutableStateFlow<List<TicketConDetalle>>(emptyList())
    val fallos = mutableSetOf<String>()
    val limites = mutableListOf<Long>()
    val consultas = mutableListOf<String>()
    var esperar: Flow<Unit> = flowOf(Unit)

    fun ticket(id: Int, estado: String = "ABIERTO", prioridad: String = "MEDIA", fecha: Long = 0,
               categoriaId: Int = 1, titulo: String = "Ticket $id", clienteId: Int = 7): TicketConDetalle {
        val categoria = categorias.value.first { it.id == categoriaId }
        return TicketConDetalle(Ticket(id, clienteId, categoriaId, titulo, "Descripción", "Laboratorio",
            prioridad, estado, fecha), emptyList(), categoria, cliente.copy(id = clienteId))
    }

    private fun <T> lectura(nombre: String, fuente: Flow<T>): Flow<T> = flow {
        consultas += nombre
        if (nombre in fallos) throw IOException(nombre)
        esperar.first()
        emitAll(fuente)
    }

    val repo = Proxy.newProxyInstance(AyudaRepository::class.java.classLoader,
        arrayOf(AyudaRepository::class.java)) { _, metodo, args ->
        when (metodo.name) {
            "observarCantidadPorEstado" -> lectura(metodo.name, lista.map { tickets ->
                tickets.count { it.ticket.estado == (args[0] as EstadoTicket).name }
            })
            "observarCantidadPorCategoria" -> lectura(metodo.name, combine(categorias, lista) { cats, tickets ->
                cats.map { categoria -> ConteoCategoria(categoria.id, categoria.nombre,
                    tickets.count { it.ticket.categoriaId == categoria.id }) }
            })
            "observarAltasSinAtender" -> {
                val limite = args[0] as Long
                limites += limite
                lectura(metodo.name, lista.map { tickets -> tickets.count {
                    it.ticket.prioridad == "ALTA" && it.ticket.estado == "ABIERTO" && it.ticket.fecha < limite
                } })
            }
            "observarTiempoPromedioResolucion" -> lectura(metodo.name, lista.map { tickets ->
                val cerrados = tickets.map { it.ticket }.filter { it.estado == "CERRADO" && it.fechaCierre != null && it.fechaCierre >= it.fecha }
                if (cerrados.isEmpty()) null
                else cerrados.map { it.fechaCierre!! - it.fecha }.average().toLong()
            })
            "observarTodosLosTickets" -> lectura(metodo.name, lista.map { tickets ->
                tickets.sortedWith(compareBy<TicketConDetalle> { when (it.ticket.prioridad) {
                    "ALTA" -> 0; "MEDIA" -> 1; else -> 2
                } }.thenByDescending { it.ticket.fecha })
            })
            else -> error("Operación inesperada: ${metodo.name}")
        }
    } as AyudaRepository
}
