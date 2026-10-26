package com.espiritu.ayudacampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.espiritu.ayudacampus.data.local.entity.Usuario
import com.espiritu.ayudacampus.data.repository.AyudaRepository
import com.espiritu.ayudacampus.model.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

data class PanelSoporteUiState(
    val conteos: Map<EstadoTicket, Int> = emptyMap(),
    val porEstado: List<DatoGrafico> = emptyList(),
    val porCategoria: List<DatoGrafico> = emptyList(),
    val altasSinAtender: Int = 0,
    val tiempoPromedioMs: Long? = null,
    val cargando: Boolean = true,
    val error: String? = null
) {
    val total get() = conteos.values.sum()
    val tiempoPromedioFormateado get() = formatearTiempoPromedio(tiempoPromedioMs)
}

fun formatearTiempoPromedio(ms: Long?): String {
    if (ms == null || ms < 0) return "Sin datos"
    val totalMinutos = ms / 60_000
    if (totalMinutos < 1) return "< 1 min"
    val horas = totalMinutos / 60
    val minutos = totalMinutos % 60
    val dias = horas / 24
    val horasRestantes = horas % 24

    return when {
        dias > 0 && horasRestantes > 0 -> "$dias d $horasRestantes h"
        dias > 0 -> "$dias d"
        horas > 0 && minutos > 0 -> "$horas h $minutos min"
        horas > 0 -> "$horas h"
        else -> "$minutos min"
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class PanelSoporteViewModel(
    private val repo: AyudaRepository,
    val usuario: Usuario,
    private val ahora: () -> Long = System::currentTimeMillis
) : ViewModel() {
    init { require(usuario.id > 0 && usuario.rol == Rol.ADMIN.name) { "Solo Soporte TI puede acceder al panel." } }
    private val recarga = MutableStateFlow(0)
    val uiState: StateFlow<PanelSoporteUiState> = recarga.flatMapLatest {
        flow {
            emit(PanelSoporteUiState())
            val estados = combine(EstadoTicket.entries.map { repo.observarCantidadPorEstado(it) }) { valores ->
                EstadoTicket.entries.zip(valores.toList()).toMap()
            }
            val alertas = limitesDeAlerta(ahora).distinctUntilChanged()
                .flatMapLatest { repo.observarAltasSinAtender(it) }
            val tiempoPromedio = repo.observarTiempoPromedioResolucion()

            emitAll(combine(estados, repo.observarCantidadPorCategoria(), alertas, tiempoPromedio) { conteos, categorias, altas, avgMs ->
                PanelSoporteUiState(
                    conteos = conteos,
                    porEstado = EstadoTicket.entries.map { DatoGrafico(etiquetaEstado(it), conteos.getValue(it)) },
                    porCategoria = categorias.map { DatoGrafico(it.nombre, it.cantidad) },
                    altasSinAtender = altas,
                    tiempoPromedioMs = avgMs,
                    cargando = false
                )
            })
        }.catch { emit(PanelSoporteUiState(cargando = false, error = "No se pudo cargar el panel de soporte.")) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PanelSoporteUiState())

    val abiertos = contador(EstadoTicket.ABIERTO)
    val enProceso = contador(EstadoTicket.EN_PROCESO)
    val resueltos = contador(EstadoTicket.RESUELTO)
    val cerrados = contador(EstadoTicket.CERRADO)
    val porEstado = uiState.map { it.porEstado }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val porCategoria = uiState.map { it.porCategoria }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val altasSinAtender = uiState.map { it.altasSinAtender }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val tiempoPromedioMs = uiState.map { it.tiempoPromedioMs }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private fun contador(estado: EstadoTicket) = uiState.map { it.conteos[estado] ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    fun reintentar() { recarga.value++ }
}

private fun etiquetaEstado(estado: EstadoTicket) = when (estado) {
    EstadoTicket.ABIERTO -> "Abiertos"
    EstadoTicket.EN_PROCESO -> "En proceso"
    EstadoTicket.RESUELTO -> "Resueltos"
    EstadoTicket.CERRADO -> "Cerrados"
}
