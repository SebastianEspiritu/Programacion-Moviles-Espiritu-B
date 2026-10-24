package com.espiritu.ayudacampus.viewmodel

import androidx.lifecycle.viewModelScope
import com.espiritu.ayudacampus.data.local.entity.Comentario
import com.espiritu.ayudacampus.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AtenderViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val datos = OperacionesRepositoryFake()
    private val modelos = mutableListOf<AtenderViewModel>()
    @Before fun preparar() { Dispatchers.setMain(dispatcher) }
    @After fun limpiar() { modelos.forEach { it.viewModelScope.cancel() }; Dispatchers.resetMain() }
    private fun modelo() = AtenderViewModel(datos.repo, datos.admin, 10).also { modelos += it }

    @Test fun cargaYOrdenaLaConversacion() = runTest {
        datos.detalle.value = datos.base.copy(comentarios = listOf(
            Comentario(2, 10, 7, "Juan", "CLIENTE", "Segundo", 200), Comentario(1, 10, 2, "Soporte", "ADMIN", "Primero", 100)))
        val vm = modelo(); assertTrue(vm.uiState.value.cargando); runCurrent()
        assertEquals(listOf(1, 2), vm.uiState.value.ticket!!.comentarios.map { it.id })
        assertTrue(vm.uiState.value.editable)
    }

    @Test fun respondeComoAdminYActualizaLaConversacion() = runTest {
        val vm = modelo(); runCurrent(); vm.cambiarRespuesta("  Revisando el equipo  "); vm.responder(); runCurrent()
        val comentario = vm.uiState.value.ticket!!.comentarios.single()
        assertEquals("ADMIN", comentario.autorRol); assertEquals(datos.admin.id, comentario.autorId)
        assertEquals("Revisando el equipo", comentario.texto); assertEquals("", vm.uiState.value.respuesta)
    }

    @Test fun rechazaRespuestasVacias() = runTest {
        val vm = modelo(); runCurrent(); vm.responder(" \n "); runCurrent()
        assertNotNull(vm.uiState.value.error); assertTrue(datos.escrituras.isEmpty())
    }

    @Test fun permiteCambiarEntreLosTresEstadosSinImponerOrden() = runTest {
        listOf(EstadoTicket.ABIERTO, EstadoTicket.EN_PROCESO, EstadoTicket.RESUELTO).forEach { origen ->
            listOf(EstadoTicket.ABIERTO, EstadoTicket.EN_PROCESO, EstadoTicket.RESUELTO).forEach { destino ->
                datos.detalle.value = datos.base.copy(ticket = datos.base.ticket.copy(estado = origen.name))
                val vm = modelo(); runCurrent(); vm.cambiarEstado(destino)
                assertEquals(origen.name, datos.detalle.value!!.ticket.estado)
                vm.guardar(); runCurrent(); assertEquals(destino.name, datos.detalle.value!!.ticket.estado)
            }
        }
    }

    @Test fun soporteNoPuedeElegirCerrado() = runTest {
        val vm = modelo(); runCurrent(); vm.cambiarEstado(EstadoTicket.CERRADO); vm.guardar(); runCurrent()
        assertEquals("ABIERTO", datos.detalle.value!!.ticket.estado)
        assertTrue(datos.escrituras.isEmpty())
    }

    @Test fun modificaLasTresPrioridadesAlGuardar() = runTest {
        val vm = modelo(); runCurrent()
        Prioridad.entries.forEach { prioridad ->
            vm.cambiarPrioridad(prioridad); vm.guardar(); runCurrent()
            assertEquals(prioridad.name, datos.detalle.value!!.ticket.prioridad)
        }
    }

    @Test fun cerradoBloqueaRespuestaEstadoYPrioridad() = runTest {
        datos.detalle.value = datos.base.copy(ticket = datos.base.ticket.copy(estado = "CERRADO"))
        val vm = modelo(); runCurrent(); vm.cambiarEstado(EstadoTicket.ABIERTO); vm.cambiarPrioridad(Prioridad.ALTA)
        vm.responder("No permitido"); runCurrent(); vm.guardar(); runCurrent()
        assertFalse(vm.uiState.value.editable); assertTrue(datos.escrituras.isEmpty())
    }

    @Test fun revalidaElEstadoAntesDeEscribir() = runTest {
        val vm = modelo(); runCurrent(); vm.cambiarEstado(EstadoTicket.RESUELTO); vm.guardar()
        datos.detalle.value = datos.base.copy(ticket = datos.base.ticket.copy(estado = "CERRADO")); runCurrent()
        assertTrue(datos.escrituras.isEmpty()); assertNotNull(vm.uiState.value.error)
    }

    @Test fun evitaRespuestasYGuardadosSimultaneos() = runTest {
        val vm = modelo(); runCurrent(); datos.pausar = true
        vm.responder("Respuesta"); runCurrent(); assertTrue(vm.uiState.value.procesando)
        vm.responder("Duplicada"); vm.guardar(); runCurrent(); datos.completar!!.invoke(); runCurrent()
        assertEquals(listOf("agregarComentario"), datos.escrituras)
    }

    @Test fun guardarNoDuplicaNiRepiteCambiosYaGuardados() = runTest {
        val vm = modelo(); runCurrent(); vm.cambiarEstado(EstadoTicket.RESUELTO)
        vm.guardar(); vm.guardar(); runCurrent(); vm.guardar(); runCurrent()
        assertEquals(listOf("guardarAtencion"), datos.escrituras)
    }

    @Test fun errorConservaBorradorYPermiteReintentar() = runTest {
        val vm = modelo(); runCurrent(); vm.cambiarRespuesta("Respuesta pendiente")
        datos.fallos += "agregarComentario"; vm.responder(); runCurrent()
        assertEquals("Respuesta pendiente", vm.uiState.value.respuesta); assertNotNull(vm.uiState.value.error)
        datos.fallos.clear(); vm.responder(); runCurrent(); assertEquals("", vm.uiState.value.respuesta)
    }

    @Test fun errorAlGuardarConservaLasSelecciones() = runTest {
        val vm = modelo(); runCurrent(); vm.cambiarEstado(EstadoTicket.RESUELTO); vm.cambiarPrioridad(Prioridad.ALTA)
        datos.fallos += "guardarAtencion"; vm.guardar(); runCurrent()
        assertTrue(vm.uiState.value.estadoEditado); assertTrue(vm.uiState.value.prioridadEditada)
        assertFalse(vm.uiState.value.procesando)
        datos.fallos.clear(); vm.guardar(); runCurrent(); assertEquals("RESUELTO", datos.detalle.value!!.ticket.estado)
    }

    @Test fun actualizaDatosSinDescartarSeleccionesPendientes() = runTest {
        val vm = modelo(); runCurrent(); vm.cambiarPrioridad(Prioridad.BAJA)
        datos.detalle.value = datos.base.copy(ticket = datos.base.ticket.copy(estado = "EN_PROCESO")); runCurrent()
        assertEquals(EstadoTicket.EN_PROCESO, vm.uiState.value.estado)
        assertEquals(Prioridad.BAJA, vm.uiState.value.prioridad)
    }

    @Test fun manejaInexistenteYErrorDeCarga() = runTest {
        datos.fallos += "observarTicket"; val vm = modelo(); runCurrent(); assertNotNull(vm.uiState.value.errorCarga)
        datos.fallos.clear(); vm.cargar(); runCurrent(); assertNotNull(vm.uiState.value.ticket)
        datos.detalle.value = null; runCurrent(); assertTrue(vm.uiState.value.inexistente)
        vm.responder("No existe"); runCurrent(); assertTrue(datos.escrituras.isEmpty())
    }

    @Test fun impideAccesoDeEstudianteOIdentidadInvalida() {
        listOf(datos.cliente, datos.admin.copy(id = 0)).forEach {
            assertThrows(IllegalArgumentException::class.java) { AtenderViewModel(datos.repo, it, 10) }
        }
    }
}
