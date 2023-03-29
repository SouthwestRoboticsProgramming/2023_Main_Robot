package com.swrobotics.robot.subsystems.drive;

import com.swrobotics.lib.drive.swerve.SwerveDrive;
import com.swrobotics.lib.drive.swerve.SwerveModule;
import com.swrobotics.lib.drive.swerve.SwerveModuleInfo;
import com.swrobotics.lib.field.FieldInfo;
import com.swrobotics.lib.gyro.NavXGyroscope;
import com.swrobotics.lib.net.NTBoolean;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.smartdashboard.*;

public final class DrivetrainSubsystem extends SwerveDrive {
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

    public static final double DRIVETRAIN_TRACKWIDTH_METERS = Units.inchesToMeters(18.5);
    public static final double DRIVETRAIN_WHEELBASE_METERS = DRIVETRAIN_TRACKWIDTH_METERS;
    private static final Translation2d[] MODULE_POSITIONS = {
            // Front left
            new Translation2d(DRIVETRAIN_TRACKWIDTH_METERS / 2.0, DRIVETRAIN_WHEELBASE_METERS / 2.0),
            // Front right
            new Translation2d(DRIVETRAIN_TRACKWIDTH_METERS / 2.0, -DRIVETRAIN_WHEELBASE_METERS / 2.0),
            // Back left
            new Translation2d(-DRIVETRAIN_TRACKWIDTH_METERS / 2.0, DRIVETRAIN_WHEELBASE_METERS / 2.0),
            // Back right
            new Translation2d(-DRIVETRAIN_TRACKWIDTH_METERS / 2.0, -DRIVETRAIN_WHEELBASE_METERS / 2.0)
    };

    private static final NTBoolean CALIBRATE = new NTBoolean("Swerve/Calibrate", false);

    public static final FieldInfo FIELD = FieldInfo.CHARGED_UP_2023;

    public final Field2d field = new Field2d();
    private final FieldObject2d ppPose = field.getObject("PathPlanner pose");

    private final StateVisualizer stateVisualizer;

    public DrivetrainSubsystem() {
        super(FIELD, new NavXGyroscope(SPI.Port.kMXP), new SwerveModuleInfo[] {
                FRONT_LEFT_SELECT.getSelected(),
                FRONT_RIGHT_SELECT.getSelected(),
                BACK_LEFT_SELECT.getSelected(),
                BACK_RIGHT_SELECT.getSelected()
        }, MODULE_POSITIONS);

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
        private final MechanismLigament2d[] ligaments;

        public StateVisualizer(String name, double size, SwerveDrive drive) {
            this.drive = drive;

            Mechanism2d m = new Mechanism2d(size, size);
            SmartDashboard.putData(name, m);

            SwerveModule[] modules = drive.getModules();

            ligaments = new MechanismLigament2d[modules.length];
            for (int i = 0; i < modules.length; i++) {
                SwerveModule module = modules[i];
                MechanismRoot2d root = m.getRoot(module.getName(), module.position.getX() + size/2, module.position.getY() + size/2);
                ligaments[i] = new MechanismLigament2d(module.getName() + " Vector",0.2,0);
                root.append(ligaments[i]);
            }
        }

        public void update() {
            SwerveModuleState[] states = drive.getModuleStates();
            for (int i = 0; i < ligaments.length; i++) {
                MechanismLigament2d ligament = ligaments[i];
                SwerveModuleState state = states[i];
                ligament.setAngle(state.angle.getDegrees());
                ligament.setLength(state.speedMetersPerSecond / 4.11 + Math.copySign(0.05, state.speedMetersPerSecond));
            }
        }
    }
}
