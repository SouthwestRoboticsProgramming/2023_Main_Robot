package com.swrobotics.robot.subsystems.intake;

import com.swrobotics.lib.net.NTDouble;

public enum GamePiece {
    CONE(
        new NTDouble("Intake/Cone Intake Speed", -0.2),
        new NTDouble("Intake/Cone Outtake Speed", 0.2)),
    CUBE(
        new NTDouble("Intake/Cube Intake Speed", 0.2),
        new NTDouble("Intake/Cube Outtake Speed", -0.2)
    );

    private final NTDouble intakeSpeed;
    private final NTDouble outtakeSpeed;

    // Direction should be 1 (forward) or -1 (backward)
    GamePiece(NTDouble intakeSpeed, NTDouble outtakeSpeed) {
        this.intakeSpeed = intakeSpeed;
        this.outtakeSpeed = outtakeSpeed;
    }

    public double getIntakeOutput() {
        return intakeSpeed.get();
    }

    public double getOuttakeOutput() {
        return outtakeSpeed.get();
    }
}
