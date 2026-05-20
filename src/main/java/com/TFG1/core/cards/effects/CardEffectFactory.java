package com.TFG1.core.cards.effects;

import com.TFG1.core.cards.Card;

/**
 * Factory pattern para obtener la estrategia de efecto de una carta
 */
public class CardEffectFactory {

    public static CardEffectStrategy getStrategy(Card card) {
        if (card == null) return null;

        switch (card.type()) {
            case PALO:
                if (card.value() >= 1 && card.value() <= 6) {
                    return new PercentageEffectStrategy();
                } else if (card.value() == 7) {
                    return new ComodinEffectStrategy();
                }
                break;

            case TRIUNFO:
                if (card.value() == 10) {
                    return new TriumphEffects.SotaStrategy();
                } else if (card.value() == 11) {
                    return new TriumphEffects.CaballoStrategy();
                } else if (card.value() == 12) {
                    return new TriumphEffects.ReyStrategy();
                }
                break;

            case JOKER:
                switch (card.suit()) {
                    case BASTOS:
                        return new ChaosJokerEffects.JokerBastosStrategy();
                    case COPAS:
                        return new ChaosJokerEffects.JokerCopasStrategy();
                    case ESPADAS:
                        return new ChaosJokerEffects.JokerEspadasStrategy();
                    case OROS:
                        return new ChaosJokerEffects.JokerOrosStrategy();
                }
                break;
        }

        return null;
    }
}
