package com.swrobotics.robot.commands.intake;

import com.swrobotics.lib.net.NTDouble;
import com.swrobotics.robot.subsystems.intake.GamePiece;
import com.swrobotics.robot.subsystems.intake.IntakeSubsystem;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.CommandBase;

public final class IntakeCommand extends CommandBase {
    private static final NTDouble CONTINUE_TIME_CUBE = new NTDouble("Intake/Cube Continue Time", 0.5);
    private static final NTDouble CONTINUE_TIME_CONE = new NTDouble("Intake/Cone Continue Time", 0.5);

    private final IntakeSubsystem intake;
    private final Timer timer;
    private boolean prevSensor;

    public IntakeCommand(IntakeSubsystem intake) {
        this.intake = intake;
        timer = new Timer();
        prevSensor = intake.isExpectedPiecePresent();

        addRequirements(intake);
    }

    @Override
    public void execute() {
        intake.run();
    }

    @Override
    public boolean isFinished() {
        boolean sensor = intake.isExpectedPiecePresent();
        double time = intake.getExpectedPiece() == GamePiece.CONE ?
                CONTINUE_TIME_CONE.get() : CONTINUE_TIME_CUBE.get();

        if (sensor && !prevSensor)
            timer.restart();
        prevSensor = sensor;

        return sensor && timer.hasElapsed(time);
    }
}
