package com.swrobotics.shufflelog.tool.data;

import imgui.extension.implot.ImPlot;

public abstract class DoubleDataPlot extends DataPlot<Double> {
    public DoubleDataPlot(String name, String path, double retentionTime) {
        super(name, path, retentionTime);
    }

    @Override
    public double getMinValue() {
        double min = Double.POSITIVE_INFINITY;
        for (DataPoint<Double> point : history) {
            min = Math.min(min, point.getValue());
        }
        return min;
    }

    @Override
    public double getMaxValue() {
        double max = Double.NEGATIVE_INFINITY;
        for (DataPoint<Double> point : history) {
            max = Math.max(max, point.getValue());
        }
        return max;
    }

    @Override
    public void plot(boolean showName) {
        double[] times = new double[history.size()];
        double[] values = new double[history.size()];
        for (int i = 0; i < history.size(); i++) {
            DataPoint<Double> point = history.get(i);
            times[i] = point.getTime();
            values[i] = point.getValue();
        }

        ImPlot.plotLine(showName ? name : "", times, values, times.length, 0);
    }
}
