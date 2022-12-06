package com.swrobotics.lib.motor.calc;

import com.swrobotics.lib.wpilib.AbstractRobot;
import com.swrobotics.mathlib.Angle;

import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.trajectory.TrapezoidProfile.Constraints;

public class ProfiledPIDCalculator extends ProfiledPIDController implements PositionCalculator, VelocityCalculator {

    public ProfiledPIDCalculator(double Kp, double Ki, double Kd, Constraints constraints) {
        super(Kp, Ki, Kd, constraints, 1 / AbstractRobot.get().getPeriodicPerSecond());
    }

    @Override
    public void reset() {} // Not enough information to properly reset TODO

    @Override
    public double calculate(Angle currentAngle, Angle targetAngle) {
        return calculate(currentAngle.ccw().rad(), targetAngle.ccw().rad());
    }
    
}
