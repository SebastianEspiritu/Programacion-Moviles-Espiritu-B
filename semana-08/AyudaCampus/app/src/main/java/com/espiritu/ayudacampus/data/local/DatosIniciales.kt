package com.espiritu.ayudacampus.data.local

import com.espiritu.ayudacampus.data.local.entity.Categoria
import com.espiritu.ayudacampus.data.local.entity.Comentario
import com.espiritu.ayudacampus.data.local.entity.Ticket
import com.espiritu.ayudacampus.data.local.entity.Usuario
import androidx.room.withTransaction

object DatosIniciales {

    suspend fun sembrar(db: AyudaDatabase) {

        db.withTransaction {
            val usuarioDao = db.usuarioDao()
            val categoriaDao = db.categoriaDao()
            val ticketDao = db.ticketDao()

            // Los datos se insertan solo la primera vez.
            if (usuarioDao.contar() > 0) return@withTransaction

            // 1. CATEGORÍAS
            val idsCategorias = categoriaDao.insertarTodas(
                listOf(
                    Categoria(nombre = "Hardware", icono = "pc"),
                    Categoria(nombre = "Software", icono = "code"),
                    Categoria(nombre = "Red / Wi-Fi", icono = "wifi"),
                    Categoria(nombre = "Cuenta", icono = "key"),
                    Categoria(nombre = "Impresión", icono = "printer")
                )
            )

            // 2. USUARIOS
            val idsUsuarios = usuarioDao.insertarTodos(
                listOf(
                    Usuario(
                        nombre = "Juan Pérez",
                        correo = "juan@campus.test",
                        clave = "1234",
                        rol = "CLIENTE"
                    ),
                    Usuario(
                        nombre = "Soporte Técnico",
                        correo = "soporte@campus.test",
                        clave = "admin",
                        rol = "ADMIN"
                    )
                )
            )

            val clienteId = idsUsuarios[0].toInt()
            val soporteId = idsUsuarios[1].toInt()

            // 3. FECHAS DE EJEMPLO
            val ahora = System.currentTimeMillis()
            val hora = 60L * 60L * 1000L

            // 4. TICKETS DE EJEMPLO
            val ejemplos = listOf(
                Ticket(
                    clienteId = clienteId,
                    categoriaId = idsCategorias[0].toInt(),
                    titulo = "Computadora no enciende",
                    descripcion = "La computadora del laboratorio no enciende.",
                    ubicacion = "Laboratorio 201",
                    prioridad = "ALTA",
                    estado = "ABIERTO",
                    fecha = ahora - 3 * hora
                ),
                Ticket(
                    clienteId = clienteId,
                    categoriaId = idsCategorias[1].toInt(),
                    titulo = "Error al iniciar programa",
                    descripcion = "El programa presenta errores al iniciar.",
                    ubicacion = "Laboratorio 202",
                    prioridad = "MEDIA",
                    estado = "EN_PROCESO",
                    fecha = ahora - 5 * hora
                ),
                Ticket(
                    clienteId = clienteId,
                    categoriaId = idsCategorias[2].toInt(),
                    titulo = "Sin conexión a Wi-Fi",
                    descripcion = "No puedo conectarme a la red del campus.",
                    ubicacion = "Biblioteca",
                    prioridad = "ALTA",
                    estado = "RESUELTO",
                    fecha = ahora - 8 * hora
                ),
                Ticket(
                    clienteId = clienteId,
                    categoriaId = idsCategorias[3].toInt(),
                    titulo = "Problema con contraseña",
                    descripcion = "Necesito ayuda para recuperar mi contraseña.",
                    ubicacion = "Aula 105",
                    prioridad = "BAJA",
                    estado = "CERRADO",
                    fecha = ahora - 24 * hora,
                    fechaCierre = ahora - 12 * hora,
                    calificacion = 5
                ),
                Ticket(
                    clienteId = clienteId,
                    categoriaId = idsCategorias[4].toInt(),
                    titulo = "Impresora sin papel",
                    descripcion = "La impresora no permite imprimir documentos.",
                    ubicacion = "Biblioteca",
                    prioridad = "BAJA",
                    estado = "ABIERTO",
                    fecha = ahora - hora
                ),
                Ticket(
                    clienteId = clienteId,
                    categoriaId = idsCategorias[0].toInt(),
                    titulo = "Pantalla con fallas",
                    descripcion = "El monitor presenta líneas en la pantalla.",
                    ubicacion = "Laboratorio 203",
                    prioridad = "MEDIA",
                    estado = "EN_PROCESO",
                    fecha = ahora - 2 * hora
                )
            )

            // Guardamos los ID reales de los tickets.
            val idsTickets = ejemplos.map { ticket ->
                ticketDao.insertarTicket(ticket).toInt()
            }

            // 5. COMENTARIOS DE EJEMPLO
            val comentarios = listOf(
                Comentario(
                    ticketId = idsTickets[0],
                    autorId = clienteId,
                    autorNombre = "Juan Pérez",
                    autorRol = "CLIENTE",
                    texto = "La computadora dejó de funcionar esta mañana."
                ),
                Comentario(
                    ticketId = idsTickets[1],
                    autorId = clienteId,
                    autorNombre = "Juan Pérez",
                    autorRol = "CLIENTE",
                    texto = "El programa muestra un mensaje de error."
                ),
                Comentario(
                    ticketId = idsTickets[2],
                    autorId = clienteId,
                    autorNombre = "Juan Pérez",
                    autorRol = "CLIENTE",
                    texto = "No puedo conectarme desde mi laptop."
                ),
                Comentario(
                    ticketId = idsTickets[2],
                    autorId = soporteId,
                    autorNombre = "Soporte Técnico",
                    autorRol = "ADMIN",
                    texto = "La conexión fue restablecida."
                )
            )

            comentarios.forEach { comentario ->
                ticketDao.insertarComentario(comentario)
            }
        }
    }
}
