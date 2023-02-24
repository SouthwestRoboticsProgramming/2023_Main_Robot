package com.swrobotics.robot.subsystems.intake;

public enum GamePiece {
    CONE(-1),
    CUBE(1);

    private final double intakeDirection;

    // Direction should be 1 (forward) or -1 (backward)
    GamePiece(double direction) {
        this.intakeDirection = direction;
    }

    public double getIntakeDirection() {
        return intakeDirection;
    }
}
