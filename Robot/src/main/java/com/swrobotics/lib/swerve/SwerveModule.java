package com.swrobotics.lib.swerve;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.TalonFXControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import com.ctre.phoenix.motorcontrol.can.TalonFXConfiguration;
import com.ctre.phoenix.sensors.AbsoluteSensorRange;
import com.ctre.phoenix.sensors.CANCoder;
import com.swrobotics.mathlib.AbsoluteAngle;
import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.CCWAngle;

import edu.wpi.first.math.geometry.Translation2d;

public final class SwerveModule {
    private static final int TALON_FX_ENCODER_TICKS_PER_ROTATION = 2048;
    private static final double DRIVE_MOTOR_GEAR_RATIO = 8.14;
    private static final double TURN_MOTOR_GEAR_RATIO = 12.8;

    public final Translation2d position;

    private final TalonFX turn;
    private final TalonFX drive;
    private final CANCoder encoder;

    private final double driveEncoderToAngle;

    public SwerveModule(int turn, int drive, int encoder, double offset, Translation2d position) {
        this.position = position;

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

        this.turn = new TalonFX(turn);
        this.turn.configAllSettings(turnConfig);
        this.drive = new TalonFX(drive);
        this.drive.configAllSettings(driveConfig);
        this.encoder = new CANCoder(encoder);
        this.encoder.configMagnetOffset(offset);
        this.encoder.configAbsoluteSensorRange(AbsoluteSensorRange.Unsigned_0_to_360);

        driveEncoderToAngle = TALON_FX_ENCODER_TICKS_PER_ROTATION * TURN_MOTOR_GEAR_RATIO / (Math.PI * 2);

        calibrateTurn();
    }

    private double turnEncoderPositionToAngle(double pos) {
        return pos / driveEncoderToAngle;
    }

    private double angleToTurnEncoderPosition(double angle) {
        return angle * driveEncoderToAngle;
    }

    public double getCurrentEncoderAngle() {
        return encoder.getAbsolutePosition();
    }

    private void calibrateTurn() {
        double encoderAngle = Math.toRadians(this.encoder.getAbsolutePosition());
        this.turn.setSelectedSensorPosition(angleToTurnEncoderPosition(encoderAngle));
    }

    long lastCalibTime = -1;

    // Angle in ccw rad, 0 forward
    public void set(double velocity, double angleRad) {
        Angle current = CCWAngle.rad(turnEncoderPositionToAngle(turn.getSelectedSensorPosition()));

        Angle angle = CCWAngle.rad(angleRad);
        Angle invAngle = angle.ccw().add(CCWAngle.deg(180));
        double absDiff = angle.ccw().getAbsDiff(current.ccw()).ccw().rad();
        double invAbsDiff = invAngle.ccw().getAbsDiff(current.ccw()).ccw().rad();
        
        Angle target;
        if (invAbsDiff < absDiff) {
            target = invAngle;
            velocity = -velocity;
        } else {
            target = angle;
        }


        double currentAngleRadiansMod = current.ccw().rad() % (2.0 * Math.PI);
        if (currentAngleRadiansMod < 0.0) {
            currentAngleRadiansMod += 2.0 * Math.PI;
        }

        // The reference angle has the range [0, 2pi) but the Falcon's encoder can go above that
        double adjustedReferenceAngleRadians = target.ccw().rad() + current.ccw().rad() - currentAngleRadiansMod;
        if (target.ccw().rad() - currentAngleRadiansMod > Math.PI) {
            adjustedReferenceAngleRadians -= 2.0 * Math.PI;
        } else if (target.ccw().rad() - currentAngleRadiansMod < -Math.PI) {
            adjustedReferenceAngleRadians += 2.0 * Math.PI;
        }


        double targetTurnEncoderPosition = angleToTurnEncoderPosition(adjustedReferenceAngleRadians);
        turn.set(TalonFXControlMode.Position, targetTurnEncoderPosition);
        drive.set(TalonFXControlMode.PercentOutput, velocity);

        // System.out.printf("TA %.3f CA %.3f TP %.3f CP %.3f%n", Math.toDegrees(angle), getCurrentEncoderAngle(), targetTurnEncoderPosition, turn.getSelectedSensorPosition());
    }
}
