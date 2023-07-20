package com.swrobotics.lib.net;

import edu.wpi.first.wpilibj.DriverStation;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a multiple choice value stored in NetworkTables. This is shown as a dropdown menu in
 * ShuffleLog with the available options. The actual value is stored as a {@code String}.
 */
public abstract class NTMultiSelect<T> extends NTPrimitive<T> {
    private final T defaultVal;
    private final String defaultName;
    private final NTStringArray metadata;

    public NTMultiSelect(String path, T defaultVal) {
        super(path, defaultVal);
        this.defaultVal = defaultVal;
        defaultName = nameOf(defaultVal);

        metadata = new NTStringArray(ShuffleLog.METADATA_TABLE + path);
    }

    // May throw IllegalArgumentException
    protected abstract T valueOf(String name);

    protected abstract String nameOf(T t);

    @SafeVarargs
    public final void setOptions(T... options) {
        setOptions(Arrays.asList(options));
    }

    public void setOptions(List<T> options) {
        String[] data = new String[options.size()];
        for (int i = 0; i < data.length; i++) {
            data[i] = nameOf(options.get(i));
        }
        metadata.set(data);

        // If the current value is invalid, set to default
        set(get());
    }

    @Override
    public T get() {
        String name = entry.getString(defaultName);
        try {
            return valueOf(name);
        } catch (IllegalArgumentException e) {
            DriverStation.reportWarning("Multi-select had invalid value: " + name, true);
            set(defaultVal);
            return defaultVal;
        }
    }

    @Override
    public void set(T value) {
        entry.setString(nameOf(value));
    }
}
