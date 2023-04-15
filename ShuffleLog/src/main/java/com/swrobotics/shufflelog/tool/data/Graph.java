package com.swrobotics.shufflelog.tool.data;

import imgui.ImVec2;
import imgui.extension.implot.ImPlot;
import imgui.extension.implot.flag.ImPlotFlags;
import imgui.extension.implot.flag.ImPlotLocation;
import imgui.extension.implot.flag.ImPlotOrientation;
import imgui.flag.ImGuiCond;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Graph {
    private static final double Y_PADDING = 0.05; // Percentage of Y span
    private static final ImVec2 GRAPH_SIZE = new ImVec2(-1, 200);

    private final String titleOverride;
    private final List<DataPlot<?>> plots;

    public Graph() {
        this(null);
    }

    public Graph(String titleOverride) {
        this.titleOverride = titleOverride;
        plots = new ArrayList<>();
    }

    public void plot() {
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (DataPlot<?> plot : plots) {
            minX = Math.min(minX, plot.getMinTime());
            maxX = Math.max(maxX, plot.getMaxTime());
            minY = Math.min(minY, plot.getMinValue());
            maxY = Math.max(maxY, plot.getMaxValue());
        }

        double pad = (maxY - minY) * Y_PADDING;

        // When the graph is completely flat, add a bit of padding so the line is visible
        if (pad < 10 * Double.MIN_VALUE) pad = 0.1;

        ImPlot.setNextPlotLimits(minX, maxX, minY - pad, maxY + pad, ImGuiCond.Always);
        if (ImPlot.beginPlot(
                getName(),
                "Time (s)",
                "Value",
                GRAPH_SIZE,
                ImPlotFlags.NoMenus | ImPlotFlags.NoChild,
                0,
                0)) {
            ImPlot.setLegendLocation(ImPlotLocation.East, ImPlotOrientation.Vertical, true);
            for (DataPlot<?> plot : plots) {
                plot.plot(plots.size() > 1);
            }
            ImPlot.endPlot();
        }
    }

    public void sample(double time) {
        Set<DataPlot<?>> invalidPlots = new HashSet<>();
        for (DataPlot<?> plot : plots) {
            boolean valid = plot.sample(time);
            if (!valid) invalidPlots.add(plot);
        }
        plots.removeAll(invalidPlots);
    }

    public void addPlot(DataPlot<?> plot) {
        plots.add(plot);
    }

    public void addPlots(List<DataPlot<?>> plots) {
        this.plots.addAll(plots);
    }

    public List<DataPlot<?>> getPlots() {
        return plots;
    }

    public void removePlot(DataPlot<?> plot) {
        plots.remove(plot);
    }

    public void clearPlots() {
        this.plots.clear();
    }

    public String getName() {
        if (titleOverride != null) return titleOverride;

        StringBuilder builder = new StringBuilder();
        boolean comma = false;
        for (DataPlot<?> plot : plots) {
            if (comma) builder.append(", ");
            else comma = true;

            builder.append(plot.getPath());
        }
        return builder.toString();
    }
}
