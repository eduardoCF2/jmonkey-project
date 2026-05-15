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
 * Clase para registrar las estadisticas de un usuario
 * Reemplazo de forma temporal el uso de base de datos para guardar el winrate y mi historial
 */
@Entity
@Table(name = "user_stats")
public class UserStats {
    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "wins")
    private int wins;

    @Column(name = "losses")
    private int losses;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_last_matches", joinColumns = @JoinColumn(name = "user_id"))
    @OrderColumn(name = "match_order")
    @Column(name = "match_details")
    private List<String> lastMatches;

    public UserStats() {}

    public UserStats(String userId) {
        this.userId = userId;
        this.wins = 0;
        this.losses = 0;
        this.lastMatches = new ArrayList<>();
    }

    public void addWin(String matchDetails) {
        wins++;
        lastMatches.add(0, matchDetails);
    }

    public void addLoss(String matchDetails) {
        losses++;
        lastMatches.add(0, matchDetails);
    }

    public double getWinRatio() {
        if (losses == 0) return wins > 0 ? 100.0 : 0.0;
        return (double) wins / losses;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }
    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }
    public List<String> getLastMatches() { return lastMatches; }
    public void setLastMatches(List<String> lastMatches) { this.lastMatches = lastMatches; }
}
