package com.swrobotics.lib.motor.calc;

import java.util.function.Supplier;

import com.swrobotics.mathlib.Angle;

import edu.wpi.first.math.controller.PIDController;

/**
 * Calculates the percent output to set a motor's position using a
 * PID controller. 
 */
public final class PIDCalculator extends PIDController implements PositionCalculator, VelocityCalculator {

    public PIDCalculator(double kp, double ki, double kd) {
        super(kp, ki, kd);
    }

    public PIDCalculator(Supplier<Double> kp, Supplier<Double> ki, Supplier<Double> kd) {
        super(kp.get(), ki.get(), kd.get());

        // TODO: Onchange
    }

    @Override
    public double calculate(Angle currentAngle, Angle targetAngle) {
        // System.out.println(currentAngle.cw().rad() + "     " + targetAngle.cw().rad());
        return this.calculate(currentAngle.cw().rad(), targetAngle.cw().rad()); // DO NOT CHANGE UNITS
    }

}
