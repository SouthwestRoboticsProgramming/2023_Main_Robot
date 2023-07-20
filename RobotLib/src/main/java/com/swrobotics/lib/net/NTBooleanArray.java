package com.swrobotics.lib.net;

/** Represents a {@code boolean[]} value stored in NetworkTables. */
public final class NTBooleanArray extends NTPrimitive<boolean[]> {
    private final boolean[] defaultVals;

    public NTBooleanArray(String path, boolean... defaultVals) {
        super(path, defaultVals);
        this.defaultVals = defaultVals;
    }

    @Override
    public boolean[] get() {
        return entry.getBooleanArray(defaultVals);
    }

    public boolean get(int index) {
        return get()[index];
    }

    @Override
    public void set(boolean[] value) {
        entry.setBooleanArray(value);
    }

    public void set(int index, boolean value) {
        boolean[] data = get();
        data[index] = value;
        set(data);
    }
}
