package com.swrobotics.shufflelog.tool.buttons;

import com.fazecast.jSerialComm.SerialPort;
import com.swrobotics.shufflelog.tool.Tool;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiColorEditFlags;

import java.util.Arrays;

public final class ButtonPanelTool implements Tool {
    private static final byte START_BYTE = (byte) 0xA5;

    // Buttons are numbered from left to right, then top to bottom
    // IDs skip the NOODS button
    private static final int[][] GROUP_ID_TO_BUTTON_MAP = {
            {27, 18,  9, 0, 35, 17},
            {28, 19, 10, 1, 34, 16},
            {29, 20, 11, 2, 33, 15},
            {30, 21, 12, 3, 26,  8},
            {31, 22, 13, 4, 25,  7},
            {32, 23, 14, 5, 24,  6}
    };

    private SerialPort serial;
    private boolean waitingForStart;
    private final byte[] readBuf = new byte[6];
    private int readIdx;

    private final boolean[] buttonStates = new boolean[36];
    private final byte[] currentData = new byte[6];

    public ButtonPanelTool() {
        serial = null;
        waitingForStart = false;
        readIdx = 0;
    }

    private void onButtonData() {
        System.arraycopy(readBuf, 0, currentData, 0, 6);

        for (int group = 0; group < 6; group++) {
            for (int bit = 0; bit < 6; bit++) {
                int buttonIdx = GROUP_ID_TO_BUTTON_MAP[group][bit];
                buttonStates[buttonIdx] = (readBuf[group] & (1 << bit)) != 0;
            }
        }
    }

    private void updateLightData() {
        byte[] data = new byte[7];
        data[0] = START_BYTE;
        for (int i = 1; i < 7; i++) {
            data[i] = (byte) (Math.random() * 63);
        }
        serial.writeBytes(data, data.length);
    }

    private void updateButtons() {
        if (serial == null) {
            SerialPort[] ports = SerialPort.getCommPorts();
            if (ports.length == 0) return;
            serial = ports[0];

            serial.openPort();
        }

        if (serial.bytesAvailable() <= 0)
            return;

        byte[] incoming = new byte[serial.bytesAvailable()];
        int read = serial.readBytes(incoming, incoming.length);

        for (int i = 0; i < read; i++) {
            if (waitingForStart) {
                if (incoming[i] == START_BYTE) {
                    waitingForStart = false;
                    readIdx = 0;
                }
            } else {
                readBuf[readIdx++] = incoming[i];
                if (readIdx == 6) {
                    // Packet finished
                    onButtonData();
                    waitingForStart = true;
                }
            }
        }
    }

    long lastRandomTime = System.currentTimeMillis();

    private void showGUI() {
        if (ImGui.begin("Button Panel")) {
            if (serial == null) {
                ImGui.text("Not connected");
            } else {
                ImGui.text("Connected on serial port: " + serial.getDescriptivePortName());
            }

            long time = System.currentTimeMillis();
            if (time - lastRandomTime > 250) {
                updateLightData();
                lastRandomTime = time;
            }

            ImGui.text("Button data");
            ImGui.text(Arrays.toString(currentData));

            ImGui.separator();
            for (int row = 0; row < 4; row++) {
                for (int col = 0; col < 9; col++) {
                    int idx = col + row * 9;

                    float on = buttonStates[idx] ? 1 : 0;
                    ImGui.pushStyleColor(ImGuiCol.Border, ImGui.colorConvertFloat4ToU32(on, on, on, 1));

                    ImGui.colorButton("##" + idx, new float[] {on, on, on, 1}, ImGuiColorEditFlags.NoTooltip);
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
        updateButtons();
        showGUI();
    }
}
