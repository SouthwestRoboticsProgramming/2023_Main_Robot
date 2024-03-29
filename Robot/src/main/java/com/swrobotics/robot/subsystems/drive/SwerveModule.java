package com.swrobotics.robot.subsystems.drive;

import com.ctre.phoenix.sensors.AbsoluteSensorRange;
import com.ctre.phoenix.sensors.CANCoder;
import com.ctre.phoenix.sensors.CANCoderConfiguration;
import com.ctre.phoenix.sensors.SensorInitializationStrategy;
import com.ctre.phoenix.sensors.SensorTimeBase;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.SparkMaxPIDController;
import com.swrobotics.lib.net.NTDouble;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.RobotBase;

public class SwerveModule {
    private static final String NT_PATH =
            "Swerve/Modules/"; // All module constants will be in the Swerve/Module folder

    private static final int ENCODER_TICKS_PER_ROTATION = 1;

    // Configured for an SDS MK4 L1 module
    private static final double DRIVE_MOTOR_GEAR_RATIO = 8.14; // 8.14 : 1
    private static final double TURN_MOTOR_GEAR_RATIO = 12.8; // 12.8 : 1
    private static final double WHEEL_DIAMETER_METERS =
            Units.inchesToMeters(3.85);
    private static final double MAX_ACHIEVABLE_VELOCITY =
            4.11; // Meters / Second, Listed on SDS website

    /* Tunable Constants */
    private static final NTDouble TURN_KP = new NTDouble(NT_PATH + "Turn kP", 0.2);
    private static final NTDouble TURN_KI = new NTDouble(NT_PATH + "Turn kI", 0.0);
    private static final NTDouble TURN_KD = new NTDouble(NT_PATH + "Turn kD", 0.1);

    // Currently, drive is open-loop so no constants are required

    private final CANSparkMax turn;
    private final SparkMaxPIDController turnPID;
    private final RelativeEncoder turnEncoder;
    private final CANSparkMax drive;
    private final RelativeEncoder driveEncoder;
    private final CANCoder encoder;

    private final NTDouble offset;
    private final double positionalOffset;

    /** The state the module is currently set to constantly try to reach */
    private SwerveModuleState targetState = new SwerveModuleState();

    public final Translation2d position;

    // Conversion helpers
    private final double turnEncoderToAngle;
    private final double driveEncoderVelocityToMPS;

    // Simulate drive encoder distance
    private double simulatedDistance = 0.0;

    public SwerveModule(
            SwerveModuleInfo moduleInfo, Translation2d position, double positionalOffset) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.position = position;
        this.positionalOffset = positionalOffset;

        // Offset is stored in NTDouble to save it for the next match
        offset =
                new NTDouble(
                        "Swerve/Modules/" + moduleInfo.name + "/Offset Degrees", moduleInfo.offset);

//        TalonFXConfiguration turnConfig = new TalonFXConfiguration();
//        turnConfig.slot0.kP = TURN_KP.get();
//        turnConfig.slot0.kI = TURN_KI.get();
//        turnConfig.slot0.kD = TURN_KD.get();
//        turnConfig.primaryPID.selectedFeedbackSensor = FeedbackDevice.IntegratedSensor;
//
//        TalonFXConfiguration driveConfig = new TalonFXConfiguration();
//        driveConfig.slot0.kP = 0;
//        driveConfig.slot0.kI = 0;
//        driveConfig.slot0.kD = 0;
//        driveConfig.primaryPID.selectedFeedbackSensor = FeedbackDevice.IntegratedSensor;
//        // FIXME: Remove limits imposed to keep robot from breaking Mason's house FIXME: Break the
        // house
        // driveConfig.peakOutputForward = 0.1;
        // driveConfig.peakOutputReverse = -0.1;

//        this.turn = new TalonFX(moduleInfo.turnMotorID);
//        this.turn.configAllSettings(turnConfig);

//        this.drive = new TalonFX(moduleInfo.driveMotorID);
//        this.drive.configAllSettings(driveConfig);

        this.turn = new CANSparkMax(moduleInfo.turnMotorID, CANSparkMaxLowLevel.MotorType.kBrushless);
        this.drive = new CANSparkMax(moduleInfo.driveMotorID, CANSparkMaxLowLevel.MotorType.kBrushless);
        this.turnPID = turn.getPIDController();
        this.turnEncoder = turn.getEncoder();
        this.driveEncoder = drive.getEncoder();

        this.encoder = new CANCoder(moduleInfo.encoderID);
        this.encoder.configFactoryDefault();

        CANCoderConfiguration config = new CANCoderConfiguration();
        config.initializationStrategy = SensorInitializationStrategy.BootToAbsolutePosition;
        config.absoluteSensorRange = AbsoluteSensorRange.Unsigned_0_to_360;
        config.sensorTimeBase = SensorTimeBase.PerSecond;

        this.encoder.configAllSettings(config);

        setState(new SwerveModuleState());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        turnEncoderToAngle =
                ENCODER_TICKS_PER_ROTATION * TURN_MOTOR_GEAR_RATIO / (Math.PI * 2);
        // driveEncoderVelocityToMPS = ((600.0 / 2048.0) / DRIVE_MOTOR_GEAR_RATIO *
        // WHEEL_DIAMETER_METERS * Math.PI) / 60; // Copied from Citrus Circuits
//        driveEncoderVelocityToMPS = 1 / ((2048 * 10) * Math.PI * WHEEL_DIAMETER_METERS);
        driveEncoderVelocityToMPS = (2 * Math.PI / 60.0) * WHEEL_DIAMETER_METERS/2.0; // RPM to Rad/s, Rad/s to meters
        calibrateWithAbsoluteEncoder();

        // Update PID constants if they are changed
        updateTurnPID();
        TURN_KP.onChange(this::updateTurnPID);
        TURN_KI.onChange(this::updateTurnPID);
        TURN_KD.onChange(this::updateTurnPID);
    }

    public void setState(SwerveModuleState state) {
        // Optimize direction to be as close to current as possible
        SwerveModuleState outputState =
                optimize(state.speedMetersPerSecond, state.angle.getRadians());
        targetState = outputState;

        // Simulate encoder distance for odometry
        simulatedDistance += outputState.speedMetersPerSecond * 0.02;

        double turnUnits = toNativeTurnUnits(outputState.angle);

        turnPID.setReference(turnUnits, CANSparkMax.ControlType.kPosition);
//        turn.set(TalonFXControlMode.Position, turnUnits);

        double driveOutput = outputState.speedMetersPerSecond / MAX_ACHIEVABLE_VELOCITY;
//        drive.set(TalonFXControlMode.PercentOutput, driveOutput);
        drive.set(driveOutput);
    }

    public void stop() {
        turn.set(0);
        drive.set(0);
//        turn.set(TalonFXControlMode.PercentOutput, 0);
//        drive.set(TalonFXControlMode.PercentOutput, 0);
    }

    /**
     * Get the current velocity and rotation of the module as read by the encoders
     *
     * @return State measured by encoders
     */
    public SwerveModuleState getState() {
        return new SwerveModuleState(getDriveVelocity(), getAngle());
    }

    public SwerveModulePosition getPosition() {
        return new SwerveModulePosition(getDistance(), getAngle());
    }

    public Rotation2d getAngle() {
        if (RobotBase.isSimulation()) {
            return targetState.angle;
        }

        return fromNativeTurnUnits(turnEncoder.getPosition());
    }

    public double getDistance() {
        if (RobotBase.isSimulation()) {
            return simulatedDistance;
        }
        return fromNativeDrivePositionUnits(driveEncoder.getPosition());
    }

    public Rotation2d getAbsoluteAngle() {
        return Rotation2d.fromDegrees(
                encoder.getAbsolutePosition() - offset.get() - positionalOffset);
    }

    public double getCalibrationAngle() {
        return encoder.getAbsolutePosition() - positionalOffset; // No offset applied
    }

    public void calibrate() {
        offset.set(getCalibrationAngle());
        calibrateWithAbsoluteEncoder();
    }

    public void setBrakeMode(boolean brake) {
        CANSparkMax.IdleMode mode = CANSparkMax.IdleMode.kCoast;
        if (brake) {
            mode = CANSparkMax.IdleMode.kBrake;
        }
        drive.setIdleMode(mode);
    }

    public double getDriveVelocity() {
        if (RobotBase.isSimulation()) {
            return targetState.speedMetersPerSecond;
        }

        return fromNativeDriveVelocityUnits(driveEncoder.getVelocity());
    }

    public void calibrateWithAbsoluteEncoder() {
        turnEncoder.setPosition(toNativeTurnUnits(getAbsoluteAngle()));
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
        double adjustedReferenceAngleRadians =
                target.getRadians() + current.getRadians() - currentAngleRadiansMod;
        if (target.getRadians() - currentAngleRadiansMod > Math.PI) {
            adjustedReferenceAngleRadians -= 2.0 * Math.PI;
        } else if (target.getRadians() - currentAngleRadiansMod < -Math.PI) {
            adjustedReferenceAngleRadians += 2.0 * Math.PI;
        }

        Rotation2d finalAngle = new Rotation2d(adjustedReferenceAngleRadians);

        return new SwerveModuleState(velocity, finalAngle);
    }

    private void updateTurnPID() {
        turnPID.setP(TURN_KP.get());
        turnPID.setI(TURN_KI.get());
        turnPID.setD(TURN_KD.get());
    }

    private Rotation2d absDiffRad(Rotation2d angle1, Rotation2d angle2) {
        double normSelf = wrap(angle1.getRadians(), 0, 2.0 * Math.PI);
        double normOther = wrap(angle2.getRadians(), 0, 2.0 * Math.PI);

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

    private double fromNativeDriveVelocityUnits(double units) {
        return units * driveEncoderVelocityToMPS;
    }

    private double fromNativeDrivePositionUnits(double units) {
        // Rotations -> Radians -> distance
        return units * (2 * Math.PI) * (WHEEL_DIAMETER_METERS / 2.0);
    }
}
