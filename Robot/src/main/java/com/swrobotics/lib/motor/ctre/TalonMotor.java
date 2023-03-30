package com.swrobotics.lib.motor.ctre;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.InvertType;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.BaseTalon;
import com.swrobotics.lib.encoder.Encoder;
import com.swrobotics.lib.motor.FeedbackMotor;
import com.swrobotics.lib.motor.Motor;
import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.CWAngle;

public abstract class TalonMotor implements FeedbackMotor {
    private final int encoderTicksPerRotation;

    private final BaseTalon talon;
    private final Encoder integratedEncoder;
    private boolean inverted, following;

    /**
     * @param talon talon to wrap, should already be configured
     */
    public TalonMotor(BaseTalon talon, int encoderTicksPerRotation) {
        this.talon = talon;
        inverted = false;
        following = false;

        this.encoderTicksPerRotation = encoderTicksPerRotation;

        integratedEncoder = new Encoder() {
            @Override
            public Angle getAngle() {
                return CWAngle.rot(TalonMotor.this.talon.getSelectedSensorPosition() / encoderTicksPerRotation);
            }

            @Override
            public Angle getVelocity() {
                return CWAngle.rot(TalonMotor.this.talon.getSelectedSensorVelocity() / encoderTicksPerRotation * 10);
            }

            @Override
            public void setAngle(Angle angle) {
                TalonMotor.this.talon.setSelectedSensorPosition(angle.cw().rot() * encoderTicksPerRotation);
            }
        };
    }

    private void updateInvertState() {
        if (following) {
            talon.setInverted(inverted ? InvertType.OpposeMaster : InvertType.FollowMaster);
        } else {
            talon.setInverted(inverted ? InvertType.InvertMotorOutput : InvertType.None);
        }
    }

    @Override
    public void follow(Motor leader) {
        if (!(leader instanceof TalonMotor))
            throw new UnsupportedOperationException("Talon motors can only follow other Talon motors");
        if (following)
            throw new IllegalStateException("Already following a motor");

        BaseTalon leaderTalon = ((TalonMotor) leader).talon;
        talon.follow(leaderTalon);
        following = true;

        updateInvertState();
    }

    @Override
    public void setPercentOut(double percent) {
        talon.set(ControlMode.PercentOutput, percent);
    }

    @Override
    public void setPosition(Angle position) {
        talon.set(ControlMode.Position, position.cw().rot() * encoderTicksPerRotation);
    }

    @Override
    public void setVelocity(Angle velocity) {
        talon.set(ControlMode.Velocity, velocity.cw().rot() * encoderTicksPerRotation / 10);
    }

    @Override
    public void setInverted(boolean inverted) {
        this.inverted = inverted;
        updateInvertState();
    }

    @Override
    public void setBrakeMode(boolean brake) {
        talon.setNeutralMode(brake ? NeutralMode.Brake : NeutralMode.Coast);
    }

    @Override
    public Encoder getIntegratedEncoder() {
        return integratedEncoder;
    }

    @Override
    public void resetIntegrator() {
        talon.setIntegralAccumulator(0);
    }

    @Override
    public void setP(double kP) {
        talon.config_kP(0, kP);
    }

    @Override
    public void setI(double kI) {
        talon.config_kI(0, kI);
    }

    @Override
    public void setD(double kD) {
        talon.config_kD(0, kD);
    }

    @Override
    public void setF(double kF) {
        talon.config_kF(0, kF);
    }
}
