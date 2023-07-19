package com.swrobotics.lib.net;

/** Represents a {@code boolean} value stored in NetworkTables. */
public class NTBoolean extends NTPrimitive<Boolean> {
    public enum Mode {
        /** Shows this entry as a checkbox in ShuffleLog. */
        TOGGLE(0),

        /** Shows this entry as a button in ShuffleLog, with the value as true when pressed. */
        MOMENTARY(1),

        /** Shows this entry as a button in ShuffleLog, with the value as false when pressed. */
        INVERSE_MOMENTARY(2),

        /** Shows this entry as a box, colored red for false and green for true */
        INDICATOR(3);

        private final int metaId;

        Mode(int metaId) {
            this.metaId = metaId;
        }
    }

    private final boolean defaultVal;

    public NTBoolean(String path, boolean defaultVal) {
        this(path, defaultVal, Mode.TOGGLE);
    }

    public NTBoolean(String path, boolean defaultVal, Mode mode) {
        super(path, defaultVal);
        this.defaultVal = defaultVal;

        int modeId = mode.metaId;
        NTInteger metadata = new NTInteger(ShuffleLog.METADATA_TABLE + path, modeId);
        metadata.set(modeId);
    }

    @Override
    public Boolean get() {
        return entry.getBoolean(defaultVal);
    }

    @Override
    public void set(Boolean value) {
        entry.setBoolean(value);
    }
}
