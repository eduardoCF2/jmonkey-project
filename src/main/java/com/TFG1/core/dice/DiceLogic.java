package com.TFG1.core.dice;

public class DiceLogic {

    public boolean isValid(Bid newBid, Bid previousBid, int totalDiceInGame) {

        // Validar la que la apuesta se pueda hacer
        if (newBid.quantity() > totalDiceInGame || newBid.quantity() <= 0) {
            return false;
        }

        // Validar que existe el numero de caras del dado
        if (newBid.value() < 1 && newBid.value() > 6) {
            return false;
        }

        // Validar primera ronda
        if (previousBid == null) {
            return newBid.value() >= 1 && newBid.value() <= 6;
        }

        // Estructura para las apuestas:
        if (newBid.quantity() > previousBid.quantity()) {
            return true;
        } else if (newBid.quantity() == previousBid.quantity()) {
            return newBid.value() > previousBid.value();
        }
        return false;
    }

}
