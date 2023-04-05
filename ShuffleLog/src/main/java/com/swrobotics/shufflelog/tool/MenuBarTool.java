package com.swrobotics.shufflelog.tool;

import imgui.ImGui;
import imgui.extension.implot.ImPlot;
import imgui.type.ImBoolean;

public final class MenuBarTool implements Tool {
    private final ImBoolean showDemo, showPlotDemo;
    private final ImBoolean plotDemoOpen;

    public MenuBarTool() {
        showDemo = new ImBoolean(false);
        showPlotDemo = new ImBoolean(false);

        plotDemoOpen = new ImBoolean(true);
    }

    @Override
    public void process() {
        if (ImGui.beginMainMenuBar()) {
            if (ImGui.beginMenu("Debug")) {
                ImGui.menuItem("Show demo", null, showDemo);
                ImGui.menuItem("Show plot demo", null, showPlotDemo);

                ImGui.endMenu();
            }

            ImGui.endMainMenuBar();
        }

        if (showDemo.get()) ImGui.showDemoWindow();
        if (showPlotDemo.get()) ImPlot.showDemoWindow(plotDemoOpen);
    }
}
