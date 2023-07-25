package com.swrobotics.lib.motor;

import com.swrobotics.lib.encoder.Encoder;

/**
 * Abstraction for a motor.
 *
 * <p>Clockwise and counterclockwise are from the perspective of looking at the face of the motor,
 * with the shaft toward you.
 */
public interface Motor {
    // Required features
    void setPercentOut(double percent);
    default void stop() { setPercentOut(0); }
    void setInverted(boolean inverted);

    // Optional features
    default void setBrakeMode(boolean brake) { throw new UnsupportedOperationException(); }
    default void follow(Motor leader) { throw new UnsupportedOperationException(); }
    default Encoder getIntegratedEncoder() { throw new UnsupportedOperationException(); }
    default void useExternalEncoder(Encoder encoder) {
        throw new UnsupportedOperationException();
    }
    default PIDControlFeature getPIDControl() { throw new UnsupportedOperationException(); }
}
