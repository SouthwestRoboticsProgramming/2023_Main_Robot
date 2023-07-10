package com.swrobotics.lib.input;

import java.util.function.Supplier;

/**
 * Represents an analog input. Most axes will have an output from either -1 to 1, or 0 to 1. Note
 * that most analog inputs have a slight "drift", where the value will not be exactly zero when not
 * touching the input.
 */
public final class InputAxis implements InputElement {
    private final Supplier<Double> getter;
    private double value;

    /**
     * Creates a new input axis that reads its value from a given getter function.
     *
     * @param getter value getter
     */
    public InputAxis(Supplier<Double> getter) {
        this.getter = getter;
        value = getter.get();
    }

    /**
     * Gets the current position of this axis.
     *
     * @return value
     */
    public double get() {
        return value;
    }

    @Override
    public void update() {
        value = getter.get();
    }
}
