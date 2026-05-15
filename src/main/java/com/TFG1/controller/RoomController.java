package com.TFG1.controller;

import com.TFG1.service.RoomService;
import com.TFG1.model.Room;
import io.javalin.Javalin;
import java.util.Map;

/**
 * Controlador de Rutas (Endpoints) para el API REST del emparejamiento.
 * Aquí me encargo de recibir peticiones desde el exterior y derivar la acción a la capa Service.
 */
public class RoomController {
    
    // Inyecto como dependencia directa el servicio responsable de toda la lógica (RoomService).
    private RoomService roomService;

    // Con este constructor exijo instanciar de forma obligatoria al servicio cuando arranque todo en App.java.
    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    // Registro uno a uno los dominios de la URL a los que mi frontend puede lanzar peticiones
    public void registerRoutes(Javalin api) {
        
        // ENDPOINT POST /api/rooms | Opcion para Crear mi Sala Privada.
        api.post("/api/rooms", ctx -> {
            // Recibo como molde 'RoomRequest' del JSON quién me solicita crearla (su UserId)
            String userId = ctx.bodyAsClass(RoomRequest.class).userId;
            
            // Le pido a mi Servicio crear la sala y devuelve un código alfanumérico (EJ: MKY23D).
            String roomCode = roomService.createRoom(userId);
            
            // Le contesto a la red con Http 201 Created y le paso el JSON con el código de la partida
            ctx.status(201).json(Map.of("roomCode", roomCode, "msg", "Sala creada correctamente"));
        });

        // ENDPOINT POST /api/rooms/{code}/join | Para unirme de Visitante a una Sala.
        api.post("/api/rooms/{code}/join", ctx -> {
            String code = ctx.pathParam("code"); // Tomo la sección de la URL con el code.
            String userId = ctx.bodyAsClass(RoomRequest.class).userId; 
            
            boolean result = roomService.joinRoom(code, userId); // Pido acceso en mi servicio.
            if(result) { // Retorno True, entrar a la sala
                ctx.status(200).result("Unido a la sala con éxito");
            } else { // Retorno False, puerta llena o la ID que pasaron no es válida
                ctx.status(400).result("Fallo al unirse: Sala llena, partida ya iniciada al juego o simplemente no existe");
            }
        });

        // ENDPOINT PUT /api/rooms/{code}/ready | Cambiar estado de Listo
        api.put("/api/rooms/{code}/ready", ctx -> {
            String code = ctx.pathParam("code");
            ReadyRequest req = ctx.bodyAsClass(ReadyRequest.class); // Extraigo la petición "isReady" del Json 
            
            // Mando notificar en memoria que cambio el estado de listo a lo que mande mi boolean
            roomService.setPlayerReady(code, req.userId, req.isReady);
            ctx.status(200).result("Estado de Readiness ha sido modificado y salvado");
        });

        // ENDPOINT POST /api/rooms/{code}/start | Arrancar mi Partida
        api.post("/api/rooms/{code}/start", ctx -> {
            String code = ctx.pathParam("code");
            String hostId = ctx.bodyAsClass(RoomRequest.class).userId; 
            
            // Fuerzo un test en el Service para comprobar si soy el Host, tengo 2 personas mínimas y todos están Readies
            boolean gameStarted = roomService.startGame(code, hostId);
            
            if(gameStarted) {
                // Todo correcto, devuelvo el 200 verde.
                ctx.status(200).result("Partida iniciada positivamente. Traspasando Lobbies");
            } else { 
                // Corto el intento porque no logré el ready de todos o no soy host
                ctx.status(400).result("Fallo al autorizar: Debo corroborar que soy Host auténtico y vigilar que mis compañeros oponentes hayan puesto que están Listos");
            }
        });
        
        // ENDPOINT GET /api/rooms/{code} | Obtener estado de la sala
        api.get("/api/rooms/{code}", ctx -> {
            String code = ctx.pathParam("code");
            Room room = roomService.getRoom(code);
            if (room != null) {
                // Mapeamos a mano para evitar el crash (Error 500) de Jackson al intentar serializar el GameManager entero
                Map<String, Object> response = new java.util.HashMap<>();
                response.put("roomCode", room.getRoomCode());
                response.put("isPlaying", room.isPlaying());
                
                Map<String, Object> playersMap = new java.util.HashMap<>();
                for (com.TFG1.model.PlayerState p : room.getPlayers().values()) {
                    Map<String, Object> pInfo = new java.util.HashMap<>();
                    pInfo.put("userId", p.getUserId());
                    pInfo.put("isReady", p.isReady());
                    pInfo.put("isHost", p.isHost());
                    pInfo.put("selectedCards", p.getSelectedCards()); // Añadimos esto
                    playersMap.put(p.getUserId(), pInfo);
                }
                response.put("players", playersMap);
                
                ctx.status(200).json(response);
            } else {
                ctx.status(404).result("Sala no encontrada");
            }
        });
        
        // ENDPOINT POST /api/rooms/{code}/cards | Seleccionar cartas para la partida
        api.post("/api/rooms/{code}/cards", ctx -> {
            String code = ctx.pathParam("code");
            CardSelectionRequest req = ctx.bodyAsClass(CardSelectionRequest.class);
            roomService.setSelectedCards(code, req.userId, req.cardIds);
            ctx.status(200).result("Cartas seleccionadas guardadas");
        });

        // ENDPOINT GET /users/{userId}/stats | Consultar mi Historial
        api.get("/users/{userId}/stats", ctx -> {
            String userId = ctx.pathParam("userId");
            // Formateo devolviendo mi objeto JSON con mis stats recuperadas del servicio
            ctx.status(200).json(roomService.getUserStats(userId));
        });
    }

    // ---------- Mis DTOs (Data Transfer Objects) ----------
    // Uso estas clases para mapear el JSON que recibo en mis endpoints.
    public static class RoomRequest {
        public String userId;
        public RoomRequest() {}
    }
    
    public static class ReadyRequest {
        public String userId;
        public boolean isReady;
        public ReadyRequest() {}
    }

    public static class CardSelectionRequest {
        public String userId;
        public java.util.List<Integer> cardIds;
        public CardSelectionRequest() {}
    }
}
