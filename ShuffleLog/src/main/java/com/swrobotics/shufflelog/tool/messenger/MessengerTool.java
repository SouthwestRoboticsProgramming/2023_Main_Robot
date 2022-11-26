package com.swrobotics.shufflelog.tool.messenger;

import com.swrobotics.messenger.client.MessengerClient;
import com.swrobotics.shufflelog.ShuffleLog;
import com.swrobotics.shufflelog.StreamUtil;
import com.swrobotics.shufflelog.tool.Tool;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiTableFlags;
import imgui.type.ImInt;
import imgui.type.ImString;

import static imgui.ImGui.*;

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
        tableNextColumn();
        text(label);
        tableNextColumn();
        setNextItemWidth(-1);
    }

    private void showConnectionParams() {
        boolean changed = false;
        if (beginTable("conn_layout", 2, ImGuiTableFlags.SizingStretchProp)) {
            fancyLabel("Host:");
            changed = inputText("##conn_host", host);

            fancyLabel("Port:");
            changed |= inputInt("##conn_port", port);

            fancyLabel("Name:");
            changed |= inputText("##conn_name", name);

            separator();

            tableNextColumn();
            text("Status:");
            tableNextColumn();
            boolean connected = msg.isConnected();
            if (connected) {
                pushStyleColor(ImGuiCol.Text, 0.0f, 1.0f, 0.0f, 1.0f);
                text("Connected");
            } else {
                pushStyleColor(ImGuiCol.Text, 1.0f, 0.0f, 0.0f, 1.0f);
                text("Not connected");

                Exception e = msg.getLastConnectionException();
                if (e != null && isItemHovered()) {
                    beginTooltip();
                    text(StreamUtil.getStackTrace(e));
                    endTooltip();
                }
            }
            popStyleColor();

            endTable();
        }

        if (changed)
            msg.reconnect(host.get(), port.get(), name.get());
    }

    @Override
    public void process() {
        if (begin("Messenger")) {
            setWindowPos(50, 50, ImGuiCond.FirstUseEver);
            setWindowSize(500, 450, ImGuiCond.FirstUseEver);

            showConnectionParams();
        }
        end();
    }
}
