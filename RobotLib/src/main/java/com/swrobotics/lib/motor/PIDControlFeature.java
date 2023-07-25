package com.swrobotics.lib.motor;

import com.swrobotics.lib.net.NTEntry;
import com.swrobotics.mathlib.Angle;

public interface PIDControlFeature {
    default void setPosition(Angle targetPos) { setPositionArbFF(targetPos, 0); }
    default void setVelocity(Angle targetVel) { setVelocityArbFF(targetVel, 0); }
    void setPositionArbFF(Angle targetPos, double arbFF);
    void setVelocityArbFF(Angle targetVel, double arbFF);

    void resetIntegrator();

    void setP(double p);
    void setI(double i);
    void setD(double d);
    void setF(double f);

    default void setPID(double p, double i, double d) {
        setP(p);
        setI(i);
        setD(d);
    }

    default void setPIDF(double p, double i, double d, double f) {
        setPID(p, i, d);
        setF(f);
    }

    default void setPID(NTEntry<Double> p, NTEntry<Double> i, NTEntry<Double> d) {
        p.nowAndOnChange(this::setP);
        i.nowAndOnChange(this::setI);
        d.nowAndOnChange(this::setD);
    }

    default void setPIDF(NTEntry<Double> p, NTEntry<Double> i, NTEntry<Double> d, NTEntry<Double> f) {
        setPID(p, i, d);
        f.nowAndOnChange(this::setF);
    }
}
