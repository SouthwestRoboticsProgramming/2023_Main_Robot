package com.swrobotics.lib.drive.swerve;

import com.pathplanner.lib.auto.BaseAutoBuilder;
import com.pathplanner.lib.auto.PIDConstants;
import com.pathplanner.lib.auto.SwerveAutoBuilder;
import com.swrobotics.lib.drive.Drivetrain;
import com.swrobotics.lib.field.FieldInfo;
import com.swrobotics.lib.field.FieldSymmetry;
import com.swrobotics.lib.gyro.Gyroscope;
import com.swrobotics.lib.net.NTDouble;
import com.swrobotics.lib.net.NTEntry;
import com.swrobotics.mathlib.CCWAngle;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Twist2d;
import edu.wpi.first.math.kinematics.*;
import edu.wpi.first.wpilibj2.command.Command;

import java.util.Map;

public class SwerveDrive extends Drivetrain {
    private static final double IS_MOVING_THRESH = 0.1;
    private static final double IS_MOVING_TURN_THRESH = Math.toRadians(15);

    private final SwerveModule[] modules;

    private final SwerveDriveKinematics kinematics;
    private final SwerveDriveOdometry odometry;

    private StopPosition stopPosition;

    public SwerveDrive(FieldInfo fieldInfo, Gyroscope gyro, SwerveModule... modules) {
        super(fieldInfo, gyro);
        this.modules = modules;

        Translation2d[] modulePositions = new Translation2d[modules.length];
        for (int i = 0; i < modules.length; i++) {
            modulePositions[i] = modules[i].position;
        }

        kinematics = new SwerveDriveKinematics(modulePositions);
        odometry =
                new SwerveDriveOdometry(
                        kinematics, gyro.getAngle().ccw().rotation2d(), getModulePositions());

        stopPosition = StopPosition.FORWARD;
        setBrakeMode(true);
    }

    private void setModuleStates(SwerveModuleState[] states) {
        for (int i = 0; i < modules.length; i++) {
            modules[i].setState(states[i]);
        }
    }

    private static final Pose2d IDENTITY_POSE = new Pose2d();

    NTEntry<Double> test = new NTDouble("Log/Drive/Module 0 CANCoder", 1234).setTemporary();
    @Override
    public void periodic() {
        super.periodic();
        test.set(modules[0].encoder.getAngle().ccw().deg());
    }

    @Override
    protected void drive(ChassisSpeeds speeds) {
        final double periodicTime = 0.02;

        // "Borrowed" from team 254
        Pose2d robot_pose_vel =
                new Pose2d(
                        speeds.vxMetersPerSecond * periodicTime,
                        speeds.vyMetersPerSecond * periodicTime,
                        Rotation2d.fromRadians(speeds.omegaRadiansPerSecond * periodicTime));
        Twist2d twist_vel = IDENTITY_POSE.log(robot_pose_vel);
        speeds =
                new ChassisSpeeds(
                        twist_vel.dx / periodicTime,
                        twist_vel.dy / periodicTime,
                        twist_vel.dtheta / periodicTime);

        SwerveModuleState[] states = kinematics.toSwerveModuleStates(speeds);
        SwerveDriveKinematics.desaturateWheelSpeeds(states, 4.0);

        double vx = speeds.vxMetersPerSecond;
        double vy = speeds.vyMetersPerSecond;
        double omega = speeds.omegaRadiansPerSecond;

        if (vx == 0 && vy == 0 && omega == 0) {
            for (int i = 0; i < states.length; i++) {
                states[i] =
                        new SwerveModuleState(0, stopPosition.getForModule(modules[i].position));
            }
        }

        setModuleStates(states);

        ChassisSpeeds estimatedChassis = kinematics.toChassisSpeeds(getModuleStates());
        gyro.setSimAngle(
                gyro.getSimAngle()
                        .ccw()
                        .add(CCWAngle.rad(estimatedChassis.omegaRadiansPerSecond * 0.02)));

        odometry.update(gyro.getAngle().ccw().rotation2d(), getModulePositions());
    }

    /** Calibrates the swerve modules' CanCoder offsets. */
    public void calibrateOffsets() {
        // Reset each of the modules so that the current position is 0
        for (SwerveModule module : modules) {
            module.calibrate();
        }
    }

    @Override
    protected Pose2d getOdometryPose() {
        return odometry.getPoseMeters();
    }

    @Override
    protected void setOdometryPose(Pose2d pose) {
        odometry.resetPosition(gyro.getAngle().ccw().rotation2d(), getModulePositions(), pose);
    }

    @Override
    public boolean isMoving() {
        ChassisSpeeds currentMovement = kinematics.toChassisSpeeds(getModuleStates());
        Translation2d translation =
                new Translation2d(
                        currentMovement.vxMetersPerSecond, currentMovement.vyMetersPerSecond);
        double chassisVelocity = translation.getNorm();
        return chassisVelocity > IS_MOVING_THRESH
                || currentMovement.omegaRadiansPerSecond > IS_MOVING_TURN_THRESH;
    }

    @Override
    public void setBrakeMode(boolean brake) {
        for (SwerveModule module : modules) {
            module.setBrakeMode(brake);
        }
    }

    @Override
    protected void stop() {
        for (SwerveModule module : modules) {
            module.stop();
        }
    }

    private void cheatChassisSpeeds(SwerveModuleState[] states) {
        addChassisSpeeds(kinematics.toChassisSpeeds(states));
    }

    @Override
    protected BaseAutoBuilder createAutoBuilder(Map<String, Command> eventMap) {
        // Create the AutoBuilder. This only needs to be created once when robot code
        // starts, not every time you want to create an auto command. A good place to
        // put this is in RobotContainer along with your subsystems.
        return new SwerveAutoBuilder(
                SwerveDrive.this::getOdometryPose, // Pose2d supplier
                SwerveDrive.this
                        ::resetPoseInternal, // Pose2d consumer, used to reset odometry at the
                                             // beginning of auto
                kinematics, // SwerveDriveKinematics
                new PIDConstants(
                        0.0, 0.0,
                        0.0), // PID constants to correct for translation error (used to create the
                              // X
                // and Y PID controllers)
                new PIDConstants(
                        2.0, 0.0,
                        0.0), // PID constants to correct for rotation error (used to create the
                // rotation controller)
                SwerveDrive.this
                        ::cheatChassisSpeeds, // Module states consumer used to output to the drive
                                              // subsystem
                eventMap,
                fieldInfo.getSymmetry() == FieldSymmetry.LATERAL,
                SwerveDrive
                        .this // The drive subsystem. Used to properly set the requirements of path
                              // following
                // commands
                );
    }

    public SwerveModulePosition[] getModulePositions() {
        SwerveModulePosition[] positions = new SwerveModulePosition[modules.length];
        for (int i = 0; i < modules.length; i++) {
            positions[i] = modules[i].getPosition();
        }

        return positions;
    }

    public SwerveModuleState[] getModuleStates() {
        SwerveModuleState[] states = new SwerveModuleState[modules.length];
        for (int i = 0; i < modules.length; i++) {
            states[i] = modules[i].getState();
        }

        return states;
    }

    public StopPosition getStopPosition() {
        return stopPosition;
    }

    public void setStopPosition(StopPosition stopPosition) {
        this.stopPosition = stopPosition;
    }

    public SwerveModule[] getModules() {
        return modules;
    }
}
