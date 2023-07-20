package com.swrobotics.lib.net;

import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.CCWAngle;
import com.swrobotics.mathlib.CWAngle;

import java.util.function.Function;

public final class NTAngle extends NTEntry<Angle> {
    public enum Mode {
        CW_ROT(CWAngle::rot, (a) -> a.cw().rot()),
        CCW_ROT(CCWAngle::rot, (a) -> a.ccw().rot()),
        CW_DEG(CWAngle::deg, (a) -> a.cw().deg()),
        CCW_DEG(CCWAngle::deg, (a) -> a.ccw().deg()),
        CW_RAD(CWAngle::rad, (a) -> a.cw().rad()),
        CCW_RAD(CCWAngle::rad, (a) -> a.ccw().rad());

        final Function<Double, Angle> from;
        final Function<Angle, Double> to;

        Mode(Function<Double, Angle> from, Function<Angle, Double> to) {
            this.from = from;
            this.to = to;
        }
    }

    private final NTEntry<Double> value;
    private final Mode mode;

    public NTAngle(String path, double defaultMeasure, Mode mode) {
        this.mode = mode;
        value = new NTDouble(path, defaultMeasure);
    }

    @Override
    public NTEntry<Angle> setPersistent() {
        value.setPersistent();
        return this;
    }

    @Override
    public void registerChangeListeners(Runnable fireFn) {
        value.registerChangeListeners(fireFn);
    }

    @Override
    public void set(Angle value) {
        this.value.set(mode.to.apply(value));
    }

    @Override
    public Angle get() {
        return mode.from.apply(value.get());
    }
}
