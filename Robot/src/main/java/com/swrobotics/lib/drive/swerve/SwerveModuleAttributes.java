package com.swrobotics.lib.drive.swerve;

import edu.wpi.first.math.util.Units;

/**
 * Stores the physical attributes of a swerve module.
 */
public final class SwerveModuleAttributes {
    public static final SwerveModuleAttributes SDS_MK4_L1
            = new SwerveModuleAttributes(8.14, 12.8, Units.inchesToMeters(4), Units.feetToMeters(13.5));
    public static final SwerveModuleAttributes SDS_MK4_L2
            = new SwerveModuleAttributes(6.75, 12.8, Units.inchesToMeters(4), Units.feetToMeters(16.3));
    public static final SwerveModuleAttributes SDS_MK4_L3
            = new SwerveModuleAttributes(6.12, 12.8, Units.inchesToMeters(4), Units.feetToMeters(18));
    public static final SwerveModuleAttributes SDS_MK4_L4
            = new SwerveModuleAttributes(5.14, 12.8, Units.inchesToMeters(4), Units.feetToMeters(21.4));

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
    public SwerveModuleAttributes(double driveGearRatio, double turnGearRatio, double wheelDiameter, double maxVelocity) {
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
        return "SwerveModuleAttributes{" +
                "driveGearRatio=" + driveGearRatio +
                ", turnGearRatio=" + turnGearRatio +
                ", wheelDiameter=" + wheelDiameter +
                ", maxVelocity=" + maxVelocity +
                '}';
    }
}
