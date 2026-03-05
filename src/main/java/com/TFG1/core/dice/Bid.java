package com.TFG1.core.dice;

public record Bid(int quantity, int value) {

    public static boolean isValid(Bid newBid, Bid lastBid) {
        if (lastBid == null) {
            return true; // La primera apuesta siempre es válida
        }

        return newBid.quantity() > lastBid.quantity() ||
                (newBid.quantity() == lastBid.quantity() && newBid.value() > lastBid.value());
    }

}
