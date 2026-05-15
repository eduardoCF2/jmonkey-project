package com.TFG1.core.cards.effects;

import com.TFG1.core.cards.Card;
import com.TFG1.core.dice.Die;
import com.TFG1.core.engine.GameManager;
import com.TFG1.core.engine.Player;

import java.util.List;

/**
 * Contenedor para las estrategias de los Jokers de Caos
 */
public class ChaosJokerEffects {

    /**
     * Joker Bastos: Todos los jugadores pasan un dado al jugador de su derecha
     */
    public static class JokerBastosStrategy implements CardEffectStrategy {
        @Override
        public void applyEffect(GameManager gameManager, Player player, Card card, String targetPlayerId) {
            gameManager.rotateDiceRight();
        }
    }

    /**
     * Joker Copas: Todos los dados de la mesa se vuelven a tirar
     */
    public static class JokerCopasStrategy implements CardEffectStrategy {
        @Override
        public void applyEffect(GameManager gameManager, Player player, Card card, String targetPlayerId) {
            gameManager.rerollTable();
        }
    }

    /**
     * Joker Espadas: Duelo a ciegas El jugador actual y el elegido ocultan sus dados
     */
    public static class JokerEspadasStrategy implements CardEffectStrategy {
        @Override
        public void applyEffect(GameManager gameManager, Player player, Card card, String targetPlayerId) {
            if (targetPlayerId == null) return;

            Player target = gameManager.getPlayerById(targetPlayerId);
            if (target != null && !target.isEliminated()) {
                player.setBlinded(true);
                target.setBlinded(true);
                gameManager.markBlindDuelActive();
            }
        }
    }

    /**
     * Joker Oros: El jugador con mas dados entrega automaticamente uno al que menos tiene
     */
    public static class JokerOrosStrategy implements CardEffectStrategy {
        @Override
        public void applyEffect(GameManager gameManager, Player player, Card card, String targetPlayerId) {
            List<Player> players = gameManager.getPlayers();
            Player maxDicePlayer = null;
            Player minDicePlayer = null;

            for (Player p : players) {
                if (p.isEliminated()) continue;
                if (maxDicePlayer == null || p.cup().size() > maxDicePlayer.cup().size()) {
                    maxDicePlayer = p;
                }
                if (minDicePlayer == null || p.cup().size() < minDicePlayer.cup().size()) {
                    minDicePlayer = p;
                }
            }

            if (maxDicePlayer != null && minDicePlayer != null && maxDicePlayer != minDicePlayer && maxDicePlayer.cup().size() > 0) {
                Die dieToTransfer = maxDicePlayer.cup().remove(maxDicePlayer.cup().size() - 1);
                minDicePlayer.cup().add(dieToTransfer);
            }
        }
    }
}
