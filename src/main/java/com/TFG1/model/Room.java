package com.TFG1.model;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Representa una sala de juego multijugador privada.
 * Aquí se agrupan las características de la sala (su código público) y los jugadores que tengo presentes.
 */
public class Room {
    private String roomCode; // El código alfanumérico único para invitar a otras personas
    
    // Clave: El userId del jugador. Valor: El PlayerState con sus datos temporales en mi sala.
    private ConcurrentHashMap<String, PlayerState> players; 
    
    private boolean isPlaying; // Determina si ya he cerrado admisiones e iniciado el juego o si sigo en el Lobby

    public Room(String roomCode) {
        this.roomCode = roomCode;
        this.players = new ConcurrentHashMap<>(); // Instancio el diccionario de jugadores, vacío al inicio
        this.isPlaying = false; // Al crearla, la pongo siempre en fase Lobby
    }

    // Método de asistencia rápida que utilizo para introducir nuevos jugadores al diccionario de mi sala
    public void addPlayer(PlayerState player) {
        players.put(player.getUserId(), player);
    }
    
    // --- Getters y Setters ---
    public String getRoomCode() { return roomCode; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }
    
    public ConcurrentHashMap<String, PlayerState> getPlayers() { return players; }
    public void setPlayers(ConcurrentHashMap<String, PlayerState> players) { this.players = players; }
    
    public boolean isPlaying() { return isPlaying; }
    public void setPlaying(boolean isPlaying) { this.isPlaying = isPlaying; }
}
