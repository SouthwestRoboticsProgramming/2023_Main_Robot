package com.swrobotics.shufflelog.tool;

import imgui.ImGui;
import imgui.type.ImBoolean;

public final class MenuBarTool implements Tool {
    private final ImBoolean showDemo;

    public MenuBarTool() {
        showDemo = new ImBoolean(false);
    }

    @Override
    public void process() {
        if (ImGui.beginMainMenuBar()) {
            if (ImGui.beginMenu("Debug")) {
                ImGui.menuItem("Show demo", null, showDemo);

                ImGui.endMenu();
            }

            ImGui.endMainMenuBar();
        }

        if (showDemo.get())
            ImGui.showDemoWindow();
    }
}
