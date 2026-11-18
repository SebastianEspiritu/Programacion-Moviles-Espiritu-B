package com.espiritu.campusgps.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.espiritu.campusgps.data.LugaresRepository
import com.espiritu.campusgps.data.UbicacionNoDisponibleException
import com.espiritu.campusgps.data.UbicacionRepository
import com.espiritu.campusgps.model.Lugar
import com.espiritu.campusgps.model.LugarConDistancia
import com.espiritu.campusgps.model.Posicion
import com.espiritu.campusgps.model.TipoError
import com.espiritu.campusgps.model.UbicacionUi
import com.espiritu.campusgps.util.distanciaMetros
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MapaViewModel(
    private val ubicacionRepo: UbicacionRepository,
    private val lugaresRepo: LugaresRepository
) : ViewModel() {

    private val _estado = MutableStateFlow<UbicacionUi>(UbicacionUi.Inicial)
    val estado: StateFlow<UbicacionUi> = _estado.asStateFlow()

    private val _lugares = MutableStateFlow(
        lugaresRepo.obtenerLugares().map { LugarConDistancia(it, null) }
    )

    // Mejora IA: filtro por categoría (null = todas)
    val categorias: List<String> = lugaresRepo.obtenerLugares().map { it.categoria }.distinct()

    private val _categoria = MutableStateFlow<String?>(null)
    val categoria: StateFlow<String?> = _categoria.asStateFlow()

    // La pantalla recibe solo los lugares de la categoría elegida (ya ordenados por distancia)
    val lugares: StateFlow<List<LugarConDistancia>> = combine(_lugares, _categoria) { lista, cat ->
        if (cat == null) lista else lista.filter { it.lugar.categoria == cat }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, _lugares.value)

    private val _seleccionado = MutableStateFlow<Int?>(null)
    val seleccionado: StateFlow<Int?> = _seleccionado.asStateFlow()

    // El más cercano se deriva de la lista: el primero con distancia calculada (null antes de buscar)
    val masCercano: StateFlow<Lugar?> = lugares
        .map { lista -> lista.firstOrNull { it.metros != null }?.lugar }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun buscar() {
        // Sin Google Play Services no se puede usar la ubicación de Google
        if (!ubicacionRepo.playServicesDisponible()) {
            _estado.value = UbicacionUi.Error(
                "Este dispositivo no tiene Google Play Services",
                TipoError.SIN_PLAY_SERVICES
            )
            return
        }
        _estado.value = UbicacionUi.Buscando
        viewModelScope.launch {
            ubicacionRepo.obtenerPosicion()
                .onSuccess {
                    _estado.value = UbicacionUi.Lista(it)
                    actualizarDistancias(it)
                }
                .onFailure { _estado.value = errorDe(it) }
        }
    }

    fun permisoDenegado() {
        _estado.value = UbicacionUi.Error(
            "Sin permiso de ubicación no puedo mostrar dónde estás",
            TipoError.SIN_PERMISO
        )
    }

    // Calcula la distancia a cada lugar, ordena de menor a mayor y publica la lista
    private fun actualizarDistancias(pos: Posicion) {
        val ordenados = lugaresRepo.obtenerLugares()
            .map { LugarConDistancia(it, distanciaMetros(pos, it)) }
            .sortedBy { it.metros }
        _lugares.value = ordenados
        ordenados.forEach { Log.d("CampusGPS", "${it.lugar.nombre}: ${it.metros} m") }
    }

    // Mejora IA: radio de cercanía en metros (null = sin radio)
    val radiosDisponibles: List<Int> = listOf(100, 200, 500)

    private val _radio = MutableStateFlow<Int?>(null)
    val radio: StateFlow<Int?> = _radio.asStateFlow()

    // Ids de los lugares cuya distancia está dentro del radio elegido
    val dentroDelRadio: StateFlow<Set<Int>> = combine(lugares, _radio) { lista, r ->
        if (r == null) emptySet()
        else lista.filter { it.metros != null && it.metros <= r }.map { it.lugar.id }.toSet()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun cambiarRadio(metros: Int?) {
        _radio.value = metros
    }

    // Cambia la categoría; si el lugar seleccionado queda oculto, se quita la selección
    fun filtrar(categoria: String?) {
        _categoria.value = categoria
        val seleccionadoVisible = _lugares.value.any {
            it.lugar.id == _seleccionado.value &&
                (categoria == null || it.lugar.categoria == categoria)
        }
        if (!seleccionadoVisible) _seleccionado.value = null
    }

    // El usuario tocó una tarjeta (o null para quitar la selección)
    fun seleccionar(id: Int?) {
        _seleccionado.value = id
    }

    // Convierte la excepción del Repository en un error que la pantalla entiende
    private fun errorDe(e: Throwable): UbicacionUi.Error = when (e) {
        is SecurityException -> UbicacionUi.Error(
            "Sin permiso de ubicación no puedo mostrar dónde estás",
            TipoError.SIN_PERMISO
        )
        is UbicacionNoDisponibleException -> UbicacionUi.Error(
            "No se pudo obtener la ubicación. Activa el GPS e intenta de nuevo",
            TipoError.GPS_APAGADO
        )
        else -> UbicacionUi.Error(
            e.message ?: "No se pudo obtener la ubicación",
            TipoError.OTRO
        )
    }
}
