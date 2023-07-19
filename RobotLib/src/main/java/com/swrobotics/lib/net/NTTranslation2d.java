package com.swrobotics.lib.net;

import edu.wpi.first.math.geometry.Translation2d;

public final class NTTranslation2d extends NTEntry<Translation2d> {
    private final NTEntry<double[]> value;

    public NTTranslation2d(String path, double defX, double defY) {
        value = new NTDoubleArray(path, defX, defY);
    }

    @Override
    public NTEntry<Translation2d> setPersistent() {
        value.setPersistent();
        return this;
    }

    @Override
    public void registerChangeListeners(Runnable fireFn) {
        value.registerChangeListeners(fireFn);
    }

    @Override
    public Translation2d get() {
        double[] coords = value.get();
        return new Translation2d(coords[0], coords[1]);
    }

    public void set(Translation2d tx) {
        set(tx.getX(), tx.getY());
    }

    public void set(double x, double y) {
        value.set(new double[] {x, y});
    }
}
