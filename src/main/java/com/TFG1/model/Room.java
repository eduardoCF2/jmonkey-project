package com.TFG1.model;

import java.util.concurrent.ConcurrentHashMap;
import com.TFG1.core.engine.GameManager;

/**
 * Representa una sala de juego multijugador privada
 * Aqui se agrupan las caracteristicas de la sala (su codigo publico) y los jugadores que tengo presentes
 */
public class Room {
    private String roomCode;

    private ConcurrentHashMap<String, PlayerState> players;

    private boolean isPlaying;
    private GameManager gameManager;

    public Room(String roomCode) {
        this.roomCode = roomCode;
        this.players = new ConcurrentHashMap<>();
        this.isPlaying = false;
        this.gameManager = new GameManager();
    }

    public void addPlayer(PlayerState player) {
        players.put(player.getUserId(), player);
    }

    public String getRoomCode() { return roomCode; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }

    public ConcurrentHashMap<String, PlayerState> getPlayers() { return players; }
    public void setPlayers(ConcurrentHashMap<String, PlayerState> players) { this.players = players; }

    public boolean isPlaying() { return isPlaying; }
    public void setPlaying(boolean isPlaying) { this.isPlaying = isPlaying; }

    public GameManager getGameManager() { return gameManager; }
}
