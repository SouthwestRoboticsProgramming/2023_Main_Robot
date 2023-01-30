package com.swrobotics.shufflelog.tool.buttons;

import com.swrobotics.shufflelog.tool.Tool;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiColorEditFlags;
import imgui.flag.ImGuiStyleVar;

public final class ButtonPanelTool implements Tool {
    private final ButtonPanel panel;

    public ButtonPanelTool() {
        panel = new SerialButtonPanel();
//        panel = new VirtualButtonPanel();
    }

    private void showGUI() {
        if (ImGui.begin("Button Panel")) {
            if (!panel.isConnected()) {
                ImGui.text("Not connected");
            } else {
                ImGui.text("Connected");
            }

            ImGui.text("Switch: " + panel.getSwitchState());

            ImGui.separator();
            ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 0);
            for (int row = 0; row < 4; row++) {
                for (int col = 0; col < 9; col++) {
                    float on = panel.isButtonDown(col, row) ? 1 : 0;
                    ImGui.pushStyleColor(ImGuiCol.Border, ImGui.colorConvertFloat4ToU32(on, on, on, 1));

                    ImGui.colorButton("##" + row + "," + col, new float[] {on, on, on, 1}, ImGuiColorEditFlags.NoTooltip | ImGuiColorEditFlags.NoDragDrop);
                    ImGui.sameLine();

                    ImGui.popStyleColor();
                }
                ImGui.newLine();
            }
            ImGui.popStyleVar();
        }
        ImGui.end();
    }

    @Override
    public void process() {
        boolean invertLight = panel.getSwitchState() == ButtonPanel.SwitchState.DOWN;

        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 4; y++) {
                panel.setButtonLight(x, y, invertLight ^ panel.isButtonDown(x, y));
            }
        }

        panel.processIO();
        showGUI();
    }
}
