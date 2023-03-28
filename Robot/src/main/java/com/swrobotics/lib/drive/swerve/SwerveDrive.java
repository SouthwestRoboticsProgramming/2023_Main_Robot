package com.swrobotics.lib.drive.swerve;

import com.pathplanner.lib.PathPlannerTrajectory;
import com.pathplanner.lib.auto.BaseAutoBuilder;
import com.pathplanner.lib.auto.PIDConstants;
import com.pathplanner.lib.auto.SwerveAutoBuilder;
import com.swrobotics.lib.drive.HolonomicDrivetrain;
import com.swrobotics.lib.field.FieldInfo;
import com.swrobotics.lib.gyro.Gyroscope;
import com.swrobotics.mathlib.CCWAngle;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.*;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

import java.util.List;
import java.util.Map;

public class SwerveDrive extends HolonomicDrivetrain {
    private static final double IS_MOVING_THRESH = 0.1;
    private static final double IS_MOVING_TURN_THRESH = Math.toRadians(15);

    private final SwerveModule[] modules;

    private final SwerveDriveKinematics kinematics;
    private final SwerveDriveOdometry odometry;

    private StopPosition stopPosition;

    public SwerveDrive(FieldInfo fieldInfo, Gyroscope gyro, SwerveModuleInfo[] moduleInfos, Translation2d[] modulePositions) {
        super(fieldInfo, gyro);

        int moduleCount = moduleInfos.length;
        if (modulePositions.length != moduleCount)
            throw new IllegalArgumentException("Position count does not match info count");

        modules = new SwerveModule[moduleCount];
        for (int i = 0; i < modulePositions.length; i++) {
            Translation2d position = modulePositions[i];

            // FIXME: Calculate positional offset
            modules[i] = new SwerveModule(moduleInfos[i], position, 0);
        }

        kinematics = new SwerveDriveKinematics(modulePositions);
        odometry = new SwerveDriveOdometry(kinematics, gyro.getAngle().ccw().rotation2d(), getModulePositions());

        stopPosition = StopPosition.NONE;
        setBrakeMode(true);
    }

    private void setModuleStates(SwerveModuleState[] states) {
        for (int i = 0; i < modules.length; i++) {
            modules[i].setState(states[i]);
        }
    }

    @Override
    protected void drive(ChassisSpeeds speeds) {
        SwerveModuleState[] states = kinematics.toSwerveModuleStates(speeds);
        SwerveDriveKinematics.desaturateWheelSpeeds(states, 4.0);

        double vx = speeds.vxMetersPerSecond;
        double vy = speeds.vyMetersPerSecond;
        double omega = speeds.omegaRadiansPerSecond;

        if (vx == 0 && vy == 0 && omega == 0) {
            // TODO: Stop position
        }

        setModuleStates(states);

        if (RobotBase.isSimulation()) {
            ChassisSpeeds estimatedChassis = kinematics.toChassisSpeeds(getModuleStates());
            gyro.setAngle(gyro.getAngle().ccw().add(CCWAngle.rad(estimatedChassis.omegaRadiansPerSecond * 0.02)));
        }
        odometry.update(gyro.getAngle().ccw().rotation2d(), getModulePositions());
    }

    /**
     * Calibrates the swerve modules' CanCoder offsets.
     */
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
        Translation2d translation = new Translation2d(currentMovement.vxMetersPerSecond, currentMovement.vyMetersPerSecond);
        double chassisVelocity = translation.getNorm();
        return chassisVelocity > IS_MOVING_THRESH || currentMovement.omegaRadiansPerSecond > IS_MOVING_TURN_THRESH;
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
                SwerveDrive.this::resetPose, // Pose2d consumer, used to reset odometry at the beginning of auto
                kinematics, // SwerveDriveKinematics
                new PIDConstants(0.0, 0.0, 0.0), // PID constants to correct for translation error (used to create the X
                // and Y PID controllers)
                new PIDConstants(2.0, 0.0, 0.0), // PID constants to correct for rotation error (used to create the
                // rotation controller)
                SwerveDrive.this::cheatChassisSpeeds, // Module states consumer used to output to the drive subsystem
                eventMap,
                true,
                SwerveDrive.this // The drive subsystem. Used to properly set the requirements of path following
                // commands
        ) {
            @Override
            public CommandBase fullAuto(List<PathPlannerTrajectory> trajectorySet) {
                return new SequentialCommandGroup(
                        new InstantCommand(() -> onPathPlannerStart()),
                        super.fullAuto(trajectorySet) // Run the path
                ).finallyDo((cancelled) -> onPathPlannerEnd());
            }
        };
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
}
