package com.TFG1.core.cards.effects;

import com.TFG1.core.cards.Card;
import com.TFG1.core.dice.Die;
import com.TFG1.core.engine.GameManager;
import com.TFG1.core.engine.Player;

/**
 * Estrategia para las cartas del 1 al 6
 * Aumentan la probabilidad de que salga la cara correspondiente al valor de la carta
 */
public class PercentageEffectStrategy implements CardEffectStrategy {

    @Override
    public void applyEffect(GameManager gameManager, Player player, Card card, String targetPlayerId) {
        int face = card.value();
        double increment = 0.0;

        switch (card.suit()) {
            case BASTOS: increment = 5.0; break;
            case COPAS: increment = 10.0; break;
            case ESPADAS: increment = 15.0; break;
            case OROS: increment = 20.0; break;
        }

        for (Die die : player.cup()) {
            die.modifyWeight(face, increment);
            die.roll();
        }
    }
}
