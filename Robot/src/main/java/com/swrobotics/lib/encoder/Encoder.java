package com.swrobotics.lib.encoder;

import com.swrobotics.mathlib.Angle;

// TODO: Velocity maybe?
public interface Encoder {
    Angle getAngle();

    Angle getVelocity();

    default void setAngle(Angle angle) {
        throw new UnsupportedOperationException();
    }

    default void setInverted(boolean inverted) {
        throw new UnsupportedOperationException();
    }
}
