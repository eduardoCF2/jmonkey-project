package com.TFG1.core.cards.effects;

import com.TFG1.core.cards.Card;
import com.TFG1.core.dice.Die;
import com.TFG1.core.engine.GameManager;
import com.TFG1.core.engine.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Estrategia para las cartas 7 (Comodin)
 * Encuentra la cara mas repetida en la mesa y cambia un dado aleatorio del
 * jugador a esa cara
 */
public class ComodinEffectStrategy implements CardEffectStrategy {

    private final Random random = new Random();

    @Override
    public void applyEffect(GameManager gameManager, Player player, Card card, String targetPlayerId) {
        List<Die> allDice = gameManager.getAllDiceOnTable();
        if (allDice.isEmpty())
            return;

        Map<Integer, Integer> frequencies = new HashMap<>();
        for (Die d : allDice) {
            frequencies.put(d.getValue(), frequencies.getOrDefault(d.getValue(), 0) + 1);
        }

        int modeFace = 1;
        int maxCount = 0;
        for (Map.Entry<Integer, Integer> entry : frequencies.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                modeFace = entry.getKey();
            }
        }

        List<Die> playerCup = player.cup();
        if (!playerCup.isEmpty()) {
            Die randomDie = playerCup.get(random.nextInt(playerCup.size()));
            randomDie.setFace(modeFace);
        }
    }
}
