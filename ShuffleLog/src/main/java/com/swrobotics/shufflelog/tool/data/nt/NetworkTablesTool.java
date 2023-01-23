package com.swrobotics.shufflelog.tool.data.nt;

import com.swrobotics.shufflelog.tool.Tool;
import edu.wpi.first.networktables.NetworkTableInstance;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiTableFlags;
import imgui.type.ImInt;
import imgui.type.ImString;

import java.util.concurrent.ExecutorService;

// FIXME-Future: Since NetworkTable#getSubTables and NetworkTable#getTopics always seem to
//   return an empty set for NT4, we only use NT3 here, which may be removed from WPILib
//   in the future
public final class NetworkTablesTool implements Tool {
    private static final String TITLE = "NetworkTables";

    private static final int VERSION_NT3 = 0;
    private static final int VERSION_NT4 = 1;
    private static final String[] VERSION_NAMES = {"NT3 (Old)", "NT4 (New)"};

    private static final int CONN_MODE_TEAM_NUMBER = 0;
    private static final int CONN_MODE_ADDRESS = 1;
    private static final String[] CONN_MODE_NAMES = {"Team Number", "Address"};

    private static final int DEFAULT_VERSION = VERSION_NT3;
    private static final int DEFAULT_CONN_MODE = CONN_MODE_ADDRESS;
    private static final String DEFAULT_HOST = "localhost";
    private static final int[] DEFAULT_PORT_PER_VERSION = {
            NetworkTableInstance.kDefaultPort3,
            NetworkTableInstance.kDefaultPort4
    };
    private static final int DEFAULT_TEAM_NUMBER = 2129;

    private final ImInt version;
    private final ImInt connectionMode;
    private final ImString host;
    private final ImInt portOrTeamNumber;

    private final NetworkTablesConnection connection;

    public NetworkTablesTool(ExecutorService threadPool) {
        version = new ImInt(DEFAULT_VERSION);
        connectionMode = new ImInt(DEFAULT_CONN_MODE);
        host = new ImString(64);
        host.set(DEFAULT_HOST);
        portOrTeamNumber = new ImInt(getDefaultPortOrTeamNumber());

        connection = new NetworkTablesConnection(threadPool);
    }

    private int getDefaultPortOrTeamNumber() {
        if (connectionMode.get() == CONN_MODE_TEAM_NUMBER)
            return DEFAULT_TEAM_NUMBER;

        return DEFAULT_PORT_PER_VERSION[version.get()];
    }

    private void updateConnectionServer() {
        NetworkTablesConnection.Params params;
        if (connectionMode.get() == CONN_MODE_TEAM_NUMBER)
            params = new NetworkTablesConnection.Params(portOrTeamNumber.get());
        else
            params = new NetworkTablesConnection.Params(host.get(), portOrTeamNumber.get());

        connection.setServerParams(version.get() == VERSION_NT4, params);
    }

    private void label(String label) {
        ImGui.tableNextColumn();
        ImGui.alignTextToFramePadding();
        ImGui.text(label);
        ImGui.tableNextColumn();
        ImGui.setNextItemWidth(-1);
    }

    private void showConnectionInfo() {
        if (ImGui.beginTable("layout", 2, ImGuiTableFlags.SizingStretchProp)) {
            // FIXME-Future: Enable when NT4 works
            // label("NT Version:"); ImGui.combo("##version", version, VERSION_NAMES);

            label("Connection Mode:");
            boolean connModeChanged = ImGui.combo("##conn_mode", connectionMode, CONN_MODE_NAMES);
            if (connModeChanged)
                portOrTeamNumber.set(getDefaultPortOrTeamNumber());

            if (connectionMode.get() == CONN_MODE_TEAM_NUMBER) {
                label("Team Number:"); ImGui.inputInt("##team_num", portOrTeamNumber);
            } else {
                label("Host:"); ImGui.inputText("##host", host);
                label("Port:"); ImGui.inputInt("##port", portOrTeamNumber);
            }

            ImGui.tableNextColumn();
            ImGui.text("Status");
            ImGui.tableNextColumn();
            NetworkTablesConnection.Status status = connection.getStatus();
            ImGui.pushStyleColor(ImGuiCol.Text, status.getColor());
            ImGui.text(status.getFriendlyName());
            ImGui.popStyleColor();

            ImGui.endTable();
        }

        updateConnectionServer();
    }

    private void showValue(NetworkTableValueRepr value) {
        ImGui.text("Value: " + value.getName() + ": " + value.getType());
    }

    private void showTable(NetworkTableRepr table) {
        ImGui.text("Table: " + table.getName());
        ImGui.indent();
        for (NetworkTableRepr subtable : table.getSubtables()) {
            showTable(subtable);
        }
        for (NetworkTableValueRepr value : table.getValues()) {
            showValue(value);
        }
        ImGui.unindent();
    }

    @Override
    public void process() {
        if (ImGui.begin(TITLE)) {
            ImGui.text("Instances: " + connection.getActiveInstances());
            showConnectionInfo();
            ImGui.separator();
            NetworkTableRepr rootTable = connection.getRootTable();
            if (rootTable == null) {
                ImGui.textDisabled("Not connected");
            } else {
                showTable(rootTable);
            }
        }
        ImGui.end();
    }
}
