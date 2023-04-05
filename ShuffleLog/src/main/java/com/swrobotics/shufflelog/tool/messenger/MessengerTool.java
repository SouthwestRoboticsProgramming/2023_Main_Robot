package com.swrobotics.shufflelog.tool.messenger;

import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.messenger.client.MessengerClient;
import com.swrobotics.shufflelog.ShuffleLog;
import com.swrobotics.shufflelog.StreamUtil;
import com.swrobotics.shufflelog.tool.Tool;
import com.swrobotics.shufflelog.tool.ToolConstants;
import com.swrobotics.shufflelog.util.Cooldown;
import com.swrobotics.shufflelog.util.RollingBuffer;

import imgui.ImGui;
import imgui.flag.*;
import imgui.type.ImInt;
import imgui.type.ImString;

import java.util.ArrayList;
import java.util.List;

public final class MessengerTool implements Tool {
    private static final class QuickConnect {
        final String name;
        final String host;
        final int port;

        public QuickConnect(String name, String host, int port) {
            this.name = name;
            this.host = host;
            this.port = port;
        }
    }

    private static final QuickConnect[] QUICK_CONNECTS = {
        new QuickConnect("Robot", "10.21.29.3", 5805),
        new QuickConnect("Simulation", "localhost", 5805)
    };

    private static final int LOG_HISTORY_SIZE = 128;

    private final ShuffleLog shuffleLog;
    private final MessengerClient msg;
    private final ImString host;
    private final ImInt port;
    private final ImString name;

    private final RollingBuffer<MessengerEvent> eventLog;
    private boolean prevConnected;

    private final List<String> clientNames;
    private final Cooldown clientsCooldown;

    public MessengerTool(ShuffleLog log) {
        shuffleLog = log;
        host = new ImString(64);
        port = new ImInt(5805);
        name = new ImString(64);

        host.set("10.21.29.3");
        name.set("ShuffleLog");

        msg = new MessengerClient(host.get(), port.get(), name.get());
        msg.addHandler(MessengerClient.EVENT_TYPE, this::onEvent);
        msg.addHandler(MessengerClient.CLIENT_LIST_TYPE, this::onClients);
        log.setMessenger(msg);

        eventLog = new RollingBuffer<>(LOG_HISTORY_SIZE);
        prevConnected = false;

        clientNames = new ArrayList<>();
        clientsCooldown = new Cooldown(ToolConstants.MSG_CONSTANT_QUERY_COOLDOWN_TIME);
    }

    private void onEvent(String msgType, MessageReader reader) {
        String type = reader.readString();
        String name = reader.readString();
        String desc = reader.readString();

        eventLog.insert(new MessengerEvent(shuffleLog.getTimestamp(), type, name, desc));
    }

    private void onClients(String type, MessageReader reader) {
        int count = reader.readInt();
        clientNames.clear();
        for (int i = 0; i < count; i++) clientNames.add(reader.readString());
        clientNames.sort(String.CASE_INSENSITIVE_ORDER);
    }

    private void fancyLabel(String label) {
        ImGui.tableNextColumn();
        ImGui.text(label);
        ImGui.tableNextColumn();
        ImGui.setNextItemWidth(-1);
    }

    private void showConnectionParams(boolean changed) {
        if (ImGui.beginTable("conn_layout", 2, ImGuiTableFlags.SizingStretchProp)) {
            fancyLabel("Host:");
            changed |= ImGui.inputText("##conn_host", host);

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

        if (changed) msg.reconnect(host.get(), port.get(), name.get());
    }

    private void showClients() {
        if (!ImGui.treeNodeEx(
                "Connected clients (" + clientNames.size() + "):##clients",
                ImGuiTreeNodeFlags.DefaultOpen)) return;

        if (clientsCooldown.request()) msg.send(MessengerClient.GET_CLIENTS_TYPE);

        for (String client : clientNames) {
            ImGui.text(client);
        }

        ImGui.treePop();
    }

    private void showEventLog() {
        boolean connected = msg.isConnected();
        if (connected && !prevConnected) {
            eventLog.clear();
        }
        prevConnected = connected;

        int tableFlags =
                ImGuiTableFlags.BordersOuter
                        | ImGuiTableFlags.BordersInnerV
                        | ImGuiTableFlags.RowBg
                        | ImGuiTableFlags.Resizable;

        ImGui.text("Event Log:");
        if (ImGui.beginChild("scroll_table")) {
            if (ImGui.beginTable("event_log", 4, tableFlags)) {
                ImGui.tableSetupColumn("Time", ImGuiTableColumnFlags.WidthStretch, 1);
                ImGui.tableSetupColumn("Type", ImGuiTableColumnFlags.WidthStretch, 1);
                ImGui.tableSetupColumn("Name", ImGuiTableColumnFlags.WidthStretch, 1);
                ImGui.tableSetupColumn("Descriptor", ImGuiTableColumnFlags.WidthStretch, 3);
                ImGui.tableHeadersRow();

                eventLog.forEach(
                        (event) -> {
                            ImGui.tableNextColumn();
                            ImGui.text(String.format("%.3f", event.getTimestamp()));
                            ImGui.tableNextColumn();
                            ImGui.text(event.getType());
                            ImGui.tableNextColumn();
                            ImGui.text(event.getName());
                            ImGui.tableNextColumn();
                            ImGui.text(event.getDescriptor());
                        });

                ImGui.endTable();
            }
        }
        ImGui.endChild();
    }

    @Override
    public void process() {
        if (ImGui.begin("Messenger")) {
            ImGui.setWindowPos(50, 50, ImGuiCond.FirstUseEver);
            ImGui.setWindowSize(500, 450, ImGuiCond.FirstUseEver);

            ImGui.alignTextToFramePadding();
            ImGui.text("Quick connect:");
            boolean changed = false;
            for (QuickConnect c : QUICK_CONNECTS) {
                ImGui.sameLine();
                if (ImGui.button(c.name)) {
                    changed = true;
                    host.set(c.host);
                    port.set(c.port);
                }
            }

            showConnectionParams(changed);
            ImGui.separator();
            showClients();
            ImGui.separator();
            showEventLog();
        }
        ImGui.end();
    }

    public boolean isConnectedToRobot() {
        return msg.isConnected() && host.get().equals("10.21.29.3") && port.get() == 5805;
    }
}
