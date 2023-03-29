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
        setPID(kP.get(), kI.get(), kD.get());
        kP.onChange(() -> setP(kP.get()));
        kI.onChange(() -> setI(kI.get()));
        kD.onChange(() -> setD(kD.get()));
    }

    default void setPIDF(double kP, double kI, double kD, double kF) {
        setP(kP);
        setI(kI);
        setD(kD);
        setF(kF);
    }

    default void setPIDF(NTDouble kP, NTDouble kI, NTDouble kD, NTDouble kF) {
        setPID(kP, kI, kD);
        setF(kF.get());
        kF.onChange(() -> setF(kF.get()));
    }
}
