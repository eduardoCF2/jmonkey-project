package com.TFG1.core.cards;

public record Card(int id, String name, Suit suit, CardType type, int value) {

    // id -> identificar cartas
    // name -> nombre de la carta
    // suit -> Palo de la carta
    // type -> Tipo de la carta
    // value -> Valor de la carta
}
