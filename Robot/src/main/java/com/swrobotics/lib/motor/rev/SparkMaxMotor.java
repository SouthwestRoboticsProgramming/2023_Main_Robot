package com.swrobotics.lib.motor.rev;

import com.revrobotics.*;
import com.swrobotics.lib.encoder.Encoder;
import com.swrobotics.lib.motor.FeedbackMotor;
import com.swrobotics.lib.motor.Motor;
import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.CWAngle;

import edu.wpi.first.math.util.Units;

/**
 * Abstract motor implementation for a Spark MAX motor connected via CAN.
 *
 * How to connect encoders to the Spark MAX:
 *   - If in Brushless mode, the NEO encoder must be connected to the primary
 *     encoder port. An additional encoder must be connected to the data port,
 *     and rewired to match the Alternate Encoder Mode pinout.
 *   - If in Brushed mode, the primary encoder can be wired to either the primary
 *     encoder port or the data port. If you want two encoders, the primary encoder
 *     must be wired to the primary encoder port, and the alternate encoder must be
 *     wired to the data port in Alternate Encoder Mode.
 *
 * Warning: If you have a alternate encoder, the Spark MAX data port must be
 * configured and flashed for Alternate Encoder Mode using the REV Hardware
 * Client BEFORE connecting the encoder to the data port, or you may damage the
 * Spark MAX and/or encoder.
 *
 * See the <a href="https://docs.revrobotics.com/sparkmax/feature-description/encoder-port">Encoder Port Documentation</a>
 * and <a href="https://docs.revrobotics.com/sparkmax/feature-description/data-port">Data Port Documentation</a>
 * for how to wire the encoders to the ports.
 */
public abstract class SparkMaxMotor implements FeedbackMotor {
    public enum EncoderPort {
        PRIMARY,
        ALTERNATE,
        ABSOLUTE
    }

    private final CANSparkMax spark;
    private final SparkMaxPIDController pid;

    private EncoderPort feedbackEncoderPort;
    private Encoder primaryEncoder, alternateEncoder;
    private Encoder absoluteEncoder;
    private RelativeEncoder primaryEncoderRev, alternateEncoderRev;
    private AbsoluteEncoder absoluteEncoderRev;

    private boolean inverted;
    private CANSparkMax leader;

    /**
     * Creates a new instance with a specified CAN ID and motor type
     *
     * @param canID can ID of the Spark MAX
     * @param type motor type connected
     */
    public SparkMaxMotor(int canID, CANSparkMaxLowLevel.MotorType type) {
        spark = new CANSparkMax(canID, type);
        pid = spark.getPIDController();

        inverted = false;
        leader = null;

        feedbackEncoderPort = EncoderPort.PRIMARY;
        primaryEncoder = alternateEncoder = absoluteEncoder;
    }

    @Override
    public void setPercentOut(double percent) {
        pid.setReference(percent, CANSparkMax.ControlType.kDutyCycle);
    }

    @Override
    public void setPosition(Angle position) {
        pid.setReference(position.cw().rot(), CANSparkMax.ControlType.kPosition);
    }

    @Override
    public void setVelocity(Angle velocity) {
        pid.setReference(
                Units.radiansPerSecondToRotationsPerMinute(velocity.cw().rad()),
                CANSparkMax.ControlType.kVelocity);
    }

    @Override
    public void setInverted(boolean inverted) {
        this.inverted = inverted;
        if (leader != null) {
            spark.follow(leader, inverted);
        } else {
            spark.setInverted(inverted);
        }
    }

    @Override
    public void follow(Motor leader) {
        if (!(leader instanceof SparkMaxMotor))
            throw new UnsupportedOperationException("NEO motors can only follow other NEO motors");
        if (this.leader != null) throw new IllegalStateException("Already following a motor");

        CANSparkMax leaderSpark = ((SparkMaxMotor) leader).spark;
        spark.follow(leaderSpark, inverted);

        this.leader = leaderSpark;
    }

    @Override
    public void setBrakeMode(boolean brake) {
        spark.setIdleMode(brake ? CANSparkMax.IdleMode.kBrake : CANSparkMax.IdleMode.kCoast);
    }

    @Override
    public Encoder getIntegratedEncoder() {
        return feedbackEncoderPort == EncoderPort.PRIMARY ? primaryEncoder : alternateEncoder;
    }

    /**
     * Gets the relative encoder connected to the encoder port.
     *
     * @return primary encoder
     */
    public Encoder getPrimaryEncoder() {
        return primaryEncoder;
    }

    /**
     * Gets the secondary encoder connected to the data port.
     *
     * @return alternate encoder
     */
    public Encoder getAlternateEncoder() {
        return alternateEncoder;
    }

    /**
     * Gets the absolute encoder connected to the data port.
     *
     * @return absolute encoder
     */
    public Encoder getAbsoluteEncoder() {
        return absoluteEncoder;
    }

    /**
     * Enables feedback using a relative encoder connected to the encoder port.
     *
     * @param type type of the encoder connected
     * @param ticksPerRotation number of ticks per encoder rotation
     * @return this
     */
    public SparkMaxMotor withPrimaryEncoder(
            SparkMaxRelativeEncoder.Type type, int ticksPerRotation) {
        if (primaryEncoder != null) throw new IllegalStateException("Primary encoder already set");

        primaryEncoderRev = spark.getEncoder(type, ticksPerRotation);
        primaryEncoderRev.setPositionConversionFactor(1); // Rotations
        primaryEncoderRev.setVelocityConversionFactor(1); // RPM

        if (feedbackEncoderPort == EncoderPort.PRIMARY) pid.setFeedbackDevice(primaryEncoderRev);

        primaryEncoder =
                new Encoder() {
                    @Override
                    public Angle getAngle() {
                        return CWAngle.rot(primaryEncoderRev.getPosition());
                    }

                    @Override
                    public Angle getVelocity() {
                        return CWAngle.rad(
                                Units.rotationsPerMinuteToRadiansPerSecond(
                                        primaryEncoderRev.getVelocity()));
                    }

                    @Override
                    public void setAngle(Angle angle) {
                        primaryEncoderRev.setPosition(angle.cw().rot());
                    }

                    @Override
                    public void setInverted(boolean inverted) {
                        primaryEncoderRev.setInverted(inverted);
                    }
                };

        return this;
    }

    /**
     * Enables feedback using a relative encoder connected to the data port. The alternate encoder
     * cannot be used at the same time as the absolute encoder.
     *
     * @param type type of encoder connected
     * @param ticksPerRotation number of encoder ticks per rotation
     * @return this
     */
    public SparkMaxMotor withAlternateEncoder(
            SparkMaxAlternateEncoder.Type type, int ticksPerRotation) {
        if (alternateEncoder != null)
            throw new IllegalStateException("Primary encoder already set");
        if (absoluteEncoder != null)
            throw new IllegalStateException(
                    "Alternate and absolute encoders cannot be used at the same time");

        alternateEncoderRev = spark.getAlternateEncoder(type, ticksPerRotation);
        alternateEncoderRev.setPositionConversionFactor(1); // Rotations
        alternateEncoderRev.setVelocityConversionFactor(1); // RPM

        if (feedbackEncoderPort == EncoderPort.ALTERNATE)
            pid.setFeedbackDevice(alternateEncoderRev);

        alternateEncoder =
                new Encoder() {
                    @Override
                    public Angle getAngle() {
                        return CWAngle.rot(alternateEncoderRev.getPosition());
                    }

                    @Override
                    public Angle getVelocity() {
                        return CWAngle.rad(
                                Units.rotationsPerMinuteToRadiansPerSecond(
                                        alternateEncoderRev.getVelocity()));
                    }

                    @Override
                    public void setAngle(Angle angle) {
                        alternateEncoderRev.setPosition(angle.cw().rot());
                    }

                    @Override
                    public void setInverted(boolean inverted) {
                        alternateEncoderRev.setInverted(inverted);
                    }
                };

        return this;
    }

    /**
     * Enables feedback using an absolute encoder connected to the data port. The absolute encoder
     * can not be used at the same time as the alternate encoder.
     *
     * @param type encoder type connected
     * @return this
     */
    public SparkMaxMotor withAbsoluteEncoder(SparkMaxAbsoluteEncoder.Type type) {
        if (absoluteEncoder != null)
            throw new IllegalStateException("Absolute encoder already set");
        if (alternateEncoder != null)
            throw new IllegalStateException(
                    "Cannot use absolute and alternate encoder at the same time");

        absoluteEncoderRev = spark.getAbsoluteEncoder(type);
        absoluteEncoderRev.setPositionConversionFactor(1);
        absoluteEncoderRev.setVelocityConversionFactor(1);

        if (feedbackEncoderPort == EncoderPort.ABSOLUTE) pid.setFeedbackDevice(absoluteEncoderRev);

        absoluteEncoder =
                new Encoder() {
                    @Override
                    public Angle getAngle() {
                        return CWAngle.rot(absoluteEncoderRev.getPosition());
                    }

                    @Override
                    public Angle getVelocity() {
                        return CWAngle.rad(
                                Units.rotationsPerMinuteToRadiansPerSecond(
                                        absoluteEncoderRev.getVelocity()));
                    }

                    @Override
                    public void setInverted(boolean inverted) {
                        absoluteEncoderRev.setInverted(inverted);
                    }
                };

        return this;
    }

    /**
     * Selects which encoder is used for feedback control. The default is the primary encoder.
     *
     * @param feedbackEncoderPort which encoder to use
     * @return this
     */
    public SparkMaxMotor setFeedbackEncoder(EncoderPort feedbackEncoderPort) {
        this.feedbackEncoderPort = feedbackEncoderPort;

        switch (feedbackEncoderPort) {
            case PRIMARY:
                if (primaryEncoder != null) pid.setFeedbackDevice(primaryEncoderRev);
                break;
            case ALTERNATE:
                if (alternateEncoder != null) pid.setFeedbackDevice(alternateEncoderRev);
                break;
            case ABSOLUTE:
                if (absoluteEncoder != null) pid.setFeedbackDevice(absoluteEncoderRev);
            default:
                throw new IllegalArgumentException(String.valueOf(feedbackEncoderPort));
        }

        return this;
    }

    @Override
    public void resetIntegrator() {
        pid.setIAccum(0);
    }

    @Override
    public void setP(double kP) {
        pid.setP(kP);
    }

    @Override
    public void setI(double kI) {
        pid.setI(kI);
    }

    @Override
    public void setD(double kD) {
        pid.setD(kD);
    }

    @Override
    public void setF(double kF) {
        pid.setFF(kF);
    }
}
