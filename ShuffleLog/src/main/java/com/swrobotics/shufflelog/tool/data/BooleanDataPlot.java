package com.swrobotics.shufflelog.tool.data;

import imgui.extension.implot.ImPlot;

public abstract class BooleanDataPlot extends DataPlot<Boolean> {
    public BooleanDataPlot(String name, String path, double retentionTime) {
        super(name, path, retentionTime);
    }

    @Override
    public double getMinValue() {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public double getMaxValue() {
        return Double.NEGATIVE_INFINITY;
    }

    @Override
    public void plot(boolean showName) {
        double[] times = new double[history.size()];
        double[] values = new double[history.size()];
        for (int i = 0; i < history.size(); i++) {
            DataPoint<Boolean> point = history.get(i);
            times[i] = point.getTime();
            values[i] = point.getValue() ? 1 : 0;
        }

        ImPlot.plotDigital(showName ? name : "", times, values, times.length);
    }
}
