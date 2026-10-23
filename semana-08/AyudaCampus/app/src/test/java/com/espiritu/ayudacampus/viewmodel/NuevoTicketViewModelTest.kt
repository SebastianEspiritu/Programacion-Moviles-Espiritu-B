package com.espiritu.ayudacampus.viewmodel

import androidx.lifecycle.viewModelScope
import com.espiritu.ayudacampus.data.local.entity.Categoria
import com.espiritu.ayudacampus.data.local.entity.Usuario
import com.espiritu.ayudacampus.data.repository.AyudaRepository
import com.espiritu.ayudacampus.model.Prioridad
import java.io.IOException
import java.lang.reflect.Proxy
import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.resume
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NuevoTicketViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val usuario = Usuario(7, "Juan", "juan@campus.test", "1234", "CLIENTE")
    private val categorias = MutableStateFlow(listOf(Categoria(2, "Hardware", "pc")))
    private val recibe = MutableStateFlow(true)
    private var recepcion: Flow<Boolean> = recibe
    private var falloCategorias = false
    private var falloRegistro = false
    private var pausarRegistro = false
    private var pendiente: Continuation<Long>? = null
    private val registros = mutableListOf<List<Any?>>()
    private val modelos = mutableListOf<NuevoTicketViewModel>()
    private val repo = Proxy.newProxyInstance(AyudaRepository::class.java.classLoader,
        arrayOf(AyudaRepository::class.java)) { _, metodo, args ->
        when (metodo.name) {
            "observarCategoriasActivas" -> flow {
                if (falloCategorias) throw IOException("Categorías")
                emitAll(categorias.map { lista -> lista.filter { it.activa } })
            }
            "crearTicket" -> {
                registros += args.take(6)
                if (falloRegistro) throw IOException("Registro")
                if (pausarRegistro) {
                    @Suppress("UNCHECKED_CAST")
                    pendiente = args.last() as Continuation<Long>
                    COROUTINE_SUSPENDED
                } else 42L
            }
            else -> error("Operación inesperada: ${metodo.name}")
        }
    } as AyudaRepository

    @Before fun preparar() { Dispatchers.setMain(dispatcher) }
    @After fun limpiar() {
        modelos.forEach { it.viewModelScope.cancel() }
        Dispatchers.resetMain()
    }
    private fun modelo() = NuevoTicketViewModel(repo, usuario, recepcion).also { modelos += it }
    private fun completar(vm: NuevoTicketViewModel) {
        vm.seleccionarCategoria(2)
        vm.cambiarTitulo("  Equipo averiado  ")
        vm.cambiarDescripcion("  El equipo no logra encender  ")
        vm.cambiarUbicacion("  Aula 201  ")
    }

    @Test fun prioridadMediaPorDefectoYCargaSoloCategoriasActivas() = runTest {
        categorias.value += Categoria(3, "Inactiva", "wifi", activa = false)
        val vm = modelo()
        assertTrue(vm.uiState.value.cargando)
        assertEquals(Prioridad.MEDIA, vm.uiState.value.prioridad)
        runCurrent()
        assertEquals(listOf(2), vm.uiState.value.categorias.map { it.id })
        assertTrue(vm.uiState.value.recibirTickets)
        assertFalse(vm.uiState.value.cargando)
    }

    @Test fun formularioVacioMuestraErroresPorCampo() = runTest {
        val vm = modelo(); runCurrent(); vm.enviar()
        assertNotNull(vm.uiState.value.errorCategoria)
        assertNotNull(vm.uiState.value.errorTitulo)
        assertNotNull(vm.uiState.value.errorDescripcion)
        assertTrue(registros.isEmpty())
    }

    @Test fun tituloNoCuentaEspaciosDeLosExtremos() = runTest {
        val vm = modelo(); runCurrent(); completar(vm)
        vm.cambiarTitulo("  abcd  "); vm.enviar(); runCurrent()
        assertNotNull(vm.uiState.value.errorTitulo)
        assertTrue(registros.isEmpty())
    }

    @Test fun descripcionNoCuentaEspaciosDeLosExtremos() = runTest {
        val vm = modelo(); runCurrent(); completar(vm)
        vm.cambiarDescripcion("  123456789  "); vm.enviar(); runCurrent()
        assertNotNull(vm.uiState.value.errorDescripcion)
        assertTrue(registros.isEmpty())
    }

    @Test fun aceptaLimitesExactosYUbicacionOpcional() = runTest {
        val vm = modelo(); runCurrent(); completar(vm)
        vm.cambiarTitulo("  12345  "); vm.cambiarDescripcion("  1234567890  ")
        vm.cambiarUbicacion(""); vm.enviar(); runCurrent()
        assertEquals(42L, vm.uiState.value.ticketCreado)
        assertEquals(listOf(7, 2, "12345", "1234567890", "", Prioridad.MEDIA), registros.single())
    }

    @Test fun categoriaEsObligatoriaAunqueLosTextosSeanValidos() = runTest {
        val vm = modelo(); runCurrent()
        vm.cambiarTitulo("Equipo"); vm.cambiarDescripcion("Equipo sin funcionar")
        vm.enviar(); runCurrent()
        assertNotNull(vm.uiState.value.errorCategoria)
        assertTrue(registros.isEmpty())
    }

    @Test fun noRegistraSiMesaEstaCerrada() = runTest {
        recibe.value = false
        val vm = modelo(); runCurrent(); completar(vm); vm.enviar(); runCurrent()
        assertEquals("Mesa de ayuda cerrada", vm.uiState.value.error)
        assertTrue(registros.isEmpty())
    }

    @Test fun vuelveAComprobarRecepcionAntesDeRegistrar() = runTest {
        val vm = modelo(); runCurrent(); completar(vm); vm.enviar()
        recibe.value = false; runCurrent()
        assertEquals("Mesa de ayuda cerrada", vm.uiState.value.error)
        assertTrue(registros.isEmpty())
        assertFalse(vm.uiState.value.enviando)
    }

    @Test fun rechazaCategoriaDesactivadaAntesDeRegistrar() = runTest {
        val vm = modelo(); runCurrent(); completar(vm); vm.enviar()
        categorias.value = emptyList(); runCurrent()
        assertNotNull(vm.uiState.value.errorCategoria)
        assertTrue(registros.isEmpty())
    }

    @Test fun noRegistraMientrasCargaDisponibilidad() = runTest {
        recepcion = MutableSharedFlow()
        val vm = modelo(); runCurrent(); completar(vm); vm.enviar(); runCurrent()
        assertTrue(vm.uiState.value.cargando)
        assertTrue(registros.isEmpty())
    }

    @Test fun impideDuplicadosDuranteRegistroYDespuesDelExito() = runTest {
        pausarRegistro = true
        val vm = modelo(); runCurrent(); completar(vm)
        vm.enviar(); vm.enviar(); runCurrent()
        assertEquals(1, registros.size)
        assertTrue(vm.uiState.value.enviando)
        vm.enviar(); vm.cambiarTitulo("Otro título")
        assertEquals("  Equipo averiado  ", vm.uiState.value.titulo)
        pendiente!!.resume(42L); runCurrent()
        vm.enviar(); runCurrent()
        assertEquals(1, registros.size)
        assertEquals(42L, vm.uiState.value.ticketCreado)
        assertFalse(vm.uiState.value.enviando)
    }

    @Test fun registraConUsuarioPrioridadYTextosNormalizados() = runTest {
        val vm = modelo(); runCurrent(); completar(vm)
        vm.cambiarPrioridad(Prioridad.ALTA); vm.enviar(); runCurrent()
        assertEquals(listOf(7, 2, "Equipo averiado", "El equipo no logra encender", "Aula 201", Prioridad.ALTA), registros.single())
        assertEquals(42L, vm.uiState.value.ticketCreado)
        assertNull(vm.uiState.value.error)
    }

    @Test fun errorDeRegistroConservaDatosYPermiteReintentar() = runTest {
        falloRegistro = true
        val vm = modelo(); runCurrent(); completar(vm); vm.enviar(); runCurrent()
        assertNotNull(vm.uiState.value.error)
        assertNull(vm.uiState.value.ticketCreado)
        assertFalse(vm.uiState.value.enviando)
        assertEquals("  Equipo averiado  ", vm.uiState.value.titulo)
        falloRegistro = false; vm.enviar(); runCurrent()
        assertEquals(42L, vm.uiState.value.ticketCreado)
    }

    @Test fun errorDeCategoriasBloqueaRegistroYPermiteRecargar() = runTest {
        falloCategorias = true
        val vm = modelo(); runCurrent(); completar(vm); vm.enviar(); runCurrent()
        assertNotNull(vm.uiState.value.errorCarga)
        assertTrue(registros.isEmpty())
        falloCategorias = false; vm.cargarDatos(); runCurrent(); vm.enviar(); runCurrent()
        assertEquals(42L, vm.uiState.value.ticketCreado)
    }

    @Test fun errorDePreferenciasBloqueaRegistro() = runTest {
        recepcion = flow { throw IOException("DataStore") }
        val vm = modelo(); runCurrent(); completar(vm); vm.enviar(); runCurrent()
        assertNotNull(vm.uiState.value.errorCarga)
        assertFalse(vm.uiState.value.recibirTickets)
        assertTrue(registros.isEmpty())
    }

    @Test(expected = IllegalArgumentException::class)
    fun soporteNoPuedeCrearTicketsComoEstudiante() {
        NuevoTicketViewModel(repo, usuario.copy(rol = "ADMIN"), recibe)
    }
}
