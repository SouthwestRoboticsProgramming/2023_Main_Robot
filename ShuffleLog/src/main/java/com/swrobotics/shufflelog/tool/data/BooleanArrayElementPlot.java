package com.swrobotics.shufflelog.tool.data;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableType;

public final class BooleanArrayElementPlot extends BooleanDataPlot {
    private static final boolean[] EMPTY = new boolean[0];

    private final NetworkTableEntry entry;
    private final int index;

    public BooleanArrayElementPlot(String name, String path, double retentionTime, NetworkTableEntry entry, int index) {
        super(name, path, retentionTime);
        this.entry = entry;
        this.index = index;

        if (entry.getType() != NetworkTableType.kBooleanArray)
            throw new IllegalArgumentException("Not a boolean array entry");
    }

    @Override
    protected Boolean read() {
        boolean[] b = entry.getBooleanArray(EMPTY);

        if (index >= b.length)
            return null;

        return b[index];
    }
}
