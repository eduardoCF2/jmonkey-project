package com.TFG1.service;

import com.TFG1.model.PlayerState;
import com.TFG1.model.Room;
import com.TFG1.model.UserStats;
import com.TFG1.repository.UserStatsRepository;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Capa de Servicios: Aqui se concentra "Logica de Negocio de Procesamiento" de las Salas y emparejamiento
 * Se manejan internamente las estructuras con diccionarios al no tener Base de Datos temporalmente
 */
public class RoomService {
    private ConcurrentHashMap<String, Room> activeRooms = new ConcurrentHashMap<>();

    private UserStatsRepository userStatsRepository;
    private com.TFG1.repository.UserRepository userRepository = new com.TFG1.repository.UserRepository();

    public RoomService(UserStatsRepository userStatsRepository) {
        this.userStatsRepository = userStatsRepository;
    }

    public Room getRoom(String roomCode) {
        return activeRooms.get(roomCode);
    }

    /**
     *Un jugador (Host) puede crear una sala en el sistema usando un codigo unico generado
     */
    public String createRoom(String hostUserId) {
        String roomCode = generateRandomCode(6);
        Room newRoom = new Room(roomCode);

        PlayerState hostPlayer = new PlayerState(hostUserId, true);
        newRoom.addPlayer(hostPlayer);

        activeRooms.put(roomCode, newRoom);
        return roomCode;
    }

    /**
     * Elimina una sala terminada del mapa de salas activas
     */
    public void closeRoom(String roomCode) {
        activeRooms.remove(roomCode);
        System.out.println("[RoomService] Sala " + roomCode + " eliminada del sistema.");
    }

    /**
     *Los jugadores se unen manualmente y bloqueo mi sala a un maximo de 4 personas
     */
    public boolean joinRoom(String roomCode, String userId) {
        Room room = activeRooms.get(roomCode);

        if (room == null || room.isPlaying()) return false;

        if (room.getPlayers().size() >= 4) return false;

        room.addPlayer(new PlayerState(userId, false));
        return true;
    }

    /**
     * Actualizo y sincronizo el estado real de mi jugador de si esta 'Listo/No Listo'
     */
    public void setPlayerReady(String roomCode, String userId, boolean isReady) {
        Room room = activeRooms.get(roomCode);

        if (room != null && room.getPlayers().containsKey(userId)) {
            room.getPlayers().get(userId).setReady(isReady);
        }
    }

    public void setSelectedCards(String roomCode, String userId, java.util.List<Integer> cardIds) {
        Room room = activeRooms.get(roomCode);
        if (room != null && room.getPlayers().containsKey(userId)) {
            room.getPlayers().get(userId).setSelectedCards(cardIds);
        }
    }

    /**
     * Guarda la seleccion de cartas del jugador (Shop/Deck Builder)
     */
    public boolean setPlayerCards(String roomCode, String userId, java.util.List<Integer> cardIds, com.TFG1.core.cards.CardRegistry registry) {
        Room room = activeRooms.get(roomCode);
        if (room == null || room.isPlaying()) return false;

        PlayerState player = room.getPlayers().get(userId);
        if (player == null) return false;

        if (cardIds.size() > 4) return false;

        int percentageCount = 0;
        int jokerCount = 0;
        int triumphCount = 0;
        java.util.Set<Integer> percentageFaces = new java.util.HashSet<>();

        for (Integer id : cardIds) {
            com.TFG1.core.cards.Card c = registry.getCardById(id);
            if (c == null) return false;

            if (c.type() == com.TFG1.core.cards.CardType.PALO) {
                if (c.value() >= 1 && c.value() <= 6) {
                    percentageCount++;
                    if (!percentageFaces.add(c.value())) {
                        return false;
                    }
                } else if (c.value() == 7) {

                    jokerCount++;
                }
            } else if (c.type() == com.TFG1.core.cards.CardType.JOKER) {
                jokerCount++;
            } else if (c.type() == com.TFG1.core.cards.CardType.TRIUNFO) {
                triumphCount++;
            }
        }

        if (percentageCount > 2 || jokerCount > 1 || triumphCount > 1) return false;

        player.setSelectedCards(cardIds);
        return true;
    }

    /**
     * Se habilita el inicio solo cuando el Host avisa A su vez valido que TODOS respondan 'Ready/isReady' a true
     */
    public boolean startGame(String roomCode, String requestUserId) {
        Room room = activeRooms.get(roomCode);
        if (room == null) return false;

        PlayerState requester = room.getPlayers().get(requestUserId);

        if (requester == null || !requester.isHost()) return false;

        if (room.getPlayers().size() < 2) return false;

        for (PlayerState player : room.getPlayers().values()) {

            if (!player.isReady() && !player.isHost()) {
                return false;
            }
        }
        room.setPlaying(true);
        return true;
    }

    /**
     * Algoritmo de Generacion para Codigos Random usando iteracion simple alfanumerica
     */
    private String generateRandomCode(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public void recordMatchResult(String userId, boolean won, String details) {
        UserStats stats = userStatsRepository.findById(userId);
        if (stats == null) {
            stats = new UserStats(userId);
        }
        if (won) {
            stats.addWin(details);
        } else {
            stats.addLoss(details);
        }
        userStatsRepository.saveOrUpdate(stats);

        com.TFG1.model.User dbUser = userRepository.findByUsername(userId);
        if (dbUser != null) {
            if (won) {
                dbUser.setCoins(dbUser.getCoins() + 15);
            } else {

                dbUser.setCoins(Math.max(0, dbUser.getCoins() - 15));
            }
            userRepository.update(dbUser);
        }
    }

    public UserStats getUserStats(String userId) {
        UserStats stats = userStatsRepository.findById(userId);
        if (stats == null) {
            stats = new UserStats(userId);
            userStatsRepository.saveOrUpdate(stats);
        }
        return stats;
    }
}
