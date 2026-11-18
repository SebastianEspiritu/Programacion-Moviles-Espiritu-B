package com.espiritu.centrotareas.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.espiritu.centrotareas.data.TareasRepository
import com.espiritu.centrotareas.model.CargaUi
import com.espiritu.centrotareas.model.PrimosUi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class TareasViewModel(private val repo: TareasRepository) : ViewModel() {

    // ----- Tarea con progreso (pasos 1 a 4)
    private val _mensaje = MutableStateFlow("Listo")
    val mensaje: StateFlow<String> = _mensaje.asStateFlow()

    // Paso 3: progreso de 0f a 1f (la pantalla solo ve la versión de lectura)
    private val _progreso = MutableStateFlow(0f)
    val progreso: StateFlow<Float> = _progreso.asStateFlow()

    // Paso 4: Job para poder cancelar y bandera "corriendo" para los botones
    private var trabajo: Job? = null
    private val _corriendo = MutableStateFlow(false)
    val corriendo: StateFlow<Boolean> = _corriendo.asStateFlow()

    fun tareaLarga() {
        if (_corriendo.value) return // doble inicio: no hace nada
        trabajo = viewModelScope.launch {
            _corriendo.value = true
            try {
                _progreso.value = 0f
                _mensaje.value = "Trabajando..."
                Log.d("HILO", "Antes: ${Thread.currentThread().name}")
                // Mejora IA: el trabajo está en el Repository; aquí solo se recolecta el progreso
                repo.tareaConProgreso().collect { valor ->
                    _progreso.value = valor
                }
                Log.d("HILO", "Despues: ${Thread.currentThread().name}")
                _mensaje.value = "Terminó"
            } finally {
                _corriendo.value = false // termine bien o cancelada
            }
        }
    }

    fun cancelar() {
        trabajo?.cancel()
        _mensaje.value = "Cancelado" // la barra se queda donde estaba
    }

    // ----- Contar primos (paso 5)
    private val _primos = MutableStateFlow<PrimosUi>(PrimosUi.Inicial)
    val primos: StateFlow<PrimosUi> = _primos.asStateFlow()
    private var trabajoPrimos: Job? = null

    fun contar(hasta: Int) {
        if (_primos.value is PrimosUi.Calculando) return
        trabajoPrimos = viewModelScope.launch {
            _primos.value = PrimosUi.Calculando
            val inicio = System.currentTimeMillis()
            try {
                val cantidad = repo.contarPrimos(hasta) // el Repository lo hace en Dispatchers.Default
                val ms = System.currentTimeMillis() - inicio
                _primos.value = PrimosUi.Listo(cantidad, ms)
            } catch (e: CancellationException) {
                _primos.value = PrimosUi.Cancelado
                throw e // se vuelve a lanzar para no romper la cancelación
            }
        }
    }

    fun cancelarPrimos() {
        trabajoPrimos?.cancel()
    }

    // ----- Dos cargas (pasos 6 y 7)
    private val _carga = MutableStateFlow<CargaUi>(CargaUi.Inicial)
    val carga: StateFlow<CargaUi> = _carga.asStateFlow()

    // Paso 7: interruptor "Simular error"
    private val _simularError = MutableStateFlow(false)
    val simularError: StateFlow<Boolean> = _simularError.asStateFlow()

    fun cambiarSimularError(valor: Boolean) {
        _simularError.value = valor
    }

    // Paso 6: primero el perfil (2 s) y después los pedidos (3 s) -> ≈ 5 s
    fun cargarUnaPorUna() {
        ejecutarCarga("Una por una") {
            val perfil = repo.cargarPerfil()
            val pedidos = repo.cargarPedidos(_simularError.value)
            Pair(perfil, pedidos)
        }
    }

    // Paso 6: las dos al mismo tiempo con async -> máximo(2 s, 3 s) ≈ 3 s
    // Paso 7: también la usa el botón Reintentar
    fun cargarALaVez() {
        ejecutarCarga("A la vez") {
            coroutineScope {
                val perfil = async { repo.cargarPerfil() }
                val pedidos = async { repo.cargarPedidos(_simularError.value) }
                Pair(perfil.await(), pedidos.await())
            }
        }
    }

    private fun ejecutarCarga(
        modo: String,
        bloque: suspend () -> Pair<String, List<String>>
    ) {
        if (_carga.value is CargaUi.Cargando) return // no lanza una segunda carga
        viewModelScope.launch {
            _carga.value = CargaUi.Cargando
            val inicio = System.currentTimeMillis()
            try {
                val (perfil, pedidos) = bloque()
                val ms = System.currentTimeMillis() - inicio
                _carga.value = CargaUi.Exito(modo, perfil, pedidos.size, ms)
            } catch (e: CancellationException) {
                throw e // primero deja pasar la cancelación
            } catch (e: Exception) {
                _carga.value = CargaUi.Error("No se pudo cargar: " + e.message)
            }
        }
    }
}
