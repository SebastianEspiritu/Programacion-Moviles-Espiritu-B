package com.espiritu.ayudacampus.data.prefs

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class PreferenciasStoreTest {
    @get:Rule val temporales = TemporaryFolder()

    @Test fun valoresInicialesSinSesion() = runTest {
        val archivo = temporales.root.resolve("ajustes.preferences_pb")
        val job = SupervisorJob()
        val prefs = PreferenciasStore(PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job)) { archivo })
        try {
            assertEquals(SesionGuardada("", 0), prefs.sesion.first())
            assertFalse(prefs.modoOscuro.first())
            assertTrue(prefs.notificaciones.first())
            assertTrue(prefs.recibirTickets.first())
        } finally { job.cancelAndJoin() }
    }

    @Test fun ajustesPersistenAlCerrarYReabrirElArchivoReal() = runTest {
        val archivo = temporales.root.resolve("ajustes.preferences_pb")
        var job = SupervisorJob()
        var prefs = PreferenciasStore(PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job)) { archivo })
        try {
            prefs.guardarSesion("ADMIN", 2)
            prefs.guardarModoOscuro(true)
            prefs.guardarNotificaciones(false)
            prefs.guardarRecibirTickets(false)
        } finally { job.cancelAndJoin() }
        job = SupervisorJob()
        prefs = PreferenciasStore(PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job)) { archivo })
        try {
            assertEquals(SesionGuardada("ADMIN", 2), prefs.sesion.first())
            assertTrue(prefs.modoOscuro.first())
            assertFalse(prefs.notificaciones.first())
            assertFalse(prefs.recibirTickets.first())
        } finally { job.cancelAndJoin() }
    }

    @Test fun cerrarSesionBorraSoloIdentidadYConservaPreferenciasEnDisco() = runTest {
        val archivo = temporales.root.resolve("ajustes.preferences_pb")
        var job = SupervisorJob()
        var prefs = PreferenciasStore(PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job)) { archivo })
        try {
            prefs.guardarSesion("CLIENTE", 1)
            prefs.guardarModoOscuro(true)
            prefs.guardarNotificaciones(false)
            prefs.guardarRecibirTickets(false)
            prefs.cerrarSesion()
        } finally { job.cancelAndJoin() }
        job = SupervisorJob()
        prefs = PreferenciasStore(PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job)) { archivo })
        try {
            assertEquals(SesionGuardada("", 0), prefs.sesion.first())
            assertTrue(prefs.modoOscuro.first())
            assertFalse(prefs.notificaciones.first())
            assertFalse(prefs.recibirTickets.first())
        } finally { job.cancelAndJoin() }
    }
}
