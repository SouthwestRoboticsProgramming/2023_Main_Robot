package com.swrobotics.lib.swerve;

import com.ctre.phoenix.sensors.BasePigeonSimCollection;
import com.ctre.phoenix.sensors.WPI_PigeonIMU;
import com.swrobotics.lib.schedule.Subsystem;
import com.swrobotics.lib.wpilib.AbstractRobot;
import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.CCWAngle;
import com.swrobotics.mathlib.Vec2d;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveDriveOdometry;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class SwerveDrive implements Subsystem {

    // Temporary
    private final WPI_PigeonIMU gyro = new WPI_PigeonIMU(0);
    private final BasePigeonSimCollection gyroSim = gyro.getSimCollection();

    private final SwerveModule[] modules;
    private final SwerveDriveKinematics kinematics;
    private final SwerveDriveOdometry odometry;

    // Temporary 
    private final Field2d fieldSim = new Field2d();

    public SwerveDrive(SwerveModule... modules) {
        // Temporary
        SmartDashboard.putData("Field", fieldSim);

        this.modules = modules;

        // Extract positions of modules for kinematics
        Translation2d[] modulePositions = new Translation2d[modules.length];
        for (int i = 0; i < modules.length; i++) {
            modulePositions[i] = modules[i].getPosition();
        }

        // Construct kinematics that describe how the drive should move
        kinematics = new SwerveDriveKinematics(modulePositions);

        // Construct odometry to estimate the pose of the robot using wheel speeds and angles
        odometry = new SwerveDriveOdometry(kinematics, new Rotation2d()); // TODO: Starting position
    }

    public void setChassisSpeeds(ChassisSpeeds speeds) {
        // Convert target body motion into indevidual wheel states
        SwerveModuleState[] states = kinematics.toSwerveModuleStates(speeds); // TODO: Center of rotation

        // Set the modules to target these states
        for (int i = 0; i < modules.length; i++) {
            modules[i].setState(states[i]);
        }
    }

    public ChassisSpeeds getChassisSpeeds() {
        // Read modules states from the modules
        SwerveModuleState[] states = getModuleStates();
        // System.out.println(states[0]);

        // Kinematics calculates how the robot should be moving
        return kinematics.toChassisSpeeds(states);
    }

    public SwerveModuleState[] getModuleStates() {
        SwerveModuleState[] states = new SwerveModuleState[modules.length];
        for (int i = 0; i < modules.length; i++) {
            states[i] = modules[i].getState();
        }
        return states;
    }

    public Vec2d getPosition() {
        return new Vec2d(10, 10);
    }

    public Angle getRotation() {
        return CCWAngle.ZERO;
    }

    public Pose2d getPose() {
        return odometry.getPoseMeters();
    }

    @Override
    public void periodic() {
        // Update odometry with simulated position
        if (AbstractRobot.isSimulation()) {
            ChassisSpeeds simulatedSpeeds = getChassisSpeeds();
            // Set gyroscope based on rotational velocity
            double currentAngle = Math.toRadians(gyro.getAngle());
            System.out.println("Current: " + currentAngle + " Delta: " + simulatedSpeeds.omegaRadiansPerSecond / 50);
            gyroSim.setRawHeading(Math.toDegrees(-currentAngle + simulatedSpeeds.omegaRadiansPerSecond / 50)); // TODO: No magic number
        }

        odometry.update(gyro.getRotation2d(), getModuleStates());
        fieldSim.setRobotPose(getPose());
    }
}
