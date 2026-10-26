package com.espiritu.ayudacampus.viewmodel

import androidx.lifecycle.viewModelScope
import com.espiritu.ayudacampus.data.local.entity.*
import com.espiritu.ayudacampus.data.local.relation.TicketConDetalle
import com.espiritu.ayudacampus.data.repository.AyudaRepository
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
class DetalleTicketViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val usuario = Usuario(7, "Juan", "juan@campus.test", "1234", "CLIENTE")
    private val base = TicketConDetalle(Ticket(10, 7, 2, "Equipo averiado", "No enciende", "Aula 201",
        estado = "RESUELTO", fecha = 1000), emptyList(), Categoria(2, "Hardware", "pc"), usuario)
    private val fuente = MutableStateFlow<TicketConDetalle?>(base)
    private var flujo: Flow<TicketConDetalle?> = fuente
    private var falloLectura = false
    private var falloAccion: String? = null
    private var pausar = false
    private var completar: (() -> Unit)? = null
    private var consultas = 0
    private val escrituras = mutableListOf<String>()
    private val modelos = mutableListOf<DetalleTicketViewModel>()
    private val repo = Proxy.newProxyInstance(AyudaRepository::class.java.classLoader,
        arrayOf(AyudaRepository::class.java)) { _, metodo, args ->
        if (metodo.name == "observarTicket") {
            assertEquals(10, args[0])
            consultas++
            flow {
                if (falloLectura) throw IOException("Lectura")
                emitAll(flujo)
            }
        } else {
            val mutar: () -> Any? = {
                if (falloAccion == metodo.name) throw IOException("Escritura")
                escrituras += metodo.name
                val detalle = requireNotNull(fuente.value)
                when (metodo.name) {
                    "agregarComentario" -> {
                        assertEquals(usuario, args[1])
                        fuente.value = detalle.copy(comentarios = detalle.comentarios + Comentario(
                            id = detalle.comentarios.size + 1, ticketId = 10, autorId = usuario.id,
                            autorNombre = usuario.nombre, autorRol = usuario.rol, texto = args[2] as String, fecha = 1500))
                    }
                    "cerrarConCalificacion" -> fuente.value = detalle.copy(ticket = detalle.ticket.copy(
                        estado = "CERRADO", calificacion = args[1] as Int, fechaCierre = 2000))
                    "reabrir" -> fuente.value = detalle.copy(ticket = detalle.ticket.copy(
                        estado = "ABIERTO", calificacion = null, fechaCierre = null))
                    else -> error("Operación inesperada: ${metodo.name}")
                }
                Unit
            }
            if (pausar) {
                @Suppress("UNCHECKED_CAST") val cont = args.last() as Continuation<Any?>
                completar = { cont.resume(mutar()) }
                COROUTINE_SUSPENDED
            } else mutar()
        }
    } as AyudaRepository

    @Before fun preparar() { Dispatchers.setMain(dispatcher) }
    @After fun limpiar() {
        modelos.forEach { it.viewModelScope.cancel() }
        Dispatchers.resetMain()
    }
    private fun modelo(id: Int = 10) = DetalleTicketViewModel(repo, usuario, id).also { modelos += it }
    private fun estado(valor: String) { fuente.value = base.copy(ticket = base.ticket.copy(estado = valor)) }

    @Test fun cargaTicketExistente() = runTest {
        val vm = modelo(); assertTrue(vm.uiState.value.cargando); runCurrent()
        assertEquals(base, vm.uiState.value.ticket)
        assertFalse(vm.uiState.value.cargando)
        assertNull(vm.uiState.value.errorCarga)
    }

    @Test fun distingueCargaDeTicketInexistente() = runTest {
        val pendientes = MutableSharedFlow<TicketConDetalle?>(); flujo = pendientes
        val vm = modelo(); runCurrent(); assertTrue(vm.uiState.value.cargando)
        pendientes.emit(null); runCurrent()
        assertTrue(vm.uiState.value.inexistente)
        assertFalse(vm.uiState.value.cargando)
        assertNull(vm.uiState.value.ticket)
    }

    @Test fun idInvalidoNoConsultaRoom() = runTest {
        val vm = modelo(0); runCurrent()
        assertTrue(vm.uiState.value.inexistente)
        assertEquals(0, consultas)
    }

    @Test fun ordenaComentariosPorFechaYDesempataPorId() = runTest {
        fun comentario(id: Int, fecha: Long) = Comentario(id, 10, 7, "Juan", "CLIENTE", "Mensaje $id", fecha)
        fuente.value = base.copy(comentarios = listOf(comentario(3, 200), comentario(2, 100), comentario(1, 100)))
        val vm = modelo(); runCurrent()
        assertEquals(listOf(1, 2, 3), vm.uiState.value.ticket!!.comentarios.map { it.id })
    }

    @Test fun mensajesVaciosNoEscriben() = runTest {
        val vm = modelo(); runCurrent()
        listOf("", "   ", "\n\t").forEach { vm.comentar(it); runCurrent() }
        assertTrue(escrituras.isEmpty())
        assertNotNull(vm.uiState.value.errorAccion)
    }

    @Test fun comentaEnAbiertoEnProcesoYResuelto() = runTest {
        listOf("ABIERTO", "EN_PROCESO", "RESUELTO").forEach { valor ->
            estado(valor); val vm = modelo(); runCurrent()
            vm.cambiarBorrador("  Mensaje del estudiante  "); vm.comentar(); runCurrent()
            assertEquals("Mensaje del estudiante", vm.uiState.value.ticket!!.comentarios.single().texto)
            assertEquals("", vm.uiState.value.borrador)
            assertNull(vm.uiState.value.errorAccion)
        }
        assertEquals(3, escrituras.size)
    }

    @Test fun cerradoNoPermiteComentarCerrarNiReabrir() = runTest {
        estado("CERRADO"); val vm = modelo(); runCurrent()
        vm.comentar("Mensaje"); runCurrent(); vm.cerrar(4); runCurrent(); vm.reabrir(); runCurrent()
        assertTrue(escrituras.isEmpty())
        assertFalse(vm.uiState.value.puedeComentar)
        assertFalse(vm.uiState.value.puedeResolver)
    }

    @Test fun cierreRechazaCalificacionesFueraDeRango() = runTest {
        val vm = modelo(); runCurrent()
        listOf(-1, 0, 6).forEach { vm.cerrar(it); runCurrent() }
        assertTrue(escrituras.isEmpty())
        assertNotNull(vm.uiState.value.errorAccion)
    }

    @Test fun cierreAceptaCadaValorEntreUnoYCinco() = runTest {
        (1..5).forEach { valor ->
            fuente.value = base; val vm = modelo(); runCurrent(); vm.cerrar(valor); runCurrent()
            assertEquals("CERRADO", vm.uiState.value.ticket!!.ticket.estado)
            assertEquals(valor, vm.uiState.value.ticket!!.ticket.calificacion)
            assertEquals(2000L, vm.uiState.value.ticket!!.ticket.fechaCierre)
        }
    }

    @Test fun soloResueltoSePuedeCerrarOReabrir() = runTest {
        listOf("ABIERTO", "EN_PROCESO", "CERRADO").forEach {
            estado(it); val vm = modelo(); runCurrent()
            vm.cerrar(5); runCurrent(); vm.reabrir(); runCurrent()
        }
        assertTrue(escrituras.isEmpty())
    }

    @Test fun reabrirLimpiaCalificacionYFecha() = runTest {
        fuente.value = base.copy(ticket = base.ticket.copy(calificacion = 4, fechaCierre = 1500))
        val vm = modelo(); runCurrent(); vm.reabrir(); runCurrent()
        val ticket = vm.uiState.value.ticket!!.ticket
        assertEquals("ABIERTO", ticket.estado)
        assertNull(ticket.calificacion); assertNull(ticket.fechaCierre)
    }

    @Test fun impideAccionesSimultaneas() = runTest {
        val vm = modelo(); runCurrent(); pausar = true
        vm.comentar("Primer mensaje"); runCurrent()
        assertTrue(vm.uiState.value.procesando)
        vm.comentar("Duplicado"); vm.cerrar(5); vm.reabrir(); runCurrent()
        completar!!.invoke(); runCurrent()
        assertEquals(listOf("agregarComentario"), escrituras)
        assertFalse(vm.uiState.value.procesando)
    }

    @Test fun dobleCierreNoDuplicaAcciones() = runTest {
        val vm = modelo(); runCurrent()
        vm.cerrar(4); vm.cerrar(4); runCurrent(); vm.cerrar(4); runCurrent()
        assertEquals(listOf("cerrarConCalificacion"), escrituras)
    }

    @Test fun sinPropiedadNoExponeContenidoNiPermiteAcciones() = runTest {
        fuente.value = base.copy(ticket = base.ticket.copy(clienteId = 99))
        val vm = modelo(); runCurrent()
        assertTrue(vm.uiState.value.sinAcceso)
        assertNull(vm.uiState.value.ticket)
        vm.comentar("Mensaje"); vm.cerrar(4); vm.reabrir(); runCurrent()
        assertTrue(escrituras.isEmpty())
    }

    @Test fun revalidaPropietarioAntesDeEscribir() = runTest {
        val vm = modelo(); runCurrent(); vm.comentar("Mensaje")
        fuente.value = base.copy(ticket = base.ticket.copy(clienteId = 99)); runCurrent()
        assertTrue(escrituras.isEmpty()); assertTrue(vm.uiState.value.sinAcceso)
    }

    @Test fun revalidaEstadoAntesDeComentar() = runTest {
        val vm = modelo(); runCurrent(); vm.comentar("Mensaje")
        estado("CERRADO"); runCurrent()
        assertTrue(escrituras.isEmpty()); assertNotNull(vm.uiState.value.errorAccion)
    }

    @Test fun cambiosExternosActualizanLaInterfaz() = runTest {
        val vm = modelo(); runCurrent(); estado("EN_PROCESO"); runCurrent()
        assertEquals("EN_PROCESO", vm.uiState.value.ticket!!.ticket.estado)
        assertTrue(vm.uiState.value.puedeComentar)
        assertFalse(vm.uiState.value.puedeResolver)
        fuente.value = null; runCurrent(); assertTrue(vm.uiState.value.inexistente)
    }

    @Test fun errorDeCargaSePuedeReintentar() = runTest {
        falloLectura = true; val vm = modelo(); runCurrent()
        assertNotNull(vm.uiState.value.errorCarga); assertNull(vm.uiState.value.ticket)
        falloLectura = false; vm.cargar(); runCurrent()
        assertEquals(base, vm.uiState.value.ticket); assertNull(vm.uiState.value.errorCarga)
    }

    @Test fun errorDeEscrituraConservaBorradorYPermiteReintentar() = runTest {
        val vm = modelo(); runCurrent(); vm.cambiarBorrador("Mi mensaje")
        falloAccion = "agregarComentario"; vm.comentar(); runCurrent()
        assertEquals("Mi mensaje", vm.uiState.value.borrador)
        assertNotNull(vm.uiState.value.errorAccion); assertFalse(vm.uiState.value.procesando)
        falloAccion = null; vm.comentar(); runCurrent()
        assertEquals("", vm.uiState.value.borrador)
        assertEquals(1, vm.uiState.value.ticket!!.comentarios.size)
    }

    @Test fun comentarioFinalNoSeDuplicaSiElCierreFalla() = runTest {
        val vm = modelo(); runCurrent(); vm.cambiarComentarioFinal("  Gracias  ")
        falloAccion = "cerrarConCalificacion"; vm.cerrar(4); runCurrent()
        assertEquals("", vm.uiState.value.comentarioFinal)
        assertEquals("Gracias", fuente.value!!.comentarios.single().texto)
        assertNotNull(vm.uiState.value.errorAccion)
        falloAccion = null; vm.cerrar(4); runCurrent()
        assertEquals(1, fuente.value!!.comentarios.size)
        assertEquals("CERRADO", fuente.value!!.ticket.estado)
    }

    @Test fun exponeDetalleComoStateFlow() = runTest {
        val vm = modelo()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.ticket.collect() }
        runCurrent(); assertEquals(base, vm.ticket.value)
    }

    @Test(expected = IllegalArgumentException::class)
    fun soporteNoPuedeUsarElDetalleDeEstudiante() {
        DetalleTicketViewModel(repo, usuario.copy(rol = "ADMIN"), 10)
    }
}
