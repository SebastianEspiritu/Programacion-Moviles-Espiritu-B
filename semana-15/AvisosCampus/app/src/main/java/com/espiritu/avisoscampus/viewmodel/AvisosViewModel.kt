package com.espiritu.avisoscampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.espiritu.avisoscampus.data.AvisosRepository
import com.espiritu.avisoscampus.model.Aviso
import com.espiritu.avisoscampus.model.Canal
import com.espiritu.avisoscampus.model.EventoAviso
import com.espiritu.avisoscampus.model.Origen
import com.espiritu.avisoscampus.model.PermisoUi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AvisosViewModel(private val repo: AvisosRepository) : ViewModel() {

    val avisos: StateFlow<List<Aviso>> = repo.avisos

    // Evento de una sola vez: "muestra esta notificación".
    // El ViewModel NO tiene Context: lo recibe la pantalla.
    // Lleva también el canal: el ViewModel decide si el aviso es urgente
    private val _eventos = MutableSharedFlow<EventoAviso>()
    val eventos: SharedFlow<EventoAviso> = _eventos.asSharedFlow()

    private val _permiso = MutableStateFlow<PermisoUi>(PermisoUi.SinPedir)
    val permiso: StateFlow<PermisoUi> = _permiso.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    // Recordatorio: segundos que faltan, o null si no hay uno activo
    private var trabajo: Job? = null
    private val _faltan = MutableStateFlow<Int?>(null)
    val faltan: StateFlow<Int?> = _faltan.asStateFlow()

    // Receptor del modo avión: apagado al iniciar
    private val _escuchandoAvion = MutableStateFlow(false)
    val escuchandoAvion: StateFlow<Boolean> = _escuchandoAvion.asStateFlow()

    fun permisoRespondido(concedido: Boolean) {
        _permiso.value = if (concedido) PermisoUi.Concedido else PermisoUi.Denegado
        // Si ya hay permiso, el mensaje de error se quita solo
        if (concedido) _mensaje.value = null
    }

    fun avisarAhora() {
        viewModelScope.launch {
            val aviso = repo.registrar("Tarea lista", "Tu entrega fue registrada", Origen.BOTON)
            _eventos.emit(EventoAviso(aviso))
        }
    }

    fun recordarEn(segundos: Int = 10) {
        if (trabajo?.isActive == true) return          // ya hay uno corriendo
        trabajo = viewModelScope.launch {
            try {
                for (s in segundos downTo 1) {
                    _faltan.value = s
                    delay(1000)
                }
                val aviso = repo.registrar(
                    "Recordatorio",
                    "Revisa tu laboratorio pendiente",
                    Origen.RECORDATORIO
                )
                _eventos.emit(EventoAviso(aviso, Canal.URGENTES))   // sale emergente
            } finally {
                _faltan.value = null                    // termine bien o cancelado
            }
        }
    }

    fun cancelarRecordatorio() {
        trabajo?.cancel()                               // no se emite ningún aviso
    }

    fun cambiarEscucha(valor: Boolean) {
        _escuchandoAvion.value = valor
    }

    fun alCambiarModoAvion(activo: Boolean) {
        viewModelScope.launch {
            val texto = if (activo) "Activado" else "Desactivado"
            val aviso = repo.registrar("Modo avión", texto, Origen.MODO_AVION)
            _eventos.emit(EventoAviso(aviso))
        }
    }

    fun noSePudoMostrar() {
        _mensaje.value = "Sin permiso: el aviso se guardó en el historial, pero no se mostró"
    }
}
