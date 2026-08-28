package com.espiritu.lab02carritokotlin

import java.util.Scanner

// 1. Modelo de datos con POO
data class ProductoPOO(
    val nombre: String,
    val precio: Double,
    var cantidad: Int,
    val categoria: String
) {
    fun calcularSubtotal(): Double = precio * cantidad
}

// 2. Módulo de Inteligencia Artificial (Recomendador)
class AsistenteIA {
    fun recomendarProducto(subtotal: Double): String {
        return when {
            subtotal < 500.0 -> "[IA Prompt]: Agrega productos por S/ ${String.format("%.2f", 500.0 - subtotal)} mas para obtener envio gratis."
            subtotal in 500.0..2000.0 -> "[IA Prompt]: Estas cerca de un super descuento! Supera los S/ 2000 para obtener un 10% OFF."
            else -> "[IA Prompt]: Felicidades! Calificas para el descuento maximo del 10% y envio VIP gratis."
        }
    }
}

// 3. Clase Carrito con encapsulamiento
class CarritoPOO {
    val nombreCliente = "Sebastián Espíritu"
    val productos = mutableListOf<ProductoPOO>()
    private val ia = AsistenteIA()

    fun agregarProducto(producto: ProductoPOO) {
        productos.add(producto)
        println("--> Producto '${producto.nombre}' agregado con exito.")
    }

    fun calcularSubtotal(): Double = productos.sumOf { it.calcularSubtotal() }

    fun calcularIGV(): Double = calcularSubtotal() * 0.18

    fun calcularDescuento(porcentaje: Double = 10.0): Double {
        val subtotal = calcularSubtotal()
        return if (subtotal > 2000.0) subtotal * (porcentaje / 100.0) else 0.0
    }

    fun calcularTotal(): Double = (calcularSubtotal() + calcularIGV()) - calcularDescuento()

    fun mostrarResumen() {
        println()
        println("=========================================")
        println("    CARRITO DE COMPRAS CON IA - TECSUP   ")
        println("=========================================")
        println("Cliente: $nombreCliente")
        println()

        println("--------- DETALLE DEL CARRITO ---------")
        productos.forEach { p ->
            println(String.format("[%s] %-16s x%d S/ %8.2f", p.categoria, p.nombre, p.cantidad, p.calcularSubtotal()))
        }
        println("---------------------------------------")

        val subtotal = calcularSubtotal()
        val igv = calcularIGV()
        val descuento = calcularDescuento()
        val total = calcularTotal()

        println(String.format("%-23s: S/ %8.2f", "Subtotal", subtotal))
        println(String.format("%-23s: S/ %8.2f", "IGV (18%)", igv))
        println(String.format("%-23s: S/ %8.2f", "Descuento (10%)", descuento))
        println(String.format("%-23s: S/ %8.2f", "TOTAL FINAL", total))
        println("---------------------------------------")
        println(ia.recomendarProducto(subtotal))
        println("=========================================\n")
    }
}

// 4. Función Interactiva que pide datos por consola
fun ejecutarCarritoPOO() {
    val scanner = Scanner(System.`in`)
    val miCarrito = CarritoPOO()

    println("=========================================")
    println("      SISTEMA DE VENTAS - TECSUP      ")
    println("=========================================")

    print("¿Cuantos productos desea ingresar al carrito?: ")
    val cantidadProductos = scanner.nextInt()
    scanner.nextLine() // Limpiar buffer

    for (i in 1..cantidadProductos) {
        println("\n--- Registro del Producto #$i ---")
        print("Nombre del producto: ")
        val nombre = scanner.nextLine()

        print("Precio (S/): ")
        val precio = scanner.nextDouble()

        print("Cantidad: ")
        val cantidad = scanner.nextInt()
        scanner.nextLine() // Limpiar buffer

        print("Categoria: ")
        val categoria = scanner.nextLine()

        miCarrito.agregarProducto(ProductoPOO(nombre, precio, cantidad, categoria))
    }

    miCarrito.mostrarResumen()
}

// 5. Punto de entrada para ejecución directa interactiva
fun main() {
    ejecutarCarritoPOO()
}