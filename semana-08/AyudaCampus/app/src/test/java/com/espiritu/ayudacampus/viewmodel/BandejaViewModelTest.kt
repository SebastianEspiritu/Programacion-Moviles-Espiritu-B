package com.espiritu.ayudacampus.viewmodel

import androidx.lifecycle.viewModelScope
import com.espiritu.ayudacampus.model.EstadoTicket
import com.espiritu.ayudacampus.model.Prioridad
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BandejaViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val datos = SoporteRepositoryFake()
    private val modelos = mutableListOf<BandejaViewModel>()
    private val ahora = 10_000_000L

    @Before fun preparar() { Dispatchers.setMain(dispatcher) }
    @After fun limpiar() { modelos.forEach { it.viewModelScope.cancel() }; Dispatchers.resetMain() }

    private fun TestScope.modelo(alertas: Boolean = false): BandejaViewModel {
        val vm = BandejaViewModel(datos.repo, datos.soporte, alertas) { ahora + testScheduler.currentTime }
        modelos += vm
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect() }
        return vm
    }

    @Test fun muestraTodosLosClientesConOrdenDelRepositorio() = runTest {
        datos.lista.value = listOf(datos.ticket(1, prioridad = "BAJA", fecha = 100),
            datos.ticket(2, prioridad = "ALTA", fecha = 10, clienteId = 8),
            datos.ticket(3, prioridad = "ALTA", fecha = 20), datos.ticket(4, prioridad = "MEDIA", fecha = 200))
        val vm = modelo(); runCurrent()
        assertEquals(listOf(3, 2, 4, 1), vm.uiState.value.tickets.map { it.ticket.id })
        assertEquals(setOf(7, 8), vm.uiState.value.tickets.map { it.ticket.clienteId }.toSet())
    }

    @Test fun filtrosDeEstadoYTodosIncluyeCerrados() = runTest {
        datos.lista.value = EstadoTicket.entries.mapIndexed { indice, estado -> datos.ticket(indice + 1, estado.name) }
        val vm = modelo(); runCurrent()
        assertEquals(4, vm.uiState.value.tickets.size)
        listOf(EstadoTicket.ABIERTO, EstadoTicket.EN_PROCESO, EstadoTicket.RESUELTO).forEach { estado ->
            vm.filtrar(estado); runCurrent()
            assertEquals(estado.name, vm.uiState.value.tickets.single().ticket.estado)
        }
        vm.filtrar(null); runCurrent(); assertEquals(4, vm.uiState.value.tickets.size)
    }

    @Test fun busquedaPorTituloIgnoraMayusculasYEspaciosExteriores() = runTest {
        datos.lista.value = listOf(datos.ticket(1, titulo = "Falla de equipo"), datos.ticket(2, titulo = "Problema de red"))
        val vm = modelo(); runCurrent(); vm.buscar("  EQUIPO  "); runCurrent()
        assertEquals(1, vm.uiState.value.tickets.single().ticket.id)
    }

    @Test fun buscaNumeroConOSinNumeralYCombinaEstado() = runTest {
        datos.lista.value = listOf(datos.ticket(12), datos.ticket(23, "RESUELTO"))
        val vm = modelo(); runCurrent()
        listOf("23", "#23").forEach { texto ->
            vm.buscar(texto); vm.filtrar(EstadoTicket.RESUELTO); runCurrent()
            assertEquals(23, vm.uiState.value.tickets.single().ticket.id)
        }
        vm.filtrar(EstadoTicket.ABIERTO); runCurrent(); assertTrue(vm.uiState.value.tickets.isEmpty())
    }

    @Test fun filtraPorSoloPrioridad() = runTest {
        datos.lista.value = listOf(
            datos.ticket(1, prioridad = "ALTA"),
            datos.ticket(2, prioridad = "MEDIA"),
            datos.ticket(3, prioridad = "BAJA")
        )
        val vm = modelo(); runCurrent()
        vm.filtrarPrioridad(Prioridad.ALTA); runCurrent()
        assertEquals(1, vm.uiState.value.tickets.single().ticket.id)

        vm.filtrarPrioridad(Prioridad.BAJA); runCurrent()
        assertEquals(3, vm.uiState.value.tickets.single().ticket.id)
    }

    @Test fun filtraPorSoloCategoria() = runTest {
        datos.lista.value = listOf(
            datos.ticket(1, categoriaId = 1),
            datos.ticket(2, categoriaId = 2)
        )
        val vm = modelo(); runCurrent()
        vm.filtrarCategoria(2); runCurrent()
        assertEquals(2, vm.uiState.value.tickets.single().ticket.id)
    }

    @Test fun combinaFiltrosTextoCategoriaYPrioridad() = runTest {
        datos.lista.value = listOf(
            datos.ticket(1, titulo = "Servidor caido", prioridad = "ALTA", categoriaId = 1),
            datos.ticket(2, titulo = "Impresora sin papel", prioridad = "ALTA", categoriaId = 1),
            datos.ticket(3, titulo = "Servidor de pruebas", prioridad = "MEDIA", categoriaId = 1),
            datos.ticket(4, titulo = "Servidor caido", prioridad = "ALTA", categoriaId = 2)
        )
        val vm = modelo(); runCurrent()

        // Búsqueda combinada: Texto "Servidor" + Prioridad ALTA + Categoría 1
        vm.buscar("servidor")
        vm.filtrarPrioridad(Prioridad.ALTA)
        vm.filtrarCategoria(1)
        runCurrent()

        assertEquals(1, vm.uiState.value.tickets.single().ticket.id)
    }

    @Test fun sinCoincidenciasDevuelveListaVacia() = runTest {
        datos.lista.value = listOf(datos.ticket(1, titulo = "Falla de red", prioridad = "BAJA", categoriaId = 1))
        val vm = modelo(); runCurrent()

        vm.buscar("servidor")
        runCurrent()

        assertTrue(vm.uiState.value.tickets.isEmpty())
    }

    @Test fun limpiarFiltrosYRestablecer() = runTest {
        datos.lista.value = listOf(
            datos.ticket(1, titulo = "Ticket 1", prioridad = "ALTA", categoriaId = 1),
            datos.ticket(2, titulo = "Ticket 2", prioridad = "MEDIA", categoriaId = 2)
        )
        val vm = modelo(); runCurrent()

        vm.buscar("Ticket 1")
        vm.filtrarPrioridad(Prioridad.ALTA)
        vm.filtrarCategoria(1)
        runCurrent()
        assertEquals(1, vm.uiState.value.tickets.size)

        vm.limpiarFiltros()
        runCurrent()
        assertEquals(2, vm.uiState.value.tickets.size)
        assertEquals("", vm.busqueda.value)
        assertNull(vm.prioridad.value)
        assertNull(vm.categoriaId.value)
    }

    @Test fun filtrosMantienenOrdenDePrioridadYFecha() = runTest {
        datos.lista.value = listOf(datos.ticket(1, fecha = 30), datos.ticket(2, prioridad = "ALTA", fecha = 10),
            datos.ticket(3, prioridad = "ALTA", fecha = 20), datos.ticket(4, "RESUELTO", "ALTA", 40))
        val vm = modelo(); runCurrent(); vm.filtrar(EstadoTicket.ABIERTO); runCurrent()
        assertEquals(listOf(3, 2, 1), vm.uiState.value.tickets.map { it.ticket.id })
    }

    @Test fun vistaDeAlertasRespetaCondicionExactaDelPanel() = runTest {
        val limite = ahora - HORA_ATENCION_MS
        datos.lista.value = listOf(datos.ticket(1, prioridad = "ALTA", fecha = limite - 1),
            datos.ticket(2, prioridad = "ALTA", fecha = limite), datos.ticket(3, "EN_PROCESO", "ALTA", 0),
            datos.ticket(4, prioridad = "MEDIA", fecha = 0))
        val vm = modelo(alertas = true); runCurrent()
        assertEquals(listOf(1), vm.uiState.value.tickets.map { it.ticket.id })
        vm.mostrarTodos(); runCurrent(); assertEquals(4, vm.uiState.value.tickets.size)
    }

    @Test fun alertaDeBandejaSeActualizaConElTiempo() = runTest {
        datos.lista.value = listOf(datos.ticket(1, prioridad = "ALTA", fecha = ahora - HORA_ATENCION_MS))
        val vm = modelo(alertas = true); runCurrent(); assertTrue(vm.uiState.value.tickets.isEmpty())
        advanceTimeBy(1000); runCurrent(); assertEquals(1, vm.uiState.value.tickets.size)
    }

    @Test fun cambiosDeRoomSeReflejanConFiltroActivo() = runTest {
        val vm = modelo(); runCurrent(); vm.filtrar(EstadoTicket.RESUELTO)
        datos.lista.value = listOf(datos.ticket(1, "RESUELTO")); runCurrent()
        assertEquals(1, vm.uiState.value.tickets.size)
        datos.lista.value = listOf(datos.ticket(1, "CERRADO")); runCurrent()
        assertTrue(vm.uiState.value.tickets.isEmpty())
    }

    @Test fun listaVaciaYCargaSeDistinguen() = runTest {
        val carga = MutableSharedFlow<Unit>(); datos.esperar = carga
        val vm = modelo(); runCurrent(); assertTrue(vm.uiState.value.cargando)
        carga.emit(Unit); runCurrent()
        assertFalse(vm.uiState.value.cargando); assertTrue(vm.uiState.value.tickets.isEmpty()); assertNull(vm.uiState.value.error)
    }

    @Test fun errorPermiteReintentarSinPerderFiltros() = runTest {
        datos.fallos += "observarTodosLosTickets"
        val vm = modelo(); runCurrent(); assertNotNull(vm.uiState.value.error)
        vm.buscar("#4"); vm.filtrar(EstadoTicket.RESUELTO)
        datos.fallos.clear(); datos.lista.value = listOf(datos.ticket(4, "RESUELTO")); vm.reintentar(); runCurrent()
        assertNull(vm.uiState.value.error); assertEquals(4, vm.uiState.value.tickets.single().ticket.id)
    }

    @Test fun rechazaEstudianteYUsuarioInvalidoAntesDeConsultar() {
        listOf(datos.cliente, datos.soporte.copy(id = 0)).forEach { usuario ->
            assertThrows(IllegalArgumentException::class.java) { BandejaViewModel(datos.repo, usuario) }
        }
        assertTrue(datos.consultas.isEmpty())
    }
}
