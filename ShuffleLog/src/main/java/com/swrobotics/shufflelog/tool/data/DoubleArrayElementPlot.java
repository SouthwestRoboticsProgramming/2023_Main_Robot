package com.swrobotics.shufflelog.tool.data;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableType;

public final class DoubleArrayElementPlot extends DoubleDataPlot {
    private static final double[] EMPTY = new double[0];

    private final NetworkTableEntry entry;
    private final int index;

    public DoubleArrayElementPlot(String name, String path, double retentionTime, NetworkTableEntry entry, int index) {
        super(name, path, retentionTime);
        this.entry = entry;
        this.index = index;

        if (entry.getType() != NetworkTableType.kDoubleArray)
            throw new IllegalArgumentException("Not a double array entry");
    }

    @Override
    protected Double read() {
        double[] d = entry.getDoubleArray(EMPTY);

        if (index >= d.length)
            return null;

        return d[index];
    }
}
