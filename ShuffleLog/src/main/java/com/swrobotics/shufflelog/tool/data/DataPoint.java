package com.swrobotics.shufflelog.tool.data;

public final class DataPoint<T> {
    private final double time;
    private final T value;

    public DataPoint(double time, T value) {
        this.time = time;
        this.value = value;
    }

    public double getTime() {
        return time;
    }

    public T getValue() {
        return value;
    }
}
