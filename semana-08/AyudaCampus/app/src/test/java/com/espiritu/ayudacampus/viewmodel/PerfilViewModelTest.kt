package com.espiritu.ayudacampus.viewmodel

import androidx.lifecycle.viewModelScope
import com.espiritu.ayudacampus.data.local.entity.*
import com.espiritu.ayudacampus.data.local.relation.TicketConDetalle
import com.espiritu.ayudacampus.data.repository.AyudaRepository
import java.io.IOException
import java.lang.reflect.Proxy
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PerfilViewModelTest {
    private val usuario = Usuario(7, "Juan", "juan@campus.test", "1234", "CLIENTE")
    private val modelos = mutableListOf<PerfilViewModel>()
    private var consultado = 0
    private var fallar = false
    private fun detalle(id: Int, estado: String) = TicketConDetalle(
        Ticket(id, 7, 1, "Equipo", "Problema", "Aula", estado = estado, fecha = 1000),
        emptyList(), Categoria(1, "Hardware", "pc"), usuario)
    private val fuente = MutableStateFlow(listOf(detalle(1, "ABIERTO"), detalle(2, "EN_PROCESO"),
        detalle(3, "RESUELTO"), detalle(4, "CERRADO")))
    private val repo = Proxy.newProxyInstance(AyudaRepository::class.java.classLoader,
        arrayOf(AyudaRepository::class.java)) { _, metodo, args ->
        check(metodo.name == "observarTicketsCliente")
        consultado = args[0] as Int
        flow {
            if (fallar) throw IOException("Lectura")
            emitAll(fuente)
        }
    } as AyudaRepository

    @Before fun preparar() { Dispatchers.setMain(StandardTestDispatcher()) }
    @After fun limpiar() { modelos.forEach { it.viewModelScope.cancel() }; Dispatchers.resetMain() }
    private fun modelo() = PerfilViewModel(repo, usuario).also { modelos += it }

    @Test fun contadoresSinFiltrosDeBusquedaYDelUsuarioAutenticado() = runTest {
        val vm = modelo(); advanceUntilIdle()
        assertEquals(usuario.id, consultado)
        assertEquals(PerfilUiState(1, 1, 4, false), vm.uiState.value)
        fuente.value = fuente.value + detalle(5, "ABIERTO"); advanceUntilIdle()
        assertEquals(2, vm.uiState.value.abiertos)
        assertEquals(5, vm.uiState.value.total)
    }

    @Test fun listaVaciaNoEsUnError() = runTest {
        fuente.value = emptyList()
        val vm = modelo(); advanceUntilIdle()
        assertEquals(PerfilUiState(cargando = false), vm.uiState.value)
    }

    @Test fun errorYReintentoDeContadores() = runTest {
        fallar = true
        val vm = modelo(); advanceUntilIdle()
        assertNotNull(vm.uiState.value.error)
        fallar = false; vm.cargar(); advanceUntilIdle()
        assertNull(vm.uiState.value.error)
        assertEquals(4, vm.uiState.value.total)
    }

    @Test fun soporteNoConsultaContadoresDeEstudiante() {
        assertThrows(IllegalArgumentException::class.java) {
            PerfilViewModel(repo, usuario.copy(rol = "ADMIN"))
        }
        assertEquals(0, consultado)
    }
}
