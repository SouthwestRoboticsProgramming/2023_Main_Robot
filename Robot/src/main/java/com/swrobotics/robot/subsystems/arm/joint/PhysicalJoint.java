package com.swrobotics.robot.subsystems.arm.joint;

import com.ctre.phoenix.sensors.*;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel;
import com.revrobotics.RelativeEncoder;
import com.swrobotics.lib.net.NTDouble;
import com.swrobotics.mathlib.MathUtil;
import com.swrobotics.robot.subsystems.arm.ArmSubsystem;
import edu.wpi.first.wpilibj.DriverStation;

public final class PhysicalJoint implements ArmJoint {
    private final CANSparkMax motor;
    private final RelativeEncoder encoder;
    private final CANCoder canCoder;

    private final double gearRatio;
    private final double flip;

    private double encoderOffset;
    private final NTDouble canCoderOffset;

    public PhysicalJoint(int motorId, int canCoderId, double gearRatio, NTDouble canCoderOffset, boolean inverted) {
        motor = new CANSparkMax(motorId, CANSparkMaxLowLevel.MotorType.kBrushless);
        encoder = motor.getEncoder();
        this.gearRatio = gearRatio;
        this.flip = inverted ? -1 : 1;

        // Make encoder position be in rotations
        encoder.setPositionConversionFactor(1);

        canCoder = new CANCoder(canCoderId);
        CANCoderConfiguration config = new CANCoderConfiguration();
        config.initializationStrategy = SensorInitializationStrategy.BootToAbsolutePosition;
        config.absoluteSensorRange = AbsoluteSensorRange.Signed_PlusMinus180;
        config.sensorTimeBase = SensorTimeBase.PerSecond;
        canCoder.configAllSettings(config);

        this.canCoderOffset = canCoderOffset;
    }

    private double getRawEncoderPos() {
        return flip * encoder.getPosition();
    }

    private double getEncoderPos() {
        return getRawEncoderPos() + encoderOffset;
    }

    // Gets the offset from home measured by the CANCoder, in joint radians
    private double getCanCoderAngle() {
        double raw = canCoder.getAbsolutePosition();
        double offset = canCoderOffset.get();

        double relative = MathUtil.wrap(raw + offset, -180, 180);
        return relative / ArmSubsystem.JOINT_TO_CANCODER_RATIO;
    }

    @Override
    public void calibrateCanCoder(double homeAngle) {
        double current = getCurrentAngle();
        double diff = current - homeAngle;

        double diffCanCoder = diff * ArmSubsystem.JOINT_TO_CANCODER_RATIO;
        if (Math.abs(diffCanCoder) > 180)
            DriverStation.reportWarning("Arm is too far from home for CANCoder to work properly!", false);

        canCoderOffset.set(diffCanCoder);
    }

    @Override
    public double getCurrentAngle() {
        return getEncoderPos() / gearRatio * 2 * Math.PI;
    }

    @Override
    public void calibrateHome(double homeAngle) {
        // Calculate where the arm is, assuming it's relatively close
        // to the home position
        double actualAngle = homeAngle + getCanCoderAngle();

        double actualPos = getRawEncoderPos();
        double expectedPos = actualAngle / (2 * Math.PI) * gearRatio;
        encoderOffset = expectedPos - actualPos;
    }

    @Override
    public void setMotorOutput(double out) {
        motor.set(out * flip);
    }
}
