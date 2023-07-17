package com.swrobotics.lib.encoder;

import com.swrobotics.mathlib.Angle;

/** Abstraction for an encoder that can measure angle and velocity. */
public interface Encoder {
    /**
     * Gets the current measured angle of the encoder.
     *
     * @return current angle
     */
    Angle getAngle();

    /**
     * Gets the current measured velocity of the encoder.
     *
     * @return current velocity
     */
    Angle getVelocity();

    /**
     * Sets the current angle to a new value.
     *
     * @param angle new angle
     * @throws UnsupportedOperationException if this encoder's angle cannot be changed
     */
    default void setAngle(Angle angle) {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets whether the encoder's measurements should be inverted. This reverses
     * clockwise/counterclockwise direction of the outputs.
     *
     * @param inverted whether the output should be inverted
     */
    default void setInverted(boolean inverted) {
        throw new UnsupportedOperationException();
    }
}
