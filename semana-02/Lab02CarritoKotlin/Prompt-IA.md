# Prompt utilizado para la versión con IA

**Descripción:** Prompt utilizado para refactorizar el laboratorio de Carrito de Compras a Programación Orientada a Objetos (POO) e integrar el asistente interactivo.

---

### Prompt enviado a la IA:

> **Rol:** Actúa como un Desarrollador Senior en Kotlin especializado en desarrollo Android y arquitectura Clean Code.
>
> **Contexto:** Dispongo de un programa funcional de un carrito de compras básico en Kotlin estructurado de forma procedimental y con datos estáticos (hardcodeados).
>
> **Tarea:** Refactorizar el programa para migrar la arquitectura a Programación Orientada a Objetos (POO) e implementar un flujo interactivo por consola mediante `Scanner`.
>
> **Requerimientos específicos:**
> 1. **Modelado de Clases:** Diseñar la clase `ProductoPOO` aplicando buenas prácticas de inmutabilidad (`val` para atributos que no cambian como nombre/precio y `var` para cantidad).
> 2. **Encapsulamiento de Lógica:** Crear la clase `CarritoPOO` que administre la colección de productos y centralice las funciones financieras (cálculo de subtotal, IGV del 18%, descuentos escalonados y total general).
> 3. **Módulo de Recomendación (IA):** Implementar una clase `AsistenteIA` que analice el monto del subtotal y genere sugerencias dinámicas para el usuario (incentivos de envío gratis, metas para alcanzar el siguiente tramo de descuento, etc.).
> 4. **Interfaz de Consola:** Crear una función ejecutable que capture de forma interactiva la cantidad de productos, sus nombres, precios y categorías, e imprima el desglose detallado final de la compra.
