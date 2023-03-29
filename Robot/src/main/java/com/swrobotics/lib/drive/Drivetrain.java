package com.swrobotics.lib.drive;

import com.pathplanner.lib.PathConstraints;
import com.pathplanner.lib.PathPlanner;
import com.pathplanner.lib.PathPlannerTrajectory;
import com.pathplanner.lib.PathPoint;
import com.pathplanner.lib.auto.BaseAutoBuilder;
import com.swrobotics.lib.field.FieldInfo;
import com.swrobotics.lib.gyro.Gyroscope;
import com.swrobotics.lib.schedule.SwitchableSubsystemBase;
import com.swrobotics.mathlib.Angle;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for a drivetrain.
 */
public abstract class Drivetrain extends SwitchableSubsystemBase {
    protected final FieldInfo fieldInfo;
    protected final Gyroscope gyro;

    private int activePathPlannerCommands;
    private Map<String, Command> autoEventMap;
    private BaseAutoBuilder autoBuilder;

    private ChassisSpeeds chassisSpeeds;

    /**
     * Creates a new instance of the drivetrain. The gyroscope passed into
     * this constructor should be fully owned by this subsystem, and should
     * not be used outside of it. To get the current orientation of the robot,
     * use {@code getPose().getRotation()}.
     *
     * @param fieldInfo information for the game field
     * @param gyro gyroscope for orientation
     */
    public Drivetrain(FieldInfo fieldInfo, Gyroscope gyro) {
        this.fieldInfo = fieldInfo;
        this.gyro = gyro;

        activePathPlannerCommands = 0;
        autoBuilder = null;
        autoEventMap = new HashMap<>();
        chassisSpeeds = new ChassisSpeeds(0, 0, 0);

        gyro.calibrate();
    }

    /**
     * Recalibrates the gyro to face forward relative to the driver station.
     */
    public void zeroGyroscope() {
        gyro.setAngle(fieldInfo.getAllianceForwardAngle());
    }

    /**
     * Adds control input to the drive base. All calls to this method each
     * periodic are added together to get the final speeds. If this is not
     * a holonomic drive base, the horizontal movement of the chassis speeds
     * should be zero.
     *
     * @param speeds chassis speeds to add
     */
    public void addChassisSpeeds(ChassisSpeeds speeds) {
        chassisSpeeds.vxMetersPerSecond += speeds.vxMetersPerSecond;
        chassisSpeeds.vyMetersPerSecond += speeds.vyMetersPerSecond;
        chassisSpeeds.omegaRadiansPerSecond += speeds.omegaRadiansPerSecond;
    }

    /**
     * Adds a translation input to this periodic cycle.
     *
     * @param translation translation to add
     * @param fieldRelative whether the translation is field relative
     */
    public void addTranslation(Translation2d translation, boolean fieldRelative) {
        if (fieldRelative)
            translation = translation.rotateBy(getPose().getRotation().times(-1));

        chassisSpeeds.vxMetersPerSecond += translation.getX();
        chassisSpeeds.vyMetersPerSecond += translation.getY();
    }

    /**
     * Adds a rotation input to this periodic cycle.
     *
     * @param rotation rotation to add
     */
    public void addRotation(Rotation2d rotation) {
        chassisSpeeds.omegaRadiansPerSecond += rotation.getRadians();
    }

    protected abstract Pose2d getOdometryPose();
    protected abstract void setOdometryPose(Pose2d pose);

    /**
     * Gets the field-relative pose of the robot.
     *
     * @return the current pose of the robot
     */
    public Pose2d getPose() {
        // If on blue alliance or not running PathPlanner, pose is correct already
        if (DriverStation.getAlliance() == DriverStation.Alliance.Blue || !isPathPlannerRunning())
            return getOdometryPose();

        // Otherwise, we need to flip the pose to be correct
        Pose2d currentPose = getOdometryPose();

        // FIXME: Builder might not flip it (i.e. rotational symmetry)
        // Undo PathPlanner pose flipping vertically
        Pose2d asBlue = new Pose2d(
                new Translation2d(currentPose.getX(), fieldInfo.getHeight() - currentPose.getY()),
                currentPose.getRotation().times(-1)
        );

        return fieldInfo.flipPoseForAlliance(asBlue);
    }

    /**
     * Resets the current odometry pose. This method should not be used
     * while a PathPlanner command is running.
     *
     * @param currentPose new pose measurement to calibrate
     */
    public void resetPose(Pose2d currentPose) {
        if (isPathPlannerRunning()) {
            DriverStation.reportWarning("Attempted to reset pose while PathPlanner is running", true);
            return;
        }

        resetPoseInternal(currentPose);
    }

    /**
     * Resets the current odometry pose regardless of whether PathPlanner is
     * running. If calling this function during PathPlanner, make sure the pose
     * is in PathPlanner coordinates.
     *
     * @param currentPose current pose to set
     */
    protected void resetPoseInternal(Pose2d currentPose) {
        gyro.setAngle(Angle.fromRotation2d(currentPose.getRotation()));
        setOdometryPose(currentPose);
    }

    /**
     * Gets whether the robot is currently trying to move.
     *
     * @return whether the drivetrain is moving
     */
    public abstract boolean isMoving();

    /**
     * Sets whether brake mode should be enabled on the drive motors.
     *
     * @param brake whether brake mode is enabled
     */
    public abstract void setBrakeMode(boolean brake);

    // ----- PathPlanner -----

    /**
     * Sets the autonomous event commands for PathPlanner. This will clear any
     * previous events set. This should be called before {@link #createAutoBuilder}
     * is called.
     *
     * @param eventMap new event map
     * @throws IllegalStateException if createAutoBuilder has been called already
     */
    public void setAutoEvents(Map<String, Command> eventMap) {
        if (autoBuilder != null)
            throw new IllegalStateException("Auto builder already initialized");

        autoEventMap = new HashMap<>(eventMap);
    }

    /**
     * Adds an autonomous event for PathPlanner. This should be called before
     * {@link #createAutoBuilder} is called.
     *
     * @param name name of the event
     * @param eventCmd command to run
     * @throws IllegalStateException if createAutoBuilder has been called already
     */
    public void addAutoEvent(String name, Command eventCmd) {
        if (autoBuilder != null)
            throw new IllegalStateException("Auto builder already initialized");

        autoEventMap.put(name, eventCmd);
    }

    /**
     * Creates the auto builder using the specified event map. The auto builder
     * should return commands from {@link BaseAutoBuilder#fullAuto(List)} which
     * call {@link #onPathPlannerStart()} and {@link #onPathPlannerEnd()} when
     * it starts and finishes.
     *
     * @param eventMap event map
     * @return the auto builder
     */
    protected abstract BaseAutoBuilder createAutoBuilder(Map<String, Command> eventMap);

    // Loads path from deployed file
    private static List<PathPlannerTrajectory> getPath(String name) {
        List<PathPlannerTrajectory> path = PathPlanner.loadPathGroup(name, new PathConstraints(2.0, 1.0));
        if (path != null) {
            return path;
        }

        System.out.println("Could not find '" + name + "', using blank path instead");

        // Generate a blank path
        path = new ArrayList<>();
        path.add(PathPlanner.generatePath(new PathConstraints(1.0, 1.0), new ArrayList<PathPoint>() {{
            add(new PathPoint(new Translation2d(), new Rotation2d()));
            add(new PathPoint(new Translation2d(1.0, 0), new Rotation2d()));
        }}));
        return path;
    }

    /**
     * Creates an autonomous command using PathPlanner.
     *
     * @param name name of the path file to load
     * @return autonomous command
     */
    public CommandBase buildPathPlannerAuto(String name) {
        if (autoBuilder == null) {
            autoBuilder = createAutoBuilder(autoEventMap);
        }

        return new SequentialCommandGroup(
                new InstantCommand(this::onPathPlannerStart),
                autoBuilder.fullAuto(getPath(name)) // Run the path
        ).finallyDo((cancelled) -> onPathPlannerEnd());
    }

    /**
     * Gets whether any PathPlanner commands are currently running for this
     * drive base.
     *
     * @return whether any PathPlanner commands are running
     */
    public boolean isPathPlannerRunning() {
        return activePathPlannerCommands > 0;
    }

    /**
     * Should be called when a PathPlanner command starts
     */
    private void onPathPlannerStart() {
        activePathPlannerCommands++;
    }

    /**
     * Should be called when a PathPlanner command ends
     */
    private void onPathPlannerEnd() {
        if (activePathPlannerCommands == 1)
            resetPoseInternal(getPose());

        activePathPlannerCommands--;
    }

    /**
     * Actually sets the drive input for the drive base. Motor output
     * should be set here.
     *
     * @param speeds speeds to drive at
     */
    protected abstract void drive(ChassisSpeeds speeds);

    /**
     * Stops the drive base by setting all motor outputs to zero. If there are
     * motors that would not be stopped by driving at zero chassis speeds, this
     * should be overridden to stop them as well.
     */
    protected void stop() {
        drive(new ChassisSpeeds(0, 0, 0));
    }

    @Override
    public void onDisable() {
        stop();
    }

    @Override
    public void periodic() {
        if (!isEnabled()) {
            stop();
            return;
        }

        drive(chassisSpeeds);
        chassisSpeeds = new ChassisSpeeds(0, 0, 0);
    }
}
