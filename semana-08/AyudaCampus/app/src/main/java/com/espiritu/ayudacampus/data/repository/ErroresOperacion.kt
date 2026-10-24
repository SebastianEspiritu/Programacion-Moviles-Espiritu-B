package com.espiritu.ayudacampus.data.repository

class CategoriaDuplicadaException : IllegalArgumentException("Ya existe una categoría con ese nombre.")
class CategoriaConTicketsException : IllegalStateException("No se puede eliminar una categoría con tickets asociados.")
class CategoriaNoExisteException : IllegalStateException("La categoría ya no existe.")
class TicketCerradoException : IllegalStateException("El ticket está cerrado y no admite modificaciones.")
