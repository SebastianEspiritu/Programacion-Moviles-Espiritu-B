package com.espiritu.navlab.navigation

sealed class Screen(val route: String) {
    //Pantalla de inicio de sesión
    object Login : Screen("login")

    //Pantalla de inicio
    object Home : Screen("home")

    //Pantalla que muiestra la lista de elementos
    object List : Screen("List")

    //Pantalla del perfil del usuario
    object Profile : Screen("profile")

    //Ruta con argumento {itemId} es el placeholder que Navigation reemplaza
    //con el valor real al momento de navegar
    object Detail : Screen("detail/{itemId}") {
        fun createRoute(itemId: Int): String = "detail/$itemId"
    }
}