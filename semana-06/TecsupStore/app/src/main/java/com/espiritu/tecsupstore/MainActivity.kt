package com.espiritu.tecsupstore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.espiritu.tecsupstore.ui.theme.TecsupStoreTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TecsupStoreTheme {
                val productosEjemplo = listOf(
                    Producto(1, "Audífonos", 89.0),
                    Producto(2, "Smartwatch", 199.0),
                    Producto(3, "Funda celular", 25.0)
                )

                Scaffold { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        productosEjemplo.forEach { producto ->
                            TarjetaProducto(producto = producto)
                        }
                    }
                }
            }
        }
    }
}