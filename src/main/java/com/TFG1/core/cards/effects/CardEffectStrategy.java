package com.TFG1.core.cards.effects;

import com.TFG1.core.cards.Card;
import com.TFG1.core.engine.GameManager;
import com.TFG1.core.engine.Player;

/**
 * Interfaz base para el patron Strategy de los efectos de las cartas
 */
public interface CardEffectStrategy {

    /**
     * Aplica el efecto de la carta al estado del juego
     *
     * @param gameManager Instancia del motor del juego
     * @param player Jugador que lanza la carta
     * @param card La carta que se esta jugando
     * @param targetPlayerId (Opcional) ID del jugador objetivo para efectos que lo requieran (Duelo, Intercambio) Puede ser null
     */
    void applyEffect(GameManager gameManager, Player player, Card card, String targetPlayerId);
}
