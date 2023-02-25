package com.swrobotics.robot.subsystems.arm.joint;

import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.wpilibj.simulation.DCMotorSim;

// Not completely physically accurate but should reflect the
// behavior of the real arm
public final class SimJoint implements ArmJoint {
    private static final double KG_PER_METER = 1;

    private final DCMotorSim motor;
    private double offset;

    public SimJoint(double length, double gearRatio) {
        // Calculate as if each segment is isolated (not accurate)
        double mass = length * KG_PER_METER;
        double moi = length * length * mass / 4;

        motor = new DCMotorSim(DCMotor.getNEO(1), gearRatio, moi);
        offset = 0;
    }

    @Override
    public double getCurrentAngle() {
        return motor.getAngularPositionRad() + offset;
    }

    @Override
    public void calibrateCanCoder(double homeAngle) {
        // Don't need to do anything
    }

    @Override
    public void calibrateHome(double homeAngle) {
        // Set the arm to be exactly at the home angle
        double current = motor.getAngularPositionRad();
        offset = homeAngle - current;
    }

    @Override
    public void setMotorOutput(double motor) {
        this.motor.setInputVoltage(motor * 12);
        this.motor.update(0.02);
    }
}
