package com.TFG1.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase para registrar las estadísticas de un usuario.
 * Reemplazo de forma temporal el uso de base de datos para guardar el winrate y mi historial.
 */
@Entity
@Table(name = "user_stats")
public class UserStats {
    @Id
    @Column(name = "user_id")
    private String userId; // Identificador del usuario al que pertenecen las estadísticas
    
    @Column(name = "wins")
    private int wins;      // Contador de partidas ganadas
    
    @Column(name = "losses")
    private int losses;    // Contador de partidas perdidas
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_last_matches", joinColumns = @JoinColumn(name = "user_id"))
    @OrderColumn(name = "match_order")
    @Column(name = "match_details")
    private List<String> lastMatches; // Lista donde guardo mi historial reciente de partidas jugadas

    // Constructor vacío por si necesito instanciar con hibernate en el futuro
    public UserStats() {}

    // Constructor principal donde inicializo todo a 0 y dejo la lista vacía
    public UserStats(String userId) {
        this.userId = userId;
        this.wins = 0;
        this.losses = 0;
        this.lastMatches = new ArrayList<>();
    }

    // Registro una victoria sumando al contador y guardando el detalle de la partida al principio de la lista
    public void addWin(String matchDetails) {
        wins++;
        lastMatches.add(0, matchDetails); // Lo añado en la posición 0 para que sea el más reciente siempre
    }

    // Registro una derrota sumando a mi contador y guardando el detalle de la partida
    public void addLoss(String matchDetails) {
        losses++;
        lastMatches.add(0, matchDetails);
    }

    // Método con el que calculo mi porcentaje/ratio de victorias en función de las perdidas
    public double getWinRatio() {
        if (losses == 0) return wins > 0 ? 100.0 : 0.0; // Si no tengo derrotas, mi ratio es 100% o 0% 
        return (double) wins / losses; // Ratio matemático Victorias sobre Derrotas usual
    }

    // --- Getters y Setters estándar ---
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }
    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }
    public List<String> getLastMatches() { return lastMatches; }
    public void setLastMatches(List<String> lastMatches) { this.lastMatches = lastMatches; }
}
