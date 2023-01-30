package com.swrobotics.shufflelog.tool.buttons;

import com.swrobotics.shufflelog.tool.Tool;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiColorEditFlags;

public final class ButtonPanelTool implements Tool {
    private final ButtonPanel panel;

    public ButtonPanelTool() {
        panel = new SerialButtonPanel();
    }

    private void showGUI() {
        if (ImGui.begin("Button Panel")) {
            if (panel.isConnected()) {
                ImGui.text("Not connected");
            } else {
                ImGui.text("Connected");
            }

            ImGui.separator();
            for (int row = 0; row < 4; row++) {
                for (int col = 0; col < 9; col++) {
                    float on = panel.isButtonDown(col, row) ? 1 : 0;
                    ImGui.pushStyleColor(ImGuiCol.Border, ImGui.colorConvertFloat4ToU32(on, on, on, 1));

                    ImGui.colorButton("##" + row + "," + col, new float[] {on, on, on, 1}, ImGuiColorEditFlags.NoTooltip);
                    ImGui.sameLine(0, 0);

                    ImGui.popStyleColor();
                }
                ImGui.newLine();
            }
        }
        ImGui.end();
    }

    @Override
    public void process() {
        panel.processIO();
        showGUI();
    }
}
