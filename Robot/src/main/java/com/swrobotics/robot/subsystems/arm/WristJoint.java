package com.swrobotics.robot.subsystems.arm;

import com.swrobotics.lib.net.NTEntry;
import com.swrobotics.mathlib.Angle;
import com.swrobotics.robot.config.NTData;
import org.littletonrobotics.junction.Logger;

public final class WristJoint extends ArmJoint {
    public WristJoint(int motorId, int canCoderId, double canCoderToArmRatio, double motorToArmRatio, NTEntry<Angle> absEncoderOffset, boolean invert) {
        super(motorId, canCoderId, canCoderToArmRatio, motorToArmRatio, absEncoderOffset, invert);
        motor.setPID(NTData.ARM_WRIST_KP, NTData.ARM_WRIST_KI, NTData.ARM_WRIST_KD);
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
