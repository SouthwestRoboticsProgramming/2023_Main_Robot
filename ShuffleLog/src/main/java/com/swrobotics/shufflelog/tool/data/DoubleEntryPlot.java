package com.swrobotics.shufflelog.tool.data;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableType;

public final class DoubleEntryPlot extends DoubleDataPlot {
    private final NetworkTableEntry entry;

    public DoubleEntryPlot(String name, String path, double retentionTime, NetworkTableEntry entry) {
        super(name, path, retentionTime);
        this.entry = entry;

        if (entry.getType() != NetworkTableType.kDouble)
            throw new IllegalArgumentException("Not a double entry");
    }

    @Override
    protected Double read() {
        if (!entry.exists())
            return null;

        return entry.getDouble(0);
    }
}
