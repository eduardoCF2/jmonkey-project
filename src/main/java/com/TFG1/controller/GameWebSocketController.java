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
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.TFG1.core.engine.GameState;
import com.TFG1.core.dice.Die;
import com.TFG1.core.cards.Card;
import com.TFG1.core.cards.CardRegistry;

/**
 * Controlador para la Pasarela de WebSockets.
 * Maneja las conexiones en tiempo real de los jugadores durante el transcurso
 * de una partida.
 */

public class GameWebSocketController {

    private static final Map<String, Set<WsContext>> roomConnections = new ConcurrentHashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void registerRoutes(Javalin api, RoomService roomService, CardRegistry cardRegistry) {

        // El endpoint será /ws/game/{code}?token=XYZ
        api.ws("/ws/game/{code}", ws -> {

            // 1. Cuando un jugador intenta conectarse a la sala
            ws.onConnect(ctx -> {
                String roomCode = ctx.pathParam("code");
                String token = ctx.queryParam("token");

                try {
                    // Evita que el servidor se cuelgue
                    ctx.session.setIdleTimeout(java.time.Duration.ofHours(1));

                    if (token == null)
                        throw new RuntimeException("No token present");
                    String username = JwtService.validateToken(token);

                    // Autorizacion

                    ctx.attribute("username", username);

                    // Metemos su tubo de comunicación en la lista de nuestra sala
                    roomConnections.putIfAbsent(roomCode, ConcurrentHashMap.newKeySet());
                    roomConnections.get(roomCode).add(ctx);

                    System.out.println("[WS] Jugador " + username + " conectado a sala " + roomCode);

                    // Avisamos a todos los de la sala (broadcast) que ha entrado alguien
                    broadcastMessage(roomCode, new WsMessage("SYSTEM", username + " se ha unido a la partida."));

                } catch (Exception e) {
                    ctx.session.close(1008, "Desautorizado: Token inválido");
                }
            });

            // Espera un mensaje
            ws.onMessage(ctx -> {
                String roomCode = ctx.pathParam("code");
                String username = ctx.attribute("username");
                String rawMessage = ctx.message();

                try {
                    WsMessage msg = mapper.readValue(rawMessage, WsMessage.class);
                    System.out.println("[WS " + roomCode + "] " + username + " -> Acción: " + msg.getType());

                    Room room = roomService.getRoom(roomCode);
                    if (room == null)
                        return;
                    GameManager gm = room.getGameManager();

                    switch (msg.getType()) {
                        case "START_GAME":
                            for (PlayerState ps : room.getPlayers().values()) {
                                gm.addPlayer(new Player(ps.getUserId(), ps.getUserId(), 5));
                            }
                            gm.dealCards(cardRegistry);
                            gm.startGame();

                            broadcastMessage(roomCode, new WsMessage("GAME_STARTED", "¡Comienza el juego de Dudo!"));
                            sendSecretDiceToPlayers(roomCode, gm);
                            sendSecretHandsToPlayers(roomCode, gm);
                            broadcastTableState(roomCode, gm);
                            broadcastMessage(roomCode,
                                    new WsMessage("NEXT_TURN", "Es el turno de: " + gm.getCurrentPlayer().getName()));
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
                                    broadcastTableState(roomCode, gm);
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

                                if (gm.getState() == GameState.GAME_OVER) {
                                    Player winner = gm.getWinner();
                                    String winnerName = (winner != null) ? winner.getName() : "Nadie";
                                    broadcastMessage(roomCode,
                                            new WsMessage("GAME_OVER", "El ganador es: " + winnerName));
                                    try {
                                        for (Player p : gm.getPlayers()) {
                                            boolean won = p.equals(winner);
                                            roomService.recordMatchResult(p.getId(), won, "Partida finalizada");
                                        }
                                    } catch (Exception e) {
                                        System.out.println("No se pudo guardar historial en DB: " + e.getMessage());
                                    }
                                } else {
                                    sendSecretDiceToPlayers(roomCode, gm);
                                    broadcastTableState(roomCode, gm);
                                    broadcastMessage(roomCode, new WsMessage("NEXT_TURN",
                                            "Nueva ronda, tira: " + gm.getCurrentPlayer().getName()));
                                }
                            } else {
                                ctx.send(mapper.writeValueAsString(new WsMessage("ERROR", "No puedes dudar ahora")));
                            }
                            break;

                        case "PLAYER_READY":
                            roomService.setPlayerReady(roomCode, username, true);
                            broadcastMessage(roomCode, new WsMessage("PLAYER_READY", username + " está listo."));
                            break;

                        case "PLAY_CARD":
                            if (msg.getPayload() != null) {
                                Map<String, Integer> map = (Map<String, Integer>) msg.getPayload();
                                int cardId = map.get("cardId");

                                boolean success = gm.playCard(username, cardId);
                                if (success) {
                                    Card playedCard = cardRegistry.getCardById(cardId);
                                    String effectMsg = username + " jugó " + playedCard.name() + ".";
                                    if (playedCard.type() == com.TFG1.core.cards.CardType.PALO)
                                        effectMsg += " ¡Ha relanzado sus dados!";
                                    else if (playedCard.type() == com.TFG1.core.cards.CardType.TRIUNFO)
                                        effectMsg += " ¡Activa bandera de saltar turno enemigo!";
                                    else if (playedCard.type() == com.TFG1.core.cards.CardType.JOKER)
                                        effectMsg += " ¡Sabotaje! El siguiente rival pierde un dado.";

                                    broadcastMessage(roomCode, new WsMessage("CARD_EFFECT", effectMsg));

                                    if (gm.getState() == GameState.GAME_OVER) {
                                        Player winner = gm.getWinner();
                                        String winnerName = (winner != null) ? winner.getName() : "Nadie";
                                        broadcastMessage(roomCode,
                                                new WsMessage("GAME_OVER", "El ganador es: " + winnerName));
                                        try {
                                            for (Player p : gm.getPlayers())
                                                roomService.recordMatchResult(p.getId(), p.equals(winner),
                                                        "Partida finalizada por Joker");
                                        } catch (Exception e) {
                                        }
                                    } else {
                                        sendSecretDiceToPlayers(roomCode, gm);
                                        sendSecretHandsToPlayers(roomCode, gm);
                                        broadcastTableState(roomCode, gm);
                                    }
                                } else {
                                    ctx.send(mapper.writeValueAsString(
                                            new WsMessage("ERROR", "No puedes jugar esa carta ahora")));
                                }
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

                    Room room = roomService.getRoom(roomCode);
                    if (room != null && room.isPlaying()) {
                        GameManager gm = room.getGameManager();
                        gm.handleDisconnect(username);

                        if (gm.getState() == GameState.GAME_OVER) {
                            Player winner = gm.getWinner();
                            if (winner != null) {
                                broadcastMessage(roomCode, new WsMessage("GAME_OVER",
                                        "El ganador es: " + winner.getName() + " por desconexión"));
                                try {
                                    for (Player p : gm.getPlayers()) {
                                        roomService.recordMatchResult(p.getId(), p.equals(winner),
                                                "Ganado por abandono");
                                    }
                                } catch (Exception e) {
                                    System.out.println("No se pudo guardar historial DB: " + e.getMessage());
                                }
                            }
                        } else {
                            broadcastTableState(roomCode, gm);
                            broadcastMessage(roomCode,
                                    new WsMessage("NEXT_TURN", "Turno de: " + gm.getCurrentPlayer().getName()));
                        }
                    }
                }
            });
            ws.onError(ctx -> {
                System.err.println("ERROR en Socket: " + ctx.error());
            });
        });
    }

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

    private static void sendSecretDiceToPlayers(String roomCode, GameManager gm) {
        Set<WsContext> playersInRoom = roomConnections.get(roomCode);
        if (playersInRoom == null)
            return;

        for (WsContext playerCtx : playersInRoom) {
            String ctxUser = playerCtx.attribute("username");
            for (Player p : gm.getPlayers()) {
                if (p.getId().equals(ctxUser)) {
                    List<Integer> diceVals = new ArrayList<>();
                    for (Die d : p.cup()) {
                        diceVals.add(d.getValue());
                    }
                    WsMessage secretMsg = new WsMessage("SECRET_DICE", diceVals);
                    try {
                        if (playerCtx.session.isOpen()) {
                            playerCtx.send(mapper.writeValueAsString(secretMsg));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
    }

    private static void sendSecretHandsToPlayers(String roomCode, GameManager gm) {
        Set<WsContext> playersInRoom = roomConnections.get(roomCode);
        if (playersInRoom == null)
            return;

        for (WsContext playerCtx : playersInRoom) {
            String ctxUser = playerCtx.attribute("username");
            for (Player p : gm.getPlayers()) {
                if (p.getId().equals(ctxUser)) {
                    WsMessage secretMsg = new WsMessage("SECRET_HAND", p.hand());
                    try {
                        if (playerCtx.session.isOpen()) {
                            playerCtx.send(mapper.writeValueAsString(secretMsg));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
    }

    private static void broadcastTableState(String roomCode, GameManager gm) {
        Map<String, Object> stateInfo = new HashMap<>();

        // Player states
        List<Map<String, Object>> playersInfo = new ArrayList<>();
        for (Player p : gm.getPlayers()) {
            Map<String, Object> pInfo = new HashMap<>();
            pInfo.put("name", p.getName());
            pInfo.put("diceCount", p.cup().size());
            pInfo.put("eliminated", p.isEliminated());
            playersInfo.add(pInfo);
        }
        stateInfo.put("players", playersInfo);

        // Current bid
        if (gm.getCurrentBid() != null) {
            Map<String, Integer> bidInfo = new HashMap<>();
            bidInfo.put("quantity", gm.getCurrentBid().quantity());
            bidInfo.put("value", gm.getCurrentBid().value());
            stateInfo.put("currentBid", bidInfo);
        } else {
            stateInfo.put("currentBid", null);
        }

        broadcastMessage(roomCode, new WsMessage("TABLE_STATE", stateInfo));
    }
}
