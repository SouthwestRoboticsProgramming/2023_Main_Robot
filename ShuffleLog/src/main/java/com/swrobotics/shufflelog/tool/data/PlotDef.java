package com.swrobotics.shufflelog.tool.data;

import edu.wpi.first.networktables.NetworkTableEntry;

public final class PlotDef {
    public enum Type {
        BOOLEAN,
        DOUBLE,
        BOOLEAN_ARRAY_ENTRY,
        DOUBLE_ARRAY_ENTRY
    }

    private final Type type;
    private final String path;
    private final String name;
    private final NetworkTableEntry entry;
    private final int index;

    public PlotDef(Type type, String path, String name, NetworkTableEntry entry) {
        this(type, path, name, entry, 0);
    }

    public PlotDef(Type type, String path, String name, NetworkTableEntry entry, int index) {
        this.type = type;
        this.path = path;
        this.name = name;
        this.entry = entry;
        this.index = index;
    }

    public Type getType() {
        return type;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public NetworkTableEntry getEntry() {
        return entry;
    }

    public int getIndex() {
        return index;
    }
}
