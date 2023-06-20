package com.swrobotics.robot.subsystems.arm;

import com.swrobotics.lib.net.NTAngle;
import com.swrobotics.mathlib.Angle;

public final class WristJoint extends ArmJoint {
    public WristJoint(int motorId, int canCoderId, double canCoderToArmRatio, double motorToArmRatio, NTAngle absEncoderOffset, boolean invert) {
        super(motorId, canCoderId, canCoderToArmRatio, motorToArmRatio, absEncoderOffset, invert);
        motor.setPID(ArmSubsystem.WRIST_KP, ArmSubsystem.WRIST_KI, ArmSubsystem.WRIST_KD);
    }

    public void setTargetAngle(Angle angle) {
        motor.setPosition(angle.mul(motorToArmRatio));
    }
}
