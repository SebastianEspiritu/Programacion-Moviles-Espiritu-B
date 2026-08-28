# Lab 02 - Carrito de Compras

**Nombre completo:** Sebastián Espíritu Canteño

## Descripción general

Este laboratorio consiste en implementar un carrito de compras en Kotlin en dos versiones: una primera versión básica (sin IA) con datos fijos en el código, y una segunda versión (con IA) que reestructura el programa usando Programación Orientada a Objetos (POO) y pide los datos de forma interactiva por consola, además de incluir un pequeño asistente que da recomendaciones según el monto de la compra.

---

## Parte sin IA

En esta parte se implementó el carrito de compras de forma funcional, sin POO, con los productos definidos directamente en el código (hardcodeados).

¿Qué hace el programa?

El programa crea una lista de productos con nombre, precio y cantidad ya definidos en el código (no los pide al usuario). Luego busca uno de esos productos por nombre y muestra si lo encontró, elimina otro producto de la lista, y finalmente muestra el detalle completo del carrito (con el listado de productos restantes) junto con el cálculo del subtotal, el IGV (18%), el total a pagar, el producto más caro del carrito, el descuento aplicado según el monto total, y el total final con descuento incluido.

### Funciones implementadas

- `Producto`: data class con nombre, precio y cantidad.
- `calcularSubtotal()`: suma el precio por cantidad de cada producto.
- `calcularIGV()`: calcula el 18% del subtotal.
- `calcularTotal()`: suma subtotal + IGV.
- `calcularDescuento()`: aplica un descuento según el total (10% si supera S/5000, 5% si supera S/3000).
- `mostrarDetalle()`: imprime el detalle de cada producto en el carrito.
- `buscarProducto()`: busca un producto por nombre dentro de la lista (reto adicional).

### Captura de la ejecución (sin IA)
## <img width="455" height="620" alt="Captura de pantalla 2026-08-27 203943" src="https://github.com/user-attachments/assets/04510215-85e0-4aa1-a2cd-b8121941b9ab" />

## Parte con IA (POO)

En esta parte se reestructuró el carrito aplicando Programación Orientada a Objetos, y se agregó entrada de datos interactiva por consola (en vez de datos fijos), además de un asistente que recomienda al cliente según el subtotal de su compra.

### ¿Qué hace el programa
Al ejecutarse, el programa le pregunta al usuario cuántos productos desea ingresar. Por cada producto, pide su nombre, categoría, cantidad y precio, y lo va agregando al carrito. Una vez registrados todos los productos, muestra un resumen completo: el detalle de cada producto con su subtotal individual, el subtotal general del carrito, el IGV (18%), el descuento aplicado (10% si el subtotal supera S/2000), el total final a pagar, y una recomendación generada por un asistente de IA simulado según cuánto lleva gastado el cliente (por ejemplo, sugiriéndole cuánto le falta para el envío gratis o el descuento máximo).

### Funciones y clases implementadas

- `ProductoPOO`: modela cada producto con nombre, precio, cantidad y categoría, y calcula su propio subtotal.
- `AsistenteIA`: recomienda al cliente según el subtotal acumulado (envío gratis, descuento, envío VIP, etc.).
- `CarritoPOO`: encapsula la lista de productos y expone funciones para agregar productos y calcular subtotal, IGV, descuento y total.
- `ejecutarCarritoPOO()`: función interactiva que pide los datos por consola con `Scanner` (nombre, categoría, cantidad, precio) y muestra el resumen final.

### Análisis: val vs var

**¿Por qué `nombre` y `precio` son `val` pero `cantidad` es `var`?**

`nombre` y `precio` se declaran como `val` porque son atributos que no deberían cambiar una vez creado el producto — no tendría sentido que el nombre o el precio de un producto ya agregado al carrito cambien solos. En cambio, `cantidad` se declara como `var` porque sí es razonable que varíe: si el cliente decide llevar más (o menos) unidades del mismo producto, se debe poder actualizar ese valor sin crear un objeto nuevo.

**¿Qué pasaría si intentas cambiar el precio después de crear el producto?**

El código no compilaría. Kotlin lanza el error `Val cannot be reassigned` en tiempo de compilación, porque un `val` es de solo lectura una vez inicializado — es una protección del lenguaje para evitar modificaciones accidentales de datos que deberían permanecer fijos.

### Captura de la ejecución (con IA)
<img width="857" height="789" alt="Captura de pantalla 2026-08-28 013147" src="https://github.com/user-attachments/assets/7ab105d0-6fcf-4391-8bb5-549cbe2335b2" />
