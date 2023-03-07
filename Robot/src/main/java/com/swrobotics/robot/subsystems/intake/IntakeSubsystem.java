package com.swrobotics.robot.subsystems.intake;

import com.swrobotics.lib.net.NTDouble;
import com.swrobotics.robot.RIOPorts;
import com.swrobotics.robot.subsystems.SwitchableSubsystemBase;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;

public final class IntakeSubsystem extends SwitchableSubsystemBase {
    private static final NTDouble CONE_HOLD = new NTDouble("Intake/Cone Hold", -0.1);

    private final PWMSparkMax motor;

    private GamePiece expectedPiece;
    private boolean running;

    public IntakeSubsystem() {
        motor = new PWMSparkMax(RIOPorts.INTAKE_PWM);

        expectedPiece = GamePiece.CONE;
        running = false;
    }

    public GamePiece getExpectedPiece() {
        return expectedPiece;
    }

    public void setExpectedPiece(GamePiece expectedPiece) {
        this.expectedPiece = expectedPiece;

        // Become more stopped
        if (!running)
            stop();
    }

    public void run() {
        if (isEnabled()) {
            motor.set(expectedPiece.getIntakeOutput());
            running = true;
        }
    }

    public void eject() {
        if (isEnabled()) {
            motor.set(-expectedPiece.getOuttakeOutput());
            running = true;
        }
    }

    public void stop() {
        motor.set(expectedPiece == GamePiece.CONE ? (CONE_HOLD.get()) : 0);
        running = false;
    }

    @Override
    public void onDisable() {
        motor.set(0);
    }

    public void debugSetRunning(boolean running) {
        if (running)
            run();
        else
            stop();
    }
}
