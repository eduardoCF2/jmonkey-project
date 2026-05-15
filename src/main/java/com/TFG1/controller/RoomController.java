package com.TFG1.controller;

import com.TFG1.service.RoomService;
import com.TFG1.model.Room;
import io.javalin.Javalin;
import java.util.Map;

/**
 * Controlador de Rutas (Endpoints) para el API REST del emparejamiento
 * Aqui me encargo de recibir peticiones desde el exterior y derivar la accion a
 * la capa Service
 */
public class RoomController {

    private RoomService roomService;
    private com.TFG1.core.cards.CardRegistry cardRegistry;

    public RoomController(RoomService roomService, com.TFG1.core.cards.CardRegistry cardRegistry) {
        this.roomService = roomService;
        this.cardRegistry = cardRegistry;
    }

    public void registerRoutes(Javalin api) {

        api.post("/api/rooms", ctx -> {

            String userId = ctx.bodyAsClass(RoomRequest.class).userId;

            String roomCode = roomService.createRoom(userId);

            ctx.status(201).json(Map.of("roomCode", roomCode, "msg", "Sala creada correctamente"));
        });

        api.post("/api/rooms/{code}/join", ctx -> {
            String code = ctx.pathParam("code");
            String userId = ctx.bodyAsClass(RoomRequest.class).userId;

            boolean result = roomService.joinRoom(code, userId);
            if (result) {
                ctx.status(200).result("Unido a la sala con éxito");
            } else {
                ctx.status(400)
                        .result("Fallo al unirse: Sala llena, partida ya iniciada al juego o simplemente no existe");
            }
        });

        api.put("/api/rooms/{code}/ready", ctx -> {
            String code = ctx.pathParam("code");
            ReadyRequest req = ctx.bodyAsClass(ReadyRequest.class);

            roomService.setPlayerReady(code, req.userId, req.isReady);
            ctx.status(200).result("Estado de Readiness ha sido modificado y salvado");
        });

        api.post("/api/rooms/{code}/cards", ctx -> {
            String code = ctx.pathParam("code");
            SelectCardsRequest req = ctx.bodyAsClass(SelectCardsRequest.class);

            boolean success = roomService.setPlayerCards(code, req.userId, req.cardIds, cardRegistry);
            if (success) {
                ctx.status(200).result("Mano de cartas seleccionada correctamente");
            } else {
                ctx.status(400).result(
                        "Error al seleccionar cartas. Verifica las restricciones de la mano o el estado de la sala.");
            }
        });

        api.post("/api/rooms/{code}/start", ctx -> {
            String code = ctx.pathParam("code");
            String hostId = ctx.bodyAsClass(RoomRequest.class).userId;

            boolean gameStarted = roomService.startGame(code, hostId);

            if (gameStarted) {

                ctx.status(200).result("Partida iniciada positivamente. Traspasando Lobbies");
            } else {

                ctx.status(400).result(
                        "Fallo al autorizar: Debo corroborar que soy Host auténtico y vigilar que mis compañeros oponentes hayan puesto que están Listos");
            }
        });

        api.get("/api/rooms/{code}", ctx -> {
            String code = ctx.pathParam("code");
            Room room = roomService.getRoom(code);
            if (room != null) {

                Map<String, Object> response = new java.util.HashMap<>();
                response.put("roomCode", room.getRoomCode());
                response.put("isPlaying", room.isPlaying());

                Map<String, Object> playersMap = new java.util.HashMap<>();
                for (com.TFG1.model.PlayerState p : room.getPlayers().values()) {
                    Map<String, Object> pInfo = new java.util.HashMap<>();
                    pInfo.put("userId", p.getUserId());
                    pInfo.put("isReady", p.isReady());
                    pInfo.put("isHost", p.isHost());
                    playersMap.put(p.getUserId(), pInfo);
                }
                response.put("players", playersMap);

                ctx.status(200).json(response);
            } else {
                ctx.status(404).result("Sala no encontrada");
            }
        });

        api.get("/users/{userId}/stats", ctx -> {
            String userId = ctx.pathParam("userId");

            ctx.status(200).json(roomService.getUserStats(userId));
        });
    }

    public static class RoomRequest {
        public String userId;

        public RoomRequest() {
        }
    }

    public static class ReadyRequest {
        public String userId;
        public boolean isReady;

        public ReadyRequest() {
        }
    }

    public static class SelectCardsRequest {
        public String userId;
        public java.util.List<Integer> cardIds;

        public SelectCardsRequest() {
        }
    }
}
