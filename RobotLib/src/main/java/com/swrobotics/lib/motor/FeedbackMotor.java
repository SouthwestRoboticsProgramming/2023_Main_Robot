package com.swrobotics.lib.motor;

import com.swrobotics.lib.encoder.Encoder;
import com.swrobotics.lib.net.NTEntry;
import com.swrobotics.mathlib.Angle;

public interface FeedbackMotor extends Motor {
    /**
     * Sets the target position, as measured by the integrated encoder.
     *
     * @param position target position
     */
    void setPosition(Angle position);

    /**
     * Sets the target position and adds an arbitrary feedforward percentage.
     *
     * @param position target position
     * @param arbFF arbitrary added feedforward percentage (-1 to 1)
     */
    void setPositionArbFF(Angle position, double arbFF);

    /**
     * Sets the target velocity in angle per second.
     *
     * @param velocity velocity in angle per second
     */
    void setVelocity(Angle velocity);

    Encoder getIntegratedEncoder();

    default void useExternalEncoder(Encoder encoder) {
        throw new UnsupportedOperationException();
    }

    void resetIntegrator();

    void setP(double kP);

    void setI(double kI);

    void setD(double kD);

    void setF(double kF);

    default void setPID(double kP, double kI, double kD) {
        setP(kP);
        setI(kI);
        setD(kD);
        setF(0);
    }

    default void setPID(NTEntry<Double> kP, NTEntry<Double> kI, NTEntry<Double> kD) {
        kP.nowAndOnChange(this::setP);
        kI.nowAndOnChange(this::setI);
        kD.nowAndOnChange(this::setD);
        setF(0);
    }

    default void setPIDF(double kP, double kI, double kD, double kF) {
        setP(kP);
        setI(kI);
        setD(kD);
        setF(kF);
    }

    default void setPIDF(NTEntry<Double> kP, NTEntry<Double> kI, NTEntry<Double> kD, NTEntry<Double> kF) {
        setPID(kP, kI, kD);
        kF.nowAndOnChange(this::setF);
    }
}
