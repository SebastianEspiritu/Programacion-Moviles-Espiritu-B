package com.espiritu.avisoscampus.viewmodel

import androidx.lifecycle.ViewModel
import com.espiritu.avisoscampus.data.AvisosRepository
import com.espiritu.avisoscampus.model.Aviso
import com.espiritu.avisoscampus.model.PermisoUi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class AvisosViewModel(private val repo: AvisosRepository) : ViewModel() {

    val avisos: StateFlow<List<Aviso>> = repo.avisos

    // Evento de una sola vez: "muestra esta notificación".
    // El ViewModel NO tiene Context: lo recibe la pantalla.
    private val _eventos = MutableSharedFlow<Aviso>()
    val eventos: SharedFlow<Aviso> = _eventos.asSharedFlow()

    private val _permiso = MutableStateFlow<PermisoUi>(PermisoUi.SinPedir)
    val permiso: StateFlow<PermisoUi> = _permiso.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    fun permisoRespondido(concedido: Boolean) {
        _permiso.value = if (concedido) PermisoUi.Concedido else PermisoUi.Denegado
    }
    // TODO Paso 3: fun avisarAhora()
    // TODO Paso 5: faltan, recordarEn(segundos) y cancelarRecordatorio()
    // TODO Paso 6: escuchandoAvion, cambiarEscucha(valor) y alCambiarModoAvion(activo)
    // TODO Paso 8: fun noSePudoMostrar()
}
