package com.swrobotics.robot.subsystems.drive;

import com.kauailabs.navx.frc.AHRS;
import com.pathplanner.lib.PathPlannerTrajectory;
import com.pathplanner.lib.auto.PIDConstants;
import com.pathplanner.lib.auto.SwerveAutoBuilder;
import com.swrobotics.lib.net.NTBoolean;
import com.swrobotics.lib.net.NTDouble;
import com.swrobotics.lib.net.NTEntry;
import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.CCWAngle;
import com.swrobotics.mathlib.CWAngle;
import com.swrobotics.mathlib.MathUtil;
import com.swrobotics.robot.subsystems.StatusLoggable;
import com.swrobotics.robot.subsystems.StatusLogging;
import com.swrobotics.robot.subsystems.SwitchableSubsystemBase;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveDriveOdometry;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.ADIS16448_IMU;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.SPI.Port;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.FieldObject2d;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.*;

import java.util.HashMap;
import java.util.List;

/*
 * Calibration instructions:
 * Align all wheel to face forward with bevel gears to the right
 * Use straight edge such as a meter stick to further align the wheels to each other
 * Deploy code
 * Look at RioLog and type those numbers into the module declarations
 */

public class DrivetrainSubsystem extends SwitchableSubsystemBase implements StatusLoggable {
    public static Angle getAllianceForward() {
        return DriverStation.getAlliance() == DriverStation.Alliance.Blue
                ? Angle.ZERO
                : CCWAngle.deg(180);
    }

    public static Angle getAllianceReverse() {
        return DriverStation.getAlliance() == DriverStation.Alliance.Blue
                ? CWAngle.deg(180)
                : Angle.ZERO;
    }

    public static Pose2d flipForAlliance(Pose2d asBlue) {
        if (DriverStation.getAlliance() == DriverStation.Alliance.Blue) return asBlue;

        // Flip horizontally to be on red alliance side
        return new Pose2d(
                new Translation2d(FIELD_WIDTH_METERS - asBlue.getX(), asBlue.getY()),
                new Rotation2d(
                        MathUtil.wrap(
                                Math.PI - asBlue.getRotation().getRadians(), 0, 2 * Math.PI)));
    }

    public StatusLogging logger;

    public void initLogging(StatusLogging logger) {
        this.logger = logger;
    }

    // The Stop Position Enum
    public enum StopPosition {
        NONE,
        CROSS,
        CIRCLE,
    }

    /* Modules that could be hot-swapped into a location on the swerve drive */
    private static final SwerveModuleInfo[] SELECTABLE_MODULES =
            new SwerveModuleInfo[] {
                new SwerveModuleInfo("Module 0", 9, 5, 1, 44.121094), // Default front left
                new SwerveModuleInfo("Module 1", 10, 6, 2, 219.111328), // Default front right
                new SwerveModuleInfo("Module 2", 11, 7, 3, 3.515625), // Default back left
                new SwerveModuleInfo("Module 3", 12, 8, 4, 76.201172) // Default back right
            };
    // Currently, no fifth module is built (not enough falcons)

    /* Chooser to select module locations */
    private final SendableChooser<SwerveModuleInfo> FRONT_LEFT_SELECT;
    private final SendableChooser<SwerveModuleInfo> FRONT_RIGHT_SELECT;
    private final SendableChooser<SwerveModuleInfo> BACK_LEFT_SELECT;
    private final SendableChooser<SwerveModuleInfo> BACK_RIGHT_SELECT;

    private static final NTBoolean CALIBRATE = new NTBoolean("Swerve/Calibrate", false);

    public static final double DRIVETRAIN_TRACKWIDTH_METERS = Units.inchesToMeters(18.5);
    public static final double DRIVETRAIN_WHEELBASE_METERS = DRIVETRAIN_TRACKWIDTH_METERS;

    /**
     * The maximum velocity of the robot in meters per second.
     *
     * <p>This is a measure of how fast the robot should be able to drive in a straight line.
     */
    public static final double MAX_ACHIEVABLE_VELOCITY_METERS_PER_SECOND = 4.11; // From SDS website

    // Setting for Robot Stop Position
    private StopPosition stopPosition = StopPosition.NONE;

    public static final double MAX_VELOCITY_METERS_PER_SECOND = 4.0;

    /**
     * The maximum angular velocity of the robot in radians per second.
     *
     * <p>This is a measure of how fast the robot can rotate in place.
     */
    // Here we calculate the theoretical maximum angular velocity. You can also
    // replace this with a measured amount.
    public static final double MAX_ANGULAR_VELOCITY_RADIANS_PER_SECOND =
            MAX_ACHIEVABLE_VELOCITY_METERS_PER_SECOND
                    / (2
                            * Math.PI
                            * Math.hypot(
                                    DRIVETRAIN_TRACKWIDTH_METERS / 2.0,
                                    DRIVETRAIN_WHEELBASE_METERS / 2.0));

    private final SwerveDriveKinematics kinematics =
            new SwerveDriveKinematics(
                    // Front left
                    new Translation2d(
                            DRIVETRAIN_TRACKWIDTH_METERS / 2.0, DRIVETRAIN_WHEELBASE_METERS / 2.0),
                    // Front right
                    new Translation2d(
                            DRIVETRAIN_TRACKWIDTH_METERS / 2.0, -DRIVETRAIN_WHEELBASE_METERS / 2.0),
                    // Back left
                    new Translation2d(
                            -DRIVETRAIN_TRACKWIDTH_METERS / 2.0, DRIVETRAIN_WHEELBASE_METERS / 2.0),
                    // Back right
                    new Translation2d(
                            -DRIVETRAIN_TRACKWIDTH_METERS / 2.0,
                            -DRIVETRAIN_WHEELBASE_METERS / 2.0));

    // Initialize a NavX over MXP port
//    private final ADIS16448_IMU gyro = new ADIS16448_IMU(
//            ADIS16448_IMU.IMUAxis.kZ, Port.kMXP, ADIS16448_IMU.CalibrationTime._4s);
    private final AHRS gyro = new AHRS(Port.kMXP);
    private Rotation2d gyroOffset = new Rotation2d(0); // Subtracted to get angle

    // Create a field sim to view where the odometry thinks we are
    public final Field2d field = new Field2d();
    private final FieldObject2d ppPose = field.getObject("PathPlanner pose");

    private final SwerveModule[] modules;

    private final SwerveDriveOdometry odometry;

    private Translation2d centerOfRotation = new Translation2d();

    private Translation2d translation = new Translation2d();
    private Rotation2d rotation = new Rotation2d();
    private ChassisSpeeds speeds = new ChassisSpeeds();

    private int activePathPlannerCommands;

    public DrivetrainSubsystem() {

        FRONT_LEFT_SELECT = new SendableChooser<SwerveModuleInfo>();
        FRONT_RIGHT_SELECT = new SendableChooser<SwerveModuleInfo>();
        BACK_LEFT_SELECT = new SendableChooser<SwerveModuleInfo>();
        BACK_RIGHT_SELECT = new SendableChooser<SwerveModuleInfo>();

        SmartDashboard.putData("Front Left Module", FRONT_LEFT_SELECT);
        SmartDashboard.putData("Front Right Module", FRONT_RIGHT_SELECT);
        SmartDashboard.putData("Back Left Module", BACK_LEFT_SELECT);
        SmartDashboard.putData("Back Right Module", BACK_RIGHT_SELECT);

        // Add all available modules to each chooser
        for (SwerveModuleInfo info : SELECTABLE_MODULES) {
            FRONT_LEFT_SELECT.addOption(info.name, info);
            FRONT_RIGHT_SELECT.addOption(info.name, info);
            BACK_LEFT_SELECT.addOption(info.name, info);
            BACK_RIGHT_SELECT.addOption(info.name, info);
        }

        FRONT_LEFT_SELECT.setDefaultOption(SELECTABLE_MODULES[0].name, SELECTABLE_MODULES[0]);
        FRONT_RIGHT_SELECT.setDefaultOption(SELECTABLE_MODULES[1].name, SELECTABLE_MODULES[1]);
        BACK_LEFT_SELECT.setDefaultOption(SELECTABLE_MODULES[2].name, SELECTABLE_MODULES[2]);
        BACK_RIGHT_SELECT.setDefaultOption(SELECTABLE_MODULES[3].name, SELECTABLE_MODULES[3]);

        // Configure modules using currently selected options
        modules =
                new SwerveModule[] {
                    // For now, each positional offset is 0.0, this will be changed later FIXME
                    new SwerveModule(
                            FRONT_LEFT_SELECT.getSelected(),
                            new Translation2d(0.3, 0.3),
                            0.0), // Front left
                    new SwerveModule(
                            FRONT_RIGHT_SELECT.getSelected(),
                            new Translation2d(0.3, -0.3),
                            90.0), // Front right
                    new SwerveModule(
                            BACK_LEFT_SELECT.getSelected(),
                            new Translation2d(-0.3, 0.3),
                            180.0), // Back left
                    new SwerveModule(
                            BACK_RIGHT_SELECT.getSelected(),
                            new Translation2d(-0.3, -0.3),
                            270.0) // Back right
                };

        setBrakeMode(true);

        SmartDashboard.putData("Field", field);
        // field.getObject("traj").setTrajectory(new Trajectory()); // Clear trajectory view

        for (int i = 0; i < 15; i++) {
            printEncoderOffsets();
        }

//        gyro.calibrate();

        // FIXME: Change back to getGyroscopeRotation
        odometry =
                new SwerveDriveOdometry(
                        kinematics, getRawGyroscopeRotation(), getModulePositions());

        // Initially start facing forward
        resetPose(new Pose2d(0, 0, new Rotation2d(0)));

        activePathPlannerCommands = 0;
    }

    // Keep this private - use getPose().getRotation() instead
    private Rotation2d getGyroscopeRotation() {
//        return Rotation2d.fromDegrees(gyro.getAngle()).plus(gyroOffset);
        return gyro.getRotation2d().plus(gyroOffset);
    }

    public Translation2d getTiltAsTranslation() {
        // FIXME: probably wrong
//        return new Translation2d(gyro.getGyroAngleX(), gyro.getGyroAngleY());
        return new Translation2d(gyro.getPitch(), -gyro.getRoll());
    }

    private Rotation2d getRawGyroscopeRotation() {
         return gyro.getRotation2d();
//        return Rotation2d.fromDegrees(-gyro.getAngle());
    }

    public void zeroGyroscope() {
        Pose2d currentPose = getPose();
        resetPose(
                new Pose2d(currentPose.getTranslation(), getAllianceForward().ccw().rotation2d()));
    }

    /**
     * @param newRotation New gyro rotation, CCW +
     */
    private void setGyroscopeRotation(Rotation2d newRotation) {
        gyroOffset = getRawGyroscopeRotation().plus(newRotation);
//        resetPose(new Pose2d(getPose().getTranslation(), getGyroscopeRotation()));
    }

    public void setChassisSpeeds(ChassisSpeeds speeds) {
        this.speeds = speeds;
    }

    public void setTargetTranslation(Translation2d targetTranslation, boolean fieldRelative) {
        translation = targetTranslation;

        if (fieldRelative) {
            translation = translation.rotateBy(getPose().getRotation().times(-1));
        }
    }

    public void setTargetRotation(Rotation2d targeRotation) {
        rotation = targeRotation;
    }

    public void setCenterOfRotation(Translation2d centerOfRotation) {
        this.centerOfRotation = centerOfRotation;
    }

    public void resetCenterOfRotation() {
        centerOfRotation = new Translation2d();
    }

    public Translation2d getCenterOfRotation() {
        return centerOfRotation;
    }

    /**
     * Gets the field-relative WPI pose of the robot.
     *
     * @return the good pose
     */
    public Pose2d getPose() {
        // If on blue alliance or not running PathPlanner, pose is correct already
        if (DriverStation.getAlliance() == DriverStation.Alliance.Blue || !isPathPlannerRunning())
            return getPathPlannerPose();

        // Otherwise, we need to flip the pose to be correct
        Pose2d currentPose = getPathPlannerPose();

        // Undo PathPlanner pose flipping vertically
        Pose2d asBlue =
                new Pose2d(
                        new Translation2d(
                                currentPose.getX(), FIELD_HEIGHT_METERS - currentPose.getY()),
                        currentPose.getRotation().times(-1));

        return flipForAlliance(asBlue);
    }

    /**
     * Gets the pose that PathPlanner uses that is transformed when on red alliance
     *
     * @return the wacky pose
     */
    public Pose2d getPathPlannerPose() {
        return odometry.getPoseMeters();
    }

    public boolean isPathPlannerRunning() {
        return activePathPlannerCommands > 0;
    }

    /**
     * Resets the current odometry pose. Do not use while PathPlanner is running!
     *
     * @param newPose new pose measurement to calibrate
     */
    public void resetPose(Pose2d newPose) {
        setGyroscopeRotation(newPose.getRotation()); // Resetting pose recalibrates gyro!
        odometry.resetPosition(getGyroscopeRotation(), getModulePositions(), newPose);
    }

    private void setModuleStates(SwerveModuleState[] states) {
        for (int i = 0; i < modules.length; i++) {
            modules[i].setState(states[i]);
        }
    }

    public SwerveModuleState[] getModuleStates() {
        SwerveModuleState[] states = new SwerveModuleState[modules.length];
        for (int i = 0; i < modules.length; i++) {
            states[i] = modules[i].getState();
        }

        return states;
    }

    public SwerveModulePosition[] getModulePositions() {
        SwerveModulePosition[] positions = new SwerveModulePosition[modules.length];
        for (int i = 0; i < modules.length; i++) {
            positions[i] = modules[i].getPosition();
        }

        return positions;
    }

    private static final double IS_MOVING_THRESH = 0.1;
    private static final double IS_MOVING_TURN_THRESH = Math.toRadians(15);

    public boolean isMoving() {
        ChassisSpeeds currentMovement = kinematics.toChassisSpeeds(getModuleStates());
        Translation2d translation =
                new Translation2d(
                        currentMovement.vxMetersPerSecond, currentMovement.vyMetersPerSecond);
        double chassisVelocity = translation.getNorm();
        return chassisVelocity > IS_MOVING_THRESH
                || currentMovement.omegaRadiansPerSecond > IS_MOVING_TURN_THRESH;
    }

    public void setStopPosition(StopPosition position) {
        stopPosition = position;
    }

    public StopPosition getStopPosition() {
        return stopPosition;
    }

    public void setBrakeMode(boolean brake) {
        for (SwerveModule module : modules) {
            module.setBrakeMode(brake);
        }
    }

    private void cheatChassisSpeeds(SwerveModuleState[] states) {
        setChassisSpeeds(kinematics.toChassisSpeeds(states));
    }

    private static final double FIELD_WIDTH_METERS = Units.inchesToMeters(54 * 12 + 1);
    private static final double FIELD_HEIGHT_METERS = 8.02;

    public SwerveAutoBuilder getAutoBuilder(HashMap<String, Command> eventMap) {
        // Create the AutoBuilder. This only needs to be created once when robot code
        // starts, not every time you want to create an auto command. A good place to
        // put this is in RobotContainer along with your subsystems.
        SwerveAutoBuilder autoBuilder =
                new SwerveAutoBuilder(
                        this::getPathPlannerPose, // Pose2d supplier
                        this::resetPose, // Pose2d consumer, used to reset odometry at the
                        // beginning of auto
                        kinematics, // SwerveDriveKinematics
                        new PIDConstants(
                                0.0, 0.0,
                                0.0), // PID constants to correct for translation error (used to
                        // create the X
                        // and Y PID controllers)
                        new PIDConstants(
                                2.0, 0.0,
                                0.0), // PID constants to correct for rotation error (used to create
                        // the
                        // rotation controller)
                        this::cheatChassisSpeeds, // Module states consumer used to output to
                        // the drive subsystem
                        eventMap,
                        true,
                        this // The drive subsystem. Used to properly set the requirements of path
                        // following
                        // commands
                        ) {
                    @Override
                    public CommandBase fullAuto(List<PathPlannerTrajectory> trajectorySet) {
                        return new SequentialCommandGroup(
                                        new InstantCommand(() -> activePathPlannerCommands++),
                                        super.fullAuto(trajectorySet) // Run the path
                                        )
                                .finallyDo(
                                        (cancelled) -> {
                                            // If no longer running PathPlanner, fix pose
                                            if (activePathPlannerCommands == 1)
                                                DrivetrainSubsystem.this.resetPose(getPose());
                                            activePathPlannerCommands--; // Decrement after so
                                            // getPose() returns good
                                            // pose above
                                        });
                    }
                };

        return autoBuilder;
    }

    public void printEncoderOffsets() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < modules.length; i++) {
            builder.append("M");
            builder.append(i);
            builder.append(" ");

            builder.append(String.format("%.3f", modules[i].getCalibrationAngle()));
            builder.append(" ");
        }
        System.out.println(builder);
        System.out.println();
    }

    private void calibrate() {
        // Reset each of the modules so that the current position is 0
        for (SwerveModule module : modules) {
            module.calibrate();
        }

        System.out.println("<---------------------------------------------------------->");
        System.out.println("<--- Make sure to copy down new offsets into constants! --->");
        System.out.println("<---------------------------------------------------------->");
    }

    public void showApriltags(AprilTagFieldLayout layout) {

        Pose2d[] poses = new Pose2d[layout.getTags().size()];
        for (int i = 0; i < poses.length; i++) {
            poses[i] = layout.getTags().get(i).pose.toPose2d();
        }

        field.getObject("Apriltags").setPoses(poses);
    }

    public void showCameraPoses(Transform3d... transforms) {
        Pose2d[] poses = new Pose2d[transforms.length];
        for (int i = 0; i < poses.length; i++) {
            poses[i] = new Pose3d().transformBy(transforms[i]).toPose2d();
        }

        field.getObject("Cameras").setPoses(poses);
    }

    @Override
    protected void onDisable() {
        for (SwerveModule module : modules) {
            module.stop();
        }
    }

    NTEntry<Double> LOG_ANGLE = new NTDouble("Log/Gyro Degrees CCW", 0).setTemporary();

    long prevCalibTime = System.currentTimeMillis();

    @Override
    public void periodic() {
        long now = System.currentTimeMillis();
        if (now - prevCalibTime >= 1000) {
            for (SwerveModule module : modules) {
                module.calibrateWithAbsoluteEncoder();
            }
            prevCalibTime = now;
        }

        // Check if it should use auto for some or all of the movement
        if (translation.getNorm() != 0.0) {
            speeds.vxMetersPerSecond = translation.getX();
            speeds.vyMetersPerSecond = translation.getY();
        }

        if (rotation.getRadians() != 0.0) {
            speeds.omegaRadiansPerSecond = rotation.getRadians();
        }

        LOG_ANGLE.set(getPose().getRotation().getDegrees());

        SwerveModuleState[] states = kinematics.toSwerveModuleStates(speeds);
        SwerveDriveKinematics.desaturateWheelSpeeds(states, 4.0);
        double vx = speeds.vxMetersPerSecond;
        double vy = speeds.vyMetersPerSecond;
        double omega = speeds.omegaRadiansPerSecond;
        if (vx == 0 && vy == 0 && omega == 0) {
            switch (stopPosition) {
                case CROSS:
                    states = setCross(states);
                    break;
                case CIRCLE:
                    states = setCircle(states);
                    break;
                default:
                    states = kinematics.toSwerveModuleStates(speeds, centerOfRotation);
                    SwerveDriveKinematics.desaturateWheelSpeeds(states, 4.0);
                    break;
            }
        }
        setModuleStates(states);
        // Reset the ChassisSpeeds for next iteration
        speeds = new ChassisSpeeds();

        // Reset auto rotation and translation for next iteration
        translation = new Translation2d();
        rotation = new Rotation2d();

        // Freshly estimated the new rotation based off of the wheels
        if (RobotBase.isSimulation()) {
            ChassisSpeeds estimatedChassis = kinematics.toChassisSpeeds(getModuleStates());
            gyroOffset =
                    gyroOffset.plus(new Rotation2d(estimatedChassis.omegaRadiansPerSecond * 0.02));
            odometry.update(gyroOffset, getModulePositions());
        } else {
            odometry.update(getGyroscopeRotation(), getModulePositions());
        }

        // ppPose.setPose(getPathPlannerPose());
        // field.setRobotPose(getPose());
        // SnapPositions.showPositions(field);

        // Check if it should calibrate the wheels
        if (CALIBRATE.get()) {
            CALIBRATE.set(
                    false); // Instantly set back so that it doesn't calibrate more than needed
            calibrate();
        }
    }

    private SwerveModuleState[] setCross(SwerveModuleState[] states) {
        states[0] = new SwerveModuleState(0, Rotation2d.fromDegrees(45));
        states[1] = new SwerveModuleState(0, Rotation2d.fromDegrees(315));
        states[2] = new SwerveModuleState(0, Rotation2d.fromDegrees(135));
        states[3] = new SwerveModuleState(0, Rotation2d.fromDegrees(225));
        return states;
    }

    private SwerveModuleState[] setCircle(SwerveModuleState[] states) {
        states[0] = new SwerveModuleState(0, Rotation2d.fromDegrees(315));
        states[1] = new SwerveModuleState(0, Rotation2d.fromDegrees(45));
        states[2] = new SwerveModuleState(0, Rotation2d.fromDegrees(225));
        states[3] = new SwerveModuleState(0, Rotation2d.fromDegrees(135));
        return states;
    }
}
