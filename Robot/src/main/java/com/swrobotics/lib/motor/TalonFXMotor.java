package com.swrobotics.lib.motor;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.InvertType;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import com.ctre.phoenix.motorcontrol.can.TalonFXConfiguration;
import com.swrobotics.lib.encoder.Encoder;
import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.CWAngle;

public final class TalonFXMotor implements FeedbackMotor {
    private static final int ENCODER_TICKS_PER_ROTATION = 2048;

    private final TalonFX fx;
    private final Encoder integratedEncoder;
    private boolean inverted, following;

    public TalonFXMotor(int canID) {
        this(canID, "");
    }

    public TalonFXMotor(int canID, String canBus) {
        TalonFXConfiguration config = new TalonFXConfiguration();

        // Set initial PIDF to zero so the motor won't move by default
        config.slot0.kP = 0;
        config.slot0.kI = 0;
        config.slot0.kD = 0;
        config.slot0.kF = 0;

        // Select the motor's integrated encoder
        config.primaryPID.selectedFeedbackSensor = FeedbackDevice.IntegratedSensor;

        fx = new TalonFX(canID, canBus);
        fx.configAllSettings(config);
        inverted = false;
        following = false;

        integratedEncoder = new Encoder() {
            @Override
            public Angle getAngle() {
                return CWAngle.rot(fx.getSelectedSensorPosition() / ENCODER_TICKS_PER_ROTATION);
            }

            @Override
            public Angle getVelocity() {
                return CWAngle.rot(fx.getSelectedSensorVelocity() / ENCODER_TICKS_PER_ROTATION * 10);
            }

            @Override
            public void setAngle(Angle angle) {
                fx.setSelectedSensorPosition(angle.cw().rot() * ENCODER_TICKS_PER_ROTATION);
            }
        };
    }

    private void updateInvertState() {
        if (following) {
            fx.setInverted(inverted ? InvertType.OpposeMaster : InvertType.FollowMaster);
        } else {
            fx.setInverted(inverted ? InvertType.InvertMotorOutput : InvertType.None);
        }
    }

    @Override
    public void follow(Motor leader) {
        if (!(leader instanceof TalonFXMotor))
            throw new UnsupportedOperationException("Talon FX motors can only follow other Talon FX motors");
        if (following)
            throw new IllegalStateException("Already following a motor");

        TalonFX leaderFX = ((TalonFXMotor) leader).fx;
        fx.follow(leaderFX);
        following = true;

        updateInvertState();
    }

    @Override
    public void setPercentOut(double percent) {
        fx.set(ControlMode.PercentOutput, percent);
    }

    @Override
    public void setPosition(Angle position) {
        fx.set(ControlMode.Position, position.cw().rot() * ENCODER_TICKS_PER_ROTATION);
    }

    @Override
    public void setVelocity(Angle velocity) {
        fx.set(ControlMode.Velocity, velocity.cw().rot() * ENCODER_TICKS_PER_ROTATION / 10);
    }

    @Override
    public void setInverted(boolean inverted) {
        this.inverted = inverted;
        updateInvertState();
    }

    @Override
    public void setBrakeMode(boolean brake) {
        fx.setNeutralMode(brake ? NeutralMode.Brake : NeutralMode.Coast);
    }

    @Override
    public Encoder getIntegratedEncoder() {
        return integratedEncoder;
    }

    @Override
    public void resetIntegrator() {
        fx.setIntegralAccumulator(0);
    }

    @Override
    public void setP(double kP) {
        fx.config_kP(0, kP);
    }

    @Override
    public void setI(double kI) {
        fx.config_kI(0, kI);
    }

    @Override
    public void setD(double kD) {
        fx.config_kD(0, kD);
    }

    @Override
    public void setF(double kF) {
        fx.config_kF(0, kF);
    }
}
