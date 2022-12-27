package com.swrobotics.robot.subsystems;

import java.util.HashMap;

import com.kauailabs.navx.frc.AHRS;
import com.pathplanner.lib.auto.PIDConstants;
import com.pathplanner.lib.auto.SwerveAutoBuilder;
import com.swrobotics.lib.net.NTMultiSelect;
import com.swrobotics.robot.VisionConstants;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveDriveOdometry;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.trajectory.Trajectory;
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

public class DrivetrainSubsystem extends SubsystemBase {

    /* Modules that could be hot-swapped into a location on the swerve drive */
    private static final SwerveModuleInfo[] SELECTABLE_MODULES = new SwerveModuleInfo[] {
        new SwerveModuleInfo("Module 0", 9, 5, 1),  // Default front left
        new SwerveModuleInfo("Module 1", 10, 6, 2), // Default front right
        new SwerveModuleInfo("Module 2", 11, 7, 3), // Default back left
        new SwerveModuleInfo("Module 3", 12, 8, 4)  // Default back right
    }
    // Currently, no fifth module is built (not enough falcons)

    /* Chooser to select module locations */
    private final SendableChooser<SwerveModuleInfo> FRONT_LEFT_SELECT;
    private final SendableChooser<SwerveModuleInfo> FRONT_RIGHT_SELECT;
    private final SendableChooser<SwerveModuleInfo> BACK_LEFT_SELECT;
    private final SendableChooser<SwerveModuleInfo> BACK_RIGHT_SELECT;

    public static final double DRIVETRAIN_TRACKWIDTH_METERS = 0.3; // FIXME - Measure
    public static final double DRIVETRAIN_WHEELBASE_METERS = 0.3; // FIXME - Measure

    /**
     * The maximum velocity of the robot in meters per second.
     * <p>
     * This is a measure of how fast the robot should be able to drive in a straight
     * line.
     */
    // public static final double MAX_VELOCITY_METERS_PER_SECOND = 6380.0 / 60.0 *
    //         SdsModuleConfigurations.MK3_STANDARD.getDriveReduction() *
    //         SdsModuleConfigurations.MK3_STANDARD.getWheelDiameter() * Math.PI;

    public static final double MAX_VELOCITY_METERS_PER_SECOND = 4.0;
    /**
     * The maximum angular velocity of the robot in radians per second.
     * <p>
     * This is a measure of how fast the robot can rotate in place.
     */
    // Here we calculate the theoretical maximum angular velocity. You can also
    // replace this with a measured amount.
    public static final double MAX_ANGULAR_VELOCITY_RADIANS_PER_SECOND = MAX_VELOCITY_METERS_PER_SECOND /
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
    private Rotation2d gyroOffset = new Rotation2d(); // Subtracted to get angle

    // Create a field sim to view where the odometry thinks we are
    public final Field2d field = new Field2d();

    private final SwerveModule[] modules;
    
    private final SwerveDriveOdometry odometry;

    private ChassisSpeeds speeds = new ChassisSpeeds();
    
    public DrivetrainSubsystem() {

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
            new SwerveModule(FRONT_LEFT_SELECT.getSelected(),  44.21, new Translation2d(0.3, 0.3)), // Front left
            new SwerveModule(FRONT_RIGHT_SELECT.getSelected(), 274.13, new Translation2d(0.3, -0.3)),  // Front right
            new SwerveModule(BACK_LEFT_SELECT.getSelected(),   258.14, new Translation2d(-0.3, 0.3)),  // Back left
            new SwerveModule(BACK_RIGHT_SELECT.getSelected(),  218.06, new Translation2d(-0.3, -0.3))  // Back right
        };

        SmartDashboard.putData("Field", field);
        System.out.println("Target Position: " + VisionConstants.DOOR_POSE.toPose2d());
        field.getObject("target").setPose(VisionConstants.DOOR_POSE.toPose2d());
        // field.getObject("traj").setTrajectory(new Trajectory()); // Clear trajectory view

        for (int i = 0; i < 15; i++) {
            printEncoderOffsets();
        }

        // FIXME: Change back to getGyroscopeRotation
        odometry = new SwerveDriveOdometry(kinematics, getRawGyroscopeRotation());
    }

    public Rotation2d getGyroscopeRotation() {
        if (RobotBase.isSimulation()) {
            return getPose().getRotation();
        }
        return gyro.getRotation2d().minus(gyroOffset);
    }

    private Rotation2d getRawGyroscopeRotation() {
        return gyro.getRotation2d();
    }

    public void zeroGyroscope() {
        setGyroscopeRotation(new Rotation2d());
    }

    /**
     * 
     * @param newRotation New gyro rotation, CCW +
     */
    public void setGyroscopeRotation(Rotation2d newRotation) {
        gyroOffset = getRawGyroscopeRotation().minus(newRotation);
        resetPose(new Pose2d(getPose().getTranslation(), getGyroscopeRotation()));
    }

    public void setChassisSpeeds(ChassisSpeeds speeds) {
        System.out.println("Set");
        this.speeds = speeds;
    }

    public void combineChassisSpeeds(ChassisSpeeds addition) {
        speeds = new ChassisSpeeds(
            speeds.vxMetersPerSecond + addition.vxMetersPerSecond,
            speeds.vyMetersPerSecond + addition.vyMetersPerSecond,
            speeds.omegaRadiansPerSecond + addition.omegaRadiansPerSecond
        );
    }

    public Pose2d getPose() {
        return odometry.getPoseMeters();
    }

    public void resetPose(Pose2d newPose) {
        odometry.resetPosition(newPose, getGyroscopeRotation());
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

    private static final double IS_MOVING_THRESH = 0.1;

    public boolean isMoving() {
        ChassisSpeeds currentMovement = kinematics.toChassisSpeeds(getModuleStates());
        Translation2d translation = new Translation2d(currentMovement.vxMetersPerSecond, currentMovement.vyMetersPerSecond);
        double chassisVelocity = translation.getNorm();
        return chassisVelocity > IS_MOVING_THRESH;
    }

    private void doNotResetPose(Pose2d pose) {}

    public SwerveAutoBuilder getAutoBuilder(HashMap<String, Command> eventMap) {
        // Create the AutoBuilder. This only needs to be created once when robot code
        // starts, not every time you want to create an auto command. A good place to
        // put this is in RobotContainer along with your subsystems.
        SwerveAutoBuilder autoBuilder = new SwerveAutoBuilder(
                this::getPose, // Pose2d supplier
                this::resetPose, // Pose2d consumer, used to reset odometry at the beginning of auto
                kinematics, // SwerveDriveKinematics
                new PIDConstants(3.0, 0.0, 0.0), // PID constants to correct for translation error (used to create the X
                                                 // and Y PID controllers)
                new PIDConstants(0.5, 0.0, 0.0), // PID constants to correct for rotation error (used to create the
                                                 // rotation controller)
                this::setModuleStates, // Module states consumer used to output to the drive subsystem
                eventMap,
                this // The drive subsystem. Used to properly set the requirements of path following
                     // commands
        );

        return autoBuilder;
    }

    public void showTrajectory(Trajectory trajectory) {
        field.getObject("traj").setTrajectory(trajectory);
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

    @Override
    public void periodic() {
        // Set this iteration's ChassisSpeeds
        SwerveModuleState[] states = kinematics.toSwerveModuleStates(speeds);
        SwerveDriveKinematics.desaturateWheelSpeeds(states, 4.0);

        setModuleStates(states);

        // Reset the ChassisSpeeds for next iteration
        speeds = new ChassisSpeeds();

        // Freshly estimated the new rotation based off of the wheels
        if (RobotBase.isSimulation()) {
            ChassisSpeeds estimatedChassis = kinematics.toChassisSpeeds(getModuleStates());
            gyroOffset = gyroOffset.plus(new Rotation2d(-estimatedChassis.omegaRadiansPerSecond * 0.02));
            odometry.update(gyroOffset, getModuleStates());
        } else {
            odometry.update(getGyroscopeRotation(), getModuleStates());
        }
        
        
        field.setRobotPose(getPose());
    }
}
