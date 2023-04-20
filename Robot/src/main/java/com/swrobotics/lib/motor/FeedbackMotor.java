package com.swrobotics.lib.motor;

import com.swrobotics.lib.encoder.Encoder;
import com.swrobotics.lib.net.NTDouble;
import com.swrobotics.mathlib.Angle;

public interface FeedbackMotor extends Motor {
    /**
     * Sets the target position, as measured by the integrated encoder.
     *
     * @param position target position
     */
    void setPosition(Angle position);

    /**
     * Sets the target velocity in angle per second.
     *
     * @param velocity velocity in angle per second
     */
    void setVelocity(Angle velocity);

    Encoder getIntegratedEncoder();

    default void setIntegratedEncoder(Encoder encoder) {
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

    default void setPID(NTDouble kP, NTDouble kI, NTDouble kD) {
        kP.nowAndOnChange(() -> setP(kP.get()));
        kI.nowAndOnChange(() -> setI(kI.get()));
        kD.nowAndOnChange(() -> setD(kD.get()));
        setF(0);
    }

    default void setPIDF(double kP, double kI, double kD, double kF) {
        setP(kP);
        setI(kI);
        setD(kD);
        setF(kF);
    }

    default void setPIDF(NTDouble kP, NTDouble kI, NTDouble kD, NTDouble kF) {
        setPID(kP, kI, kD);
        kF.nowAndOnChange(() -> setF(kF.get()));
    }
}
