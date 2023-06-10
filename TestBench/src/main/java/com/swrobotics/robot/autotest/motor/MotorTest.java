package com.swrobotics.robot.autotest.motor;

import com.swrobotics.lib.encoder.Encoder;
import com.swrobotics.lib.motor.FeedbackMotor;
import com.swrobotics.lib.motor.Motor;
import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.CWAngle;
import com.swrobotics.robot.autotest.HumanConfirmation;
import com.swrobotics.robot.autotest.Test;
import edu.wpi.first.wpilibj2.command.*;

public class MotorTest extends SequentialCommandGroup {
    public MotorTest(Motor motor, String name, boolean testEncoder, boolean canInvertEncoder) {
        // Have human confirm the motor is physically turning the correct direction
        addCommands(
                new InstantCommand(() -> {
                    motor.setInverted(false);
                    motor.setPercentOut(0.1);
                }),
                new HumanConfirmation("pct ccw", "Is " + name + " turning counterclockwise?"),
                new InstantCommand(() -> {
                    motor.setInverted(true);
                    motor.setPercentOut(0.1);
                }),
                new HumanConfirmation("pct ccw inv", "Is " + name + " turning clockwise?"),
                new InstantCommand(motor::stop),
                new WaitCommand(1)
        );

        if (!testEncoder || !(motor instanceof FeedbackMotor))
            return;

        FeedbackMotor m = (FeedbackMotor) motor;
        Encoder encoder = m.getIntegratedEncoder();

        double randomPosition = 10 + Math.random() * 30;

        // If we can, test encoder with inverted phase
        if (canInvertEncoder) {
            addCommands(
                    encoderTest(m, encoder, true, true, true),
                    encoderTest(m, encoder, false, true, true)
            );
        }

        // Test if encoder output is correct direction
        addCommands(
                encoderTest(m, encoder, false, false, canInvertEncoder),
                encoderTest(m, encoder, true, false, canInvertEncoder),

                new InstantCommand(() -> {
                    encoder.setAngle(CWAngle.rot(randomPosition));
                }),
                Test.check("set position", () -> Math.abs(encoder.getAngle().cw().rot() - randomPosition) < 0.1)
        );
    }

    private Command encoderTest(Motor m, Encoder encoder, boolean invertMotor, boolean invertEncoder, boolean canInvertEncoder) {
        String prefix = "enc M:" + (invertMotor ? "I" : "F") + " E:" + (invertEncoder ? "I" : "F") + " ";
        Angle[] initialAngle = new Angle[1];
        return Commands.sequence(
                new InstantCommand(() -> {
                    m.setInverted(invertMotor);
                    if (canInvertEncoder)
                        encoder.setInverted(invertEncoder);
                    m.setPercentOut(0.1);
                }),
                new WaitCommand(0.5), // Give motor time to update invert state
                new InstantCommand(() -> initialAngle[0] = encoder.getAngle()),
                new WaitCommand(5), // Motor should have made at least one revolution
                Test.check(prefix + "pos", () -> {
                    double encoderNow = encoder.getAngle().ccw().rot();
                    System.out.println("POS check: now: " + encoderNow + ", was: " + initialAngle[0].ccw().rot());
                    return (encoder.getAngle().ccw().rot() > initialAngle[0].ccw().rot()) ^ invertEncoder;
                }),
                Test.check(prefix + "vel", () -> encoder.getVelocity().ccw().rot() * (invertEncoder ? -1 : 1) > 0.2),
                new InstantCommand(m::stop),
                new WaitCommand(1) // Give motor time to slow down
        );
    }
}
