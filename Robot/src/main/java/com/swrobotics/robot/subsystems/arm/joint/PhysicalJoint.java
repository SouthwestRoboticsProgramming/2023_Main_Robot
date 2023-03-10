package com.swrobotics.robot.subsystems.arm.joint;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel;
import com.revrobotics.RelativeEncoder;
import edu.wpi.first.math.util.Units;

public final class PhysicalJoint implements ArmJoint {
    private final CANSparkMax motor;
    private final RelativeEncoder encoder;
    private final double gearRatio;
    private final double flip;

    private double encoderOffset;

    public PhysicalJoint(int canId, double gearRatio, boolean inverted) {
        motor = new CANSparkMax(canId, CANSparkMaxLowLevel.MotorType.kBrushless);
        motor.setIdleMode(CANSparkMax.IdleMode.kBrake);

        encoder = motor.getEncoder();
        this.gearRatio = gearRatio;
        this.flip = inverted ? -1 : 1;

        // Make encoder position be in rotations
        encoder.setPositionConversionFactor(1);
    }

    private double getRawEncoderPos() {
        return flip * encoder.getPosition();
    }

    private double getEncoderPos() {
        return getRawEncoderPos() + encoderOffset;
    }

    @Override
    public double getCurrentAngle() {
        return getEncoderPos() / gearRatio * 2 * Math.PI;
    }

    @Override
    public double getCurrentAngularVelocity() {
        double encoderVelRadPerSec = Units.rotationsPerMinuteToRadiansPerSecond(encoder.getVelocity());
        return encoderVelRadPerSec * flip / gearRatio;
    }

    @Override
    public void setCurrentAngle(double angle) {
        double actualPos = getRawEncoderPos();
        double expectedPos = angle / (2 * Math.PI) * gearRatio;
        encoderOffset = expectedPos - actualPos;
    }

    @Override
    public void setMotorOutput(double out) {
        motor.set(out * flip);
    }
}
