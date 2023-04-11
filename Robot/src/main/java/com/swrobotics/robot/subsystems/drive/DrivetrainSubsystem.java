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
import com.swrobotics.lib.net.NTEntry;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.smartdashboard.*;

import java.util.Arrays;

public final class DrivetrainSubsystem extends SwerveDrive {
    public static final FieldInfo FIELD = FieldInfo.CHARGED_UP_2023;

    private static final SwerveModuleInfo[] SELECTABLE_MODULES = new SwerveModuleInfo[] {
            new SwerveModuleInfo("Module 0", 9, 5, 1, 38.41),  // Default front left
            new SwerveModuleInfo("Module 1", 10, 6, 2, 185.45), // Default front right
            new SwerveModuleInfo("Module 2", 11, 7, 3, 132.63), // Default back left
            new SwerveModuleInfo("Module 3", 12, 8, 4, 78.93)  // Default back right
    };

    private static final SendableChooser<SwerveModuleInfo> FRONT_LEFT_SELECT = new SendableChooser<>();
    private static final SendableChooser<SwerveModuleInfo> FRONT_RIGHT_SELECT = new SendableChooser<>();
    private static final SendableChooser<SwerveModuleInfo> BACK_LEFT_SELECT = new SendableChooser<>();
    private static final SendableChooser<SwerveModuleInfo> BACK_RIGHT_SELECT = new SendableChooser<>();

    static {
        SmartDashboard.putData("Front Left Module", FRONT_LEFT_SELECT);
        SmartDashboard.putData("Front Right Module", FRONT_RIGHT_SELECT);
        SmartDashboard.putData("Back Left Module", BACK_LEFT_SELECT);
        SmartDashboard.putData("Back Right Module", BACK_RIGHT_SELECT);

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
    }

    private static final double DRIVETRAIN_TRACKWIDTH_METERS = Units.inchesToMeters(18.5);
    private static final double DRIVETRAIN_WHEELBASE_METERS = DRIVETRAIN_TRACKWIDTH_METERS;

    private static final NTBoolean CALIBRATE = new NTBoolean("Swerve/Calibrate", false);

    private static final NTDouble TURN_KP = new NTDouble("Swerve/Modules/Turn kP", 0.2);
    private static final NTDouble TURN_KI = new NTDouble("Swerve/Modules/Turn kI", 0.0);
    private static final NTDouble TURN_KD = new NTDouble("Swerve/Modules/Turn kD", 0.1);

    public final Field2d field = new Field2d();
    private final FieldObject2d ppPose = field.getObject("PathPlanner pose");
    private final StateVisualizer stateVisualizer;

    private static SwerveModule makeModule(SwerveModuleInfo info, double x, double y) {
        FeedbackMotor driveMotor = new TalonFXMotor(info.driveMotorID);
        FeedbackMotor turnMotor = new TalonFXMotor(info.turnMotorID);
        Encoder encoder = new CanCoder(info.encoderID).getAbsolute();

        turnMotor.setPID(TURN_KP, TURN_KI, TURN_KD);

        return new SwerveModule(
                SwerveModuleAttributes.SDS_MK4_L1,
                driveMotor,
                turnMotor,
                encoder,
                new Translation2d(x * DRIVETRAIN_TRACKWIDTH_METERS / 2, y * DRIVETRAIN_WHEELBASE_METERS / 2),
                info.offset
        );
    }

    public DrivetrainSubsystem() {
        super(
                FIELD,
                new NavXGyroscope(SPI.Port.kMXP),
                makeModule(FRONT_LEFT_SELECT.getSelected(), 1, 1),
                makeModule(FRONT_RIGHT_SELECT.getSelected(), 1, -1),
                makeModule(BACK_LEFT_SELECT.getSelected(), -1, 1),
                makeModule(BACK_RIGHT_SELECT.getSelected(), -1, -1)
        );

        SmartDashboard.putData("Field", field);

        stateVisualizer = new StateVisualizer("Swerve", DRIVETRAIN_TRACKWIDTH_METERS * 2, this);
    }

    public Translation2d getTiltAsTranslation() {
        NavXGyroscope gyro = (NavXGyroscope) this.gyro;
        return new Translation2d(gyro.getPitch(), -gyro.getRoll());
    }

    @Override
    public void periodic() {
        super.periodic();

        ppPose.setPose(getOdometryPose());
        field.setRobotPose(getPose());
        stateVisualizer.update();

        if (CALIBRATE.get()) {
            CALIBRATE.set(false); // Instantly set back so that it doesn't calibrate more than needed
            calibrateOffsets();
        }
    }

    public static final class StateVisualizer {
        private final SwerveDrive drive;
        private final FieldObject2d fieldWheels;
        private final MechanismLigament2d[] ligaments;

        public StateVisualizer(String name, double size, DrivetrainSubsystem drive) {
            this.drive = drive;

            Mechanism2d m = new Mechanism2d(size, size);
            SmartDashboard.putData(name, m);

            SwerveModule[] modules = drive.getModules();

            ligaments = new MechanismLigament2d[modules.length];
            for (int i = 0; i < modules.length; i++) {
                SwerveModule module = modules[i];
                MechanismRoot2d root = m.getRoot(String.valueOf(i), module.position.getX() + size/2, module.position.getY() + size/2);
                ligaments[i] = new MechanismLigament2d(i + " Vector",0.2,0);
                root.append(ligaments[i]);
            }

            fieldWheels = drive.field.getObject("Swerve Modules");
        }

        public void update() {
            SwerveModule[] modules = drive.getModules();
            SwerveModuleState[] states = drive.getModuleStates();

            Pose2d robotPose = drive.getPose();

            Pose2d[] fieldPoses = new Pose2d[ligaments.length];
            for (int i = 0; i < ligaments.length; i++) {
                MechanismLigament2d ligament = ligaments[i];
                SwerveModuleState state = states[i];
                ligament.setAngle(state.angle.getDegrees());
                ligament.setLength(state.speedMetersPerSecond / 4.11 + Math.copySign(0.05, state.speedMetersPerSecond));

                Rotation2d outputAngle;
                if (state.speedMetersPerSecond == 0) {
                    outputAngle = state.angle;
                } else {
                    outputAngle = new Translation2d(state.speedMetersPerSecond, 0).rotateBy(state.angle).getAngle();
                }

                SwerveModule module = modules[i];
                Pose2d fieldPose = new Pose2d(
                        robotPose.getTranslation().plus(module.position.rotateBy(robotPose.getRotation())),
                        outputAngle.plus(robotPose.getRotation())
                );
                fieldPoses[i] = fieldPose;
            }
            System.out.println(Arrays.toString(fieldPoses));

            fieldWheels.setPoses(fieldPoses);
        }
    }
}
