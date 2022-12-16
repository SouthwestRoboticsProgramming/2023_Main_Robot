package com.swrobotics.lib.motor.calc;

import com.swrobotics.mathlib.Angle;

/**
 * Implement this if you want to control your motor's position 
 * in a non-standard way.
 */
public interface PositionCalculator {
    /**
     * Resets the calculator before use.
     * This will be called when switching to the position control mode
     * from another mode.
     */
    void reset();

    /**
     * Calculates the percent output of the motor for a target angle.
     * The percent output will be clamped before use.
     * 
     * @return percent output (-1 to 1)
     */
    double calculate(Angle currentAngle, Angle targetAngle);
}
