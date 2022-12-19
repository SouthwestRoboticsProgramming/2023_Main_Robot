/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package com.swrobotics.lib.swerve;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.math.util.Units;

/**
 * The Constants class provides a convenient place for teams to hold robot-wide numerical or boolean
 * constants.  This class should not be used for any other purpose.  All constants should be
 * declared globally (i.e. public static).  Do not put anything functional in this class.
 *
 * <p>It is advised to statically import this class (or one of its inner classes) wherever the
 * constants are needed, to reduce verbosity.
 */
public final class Constants {
    // USB PORTS
    public static final int leftJoystick = 0;
    public static final int rightJoystick = 1;
    public static final int xBoxController = 2;

    // CAN ADDRESSES
    public static final int frontLeftDriveMotor = 20;
    public static final int frontLeftTurningMotor = 21;
    public static final int frontRightDriveMotor = 22;
    public static final int frontRightTurningMotor = 23;
    public static final int backLeftDriveMotor = 24;
    public static final int backLeftTurningMotor = 25;
    public static final int backRightDriveMotor = 26;
    public static final int backRightTurningMotor = 27;

    public static final class DriveConstants {
        public static final double kTrackWidth = Units.inchesToMeters(30);
        //Distance between centers of right and left wheels on robot. Meters?
        public static final double kWheelBase = Units.inchesToMeters(30);
        //Distance between front and back wheels on robot. Meters?

        public static Translation2d[] modulePositions = {
            new Translation2d(kWheelBase / 2, kTrackWidth / 2),
            new Translation2d(kWheelBase / 2, -kTrackWidth / 2),
            new Translation2d(-kWheelBase / 2, kTrackWidth / 2),
            new Translation2d(-kWheelBase / 2, -kTrackWidth / 2)
        };

        public static final SwerveDriveKinematics kDriveKinematics = new SwerveDriveKinematics(
            modulePositions[0],
            modulePositions[1],
            modulePositions[2],
            modulePositions[3]
        );

        public static final boolean kGyroReversed = false;

        public static final DCMotor kDriveGearbox = DCMotor.getFalcon500(2);

        // These are example values only - DO NOT USE THESE FOR YOUR OWN ROBOT!
        // These characterization values MUST be determined either experimentally or theoretically
        // for *your* robot's drive.
        // The RobotPy Characterization Toolsuite provides a convenient tool for obtaining these
        // values for your robot.
        public static final double ksVolts = 0.587;
        public static final double kvVoltSecondsPerMeter = 2.3;
        public static final double kaVoltSecondsSquaredPerMeter = 0.0917;

        public static final double kMaxSpeedMetersPerSecond = Units.feetToMeters(14);
//        public static final double kMaxSpeedMetersPerSecond = 3;

        public static final double kvVoltSecondsPerRadian = 3.41; // originally 1.5
        public static final double kaVoltSecondsSquaredPerRadian = 0.111; // originally 0.3

        public static final double kMaxChassisRotationSpeed = 10 * Math.PI;

    }

    public static final class ModuleConstants {
        public static final double  kDriveMotorGearRatio = 8.16; //6.89 to 1
        public static final double kTurningMotorGearRatio = 12.8; //12 to 1
        public static final int kEncoderCPR = 2048;
        public static final double kWheelDiameterMeters = Units.inchesToMeters(4); //10.16 cm
        public static final double kMaxModuleAngularSpeedRadiansPerSecond = 10 * Math.PI;
        public static final double kMaxModuleAngularAccelerationRadiansPerSecondSquared = 10 * Math.PI;

        public static final double kDriveEncoderDistancePerPulse =
                (kWheelDiameterMeters * Math.PI) / ((double) kEncoderCPR * kDriveMotorGearRatio);

        public static final double kTurningEncoderDistancePerPulse =
                // Assumes the encoders are on a 1:1 reduction with the module shaft.
                (2.0 * Math.PI) / (kEncoderCPR* kTurningMotorGearRatio);

        public static final double kPModuleTurningController = 2.4;

//        public static final double kPModuleDriveController = 1.57;
        public static final double kPModuleDriveController = 2.3;

        public static final double kvVoltSecondsPerMeter = 1.47;
        public static final double kaVoltSecondsSquaredPerMeter = 0.0348;

        public static final double kvVoltSecondsPerRadian = 1.47; // originally 1.5
        public static final double kaVoltSecondsSquaredPerRadian = 0.0348; // originally 0.3

    }

    public static final class OIConstants {
        public static final int kDriverControllerPort = 1;

    }

    public static final class AutoConstants {
        public static final double kMaxSpeedMetersPerSecond = DriveConstants.kMaxSpeedMetersPerSecond;
        public static final double kMaxAccelerationMetersPerSecondSquared = 3;
        public static final double kMaxAngularSpeedRadiansPerSecond = Math.PI;
        public static final double kMaxAngularSpeedRadiansPerSecondSquared = Math.PI;

        public static final double kPXController = 1;
        public static final double kPYController = 1;
        public static final double kPThetaController = 3.5;

        //Constraint for the motion profilied robot angle controller
        public static final TrapezoidProfile.Constraints kThetaControllerConstraints =
                new TrapezoidProfile.Constraints(kMaxAngularSpeedRadiansPerSecond * 10,
                        kMaxAngularSpeedRadiansPerSecondSquared * 10);

    }

    public static int intakeMotor = 47;
    public static final int pcmOne = 11;
    public static final int intakePistonForward = 2; // 2
    public static final int intakePistonReverse = 3; // 3
    public static enum IntakeStates {
        INTAKE_EMPTY, INTAKE_ONE_BALL, INTAKE_FOUR_BALLS, INTAKE_FIVE_BALLS
    }
    public static final int flywheelMotorA = 40;
    public static final int flywheelMotorB = 41;
    public static final int turretEncoder = 61;
    public static final int turretMotor = 60;
    public static final int turretHomeSensor = 30;
    public static final int climbMotorA = 50;
    public static final int climbMotorB = 51;
    public static final int climbPistonAForward = 31;
    public static final int climbPistonAReverse = 32;
    public static final int climbPistonBForward = 33;
    public static final int climbPistonBReverse = 34;
    public static final int indexerMotor = 35;
    public static final int uptakeMotor = 36;

    // Undetermined IDs
    public static final int carouselMotor = 4201;

    public static final int xEncoderPortA = 0;
    public static final int xEncoderPortB = 1;
    public static final int yEncoderPortA = 2;
    public static final int yEncoderPortB = 3;
    public static final int rotationEncoderPortA = 4;
    public static final int rotationEncoderPortB = 5;

}