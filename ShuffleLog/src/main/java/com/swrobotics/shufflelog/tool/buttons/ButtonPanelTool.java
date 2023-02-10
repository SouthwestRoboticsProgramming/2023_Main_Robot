package com.swrobotics.shufflelog.tool.buttons;

import com.swrobotics.messenger.client.MessengerClient;
import com.swrobotics.shufflelog.tool.Tool;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiColorEditFlags;
import imgui.flag.ImGuiStyleVar;

public final class ButtonPanelTool implements Tool {
    private final ButtonPanel panel;
    private final ReactionTime reactionTime;
    private final RobotButtonIO io;

    public ButtonPanelTool(MessengerClient msg) {
        panel = new SerialButtonPanel();
//        panel = new VirtualButtonPanel();

        reactionTime = new ReactionTime(panel);
        reactionTime.begin();

        io = new RobotButtonIO(msg, panel);
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

    private ButtonPanel.SwitchState prevSwitch = ButtonPanel.SwitchState.DOWN;

    @Override
    public void process() {
        ButtonPanel.SwitchState switchState = panel.getSwitchState();
        boolean isMatchMode = switchState == ButtonPanel.SwitchState.DOWN;
        io.setEnabled(isMatchMode);
        io.sendButtonData();

        if (!isMatchMode) {
            if (prevSwitch == ButtonPanel.SwitchState.DOWN)
                reactionTime.begin();

            reactionTime.update();
        }
        prevSwitch = switchState;

        panel.processIO();
        showGUI();
    }
}
