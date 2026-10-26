package com.espiritu.ayudacampus.viewmodel

import androidx.lifecycle.viewModelScope
import com.espiritu.ayudacampus.data.local.entity.Categoria
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CategoriasViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val datos = OperacionesRepositoryFake()
    private val modelos = mutableListOf<CategoriasViewModel>()
    @Before fun preparar() { Dispatchers.setMain(dispatcher) }
    @After fun limpiar() { modelos.forEach { it.viewModelScope.cancel() }; Dispatchers.resetMain() }
    private fun modelo(id: Int? = null) = CategoriasViewModel(datos.repo, datos.admin, id).also { modelos += it }

    @Test fun cargaListaYFormularioDeEdicion() = runTest {
        val vm = modelo(2); assertTrue(vm.uiState.value.cargando); runCurrent()
        assertEquals(2, vm.uiState.value.categorias.size); assertEquals("Software", vm.uiState.value.nombre)
        assertEquals("code", vm.uiState.value.icono); assertFalse(vm.uiState.value.activa)
    }

    @Test fun rechazaNombresVaciosOSoloEspacios() = runTest {
        val vm = modelo(); runCurrent()
        listOf("", " \n ").forEach { vm.cambiarNombre(it); vm.guardarFormulario(); runCurrent() }
        assertNotNull(vm.uiState.value.errorNombre); assertTrue(datos.escrituras.isEmpty())
    }

    @Test fun rechazaDuplicadosActivosEInactivosSinDistinguirMayusculas() = runTest {
        val vm = modelo(); runCurrent()
        listOf("  HARDWARE  ", "software").forEach { vm.cambiarNombre(it); vm.guardarFormulario(); runCurrent() }
        assertNotNull(vm.uiState.value.errorNombre); assertTrue(datos.escrituras.isEmpty())
    }

    @Test fun editaSinConsiderarDuplicadoSuPropioNombre() = runTest {
        val vm = modelo(1); runCurrent(); vm.cambiarNombre(" hardware "); vm.cambiarIcono("wifi")
        vm.cambiarActiva(false); vm.guardarFormulario(); runCurrent()
        assertTrue(vm.uiState.value.guardado)
        assertEquals(Categoria(1, "hardware", "wifi", false), datos.categorias.value.first { it.id == 1 })
    }

    @Test fun edicionNoPuedeUsarNombreDeOtraCategoria() = runTest {
        val vm = modelo(1); runCurrent(); vm.cambiarNombre(" Software "); vm.guardarFormulario(); runCurrent()
        assertNotNull(vm.uiState.value.errorNombre); assertTrue(datos.escrituras.isEmpty())
    }

    @Test fun creaConNombreRecortadoIconoYEstadoElegidos() = runTest {
        val vm = modelo(); runCurrent(); vm.cambiarNombre("  Impresión  "); vm.cambiarIcono("printer")
        vm.cambiarActiva(false); vm.guardarFormulario(); runCurrent()
        assertEquals(Categoria(3, "Impresión", "printer", false), datos.categorias.value.last())
        assertTrue(vm.uiState.value.guardado)
    }

    @Test fun rechazaIconosDesconocidos() = runTest {
        val vm = modelo(); runCurrent(); vm.cambiarNombre("Nueva"); vm.cambiarIcono("otro"); vm.guardarFormulario(); runCurrent()
        assertNotNull(vm.uiState.value.error); assertTrue(datos.escrituras.isEmpty())
    }

    @Test fun categoriaConTicketsNoSeElimina() = runTest {
        val vm = modelo(); runCurrent(); vm.eliminar(datos.categorias.value.first()); runCurrent()
        assertTrue(vm.uiState.value.error!!.contains("tickets asociados"))
        assertEquals(2, datos.categorias.value.size); assertNotNull(datos.detalle.value)
    }

    @Test fun eliminaCategoriaSinTicketsYActualizaLista() = runTest {
        val vm = modelo(); runCurrent(); vm.eliminar(datos.categorias.value.last()); runCurrent()
        assertEquals(listOf(1), vm.uiState.value.categorias.map { it.id })
    }

    @Test fun alternaDisponibilidadReactivamente() = runTest {
        val vm = modelo(); runCurrent(); vm.alternar(datos.categorias.value.first()); runCurrent()
        assertFalse(vm.uiState.value.categorias.first().activa)
    }

    @Test fun bloqueoEvitaGuardadosDuplicadosYDespuesDelExito() = runTest {
        val vm = modelo(); runCurrent(); vm.cambiarNombre("Red")
        datos.pausar = true; vm.guardarFormulario(); vm.guardarFormulario(); runCurrent()
        assertTrue(vm.uiState.value.procesando)
        vm.eliminar(datos.categorias.value.last()); runCurrent()
        datos.completar!!.invoke(); runCurrent(); vm.guardarFormulario(); runCurrent()
        assertEquals(listOf("guardarCategoria"), datos.escrituras)
    }

    @Test fun errorDeEscrituraConservaDatosYPermiteReintentar() = runTest {
        val vm = modelo(); runCurrent(); vm.cambiarNombre("Red")
        datos.fallos += "guardarCategoria"; vm.guardarFormulario(); runCurrent()
        assertEquals("Red", vm.uiState.value.nombre); assertNotNull(vm.uiState.value.error)
        assertFalse(vm.uiState.value.procesando); assertFalse(vm.uiState.value.guardado)
        datos.fallos.clear(); vm.guardarFormulario(); runCurrent(); assertTrue(vm.uiState.value.guardado)
    }

    @Test fun errorDeLecturaPermiteRecargar() = runTest {
        datos.fallos += "observarCategorias"; val vm = modelo(); runCurrent()
        assertNotNull(vm.uiState.value.errorCarga)
        datos.fallos.clear(); vm.cargar(); runCurrent(); assertNull(vm.uiState.value.errorCarga)
        assertEquals(2, vm.uiState.value.categorias.size)
    }

    @Test fun actualizacionesNoSobrescribenElFormulario() = runTest {
        val vm = modelo(1); runCurrent(); vm.cambiarNombre("Mi edición")
        datos.categorias.value += Categoria(3, "Cuenta", "key"); runCurrent()
        assertEquals("Mi edición", vm.uiState.value.nombre)
        datos.categorias.value = datos.categorias.value.filter { it.id != 1 }; runCurrent()
        assertTrue(vm.uiState.value.inexistente)
    }

    @Test fun capturaDuplicadoDetectadoPorRepositorioDuranteElGuardado() = runTest {
        val vm = modelo(); runCurrent(); vm.cambiarNombre("Red"); vm.guardarFormulario()
        datos.categorias.value += Categoria(3, "RED", "wifi"); runCurrent()
        assertNotNull(vm.uiState.value.errorNombre); assertFalse(vm.uiState.value.guardado)
        assertTrue(datos.escrituras.isEmpty())
    }

    @Test fun rechazaEstudianteYIdentidadInvalida() {
        listOf(datos.cliente, datos.admin.copy(id = 0)).forEach {
            assertThrows(IllegalArgumentException::class.java) { CategoriasViewModel(datos.repo, it) }
        }
    }
}
