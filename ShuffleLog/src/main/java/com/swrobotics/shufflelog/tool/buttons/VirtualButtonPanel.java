package com.swrobotics.shufflelog.tool.buttons;

import imgui.ImGui;
import imgui.flag.ImGuiColorEditFlags;
import imgui.flag.ImGuiStyleVar;

public final class VirtualButtonPanel implements ButtonPanel {
    private static final float[] LIGHT_ON = {1, 1, 1, 1};
    private static final float[] LIGHT_OFF = {0, 0, 0, 1};

    private final boolean[][] buttonsDown = new boolean[WIDTH][HEIGHT];
    private final boolean[][] lightsOn = new boolean[WIDTH][HEIGHT];

    @Override
    public boolean isButtonDown(int x, int y) {
        return buttonsDown[x][y];
    }

    @Override
    public void setButtonLight(int x, int y, boolean on) {
        lightsOn[x][y] = on;
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public SwitchState getSwitchState() {
        return SwitchState.DOWN;
    }

    @Override
    public void processIO() {
        if (ImGui.begin("Virtual Button Panel")) {
            ImGui.setWindowSize(0, 0);
            ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 0);
            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    if (x != 0)
                        ImGui.sameLine();

                    float[] color = lightsOn[x][y] ? LIGHT_ON : LIGHT_OFF;
                    ImGui.colorButton("##" + x + "," + y, color, ImGuiColorEditFlags.NoTooltip | ImGuiColorEditFlags.NoDragDrop);
                    buttonsDown[x][y] = ImGui.isItemActive();
                }
            }
            ImGui.popStyleVar();
        }
        ImGui.end();
    }
}
