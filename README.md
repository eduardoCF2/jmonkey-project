# 🎲 Dudo & Magia - Backend Game Server (TFG)

![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Javalin](https://img.shields.io/badge/Javalin-Framework-blue?style=for-the-badge&logo=java)
![Hibernate](https://img.shields.io/badge/Hibernate-ORM-59666C?style=for-the-badge&logo=hibernate&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/redis-%23DD0031.svg?style=for-the-badge&logo=redis&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=JSON%20web%20tokens)

Repositorio oficial del backend y motor lógico para el Trabajo de Fin de Grado (TFG): **Una implementación digital, multijugador y enriquecida del clásico juego de mesa "Dudo" (Liar's Dice) con mecánicas de cartas de manipulación estadística.**

> **📚 Nota para jugadores:** Si buscas las reglas del juego, cómo jugar o cómo funciona la tienda de cartas, por favor consulta el [Manual de Usuario (userManual.md)](./userManual.md).

---

## 🛠 Arquitectura y Stack Tecnológico

Este proyecto está construido bajo una arquitectura Cliente-Servidor. Este repositorio contiene el código del **Servidor Autorizado (Authoritative Server)**, encargado de dictar la lógica del juego, manejar las conexiones en tiempo real, evitar trampas de los clientes y persistir los datos.

*   **Lenguaje:** Java 17+
*   **Servidor Web y Enrutamiento REST:** [Javalin](https://javalin.io/).
*   **Comunicaciones en Tiempo Real:** WebSockets (Gestionado nativamente por Javalin).
*   **Persistencia y ORM:** [Hibernate](https://hibernate.org/) (Gestión de Base de Datos Relacional).
*   **Base de Datos Principal:** PostgreSQL (Para usuarios, estadísticas e inventario).
*   **Almacenamiento en Caché/Estados rápidos:** Redis (Ideal para manejar estados de salas y sesiones rápidas).
*   **Seguridad y Autenticación:** JWT (JSON Web Tokens) inyectados durante el handshake HTTP de los WebSockets y validación de endpoints REST.
*   **Logging:** Log4j 2.x (Para trazas asíncronas de eventos del motor).

---

## ⚙️ Características Técnicas Implementadas

1.  **Motor Multijugador por WebSockets:** El servidor instanciará *Salas (Rooms)* independientes. Todas las acciones de los usuarios (pujar, dudar, tirar dados) se encolan y resuelven de forma segura en el hilo del servidor, emitiendo *broadcasts* del nuevo estado a los miembros de la sala.
2.  **Sistema de Inventario y Economía:** Integración de una tienda mediante API RESTful que permite la compra transaccional de cartas coleccionables que se guardan en la DB mediante Hibernate.
3.  **Patrón Strategy (Cartas):** El motor lógico del juego emplea patrones de diseño de software (como *Factory* y *Strategy*) para interpretar dinámicamente los efectos de las cartas y alterar matemáticamente las matrices de probabilidad de los dados en tiempo real.
4.  **Autenticación Segura:** Generación y validación de tokens JWT en el inicio de sesión. Las contraseñas están hasheadas en base de datos.
5.  **Externalización de Configuración:** Aplicación de las metodologías *12-Factor App* inyectando las dependencias externas (URLs de BD, puertos, secretos de JWT) mediante un fichero `application.properties`.

---

## 🚀 Despliegue y Ejecución (Getting Started)

### Requisitos Previos
*   Java Development Kit (JDK) 17 o superior.
*   Maven 3.8+.
*   Instancia local o remota de **PostgreSQL** iniciada.
*   Instancia local o remota de **Redis** iniciada (opcional dependiendo de la configuración actual del entorno).

### Configuración del Entorno (`application.properties`)

Antes de compilar, necesitas crear o editar el archivo `application.properties` en la carpeta `src/main/resources/` con tus credenciales de entorno:

```properties
# Servidor HTTP
server.port=7070

# Configuración Hibernate / PostgreSQL
db.url=jdbc:postgresql://localhost:5432/dudodb
db.username=tu_usuario
db.password=tu_contraseña

# Configuración de Seguridad
jwt.secret=CLAVE_SECRETA_PARA_GENERAR_LOS_TOKENS_HS256

# Configuración Redis
redis.host=localhost
redis.port=6379
```

### Compilación y Ejecución

1.  Clona el repositorio:
    ```bash
    git clone https://github.com/tu_usuario/jmonkey-project.git
    cd jmonkey-project
    ```
2.  Instala las dependencias y compila con Maven:
    ```bash
    mvn clean install
    ```
3.  Ejecuta la clase principal (`App.java`):
    ```bash
    mvn exec:java -Dexec.mainClass="com.TFG1.App"
    ```
    El servidor iniciará en `http://localhost:7070`.

---

## 📖 Documentación Complementaria

*   **Manual de Juego:** [userManual.md](./userManual.md) - Explicación detallada para usuarios finales sobre reglas de las rondas, penalizaciones y descripciones de las cartas.
## 📡 Endpoints de la API REST y WebSockets

El servidor expone la siguiente API para gestionar la cuenta, la tienda y la sala de espera antes de la partida. Todos los endpoints (excepto login/register) requieren cabecera `Authorization: Bearer <JWT>`.

### Autenticación (`/api`)
*   `POST /api/register` - Registro de un nuevo usuario.
*   `POST /api/login` - Inicio de sesión (Devuelve Token JWT).
*   `GET /api/profile` - Obtener datos del perfil activo.

### Tienda e Inventario (`/api/shop`)
*   `GET /api/shop/cards` - Lista las cartas disponibles en la tienda y sus precios.
*   `POST /api/shop/buy-card` - Compra de una carta gastando oro.

### Salas y Lobby (`/api/rooms`)
*   `POST /api/rooms` - Crea una nueva sala (Devuelve el código de 6 caracteres).
*   `POST /api/rooms/{code}/join` - Unirse a una sala existente.
*   `POST /api/rooms/{code}/cards` - Configurar tu mazo de cartas para la partida.
*   `PUT /api/rooms/{code}/ready` - Marcar estado como "Listo".
*   `POST /api/rooms/{code}/start` - El Anfitrión arranca la partida.

### Motor de Juego (WebSockets)
*   `WS /ws/game/{code}` - Conexión TCP bidireccional y persistente para jugar en tiempo real. Soporta envío de JSONs con acciones como "PUJAR", "DUDAR", o "USAR_CARTA".

---
*Trabajo de Fin de Grado (TFG) por Eduardo Cachero, David Soler, David Sanz. 2026*
