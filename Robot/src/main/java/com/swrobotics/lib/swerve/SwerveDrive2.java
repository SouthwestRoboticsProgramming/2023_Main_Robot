package com.swrobotics.lib.swerve;

import com.ctre.phoenix.motorcontrol.can.TalonFX;
import com.swrobotics.lib.gyro.Gyroscope;
import com.swrobotics.lib.gyro.PigeonGyroscope;
import com.swrobotics.lib.schedule.Scheduler;
import com.swrobotics.lib.schedule.Subsystem;

/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/


import com.swrobotics.lib.swerve.Constants.*;
import com.swrobotics.lib.wpilib.AbstractRobot;
import com.swrobotics.mathlib.CCWAngle;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveDriveOdometry;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class SwerveDrive2 implements Subsystem {

    private final XboxController controller = new XboxController(0);
    private final Field2d field = new Field2d();

    public static final double kMaxAngularSpeed = DriveConstants.kMaxChassisRotationSpeed; // 3 meters per second

    private final SwerveDriveOdometry m_odometry = new SwerveDriveOdometry(DriveConstants.kDriveKinematics, getHeadingRotation2d());

    private double m_trajectoryTime;
    private Trajectory currentTrajectory;

    /**
     * Just like a graph's quadrants
     * 0 is Front Left
     * 1 is Front Right
     * 2 is Back Left
     * 3 is Back Right
     */
    private SwerveModule[] mSwerveModules = new SwerveModule[] {
        new SwerveModule(0, new TalonFX(Constants.frontLeftTurningMotor), new TalonFX(Constants.frontLeftDriveMotor), 0, true, false),
        new SwerveModule(1, new TalonFX(Constants.frontRightTurningMotor), new TalonFX(Constants.frontRightDriveMotor), 0, true, false), //true
        new SwerveModule(2, new TalonFX(Constants.backLeftTurningMotor), new TalonFX(Constants.backLeftDriveMotor), 0, true, false),
        new SwerveModule(3, new TalonFX(Constants.backRightTurningMotor), new TalonFX(Constants.backRightDriveMotor), 0, true, false) //true
    };

    private final Gyroscope gyro = new PigeonGyroscope(0);

    public SwerveDrive2() {

        SmartDashboard.putData("Field", field);

        Scheduler.get().addSubsystem(this, mSwerveModules[0]);
        Scheduler.get().addSubsystem(this, mSwerveModules[1]);
        Scheduler.get().addSubsystem(this, mSwerveModules[2]);
        Scheduler.get().addSubsystem(this, mSwerveModules[3]);

        // SmartDashboardTab.putData("SwerveDrive","swerveDriveSubsystem", this);
        if (AbstractRobot.isSimulation()) {

        }
    }

    // public double getGyroRate() { // TODO: Add gyro velocity
    //     return mNavX.getRate();
    // }

    /**
     * Returns the angle of the robot as a Rotation2d.
     *
     * @return The angle of the robot.
     */
    public Rotation2d getHeadingRotation2d() {
        return Rotation2d.fromDegrees(getHeadingDegrees());
    }

    /**
     * Returns the turn rate of the robot.
     *
     * @return The turn rate of the robot, in degrees per second
     */
    // public double getTurnRate() {
    //     return mNavX.getRate();
    // }

    /**
     * Returns the heading of the robot in degrees.
     *
     * @return the robot's heading in degrees, from 180 to 180
     */
    public double getHeadingDegrees() {
        try {
            return gyro.getAngle().ccw().deg();
        } catch (Exception e) {
            System.out.println("Cannot Get NavX Heading " + e.getStackTrace());
            return 0;
        }
    }

    /**
     * Resets the drive encoders to currently read a position of 0.
     */
    public void resetEncoders() {
        for (int i = 0; i < 4; i++){
            mSwerveModules[i].reset();
        }
    }

    /**
     * Zeroes the heading of the robot.
     */
    public void zeroHeading() {
        // mNavX.reset();
    }


    public SwerveModule getSwerveModule(int i) {
        return mSwerveModules[i];
    }

    /**
     * Returns the currently-estimated pose of the robot.
     *
     * @return The pose.
     */
    public Pose2d getPose() {
        return m_odometry.getPoseMeters();
    }

    /**
     * Method to drive the robot using joystick info.
     *
     * @param xSpeed        Speed of the robot in the x direction (forward).
     * @param ySpeed        Speed of the robot in the y direction (sideways).
     * @param rot           Angular rate of the robot.
     * @param fieldRelative Whether the provided x and y speeds are relative to the field.
     */
    @SuppressWarnings("ParameterName")
    public void drive(double xSpeed, double ySpeed, double rot, boolean fieldRelative) {
        xSpeed *= DriveConstants.kMaxSpeedMetersPerSecond;
        ySpeed *= DriveConstants.kMaxSpeedMetersPerSecond;
        // rot *= kMaxAngularSpeed;

        var swerveModuleStates = DriveConstants.kDriveKinematics.toSwerveModuleStates(
                fieldRelative ? ChassisSpeeds.fromFieldRelativeSpeeds(
                        xSpeed, ySpeed, rot, getHeadingRotation2d())
                        : new ChassisSpeeds(xSpeed, ySpeed, rot)
        ); //from 2910's code
        SwerveDriveKinematics.desaturateWheelSpeeds(swerveModuleStates, Constants.DriveConstants.kMaxSpeedMetersPerSecond);

        mSwerveModules[0].setDesiredState(swerveModuleStates[0]);
        mSwerveModules[1].setDesiredState(swerveModuleStates[1]);
        mSwerveModules[2].setDesiredState(swerveModuleStates[2]);
        mSwerveModules[3].setDesiredState(swerveModuleStates[3]);
    }

    public void setSwerveDriveNeutralMode(boolean mode) {
        for(int i = 0; i < mSwerveModules.length; i++) {
            mSwerveModules[i].setBrakeMode(mode);
        }
    }

    /**
     * Sets the swerve ModuleStates.
     *
     * @param desiredStates The desired SwerveModule states.
     */
    public void setModuleStates(SwerveModuleState[] desiredStates) {
        SwerveDriveKinematics.desaturateWheelSpeeds(desiredStates, Constants.DriveConstants.kMaxSpeedMetersPerSecond);
        mSwerveModules[0].setDesiredState(desiredStates[0]);
        mSwerveModules[1].setDesiredState(desiredStates[1]);
        mSwerveModules[2].setDesiredState(desiredStates[2]);
        mSwerveModules[3].setDesiredState(desiredStates[3]);
    }

    /**
     * Updates the field relative position of the robot.
     */
    public void updateOdometry() {
        m_odometry.update(
            getHeadingRotation2d(),
            mSwerveModules[0].getState(),
            mSwerveModules[1].getState(),
            mSwerveModules[2].getState(),
            mSwerveModules[3].getState()
        );
    }

    public void resetOdometry(Pose2d pose, Rotation2d rotation) {
        m_odometry.resetPosition(pose, rotation);
    }

    @Override
    public void periodic() {

        Pose2d poseMeters = getPose();
        Pose2d poseFeet = new Pose2d(
            Units.metersToFeet(poseMeters.getX()),
            Units.metersToFeet(poseMeters.getY()),
            poseMeters.getRotation()
        );

        field.setRobotPose(poseFeet);

        double x = deadband(controller.getLeftX(), 0.2);
        double y = deadband(-controller.getLeftY(), 0.2);
        double rot = deadband(controller.getRightX(), 0.2);

        drive(
            y, -x, /*-rot*/ 0.01,
            true);

        // sampleTrajectory();
        updateOdometry();

        
        if (AbstractRobot.isSimulation()) {
            SwerveModuleState[] moduleStates = {
                mSwerveModules[0].getState(),
                mSwerveModules[1].getState(),
                mSwerveModules[2].getState(),
                mSwerveModules[3].getState()
            };
            
            var chassisSpeed = DriveConstants.kDriveKinematics.toChassisSpeeds(moduleStates);
            double chassisRotationSpeed = chassisSpeed.omegaRadiansPerSecond;

            System.out.println(gyro.getAngle());
            
            gyro.offsetBy(CCWAngle.rad(chassisRotationSpeed *  (1 / AbstractRobot.get().getPeriodicPerSecond())));
        }
    }

    // TODO: Remove
    private double deadband(double value, double deadband) {
        if (Math.abs(value) < deadband) {
            return 0;
        }

        return value;
    }


    // private void sampleTrajectory() {
    //     if(DriverStation.getInstance().isAutonomous()) {
    //         try {
    //             var currentTrajectoryState = currentTrajectory.sample(Timer.getFPGATimestamp() - startTime);

    //             System.out.println("Trajectory Time: " + (Timer.getFPGATimestamp() - startTime));
    //             System.out.println("Trajectory Pose: " + currentTrajectoryState.poseMeters);
    //             System.out.println("Trajectory Speed: " + currentTrajectoryState.velocityMetersPerSecond);
    //             System.out.println("Trajectory angular speed: " + currentTrajectoryState.curvatureRadPerMeter);
    //         } catch (Exception e) {

    //         }
    //     }

    // }

    // public void setTrajectoryTime(double trajectoryTime) {
    //     m_trajectoryTime = trajectoryTime;
    // }

    // double startTime;
    // public void setCurrentTrajectory(Trajectory trajectory) {
    //     currentTrajectory = trajectory;
    //     startTime = Timer.getFPGATimestamp();
    // }
}

