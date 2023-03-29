package com.swrobotics.lib.drive.swerve;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.TalonFXControlMode;
import com.swrobotics.lib.encoder.Encoder;
import com.swrobotics.lib.motor.FeedbackMotor;
import com.swrobotics.lib.net.NTDouble;

import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.CCWAngle;
import com.swrobotics.mathlib.MathUtil;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.wpilibj.RobotBase;

public class SwerveModule {
    private final SwerveModuleAttributes attribs;

    private final FeedbackMotor turn;
    private final FeedbackMotor drive;

    private final Encoder encoder;
    private final Encoder turnEncoder;
    private final Encoder driveEncoder;

    private final NTDouble offset;
    public final Translation2d position;
    private final double positionalOffset;

    /** The state the module is currently set to constantly try to reach */
    private SwerveModuleState targetState = new SwerveModuleState();

    // Simulate drive encoder distance
    private double simulatedDistance = 0.0;

    /**
     * Creates a new swerve module with the given parameters. The motors
     * provided should be configured such that:
     *   - Turn motor PID is tuned for turning
     *   - Counterclockwise turn motor corresponds to counterclockwise wheel movement
     *   - Drive motor positive percent corresponds to forward
     *
     * @param attribs physical attributes of the module
     * @param driveMotor motor for driving the wheel
     * @param turnMotor motor for turning the wheel
     * @param encoder absolute encoder for calibration
     * @param position position relative to robot center
     * @param offset NetworkTables entry to store encoder offset
     */
    public SwerveModule(
            SwerveModuleAttributes attribs,
            FeedbackMotor driveMotor,
            FeedbackMotor turnMotor,
            Encoder encoder,
            Translation2d position,
            NTDouble offset
    ) {
        this.attribs = attribs;
        this.drive = driveMotor;
        this.turn = turnMotor;
        this.encoder = encoder;
        this.position = position;
        this.offset = offset;

        turnEncoder = turn.getIntegratedEncoder();
        driveEncoder = drive.getIntegratedEncoder();

        positionalOffset = MathUtil.wrap(position.getAngle().getDegrees(), -180, 180);

        setState(new SwerveModuleState());
        calibrateWithAbsoluteEncoder();
    }






    public void setState(SwerveModuleState state) {
        // Optimize direction to be as close to current as possible
        SwerveModuleState outputState = optimize(state.speedMetersPerSecond, state.angle.getRadians());
        targetState = outputState;

        // Simulate encoder distance for odometry
        simulatedDistance += outputState.speedMetersPerSecond * 0.02;

        Angle turnUnits = toNativeTurnUnits(outputState.angle);
        turn.setPosition(turnUnits);

        double driveOutput = outputState.speedMetersPerSecond / attribs.getMaxVelocity();
        drive.setPercentOut(driveOutput);
    }

    public void stop() {
        turn.setPercentOut(0);
        drive.setPercentOut(0);
    }

    /**
     * Get the current velocity and rotation of the module as read by the encoders
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

        return fromNativeTurnUnits(turnEncoder.getAngle());
    }

    public double getDistance() {
        if (RobotBase.isSimulation()) {
            return simulatedDistance;
        }
        return fromNativeDriveUnits(driveEncoder.getAngle());
    }

    public Rotation2d getAbsoluteAngle() {
        return Rotation2d.fromDegrees(encoder.getAngle().ccw().deg() - offset.get() - positionalOffset);
    }

    public double getCalibrationAngle() {
        return encoder.getAngle().ccw().deg() - positionalOffset; // No offset applied
    }

    public void calibrate() {
        offset.set(getCalibrationAngle());
        calibrateWithAbsoluteEncoder();
    }

    public void setBrakeMode(boolean brake) {
        drive.setBrakeMode(brake);
    }

    public double getDriveVelocity() {
        if (RobotBase.isSimulation()) {
            return targetState.speedMetersPerSecond;
        }

        return fromNativeDriveUnits(driveEncoder.getVelocity());
    }

    private void calibrateWithAbsoluteEncoder() {
        turnEncoder.setAngle(toNativeTurnUnits(getAbsoluteAngle()));
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

        return new SwerveModuleState(velocity, finalAngle);
    }

    private Rotation2d absDiffRad(Rotation2d angle1, Rotation2d angle2) {
        double normSelf = MathUtil.wrap(angle1.getRadians(), 0,  2.0 * Math.PI);
        double normOther = MathUtil.wrap(angle2.getRadians(), 0,  2.0 * Math.PI);

        double diffRad = normOther - normSelf;
        double direct = Math.abs(diffRad);
        double wrapped = (2.0 * Math.PI) - direct;

        return new Rotation2d(Math.min(direct, wrapped));
    }

    private Angle toNativeTurnUnits(Rotation2d angle) {
        return CCWAngle.rad(angle.getRadians() * attribs.getTurnGearRatio());
    }

    private Rotation2d fromNativeTurnUnits(Angle units) {
        return new Rotation2d(units.ccw().rad() / attribs.getTurnGearRatio());
    }

    private double fromNativeDriveUnits(Angle units) {
        return units.cw().rad() / attribs.getDriveGearRatio() * (attribs.getWheelDiameter() / 2);
    }
}
