package com.swrobotics.shufflelog.tool;

import com.swrobotics.messenger.client.MessengerClient;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiColorEditFlags;

public final class ConeOrCubeTool implements Tool {
    private static final String MESSAGE = "Robot:GamePiece";

    private static final int CONE_COLOR = ImGui.getColorU32(1, 1, 0, 1);
    private static final int CUBE_COLOR = ImGui.getColorU32(0.25f, 0.25f, 1, 1);

    // Null is not connected
    private Boolean isCone;

    public ConeOrCubeTool(MessengerClient msg) {
        isCone = null;

        msg.addHandler(MESSAGE, (type, data) -> {
            isCone = data.readBoolean();
        });

        msg.addDisconnectHandler(() -> isCone = null);
    }

    @Override
    public void process() {
        if (ImGui.begin("Cone or Cube")) {
            if (isCone == null) {
                ImGui.textDisabled("Unknown");
                ImGui.end();
                return;
            }

            float[] color;
            if (isCone)
                color = new float[] {1, 1, 0, 1};
            else
                color = new float[] {0.25f, 0.25f, 1, 1};

            ImVec2 size = ImGui.getContentRegionAvail();
            ImGui.colorButton("##button", color, ImGuiColorEditFlags.NoTooltip | ImGuiColorEditFlags.NoDragDrop, size.x, size.y);
        }
        ImGui.end();
    }
}
