package com.espiritu.avisoscampus.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// Escucha el anuncio del sistema cuando se activa o desactiva el modo avión.
// onReceive es corto: solo lee el dato y se lo pasa a quien lo registró.
class ModoAvionReceiver(
    private val alCambiar: (Boolean) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_AIRPLANE_MODE_CHANGED) {
            alCambiar(intent.getBooleanExtra("state", false))
        }
    }
}
