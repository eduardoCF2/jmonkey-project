package com.TFG1.core.dice;

import java.util.Random;

public class Die {

    private int value;
    private final Random random;
    private final double[] weights;

    public Die() {
        this.random = new Random();
        this.weights = new double[6];

        for (int i = 0; i < 6; i++) {
            weights[i] = 100.0 / 6.0;
        }
        roll();
    }

    public void roll() {
        double r = random.nextDouble() * 100.0;
        double cumulative = 0.0;
        for (int i = 0; i < 6; i++) {
            cumulative += weights[i];
            if (r <= cumulative) {
                this.value = i + 1;
                return;
            }
        }
        this.value = 6;
    }

    public int getValue() {
        return this.value;
    }

    public void setFace(int face) {
        if (face >= 1 && face <= 6) {
            this.value = face;
        }
    }

    public void modifyWeight(int face, double increasePercentage) {
        if (face < 1 || face > 6) return;

        int targetIndex = face - 1;
        double currentWeight = weights[targetIndex];

        if (currentWeight + increasePercentage > 100.0) {
            increasePercentage = 100.0 - currentWeight;
        }

        weights[targetIndex] += increasePercentage;

        double decreasePerFace = increasePercentage / 5.0;
        for (int i = 0; i < 6; i++) {
            if (i != targetIndex) {
                weights[i] -= decreasePerFace;

                if (weights[i] < 0.0) {
                    weights[i] = 0.0;
                }
            }
        }

        double total = 0.0;
        for (double w : weights) total += w;
        if (total > 0) {
            for (int i = 0; i < 6; i++) {
                weights[i] = (weights[i] / total) * 100.0;
            }
        }
    }

    @Override
    public String toString() {
        return "[" + value + "]";
    }
}
