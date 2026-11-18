package com.espiritu.centrotareas.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.IOException

class TareasRepositoryLocal : TareasRepository {

    override suspend fun cargarPerfil(): String {
        delay(2000)
        return "Ana Torres (ficticia)"
    }

    override suspend fun cargarPedidos(simularError: Boolean): List<String> {
        delay(3000)
        if (simularError) throw IOException("Sin conexión")
        return listOf("Pedido 101", "Pedido 102", "Pedido 103")
    }

    // Mejora IA: 100 vueltas de 40 ms (≈ 4 s). flowOn mueve la emisión a Dispatchers.Default;
    // quien recolecta (el ViewModel) sigue en el hilo principal.
    override fun tareaConProgreso(): Flow<Float> = flow {
        Log.d("HILO", "Dentro: ${Thread.currentThread().name}")
        for (i in 1..100) {
            delay(40) // delay revisa la cancelación
            emit(i / 100f)
        }
    }.flowOn(Dispatchers.Default)

    // Paso 5: cálculo pesado en Dispatchers.Default, cancelable a mitad del ciclo
    override suspend fun contarPrimos(hasta: Int): Int = withContext(Dispatchers.Default) {
        var cantidad = 0
        for (n in 2..hasta) {
            ensureActive()
            if (esPrimo(n)) cantidad++
        }
        cantidad
    }

    // Prueba dividir entre 2 y la raíz de n (d * d <= n en Long para evitar desbordes)
    private fun esPrimo(n: Int): Boolean {
        if (n < 2) return false
        var d = 2
        while (d.toLong() * d <= n) {
            if (n % d == 0) return false
            d++
        }
        return true
    }
}
