package com.swrobotics.shufflelog.tool.messenger;

import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.messenger.client.MessengerClient;
import com.swrobotics.shufflelog.ShuffleLog;
import com.swrobotics.shufflelog.StreamUtil;
import com.swrobotics.shufflelog.tool.Tool;
import com.swrobotics.shufflelog.util.RollingBuffer;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiTableColumnFlags;
import imgui.flag.ImGuiTableFlags;
import imgui.type.ImInt;
import imgui.type.ImString;

public final class MessengerTool implements Tool {
    private static final int LOG_HISTORY_SIZE = 128;

    private final MessengerClient msg;
    private final ImString host;
    private final ImInt port;
    private final ImString name;

    private final RollingBuffer<MessengerEvent> eventLog;
    private boolean prevConnected;

    public MessengerTool(ShuffleLog log) {
        host = new ImString(64);
        port = new ImInt(5805);
        name = new ImString(64);

        host.set("localhost");
        name.set("ShuffleLog");

        msg = new MessengerClient(host.get(), port.get(), name.get());
        msg.addHandler(MessengerClient.EVENT_TYPE, this::onEvent);
        log.setMessenger(msg);

        eventLog = new RollingBuffer<>(LOG_HISTORY_SIZE);
        prevConnected = false;
    }

    private void onEvent(String msgType, MessageReader reader) {
        String type = reader.readString();
        String name = reader.readString();
        String desc = reader.readString();

        eventLog.insert(new MessengerEvent(type, name, desc));
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
            if (msg.isConnected()) {
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

    private void showEventLog() {
        boolean connected = msg.isConnected();
        if (connected && !prevConnected) {
            eventLog.clear();
        }
        prevConnected = connected;

        int tableFlags = ImGuiTableFlags.BordersOuter
                | ImGuiTableFlags.BordersInnerV
                | ImGuiTableFlags.RowBg
                | ImGuiTableFlags.Resizable;

        ImGui.text("Event Log:");
        if (ImGui.beginChild("scroll_table")) {
            if (ImGui.beginTable("event_log", 3, tableFlags)) {
                ImGui.tableSetupColumn("Type", ImGuiTableColumnFlags.WidthStretch, 1);
                ImGui.tableSetupColumn("Name", ImGuiTableColumnFlags.WidthStretch, 1);
                ImGui.tableSetupColumn("Descriptor", ImGuiTableColumnFlags.WidthStretch, 2);
                ImGui.tableHeadersRow();

                eventLog.forEach((event) -> {
                    ImGui.tableNextColumn();
                    ImGui.text(event.getType());
                    ImGui.tableNextColumn();
                    ImGui.text(event.getName());
                    ImGui.tableNextColumn();
                    ImGui.text(event.getDescriptor());
                });

                ImGui.endTable();
            }
            ImGui.endChild();
        }
    }

    @Override
    public void process() {
        if (ImGui.begin("Messenger")) {
            ImGui.setWindowPos(50, 50, ImGuiCond.FirstUseEver);
            ImGui.setWindowSize(500, 450, ImGuiCond.FirstUseEver);

            showConnectionParams();
            ImGui.separator();
            showEventLog();
        }
        ImGui.end();
    }
}
