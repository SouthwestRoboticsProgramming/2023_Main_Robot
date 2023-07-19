package com.swrobotics.robot.config;

/**
 * Class to define all CAN IDs in one place, so it is easy to verify all the
 * IDs are set correctly
 */
public final class CANAllocation {
    public static final SwerveIds SWERVE_FL = new SwerveIds(9, 5, 1);
    public static final SwerveIds SWERVE_FR = new SwerveIds(10, 6, 2);
    public static final SwerveIds SWERVE_BL = new SwerveIds(11, 7, 3);
    public static final SwerveIds SWERVE_BR = new SwerveIds(12, 8, 4);

    public static final int ARM_BOTTOM_CANCODER = 13;
    public static final int ARM_TOP_CANCODER = 14;
    public static final int ARM_WRIST_CANCODER = 15;

    public static final int ARM_BOTTOM_MOTOR = 23;
    public static final int ARM_TOP_MOTOR = 24;
    public static final int ARM_WRIST_MOTOR = 25;

    public static final int INTAKE_MOTOR = 26;

    public static final int PDP = 62;

    public static final class SwerveIds {
        public final int drive, turn, encoder;

        public SwerveIds(int drive, int turn, int encoder) {
            this.drive = drive;
            this.turn = turn;
            this.encoder = encoder;
        }
    }

    private CANAllocation() {
        throw new AssertionError();
    }
}
