package com.swrobotics.robot.subsystems.drive;

import com.swrobotics.lib.drive.swerve.StopPosition;
import com.swrobotics.lib.drive.swerve.SwerveDrive;
import com.swrobotics.lib.drive.swerve.SwerveModule;
import com.swrobotics.lib.drive.swerve.SwerveModuleAttributes;
import com.swrobotics.lib.encoder.CanCoder;
import com.swrobotics.lib.encoder.Encoder;
import com.swrobotics.lib.field.FieldInfo;
import com.swrobotics.lib.gyro.PigeonGyroscope;
import com.swrobotics.lib.motor.FeedbackMotor;
import com.swrobotics.lib.motor.ctre.TalonFXMotor;
import com.swrobotics.lib.net.NTBoolean;
import com.swrobotics.lib.net.NTPrimitive;
import com.swrobotics.robot.config.NTData;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.smartdashboard.*;
import org.littletonrobotics.junction.Logger;

import static com.swrobotics.robot.subsystems.drive.DrivetrainConstants.*;

public final class DrivetrainSubsystem extends SwerveDrive {
    public static final FieldInfo FIELD = FieldInfo.CHARGED_UP_2023;

    private static final NTPrimitive<Boolean> CALIBRATE = new NTBoolean("Swerve/Calibrate", false);

    private final PigeonGyroscope gyro;

    private final Field2d field = new Field2d();
    private final FieldObject2d ppPose = field.getObject("PathPlanner pose");
    private final StateVisualizer stateVisualizer;

    private static SwerveModule makeModule(SwerveModuleInfo info, Translation2d pos) {
        FeedbackMotor driveMotor = new TalonFXMotor(info.driveMotorID);
        FeedbackMotor turnMotor = new TalonFXMotor(info.turnMotorID);
        Encoder encoder = new CanCoder(info.encoderID).getAbsolute();

        // MK4i is inverted
        driveMotor.setInverted(true);
        turnMotor.setInverted(true);

        turnMotor.setPID(NTData.SWERVE_TURN_KP, NTData.SWERVE_TURN_KI, NTData.SWERVE_TURN_KD);

        return new SwerveModule(
                SwerveModuleAttributes.SDS_MK4I_L3, driveMotor, turnMotor, encoder, pos, info.offset);
    }

    public DrivetrainSubsystem(PigeonGyroscope gyro) {
        super(
                FIELD,
                gyro,
                makeModule(MODULES[0], FRONT_LEFT_POSITION),
                makeModule(MODULES[1], FRONT_RIGHT_POSITION),
                makeModule(MODULES[2], BACK_LEFT_POSITION),
                makeModule(MODULES[3], BACK_RIGHT_POSITION));

        this.gyro = gyro;
        stateVisualizer = new StateVisualizer("Swerve", DRIVETRAIN_TRACKWIDTH_METERS * 2, this);

        SmartDashboard.putData("Field", field);

        setStopPosition(StopPosition.COAST);
    }

    public Translation2d getTiltAsTranslation() {
        return gyro.getUpVector().xy().translation2d();
    }

    @Override
    public void periodic() {
        setBrakeMode(getStopPosition() != StopPosition.COAST);

        super.periodic();

        if (CALIBRATE.get()) {
            CALIBRATE.set(false);
            calibrateOffsets();
        }

        // Log gyro data
        Logger.getInstance().recordOutput("Gyro/RawPitch", gyro.getPitch().ccw().deg());
        Logger.getInstance().recordOutput("Gyro/Angle", gyro.getAngle().ccw().deg());
        Logger.getInstance().recordOutput("Gyro/RawRoll", gyro.getRoll().ccw().deg());
        Logger.getInstance().recordOutput("Gyro/Up Vector", gyro.getUpVector().components());
//        Logger.getInstance().recordOutput("Gyro/OffsetAmountDeg", gyroOffset.getDegrees());

        Logger.getInstance().recordOutput("SwerveStates/Setpoints", getModuleTargetStates());
        Logger.getInstance().recordOutput("SwerveStates/Measured", getModuleStates());

        // Log odometry pose
        Logger.getInstance().recordOutput("Odometry/Robot2d", getPose());

        // TODO-Mason: Does logger provide a better way of doing this?
        ppPose.setPose(getOdometryPose());
        field.setRobotPose(getPose());
        stateVisualizer.update();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // Clear setpoint logs
        Logger.getInstance().recordOutput("SwerveStates/Setpoints", new double[] {});
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
                MechanismRoot2d root =
                        m.getRoot(
                                String.valueOf(i),
                                module.position.getX() + size / 2,
                                module.position.getY() + size / 2);
                ligaments[i] = new MechanismLigament2d(i + " Vector", 0.2, 0);
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
                ligament.setLength(
                        state.speedMetersPerSecond / 4.11
                                + Math.copySign(0.05, state.speedMetersPerSecond));

                Rotation2d outputAngle;
                if (state.speedMetersPerSecond == 0) {
                    outputAngle = state.angle;
                } else {
                    outputAngle =
                            new Translation2d(state.speedMetersPerSecond, 0)
                                    .rotateBy(state.angle)
                                    .getAngle();
                }

                SwerveModule module = modules[i];
                Pose2d fieldPose =
                        new Pose2d(
                                robotPose
                                        .getTranslation()
                                        .plus(module.position.rotateBy(robotPose.getRotation())),
                                outputAngle.plus(robotPose.getRotation()));
                fieldPoses[i] = fieldPose;
            }

            fieldWheels.setPoses(fieldPoses);
        }
    }
}
