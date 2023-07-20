package com.swrobotics.lib.net;

/** Represents a {@code double[]} value stored in NetworkTables. */
public class NTDoubleArray extends NTPrimitive<double[]> {
    private final double[] defaultVals;

    public NTDoubleArray(String path, double... defaultVals) {
        super(path, defaultVals);
        this.defaultVals = defaultVals;
    }

    @Override
    public double[] get() {
        return entry.getDoubleArray(defaultVals);
    }

    public double get(int index) {
        return get()[index];
    }

    @Override
    public void set(double[] value) {
        entry.setDoubleArray(value);
    }

    public void set(int index, double value) {
        double[] data = get();
        data[index] = value;
        set(data);
    }
}
