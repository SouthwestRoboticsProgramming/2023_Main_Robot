package com.swrobotics.robot.control;

public class InputUtils {
    public static double deadband(double value, double deadband) {
        if (Math.abs(value) < deadband) {
            return 0.0;
        }

        if (value < 0.0) {
            return (value + deadband) / (1.0 - deadband);
        }

        return (value - deadband) / (1.0 - deadband);
    }

    public static double modifyAxis(double value, double deadband) {
        // Deadband
        value = deadband(value, deadband);

        // Square the axis
        value = Math.copySign(value * value, value);

        return value;
    }
}
