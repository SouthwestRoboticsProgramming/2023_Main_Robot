package com.swrobotics.lib.motor.calc;

import com.swrobotics.mathlib.Angle;

import edu.wpi.first.math.controller.SimpleMotorFeedforward;

public class FeedforwardCalculator extends SimpleMotorFeedforward implements VelocityCalculator {

    public FeedforwardCalculator(double ks, double kv) {
        super(ks, kv);
    }

    public FeedforwardCalculator(double ks, double kv, double ka) {
        super(ks, kv, ka);
    }

    @Override
    public void reset() {} // No need to reset

    @Override
    public double calculate(Angle currentVelocity, Angle targetVelocity) {
        return calculate(targetVelocity.ccw().rad());
    }
    
}
