package com.swrobotics.robot.subsystems.intake;

import com.swrobotics.lib.net.NTEntry;
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

    private final NTEntry<Double> intakeSpeed;
    private final NTEntry<Double> outtakeSpeed;
    private final NTEntry<Double> holdSpeed;

    // Direction should be 1 (forward) or -1 (backward)
    GamePiece(NTEntry<Double> intakeSpeed, NTEntry<Double> outtakeSpeed, NTEntry<Double> holdSpeed) {
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
