package com.TFG1.core.cards.effects;

import com.TFG1.core.cards.Card;
import com.TFG1.core.dice.Die;
import com.TFG1.core.engine.GameManager;
import com.TFG1.core.engine.Player;

import java.util.List;
import java.util.Random;

/**
 * Contenedor para las estrategias de las cartas de Triunfo (10, 11, 12)
 */
public class TriumphEffects {

    private static final Random random = new Random();

    /**
     * Sota (10): Reinicia la tirada actual
     */
    public static class SotaStrategy implements CardEffectStrategy {
        @Override
        public void applyEffect(GameManager gameManager, Player player, Card card, String targetPlayerId) {
            gameManager.rerollTable();
        }
    }

    /**
     * Caballo (11): Intercambia un dado aleatorio del jugador con un dado aleatorio del oponente
     */
    public static class CaballoStrategy implements CardEffectStrategy {
        @Override
        public void applyEffect(GameManager gameManager, Player player, Card card, String targetPlayerId) {
            if (targetPlayerId == null) return;

            Player target = gameManager.getPlayerById(targetPlayerId);
            if (target == null || target.isEliminated() || player.cup().isEmpty() || target.cup().isEmpty()) return;

            List<Die> playerCup = player.cup();
            List<Die> targetCup = target.cup();

            int playerDieIdx = random.nextInt(playerCup.size());
            int targetDieIdx = random.nextInt(targetCup.size());

            Die temp = playerCup.get(playerDieIdx);
            playerCup.set(playerDieIdx, targetCup.get(targetDieIdx));
            targetCup.set(targetDieIdx, temp);
        }
    }

    /**
     * Rey (12): Muestra a todos un dado aleatorio de un jugador elegido
     */
    public static class ReyStrategy implements CardEffectStrategy {
        @Override
        public void applyEffect(GameManager gameManager, Player player, Card card, String targetPlayerId) {
            if (targetPlayerId == null) return;

            Player target = gameManager.getPlayerById(targetPlayerId);
            if (target == null || target.isEliminated() || target.cup().isEmpty()) return;

            Die randomDie = target.cup().get(random.nextInt(target.cup().size()));
            gameManager.revealDie(target, randomDie);
        }
    }
}
