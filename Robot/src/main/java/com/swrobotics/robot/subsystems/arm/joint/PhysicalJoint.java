package com.swrobotics.robot.subsystems.arm.joint;

import com.ctre.phoenix.sensors.*;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel;
import com.revrobotics.RelativeEncoder;
import com.swrobotics.lib.net.NTDouble;
import com.swrobotics.lib.net.NTEntry;
import com.swrobotics.mathlib.MathUtil;
import com.swrobotics.robot.subsystems.arm.ArmSubsystem;

// Calibrating CANCoder:
//   Assume arm is physically in home position
//   Zero CANCoders (zero on cancoder = home position)

// Calibrating home:
//   Assume arm is within 60 degrees of home so CANCoders work
//   Set encoder to scaled current angle of home position + encoder angle

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
        motor.setIdleMode(CANSparkMax.IdleMode.kBrake);
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

        test = new NTDouble("Log/Arm/CanCoder " + canCoderId, -123).setTemporary();
    }

    private double getRawEncoderPos() { return flip * encoder.getPosition(); }
    private double getEncoderPos() { return getRawEncoderPos() + encoderOffset; }

    private double getRawCanCoderPos() { return -flip * canCoder.getAbsolutePosition(); }
    private double getCanCoderPos() { return MathUtil.wrap(getRawCanCoderPos() + canCoderOffset.get(), -180, 180) / ArmSubsystem.JOINT_TO_CANCODER_RATIO; }

    // Called when specified in NT
    @Override
    public void calibrateCanCoder() {
        canCoderOffset.set(-getRawCanCoderPos());
    }

    @Override
    public double getCurrentAngle() {
        return getEncoderPos() / gearRatio * 2 * Math.PI;
    }

    // Called on startup
    @Override
    public void calibrateHome(double homeAngle) {
        double actualAngle = homeAngle - Math.toRadians(getCanCoderPos());

        double actualPos = getRawEncoderPos();
        double expectedPos = actualAngle / (2 * Math.PI) * gearRatio;
        encoderOffset = expectedPos - actualPos;
    }

    private final NTEntry<Double> test;

    @Override
    public void setMotorOutput(double out) {
        test.set(getCanCoderPos());
        motor.set(out * flip);
    }
}
