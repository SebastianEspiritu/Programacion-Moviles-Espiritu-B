# Mi Carrito TECSUP - Jetpack Compose

**Alumno:** Sebastián Espíritu Canteño  
**Curso:** Desarrollo de Aplicaciones Móviles  
**Laboratorio:** Lab 04 - Carrito de Compras en Jetpack Compose

---

## Descripción del Proyecto
Aplicación móvil desarrollada en Kotlin y Jetpack Compose que gestiona un carrito de compras dinámico. Permite agregar productos con su precio y cantidad, calcular automáticamente el Subtotal, IGV y Total, eliminar items individuales con un diálogo de confirmación y aplicar descuentos escalonados (5% y 10%) según el monto acumulado.

---

## Capturas de Pantalla

| Carrito Vacío | Carrito con Productos y Totales |
| :---: | :---: |
| <img width="334" height="668" alt="vacio" src="https://github.com/user-attachments/assets/026f9bb0-3f7c-473e-b98e-befbb037acb3" /> | <img width="455" height="966" alt="lleno" src="https://github.com/user-attachments/assets/04a5f578-49e9-474d-becb-ad7f62bde3fb" />|

---

## Preguntas Conceptuales

### (a) ¿Por qué `mutableStateListOf` y no una `MutableList` normal?
`mutableStateListOf` es una estructura de datos observable integrada con el sistema de estado de Jetpack Compose. Cuando se agregan, eliminan o modifican elementos dentro de esta lista, Compose detecta el cambio automáticamente y desencadena la recomposición de la interfaz de usuario. Una `MutableList` común de Kotlin no notifica a Compose sobre los cambios internos, por lo que la pantalla no se actualizaría.

### (b) ¿Por qué la lista es `val`?
Se declara como `val` porque la **referencia** en memoria a la lista reactiva nunca cambia durante el ciclo de vida de la pantalla. Lo que se modifica internamente son sus elementos (su contenido), no la instancia de la lista en sí. Usar `val` junto con `remember` garantiza que Compose mantenga siempre la misma referencia activa entre recomposiciones.

### (c) ¿Qué hace `weight(1f)` en la `LazyColumn` (o en el `Box`)?
Dentro de una `Column`, el modificador `weight(1f)` le indica al componente que ocupe todo el espacio vertical restante disponible. Esto permite que el formulario quede fijo arriba, la `LazyColumn` (o el mensaje de estado vacío) se expanda dinámicamente al centro, y el panel de totales quede empujado y fijo en la parte inferior de la pantalla sin solaparse.
