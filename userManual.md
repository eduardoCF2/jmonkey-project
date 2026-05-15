# 📖 Manual de Usuario: Dudo y Magia

¡Bienvenido al manual oficial de usuario! Este documento está diseñado para guiarte paso a paso por todas las funcionalidades de nuestro juego de mesa virtual, desde la creación de tu cuenta hasta coronarte como el maestro mentiroso de la mesa.

---

## 1. Primeros Pasos: Acceso al Sistema

### 1.1 Registro e Inicio de Sesión
Para poder jugar y guardar tu progreso, debes tener una cuenta:
- **Registro:** Introduce un nombre de usuario y una contraseña segura. El sistema creará tu perfil en la base de datos de forma encriptada.
- **Login:** Una vez registrado, introduce tus credenciales para obtener acceso al menú principal. Tu sesión está protegida mediante un token de seguridad (JWT).

### 1.2 El Perfil de Usuario
Desde el menú principal puedes consultar tus estadísticas globales. El sistema guarda automáticamente:
- Partidas jugadas.
- Partidas ganadas y perdidas.
- Tu saldo actual de **Oro** (ganarás 15 monedas de oro por cada victoria).

---

## 2. La Tienda y el Metajuego

El juego incorpora un sistema de cartas que puedes utilizar para ganar ventajas estratégicas. 

### 2.1 Comprar Cartas
Accede a la Tienda desde el menú principal para gastar tu oro en nuevas cartas. Existen 4 rarezas basadas en los palos de la baraja española:
- **Bastos (Comunes):** Gratuitas e infinitas.
- **Copas (Raras):** Cuestan 50 de oro.
- **Espadas (Épicas):** Cuestan 150 de oro.
- **Oros (Legendarias):** Cuestan 300 de oro.
> **Nota:** Las cartas compradas con oro son **consumibles**. Si las usas en una partida, desaparecerán de tu inventario.

---

## 3. Preparando la Partida (Lobby)

Para jugar con tus amigos, deberéis reuniros en una Sala Privada.

### 3.1 Crear o Unirse a una Sala
- **Crear Sala (Host):** El sistema generará un código único de 6 letras y números. Compártelo con tus amigos.
- **Unirse a Sala:** Introduce el código que te haya pasado el anfitrión. La sala admite un máximo de 4 jugadores.

### 3.2 Construcción del Mazo
Antes de darle a "Listo", debes seleccionar las cartas que llevarás a la mesa. Selecciona un máximo de 4 cartas siguiendo estas reglas obligatorias:
- **Cartas de Porcentaje (1 al 6):** Máximo 2 cartas y no pueden tener el mismo número.
- **Triunfos (10, 11 o 12):** Máximo 1 carta de triunfo.
- **Joker:** Máximo 1 joker.

### 3.3 Arrancar
Cuando tengas tu mazo listo, pulsa **"Listo"**. Cuando todos los jugadores estén listos, el Anfitrión podrá iniciar la partida y el servidor os conectará a la mesa 3D en tiempo real.

---

## 4. Reglas del Juego (El Dudo)

El objetivo es ser el último jugador con dados en la mesa. Cada jugador empieza con **5 dados**.

### 4.1 La Ronda
Al inicio de cada ronda, se agitan los cubiletes. **Solo tú puedes ver tus propios dados.** 

### 4.2 Tu Turno (Pujar)
Cuando te toque, debes hacer una afirmación ("Pujar") sobre el total de dados en la mesa. 
*Ejemplo: "Creo que en total hay por lo menos cuatro dados con el número 3".*

El turno pasa al siguiente jugador, quien tiene dos opciones:
1. **Subir la apuesta:** Hacer una puja mayor (ej. "Cinco 3s" o "Cuatro 5s").
2. **Dudar (Llamar Mentiroso):** Si cree que la apuesta anterior es falsa, levanta los cubiletes de todos. 
   - Si había menos dados de los apostados, el que pujó pierde un dado.
   - Si había la misma cantidad o más, el que dudó pierde un dado.

Quien pierde el dado, comienza la siguiente ronda.

---

## 5. Uso de Cartas en la Mesa

Durante tu turno, **antes de pujar o dudar**, puedes jugar una de las cartas que trajiste de tu mazo para alterar la realidad a tu favor.

### Efectos Disponibles:
1. **Cartas de Número (1 al 6):** Aumentan la probabilidad (del +5% al +20% según el palo) de que tus dados caigan en ese número concreto al re-tirarlos.
2. **Comodines (7):** Transforma uno de tus dados a la "Moda" (la cara que más se repite en la mesa entera en ese momento).
3. **Sotas (10):** Crea un terremoto en la mesa; todos los jugadores vuelven a lanzar sus dados.
4. **Caballos (11):** Intercambia uno de tus dados con el de un rival elegido.
5. **Reyes (12):** Obliga a un rival a revelar públicamente uno de sus dados al resto de la mesa.
6. **Jokers:**
   - *Bastos:* Todos pasan un dado al jugador de su derecha.
   - *Copas:* Todos los jugadores re-tiran sus dados.
   - *Espadas (Duelo a Ciegas):* Tú y el rival que elijas jugaréis la ronda a ciegas, sin poder ver vuestros propios dados.
   - *Oros (Robin Hood):* Quien más dados tenga, le regala uno automáticamente al que menos tenga.

---

## 6. Fin del Juego
Cuando un jugador se queda a 0 dados, es eliminado y pasa a ser espectador. La partida termina cuando solo queda un superviviente. El ganador recibe oro y la victoria se registra en su historial.
