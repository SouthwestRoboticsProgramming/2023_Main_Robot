package com.swrobotics.robot.subsystems.intake;

import com.swrobotics.lib.net.NTDouble;

public enum GamePiece {
    CONE(
            new NTDouble("Intake/Cone Intake Speed", 1),
            new NTDouble("Intake/Cone Outtake Speed", 0.4),
            new NTDouble("Intake/Cone Hold", 0.1)),
    CUBE(
            new NTDouble("Intake/Cube Intake Speed", -0.6),
            new NTDouble("Intake/Cube Outtake Speed", -0.4),
            new NTDouble("Intake/Cube Hold", -0.1));

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
