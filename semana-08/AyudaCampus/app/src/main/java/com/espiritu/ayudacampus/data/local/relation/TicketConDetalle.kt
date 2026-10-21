package com.espiritu.ayudacampus.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.espiritu.ayudacampus.data.local.entity.Categoria
import com.espiritu.ayudacampus.data.local.entity.Comentario
import com.espiritu.ayudacampus.data.local.entity.Ticket
import com.espiritu.ayudacampus.data.local.entity.Usuario

data class TicketConDetalle(
    @Embedded
    val ticket: Ticket,

    @Relation(
        parentColumn = "id",
        entityColumn = "ticketId"
    )
    val comentarios: List<Comentario>,

    @Relation(
        parentColumn = "categoriaId",
        entityColumn = "id"
    )
    val categoria: Categoria,

    @Relation(
        parentColumn = "clienteId",
        entityColumn = "id"
    )
    val cliente: Usuario
)
