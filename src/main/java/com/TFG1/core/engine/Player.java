package com.TFG1.core.engine;

import com.TFG1.core.dice.Die;
import com.TFG1.core.cards.Card;
import java.util.ArrayList;
import java.util.List;

public class Player {

    private int id; // id del jugador
    private final String name; // Nombre del jugador
    private final List<Die> cup; // Dados ocultos del jugador
    private final List<Card> hand; // Cartas del jugador

    // public Player() {
    // }

    public Player(int id, String name, int initialDiceCount) {
        this.id = id;
        this.name = name;
        this.cup = new ArrayList<>();
        this.hand = new ArrayList<>();

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

    public int getId() {
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
}
