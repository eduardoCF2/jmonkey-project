package com.TFG1.controller;

import com.TFG1.model.WsMessage;
import com.TFG1.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.javalin.websocket.WsContext;

import com.TFG1.core.engine.GameManager;
import com.TFG1.core.engine.Player;
import com.TFG1.core.dice.Bid;
import com.TFG1.model.PlayerState;
import com.TFG1.model.Room;
import com.TFG1.service.RoomService;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controlador para la Pasarela de WebSockets.
 * Maneja las conexiones en tiempo real de los jugadores durante el transcurso
 * de una partida.
 */
public class GameWebSocketController {

    // Diccionario para guardar las conexiones activas por sala.
    // Clave: roomCode. Valor: Set de conexiones (Contextos WebSocket de los
    // jugadores ahí metidos).
    private static final Map<String, Set<WsContext>> roomConnections = new ConcurrentHashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void registerRoutes(Javalin api, RoomService roomService) {

        // El endpoint será /ws/game/{code}?token=XYZ
        api.ws("/ws/game/{code}", ws -> {

            // 1. Cuando un jugador intenta conectarse a la sala
            ws.onConnect(ctx -> {
                String roomCode = ctx.pathParam("code");
                String token = ctx.queryParam("token");

                try {
                    // Evitar que el servidor corte por inactividad a los pocos segundos/minutos. Lo
                    // subimos a 1 hora.
                    ctx.session.setIdleTimeout(java.time.Duration.ofHours(1));

                    // Validamos Seguridad
                    if (token == null)
                        throw new RuntimeException("No token present");
                    String username = JwtService.validateToken(token);

                    // Autorizado. Le guardamos un "pin" en la solapa para reconocerle en los
                    // siguientes mensajes
                    ctx.attribute("username", username);

                    // Metemos su tubo de comunicación en la lista de nuestra sala
                    roomConnections.putIfAbsent(roomCode, ConcurrentHashMap.newKeySet());
                    roomConnections.get(roomCode).add(ctx);

                    System.out.println("✅ [WS] Jugador " + username + " conectado a sala " + roomCode);

                    // Avisamos a todos los de la sala (broadcast) que ha entrado alguien
                    broadcastMessage(roomCode, new WsMessage("SYSTEM", username + " se ha unido a la partida."));

                } catch (Exception e) {
                    // Si el token es falso o no hay token, cerramos la puerta violentamente
                    ctx.session.close(1008, "Desautorizado: Token inválido");
                }
            });

            // 2. Cuando recibimos un mensaje (Cartas jugadas, dados, chat...)
            ws.onMessage(ctx -> {
                String roomCode = ctx.pathParam("code");
                String username = ctx.attribute("username");
                String rawMessage = ctx.message();

                try {
                    WsMessage msg = mapper.readValue(rawMessage, WsMessage.class);
                    System.out.println("📩 [WS " + roomCode + "] " + username + " -> Acción: " + msg.getType());

                    Room room = roomService.getRoom(roomCode);
                    if (room == null)
                        return;
                    GameManager gm = room.getGameManager();

                    switch (msg.getType()) {
                        case "START_GAME":
                            // Inicializamos todos los jugadores dentro de la logica del juego
                            for (PlayerState ps : room.getPlayers().values()) {
                                gm.addPlayer(new Player(ps.getUserId(), ps.getUserId(), 5));
                            }
                            gm.startGame();

                            broadcastMessage(roomCode, new WsMessage("GAME_STARTED", "¡Comienza el juego de Dudo!"));

                            // Enviamos un mensaje PRIVADO a cada uno diciendole cuáles son sus dados
                            // ocultos
                            Set<WsContext> playersInRoom = roomConnections.get(roomCode);
                            for (WsContext playerCtx : playersInRoom) {
                                String ctxUser = playerCtx.attribute("username");
                                // Aquí podríamos extraer los dados exactos iterando por la lista de
                                // gm.players()
                                // De momento, solo avisamos
                                WsMessage secretMsg = new WsMessage("SECRET_DICE",
                                        "Tus dados han sido tirados. (El servidor los conoce)");
                                playerCtx.send(mapper.writeValueAsString(secretMsg));
                            }
                            break;

                        case "PLACE_BID":
                            if (msg.getPayload() != null) {
                                Map<String, Integer> map = (Map<String, Integer>) msg.getPayload();
                                int qty = map.get("quantity");
                                int val = map.get("value");

                                boolean success = gm.placeBid(username, new Bid(qty, val));
                                if (success) {
                                    broadcastMessage(roomCode,
                                            new WsMessage("NEW_BID", username + " apostó " + qty + " dados de " + val));
                                    broadcastMessage(roomCode, new WsMessage("NEXT_TURN",
                                            "Es el turno de: " + gm.getCurrentPlayer().getName()));
                                } else {
                                    ctx.send(mapper.writeValueAsString(new WsMessage("ERROR", "Jugada inválida")));
                                }
                            }
                            break;

                        case "CALL_DOUBT":
                            boolean startedDoubt = gm.callDoubt(username);
                            if (startedDoubt) {
                                broadcastMessage(roomCode,
                                        new WsMessage("DOUBT_RESULT", username + " HA DUDADO. Se resuelve la ronda."));
                                broadcastMessage(roomCode, new WsMessage("NEXT_TURN",
                                        "Nueva ronda, tira: " + gm.getCurrentPlayer().getName()));
                            } else {
                                ctx.send(mapper.writeValueAsString(new WsMessage("ERROR", "No puedes dudar ahora")));
                            }
                            break;

                        default:
                            broadcastMessage(roomCode,
                                    new WsMessage(msg.getType(), username + " -> " + msg.getPayload()));
                            break;
                    }

                } catch (Exception e) {
                    System.err.println("Error parseando JSON de socket: " + e.getMessage());
                }
            });

            // 3. Cuando un jugador cierra el juego o se le va el internet
            ws.onClose(ctx -> {
                String roomCode = ctx.pathParam("code");
                String username = ctx.attribute("username");

                if (username != null && roomConnections.containsKey(roomCode)) {
                    roomConnections.get(roomCode).remove(ctx);
                    System.out.println("[WS] Jugador " + username + " se desconectó de " + roomCode);
                    broadcastMessage(roomCode, new WsMessage("SYSTEM", username + " se ha desconectado."));
                }
            });

            // 4. Manejo de Errores imprevistos
            ws.onError(ctx -> {
                System.err.println("ERROR en Socket: " + ctx.error());
            });
        });
    }

    // Método utilitario para enviar un mensaje a TODOS los jugadores enchufados en
    // una Sala X
    private static void broadcastMessage(String roomCode, WsMessage message) {
        Set<WsContext> playersInRoom = roomConnections.get(roomCode);
        if (playersInRoom != null) {
            String jsonFinal;
            try {
                jsonFinal = mapper.writeValueAsString(message);
                for (WsContext player : playersInRoom) {
                    // Asegurarnos que la tubería siga abierta antes de disparar el mensaje
                    if (player.session.isOpen()) {
                        player.send(jsonFinal);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
