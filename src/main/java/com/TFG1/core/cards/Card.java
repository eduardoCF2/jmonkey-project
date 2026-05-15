package com.TFG1.core.cards;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Card(
    @JsonProperty("id") int id,
    @JsonProperty("name") String name,
    @JsonProperty("suit") Suit suit,
    @JsonProperty("type") CardType type,
    @JsonProperty("value") int value,
    @JsonProperty("isConsumable") boolean isConsumable,
    @JsonProperty("price") int price) {

}
