package com.swrobotics.robot;

/**
 * Class to define all IO ports used on the RoboRIO in one place, so it is easy to verify all the
 * wires are plugged in correctly
 */
public final class RIOPorts {
    public static final int INTAKE_PWM = 1; // Blue tape

    private RIOPorts() {
        throw new AssertionError();
    }
}
