package com.espiritu.campusgps.data

import com.espiritu.campusgps.model.Lugar

interface LugaresRepository {
    fun obtenerLugares(): List<Lugar>
}
