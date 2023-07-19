package com.swrobotics.robot.subsystems.intake;

import com.swrobotics.lib.motor.Motor;
import com.swrobotics.lib.motor.rev.NEOMotor;
import com.swrobotics.lib.schedule.SwitchableSubsystemBase;
import com.swrobotics.robot.config.CANAllocation;
import org.littletonrobotics.junction.Logger;

public final class IntakeSubsystem extends SwitchableSubsystemBase {
    public enum Mode {
        INTAKE,
        EJECT,
        OFF
    }

    private final Motor motor;
    private GamePiece heldPiece;

    public IntakeSubsystem() {
        motor = new NEOMotor(CANAllocation.INTAKE_MOTOR);

        // TODO-Mason: Can we assume we'll always start with a cube?
        heldPiece = GamePiece.CUBE;
    }

    public void set(Mode mode, GamePiece gamePiece) {
        if (!isEnabled())
            return;

        Logger.getInstance().recordOutput("Intake/Mode", mode.toString());
        Logger.getInstance().recordOutput("Intake/Game Piece", gamePiece.toString());
        Logger.getInstance().recordOutput("Intake/Held Piece", heldPiece.toString());

        double out = 0;
        switch (mode) {
            case INTAKE:
                out = gamePiece.getIntakeOutput();
                heldPiece = gamePiece;
                break;
            case EJECT:
                out = gamePiece.getOuttakeOutput();
                break;
            case OFF:
                out = gamePiece.getHoldOutput();
                break;
        }
        Logger.getInstance().recordOutput("Intake/Output", out);

        motor.setPercentOut(out);
    }

    public GamePiece getHeldPiece() {
        return heldPiece;
    }

    @Override
    public void onDisable() {
        motor.stop();
    }
}
