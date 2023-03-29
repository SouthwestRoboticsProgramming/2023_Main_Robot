package com.swrobotics.robot.io;

import com.swrobotics.lib.encoder.CanCoder;
import com.swrobotics.lib.encoder.Encoder;
import com.swrobotics.lib.gyro.Gyroscope;
import com.swrobotics.lib.gyro.NavXGyroscope;
import com.swrobotics.lib.motor.FeedbackMotor;
import com.swrobotics.lib.motor.Motor;
import com.swrobotics.lib.motor.ctre.TalonFXMotor;
import com.swrobotics.lib.motor.rev.NEOMotor;
import com.swrobotics.lib.motor.rev.PWMSparkMaxMotor;
import edu.wpi.first.wpilibj.SPI;

public final class PhysicalRobotIO implements RobotIO {
    // ---- CAN IDs ----
    private static final SwerveModuleInfo[] MODULES = {
            new SwerveModuleInfo(9, 5, 1),
            new SwerveModuleInfo(10, 6, 2),
            new SwerveModuleInfo(11, 7, 3),
            new SwerveModuleInfo(12, 8, 4)
    };

    private static final int ARM_BOTTOM_CANCODER_ID = 13;
    private static final int ARM_TOP_CANCODER_ID = 14;
    private static final int ARM_BOTTOM_MOTOR_ID = 23;
    private static final int ARM_TOP_MOTOR_ID = 24;

    @Override
    public Gyroscope getGyroscope() {
        return new NavXGyroscope(SPI.Port.kMXP);
    }

    // ---- Swerve ----
    private static final class SwerveModuleInfo {
        final int driveId;
        final int turnId;
        final int encoderId;

        public SwerveModuleInfo(int driveId, int turnId, int encoderId) {
            this.driveId = driveId;
            this.turnId = turnId;
            this.encoderId = encoderId;
        }
    }

    @Override
    public FeedbackMotor getSwerveDriveMotor(int module) {
        return new TalonFXMotor(MODULES[module].driveId);
    }

    @Override
    public FeedbackMotor getSwerveTurnMotor(int module) {
        return new TalonFXMotor(MODULES[module].turnId);
    }

    @Override
    public Encoder getSwerveEncoder(int module) {
        return new CanCoder(MODULES[module].encoderId).getAbsolute();
    }

    // ---- Arm ----
    @Override
    public FeedbackMotor getArmBottomMotor() {
        return new NEOMotor(ARM_BOTTOM_MOTOR_ID);
    }

    @Override
    public FeedbackMotor getArmTopMotor() {
        return new NEOMotor(ARM_TOP_MOTOR_ID);
    }

    @Override
    public Encoder getArmBottomEncoder() {
        return new CanCoder(ARM_BOTTOM_CANCODER_ID).getAbsolute();
    }

    @Override
    public Encoder getArmTopEncoder() {
        return new CanCoder(ARM_TOP_CANCODER_ID).getAbsolute();
    }

    @Override
    public Motor getIntakeMotor() {
        return new PWMSparkMaxMotor(RIOPorts.INTAKE_PWM);
    }
}
