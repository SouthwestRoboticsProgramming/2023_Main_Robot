package com.swrobotics.lib.motor.ctre;

import com.ctre.phoenix.motorcontrol.*;
import com.ctre.phoenix.motorcontrol.can.BaseTalon;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import com.ctre.phoenix.sensors.CANCoder;
import com.swrobotics.lib.encoder.CanCoder;
import com.swrobotics.lib.encoder.Encoder;
import com.swrobotics.lib.motor.FeedbackMotor;
import com.swrobotics.lib.motor.Motor;
import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.CCWAngle;
import com.swrobotics.mathlib.CWAngle;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;

/** Abstract motor implementation for CTRE Talon motors connected via CAN. */
public abstract class TalonMotor implements FeedbackMotor {
    /** Encoder implementation for the integrated encoder. */
    public static final class IntegratedEncoder implements Encoder {
        public final TalonMotor motor;

        private IntegratedEncoder(TalonMotor motor) {
            this.motor = motor;
        }

        @Override
        public Angle getAngle() {
            return CCWAngle.rot(
                    motor.talon.getSelectedSensorPosition() / motor.encoderTicksPerRotation);
        }

        @Override
        public Angle getVelocity() {
            return CCWAngle.rot(
                    motor.talon.getSelectedSensorVelocity() / motor.encoderTicksPerRotation * 10);
        }

        @Override
        public void setAngle(Angle angle) {
            motor.talon.setSelectedSensorPosition(angle.ccw().rot() * motor.encoderTicksPerRotation);
        }

        @Override
        public void setInverted(boolean inverted) {
            if (!motor.canSetSensorPhase())
                throw new UnsupportedOperationException("Cannot invert integrated encoder");

            motor.talon.setSensorPhase(inverted);
        }
    }

    protected final BaseTalon talon;
    private final Faults faults;

    private Encoder integratedEncoder;
    private boolean inverted, following;
    private int encoderTicksPerRotation;
    private Boolean brake;

    /**
     * @param talon talon to wrap, should already be configured
     */
    public TalonMotor(BaseTalon talon) {
        this.talon = talon;
        faults = new Faults();

        inverted = false;
        following = false;

        // Null is neither true nor false, so first call to brake mode will always update motor state
        brake = null;

        integratedEncoder = null;
        updateInvertState();
    }

    /**
     * Gets whether the hardware supports setting the phase of the integrated sensor.
     *
     * @return if the sensor phase can be set
     */
    protected abstract boolean canSetSensorPhase();

    /**
     * Enables support for the integrated encoder, with specified ticks per rotation.
     *
     * @param encoderTicksPerRotation number of encoder ticks per shaft rotation
     */
    protected void enableIntegratedEncoder(int encoderTicksPerRotation) {
        this.encoderTicksPerRotation = encoderTicksPerRotation;
        integratedEncoder = new IntegratedEncoder(this);
    }

    private void updateInvertState() {
        // This will invert both the motor output and sensor input, keeping the
        // overall sensor phase the same.

        // According to CTRE Docs: Sensor phase is not the same as sensor
        // direction. When SetInverted is called on a motor controller, the
        // values reported by the selected sensor are also inverted. As a
        // result, changing the SetInverted input does not require changing
        // the sensor phase.

        if (following) {
            talon.setInverted(inverted ? InvertType.OpposeMaster : InvertType.FollowMaster);
        } else {
            talon.setInverted(inverted ? InvertType.InvertMotorOutput : InvertType.None);
        }
    }

    @Override
    public void follow(Motor leader) {
        if (!(leader instanceof TalonMotor))
            throw new UnsupportedOperationException(
                    "Talon motors can only follow other Talon motors");
        if (following) throw new IllegalStateException("Already following a motor");

        BaseTalon leaderTalon = ((TalonMotor) leader).talon;
        talon.follow(leaderTalon);
        following = true;

        updateInvertState();
    }

    private double prevSensorFaultTimestamp = Double.NEGATIVE_INFINITY;
    private void testSensorPhase() {
        // Talon FX always has correct sensor phase, so we can skip checking
        // CTRE Docs: Talon FX automatically phases your sensor for you. It
        // will always be correct, provided you use the getSelected* API and
        // have configured the selected feedback type to be integrated sensor.
        if (talon instanceof TalonFX)
            return;

        talon.getFaults(faults);
        if (faults.SensorOutOfPhase) {
            // Limit warning to print at most every 5 seconds
            double time = Timer.getFPGATimestamp();
            if (prevSensorFaultTimestamp < time + 5) {
                DriverStation.reportWarning("Talon (" + talon.getDeviceID() + ") sensor out of phase, use #getIntegratedEncoder().setInverted() to fix this", true);
                prevSensorFaultTimestamp = time;
            }
        }
    }

    @Override
    public void setPercentOut(double percent) {
        talon.set(ControlMode.PercentOutput, percent);
        testSensorPhase();
    }

    @Override
    public void setPosition(Angle position) {
        if (integratedEncoder == null) throw new IllegalStateException("No feedback encoder set");

        talon.set(ControlMode.Position, position.ccw().rot() * encoderTicksPerRotation);
        testSensorPhase();
    }

    @Override
    public void setPositionArbFF(Angle position, double arbFF) {
        if (integratedEncoder == null) throw new IllegalStateException("No feedback encoder set");

        talon.set(ControlMode.Position, position.ccw().rot() * encoderTicksPerRotation, DemandType.ArbitraryFeedForward, arbFF);
        testSensorPhase();
    }

    @Override
    public void setVelocity(Angle velocity) {
        if (integratedEncoder == null) throw new IllegalStateException("No feedback encoder set");

        talon.set(ControlMode.Velocity, velocity.ccw().rot() * encoderTicksPerRotation / 10);
        testSensorPhase();
    }

    @Override
    public void setInverted(boolean inverted) {
        this.inverted = inverted;
        updateInvertState();
    }

    @Override
    public void setBrakeMode(boolean brake) {
        if (this.brake != Boolean.valueOf(brake)) {
            talon.setNeutralMode(brake ? NeutralMode.Brake : NeutralMode.Coast);
            this.brake = brake;
        }
    }

    @Override
    public Encoder getIntegratedEncoder() {
        return integratedEncoder;
    }

    @Override
    public void useExternalEncoder(Encoder encoder) {
        if (encoder instanceof IntegratedEncoder) {
            TalonMotor srcTalon = ((IntegratedEncoder) encoder).motor;
            talon.configRemoteFeedbackFilter(srcTalon.talon, 0);
        } else if (encoder instanceof CanCoder.RelativeEncoder) {
            CANCoder canCoder = ((CanCoder.RelativeEncoder) encoder).can;
            talon.configRemoteFeedbackFilter(canCoder, 0);
        } else {
            throw new IllegalArgumentException(encoder + " cannot be set as Talon feedback sensor");
        }

        talon.configSelectedFeedbackSensor(FeedbackDevice.RemoteSensor0);
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
