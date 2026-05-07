package com.TFG1.service;

import com.TFG1.model.PlayerState;
import com.TFG1.model.Room;
import com.TFG1.model.UserStats;
import com.TFG1.repository.UserStatsRepository;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Capa de Servicios: Aquí se concentra "Lógica de Negocio de Procesamiento" de las Salas y emparejamiento.
 * Se manejan internamente las estructuras con diccionarios al no tener Base de Datos temporalmente.
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
     *Un jugador (Host) puede crear una sala en el sistema usando un código único generado.
     */
    public String createRoom(String hostUserId) {
        String roomCode = generateRandomCode(6); // Genero la ID alfanumérica de tamaño 6
        Room newRoom = new Room(roomCode); // Fabrico e instancio mi nueva sala
        
        // Al creador de la sala lo incorporo automáticamente y le doy poder de Host ('true')
        PlayerState hostPlayer = new PlayerState(hostUserId, true);
        newRoom.addPlayer(hostPlayer); 
        
        // Guardo y empaqueto mi sala en la lista maestra de salas activas
        activeRooms.put(roomCode, newRoom);
        return roomCode;
    }

    /**
     *Los jugadores se unen manualmente y bloqueo mi sala a un máximo de 4 personas.
     */
    public boolean joinRoom(String roomCode, String userId) {
        Room room = activeRooms.get(roomCode); // Busco en mi HashMap si existe por su código

        // Si mi sala no existe o su estado ya es Playing, no le doy el pase
        if (room == null || room.isPlaying()) return false;
        
        // Evito que entren más si ya hay 4 usuarios en mi Room.
        if (room.getPlayers().size() >= 4) return false;
        
        // Admito al invitado con el rol pasivo poniendo su parámetro Host en falso.
        room.addPlayer(new PlayerState(userId, false));
        return true;
    }

    /**
     * Actualizo y sincronizo el estado real de mi jugador de si está 'Listo/No Listo'.
     */
    public void setPlayerReady(String roomCode, String userId, boolean isReady) {
        Room room = activeRooms.get(roomCode);
        // Me aseguro de que no he borrado la sala y que el jugador esté dentro antes de cambiarle el estado
        if (room != null && room.getPlayers().containsKey(userId)) {
            room.getPlayers().get(userId).setReady(isReady);
        }
    }

    /**
     * Se habilita el inicio solo cuando el Host avisa. A su vez valido que TODOS respondan 'Ready/isReady' a true.
     */
    public boolean startGame(String roomCode, String requestUserId) {
        Room room = activeRooms.get(roomCode);
        if (room == null) return false;
        
        // Capto el objeto PlayerState de quien me está pidiendo arrancar la partida
        PlayerState requester = room.getPlayers().get(requestUserId);
        
        //Solo le dejo iniciar a este requester si coincide con el Host.
        if (requester == null || !requester.isHost()) return false;
        
        //Evito el fallo si estás solo, pido al menos 2 personas para iniciar.
        if (room.getPlayers().size() < 2) return false;
        
        // Se revisa a todos los jugadores que tengo anotados en la estancia
        for (PlayerState player : room.getPlayers().values()) {
            // Si un solo participante no está Ready y no es el Host, rechazo arrancar.
            if (!player.isReady() && !player.isHost()) { 
                return false;
            }
        }
        room.setPlaying(true); // Cierro mi sala. Quien llame después para unirse recibirá error.
        return true;
    }

    /**
     * Algoritmo de Generación para Códigos Random usando iteración simple alfanumérica.
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
    
    // ------ SECCIÓN INFERIOR PROVISIONAL DE STATS MEMORIA ------
    
    // Grabo o documento mi Match en el historial y guardo un string descriptivo (details).
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
        
        // Sumar o restar monedas al jugador
        com.TFG1.model.User dbUser = userRepository.findByUsername(userId);
        if (dbUser != null) {
            if (won) {
                dbUser.setCoins(dbUser.getCoins() + 15);
            } else {
                // No permitimos saldo negativo
                dbUser.setCoins(Math.max(0, dbUser.getCoins() - 15));
            }
            userRepository.update(dbUser);
        }
    }
    
    // Obtengo la información para mi Perfil directamente de mi diccionario en caché.
    public UserStats getUserStats(String userId) {
        UserStats stats = userStatsRepository.findById(userId);
        if (stats == null) {
            stats = new UserStats(userId);
            userStatsRepository.saveOrUpdate(stats);
        }
        return stats;
    }
}
