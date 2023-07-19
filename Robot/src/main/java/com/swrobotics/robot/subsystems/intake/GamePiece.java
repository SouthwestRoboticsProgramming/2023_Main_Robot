package com.swrobotics.robot.subsystems.intake;

import com.swrobotics.lib.net.NTDouble;
import com.swrobotics.robot.config.NTData;

public enum GamePiece {
    CONE(
            NTData.INTAKE_CONE_IN_SPEED,
            NTData.INTAKE_CONE_OUT_SPEED,
            NTData.INTAKE_CONE_HOLD),
    CUBE(
            NTData.INTAKE_CUBE_IN_SPEED,
            NTData.INTAKE_CUBE_OUT_SPEED,
            NTData.INTAKE_CUBE_HOLD);

    private final NTDouble intakeSpeed;
    private final NTDouble outtakeSpeed;
    private final NTDouble holdSpeed;

    // Direction should be 1 (forward) or -1 (backward)
    GamePiece(NTDouble intakeSpeed, NTDouble outtakeSpeed, NTDouble holdSpeed) {
        this.intakeSpeed = intakeSpeed;
        this.outtakeSpeed = outtakeSpeed;
        this.holdSpeed = holdSpeed;
    }

    public double getIntakeOutput() {
        return intakeSpeed.get();
    }

    public double getOuttakeOutput() {
        return outtakeSpeed.get();
    }

    public double getHoldOutput() {
        return holdSpeed.get();
    }
}
