package com.swrobotics.robot.subsystems.intake;

import com.swrobotics.lib.net.NTDouble;
import com.swrobotics.robot.RIOPorts;
import com.swrobotics.robot.subsystems.SwitchableSubsystemBase;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;

public final class IntakeSubsystem extends SwitchableSubsystemBase {
    private static final NTDouble SPEED = new NTDouble("Intake3/Speed", 0.2);
    private static final NTDouble CONE_HOLD = new NTDouble("Intake3/Cone Hold", 0.1);

    private final PWMSparkMax motor;
    private final DigitalInput coneBeamBreak;
    private final DigitalInput cubeBeamBreak;

    private GamePiece expectedPiece;
    private boolean running;

    public IntakeSubsystem() {
        motor = new PWMSparkMax(RIOPorts.INTAKE_PWM);
        coneBeamBreak = new DigitalInput(RIOPorts.INTAKE_SENSOR_CONE_DIO);
        cubeBeamBreak = new DigitalInput(RIOPorts.INTAKE_SENSOR_CUBE_DIO);

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

    public boolean isExpectedPiecePresent() {
        DigitalInput sensor = expectedPiece == GamePiece.CONE ? coneBeamBreak : cubeBeamBreak;
        return !sensor.get();
    }

    public void run() {
        if (isEnabled()) {
            motor.set(expectedPiece.getIntakeDirection() * SPEED.get());
            running = true;
        }
    }

    public void eject() {
        if (isEnabled()) {
            motor.set(-expectedPiece.getIntakeDirection() * SPEED.get());
            running = true;
        }
    }

    public void stop() {
        motor.set(expectedPiece == GamePiece.CONE ? (expectedPiece.getIntakeDirection() * CONE_HOLD.get()) : 0);
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
