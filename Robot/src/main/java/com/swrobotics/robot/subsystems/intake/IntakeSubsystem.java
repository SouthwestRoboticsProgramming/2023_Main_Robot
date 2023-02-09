package com.swrobotics.robot.subsystems.intake;

import com.swrobotics.lib.net.NTDouble;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

// Slurp
public final class IntakeSubsystem extends SubsystemBase {
    private static final int MOTOR_PORT = 1; // FIXME
    private static final int BEAM_BREAK_PORT = 1; // FIXME

    private static final NTDouble SPEED = new NTDouble("Intake/Speed", 0.25);
    private static final NTDouble CONTINUE_TIME = new NTDouble("Intake/Continue Time", 0.25);
    private static final NTDouble EJECT_TIME = new NTDouble("Intake/Eject Time", 1);

    private final PWMSparkMax motor;
    private final DigitalInput beamBreak;

    private GamePiece expectedPiece, heldPiece;

    public IntakeSubsystem() {
        motor = new PWMSparkMax(MOTOR_PORT);
        beamBreak = new DigitalInput(BEAM_BREAK_PORT);

        expectedPiece = heldPiece = GamePiece.CUBE;
    }

    public void setExpectedPiece(GamePiece expectedPiece) {
        this.expectedPiece = expectedPiece;
    }

    private boolean isGamePiecePresent() {
        System.out.println(beamBreak.get());
        return !beamBreak.get();
    }

    public Command intake() {
        CommandBase cmd = new CommandBase() {
            final Timer timer = new Timer();

            @Override
            public void initialize() {
                timer.start();
            }

            @Override
            public void execute() {
                heldPiece = expectedPiece;
                motor.set(heldPiece.getIntakeDirection() * SPEED.get());
                if (!isGamePiecePresent())
                    timer.restart();
            }

            @Override
            public boolean isFinished() {
                return timer.hasElapsed(CONTINUE_TIME.get());
            }

            @Override
            public void end(boolean cancelled) {
                motor.set(0);
            }
        };
        cmd.addRequirements(this);
        return cmd;
    }

    public Command eject() {
        CommandBase cmd = new CommandBase() {
            final Timer timer = new Timer();

            @Override
            public void initialize() {
                timer.start();
            }

            @Override
            public void execute() {
                motor.set(-heldPiece.getIntakeDirection() * SPEED.get());
            }

            @Override
            public boolean isFinished() {
                System.out.println(timer.get());
                return timer.hasElapsed(EJECT_TIME.get());
            }

            @Override
            public void end(boolean cancelled) {
                motor.set(0);
            }
        };
        cmd.addRequirements(this);
        return cmd;
    }
}
