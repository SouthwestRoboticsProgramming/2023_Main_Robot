package com.swrobotics.robot.subsystems.arm;

import edu.wpi.first.math.util.Units;

public final class ArmConstants {
    // Lengths of the arm segments in meters
    public static final double BOTTOM_LENGTH = Units.inchesToMeters(35);
    public static final double TOP_LENGTH = Units.inchesToMeters(29.95824774);
    public static final double WRIST_RAD = Units.inchesToMeters(6.49785148 + 1.125);

    // Gear ratios from motor output to arm joint movement
    public static final double BOTTOM_GEAR_RATIO = 600;
    public static final double TOP_GEAR_RATIO = 288;
    public static final double WRIST_GEAR_RATIO = 10 * (50.0/28.0);
    public static final double CANCODER_TO_ARM_RATIO = 2;
    public static final double WRIST_CANCODER_TO_ARM_RATIO = 1;

    public static final int BOTTOM_MOTOR_ID = 23;
    public static final int TOP_MOTOR_ID = 24;
    public static final int WRIST_MOTOR_ID = 25;

    public static final int BOTTOM_CANCODER_ID = 13;
    public static final int TOP_CANCODER_ID = 14;
    public static final int WRIST_CANCODER_ID = 15;

    // 50:28  10:1

    private ArmConstants() {
        throw new AssertionError();
    }
}
