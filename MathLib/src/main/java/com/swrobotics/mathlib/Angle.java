package com.swrobotics.mathlib;

import edu.wpi.first.math.geometry.Rotation2d;

/**
 * Represents an angle measurement. All instances of this interface should be immutable (they cannot
 * change value).
 */
public interface Angle {
    /** An angle with measure zero. */
    Angle ZERO = CCWAngle.rad(0);

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

    default Angle add(Angle o) { return ccw().add(o.ccw()); }
    default Angle sub(Angle o) { return ccw().sub(o.ccw()); }
    default Angle mul(double scalar) { return ccw().mul(scalar); }
    default Angle div(double scalar) { return ccw().div(scalar); }

    static Angle fromRotation2d(Rotation2d rot) {
        return CCWAngle.rad(rot.getRadians());
    }
}
