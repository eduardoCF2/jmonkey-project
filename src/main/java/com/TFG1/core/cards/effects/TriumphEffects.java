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

            // Print cups BEFORE swap
            StringBuilder sbPlayerBefore = new StringBuilder();
            for (Die d : playerCup) sbPlayerBefore.append(d.getValue()).append(" ");
            StringBuilder sbTargetBefore = new StringBuilder();
            for (Die d : targetCup) sbTargetBefore.append(d.getValue()).append(" ");

            System.out.println("[DEBUG CABALLO] ANTES DEL INTERCAMBIO:");
            System.out.println(" > Cubilete de " + player.getName() + " (Emisor): [ " + sbPlayerBefore.toString() + "]");
            System.out.println(" > Cubilete de " + target.getName() + " (Objetivo): [ " + sbTargetBefore.toString() + "]");

            int playerDieIdx = random.nextInt(playerCup.size());
            int targetDieIdx = random.nextInt(targetCup.size());

            Die playerDie = playerCup.get(playerDieIdx);
            Die targetDie = targetCup.get(targetDieIdx);

            System.out.println(" -> Intercambiando dado en indice " + playerDieIdx + " de " + player.getName() + " (Valor=" + playerDie.getValue() + ") con dado en indice " + targetDieIdx + " de " + target.getName() + " (Valor=" + targetDie.getValue() + ")");

            // Intercambiar valores de cara
            int tempVal = playerDie.getValue();
            playerDie.setFace(targetDie.getValue());
            targetDie.setFace(tempVal);

            // Intercambiar pesos de probabilidad
            double[] tempWeights = new double[6];
            System.arraycopy(playerDie.getWeights(), 0, tempWeights, 0, 6);
            playerDie.setWeights(targetDie.getWeights());
            targetDie.setWeights(tempWeights);

            // Print cups AFTER swap
            StringBuilder sbPlayerAfter = new StringBuilder();
            for (Die d : playerCup) sbPlayerAfter.append(d.getValue()).append(" ");
            StringBuilder sbTargetAfter = new StringBuilder();
            for (Die d : targetCup) sbTargetAfter.append(d.getValue()).append(" ");

            System.out.println("[DEBUG CABALLO] DESPUES DEL INTERCAMBIO:");
            System.out.println(" > Cubilete de " + player.getName() + " (Emisor): [ " + sbPlayerAfter.toString() + "]");
            System.out.println(" > Cubilete de " + target.getName() + " (Objetivo): [ " + sbTargetAfter.toString() + "]");
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
