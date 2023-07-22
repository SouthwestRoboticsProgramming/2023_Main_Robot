package com.swrobotics.robot.commands;

import com.swrobotics.lib.time.Duration;
import com.swrobotics.lib.time.Timestamp;
import com.swrobotics.robot.subsystems.intake.GamePiece;
import com.swrobotics.robot.subsystems.intake.IntakeSubsystem;
import edu.wpi.first.wpilibj2.command.CommandBase;

public final class IntakeRunCommand extends CommandBase {
    private final IntakeSubsystem intake;
    private final IntakeSubsystem.Mode mode;
    private final GamePiece piece;
    private final Duration duration;
    private Timestamp endTime;

    public IntakeRunCommand(IntakeSubsystem intake, IntakeSubsystem.Mode mode, GamePiece piece, Duration dur) {
        this.intake = intake;
        this.mode = mode;
        this.piece = piece;
        this.duration = dur;
    }

    @Override
    public void initialize() {
        endTime = Timestamp.now().after(duration);
    }

    @Override
    public void execute() {
        intake.set(mode, piece);
    }

    @Override
    public void end(boolean interrupted) {
        intake.set(IntakeSubsystem.Mode.OFF, piece);
    }

    @Override
    public boolean isFinished() {
        return Timestamp.now().isAtOrAfter(endTime);
    }
}
