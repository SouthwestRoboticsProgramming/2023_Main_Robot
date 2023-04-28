package com.swrobotics.robot.subsystems.drive;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.util.Units;

public final class DrivetrainConstants {
    public static final double MAX_ACHIEVABLE_VELOCITY_METERS_PER_SECOND = Units.feetToMeters(18.0); // Thoretical

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

    // Kinematics doesn't change unless goemetry does
    protected static final SwerveDriveKinematics kinematics = new SwerveDriveKinematics(
            FRONT_LEFT_POSITION, FRONT_RIGHT_POSITION, BACK_LEFT_POSITION, BACK_RIGHT_POSITION);

    /* Modules that could be hot-swapped into a location on the swerve drive */
    protected static final SwerveModuleInfo[] MODULES = new SwerveModuleInfo[] {
            new SwerveModuleInfo("Front Right", 9, 5, 1, 38.41), // Default front left
            new SwerveModuleInfo("Front Left", 10, 6, 2, 185.45), // Default front right
            new SwerveModuleInfo("Back Left", 11, 7, 3, 132.63), // Default back left
            new SwerveModuleInfo("Back Right", 12, 8, 4, 78.93) // Default back right
    };

    /* Stop positions */
    public enum StopPosition {
        COAST(null),
        STRAIGHT(new SwerveModuleState[] {
                new SwerveModuleState(),
                new SwerveModuleState(),
                new SwerveModuleState(),
                new SwerveModuleState()
        }),
        CROSS(new SwerveModuleState[] { // Automatically point towards the defined center
                new SwerveModuleState(0, FRONT_LEFT_POSITION.getAngle()),
                new SwerveModuleState(0, FRONT_RIGHT_POSITION.getAngle()),
                new SwerveModuleState(0, BACK_LEFT_POSITION.getAngle()),
                new SwerveModuleState(0, BACK_RIGHT_POSITION.getAngle())
        }),
        CIRCLE(new SwerveModuleState[] { // Automatically point towards the defined center
                new SwerveModuleState(0, FRONT_LEFT_POSITION.getAngle().plus(Rotation2d.fromDegrees(90))),
                new SwerveModuleState(0, FRONT_RIGHT_POSITION.getAngle().plus(Rotation2d.fromDegrees(90))),
                new SwerveModuleState(0, BACK_LEFT_POSITION.getAngle().plus(Rotation2d.fromDegrees(90))),
                new SwerveModuleState(0, BACK_RIGHT_POSITION.getAngle().plus(Rotation2d.fromDegrees(90)))
        });

        private final SwerveModuleState[] states;

        private StopPosition(SwerveModuleState[] states) {
            this.states = states;
        }

        public SwerveModuleState[] getStates() {
            return states;
        }
    }

}
