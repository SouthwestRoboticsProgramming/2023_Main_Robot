package com.swrobotics.shufflelog.tool.data.nt;

import com.swrobotics.shufflelog.tool.Tool;
import edu.wpi.first.networktables.NetworkTableInstance;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiTableFlags;
import imgui.type.ImInt;
import imgui.type.ImString;

public final class NetworkTablesTool implements Tool {
    private static final String TITLE = "NetworkTables";

    private static final int VERSION_NT3 = 0;
    private static final int VERSION_NT4 = 1;
    private static final String[] VERSION_NAMES = {"NT3 (Old)", "NT4 (New)"};

    private static final int CONN_MODE_TEAM_NUMBER = 0;
    private static final int CONN_MODE_ADDRESS = 1;
    private static final String[] CONN_MODE_NAMES = {"Team Number", "Address"};

    private static final int DEFAULT_VERSION = VERSION_NT4;
    private static final int DEFAULT_CONN_MODE = CONN_MODE_TEAM_NUMBER;
    private static final String DEFAULT_HOST = "localhost";
    private static final int[] DEFAULT_PORT_PER_VERSION = {
            NetworkTableInstance.kDefaultPort3,
            NetworkTableInstance.kDefaultPort4
    };
    private static final int DEFAULT_TEAM_NUMBER = 2129;

    private static final int COLOR_CONNECTED = ImGui.colorConvertFloat4ToU32(0, 1, 0, 1);
    private static final int COLOR_NOT_CONNECTED = ImGui.colorConvertFloat4ToU32(1, 0, 0, 1);

    private final ImInt version;
    private final ImInt connectionMode;
    private final ImString host;
    private final ImInt portOrTeamNumber;

    private NetworkTablesConnection connection;

    public NetworkTablesTool() {
        version = new ImInt(DEFAULT_VERSION);
        connectionMode = new ImInt(DEFAULT_CONN_MODE);
        host = new ImString(64);
        host.set(DEFAULT_HOST);
        portOrTeamNumber = new ImInt(getDefaultPortOrTeamNumber());

        connection = openConnection();
    }

    private int getDefaultPortOrTeamNumber() {
        if (connectionMode.get() == CONN_MODE_TEAM_NUMBER)
            return DEFAULT_TEAM_NUMBER;

        return DEFAULT_PORT_PER_VERSION[version.get()];
    }

    private NetworkTablesConnection openConnection() {
        boolean isNt4 = version.get() == VERSION_NT4;
        if (connectionMode.get() == CONN_MODE_TEAM_NUMBER) {
            return NetworkTablesConnection.fromTeamNumber(portOrTeamNumber.get(), isNt4);
        } else {
            return NetworkTablesConnection.fromAddress(host.get(), portOrTeamNumber.get(), isNt4);
        }
    }

    private void label(String label) {
        ImGui.tableNextColumn();
        ImGui.alignTextToFramePadding();
        ImGui.text(label);
        ImGui.tableNextColumn();
        ImGui.setNextItemWidth(-1);
    }

    private void showConnectionInfo() {
        boolean changed = false;

        if (ImGui.beginTable("layout", 2, ImGuiTableFlags.SizingStretchProp)) {
            label("NT Version:"); changed = ImGui.combo("##version", version, VERSION_NAMES);

            label("Connection Mode:");
            boolean connModeChanged = ImGui.combo("##conn_mode", connectionMode, CONN_MODE_NAMES);
            changed |= connModeChanged;
            if (connModeChanged)
                portOrTeamNumber.set(getDefaultPortOrTeamNumber());

            if (connectionMode.get() == CONN_MODE_TEAM_NUMBER) {
                label("Team Number:"); changed |= ImGui.inputInt("##team_num", portOrTeamNumber);
            } else {
                label("Host:"); changed |= ImGui.inputText("##host", host);
                label("Port:"); changed |= ImGui.inputInt("##port", portOrTeamNumber);
            }

            ImGui.tableNextColumn();
            ImGui.text("Status");
            ImGui.tableNextColumn();
            if (connection != null && connection.isConnected()) {
                ImGui.pushStyleColor(ImGuiCol.Text, COLOR_CONNECTED);
                ImGui.text("Connected");
            } else {
                ImGui.pushStyleColor(ImGuiCol.Text, COLOR_NOT_CONNECTED);
                ImGui.text("Not Connected");
            }
            ImGui.popStyleColor();

            ImGui.endTable();
        }

        if (changed) {
            if (connection != null)
                connection.close();
            connection = openConnection();
        }
    }

    @Override
    public void process() {
        if (ImGui.begin(TITLE)) {
            showConnectionInfo();
            ImGui.separator();
        }
        ImGui.end();
    }
}
