package com.espiritu.ayudacampus.viewmodel

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

internal const val HORA_ATENCION_MS = 60L * 60 * 1000

// Room invalida por cambios de tablas, no por el paso del tiempo.
internal fun limitesDeAlerta(ahora: () -> Long) = flow {
    while (currentCoroutineContext().isActive) {
        emit(ahora() - HORA_ATENCION_MS)
        delay(1000)
    }
}
