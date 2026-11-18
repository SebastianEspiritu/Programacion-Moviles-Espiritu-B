# CentroTareas

App de la Semana 14 (Hilos en Android) que hace tres tareas que tardan: una tarea con barra de progreso, contar números primos y dos cargas de datos simuladas, sin congelar nunca la pantalla gracias a las corrutinas.

## Observación del Paso 1

**¿Qué pasa con la pantalla?** Al pulsar **Tarea larga (MAL)** la app se congela durante 4 segundos: el botón **Tócame** deja de sumar, los botones no reaccionan al toque y nada se mueve. Cuando pasan los 4 segundos la app "despierta" de golpe, procesa los toques que quedaron en cola y el texto cambia directamente a **Terminó**.

**¿Por qué no se ve "Trabajando..."?** Porque `Thread.sleep(4000)` se ejecuta en el **hilo principal**, que es el mismo que dibuja la pantalla. El código sí cambia el mensaje a "Trabajando...", pero el hilo principal se queda dormido antes de poder dibujar ese cambio. Cuando despierta, el mensaje ya es "Terminó", así que solo se dibuja el valor final.

**¿Qué mostraría Android si la espera fuera de 10 segundos?** Mostraría el diálogo **ANR (Application Not Responding)**: *"CentroTareas no responde"*, con las opciones **Cerrar app** o **Esperar**. Android lo muestra cuando la app no responde a un toque en unos 5 segundos porque el hilo principal está bloqueado.
