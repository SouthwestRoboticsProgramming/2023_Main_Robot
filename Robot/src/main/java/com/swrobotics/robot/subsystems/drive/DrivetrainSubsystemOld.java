package com.swrobotics.robot.subsystems.drive;

import com.swrobotics.lib.drive.swerve.SwerveDrive;
import com.swrobotics.lib.drive.swerve.SwerveModule;
import com.swrobotics.lib.drive.swerve.SwerveModuleAttributes;
import com.swrobotics.lib.encoder.CanCoder;
import com.swrobotics.lib.encoder.Encoder;
import com.swrobotics.lib.field.FieldInfo;
import com.swrobotics.lib.gyro.NavXGyroscope;
import com.swrobotics.lib.motor.FeedbackMotor;
import com.swrobotics.lib.motor.ctre.TalonFXMotor;
import com.swrobotics.lib.net.NTBoolean;
import com.swrobotics.lib.net.NTDouble;
import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.CCWAngle;
import com.swrobotics.mathlib.CWAngle;
import com.swrobotics.mathlib.MathUtil;
//import com.swrobotics.robot.subsystems.SwitchableSubsystemBase;
import edu.wpi.first.math.estimator.SwerveDrivePoseEstimator;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.SPI.Port;
import edu.wpi.first.wpilibj.smartdashboard.*;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import org.littletonrobotics.junction.Logger;

import static com.swrobotics.robot.subsystems.drive.DrivetrainConstants.*;

/*
 * Calibration instructions:
 * Align all wheel to face forward with bevel gears to the right
 * Use straight edge such as a meter stick to further align the wheels to each other
 * Deploy code
 * Look at RioLog and type those numbers into the module declarations
 */

public class DrivetrainSubsystemOld {

//    // Toggle to calibrate all of the modules in one click
//    private static final NTBoolean CALIBRATE = new NTBoolean("Swerve/Calibrate", false);
//
//    // Initialize a NavX2 over MXP port
//    private final AHRS gyro = new AHRS(Port.kMXP);
//    private Rotation2d gyroOffset = new Rotation2d(0); // Subtracted to get angle
//
//    private final SwerveModule[] modules;
//
//    private final SwerveDrivePoseEstimator odometry;
//
//    private Translation2d centerOfRotation = new Translation2d();
//
//    private StopPosition stopPosition = StopPosition.COAST;
//
//    private Translation2d translation = new Translation2d();
//    private Rotation2d rotation = new Rotation2d();
//    private ChassisSpeeds speeds = new ChassisSpeeds();
//
//    private int activePathPlannerCommands = 0;
//
//    public DrivetrainSubsystemOld() {
//
//        // Configure modules using currently selected options
//        modules = new SwerveModule[] {
//                // For now, each positional offset is 0.0, this will be changed later FIXME
//                new SwerveModule(
//                        MODULES[0],
//                        FRONT_LEFT_POSITION,
//                        0.0), // Front left
//                new SwerveModule(
//                        MODULES[1],
//                        FRONT_RIGHT_POSITION,
//                        90.0), // Front right
//                new SwerveModule(
//                        MODULES[2],
//                        BACK_LEFT_POSITION,
//                        180.0), // Back left
//                new SwerveModule(
//                        MODULES[3],
//                        BACK_RIGHT_POSITION,
//                        270.0) // Back right
//        };
//
//        // TODO: Proper brake mode
//
//        // Calibrate gyro on boot
//        gyro.calibrate();
//
//        // Create a system to measure where the robot is
//        odometry = new SwerveDrivePoseEstimator(kinematics, getGyroscopeRotation(), getModulePositions(), new Pose2d());
//
//        CALIBRATE.onChange(() -> calibrate());
//=======
//public final class DrivetrainSubsystem extends SwerveDrive {
//    public static final FieldInfo FIELD = FieldInfo.CHARGED_UP_2023;
//
//    private static final SwerveModuleInfo[] SELECTABLE_MODULES =
//            new SwerveModuleInfo[] {
//                new SwerveModuleInfo("Module 0", 9, 5, 1, 38.41), // Default front left
//                new SwerveModuleInfo("Module 1", 10, 6, 2, 185.45), // Default front right
//                new SwerveModuleInfo("Module 2", 11, 7, 3, 132.63), // Default back left
//                new SwerveModuleInfo("Module 3", 12, 8, 4, 78.93) // Default back right
//            };
//
//    private static final SendableChooser<SwerveModuleInfo> FRONT_LEFT_SELECT =
//            new SendableChooser<>();
//    private static final SendableChooser<SwerveModuleInfo> FRONT_RIGHT_SELECT =
//            new SendableChooser<>();
//    private static final SendableChooser<SwerveModuleInfo> BACK_LEFT_SELECT =
//            new SendableChooser<>();
//    private static final SendableChooser<SwerveModuleInfo> BACK_RIGHT_SELECT =
//            new SendableChooser<>();
//
//    static {
//        SmartDashboard.putData("Front Left Module", FRONT_LEFT_SELECT);
//        SmartDashboard.putData("Front Right Module", FRONT_RIGHT_SELECT);
//        SmartDashboard.putData("Back Left Module", BACK_LEFT_SELECT);
//        SmartDashboard.putData("Back Right Module", BACK_RIGHT_SELECT);
//
//        for (SwerveModuleInfo info : SELECTABLE_MODULES) {
//            FRONT_LEFT_SELECT.addOption(info.name, info);
//            FRONT_RIGHT_SELECT.addOption(info.name, info);
//            BACK_LEFT_SELECT.addOption(info.name, info);
//            BACK_RIGHT_SELECT.addOption(info.name, info);
//        }
//
//        FRONT_LEFT_SELECT.setDefaultOption(SELECTABLE_MODULES[0].name, SELECTABLE_MODULES[0]);
//        FRONT_RIGHT_SELECT.setDefaultOption(SELECTABLE_MODULES[1].name, SELECTABLE_MODULES[1]);
//        BACK_LEFT_SELECT.setDefaultOption(SELECTABLE_MODULES[2].name, SELECTABLE_MODULES[2]);
//        BACK_RIGHT_SELECT.setDefaultOption(SELECTABLE_MODULES[3].name, SELECTABLE_MODULES[3]);
//>>>>>>> 2ecdd5a9f6610fccea9896f015a0fc5fd119a103
//    }
//
//    private static final double DRIVETRAIN_TRACKWIDTH_METERS = Units.inchesToMeters(18.5);
//    private static final double DRIVETRAIN_WHEELBASE_METERS = DRIVETRAIN_TRACKWIDTH_METERS;
//
//    private static final NTBoolean CALIBRATE = new NTBoolean("Swerve/Calibrate", false);
//
//    private static final NTDouble TURN_KP = new NTDouble("Swerve/Modules/Turn kP", 0.2);
//    private static final NTDouble TURN_KI = new NTDouble("Swerve/Modules/Turn kI", 0.0);
//    private static final NTDouble TURN_KD = new NTDouble("Swerve/Modules/Turn kD", 0.1);
//
//    public final Field2d field = new Field2d();
//    private final FieldObject2d ppPose = field.getObject("PathPlanner pose");
//    private final StateVisualizer stateVisualizer;
//
//    private static SwerveModule makeModule(SwerveModuleInfo info, double x, double y) {
//        FeedbackMotor driveMotor = new TalonFXMotor(info.driveMotorID);
//        FeedbackMotor turnMotor = new TalonFXMotor(info.turnMotorID);
//        Encoder encoder = new CanCoder(info.encoderID).getAbsolute();
//
//        turnMotor.setPID(TURN_KP, TURN_KI, TURN_KD);
//
//        return new SwerveModule(
//                SwerveModuleAttributes.SDS_MK4_L1,
//                driveMotor,
//                turnMotor,
//                encoder,
//                new Translation2d(
//                        x * DRIVETRAIN_TRACKWIDTH_METERS / 2, y * DRIVETRAIN_WHEELBASE_METERS / 2),
//                info.offset);
//    }
//
//    public DrivetrainSubsystem() {
//        super(
//                FIELD,
//                new NavXGyroscope(Port.kMXP),
//                makeModule(FRONT_LEFT_SELECT.getSelected(), 1, 1),
//                makeModule(FRONT_RIGHT_SELECT.getSelected(), 1, -1),
//                makeModule(BACK_LEFT_SELECT.getSelected(), -1, 1),
//                makeModule(BACK_RIGHT_SELECT.getSelected(), -1, -1));
//
//        SmartDashboard.putData("Field", field);
//
//        stateVisualizer = new StateVisualizer("Swerve", DRIVETRAIN_TRACKWIDTH_METERS * 2, this);
//    }
//
//<<<<<<< HEAD
//    // Get the gyroscope without any offsets etc.
//    private Rotation2d getRawGyroscopeRotation() {
//        // return gyro.getRotation2d();
//        return Rotation2d.fromDegrees(gyro.getAngle());
//    }
//
//    /*
//     * Representing non-yaw tilt as a translation helps to drive in the correct
//     * direction to balance
//     */
//    public Translation2d getTiltAsTranslation() {
//        return new Translation2d(gyro.getPitch(), -gyro.getRoll());
//    }
//
//    public void zeroGyroscope() {
//        setGyroscopeRotation(new Rotation2d());
//    }
//
//    /**
//     * @param newRotation New gyro rotation, CCW +
//     */
//    private void setGyroscopeRotation(Rotation2d newRotation) {
//        gyroOffset = getRawGyroscopeRotation().plus(newRotation);
//    }
//
//    // Set the desired movement of the drivebase
//    public void setChassisSpeeds(ChassisSpeeds speeds) {
//        this.speeds = speeds;
//    }
//
//    // Set the desired movement with just translation (for auto features)
//    public void setTargetTranslation(Translation2d targetTranslation, boolean fieldRelative) {
//        translation = targetTranslation;
//
//        if (fieldRelative) {
//            translation = translation.rotateBy(getPose().getRotation().times(-1));
//        }
//    }
//
//    // Set the desired movement with just rotation (for auto features)
//    public void setTargetRotation(Rotation2d targeRotation) {
//        rotation = targeRotation;
//    }
//
//    /**
//     * Gets the field-relative WPI pose of the robot.
//     *
//     * @return The estimated pose of the robot
//     */
//    public Pose2d getPose() {
//        // If on blue alliance or not running PathPlanner, pose is correct already
//        if (DriverStation.getAlliance() == DriverStation.Alliance.Blue || !isPathPlannerRunning())
//            return getPathPlannerPose();
//
//        // Otherwise, we need to flip the pose to be correct
//        Pose2d currentPose = getPathPlannerPose();
//
//        // Undo PathPlanner pose flipping vertically
//        Pose2d asBlue = new Pose2d(
//                new Translation2d(
//                        currentPose.getX(), FIELD_HEIGHT_METERS - currentPose.getY()),
//                currentPose.getRotation().times(-1));
//
//        return flipForAlliance(asBlue);
//    }
//
//    /**
//     * Resets the current odometry pose. Do not use while PathPlanner is running!
//     *
//     * @param newPose new pose measurement to calibrate
//     */
//    public void resetPose(Pose2d newPose) {
//        setGyroscopeRotation(newPose.getRotation()); // Resetting pose recalibrates gyro!
//        odometry.resetPosition(getGyroscopeRotation(), getModulePositions(), newPose);
//    }
//
//    private void setModuleStates(SwerveModuleState[] states) {
//        for (int i = 0; i < modules.length; i++) {
//            modules[i].setState(states[i]);
//        }
//    }
//
//    public SwerveModuleState[] getModuleStates() {
//        SwerveModuleState[] states = new SwerveModuleState[modules.length];
//        for (int i = 0; i < modules.length; i++) {
//            states[i] = modules[i].getState();
//        }
//
//        return states;
//    }
//
//    public SwerveModulePosition[] getModulePositions() {
//        SwerveModulePosition[] positions = new SwerveModulePosition[modules.length];
//        for (int i = 0; i < modules.length; i++) {
//            positions[i] = modules[i].getPosition();
//        }
//
//        return positions;
//    }
//
//    public void setCenterOfRotation(Translation2d centerOfRotation) {
//        this.centerOfRotation = centerOfRotation;
//    }
//
//    public void resetCenterOfRotation() {
//        centerOfRotation = new Translation2d();
//    }
//
//    public Translation2d getCenterOfRotation() {
//        return centerOfRotation;
//    }
//
//    public void setStopPosition(StopPosition position) {
//        stopPosition = position;
//    }
//
//    public StopPosition getStopPosition() {
//        return stopPosition;
//    }
//
//    public void setBrakeMode(boolean brake) {
//        for (SwerveModule module : modules) {
//            module.setBrakeMode(brake);
//        }
//    }
//
//    // Used to work around a semi-broken PathPlanner feature
//    private void cheatChassisSpeeds(SwerveModuleState[] states) {
//        setChassisSpeeds(kinematics.toChassisSpeeds(states));
//    }
//
//    public SwerveAutoBuilder getAutoBuilder(HashMap<String, Command> eventMap) {
//        // Create the AutoBuilder. This only needs to be created once when robot code
//        // starts, not every time you want to create an auto command. A good place to
//        // put this is in RobotContainer along with your subsystems.
//        SwerveAutoBuilder autoBuilder = new SwerveAutoBuilder(
//                this::getPathPlannerPose, // Pose2d supplier
//                this::resetPose, // Pose2d consumer, used to reset odometry at the
//                // beginning of auto
//                kinematics, // SwerveDriveKinematics
//                new PIDConstants(
//                        0.0, 0.0, // FIXME: Closed loop here instead
//                        0.0), // PID constants to correct for translation error (used to
//                // create the X
//                // and Y PID controllers)
//                new PIDConstants(
//                        2.0, 0.0,
//                        0.0), // PID constants to correct for rotation error (used to create
//                // the
//                // rotation controller)
//                this::cheatChassisSpeeds, // Module states consumer used to output to
//                // the drive subsystem
//                eventMap,
//                true,
//                this // The drive subsystem. Used to properly set the requirements of path
//        // following
//        // commands
//        ) {
//            @Override
//            public CommandBase fullAuto(List<PathPlannerTrajectory> trajectorySet) {
//                return new SequentialCommandGroup(
//                        new InstantCommand(() -> activePathPlannerCommands++),
//                        super.fullAuto(trajectorySet) // Run the path
//                )
//                        .finallyDo(
//                                (cancelled) -> {
//                                    // If no longer running PathPlanner, fix pose
//                                    if (activePathPlannerCommands == 1)
//                                        DrivetrainSubsystem.this.resetPose(getPose());
//                                    activePathPlannerCommands--; // Decrement after so
//                                    // getPose() returns good
//                                    // pose above
//                                });
//            }
//        };
//
//        return autoBuilder;
//    }
//
//    /**
//     * Gets the pose that PathPlanner uses that is transformed when on red alliance
//     *
//     * @return the wacky pose
//     */
//    private Pose2d getPathPlannerPose() {
//        return odometry.getEstimatedPosition();
//    }
//
//    private boolean isPathPlannerRunning() {
//        return activePathPlannerCommands > 0;
//    }
//
//    // Calibrates the swerve modules when they are rotated correctly
//    private void calibrate() {
//        CALIBRATE.set(false); // Reset the NT value
//
//        // Reset each of the modules so that the current position is 0
//        for (SwerveModule module : modules) {
//            module.calibrate();
//        }
//
//        System.out.println("<---------------------------------------------------------->");
//        System.out.println("<--- Make sure to copy down new offsets into constants! --->");
//        System.out.println("<---------------------------------------------------------->");
//    }
//
//    // TODO: Determine if we have climbed to automatically coast in if we haven't
//    @Override
//    protected void onDisable() {
//        // Stop the modules
//        for (SwerveModule module : modules) {
//            module.stop();
//        }
//
//        // Clear setpoint logs
//        Logger.getInstance().recordOutput("SwerveStates/Setpoints", new double[] {});
//    }
//
//    @Override
//    public void periodic() {
//        // Log gyro data
//        Logger.getInstance().recordOutput("Gyro/RawPitch", gyro.getPitch());
//        Logger.getInstance().recordOutput("Gyro/RawYaw", gyro.getYaw());
//        Logger.getInstance().recordOutput("Gyro/RawRoll", gyro.getRoll());
//        Logger.getInstance().recordOutput("Gyro/OffsetAmountDeg", gyroOffset.getDegrees());
//
//        // TODO: Characterization mode
//
//        // Check if it should use auto for some or all of the movement
//        if (translation.getNorm() != 0.0) {
//            speeds.vxMetersPerSecond = translation.getX();
//            speeds.vyMetersPerSecond = translation.getY();
//        }
//=======
//    public Translation2d getTiltAsTranslation() {
//        NavXGyroscope gyro = (NavXGyroscope) this.gyro;
//        return new Translation2d(gyro.getPitch(), -gyro.getRoll());
//    }
//
//    @Override
//    public void periodic() {
//        super.periodic();
//>>>>>>> 2ecdd5a9f6610fccea9896f015a0fc5fd119a103
//
//        ppPose.setPose(getOdometryPose());
//        field.setRobotPose(getPose());
//        stateVisualizer.update();
//
//<<<<<<< HEAD
//        SwerveModuleState[] states = kinematics.toSwerveModuleStates(speeds);
//        SwerveDriveKinematics.desaturateWheelSpeeds(states, 4.0);
//
//        double vx = speeds.vxMetersPerSecond;
//        double vy = speeds.vyMetersPerSecond;
//        double omega = speeds.omegaRadiansPerSecond;
//        if (vx == 0 && vy == 0 && omega == 0) {
//            if (stopPosition != StopPosition.COAST) {
//                states = stopPosition.getStates();
//            } else { // Keep going in the same direction
//                states = getModuleStates();
//                // Remove any velocity
//                for (int i = 0; i < states.length; i++) {
//                    states[i] = new SwerveModuleState(0.0, getModuleStates()[i].angle);
//                }
//            }
//        }
//
//        setModuleStates(states);
//
//        // Reset the ChassisSpeeds for next iteration
//        speeds = new ChassisSpeeds();
//
//        // Reset auto rotation and translation for next iteration
//        translation = new Translation2d();
//        rotation = new Rotation2d();
//
//        // Freshly estimated the new rotation based off of the wheels
//        if (RobotBase.isSimulation()) {
//            ChassisSpeeds estimatedChassis = kinematics.toChassisSpeeds(getModuleStates());
//            gyroOffset = gyroOffset.plus(new Rotation2d(estimatedChassis.omegaRadiansPerSecond * 0.02));
//            odometry.update(gyroOffset, getModulePositions());
//        } else {
//            odometry.update(getGyroscopeRotation(), getModulePositions());
//        }
//
//        // Log states
//        Logger.getInstance().recordOutput("SwerveStates/Setpoints", states);
//        Logger.getInstance().recordOutput("SwerveStates/Measured", getModuleStates());
//
//        // Log odometry pose
//        Logger.getInstance().recordOutput("Odometry/Robot2d", getPose());
//    }
//
//    public static Angle getAllianceForward() {
//        return DriverStation.getAlliance() == DriverStation.Alliance.Blue
//                ? Angle.ZERO
//                : CCWAngle.deg(180);
//    }
//
//    public static Angle getAllianceReverse() {
//        return DriverStation.getAlliance() == DriverStation.Alliance.Blue
//                ? CWAngle.deg(180)
//                : Angle.ZERO;
//    }
//
//    // Used to flip for blue alliance
//    private static final double FIELD_WIDTH_METERS = Units.inchesToMeters(54 * 12 + 1);
//    private static final double FIELD_HEIGHT_METERS = 8.02;
//
//    public static Pose2d flipForAlliance(Pose2d asBlue) {
//        if (DriverStation.getAlliance() == DriverStation.Alliance.Blue)
//            return asBlue;
//
//        // Flip horizontally to be on red alliance side
//        return new Pose2d(
//                new Translation2d(FIELD_WIDTH_METERS - asBlue.getX(), asBlue.getY()),
//                new Rotation2d(
//                        MathUtil.wrap(
//                                Math.PI - asBlue.getRotation().getRadians(), 0, 2 * Math.PI)));
//=======
//        if (CALIBRATE.get()) {
//            CALIBRATE.set(
//                    false); // Instantly set back so that it doesn't calibrate more than needed
//            calibrateOffsets();
//        }
//    }
//
//    public static final class StateVisualizer {
//        private final SwerveDrive drive;
//        private final FieldObject2d fieldWheels;
//        private final MechanismLigament2d[] ligaments;
//
//        public StateVisualizer(String name, double size, DrivetrainSubsystem drive) {
//            this.drive = drive;
//
//            Mechanism2d m = new Mechanism2d(size, size);
//            SmartDashboard.putData(name, m);
//
//            SwerveModule[] modules = drive.getModules();
//
//            ligaments = new MechanismLigament2d[modules.length];
//            for (int i = 0; i < modules.length; i++) {
//                SwerveModule module = modules[i];
//                MechanismRoot2d root =
//                        m.getRoot(
//                                String.valueOf(i),
//                                module.position.getX() + size / 2,
//                                module.position.getY() + size / 2);
//                ligaments[i] = new MechanismLigament2d(i + " Vector", 0.2, 0);
//                root.append(ligaments[i]);
//            }
//
//            fieldWheels = drive.field.getObject("Swerve Modules");
//        }
//
//        public void update() {
//            SwerveModule[] modules = drive.getModules();
//            SwerveModuleState[] states = drive.getModuleStates();
//
//            Pose2d robotPose = drive.getPose();
//
//            Pose2d[] fieldPoses = new Pose2d[ligaments.length];
//            for (int i = 0; i < ligaments.length; i++) {
//                MechanismLigament2d ligament = ligaments[i];
//                SwerveModuleState state = states[i];
//                ligament.setAngle(state.angle.getDegrees());
//                ligament.setLength(
//                        state.speedMetersPerSecond / 4.11
//                                + Math.copySign(0.05, state.speedMetersPerSecond));
//
//                Rotation2d outputAngle;
//                if (state.speedMetersPerSecond == 0) {
//                    outputAngle = state.angle;
//                } else {
//                    outputAngle =
//                            new Translation2d(state.speedMetersPerSecond, 0)
//                                    .rotateBy(state.angle)
//                                    .getAngle();
//                }
//
//                SwerveModule module = modules[i];
//                Pose2d fieldPose =
//                        new Pose2d(
//                                robotPose
//                                        .getTranslation()
//                                        .plus(module.position.rotateBy(robotPose.getRotation())),
//                                outputAngle.plus(robotPose.getRotation()));
//                fieldPoses[i] = fieldPose;
//            }
//
//            fieldWheels.setPoses(fieldPoses);
//        }
//>>>>>>> 2ecdd5a9f6610fccea9896f015a0fc5fd119a103
//    }
}
