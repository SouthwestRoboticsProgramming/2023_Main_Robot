package com.swrobotics.robot.subsystems.drive;

import com.swrobotics.robot.CANAllocation;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.util.Units;

public final class DrivetrainConstants {
    public static final double DRIVETRAIN_TRACKWIDTH_METERS = Units.inchesToMeters(25 - 3 * 2);
    public static final double DRIVETRAIN_WHEELBASE_METERS = DRIVETRAIN_TRACKWIDTH_METERS;

    public static final Translation2d FRONT_LEFT_POSITION = new Translation2d(
            DRIVETRAIN_TRACKWIDTH_METERS / 2.0, DRIVETRAIN_WHEELBASE_METERS / 2.0);

    public static final Translation2d FRONT_RIGHT_POSITION = new Translation2d(
            DRIVETRAIN_TRACKWIDTH_METERS / 2.0, -DRIVETRAIN_WHEELBASE_METERS / 2.0);

    public static final Translation2d BACK_LEFT_POSITION = new Translation2d(
            -DRIVETRAIN_TRACKWIDTH_METERS / 2.0, DRIVETRAIN_WHEELBASE_METERS / 2.0);

    public static final Translation2d BACK_RIGHT_POSITION = new Translation2d(
            -DRIVETRAIN_TRACKWIDTH_METERS / 2.0, -DRIVETRAIN_WHEELBASE_METERS / 2.0);

    /* Modules that could be hot-swapped into a location on the swerve drive */
    protected static final SwerveModuleInfo[] MODULES = new SwerveModuleInfo[] {
            new SwerveModuleInfo("Front Left", CANAllocation.SWERVE_FL, 38.41), // Default front left
            new SwerveModuleInfo("Front Right", CANAllocation.SWERVE_FR, 185.45), // Default front right
            new SwerveModuleInfo("Back Left", CANAllocation.SWERVE_BL, 132.63), // Default back left
            new SwerveModuleInfo("Back Right", CANAllocation.SWERVE_BR, 78.93) // Default back right
    };
}
