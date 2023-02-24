package com.swrobotics.robot;

/**
 * Class to define all IO ports used on the RoboRIO in one place, so
 * it is easy to verify all the wires are plugged in correctly
 */
public final class RIOPorts {
    public static final int LIGHTS_PWM = 0;
    public static final int INTAKE_PWM = 1; // Blue tape

    public static final int INTAKE_SENSOR_CONE_DIO = 0;
    public static final int INTAKE_SENSOR_CUBE_DIO = 1;
    public static final int ARM_DETECT_DIO = 2;

    private RIOPorts() {
        throw new AssertionError();
    }
}
