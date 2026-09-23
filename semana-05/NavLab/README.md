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
