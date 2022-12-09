package com.swrobotics.lib.swerve;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.TalonFXControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import com.ctre.phoenix.motorcontrol.can.TalonFXConfiguration;
import com.ctre.phoenix.sensors.AbsoluteSensorRange;
import com.ctre.phoenix.sensors.CANCoder;

import edu.wpi.first.math.geometry.Translation2d;

public final class SwerveModule {
    private static final int TALON_FX_ENCODER_TICKS_PER_ROTATION = 2048;
    private static final double DRIVE_MOTOR_GEAR_RATIO = 8.14;
    private static final double TURN_MOTOR_GEAR_RATIO = 12.8;

    public final Translation2d position;

    private final TalonFX turn;
    private final TalonFX drive;
    private final CANCoder encoder;

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

        calibrateTurn();
    }

    private double angleToDriveEncoderPosition(double angle) {
        return angle / 360.0 * TALON_FX_ENCODER_TICKS_PER_ROTATION * TURN_MOTOR_GEAR_RATIO;
    }

    public double getCurrentEncoderAngle() {
        return encoder.getAbsolutePosition();
    }

    private void calibrateTurn() {
        double encoderAngle = this.encoder.getAbsolutePosition();
        this.turn.setSelectedSensorPosition(angleToDriveEncoderPosition(encoderAngle));
    }

    long lastCalibTime = -1;

    // Angle in ccw rad, 0 forward
    public void set(double velocity, double angle) {
        if (angle > Math.PI * 2)
            angle -= Math.PI * 2;
        if (angle < 0)
            angle += Math.PI * 2;

        // if (lastCalibTime == -1 ||System.currentTimeMillis() - lastCalibTime > 1000) {
        //     lastCalibTime = System.currentTimeMillis();
        //     calibrateTurn();
        // }

        double targetTurnEncoderPosition = angleToDriveEncoderPosition(Math.toDegrees(angle));
        turn.set(TalonFXControlMode.Position, targetTurnEncoderPosition);
        drive.set(TalonFXControlMode.PercentOutput, velocity);

        // System.out.printf("TA %.3f CA %.3f TP %.3f CP %.3f%n", Math.toDegrees(angle), getCurrentEncoderAngle(), targetTurnEncoderPosition, turn.getSelectedSensorPosition());
    }
}
