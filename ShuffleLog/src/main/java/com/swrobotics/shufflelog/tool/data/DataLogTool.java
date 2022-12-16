package com.swrobotics.shufflelog.tool.data;

import com.swrobotics.shufflelog.ShuffleLog;
import com.swrobotics.shufflelog.tool.Tool;
import edu.wpi.first.networktables.NetworkTableEntry;

import java.util.ArrayList;
import java.util.List;

import static imgui.ImGui.*;

public final class DataLogTool implements Tool {
    private static final int HISTORY_RETENTION_TIME = 10; // Seconds

    private final ShuffleLog log;
    private final List<Graph> graphs;

    public DataLogTool(ShuffleLog log) {
        this.log = log;
        graphs = new ArrayList<>();
    }

    private void addPlot(DataPlot<?> plot) {
        for (Graph graph : graphs) {
            for (DataPlot<?> p : graph.getPlots()) {
                if (p.getPath().equals(plot.getPath())) {
                    return;
                }
            }
        }

        Graph graph = new Graph();
        graph.addPlot(plot);
        graphs.add(graph);
    }

    public void addBooleanPlot(String path, String name, NetworkTableEntry entry) {
        addPlot(new BooleanEntryPlot(name, path, HISTORY_RETENTION_TIME, entry));
    }

    public void addDoublePlot(String path, String name, NetworkTableEntry entry) {
        addPlot(new DoubleEntryPlot(name, path, HISTORY_RETENTION_TIME, entry));
    }

    public void addBooleanArrayEntryPlot(String path, String name, NetworkTableEntry entry, int index) {
        addPlot(new BooleanArrayElementPlot(name, path, HISTORY_RETENTION_TIME, entry, index));
    }

    public void addDoubleArrayEntryPlot(String path, String name, NetworkTableEntry entry, int index) {
        addPlot(new DoubleArrayElementPlot(name, path, HISTORY_RETENTION_TIME, entry, index));
    }

    private void showGraph(Graph graph, double time) {
        graph.sample(time);
        graph.plot();
    }

    @Override
    public void process() {
        double time = log.getTimestamp();

        if (begin("Data Log")) {
            if (beginChild("drag_target")) {
                List<Graph> graphsCopy = new ArrayList<>(graphs);
                for (Graph graph : graphsCopy) {
                    if (graph.getPlots().isEmpty()) {
                        graphs.remove(graph);
                        continue;
                    }

                    String name = graph.getName();

                    pushID(name);
                    showGraph(graph, time);

                    if (beginPopupContextItem(name)) {
                        if (selectable("Remove graph")) {
                            graphs.remove(graph);
                        }

                        if (graph.getPlots().size() > 1) {
                            separator();

                            List<DataPlot<?>> split = new ArrayList<>();
                            for (DataPlot<?> plot : graph.getPlots()) {
                                if (selectable("Split '" + plot.getName() + "'")) {
                                    split.add(plot);
                                }
                            }

                            if (selectable("Split all")) {
                                boolean shouldSplit = false;
                                for (DataPlot<?> plot : graph.getPlots()) {
                                    // Keep the first plot in the current graph
                                    if (!shouldSplit) {
                                        shouldSplit = true;
                                        continue;
                                    }

                                    split.add(plot);
                                }
                            }

                            for (DataPlot<?> plot : split) {
                                graph.removePlot(plot);
                                addPlot(plot);
                            }

                            separator();

                            DataPlot<?> removed = null;
                            for (DataPlot<?> plot : graph.getPlots()) {
                                if (selectable("Remove '" + plot.getName() + "'")) {
                                    removed = plot;
                                }
                            }

                            if (removed != null)
                                graph.removePlot(removed);
                        }

                        endPopup();
                    }

                    if (beginDragDropSource()) {
                        text(name);
                        setDragDropPayload("DATALOG_DRAG_GRAPH", graph);
                        endDragDropSource();
                    }

                    if (beginDragDropTarget()) {
                        Graph payload = acceptDragDropPayload("DATALOG_DRAG_GRAPH");
                        if (payload != null) {
                            graphs.remove(payload);
                            graph.addPlots(payload.getPlots());
                            payload.clearPlots();
                        }
                        endDragDropTarget();
                    }
                    popID();
                }
                endChild();
            }
            if (beginDragDropTarget()) {
                PlotDef plotDef = acceptDragDropPayload("NT_DRAG_VALUE");
                if (plotDef != null) {
                    switch (plotDef.getType()) {
                        case DOUBLE:
                            addDoublePlot(plotDef.getPath(), plotDef.getName(), plotDef.getEntry());
                            break;
                        case BOOLEAN:
                            addBooleanPlot(plotDef.getPath(), plotDef.getName(), plotDef.getEntry());
                            break;
                        case DOUBLE_ARRAY_ENTRY:
                            addDoubleArrayEntryPlot(plotDef.getPath(), plotDef.getName(), plotDef.getEntry(), plotDef.getIndex());
                            break;
                        case BOOLEAN_ARRAY_ENTRY:
                            addBooleanArrayEntryPlot(plotDef.getPath(), plotDef.getName(), plotDef.getEntry(), plotDef.getIndex());
                            break;
                    }
                }
                endDragDropTarget();
            }
        }
        end();
    }
}
