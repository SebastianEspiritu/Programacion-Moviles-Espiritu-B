package com.espiritu.ayudacampus.viewmodel

import androidx.lifecycle.viewModelScope
import com.espiritu.ayudacampus.model.EstadoTicket
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PanelSoporteViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val datos = SoporteRepositoryFake()
    private val modelos = mutableListOf<PanelSoporteViewModel>()
    private val ahora = 10_000_000L
    @Before fun preparar() { Dispatchers.setMain(dispatcher) }
    @After fun limpiar() { modelos.forEach { it.viewModelScope.cancel() }; Dispatchers.resetMain() }

    private fun TestScope.modelo(): PanelSoporteViewModel {
        val vm = PanelSoporteViewModel(datos.repo, datos.soporte) { ahora + testScheduler.currentTime }
        modelos += vm
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect() }
        return vm
    }

    @Test fun cuentaLosCuatroEstadosYElTotal() = runTest {
        datos.lista.value = listOf(datos.ticket(1), datos.ticket(2), datos.ticket(3, "EN_PROCESO"),
            datos.ticket(4, "RESUELTO"), datos.ticket(5, "CERRADO"))
        val vm = modelo(); runCurrent()
        assertEquals(mapOf(EstadoTicket.ABIERTO to 2, EstadoTicket.EN_PROCESO to 1,
            EstadoTicket.RESUELTO to 1, EstadoTicket.CERRADO to 1), vm.uiState.value.conteos)
        assertEquals(5, vm.uiState.value.total)
    }

    @Test fun convierteEstadisticasEnDatosDeGraficosIncluyendoCeros() = runTest {
        datos.lista.value = listOf(datos.ticket(1), datos.ticket(2, "CERRADO"))
        val vm = modelo(); runCurrent()
        assertEquals(listOf("Abiertos", "En proceso", "Resueltos", "Cerrados"), vm.uiState.value.porEstado.map { it.etiqueta })
        assertEquals(listOf(1, 0, 0, 1), vm.uiState.value.porEstado.map { it.valor })
        assertEquals(listOf("Hardware", "Software"), vm.uiState.value.porCategoria.map { it.etiqueta })
        assertEquals(listOf(2, 0), vm.uiState.value.porCategoria.map { it.valor })
    }

    @Test fun alertaExigeAltaAbiertoYMasDeUnaHora() = runTest {
        val limite = ahora - HORA_ATENCION_MS
        datos.lista.value = listOf(
            datos.ticket(1, prioridad = "ALTA", fecha = limite - 1),
            datos.ticket(2, prioridad = "ALTA", fecha = limite),
            datos.ticket(3, prioridad = "ALTA", fecha = limite + 1),
            datos.ticket(4, "EN_PROCESO", "ALTA", limite - 1),
            datos.ticket(5, "RESUELTO", "ALTA", limite - 1),
            datos.ticket(6, "CERRADO", "ALTA", limite - 1),
            datos.ticket(7, prioridad = "MEDIA", fecha = limite - 1),
            datos.ticket(8, prioridad = "BAJA", fecha = limite - 1))
        val vm = modelo(); runCurrent()
        assertEquals(1, vm.uiState.value.altasSinAtender)
        assertEquals(limite, datos.limites.first())
    }

    @Test fun alertaSeActualizaAlPasarElTiempoSinCambiarRoom() = runTest {
        datos.lista.value = listOf(datos.ticket(1, prioridad = "ALTA", fecha = ahora - HORA_ATENCION_MS))
        val vm = modelo(); runCurrent()
        assertEquals(0, vm.uiState.value.altasSinAtender)
        advanceTimeBy(1000); runCurrent()
        assertEquals(1, vm.uiState.value.altasSinAtender)
        assertEquals(ahora + 1000 - HORA_ATENCION_MS, datos.limites.last())
    }

    @Test fun cambiosEnRoomActualizanPanelGraficosYAlertas() = runTest {
        val vm = modelo(); runCurrent()
        datos.lista.value = listOf(datos.ticket(1, prioridad = "ALTA", fecha = 0, categoriaId = 2))
        runCurrent()
        assertEquals(1, vm.uiState.value.total)
        assertEquals(listOf(0, 1), vm.uiState.value.porCategoria.map { it.valor })
        assertEquals(1, vm.uiState.value.altasSinAtender)
        datos.lista.value = listOf(datos.ticket(1, "EN_PROCESO", "ALTA", 0, 2)); runCurrent()
        assertEquals(0, vm.uiState.value.altasSinAtender)
        assertEquals(1, vm.uiState.value.conteos[EstadoTicket.EN_PROCESO])
    }

    @Test fun estadoVacioEsExitoConConteosEnCero() = runTest {
        val vm = modelo(); runCurrent()
        assertEquals(0, vm.uiState.value.total)
        assertTrue(vm.uiState.value.porEstado.all { it.valor == 0 })
        assertNull(vm.uiState.value.error)
        assertFalse(vm.uiState.value.cargando)
    }

    @Test fun mantieneCargaHastaRecibirTodasLasConsultas() = runTest {
        val carga = MutableSharedFlow<Unit>(); datos.esperar = carga
        val vm = modelo(); runCurrent()
        assertTrue(vm.uiState.value.cargando)
        carga.emit(Unit); runCurrent()
        assertFalse(vm.uiState.value.cargando)
    }

    @Test fun erroresEnEstadisticasCategoriasYAlertasPermitenReintentar() = runTest {
        listOf("observarCantidadPorEstado", "observarCantidadPorCategoria", "observarAltasSinAtender").forEach { consulta ->
            datos.fallos += consulta
            val vm = modelo(); runCurrent()
            assertNotNull(vm.uiState.value.error)
            assertFalse(vm.uiState.value.cargando)
            datos.fallos.clear(); vm.reintentar(); runCurrent()
            assertNull(vm.uiState.value.error)
            assertFalse(vm.uiState.value.cargando)
        }
    }

    @Test fun exponeLosStateFlowDelContrato() = runTest {
        datos.lista.value = listOf(datos.ticket(1, "CERRADO"))
        val vm = modelo()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.cerrados.collect() }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.porEstado.collect() }
        runCurrent()
        assertEquals(1, vm.cerrados.value)
        assertEquals(1, vm.porEstado.value.last().valor)
    }

    @Test fun dejaDeActualizarElRelojSinSuscriptores() = runTest {
        val vm = PanelSoporteViewModel(datos.repo, datos.soporte) { ahora + testScheduler.currentTime }; modelos += vm
        val colector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect() }
        runCurrent(); colector.cancel(); advanceTimeBy(6000); runCurrent()
        val consultas = datos.limites.size
        advanceTimeBy(5000); runCurrent()
        assertEquals(consultas, datos.limites.size)
    }

    @Test fun rechazaEstudianteYUsuarioInvalidoAntesDeConsultar() {
        listOf(datos.cliente, datos.soporte.copy(id = 0)).forEach { usuario ->
            assertThrows(IllegalArgumentException::class.java) { PanelSoporteViewModel(datos.repo, usuario) }
        }
        assertTrue(datos.consultas.isEmpty())
    }
}
