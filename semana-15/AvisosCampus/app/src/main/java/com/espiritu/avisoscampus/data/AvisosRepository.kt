package com.espiritu.avisoscampus.data

import com.espiritu.avisoscampus.model.Aviso
import com.espiritu.avisoscampus.model.Origen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

interface AvisosRepository {
    val avisos: StateFlow<List<Aviso>>

    // suspend: guardar en disco (DataStore) no debe bloquear el hilo principal
    suspend fun registrar(titulo: String, texto: String, origen: Origen): Aviso
}

// Versión en memoria de la Fase 1: se sigue usando para pruebas, pero se borra al cerrar la app
class AvisosRepositoryMemoria : AvisosRepository {
    private val _avisos = MutableStateFlow<List<Aviso>>(emptyList())
    override val avisos: StateFlow<List<Aviso>> = _avisos.asStateFlow()
    private var siguiente = 1

    override suspend fun registrar(titulo: String, texto: String, origen: Origen): Aviso {
        val hora = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val aviso = Aviso(siguiente++, titulo, texto, origen, hora)
        _avisos.value = listOf(aviso) + _avisos.value      // lo más reciente arriba
        return aviso
    }
}
