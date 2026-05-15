package com.TFG1.model;

/**
 * Representa el estado de un jugador especifico dentro del contexto de una sala de juego
 * Sirve para saber variables concretas que solo me importan mientras el jugador esta en el Lobby de mi sala
 */
public class PlayerState {
    private String userId;   // El ID o Nombre de Usuario real del jugador
    private boolean isHost;  // Indica si este jugador es el creador de la sala (quien tiene permisos para iniciarla)
    private boolean isReady; // Indica si el jugador ha marcado que está "Listo" para empezar (RF-07)
    private java.util.List<Integer> selectedCards = new java.util.ArrayList<>();

    public PlayerState() {}

    public PlayerState(String userId, boolean isHost) {
        this.userId = userId;
        this.isHost = isHost;
        this.isReady = false;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public boolean isHost() { return isHost; }
    public void setHost(boolean isHost) { this.isHost = isHost; }

    public boolean isReady() { return isReady; }
    public void setReady(boolean isReady) { this.isReady = isReady; }

    public java.util.List<Integer> getSelectedCards() { return selectedCards; }
    public void setSelectedCards(java.util.List<Integer> selectedCards) { this.selectedCards = selectedCards; }
}
