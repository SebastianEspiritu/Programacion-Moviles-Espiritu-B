package com.espiritu.campusgps.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.espiritu.campusgps.data.LugaresRepositoryLocal
import com.espiritu.campusgps.data.UbicacionRepositoryGoogle

// El Context se usa aquí para crear el Repository; el ViewModel nunca lo recibe
class CampusViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapaViewModel::class.java)) {
            return MapaViewModel(
                ubicacionRepo = UbicacionRepositoryGoogle(context.applicationContext),
                lugaresRepo = LugaresRepositoryLocal()
            ) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
