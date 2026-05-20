package com.TFG1.core.engine;

import com.TFG1.core.dice.Die;
import com.TFG1.core.cards.Card;
import java.util.ArrayList;
import java.util.List;

public class Player {

    private String id;
    private final String name;
    private final List<Die> cup;
    private final List<Card> hand;
    private final List<Integer> cardsToLoseOnDefeat;
    private boolean isBlinded;

    public Player(String id, String name, int initialDiceCount) {
        this.id = id;
        this.name = name;
        this.cup = new ArrayList<>();
        this.hand = new ArrayList<>();
        this.cardsToLoseOnDefeat = new ArrayList<>();

        for (int i = 0; i < initialDiceCount; i++) {
            this.cup.add(new Die());
        }
    }

    public void loseDie() {
        if (!cup.isEmpty()) {
            cup.remove(cup.size() - 1);
        }
    }

    public void eliminateFull() {
        cup.clear();
    }

    public boolean isEliminated() {
        return cup.isEmpty();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Die> cup() {
        return cup;
    }

    public List<Card> hand() {
        return hand;
    }

    public List<Integer> getCardsToLoseOnDefeat() {
        return cardsToLoseOnDefeat;
    }

    public boolean isBlinded() {
        return isBlinded;
    }

    public void setBlinded(boolean blinded) {
        this.isBlinded = blinded;
    }
}
