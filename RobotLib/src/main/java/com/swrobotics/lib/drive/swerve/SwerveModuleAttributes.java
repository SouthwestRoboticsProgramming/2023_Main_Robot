package com.swrobotics.lib.drive.swerve;

import edu.wpi.first.math.util.Units;

/** Stores the physical attributes of a swerve module. */
public final class SwerveModuleAttributes {
    private static final double SDS_WHEEL_DIA = Units.inchesToMeters(4);

    private static final double L1_DRIVE_RATIO = 8.14;
    private static final double L2_DRIVE_RATIO = 6.75;
    private static final double L3_DRIVE_RATIO = 6.12;
    private static final double L4_DRIVE_RATIO = 5.14;

    private static final double MK4_TURN_RATIO = 12.8;
    private static final double MK4I_TURN_RATIO = 150.0 / 7;

    public static final SwerveModuleAttributes SDS_MK4_L1 = new SwerveModuleAttributes(L1_DRIVE_RATIO, MK4_TURN_RATIO, SDS_WHEEL_DIA, Units.feetToMeters(13.5));
    public static final SwerveModuleAttributes SDS_MK4_L2 = new SwerveModuleAttributes(L2_DRIVE_RATIO, MK4_TURN_RATIO, SDS_WHEEL_DIA, Units.feetToMeters(16.3));
    public static final SwerveModuleAttributes SDS_MK4_L3 = new SwerveModuleAttributes(L3_DRIVE_RATIO, MK4_TURN_RATIO, SDS_WHEEL_DIA, Units.feetToMeters(18));
    public static final SwerveModuleAttributes SDS_MK4_L4 = new SwerveModuleAttributes(L4_DRIVE_RATIO, MK4_TURN_RATIO, SDS_WHEEL_DIA, Units.feetToMeters(21.4));

    public static final SwerveModuleAttributes SDS_MK4I_L1 = new SwerveModuleAttributes(L1_DRIVE_RATIO, MK4I_TURN_RATIO, SDS_WHEEL_DIA, Units.feetToMeters(13.5));
    public static final SwerveModuleAttributes SDS_MK4I_L2 = new SwerveModuleAttributes(L2_DRIVE_RATIO, MK4I_TURN_RATIO, SDS_WHEEL_DIA, Units.feetToMeters(16.3));
    public static final SwerveModuleAttributes SDS_MK4I_L3 = new SwerveModuleAttributes(L3_DRIVE_RATIO, MK4I_TURN_RATIO, SDS_WHEEL_DIA, Units.feetToMeters(18.0));

    private final double driveGearRatio;
    private final double turnGearRatio;
    private final double wheelDiameter;
    private final double maxVelocity;

    /**
     * @param driveGearRatio gear ratio from drive motor to wheel, >1 is reduction
     * @param turnGearRatio gear ratio from turn motor to wheel, >1 is reduction
     * @param wheelDiameter wheel diameter in meters
     * @param maxVelocity maximum achievable velocity in meters per second
     */
    public SwerveModuleAttributes(
            double driveGearRatio, double turnGearRatio, double wheelDiameter, double maxVelocity) {
        this.driveGearRatio = driveGearRatio;
        this.turnGearRatio = turnGearRatio;
        this.wheelDiameter = wheelDiameter;
        this.maxVelocity = maxVelocity;
    }

    public double getDriveGearRatio() {
        return driveGearRatio;
    }

    public double getTurnGearRatio() {
        return turnGearRatio;
    }

    public double getWheelDiameter() {
        return wheelDiameter;
    }

    public double getMaxVelocity() {
        return maxVelocity;
    }

    @Override
    public String toString() {
        return "SwerveModuleAttributes{"
                + "driveGearRatio="
                + driveGearRatio
                + ", turnGearRatio="
                + turnGearRatio
                + ", wheelDiameter="
                + wheelDiameter
                + ", maxVelocity="
                + maxVelocity
                + '}';
    }
}
