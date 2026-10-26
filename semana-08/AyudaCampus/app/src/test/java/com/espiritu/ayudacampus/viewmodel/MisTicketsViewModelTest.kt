package com.espiritu.ayudacampus.viewmodel

import androidx.lifecycle.viewModelScope
import com.espiritu.ayudacampus.data.local.entity.*
import com.espiritu.ayudacampus.data.local.relation.TicketConDetalle
import com.espiritu.ayudacampus.data.repository.AyudaRepository
import com.espiritu.ayudacampus.model.EstadoTicket
import java.io.IOException
import java.lang.reflect.Proxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MisTicketsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val usuario = Usuario(7, "Juan", "juan@campus.test", "1234", "CLIENTE")
    private val categoria = Categoria(1, "Hardware", "pc")
    private fun ticket(id: Int, titulo: String, estado: String, clienteId: Int = usuario.id) = TicketConDetalle(
        Ticket(id, clienteId, categoria.id, titulo, "Descripción del problema", "Laboratorio",
            estado = estado, fecha = 1000L - id), emptyList(), categoria, usuario.copy(id = clienteId))
    private val iniciales = listOf(
        ticket(12, "Computadora no enciende", "ABIERTO"),
        ticket(23, "Falla de red", "EN_PROCESO"),
        ticket(34, "Equipo reparado", "RESUELTO"),
        ticket(45, "Computadora revisada", "CERRADO"))
    private val fuente = MutableStateFlow(iniciales)
    private var flujo: Flow<List<TicketConDetalle>> = fuente
    private var fallar = false
    private var clienteConsultado: Int? = null
    private val modelos = mutableListOf<MisTicketsViewModel>()
    private val repo = Proxy.newProxyInstance(AyudaRepository::class.java.classLoader,
        arrayOf(AyudaRepository::class.java)) { _, metodo, args ->
        check(metodo.name == "observarTicketsCliente") { "Operación inesperada: ${metodo.name}" }
        val id = args[0] as Int
        clienteConsultado = id
        flow {
            if (fallar) throw IOException("Room no disponible")
            emitAll(flujo.map { lista -> lista.filter { it.ticket.clienteId == id } })
        }
    } as AyudaRepository

    @Before fun preparar() { Dispatchers.setMain(dispatcher) }
    @After fun limpiar() {
        modelos.forEach { it.viewModelScope.cancel() }
        Dispatchers.resetMain()
    }
    private fun TestScope.modelo(): MisTicketsViewModel {
        val vm = MisTicketsViewModel(repo, usuario).also { modelos += it }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect() }
        return vm
    }

    @Test fun cargaInicialHastaRecibirDatos() = runTest {
        val pendientes = MutableSharedFlow<List<TicketConDetalle>>()
        flujo = pendientes
        val vm = modelo(); runCurrent()
        assertTrue(vm.uiState.value.cargando)
        pendientes.emit(emptyList()); runCurrent()
        assertFalse(vm.uiState.value.cargando)
        assertTrue(vm.uiState.value.tickets.isEmpty())
        assertNull(vm.uiState.value.error)
    }

    @Test fun consultaSoloAlEstudianteAutenticadoYConservaOrden() = runTest {
        fuente.value = iniciales + ticket(99, "Otro estudiante", "ABIERTO", clienteId = 90)
        val vm = modelo(); runCurrent()
        assertEquals(usuario.id, clienteConsultado)
        assertEquals(listOf(12, 23, 34, 45), vm.uiState.value.tickets.map { it.ticket.id })
    }

    @Test fun todosIncluyeCerrados() = runTest {
        val vm = modelo(); runCurrent()
        assertEquals(4, vm.uiState.value.tickets.size)
        assertTrue(vm.uiState.value.tickets.any { it.ticket.estado == "CERRADO" })
    }

    @Test fun cadaFiltroSeleccionaElEstadoCorrecto() = runTest {
        val vm = modelo(); runCurrent()
        listOf(EstadoTicket.ABIERTO, EstadoTicket.EN_PROCESO, EstadoTicket.RESUELTO).forEach { estado ->
            vm.filtrar(estado); runCurrent()
            assertEquals(1, vm.uiState.value.tickets.size)
            assertEquals(estado.name, vm.uiState.value.tickets.single().ticket.estado)
        }
        vm.filtrar(null); runCurrent()
        assertEquals(4, vm.uiState.value.tickets.size)
    }

    @Test fun buscaTituloSinDistinguirMayusculasYRecortaEspacios() = runTest {
        val vm = modelo(); runCurrent()
        vm.buscar("  COMPUTADORA  "); runCurrent()
        assertEquals(listOf(12, 45), vm.uiState.value.tickets.map { it.ticket.id })
    }

    @Test fun buscaNumeroConOSinNumeral() = runTest {
        val vm = modelo(); runCurrent()
        listOf("23", "#23").forEach {
            vm.buscar(it); runCurrent()
            assertEquals(23, vm.uiState.value.tickets.single().ticket.id)
        }
    }

    @Test fun combinaBusquedaYEstadoSinCambiarContadorDeAbiertos() = runTest {
        val vm = modelo(); runCurrent()
        vm.buscar("computadora"); vm.filtrar(EstadoTicket.RESUELTO); runCurrent()
        assertTrue(vm.uiState.value.tickets.isEmpty())
        assertEquals(1, vm.uiState.value.abiertos)
        vm.filtrar(EstadoTicket.ABIERTO); runCurrent()
        assertEquals(12, vm.uiState.value.tickets.single().ticket.id)
    }

    @Test fun busquedaSinCoincidenciasNoEsUnError() = runTest {
        val vm = modelo(); runCurrent()
        vm.buscar("inexistente"); runCurrent()
        assertTrue(vm.uiState.value.tickets.isEmpty())
        assertNull(vm.uiState.value.error)
        assertFalse(vm.uiState.value.cargando)
        vm.buscar(" "); runCurrent()
        assertEquals(4, vm.uiState.value.tickets.size)
    }

    @Test fun actualizacionesDelRepositorioActualizanListaYContador() = runTest {
        val vm = modelo(); runCurrent()
        fuente.value = listOf(ticket(50, "Nuevo", "ABIERTO")) + iniciales
        runCurrent()
        assertEquals(50, vm.uiState.value.tickets.first().ticket.id)
        assertEquals(2, vm.uiState.value.abiertos)
    }

    @Test fun errorDeCargaSePuedeReintentarManteniendoFiltros() = runTest {
        fallar = true
        val vm = modelo(); runCurrent()
        assertNotNull(vm.uiState.value.error)
        assertFalse(vm.uiState.value.cargando)
        vm.buscar("red"); vm.filtrar(EstadoTicket.EN_PROCESO)
        fallar = false; vm.reintentar(); runCurrent()
        assertNull(vm.uiState.value.error)
        assertEquals(23, vm.uiState.value.tickets.single().ticket.id)
    }

    @Test fun exponeTicketsComoStateFlowFiltrado() = runTest {
        val vm = modelo()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.tickets.collect() }
        runCurrent(); vm.buscar("#34"); runCurrent()
        assertEquals(34, vm.tickets.value.single().ticket.id)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rechazaPerfilDeSoporte() {
        MisTicketsViewModel(repo, usuario.copy(rol = "ADMIN"))
    }
}
