package com.swrobotics.lib.net;

import com.swrobotics.mathlib.Vec2d;

public final class NTVec2d extends NTEntry<Vec2d> {
    private final NTEntry<double[]> value;

    public NTVec2d(String path, double defX, double defY) {
        value = new NTDoubleArray(path, defX, defY);
    }

    @Override
    public Vec2d get() {
        double[] coords = value.get();
        return new Vec2d(coords[0], coords[1]);
    }

    public void set(Vec2d v) {
        set(v.x, v.y);
    }

    public void set(double x, double y) {
        value.set(new double[] {x, y});
    }

    @Override
    public NTEntry<Vec2d> setPersistent() {
        value.setPersistent();
        return this;
    }

    @Override
    public void registerChangeListeners(Runnable fireFn) {
        value.registerChangeListeners(fireFn);
    }
}
