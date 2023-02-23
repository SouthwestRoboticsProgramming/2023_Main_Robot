package com.swrobotics.robot.subsystems.intake3;

import com.swrobotics.lib.net.NTDouble;
import com.swrobotics.robot.subsystems.intake.GamePiece;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public final class Intake3Subsystem extends SubsystemBase {
    private static final int MOTOR_PORT_PWM = 1;
    private static final int CONE_BEAM_PORT_DIO = 0;
    private static final int CUBE_BEAM_PORT_DIO = 1;

    private static final NTDouble SPEED = new NTDouble("Intake3/Speed", 0.2);

    private final PWMSparkMax motor;
    private final DigitalInput coneBeamBreak;
    private final DigitalInput cubeBeamBreak;

    private GamePiece expectedPiece;

    public Intake3Subsystem() {
        motor = new PWMSparkMax(MOTOR_PORT_PWM);
        coneBeamBreak = new DigitalInput(CONE_BEAM_PORT_DIO);
        cubeBeamBreak = new DigitalInput(CUBE_BEAM_PORT_DIO);

        expectedPiece = GamePiece.CONE;
    }

    public GamePiece getExpectedPiece() {
        return expectedPiece;
    }

    public void setExpectedPiece(GamePiece expectedPiece) {
        this.expectedPiece = expectedPiece;
    }

    public boolean isExpectedPiecePresent() {
        DigitalInput sensor = expectedPiece == GamePiece.CONE ? coneBeamBreak : cubeBeamBreak;
        return !sensor.get();
    }

    public void run() {
        motor.set(expectedPiece.getIntakeDirection() * SPEED.get());
    }

    public void stop() {
        motor.set(0);
    }

    public void debugSetRunning(boolean running) {
        if (running)
            run();
        else
            stop();
    }
}
