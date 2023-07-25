package com.swrobotics.lib.motor;

import com.swrobotics.lib.encoder.Encoder;
import com.swrobotics.lib.motor.rev.SmartMotionSlot;

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
    // There's probably a better way to do this, I just haven't thought of it yet
    default void setBrakeMode(boolean brake) { throw new UnsupportedOperationException(); }
    default void follow(Motor leader) { throw new UnsupportedOperationException(); }

    default Encoder getIntegratedEncoder() { throw new UnsupportedOperationException(); }
    default void useExternalEncoder(Encoder encoder) {
        throw new UnsupportedOperationException();
    }
    default PIDControlFeature getPIDControl() { throw new UnsupportedOperationException(); }

    default int getSmartMotionSlotCount() { return 0; }
    default SmartMotionSlot getSmartMotionSlot(int index) { throw new UnsupportedOperationException(); }
}
