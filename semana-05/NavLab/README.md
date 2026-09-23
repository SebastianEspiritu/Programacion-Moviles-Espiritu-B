## Sebastián Espíritu Canteño C24-B

# Portal Académico Tecsup - Jetpack Compose Navigation

Aplicación móvil desarrollada en **Android con Jetpack Compose** que implementa una arquitectura de navegación moderna utilizando **Navigation Compose** y componentes visuales de **Material3**. La app simula la interfaz de un Portal Académico Universitario para la gestión de expedientes y consulta de directorio estudiantil.

---

## Explicación de la Estructura del Código

La aplicación sigue una arquitectura limpia basada en separación de responsabilidades y rutas centralizadas:

* **`MainActivity.kt`**: Punto de entrada de la aplicación. Configura la llamada principal al tema de la app (`NavLabTheme`) y ejecuta el grafo de navegación `AppNavigation()`.
  
* **`Screen.kt`**: Define una `sealed class` que centraliza de manera segura (Type-Safe) todas las rutas de la aplicación (`Login`, `Home`, `List`, `Detail/{itemId}` y `Profile`), incluyendo el método auxiliar `createRoute(itemId)` para la transferencia de argumentos.
  
* **`AppNavigation.kt`**: Configura el contenedor `NavHost` y gestiona las transiciones entre pantallas a través de un `NavController`, definiendo `Login` como la pantalla inicial.
  
* **`LoginScreen.kt`** (agregado para la mejora): Pantalla de autenticación con formulario de ingreso (usuario y contraseña) integrado en una tarjeta elevada Material3 sobre fondo degradado morado.
  
* **`HomeScreen.kt`**: Dashboard principal tipo "Portal Académico" que presenta opciones en forma de tarjetas elevadas (`AcademicMenuCard`) para navegar hacia el directorio o el perfil del usuario.
  
* **`ListScreen.kt`**: Implementa una `LazyColumn` que renderiza la lista de estudiantes con avatares de iniciales circulares, enviando el `itemId` seleccionado hacia la pantalla de detalle.
  
* **`DetailScreen.kt`**: Recibe y procesa el argumento tipo `Int` (`itemId`) enviado desde el `NavHost` para construir el expediente académico con información estructurada del alumno.
  
* **`ProfileScreen.kt`**: Muestra la información personal y de matrícula del usuario agrupada en categorías, e incluye una `TopAppBar` con navegación de retorno y un botón para cerrar sesión.

---

## Requerimientos Funcionales

1. **Inicio de sesión**: Al abrir la aplicación, el usuario ve una pantalla de login donde puede ingresar sus credenciales para acceder al portal académico.
   
2. **Navegación principal**: Desde la pantalla de bienvenida (Home), el usuario puede elegir mediante tarjetas si desea ir al "Directorio de Alumnos" o a su "Perfil Académico".
   
3. **Visualización de detalles**: Si el usuario selecciona a un estudiante de la lista en el directorio, la aplicación abre su expediente académico mostrando los datos y el ID recibido de ese estudiante.
   
4. **Navegación de retorno**: Si el usuario presiona la flecha de regreso en la barra superior (`TopAppBar`), la aplicación lo devuelve a la pantalla anterior.
   
5. **Cierre de sesión**: Si el usuario presiona el botón "Cerrar Sesión" en su perfil, la app cierra la sesión y lo regresa automáticamente a la pantalla de inicio de sesión.
---

## Dependencias Agregadas

En el archivo `build.gradle.kts` (Módulo `:app`) se integraron las siguientes dependencias clave:

```kotlin
dependencies {
    // Navegación con Jetpack Compose
    implementation("androidx.navigation:navigation-compose:2.8.0") // O versión equivalente utilizada

    // Iconos extendidos de Material Design (ChevronRight, Badge, School, ExitToApp, etc.)
    implementation("androidx.compose.material:material-icons-extended")

    // Componentes de diseño Material 3
    implementation("androidx.compose.material3:material3")
}
```

## PROMPT UTILIZADO PARA LA MEJORA:
Tengo una app en Jetpack Compose con navegación (Navigation Compose) ya funcionando, con esta estructura:

- Screen.kt: sealed class con las rutas (Home, List, Detail/{itemId}, Profile)
- AppNavigation.kt: NavHost con los composables
- MainActivity.kt
- HomeScreen.kt: pantalla "Bienvenido, [Nombre]" con dos tarjetas/opciones: "Directorio de Alumnos" y "Mi Perfil Académico"
- ListScreen.kt: Directorio de Alumnos con avatares, cada uno navega a Detail pasando su id como Int
- DetailScreen.kt: Expediente Académico, muestra los datos del alumno recibido desde el NavHost
- ProfileScreen.kt: "Configuración de Perfil" con secciones de información personal y académica, y un botón al final

Necesito dos cambios puntuales, manteniendo intacta la lógica de navegación que ya tengo (rutas, argumentos, NavHost):

1. Agregar una pantalla de Login como punto de partida de la app:
   - Crear LoginScreen.kt con campo de correo institucional, campo de contraseña y botón "Iniciar Sesión"
   - Agregar la ruta Login en Screen.kt
   - Cambiar el startDestination del NavHost en AppNavigation.kt de Home a Login
   - Al presionar "Iniciar Sesión" (no hace falta validar contra nada real, puede ser solo navegación), debe navegar a Home ("Bienvenido, [Nombre]") usando navController.navigate(Screen.Home.route) { popUpTo(Screen.Login.route) { inclusive = true } } para que no se pueda volver al login con el botón atrás

2. Corregir ProfileScreen.kt ("Configuración de Perfil"):
   - Agregar un TopAppBar con flecha de regreso (ícono ArrowBack) que use navController.popBackStack() para volver a Home (donde están las opciones "Directorio de Alumnos" y "Mi Perfil Académico")
   - Eliminar el botón "Ir al Inicio" que tenía antes
   - Dejar solo un botón que diga exactamente "Cerrar Sesión", ubicado abajo como estaba, pero que ahora navegue de vuelta a Login usando navController.navigate(Screen.Login.route) { popUpTo(0) } para limpiar todo el back stack

Dame el código completo de los archivos que cambian: Screen.kt, AppNavigation.kt, LoginScreen.kt (nuevo) y ProfileScreen.kt actualizado, manteniendo el mismo estilo visual morado/Material3 que ya usamos. Comenta brevemente qué cambiaste en cada uno.
