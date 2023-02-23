package com.swrobotics.robot.commands.intake;

import com.swrobotics.lib.net.NTDouble;
import com.swrobotics.robot.subsystems.intake3.Intake3Subsystem;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.CommandBase;

public final class IntakeCommand extends CommandBase {
    private static final NTDouble CONTINUE_TIME = new NTDouble("Intake/Continue Time", 0.5);

    private final Intake3Subsystem intake;
    private final Timer timer;
    private boolean prevSensor;

    public IntakeCommand(Intake3Subsystem intake) {
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

        if (sensor && !prevSensor)
            timer.restart();
        prevSensor = sensor;

        return sensor && timer.hasElapsed(CONTINUE_TIME.get());
    }
}
