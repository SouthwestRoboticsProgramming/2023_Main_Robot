package com.swrobotics.shufflelog.tool.data;

import com.swrobotics.shufflelog.ShuffleLog;
import com.swrobotics.shufflelog.tool.Tool;
import edu.wpi.first.networktables.NetworkTableEntry;
import imgui.ImGui;

import java.util.ArrayList;
import java.util.List;

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

        if (ImGui.begin("Data Log")) {
            if (ImGui.beginChild("drag_target")) {
                List<Graph> graphsCopy = new ArrayList<>(graphs);
                for (Graph graph : graphsCopy) {
                    if (graph.getPlots().isEmpty()) {
                        graphs.remove(graph);
                        continue;
                    }

                    String name = graph.getName();

                    ImGui.pushID(name);
                    showGraph(graph, time);

                    if (ImGui.beginPopupContextItem(name)) {
                        if (ImGui.selectable("Remove graph")) {
                            graphs.remove(graph);
                        }

                        if (graph.getPlots().size() > 1) {
                            ImGui.separator();

                            List<DataPlot<?>> split = new ArrayList<>();
                            for (DataPlot<?> plot : graph.getPlots()) {
                                if (ImGui.selectable("Split '" + plot.getName() + "'")) {
                                    split.add(plot);
                                }
                            }

                            if (ImGui.selectable("Split all")) {
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

                            ImGui.separator();

                            DataPlot<?> removed = null;
                            for (DataPlot<?> plot : graph.getPlots()) {
                                if (ImGui.selectable("Remove '" + plot.getName() + "'")) {
                                    removed = plot;
                                }
                            }

                            if (removed != null)
                                graph.removePlot(removed);
                        }

                        ImGui.endPopup();
                    }

                    if (ImGui.beginDragDropSource()) {
                        ImGui.text(name);
                        ImGui.setDragDropPayload("DATALOG_DRAG_GRAPH", graph);
                        ImGui.endDragDropSource();
                    }

                    if (ImGui.beginDragDropTarget()) {
                        Graph payload = ImGui.acceptDragDropPayload("DATALOG_DRAG_GRAPH");
                        if (payload != null) {
                            graphs.remove(payload);
                            graph.addPlots(payload.getPlots());
                            payload.clearPlots();
                        }
                        ImGui.endDragDropTarget();
                    }
                    ImGui.popID();
                }
                ImGui.endChild();
            }
            if (ImGui.beginDragDropTarget()) {
                PlotDef plotDef = ImGui.acceptDragDropPayload("NT_DRAG_VALUE");
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
                ImGui.endDragDropTarget();
            }
        }
        ImGui.end();
    }
}
