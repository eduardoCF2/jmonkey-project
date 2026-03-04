package com.TFG1.core.dice;

import java.util.Random;

public class Die {

    private int value;
    private final Random random;

    public Die() {
        this.random = new Random();
        roll();
    }

    public void roll() {
        this.value = random.nextInt(6) + 1;
    }

    public int getValue() {
        return this.value;
    }

    @Override

    public String toString() {
        return "[" + value + "]";
    }

}
