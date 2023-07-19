package com.swrobotics.lib.net;

/** Represents a {@code int} value stored in NetworkTables. */
public final class NTInteger extends NTPrimitive<Integer> {
    private final int defaultVal;

    /**
     * Creates a new {@code int} entry with a specified path. The path can be split using the '/'
     * character to organize entries into groups.
     *
     * @param path path
     */
    public NTInteger(String path, int defaultVal) {
        super(path, defaultVal);
        this.defaultVal = defaultVal;
    }

    @Override
    public Integer get() {
        return entry.getNumber(defaultVal).intValue();
    }

    @Override
    public void set(Integer value) {
        entry.setNumber(value);
    }
}
