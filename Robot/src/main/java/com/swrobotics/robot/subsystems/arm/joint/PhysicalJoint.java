package com.swrobotics.robot.subsystems.arm.joint;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel;
import com.revrobotics.RelativeEncoder;

public final class PhysicalJoint implements ArmJoint {
    private final CANSparkMax motor;
    private final RelativeEncoder encoder;
    private final double gearRatio;

    public PhysicalJoint(int canId, double gearRatio) {
        motor = new CANSparkMax(canId, CANSparkMaxLowLevel.MotorType.kBrushless);
        encoder = motor.getEncoder();
        this.gearRatio = gearRatio;

        // Make encoder position be in rotations
        encoder.setPositionConversionFactor(1);
    }

    @Override
    public double getCurrentAngle() {
        return encoder.getPosition() / gearRatio * 2 * Math.PI;
    }

    @Override
    public void setCurrentAngle(double angle) {
        encoder.setPosition(angle / (2 * Math.PI) * gearRatio);
    }

    @Override
    public void setMotorOutput(double out) {
        motor.set(out);
    }
}
