package com.swrobotics.robot.subsystems.intake;

import com.swrobotics.lib.motor.Motor;
import com.swrobotics.lib.motor.rev.PWMSparkMaxMotor;
import com.swrobotics.lib.schedule.SwitchableSubsystemBase;
import com.swrobotics.robot.RIOPorts;

public final class IntakeSubsystem extends SwitchableSubsystemBase {
    public enum Mode {
        INTAKE,
        EJECT,
        OFF
    }

    private final Motor motor;

    public IntakeSubsystem() {
        // TODO: If we're running CAN to the end of the arm, do we want to
        //       switch this to CAN?
        motor = new PWMSparkMaxMotor(RIOPorts.INTAKE_PWM);
    }

    public void set(Mode mode, GamePiece gamePiece) {
        if (!isEnabled())
            return;

        double out = 0;
        switch (mode) {
            case INTAKE:
                out = gamePiece.getIntakeOutput();
                break;
            case EJECT:
                out = gamePiece.getOuttakeOutput();
                break;
            case OFF:
                out = gamePiece.getHoldOutput();
                break;
        }

        motor.setPercentOut(out);
    }

    @Override
    public void onDisable() {
        motor.stop();
    }
}
