package com.espiritu.ayudacampus.ui.cliente

import java.io.File
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Test
import org.w3c.dom.Element

class TicketsAbiertosResourceTest {
    @Test fun recursoIncluyeSingularYPluralConCantidadFormateada() {
        val archivo = listOf(File("src/main/res/values/strings.xml"), File("app/src/main/res/values/strings.xml"))
            .first { it.isFile }
        val documento = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(archivo)
        val plurales = documento.getElementsByTagName("plurals")
        val recurso = (0 until plurales.length).map { plurales.item(it) as Element }
            .single { it.getAttribute("name") == "tickets_abiertos" }
        val elementos = recurso.getElementsByTagName("item")
        val textos = (0 until elementos.length).associate { indice ->
            val item = elementos.item(indice) as Element
            item.getAttribute("quantity") to item.textContent
        }
        // Comprueba los textos reales; la selección por idioma la realiza Android.
        listOf(
            Triple(0, "other", "Tienes 0 tickets abiertos"),
            Triple(1, "one", "Tienes 1 ticket abierto"),
            Triple(2, "other", "Tienes 2 tickets abiertos")
        ).forEach { (cantidad, categoria, esperado) ->
            assertEquals(esperado, String.format(Locale.forLanguageTag("es"), textos.getValue(categoria), cantidad))
        }
    }
}
