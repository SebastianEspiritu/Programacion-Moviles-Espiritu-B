
package com.espiritu.ayudacampus.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.espiritu.ayudacampus.data.local.dao.CategoriaDao
import com.espiritu.ayudacampus.data.local.dao.TicketDao
import com.espiritu.ayudacampus.data.local.dao.UsuarioDao
import com.espiritu.ayudacampus.data.local.entity.Categoria
import com.espiritu.ayudacampus.data.local.entity.Comentario
import com.espiritu.ayudacampus.data.local.entity.Ticket
import com.espiritu.ayudacampus.data.local.entity.Usuario

@Database(
    entities = [
        Categoria::class,
        Usuario::class,
        Ticket::class,
        Comentario::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AyudaDatabase : RoomDatabase() {

    abstract fun categoriaDao(): CategoriaDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun ticketDao(): TicketDao

    companion object {

        @Volatile
        private var INSTANCE: AyudaDatabase? = null

        fun obtener(context: Context): AyudaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instancia = Room.databaseBuilder(
                    context.applicationContext,
                    AyudaDatabase::class.java,
                    "ayudacampus.db"
                ).build()

                INSTANCE = instancia
                instancia
            }
        }
    }
}
