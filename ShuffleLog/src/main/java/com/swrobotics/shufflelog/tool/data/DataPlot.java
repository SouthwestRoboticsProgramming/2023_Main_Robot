package com.swrobotics.shufflelog.tool.data;

import java.util.ArrayList;
import java.util.List;

public abstract class DataPlot<T> {
    protected final List<DataPoint<T>> history;
    private final String path;
    protected final String name;
    private final double retentionTime;

    public DataPlot(String name, String path, double retentionTime) {
        this.name = name;
        this.path = path;
        this.retentionTime = retentionTime;
        history = new ArrayList<>();
    }

    public abstract void plot(boolean showName);

    public abstract double getMinValue();

    public abstract double getMaxValue();

    public double getMinTime() {
        return history.get(0).getTime();
    }

    public double getMaxTime() {
        return history.get(history.size() - 1).getTime();
    }

    /**
     * @return current value or null if invalid
     */
    protected abstract T read();

    /**
     * @param time current timestamp
     * @return whether this plot is still valid
     */
    public final boolean sample(double time) {
        T value = read();
        if (value == null) return false;

        history.add(new DataPoint<>(time, value));

        while (time - history.get(0).getTime() > retentionTime) history.remove(0);

        return true;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }
}
