package com.swrobotics.lib.gyro;

import com.swrobotics.mathlib.Angle;

import edu.wpi.first.wpilibj.RobotBase;

/** Represents a gyroscope with at least one axis. */
public abstract class Gyroscope {
    private Angle offset;
    private Angle simAngle;

    public Gyroscope() {
        offset = Angle.ZERO;
        simAngle = Angle.ZERO;
    }

    /**
     * Calibrates the gyro using its integrated calibration routine. This may take a few seconds, so
     * don't call this in a periodic function! Also, make sure the robot is not moving when you call
     * this method.
     */
    public abstract void calibrate();

    /**
     * Gets the current angle of the gyroscope.
     *
     * @return current angle
     */
    public Angle getAngle() {
        return getCurrentAngle().ccw().add(offset.ccw()).wrapDeg(0, 360);
    }

    /**
     * Sets the current angle of the gyroscope to a new angle.
     *
     * @param newAngle new current angle
     */
    public void setAngle(Angle newAngle) {
        offset = newAngle.sub(getCurrentAngle());
        System.out.println("GYRO NEW ANGLE: " + newAngle + " | " + getAngle());
    }

    private Angle getCurrentAngle() {
        if (RobotBase.isSimulation()) {
            return simAngle;
        } else {
            return getRawAngle();
        }
    }

    /**
     * Gets the raw angle from the gyroscope, without the offset applied.
     *
     * @return raw angle
     */
    protected abstract Angle getRawAngle();

    /**
     * Gets the current raw simulated angle.
     *
     * @return simulated angle
     */
    public Angle getSimAngle() {
        return simAngle;
    }

    /**
     * Sets the current raw simulated angle. This has no effect if the code is not running in a
     * simulation.
     *
     * @param simAngle simulated angle
     */
    public void setSimAngle(Angle simAngle) {
        this.simAngle = simAngle;
    }
}
