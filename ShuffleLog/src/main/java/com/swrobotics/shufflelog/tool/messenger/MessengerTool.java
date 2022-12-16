package com.swrobotics.shufflelog.tool.messenger;

import com.swrobotics.messenger.client.MessengerClient;
import com.swrobotics.shufflelog.ShuffleLog;
import com.swrobotics.shufflelog.StreamUtil;
import com.swrobotics.shufflelog.tool.Tool;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiTableFlags;
import imgui.type.ImInt;
import imgui.type.ImString;

public final class MessengerTool implements Tool {
    private final MessengerClient msg;
    private final ImString host;
    private final ImInt port;
    private final ImString name;

    public MessengerTool(ShuffleLog log) {
        host = new ImString(64);
        port = new ImInt(5805);
        name = new ImString(64);

        host.set("localhost");
        name.set("ShuffleLog");

        msg = new MessengerClient(host.get(), port.get(), name.get());
        log.setMessenger(msg);
    }

    private void fancyLabel(String label) {
        ImGui.tableNextColumn();
        ImGui.text(label);
        ImGui.tableNextColumn();
        ImGui.setNextItemWidth(-1);
    }

    private void showConnectionParams() {
        boolean changed = false;
        if (ImGui.beginTable("conn_layout", 2, ImGuiTableFlags.SizingStretchProp)) {
            fancyLabel("Host:");
            changed = ImGui.inputText("##conn_host", host);

            fancyLabel("Port:");
            changed |= ImGui.inputInt("##conn_port", port);

            fancyLabel("Name:");
            changed |= ImGui.inputText("##conn_name", name);

            ImGui.separator();

            ImGui.tableNextColumn();
            ImGui.text("Status:");
            ImGui.tableNextColumn();
            boolean connected = msg.isConnected();
            if (connected) {
                ImGui.pushStyleColor(ImGuiCol.Text, 0.0f, 1.0f, 0.0f, 1.0f);
                ImGui.text("Connected");
            } else {
                ImGui.pushStyleColor(ImGuiCol.Text, 1.0f, 0.0f, 0.0f, 1.0f);
                ImGui.text("Not connected");

                Exception e = msg.getLastConnectionException();
                if (e != null && ImGui.isItemHovered()) {
                    ImGui.beginTooltip();
                    ImGui.text(StreamUtil.getStackTrace(e));
                    ImGui.endTooltip();
                }
            }
            ImGui.popStyleColor();

            ImGui.endTable();
        }

        if (changed)
            msg.reconnect(host.get(), port.get(), name.get());
    }

    @Override
    public void process() {
        if (ImGui.begin("Messenger")) {
            ImGui.setWindowPos(50, 50, ImGuiCond.FirstUseEver);
            ImGui.setWindowSize(500, 450, ImGuiCond.FirstUseEver);

            showConnectionParams();
        }
        ImGui.end();
    }
}
