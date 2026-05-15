# 📖 Manual de Usuario: Dudo & Magia

¡Bienvenido al manual oficial de usuario! Este documento está diseñado para guiarte paso a paso por todas las funcionalidades de nuestro juego de mesa virtual, desde la creación de tu cuenta hasta coronarte como el maestro mentiroso de la mesa.

---

## 1. Primeros Pasos: Acceso al Sistema

### 1.1 Registro e Inicio de Sesión
Para poder jugar, guardar tu progreso y acumular riquezas, debes tener una cuenta:
- **Registro:** Introduce un nombre de usuario y una contraseña segura. El sistema creará tu perfil en la base de datos de forma encriptada.
- **Login:** Una vez registrado, introduce tus credenciales para obtener acceso al menú principal. Tu sesión estará protegida en todo momento.

### 1.2 El Perfil de Usuario
Desde el menú principal puedes consultar tus estadísticas globales. El sistema guarda automáticamente:
- **Partidas jugadas.**
- **Partidas ganadas y perdidas.**
- **Tu saldo actual de Oro** (ganarás 15 monedas de oro por cada victoria en la mesa).

---

## 2. La Tienda y el Metajuego

El juego incorpora un sistema de cartas coleccionables basado en la baraja española que puedes utilizar para ganar ventajas estratégicas y manipular las probabilidades de los dados.

### 2.1 Comprar Cartas
Accede a la Tienda desde el menú principal para gastar tu oro en nuevas cartas. Existen 4 rarezas:

| Palo (Rareza) | Coste en Oro | Disponibilidad |
| :--- | :--- | :--- |
| **Bastos** (Comunes) | Gratis | Infinitas |
| **Copas** (Raras) | 50 Monedas | Consumibles (1 uso) |
| **Espadas** (Épicas) | 150 Monedas | Consumibles (1 uso) |
| **Oros** (Legendarias) | 300 Monedas | Consumibles (1 uso) |

> ⚠️ **Atención:** Las cartas compradas con oro (Copas, Espadas y Oros) desaparecen de tu inventario tras usarlas en una partida. ¡Úsalas sabiamente!

---

## 3. Preparando la Partida (Lobby)

Para jugar con tus amigos, deberéis reuniros en una Sala Privada.

### 3.1 Crear o Unirse a una Sala
- **Crear Sala (Host):** El sistema generará un código único de 6 caracteres alfanuméricos. Compártelo con tus amigos.
- **Unirse a Sala:** Introduce el código que te haya pasado el anfitrión. La sala admite un **máximo de 4 jugadores**.

### 3.2 Construcción del Mazo (Deckbuilding)
Antes de darle a "Listo", debes seleccionar las cartas que llevarás a la mesa. Selecciona un máximo de **4 cartas** siguiendo estas estrictas reglas de equilibrio:
- **Cartas de Porcentaje (1 al 6):** Máximo 2 cartas. *No pueden potenciar el mismo número.*
- **Triunfos (10, 11 o 12):** Máximo 1 carta de triunfo.
- **Joker:** Máximo 1 joker.

### 3.3 Arrancar
Cuando tengas tu mazo configurado, pulsa **"Listo"**. Cuando todos los jugadores de la sala confirmen, el Anfitrión podrá iniciar la partida y el servidor os conectará a la mesa 3D en tiempo real.

---

## 4. Reglas del Juego (El Dudo)

El objetivo es ser el **último jugador con dados en la mesa**. Cada jugador empieza con **5 dados** ocultos en su cubilete.

### 4.1 La Ronda
Al inicio de cada ronda, se agitan los cubiletes. **Solo tú puedes ver tus propios dados.** Toda la demás información en la mesa es un misterio.

### 4.2 Tu Turno (Pujar o Dudar)
Cuando te toque, debes hacer una afirmación ("Pujar") sobre el total de dados en la mesa. 
> *Ejemplo: "Creo que entre todos los jugadores hay por lo menos cuatro dados con el número 3".*

El turno pasa al jugador de tu izquierda, quien tiene dos opciones obligatorias:
1. **Subir la apuesta:** Hacer una puja matemáticamente mayor (ej. "Cinco 3s" o "Cuatro 5s").
2. **Dudar (Llamar Mentiroso):** Si cree que la apuesta anterior es estadísticamente imposible o falsa, levanta los cubiletes de todos. 
   - **Si había menos dados de los apostados:** El jugador que pujó (mintió) pierde un dado.
   - **Si había la misma cantidad o más:** El jugador que dudó (se equivocó) pierde un dado.

El jugador que pierde el dado es el encargado de abrir la siguiente ronda.

---

## 5. El Arte de la Magia: Uso de Cartas en la Mesa

Durante tu turno, **estrictamente antes de pujar o dudar**, puedes jugar una de las cartas que trajiste en tu mazo para alterar la realidad matemática a tu favor.

### ✨ Efectos Disponibles:

#### 1. Cartas de Probabilidad (Números del 1 al 6)
Al jugarlas, tus dados se re-tirarán automáticamente con una probabilidad alterada de caer en el número de la carta:
- **Bastos:** +5% extra de probabilidad.
- **Copas:** +10% extra.
- **Espadas:** +15% extra.
- **Oros:** +20% extra.

#### 2. Triunfos y Comodines
- **Comodines (7):** Lee la mesa secreta y transforma uno de tus dados a la "Moda" (la cara de dado que más se repite entre todos los jugadores).
- **Sotas (10):** ¡Terremoto! Obliga a todos los jugadores de la mesa a volver a lanzar sus dados inmediatamente.
- **Caballos (11):** Intercambia, sin mirar, uno de tus dados con un dado aleatorio de un rival de tu elección.
- **Reyes (12):** Obliga a un rival elegido a revelar públicamente uno de sus dados al resto de la mesa.

#### 3. Los Jokers del Caos
- **Joker de Bastos:** Todos los jugadores pasan obligatoriamente un dado al jugador de su derecha.
- **Joker de Copas:** Todos los jugadores re-tiran sus dados actuales.
- **Joker de Espadas (Duelo a Ciegas):** Tú y el rival que elijas jugaréis la ronda a ciegas. Ninguno de los dos podrá ver el valor de sus propios dados hasta que termine la ronda.
- **Joker de Oros (Robin Hood):** El servidor fuerza al jugador que más dados tenga a regalarle uno al jugador que menos tenga en la mesa.

---

## 6. Fin del Juego

Cuando un jugador pierde su último dado (se queda a 0), es **eliminado de la partida** y pasa a modo espectador. 
La partida termina cuando solo queda **un único superviviente**. El ganador recibe su recompensa en oro y el servidor registra la victoria en su historial. ¡Buena suerte!
