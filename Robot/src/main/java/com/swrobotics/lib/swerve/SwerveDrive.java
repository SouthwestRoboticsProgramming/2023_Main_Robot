package com.swrobotics.lib.swerve;

import com.swrobotics.lib.gyro.Gyroscope;
import com.swrobotics.lib.schedule.Scheduler;
import com.swrobotics.lib.schedule.Subsystem;
import com.swrobotics.lib.wpilib.AbstractRobot;
import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.CCWAngle;
import com.swrobotics.mathlib.CoordinateConversions;
import com.swrobotics.mathlib.Vec2d;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveDriveOdometry;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class SwerveDrive implements Subsystem {

    // Temporary
    private final Gyroscope gyro;
    private final XboxController controller = new XboxController(0);

    private final SwerveModule[] modules;
    private final SwerveDriveKinematics kinematics;
    private final SwerveDriveOdometry odometry;

    // Temporary 
    private final Field2d fieldSim = new Field2d();

    public SwerveDrive(Gyroscope gyro, SwerveModule... modules) {
        this.gyro = gyro;

        // Temporary
        SmartDashboard.putData("Field", fieldSim);

        this.modules = modules;

        // Extract positions of modules for kinematics
        Translation2d[] modulePositions = new Translation2d[modules.length];
        for (int i = 0; i < modules.length; i++) {
            modulePositions[i] = modules[i].getPosition();

            // Schedule the module
            Scheduler.get().addSubsystem(this, modules[i]);
        }

        // Construct kinematics that describe how the drive should move
        kinematics = new SwerveDriveKinematics(modulePositions);

        // Construct odometry to estimate the pose of the robot using wheel speeds and angles
        odometry = new SwerveDriveOdometry(kinematics, new Rotation2d()); // TODO: Starting position
    }

    public void setChassisSpeeds(ChassisSpeeds speeds) {
        // Convert target body motion into individual wheel states
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
        return CoordinateConversions.fromWPICoords(getPose().getTranslation());
    }

    public Angle getRotation() {
        return CoordinateConversions.fromWPIAngle(getPose().getRotation());
    }

    public Pose2d getPose() {
        return odometry.getPoseMeters();
    }

    @Override
    public void periodic() {
        double x = controller.getLeftX() * 3.0;
        double y = -controller.getLeftY() * 3.0;
        double rotation = -controller.getRightX() * 2 * Math.PI;

        setChassisSpeeds(new ChassisSpeeds(y, -x, rotation));

        // Update odometry with simulated position
        if (AbstractRobot.isSimulation()) {
            ChassisSpeeds simulatedSpeeds = getChassisSpeeds();
            // Set gyroscope based on rotational velocity
            Angle currentAngle = gyro.getAngle();
            gyro.setAngle(currentAngle.ccw().add(CCWAngle.rad(simulatedSpeeds.omegaRadiansPerSecond / 50))); // TODO: No magic number
        }

        odometry.update(new Rotation2d(gyro.getAngle().ccw().rad()), getModuleStates());
        fieldSim.setRobotPose(getPose());
    }
}
