package com.swrobotics.lib.input;

import com.swrobotics.mathlib.Vec2d;
import edu.wpi.first.wpilibj.GenericHID;

import java.util.Arrays;
import java.util.List;

/** Represents an Xbox controller attached to the driver station. */
public final class XboxController extends InputSource {
    private final edu.wpi.first.wpilibj.XboxController xbox;
    private final List<InputElement> elements;
    private final double deadband;

    public final InputButton a, b, x, y;
    public final InputButton back, start;
    public final InputButton leftStickButton, rightStickButton;
    public final InputButton leftBumper, rightBumper;
    public final InputAxis leftStickX, leftStickY;
    public final InputAxis rightStickX, rightStickY;
    public final InputAxis leftTrigger, rightTrigger;

    public final InputDpad dpad;

    /**
     * Creates a new Xbox controller connected to a given port.
     *
     * @param port port the controller is on
     */
    public XboxController(int port, double deadband) {
        xbox = new edu.wpi.first.wpilibj.XboxController(port);
        this.deadband = deadband;

        a = new InputButton(xbox::getAButton);
        b = new InputButton(xbox::getBButton);
        x = new InputButton(xbox::getXButton);
        y = new InputButton(xbox::getYButton);
        back = new InputButton(xbox::getBackButton);
        start = new InputButton(xbox::getStartButton);
        leftStickButton = new InputButton(xbox::getLeftStickButton);
        rightStickButton = new InputButton(xbox::getRightStickButton);
        leftBumper = new InputButton(xbox::getLeftBumper);
        rightBumper = new InputButton(xbox::getRightBumper);

        leftStickX = new InputAxis(xbox::getLeftX, deadband);
        leftStickY = new InputAxis(xbox::getLeftY, deadband);
        rightStickX = new InputAxis(xbox::getRightX, deadband);
        rightStickY = new InputAxis(xbox::getRightY, deadband);
        leftTrigger = new InputAxis(xbox::getLeftTriggerAxis, deadband);
        rightTrigger = new InputAxis(xbox::getRightTriggerAxis, deadband);

        dpad = new InputDpad(xbox::getPOV);

        elements =
                Arrays.asList(
                        a,
                        b,
                        x,
                        y,
                        back,
                        start,
                        leftStickButton,
                        rightStickButton,
                        leftBumper,
                        rightBumper,
                        leftStickX,
                        leftStickY,
                        rightStickX,
                        rightStickY,
                        leftTrigger,
                        rightTrigger,
                        dpad);
    }

    /**
     * Sets the rumble feedback output of the controller.
     *
     * @param amount percentage of rumble from 0 to 1
     */
    public void setRumble(double amount) {
        xbox.setRumble(GenericHID.RumbleType.kLeftRumble, amount);
        xbox.setRumble(GenericHID.RumbleType.kRightRumble, amount);
    }

    public Vec2d getLeftStick() {
        return new Vec2d(leftStickX.getRaw(), leftStickY.getRaw()).deadband(deadband);
    }

    public Vec2d getRightStick() {
        return new Vec2d(leftStickX.getRaw(), leftStickY.getRaw()).deadband(deadband);
    }

    @Override
    protected Iterable<InputElement> getElements() {
        return elements;
    }
}
