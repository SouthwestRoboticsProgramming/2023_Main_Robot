package com.swrobotics.lib.net;

/** Represents a {@code String[]} value stored in NetworkTables. */
public final class NTStringArray extends NTPrimitive<String[]> {
    private final String[] defaultVals;

    public NTStringArray(String path, String... defaultVals) {
        super(path, defaultVals);
        this.defaultVals = defaultVals;
    }

    @Override
    public String[] get() {
        return entry.getStringArray(defaultVals);
    }

    public String get(int index) {
        return get()[index];
    }

    @Override
    public void set(String[] value) {
        entry.setStringArray(value);
    }

    public void set(int index, String value) {
        String[] data = get();
        data[index] = value;
        set(data);
    }
}
