# 🎲 Manual de Juego: Dudo (Liar's Dice) Multijugador

Bienvenido al sistema backend de nuestro juego de mesa. Este documento detalla estrictamente el flujo de la partida estándar, cómo interactúan los jugadores y las reglas del clásico juego "Dudo" (también conocido como Mentiroso o Perudo) aplicadas a este entorno Cliente-Servidor.

---

## 1. Fase de Lobby (Preparativos)

El juego se organiza en salas privadas (*Custom Match*).
1. **Creación de Sala:** Un jugador hace de Anfitrión (Host) creando una nueva sala. El sistema genera un código alfanumérico único de 6 caracteres.
2. **Invitación:** El anfitrión comparte el código con sus amigos. 
3. **Acceso:** Hasta 3 amigos más (máximo 4 jugadores por sala) pueden unirse al *lobby* utilizando dicho código.
4. **Confirmación:** Cuando un invitado ha seleccionado su personaje y está preparado, pulsa el botón *"Estoy Listo"*.
5. **Arranque:** Una vez todos los invitados están en estado "Listo", el anfitrión oprime el botón central de iniciar y el servidor transporta a todos a la mesa 3D virtual.

---

## 2. Inicio de Partida y Reparto

Al arrancar, el servidor asigna **5 dados a cada jugador**. 

Cada ronda, el servidor agita virtualmente todos los cubiletes. El resultado de los dados se envía **exclusivamente en secreto** a la pantalla de su respectivo dueño. 
>**Regla de oro:** Nadie conoce los dados del rival. Tu única información veraz son tus propios dados. El resto tendrás que deducirlo (o inventártelo).

---

## 3. Lógica del Turno: Las Pujas

El jugador en turno debe realizar una afirmación sobre el conjunto total de dados que hay encima de la mesa. A esto se le llama "Pujar".

**Ejemplo de puja:** *"Creo que entre todos los cubiletes de la mesa hay por lo menos cuatro 3s."* (Cantidad = 4, Valor = 3).

El turno pasa al jugador de la izquierda (sentido horario), el cual **está obligado** a tomar una de estas dos decisiones:

### Opción A: Subir la Apuesta
No está seguro de si su compañero miente, pero prefiere no arriesgarse a acusarlo. Debe subir la puja.
El sistema **solo** admite pujas que sean superiores a las del jugador anterior. Solo puedes aumentarla matemáticamente:
*   Subiendo la *Cantidad* de dados (Ej. de "cuatro 3s" a "cinco 3s").
*   Subiendo el *Valor nominal* del dado (Ej. de "cuatro 3s" a "cuatro 5s").

### Opción B: ¡Mentira! (Dudar)
Si cree que la apuesta anterior es matemáticamente exagerada o falsa, acciona el botón **Dudar** (Dudó / Call Liar). Esto corta automáticamente la ronda actual.

---

## 4. Resolución de Ronda

Cuando un jugador Duda de la apuesta de la víctima anterior, el servidor levanta los cubiletes de todos los jugadores y cuenta cuántos dados reales existen con ese valor.

1. **Si el acusador tenía razón (Mentira):** En la mesa había menos dados que los apostados. El jugador que mintió recibe el castigo.
2. **Si el acusador se equivocó (Verdad):** En la mesa había en efecto ese número exacto de dados o más. El jugador que acusó (dudó) recibe el castigo por no creer a su compañero.

**El castigo:** El perdedor de un *Dudó* pierde permanentemente 1 dado físico de su cubilete.
> El jugador que acaba de perder el dado es el encargado de abrir la nueva ronda realizando la primera puja con los dados recién lanzados.

---

## 5. Fin de la partida

Cuando un jugador pierde sus últimos dados y se queda con el cubilete vacío (0 dados), **es eliminado automáticamente** de la mesa y se convierte en espectador. 
*(Si un jugador se desconecta de la sala por pérdida de internet o abandono voluntario, sus dados son triturados y la ronda avanza saltando su turno).*

El juego finaliza inmediatamente cuando solo queda **un último superviviente** en la mesa. Las victorias y derrotas se grabarán automáticamente en el disco persistente del servidor para ser consultadas en el Perfil de Usuario posteriormente.

---

## 6. Mecánicas de Cartas y Metajuego (Novedad)

El juego cuenta con un sistema de cartas coleccionables basado en la baraja española que añade economía y manipulación de probabilidades a la partida. 

### Tienda y Preparación del Mazo (Lobby)
Antes de empezar a jugar, en la fase de Lobby, puedes configurar una mano de hasta 4 cartas de tu inventario. Estas cartas se clasifican por su rareza o palo, costando oro que ganas venciendo en las partidas:
*   **Bastos (Comunes):** Gratuitas e infinitas.
*   **Copas (Raras), Espadas (Épicas) y Oros (Legendarias):** Cuestan oro y desaparecen de tu inventario tras usarse una sola vez (*Consumibles*).

El mazo que lleves a la mesa debe respetar los siguientes límites:
- **Máximo 2 cartas de Porcentaje (del 1 al 6):** Sus valores no pueden repetirse.
- **Máximo 1 Triunfo (10, 11 o 12).**
- **Máximo 1 Joker.**

### Efectos en la Partida
Durante tu turno (antes de pujar o dudar), puedes jugar una carta para alterar el curso de la partida:

#### 1. Cartas de Porcentaje (Números del 1 al 6)
Al jugar una carta de este tipo, el número de la carta indicará la cara del dado que quieres potenciar. Automáticamente re-tirarás tus dados, aumentando temporalmente tus probabilidades de que salga esa cara.
*   Bastos: +5% extra de probabilidad.
*   Copas: +10% extra de probabilidad.
*   Espadas: +15% extra de probabilidad.
*   Oros: +20% extra de probabilidad.
*(El servidor se encarga matemáticamente de re-equilibrar las otras 5 caras para mantener un total del 100%).*

#### 2. Cartas Especiales y Triunfos
Son herramientas tácticas que rompen las reglas:
- **El Comodín (7):** Lee la mesa, detecta cuál es la cara de dado más repetida entre todos los jugadores (la *Moda*), y mágicamente voltea uno de tus dados a esa cara.
- **Sotas de Bastos/Copas (10):** Agita la mesa completa y obliga a todos los jugadores a re-tirar todos sus dados actuales.
- **Caballos de Espadas (11):** Te permite intercambiar uno de tus dados aleatorios con un dado aleatorio de un oponente a tu elección.
- **Reyes de Oros (12):** Fuerza a un oponente elegido a mostrar de forma pública uno de sus dados a todos los jugadores de la mesa.

#### 3. Los Jokers del Caos
Son cartas de naturaleza impredecible. Siempre son de uso infinito.
- **Joker de Bastos:** Hace que todos los jugadores pasen obligatoriamente un dado de su cubilete al jugador que tengan a la derecha.
- **Joker de Copas:** ¡Caos! Todos los jugadores de la mesa vuelven a tirar inmediatamente sus dados.
- **Joker de Espadas (Duelo a Ciegas):** Obliga a la mesa a jugar a oscuras. Tanto tú como el rival que elijas dejaréis de ver temporalmente los valores de vuestros propios dados en la pantalla (cubilete ciego).
- **Joker de Oros:** El servidor fuerza al jugador que tenga más dados en la mesa a regalarle automáticamente uno de los suyos al jugador que menos tenga.
