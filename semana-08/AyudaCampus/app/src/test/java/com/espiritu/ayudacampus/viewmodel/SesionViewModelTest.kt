package com.espiritu.ayudacampus.viewmodel

import androidx.lifecycle.viewModelScope
import com.espiritu.ayudacampus.data.local.entity.Usuario
import com.espiritu.ayudacampus.data.prefs.PreferenciasSesion
import com.espiritu.ayudacampus.data.prefs.SesionGuardada
import com.espiritu.ayudacampus.data.repository.AyudaRepository
import com.espiritu.ayudacampus.model.Rol
import java.io.IOException
import java.lang.reflect.Proxy
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SesionViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val modelos = mutableListOf<SesionViewModel>()
    private val cliente = Usuario(7, "Juan", "juan@campus.test", "1234", "CLIENTE")
    private val soporte = Usuario(12, "Soporte", "soporte@campus.test", "admin", "ADMIN")
    private var consultas = 0
    private var falloRepo = false

    // El doble falla ante cualquier operación ajena al contrato de sesión.
    private val repo = Proxy.newProxyInstance(
        AyudaRepository::class.java.classLoader, arrayOf(AyudaRepository::class.java)
    ) { _, method, args ->
        consultas++
        if (falloRepo) throw IOException("Base no disponible")
        when (method.name) {
            "login" -> listOf(cliente, soporte).find { it.correo == args[0] && it.clave == args[1] }
            "obtenerUsuarioPorId" -> listOf(cliente, soporte).find { it.id == args[0] }
            else -> error("Operación inesperada: ${method.name}")
        }
    } as AyudaRepository

    private class PreferenciasMemoria : PreferenciasSesion {
        private val guardada = MutableStateFlow(SesionGuardada("", 0))
        var falloLectura = false
        var falloEscritura = false
        override val sesion: Flow<SesionGuardada> get() =
            if (falloLectura) flow { throw IOException("Lectura") } else guardada
        override val modoOscuro = MutableStateFlow(false)
        val actual get() = guardada.value
        override suspend fun guardarSesion(rol: String, usuarioId: Int) {
            if (falloEscritura) throw IOException("Escritura")
            guardada.value = SesionGuardada(rol, usuarioId)
        }
        override suspend fun cerrarSesion() { guardada.value = SesionGuardada("", 0) }
    }

    @Before fun preparar() { Dispatchers.setMain(dispatcher) }
    @After fun limpiar() {
        modelos.forEach { it.viewModelScope.cancel() }
        Dispatchers.resetMain()
    }

    private fun modelo(prefs: PreferenciasMemoria = PreferenciasMemoria(),
                       inicializar: suspend () -> Unit = {}): SesionViewModel =
        SesionViewModel(repo, prefs, inicializar).also { modelos += it }

    @Test fun sinSesionAbreLoginSinConsultarUsuarios() = runTest {
        val vm = modelo()
        advanceUntilIdle()
        assertFalse(vm.restaurando.value)
        assertNull(vm.usuarioActual.value)
        assertEquals(0, consultas)
    }

    @Test fun camposVaciosNoConsultanRepositorio() = runTest {
        val vm = modelo(); advanceUntilIdle()
        listOf("" to "1234", "juan@campus.test" to "", " " to " ").forEach { (correo, clave) ->
            vm.login(correo, clave, Rol.CLIENTE)
            assertNotNull(vm.error.value)
        }
        assertEquals(0, consultas)
    }

    @Test fun credencialesIncorrectasNoGuardanSesion() = runTest {
        val prefs = PreferenciasMemoria(); val vm = modelo(prefs); advanceUntilIdle()
        vm.login(cliente.correo, "incorrecta", Rol.CLIENTE); advanceUntilIdle()
        assertEquals("Correo o contraseña incorrectos", vm.error.value)
        assertNull(vm.usuarioActual.value)
        assertEquals(0, prefs.actual.usuarioId)
    }

    @Test fun rolIncorrectoNoGuardaSesion() = runTest {
        val prefs = PreferenciasMemoria(); val vm = modelo(prefs); advanceUntilIdle()
        vm.login(cliente.correo, cliente.clave, Rol.ADMIN); advanceUntilIdle()
        assertNotNull(vm.error.value)
        assertNull(vm.usuarioActual.value)
        assertEquals(0, prefs.actual.usuarioId)
    }

    @Test fun ambosPerfilesInicianSesionYSeRestauranPorId() = runTest {
        listOf(cliente, soporte).forEach { usuario ->
            val prefs = PreferenciasMemoria(); val vm = modelo(prefs); advanceUntilIdle()
            vm.login(" ${usuario.correo} ", usuario.clave, Rol.valueOf(usuario.rol)); advanceUntilIdle()
            assertEquals(usuario, vm.usuarioActual.value)
            assertEquals(SesionGuardada(usuario.rol, usuario.id), prefs.actual)
            val restaurado = modelo(prefs); advanceUntilIdle()
            assertEquals(usuario, restaurado.usuarioActual.value)
            assertNull(restaurado.error.value)
        }
    }

    @Test fun sesionesInconsistentesSeEliminan() = runTest {
        listOf(SesionGuardada("CLIENTE", 999), SesionGuardada("ADMIN", cliente.id),
            SesionGuardada("OTRO", cliente.id), SesionGuardada("CLIENTE", 0)).forEach { sesion ->
            val prefs = PreferenciasMemoria(); prefs.guardarSesion(sesion.rol, sesion.usuarioId)
            val vm = modelo(prefs); advanceUntilIdle()
            assertNull(vm.usuarioActual.value)
            assertNotNull(vm.error.value)
            assertEquals(SesionGuardada("", 0), prefs.actual)
        }
    }

    @Test fun restauracionEsperaLaSiembraYBloqueaElLogin() = runTest {
        val carga = CompletableDeferred<Unit>()
        val prefs = PreferenciasMemoria(); prefs.guardarSesion(cliente.rol, cliente.id)
        val vm = modelo(prefs) { carga.await() }; runCurrent()
        vm.login(soporte.correo, soporte.clave, Rol.ADMIN); runCurrent()
        assertTrue(vm.restaurando.value)
        assertEquals(0, consultas)
        carga.complete(Unit); advanceUntilIdle()
        assertEquals(cliente, vm.usuarioActual.value)
    }

    @Test fun falloInicializacionSeExponeYSigueBloqueandoElLogin() = runTest {
        val vm = modelo { throw IOException("Siembra fallida") }; advanceUntilIdle()
        assertNotNull(vm.errorInicializacion.value)
        assertFalse(vm.restaurando.value)
        vm.login(cliente.correo, cliente.clave, Rol.CLIENTE); advanceUntilIdle()
        assertEquals(0, consultas)
        assertNull(vm.usuarioActual.value)
    }

    @Test fun falloLecturaSesionPermiteVolverAlLogin() = runTest {
        val prefs = PreferenciasMemoria().apply { falloLectura = true }
        val vm = modelo(prefs); advanceUntilIdle()
        assertFalse(vm.restaurando.value)
        assertNotNull(vm.error.value)
        assertNull(vm.usuarioActual.value)
    }

    @Test fun falloEscrituraNoAutenticaAlUsuario() = runTest {
        val prefs = PreferenciasMemoria().apply { falloEscritura = true }
        val vm = modelo(prefs); advanceUntilIdle()
        vm.login(cliente.correo, cliente.clave, Rol.CLIENTE); advanceUntilIdle()
        assertNull(vm.usuarioActual.value)
        assertNotNull(vm.error.value)
        assertFalse(vm.ingresando.value)
    }

    @Test fun falloRepositorioNoDejaLaPantallaCargando() = runTest {
        val vm = modelo(); advanceUntilIdle(); falloRepo = true
        vm.login(cliente.correo, cliente.clave, Rol.CLIENTE); advanceUntilIdle()
        assertNotNull(vm.error.value)
        assertFalse(vm.ingresando.value)
        assertNull(vm.usuarioActual.value)
    }

    @Test fun aparienciaSeObservaComoEstado() = runTest {
        val prefs = PreferenciasMemoria(); val vm = modelo(prefs); advanceUntilIdle()
        prefs.modoOscuro.value = true; runCurrent()
        assertTrue(vm.modoOscuro.value)
    }

    @Test fun doblePulsacionSoloIniciaUnaPeticion() = runTest {
        val vm = modelo(); advanceUntilIdle()
        repeat(2) { vm.login(cliente.correo, cliente.clave, Rol.CLIENTE) }
        advanceUntilIdle()
        assertEquals(1, consultas)
        assertEquals(cliente, vm.usuarioActual.value)
    }
}
