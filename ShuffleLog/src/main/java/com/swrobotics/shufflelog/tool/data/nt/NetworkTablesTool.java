package com.swrobotics.shufflelog.tool.data.nt;

import com.swrobotics.shufflelog.tool.Tool;
import imgui.ImGui;

public final class NetworkTablesTool implements Tool {
    private static final String TITLE = "NetworkTables";

    @Override
    public void process() {
        if (ImGui.begin(TITLE)) {
            ImGui.text("TODO");
        }
        ImGui.end();
    }
}
