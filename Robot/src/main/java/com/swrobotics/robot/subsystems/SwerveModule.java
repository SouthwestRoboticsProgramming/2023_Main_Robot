package com.swrobotics.robot.subsystems;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.TalonFXControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import com.ctre.phoenix.motorcontrol.can.TalonFXConfiguration;
import com.ctre.phoenix.sensors.AbsoluteSensorRange;
import com.ctre.phoenix.sensors.CANCoder;
import com.ctre.phoenix.sensors.CANCoderConfiguration;
import com.ctre.phoenix.sensors.SensorInitializationStrategy;
import com.ctre.phoenix.sensors.SensorTimeBase;
import com.swrobotics.lib.net.NTDouble;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.RobotBase;

public class SwerveModule {
    private SwerveModuleState targetState = new SwerveModuleState();

    private static final int TALON_FX_ENCODER_TICKS_PER_ROTATION = 2048;
    private static final double DRIVE_MOTOR_GEAR_RATIO = 8.14;
    private static final double WHEEL_DIAMETER_METERS = Units.inchesToMeters(4.0);
    private static final double TURN_MOTOR_GEAR_RATIO = 12.8;
    private static final double MAX_VELOCITY = 4.0;

    private final TalonFX turn;
    private final TalonFX drive;
    private final CANCoder encoder;

    private final NTDouble offset;
    public final Translation2d position;

    // Conversion helpers
    private final double turnEncoderToAngle;
    private final double driveEncoderVelocityToMPS;

    public SwerveModule(SwerveModuleInfo moduleInfo, Translation2d position) {
        this.position = position;

        // Offset is stored in NTDouble to save it for the next match
        offset = new NTDouble("Swerve/Modules/" + moduleInfo.name + "/Offset Degrees", moduleInfo.offset);

        TalonFXConfiguration turnConfig = new TalonFXConfiguration();
        turnConfig.slot0.kP = 0.2;
        turnConfig.slot0.kI = 0;
        turnConfig.slot0.kD = 0.1;
        turnConfig.primaryPID.selectedFeedbackSensor = FeedbackDevice.IntegratedSensor;

        TalonFXConfiguration driveConfig = new TalonFXConfiguration();
        driveConfig.slot0.kP = 0;
        driveConfig.slot0.kI = 0;
        driveConfig.slot0.kD = 0;
        driveConfig.primaryPID.selectedFeedbackSensor = FeedbackDevice.IntegratedSensor;
        driveConfig.peakOutputForward = 0.1;
        driveConfig.peakOutputReverse = -0.1;

        this.turn = new TalonFX(moduleInfo.turnMotorID);
        this.turn.configAllSettings(turnConfig);

        this.drive = new TalonFX(moduleInfo.driveMotorID);
        this.drive.configAllSettings(driveConfig);

        this.encoder = new CANCoder(moduleInfo.encoderID);
        this.encoder.configFactoryDefault();

        CANCoderConfiguration config = new CANCoderConfiguration();
        config.initializationStrategy = SensorInitializationStrategy.BootToAbsolutePosition;
        config.absoluteSensorRange = AbsoluteSensorRange.Unsigned_0_to_360;
        config.sensorTimeBase = SensorTimeBase.PerSecond;

        this.encoder.configAllSettings(config);

        setState(new SwerveModuleState());

        turnEncoderToAngle = TALON_FX_ENCODER_TICKS_PER_ROTATION * TURN_MOTOR_GEAR_RATIO / (Math.PI * 2);
        // driveEncoderVelocityToMPS = ((600.0 / 2048.0) / DRIVE_MOTOR_GEAR_RATIO * WHEEL_DIAMETER_METERS * Math.PI) / 60; // Copied from Citrus Circuits
        driveEncoderVelocityToMPS = 1 / ((2048 * 10) * Math.PI * WHEEL_DIAMETER_METERS);
        calibrateWithAbsoluteEncoder();
    }

    public void setState(SwerveModuleState state) {
        // Optimize direction
        // SwerveModuleState outputState = optimize(state);
        // SwerveModuleState outputState = SwerveModuleState.optimize(state, getAngle());
        // SwerveModuleState outputState = state;
        SwerveModuleState outputState = optimize(state.speedMetersPerSecond, state.angle.getRadians());
        targetState = outputState;

        double turnUnits = toNativeTurnUnits(outputState.angle);

        turn.set(TalonFXControlMode.Position, turnUnits);

        double driveOutput = outputState.speedMetersPerSecond / MAX_VELOCITY;
        drive.set(TalonFXControlMode.PercentOutput, driveOutput);

    }

    public SwerveModuleState getState() {
        return new SwerveModuleState(getDriveVelocity(), getAngle());
    }

    public Rotation2d getAngle() {
        if (RobotBase.isSimulation()) {
            return targetState.angle;
        }

        return fromNativeTurnUnits(turn.getSelectedSensorPosition());
    }

    public Rotation2d getAbsoluteAngle() {
        return Rotation2d.fromDegrees(encoder.getAbsolutePosition() - offset.get());
    }

    public double getCalibrationAngle() {
        return encoder.getAbsolutePosition(); // No offset applied
    }

    public void calibrate() {
        offset.set(getCalibrationAngle());
        calibrateWithAbsoluteEncoder();
    }

    public double getDriveVelocity() {
        if (RobotBase.isSimulation()) {
            return targetState.speedMetersPerSecond;
        }

        return fromNativeDriveUnits(drive.getSelectedSensorVelocity());
    }

    private void calibrateWithAbsoluteEncoder() {
        turn.setSelectedSensorPosition(toNativeTurnUnits(getAbsoluteAngle()));
    }

    private SwerveModuleState optimize(double velocity, double angleRad) {
        Rotation2d current = getAngle(); // Difference in unpacking SwerveModuleState

        Rotation2d targetAngle = new Rotation2d(angleRad);
        Rotation2d invAngle = targetAngle.plus(Rotation2d.fromDegrees(180));
        // Possibly the trouble lines

        double absDiff = absDiffRad(targetAngle, current).getRadians();
        double invAbsDiff = absDiffRad(invAngle, current).getRadians();
        
        Rotation2d target;
        if (invAbsDiff < absDiff) {
            target = invAngle;
            velocity = -velocity;
        } else {
            target = targetAngle;
        }


        double currentAngleRadiansMod = current.getRadians() % (2.0 * Math.PI);
        if (currentAngleRadiansMod < 0.0) {
            currentAngleRadiansMod += 2.0 * Math.PI;
        }

        // The reference angle has the range [0, 2pi) but the Falcon's encoder can go above that
        double adjustedReferenceAngleRadians = target.getRadians() + current.getRadians() - currentAngleRadiansMod;
        if (target.getRadians() - currentAngleRadiansMod > Math.PI) {
            adjustedReferenceAngleRadians -= 2.0 * Math.PI;
        } else if (target.getRadians() - currentAngleRadiansMod < -Math.PI) {
            adjustedReferenceAngleRadians += 2.0 * Math.PI;
        }

        Rotation2d finalAngle = new Rotation2d(adjustedReferenceAngleRadians);

        double targetTurnEncoderPosition = toNativeTurnUnits(finalAngle);

        return new SwerveModuleState(velocity, finalAngle);
    }

    private Rotation2d absDiffRad(Rotation2d angle1, Rotation2d angle2) {
        double normSelf = wrap(angle1.getRadians(), 0,  2.0 * Math.PI);
        double normOther = wrap(angle2.getRadians(), 0,  2.0 * Math.PI);

        double diffRad = normOther - normSelf;
        double direct = Math.abs(diffRad);
        double wrapped = (2.0 * Math.PI) - direct;

        return new Rotation2d(Math.min(direct, wrapped));
    }

    private double wrap(double a, double min, double max) {
        return floorMod(a - min, max - min) + min;
    }

    private double floorMod(double x, double y) {
        return x - Math.floor(x / y) * y;
    }

    private double toNativeTurnUnits(Rotation2d angle) {
        return angle.getRadians() * turnEncoderToAngle;
    }

    private Rotation2d fromNativeTurnUnits(double units) {
        return new Rotation2d(units / turnEncoderToAngle);
    }

    private double toNativeDriveUnits(double velocityMPS) {
        return velocityMPS / driveEncoderVelocityToMPS;
    }

    private double fromNativeDriveUnits(double units) {
        return units / ((2048 / 10) * DRIVE_MOTOR_GEAR_RATIO / (Math.PI * WHEEL_DIAMETER_METERS));
    }

}
