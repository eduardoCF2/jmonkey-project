package com.TFG1.core.cards;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Card(
    @JsonProperty("id") int id, 
    @JsonProperty("name") String name, 
    @JsonProperty("suit") Suit suit, 
    @JsonProperty("type") CardType type, 
    @JsonProperty("value") int value) {

    // id -> identificar cartas
    // name -> nombre de la carta
    // suit -> Palo de la carta
    // type -> Tipo de la carta
    // value -> Valor de la carta
}
