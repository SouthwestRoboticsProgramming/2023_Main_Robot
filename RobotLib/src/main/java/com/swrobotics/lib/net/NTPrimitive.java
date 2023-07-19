package com.swrobotics.lib.net;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableEvent;
import edu.wpi.first.networktables.NetworkTableInstance;

import java.util.EnumSet;

/**
 * Represents one data entry in NetworkTables.
 *
 * @param <T> data type
 */
public abstract class NTPrimitive<T> extends NTEntry<T> {
    protected final NetworkTableEntry entry;

    /**
     * Creates a new entry with a specified path. The path can be split using the '/' character to
     * organize entries into groups.
     *
     * <p>NOTE: This entry is persistent by default.
     *
     * @param path path
     */
    public NTPrimitive(String path, T defaultVal) {
        NetworkTableInstance inst = NetworkTableInstance.getDefault();
        NetworkTable table = inst.getTable("");
        String[] parts = path.split("/");
        for (int i = 0; i < parts.length - 1; i++) {
            table = table.getSubTable(parts[i]);
        }
        entry = table.getEntry(parts[parts.length - 1]);

        // Ensure entry actually exists so it is editable
        if (!entry.exists()) set(defaultVal);
    }

    @Override
    public NTEntry<T> setPersistent() {
        entry.setPersistent();
        return this;
    }

    @Override
    public void registerChangeListeners(Runnable fireFn) {
        NetworkTableInstance.getDefault()
                .addListener(
                        entry,
                        EnumSet.of(NetworkTableEvent.Kind.kValueAll),
                        (event) -> fireFn.run());
    }
}
