package com.swrobotics.lib.input;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Represents a binary input (pressed or not pressed). */
public final class InputButton implements InputElement {
    private final Supplier<Boolean> getter;
    private final List<Runnable> onRising, onFalling;
    private boolean pressed, wasPressed;

    /**
     * Creates a new input button that reads its value from a provided getter function.
     *
     * @param getter value getter
     */
    public InputButton(Supplier<Boolean> getter) {
        this.getter = getter;

        onRising = new ArrayList<>();
        onFalling = new ArrayList<>();

        pressed = wasPressed = getter.get();
    }

    /**
     * Gets whether this button is currently pressed.
     *
     * @return pressed
     */
    public boolean isPressed() {
        return pressed;
    }

    /**
     * Gets whether this button was just pressed during this periodic cycle. This is when the button
     * was not pressed the previous periodic, but is now pressed.
     *
     * @return if button was just pressed
     */
    public boolean isRising() {
        return pressed && !wasPressed;
    }

    /**
     * Gets whether this button was just released during this periodic cycle. This is when the
     * button was pressed the previous periodic, but is now not pressed.
     *
     * @return if button was just released
     */
    public boolean isFalling() {
        return !wasPressed && pressed;
    }

    /**
     * Adds a function that will be called whenever the button is pressed. This function will be
     * invoked on each periodic where {@link #isRising()} returns {@code true}.
     *
     * @param risingFn function to call
     * @return this
     */
    public InputButton onRising(Runnable risingFn) {
        onRising.add(risingFn);
        return this;
    }

    public InputButton onRising(Command command) {
        onRising(() -> CommandScheduler.getInstance().schedule(command));
        return this;
    }

    /**
     * Adds a function that will be called whenever the button is released. This function will be
     * invoked on each periodic where {@link #isFalling()} returns {@code true}.
     *
     * @param fallingFn function to call
     * @return this
     */
    public InputButton onFalling(Runnable fallingFn) {
        onFalling.add(fallingFn);
        return this;
    }

    public InputButton onFalling(Command command) {
        onFalling(() -> CommandScheduler.getInstance().schedule(command));
        return this;
    }

    @Override
    public void update() {
        wasPressed = pressed;
        pressed = getter.get();
        if (isRising()) {
            for (Runnable risingFn : onRising) {
                risingFn.run();
            }
        }
        if (isFalling()) {
            for (Runnable fallingFn : onFalling) {
                fallingFn.run();
            }
        }
    }
}
