package com.swrobotics.lib.gyro;

import com.swrobotics.mathlib.Angle;

/**
 * Represents a gyroscope with at least one axis.
 */
public abstract class Gyroscope {
    private Angle offset;

    public Gyroscope() {
        offset = Angle.ZERO;
    }

    /**
     * Calibrates the gyro using its integrated calibration routine. This may
     * take a few seconds, so don't call this in a periodic function! Also,
     * make sure the robot is not moving when you call this method.
     */
    public abstract void calibrate();

    /**
     * Gets the current angle of the gyroscope.
     *
     * @return current angle
     */
    public Angle getAngle() {
        return getRawAngle().ccw()
                .add(offset.ccw())
                .wrapDeg(0, 360);
    }

    /**
     * Sets the current angle of the gyroscope to a new angle.
     *
     * @param newAngle new current angle
     */
    public void setAngle(Angle newAngle) {
        offset = getRawAngle().ccw().add(newAngle.ccw());
    }

    /**
     * Gets the raw angle from the gyroscope, without the offset applied.
     *
     * @return raw angle
     */
    protected abstract Angle getRawAngle();
}
