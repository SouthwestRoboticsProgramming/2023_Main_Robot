package com.swrobotics.mathlib;

import edu.wpi.first.math.geometry.Rotation2d;

/**
 * Represents an angle measurement. All instances of this interface should be immutable (they cannot
 * change value).
 */
public interface Angle {
    /** An angle with measure zero. */
    Angle ZERO = AbsoluteAngle.rad(0);

    /**
     * Gets the absolute value of this angle.
     *
     * @return absolute value
     */
    AbsoluteAngle abs();

    /**
     * Gets this angle as counterclockwise.
     *
     * @return counterclockwise
     */
    CCWAngle ccw();

    /**
     * Gets this angle as clockwise.
     *
     * @return clockwise
     */
    CWAngle cw();

    /**
     * Reverses the direction of this angle.
     *
     * @return negated angle
     */
    Angle negate();

    static Angle fromRotation2d(Rotation2d rot) {
        return CCWAngle.rad(rot.getRadians());
    }
}
