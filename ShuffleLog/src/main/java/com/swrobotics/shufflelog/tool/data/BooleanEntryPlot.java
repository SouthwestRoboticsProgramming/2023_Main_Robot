package com.swrobotics.shufflelog.tool.data;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableType;

public final class BooleanEntryPlot extends BooleanDataPlot {
    private final NetworkTableEntry entry;

    public BooleanEntryPlot(String name, String path, double retentionTime, NetworkTableEntry entry) {
        super(name, path, retentionTime);
        this.entry = entry;

        if (entry.getType() != NetworkTableType.kBoolean)
            throw new IllegalArgumentException("Not a boolean entry");
    }

    @Override
    protected Boolean read() {
        if (!entry.exists())
            return null;

        return entry.getBoolean(false);
    }
}
