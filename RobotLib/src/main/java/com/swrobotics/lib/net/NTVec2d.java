package com.swrobotics.lib.net;

import com.swrobotics.mathlib.Vec2d;

public final class NTVec2d extends NTDoubleArray {
    public NTVec2d(String path, double defX, double defY) {
        super(path, defX, defY);
    }

    public Vec2d getVec() {
        double[] coords = get();
        return new Vec2d(coords[0], coords[1]);
    }

    public void set(Vec2d v) {
        set(v.x, v.y);
    }

    public void set(double x, double y) {
        set(new double[] {x, y});
    }
}
