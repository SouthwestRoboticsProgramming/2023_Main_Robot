package com.swrobotics.robot.subsystems.arm;

import com.swrobotics.lib.net.NTAngle;
import com.swrobotics.mathlib.Angle;
import org.littletonrobotics.junction.Logger;

// Angle 0 on the wrist is the angle it naturally sits at when the robot is off
// (i.e. center of gravity is directly below the axis)
public final class WristJoint extends ArmJoint {
    public WristJoint(int motorId, int canCoderId, double canCoderToArmRatio, double motorToArmRatio, NTAngle absEncoderOffset, boolean invert) {
        super(motorId, canCoderId, canCoderToArmRatio, motorToArmRatio, absEncoderOffset, invert);
        motor.setPID(ArmSubsystem.WRIST_KP, ArmSubsystem.WRIST_KI, ArmSubsystem.WRIST_KD);
    }

    @Override
    protected Angle getCalibrationAngle(Angle home) {
        return super.getCalibrationAngle(home).ccw().wrapDeg(-180, 180);
    }

    public void setTargetAngle(Angle angle, double ff) {
        motor.setPositionArbFF(angle.mul(motorToArmRatio), ff);
        Logger.getInstance().recordOutput("Wrist/Target ccw deg", angle.ccw().deg());
        Logger.getInstance().recordOutput("Wrist/Current ccw deg", getCurrentAngle().ccw().deg());
        Logger.getInstance().recordOutput("Wrist/Motor target ccw deg", angle.mul(motorToArmRatio).ccw().deg());
        Logger.getInstance().recordOutput("Wrist/Motor current ccw deg", motorEncoder.getAngle().ccw().deg());
        Logger.getInstance().recordOutput("Wrist/Arb FF", ff);
    }
}
