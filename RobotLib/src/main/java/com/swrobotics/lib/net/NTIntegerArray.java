package com.swrobotics.lib.net;

/** Represents a {@code int[]} value stored in NetworkTables. */
public final class NTIntegerArray extends NTPrimitive<int[]> {
    private final double[] defaultVals;

    public NTIntegerArray(String path, int... defaultVals) {
        super(path, defaultVals);
        this.defaultVals = intArrayToDoubleArray(defaultVals);
    }

    @Override
    public int[] get() {
        return doubleArrayToIntArray(entry.getDoubleArray(defaultVals));
    }

    public int get(int index) {
        return get()[index];
    }

    @Override
    public void set(int[] data) {
        entry.setDoubleArray(intArrayToDoubleArray(data));
    }

    public void set(int index, int value) {
        int[] data = get();
        data[index] = value;
        set(data);
    }

    private double[] intArrayToDoubleArray(int[] data) {
        double[] out = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            out[i] = data[i];
        }
        return out;
    }

    private int[] doubleArrayToIntArray(double[] data) {
        int[] out = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            out[i] = (int) data[i];
        }
        return out;
    }
}
