package com.swrobotics.shufflelog.tool.data;

import com.swrobotics.shufflelog.tool.Tool;
import com.swrobotics.shufflelog.tool.data.PlotDef.Type;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableType;
import imgui.flag.*;
import imgui.type.ImBoolean;
import imgui.type.ImDouble;
import imgui.type.ImInt;
import imgui.type.ImString;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static imgui.ImGui.*;

public final class NetworkTablesTool implements Tool {
    private static final int TEAM_NUMBER = 0;
    private static final int ADDRESS = 1;
    private static final String[] MODE_NAMES = {"Team Number", "Address"};

    private static final int DEFAULT_TEAM_NUMBER = 2129;
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = NetworkTableInstance.kDefaultPort;

    private static final String METADATA_TABLE = "ShuffleLog_Meta";

    private static final int BOOL_MODE_TOGGLE = 0;
    private static final int BOOL_MODE_MOMENTARY = 1;
    private static final int BOOL_MODE_INV_MOMENTARY = 2;
    private static final int BOOL_MODE_INDICATOR = 3;

    private final ExecutorService threadPool;
    private final NetworkTableInstance nt;
    private final NetworkTable metadata;
    private final DataLogTool dataLog;

    private final ImInt connectionMode;
    private final ImString host;
    private final ImInt portOrTeamNumber;

    private Future<?> reconnectFuture;
    private boolean requiresReconnect;

    public NetworkTablesTool(ExecutorService threadPool, DataLogTool dataLog) {
        this.threadPool = threadPool;
        this.dataLog = dataLog;
        nt = NetworkTableInstance.getDefault();
        metadata = nt.getTable(METADATA_TABLE);
        connectionMode = new ImInt(TEAM_NUMBER);

        host = new ImString(64);
        portOrTeamNumber = new ImInt(2129);

        requiresReconnect = true;
    }

    private void connectNT() {
        if (connectionMode.get() == ADDRESS) {
            nt.setServer(host.get(), portOrTeamNumber.get());
        } else {
            nt.setServerTeam(portOrTeamNumber.get());
        }
        nt.setNetworkIdentity("ShuffleLog");
        nt.startClient();
        nt.startDSClient();
    }

    private void reconnectNT() {
        if (!requiresReconnect)
            return;

        // If currently reconnecting, don't try again until it's done
        if (reconnectFuture != null && !reconnectFuture.isDone())
            return;

        requiresReconnect = false;
        reconnectFuture = threadPool.submit(() -> {
            disconnectNT();
            connectNT();
        });
    }

    private void disconnectNT() {
        nt.stopDSClient();
        nt.stopClient();
        while (nt.getNetworkMode() != 0) {
            Thread.onSpinWait();
        }
        for (NetworkTableEntry entry : nt.getEntries("", 0)) {
            entry.delete();
        }
    }

    private void showConnectionParams() {
        boolean paramsChanged = false;

        text("Connection Mode:");
        if (combo("##conn_mode", connectionMode, MODE_NAMES)) {
            paramsChanged = true;

            if (connectionMode.get() == TEAM_NUMBER) {
                portOrTeamNumber.set(DEFAULT_TEAM_NUMBER);
            } else {
                host.set(DEFAULT_HOST);
                portOrTeamNumber.set(DEFAULT_PORT);
            }
        }

        separator();

        if (beginTable("layout", 2, ImGuiTableFlags.SizingStretchProp)) {
            if (connectionMode.get() == TEAM_NUMBER) {
                tableNextColumn();
                text("Team: ");
                tableNextColumn();
                pushItemWidth(-1);
                paramsChanged |= inputInt("##team", portOrTeamNumber);
                popItemWidth();
            } else {
                tableNextColumn();
                text("Host:");
                tableNextColumn();
                pushItemWidth(-1);
                paramsChanged |= inputText("##host", host);
                popItemWidth();

                tableNextColumn();
                text("Port: ");
                tableNextColumn();
                pushItemWidth(-1);
                paramsChanged |= inputInt("##port", portOrTeamNumber);
                popItemWidth();
            }

            separator();

            tableNextColumn();
            text("Status:");
            tableNextColumn();
            boolean connected = nt.isConnected();
            if (connected) {
                pushStyleColor(ImGuiCol.Text, 0.0f, 1.0f, 0.0f, 1.0f);
                text("Connected");
            } else {
                pushStyleColor(ImGuiCol.Text, 1.0f, 0.0f, 0.0f, 1.0f);
                text("Not connected");
            }
            popStyleColor();

            endTable();
        }

        if (paramsChanged) {
            requiresReconnect = true;
        }

        reconnectNT();
    }

    private final ImBoolean b = new ImBoolean();
    private final ImDouble d = new ImDouble();
    private final ImInt tempInt = new ImInt();
    private final ImString s = new ImString(256);

    private void showPrimitiveEntry(String name, NetworkTableEntry entry, String path) {
        tableNextColumn();
        treeNodeEx(name, ImGuiTreeNodeFlags.Leaf | ImGuiTreeNodeFlags.NoTreePushOnOpen | ImGuiTreeNodeFlags.SpanFullWidth);
        if ((entry.getType() == NetworkTableType.kBoolean || entry.getType() == NetworkTableType.kDouble) && beginDragDropSource()) {
            text(name);
            setDragDropPayload("NT_DRAG_VALUE", new PlotDef(
                    entry.getType() == NetworkTableType.kBoolean ? Type.BOOLEAN : Type.DOUBLE,
                    path, name, entry
            ));
            endDragDropSource();
        }
        tableNextColumn();
        pushID("nt_value:" + name);
        pushItemWidth(-1);
        boolean graph = false;
        switch (entry.getType()) {
            case kBoolean: {
                graph = true;

                int mode = BOOL_MODE_TOGGLE;
                NetworkTableEntry metadataEntry = metadata.getEntry(path.substring(1)); // Substring to remove leading slash
                if (metadataEntry.exists() && metadataEntry.getType() == NetworkTableType.kDouble) {
                    mode = metadataEntry.getNumber(BOOL_MODE_TOGGLE).intValue();
                }

                switch (mode) {
                    case BOOL_MODE_TOGGLE: {
                        b.set(entry.getBoolean(false));
                        if (checkbox("", b)) {
                            entry.setBoolean(b.get());
                        }
                        break;
                    }
                    case BOOL_MODE_MOMENTARY:
                    case BOOL_MODE_INV_MOMENTARY: {
                        button("Press");
                        boolean pressed = isItemActive();

                        if (mode == BOOL_MODE_INV_MOMENTARY)
                            pressed = !pressed;

                        entry.setBoolean(pressed);
                        break;
                    }
                    case BOOL_MODE_INDICATOR: {
                        if (entry.getBoolean(false)) {
                            colorButton("", new float[]{0, 1, 0, 1}, ImGuiColorEditFlags.NoTooltip);
                        } else {
                            colorButton("", new float[]{1, 0, 0, 1}, ImGuiColorEditFlags.NoTooltip);
                        }
                        break;
                    }
                }

                break;
            }
            case kDouble: {
                graph = true;
                d.set(entry.getDouble(0.0));
                if (dragScalar("", ImGuiDataType.Double, d, 0.1f)) {
                    entry.setDouble(d.get());
                }
                break;
            }
            case kString: {
                NetworkTableEntry metadataEntry = metadata.getEntry(path.substring(1)); // Substring to remove leading slash
                if (metadataEntry.exists() && metadataEntry.getType() == NetworkTableType.kStringArray) {
                    // It is actually an enum
                    String[] enumValues = metadataEntry.getStringArray(EMPTY_STRING_ARRAY);
                    String current = entry.getString("");

                    int currentIdx = 0;
                    for (int i = 0; i < enumValues.length; i++) {
                        if (enumValues[i].equals(current)) {
                            currentIdx = i;
                            break;
                        }
                    }
                    tempInt.set(currentIdx);

                    if (combo("", tempInt, enumValues)) {
                        int newIdx = tempInt.get();
                        entry.setString(enumValues[newIdx]);
                    }
                } else {
                    // It is a normal String
                    s.set(entry.getString(""));
                    if (inputText("", s)) {
                        entry.setString(s.get());
                    }
                }
                break;
            }
            default: {
                textDisabled("Can't edit (unknown type)");
            }
        }
        popItemWidth();
        popID();
        tableNextColumn();
        text(entry.getType().name());
        tableNextColumn();
        if (graph && button("Graph##" + path)) {
            if (entry.getType() == NetworkTableType.kBoolean) {
                dataLog.addBooleanPlot(path, name, entry);
            } else {
                dataLog.addDoublePlot(path, name, entry);
            }
        }
    }

    private static final boolean[] EMPTY_BOOL_ARRAY = new boolean[0];
    private static final double[] EMPTY_DOUBLE_ARRAY = new double[0];
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private void showArrayEntry(String name, NetworkTableEntry entry, String path) {
        tableNextColumn();
        boolean open = treeNodeEx(name, ImGuiTreeNodeFlags.SpanFullWidth);
        tableNextColumn();
        textDisabled("--");
        tableNextColumn();
        text(entry.getType().name());
        tableNextColumn();

        if (open) {
            switch (entry.getType()) {
                case kBooleanArray: {
                    boolean[] val = entry.getBooleanArray(EMPTY_BOOL_ARRAY);
                    boolean changed = false;
                    for (int i = 0; i < val.length; i++) {
                        tableNextColumn();
                        treeNodeEx(String.valueOf(i), ImGuiTreeNodeFlags.Leaf | ImGuiTreeNodeFlags.NoTreePushOnOpen | ImGuiTreeNodeFlags.SpanFullWidth);
                        if (beginDragDropSource()) {
                            text(name + "[" + i + "]");
                            setDragDropPayload("NT_DRAG_VALUE", new PlotDef(
                                    Type.BOOLEAN_ARRAY_ENTRY, path + "/" + i, name + "/" + i, entry, i
                            ));
                            endDragDropSource();
                        }
                        tableNextColumn();
                        b.set(val[i]);
                        pushItemWidth(-1);
                        changed |= checkbox("##nt_elem:" + i, b);
                        popItemWidth();
                        tableNextColumn();
                        textDisabled("--");
                        val[i] = b.get();
                        tableNextColumn();
                        if (button("Graph##" + path + "/" + i)) {
                            dataLog.addBooleanArrayEntryPlot(path + "/" + i, name + "/" + i, entry, i);
                        }
                    }
                    if (changed) entry.setBooleanArray(val);
                    break;
                }
                case kDoubleArray: {
                    double[] val = entry.getDoubleArray(EMPTY_DOUBLE_ARRAY);
                    boolean changed = false;
                    for (int i = 0; i < val.length; i++) {
                        tableNextColumn();
                        treeNodeEx(String.valueOf(i), ImGuiTreeNodeFlags.Leaf | ImGuiTreeNodeFlags.NoTreePushOnOpen | ImGuiTreeNodeFlags.SpanFullWidth);
                        if (beginDragDropSource()) {
                            text(name + "[" + i + "]");
                            setDragDropPayload("NT_DRAG_VALUE", new PlotDef(
                                    Type.DOUBLE_ARRAY_ENTRY, path + "/" + i, name + "/" + i, entry, i
                            ));
                            endDragDropSource();
                        }
                        tableNextColumn();
                        d.set(val[i]);
                        pushItemWidth(-1);
                        changed |= dragScalar("##nt_elem:" + i, ImGuiDataType.Double, d, 0.1f);
                        popItemWidth();
                        tableNextColumn();
                        textDisabled("--");
                        val[i] = d.get();
                        tableNextColumn();
                        if (button("Graph##" + path + "/" + i)) {
                            dataLog.addDoubleArrayEntryPlot(path + "/" + i, name + "/" + i, entry, i);
                        }
                    }
                    if (changed) entry.setDoubleArray(val);
                    break;
                }
                case kStringArray: {
                    String[] val = entry.getStringArray(EMPTY_STRING_ARRAY);
                    boolean changed = false;
                    for (int i = 0; i < val.length; i++) {
                        tableNextColumn();
                        treeNodeEx(String.valueOf(i), ImGuiTreeNodeFlags.Leaf | ImGuiTreeNodeFlags.NoTreePushOnOpen | ImGuiTreeNodeFlags.SpanFullWidth);
                        tableNextColumn();
                        s.set(val[i]);
                        pushItemWidth(-1);
                        changed |= inputText("##nt_elem:" + i, s);
                        popItemWidth();
                        tableNextColumn();
                        textDisabled("--");
                        val[i] = s.get();
                        tableNextColumn();
                    }
                    if (changed) entry.setStringArray(val);
                    break;
                }
                default: {
                    throw new IllegalStateException("Unknown array type");
                }
            }
            treePop();
        }
    }

    private void showEntry(String name, NetworkTableEntry entry, String path) {
        tableNextRow();

        NetworkTableType type = entry.getType();
        boolean isArray =
                type == NetworkTableType.kBooleanArray ||
                type == NetworkTableType.kDoubleArray ||
                type == NetworkTableType.kStringArray;

        if (isArray) {
            showArrayEntry(name, entry, path);
        } else {
            showPrimitiveEntry(name, entry, path);
        }
    }

    private void showTable(String name, NetworkTable table, String path, boolean isRoot) {
        // Hide the metadata table so you can't accidentally break things
        if (path.equals("/" + METADATA_TABLE))
            return;

        tableNextRow();

        tableNextColumn();
        boolean open = treeNodeEx(name, ImGuiTreeNodeFlags.SpanFullWidth | (isRoot ? ImGuiTreeNodeFlags.DefaultOpen : 0));
        tableNextColumn();
        textDisabled("--");
        tableNextColumn();
        textDisabled("--");

        if (open) {
            for (String key : table.getKeys()) {
                showEntry(key, table.getEntry(key), path + "/" + key);
            }

            for (String subtable : table.getSubTables()) {
                showTable(subtable, table.getSubTable(subtable), path + "/" + subtable, false);
            }

            treePop();
        }
    }

    private void showEntryTree() {
        int flags = ImGuiTableFlags.BordersV
                | ImGuiTableFlags.BordersOuterH
                | ImGuiTableFlags.Resizable
                | ImGuiTableFlags.RowBg;

        if (beginTable("nt_tree", 4, flags)) {
            tableSetupColumn("Name", ImGuiTableColumnFlags.WidthFixed, 135);
            tableSetupColumn("Value", ImGuiTableColumnFlags.WidthStretch);
            tableSetupColumn("Type", ImGuiTableColumnFlags.WidthFixed, 110);
            tableHeadersRow();

            showTable("Root", nt.getTable("/"), "", true);

            endTable();
        }
    }

    @Override
    public void process() {
        if (begin("NetworkTables")) {
            setWindowSize(500, 450, ImGuiCond.FirstUseEver);

            showConnectionParams();
            separator();
            showEntryTree();
        }
        end();
    }
}
