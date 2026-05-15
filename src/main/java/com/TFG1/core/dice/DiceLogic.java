package com.TFG1.core.dice;

public class DiceLogic {

    public boolean isValid(Bid newBid, Bid previousBid, int totalDiceInGame) {

        if (newBid.quantity() > totalDiceInGame || newBid.quantity() <= 0) {
            return false;
        }

        if (newBid.value() < 1 && newBid.value() > 6) {
            return false;
        }

        if (previousBid == null) {
            return newBid.value() >= 1 && newBid.value() <= 6;
        }

        if (newBid.value() != previousBid.value()) {
            return false;
        }

        return newBid.quantity() > previousBid.quantity();
    }

}
