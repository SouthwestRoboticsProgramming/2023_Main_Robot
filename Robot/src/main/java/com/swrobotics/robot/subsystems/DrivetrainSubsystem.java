package com.swrobotics.robot.subsystems;

import java.util.HashMap;

import com.kauailabs.navx.frc.AHRS;
import com.pathplanner.lib.auto.PIDConstants;
import com.pathplanner.lib.auto.SwerveAutoBuilder;
import com.swrobotics.lib.net.NTBoolean;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveDriveOdometry;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.SPI.Port;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;


/*
 * Calibration instructions:
 * Align all wheel to face forward with bevel gears to the right
 * Use straight edge such as a meter stick to further align the wheels to each other
 * Deploy code
 * Look at RioLog and type those numbers into the module declarations
 */

public class DrivetrainSubsystem extends SubsystemBase implements StatusLoggable {

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
    private static final SwerveModuleInfo[] SELECTABLE_MODULES = new SwerveModuleInfo[] {
        new SwerveModuleInfo("Module 0", 9, 5, 1, 38.41),  // Default front left
        new SwerveModuleInfo("Module 1", 10, 6, 2, 185.45), // Default front right
        new SwerveModuleInfo("Module 2", 11, 7, 3, 132.63), // Default back left
        new SwerveModuleInfo("Module 3", 12, 8, 4, 78.93)  // Default back right
    };
    // Currently, no fifth module is built (not enough falcons)

    /* Chooser to select module locations */
    private final SendableChooser<SwerveModuleInfo> FRONT_LEFT_SELECT;
    private final SendableChooser<SwerveModuleInfo> FRONT_RIGHT_SELECT;
    private final SendableChooser<SwerveModuleInfo> BACK_LEFT_SELECT;
    private final SendableChooser<SwerveModuleInfo> BACK_RIGHT_SELECT;

    private static final NTBoolean CALIBRATE = new NTBoolean("Swerve/Calibrate", false);

    public static final double DRIVETRAIN_TRACKWIDTH_METERS = 0.3;
    public static final double DRIVETRAIN_WHEELBASE_METERS = 0.3;

    /**
     * The maximum velocity of the robot in meters per second.
     * <p>
     * This is a measure of how fast the robot should be able to drive in a straight
     * line.
     */
    public static final double MAX_ACHIEVABLE_VELOCITY_METERS_PER_SECOND = 4.11; // From SDS website

    // Setting for Robot Stop Position
    private StopPosition stopPosition = StopPosition.NONE;

    public static final double MAX_VELOCITY_METERS_PER_SECOND = 4.0;
    
    /**
     * The maximum angular velocity of the robot in radians per second.
     * <p>
     * This is a measure of how fast the robot can rotate in place.
     */
    // Here we calculate the theoretical maximum angular velocity. You can also
    // replace this with a measured amount.
    public static final double MAX_ANGULAR_VELOCITY_RADIANS_PER_SECOND = MAX_ACHIEVABLE_VELOCITY_METERS_PER_SECOND /
            Math.hypot(DRIVETRAIN_TRACKWIDTH_METERS / 2.0, DRIVETRAIN_WHEELBASE_METERS / 2.0);

    private final SwerveDriveKinematics kinematics = new SwerveDriveKinematics(
            // Front left
            new Translation2d(DRIVETRAIN_TRACKWIDTH_METERS / 2.0, DRIVETRAIN_WHEELBASE_METERS / 2.0),
            // Front right
            new Translation2d(DRIVETRAIN_TRACKWIDTH_METERS / 2.0, -DRIVETRAIN_WHEELBASE_METERS / 2.0),
            // Back left
            new Translation2d(-DRIVETRAIN_TRACKWIDTH_METERS / 2.0, DRIVETRAIN_WHEELBASE_METERS / 2.0),
            // Back right
            new Translation2d(-DRIVETRAIN_TRACKWIDTH_METERS / 2.0, -DRIVETRAIN_WHEELBASE_METERS / 2.0));

    // Initialize a NavX over MXP port
    private final AHRS gyro = new AHRS(Port.kMXP);
    private Rotation2d gyroOffset = new Rotation2d(-Math.PI / 2); // Subtracted to get angle

    // Create a field sim to view where the odometry thinks we are
    public final Field2d field = new Field2d();

    private final SwerveModule[] modules;
    
    private final SwerveDriveOdometry odometry;

    private Translation2d translation = new Translation2d();
    private Rotation2d rotation = new Rotation2d();
    private ChassisSpeeds speeds = new ChassisSpeeds();
    
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
        modules = new SwerveModule[] {
            // For now, each positional offset is 0.0, this will be changed later FIXME
            new SwerveModule(FRONT_LEFT_SELECT.getSelected(), new Translation2d(0.3, 0.3), 0.0), // Front left
            new SwerveModule(FRONT_RIGHT_SELECT.getSelected(), new Translation2d(0.3, -0.3), 90.0),  // Front right
            new SwerveModule(BACK_LEFT_SELECT.getSelected(), new Translation2d(-0.3, 0.3), 180.0),  // Back left
            new SwerveModule(BACK_RIGHT_SELECT.getSelected(), new Translation2d(-0.3, -0.3), 270.0)  // Back right
        };

        setBrakeMode(true);

        SmartDashboard.putData("Field", field);
        // field.getObject("traj").setTrajectory(new Trajectory()); // Clear trajectory view

        for (int i = 0; i < 15; i++) {
            printEncoderOffsets();
        }

        gyro.calibrate();

        // FIXME: Change back to getGyroscopeRotation
        odometry = new SwerveDriveOdometry(kinematics, getRawGyroscopeRotation(), getModulePositions());
    }

    public Rotation2d getGyroscopeRotation() {
        return gyro.getRotation2d().plus(gyroOffset);
    }

    public Translation2d getTiltAsTranslation() {
        return new Translation2d(gyro.getPitch(), -gyro.getRoll());
    }

    private Rotation2d getRawGyroscopeRotation() {
        // return gyro.getRotation2d();
        return Rotation2d.fromDegrees(gyro.getAngle());
    }

    public void zeroGyroscope() {
        setGyroscopeRotation(new Rotation2d());
    }

    /**
     * 
     * @param newRotation New gyro rotation, CCW +
     */
    public void setGyroscopeRotation(Rotation2d newRotation) {
        gyroOffset = getRawGyroscopeRotation().plus(newRotation);
        resetPose(new Pose2d(getPose().getTranslation(), getGyroscopeRotation()));
    }

    public void setChassisSpeeds(ChassisSpeeds speeds) {
        this.speeds = speeds;
    }

    public void setTargetTranslation(Translation2d targetTranslation, boolean fieldRelative) {
        translation = targetTranslation;

        if (fieldRelative) {
            translation.rotateBy(getGyroscopeRotation().times(-1));
        }
    }

    public void setTargetRotation(Rotation2d targeRotation) {
        rotation = targeRotation;
    }

    public Pose2d getPose() {
        return odometry.getPoseMeters();
    }

    public void resetPose(Pose2d newPose) {
        odometry.resetPosition(getGyroscopeRotation(), getModulePositions(), newPose);
    }

    public void setModuleStates(SwerveModuleState[] states) {
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

    public boolean isMoving() {
        ChassisSpeeds currentMovement = kinematics.toChassisSpeeds(getModuleStates());
        Translation2d translation = new Translation2d(currentMovement.vxMetersPerSecond, currentMovement.vyMetersPerSecond);
        double chassisVelocity = translation.getNorm();
        return chassisVelocity > IS_MOVING_THRESH;
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

    public SwerveAutoBuilder getAutoBuilder(HashMap<String, Command> eventMap) {
        // Create the AutoBuilder. This only needs to be created once when robot code
        // starts, not every time you want to create an auto command. A good place to
        // put this is in RobotContainer along with your subsystems.
        SwerveAutoBuilder autoBuilder = new SwerveAutoBuilder(
                this::getPose, // Pose2d supplier
                this::resetPose, // Pose2d consumer, used to reset odometry at the beginning of auto
                kinematics, // SwerveDriveKinematics
                new PIDConstants(0.0, 0.0, 0.0), // PID constants to correct for translation error (used to create the X
                                                 // and Y PID controllers)
                new PIDConstants(2.0, 0.0, 0.0), // PID constants to correct for rotation error (used to create the
                                                 // rotation controller)
                this::cheatChassisSpeeds, // Module states consumer used to output to the drive subsystem
                eventMap,
                this // The drive subsystem. Used to properly set the requirements of path following
                     // commands
        );

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

    @Override
    public void periodic() {
        // Check if it should use auto for some or all of the movement
        if (translation.getNorm() != 0.0) {
            speeds.vxMetersPerSecond = translation.getX();
            speeds.vyMetersPerSecond = translation.getY();
        }

        if (rotation.getRadians() != 0.0) {
            speeds.omegaRadiansPerSecond = rotation.getRadians();
        }

        SwerveModuleState[] states = kinematics.toSwerveModuleStates(speeds);
        SwerveDriveKinematics.desaturateWheelSpeeds(states, 4.0);
        double vx = speeds.vxMetersPerSecond;
        double vy = speeds.vyMetersPerSecond;
        double omega = speeds.omegaRadiansPerSecond;
        if(vx == 0 && vy == 0 && omega == 0) {
           switch (stopPosition) {
               case CROSS:
                  states = setCross(states);
                  break;
               case CIRCLE:
                  states = setCircle(states);
                  break;
               default:
                   states = kinematics.toSwerveModuleStates(speeds);
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
            gyroOffset = gyroOffset.plus(new Rotation2d(estimatedChassis.omegaRadiansPerSecond * 0.02));
            odometry.update(gyroOffset, getModulePositions());
        } else {
            odometry.update(getGyroscopeRotation(), getModulePositions());
        }
        
        field.setRobotPose(getPose());

        // Check if it should calibrate the wheels
        if (CALIBRATE.get()) {
            CALIBRATE.set(false); // Instantly set back so that it doesn't calibrate more than needed
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
