package com.swrobotics.robot.subsystems.arm.joint;

import com.swrobotics.lib.encoder.CanCoder;
import com.swrobotics.lib.encoder.Encoder;
import com.swrobotics.lib.motor.rev.NEOMotor;
import com.swrobotics.lib.net.NTBoolean;
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

// TODO: Check if encoder directions are correct
public final class PhysicalJoint implements ArmJoint {
    private static final NTBoolean BRAKE_MODE = new NTBoolean("Arm/Brake Mode", true);

    private final NEOMotor motor;
    private final Encoder encoder;
    private final Encoder canCoder;

    private final double gearRatio;
    private final double flip;

    private double encoderOffset;
    private final NTDouble canCoderOffset;

    private NTEntry<Double> canCoderLogTest;

    public PhysicalJoint(
            int motorId,
            int canCoderId,
            double gearRatio,
            NTDouble canCoderOffset,
            boolean inverted) {
        motor = new NEOMotor(motorId);
        BRAKE_MODE.nowAndOnChange(() -> motor.setBrakeMode(BRAKE_MODE.get()));
        encoder = motor.getIntegratedEncoder();

        this.gearRatio = gearRatio;
        this.flip = inverted ? -1 : 1;

        canCoder = new CanCoder(canCoderId).getAbsolute();
        this.canCoderOffset = canCoderOffset;

        canCoderLogTest = new NTDouble("Log/Arm/CANCoder " + canCoderId + " (cw deg)", 12345).setTemporary();
    }

    private double getRawEncoderPos() {
        return flip * encoder.getAngle().ccw().rot();
    }

    private double getEncoderPos() {
        return getRawEncoderPos() + encoderOffset;
    }

    private double getRawCanCoderPos() {
        return -flip * canCoder.getAngle().ccw().deg();
    }

    private double getCanCoderPos() {
        return MathUtil.wrap(getRawCanCoderPos() + canCoderOffset.get(), -180, 180)
                / ArmSubsystem.JOINT_TO_CANCODER_RATIO;
    }

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

    @Override
    public void setMotorOutput(double out) {
        motor.setPercentOut(out * flip);
        canCoderLogTest.set(canCoder.getAngle().cw().deg());
    }
}
