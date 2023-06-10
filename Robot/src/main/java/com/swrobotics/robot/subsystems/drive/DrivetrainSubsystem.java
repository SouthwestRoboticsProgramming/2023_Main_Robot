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
import edu.wpi.first.math.geometry.Translation2d;
import org.littletonrobotics.junction.Logger;

import static com.swrobotics.robot.subsystems.drive.DrivetrainConstants.*;

public final class DrivetrainSubsystem extends SwerveDrive {
    public static final FieldInfo FIELD = FieldInfo.CHARGED_UP_2023;

    private static final NTBoolean CALIBRATE = new NTBoolean("Swerve/Calibrate", false);

    private static final NTDouble TURN_KP = new NTDouble("Swerve/Modules/Turn kP", 0.2);
    private static final NTDouble TURN_KI = new NTDouble("Swerve/Modules/Turn kI", 0.0);
    private static final NTDouble TURN_KD = new NTDouble("Swerve/Modules/Turn kD", 0.1);

    private final NavXGyroscope gyro;

    // FIXME: Update for MK4i
    private static SwerveModule makeModule(SwerveModuleInfo info, Translation2d pos) {
        FeedbackMotor driveMotor = new TalonFXMotor(info.driveMotorID);
        FeedbackMotor turnMotor = new TalonFXMotor(info.turnMotorID);
        Encoder encoder = new CanCoder(info.encoderID).getAbsolute();

        turnMotor.setPID(TURN_KP, TURN_KI, TURN_KD);

        return new SwerveModule(
                SwerveModuleAttributes.SDS_MK4_L4, driveMotor, turnMotor, encoder, pos, info.offset);
    }

    public DrivetrainSubsystem(NavXGyroscope gyro) {
        super(
                FIELD,
                gyro,
                makeModule(MODULES[0], FRONT_LEFT_POSITION),
                makeModule(MODULES[1], FRONT_RIGHT_POSITION),
                makeModule(MODULES[2], BACK_LEFT_POSITION),
                makeModule(MODULES[3], BACK_RIGHT_POSITION));

        this.gyro = gyro;
    }

    // FIXME: Correct for weird gyro mounting
    // FIXME: This should probably be based on the up vector rather than raw angles
    public Translation2d getTiltAsTranslation() {
        return new Translation2d(gyro.getPitch(), -gyro.getRoll());
    }

    @Override
    public void periodic() {
        // FIXME: Bring back coast mode (this should be in lib)
        super.periodic();

        if (CALIBRATE.get()) {
            CALIBRATE.set(false);
            calibrateOffsets();
        }

        // Log gyro data
        Logger.getInstance().recordOutput("Gyro/RawPitch", gyro.getPitch());
        Logger.getInstance().recordOutput("Gyro/Angle", gyro.getAngle().ccw().deg());
        Logger.getInstance().recordOutput("Gyro/RawRoll", gyro.getRoll());
//        Logger.getInstance().recordOutput("Gyro/OffsetAmountDeg", gyroOffset.getDegrees());

        Logger.getInstance().recordOutput("SwerveStates/Setpoints", getModuleTargetStates());
        Logger.getInstance().recordOutput("SwerveStates/Measured", getModuleStates());

        // Log odometry pose
        Logger.getInstance().recordOutput("Odometry/Robot2d", getPose());
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // Clear setpoint logs
        Logger.getInstance().recordOutput("SwerveStates/Setpoints", new double[] {});
    }
}
