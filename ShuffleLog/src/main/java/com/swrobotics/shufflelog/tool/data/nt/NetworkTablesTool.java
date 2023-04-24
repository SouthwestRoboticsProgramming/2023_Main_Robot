package com.swrobotics.shufflelog.tool.data.nt;

import com.swrobotics.shufflelog.tool.Tool;
import com.swrobotics.shufflelog.tool.data.DataLogTool;
import com.swrobotics.shufflelog.tool.data.PlotDef;
import com.swrobotics.shufflelog.tool.data.ValueAccessor;

import com.swrobotics.shufflelog.tool.smartdashboard.SmartDashboard;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableType;

import imgui.ImGui;
import imgui.flag.*;
import imgui.type.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

// FIXME-Future: Since NetworkTable#getSubTables and NetworkTable#getTopics always seem to
//   return an empty set for NT4, we only use NT3 here, which may be removed from WPILib
//   in the future
public final class NetworkTablesTool implements Tool {
    private static final String TITLE = "NetworkTables";
    private static final String METADATA_TABLE_NAME = "ShuffleLog_Meta";

    private static final int VERSION_NT3 = 0;
    private static final int VERSION_NT4 = 1;
    private static final String[] VERSION_NAMES = {"NT3 (Old)", "NT4 (New)"};

    private static final int CONN_MODE_TEAM_NUMBER = 0;
    private static final int CONN_MODE_ADDRESS = 1;
    private static final String[] CONN_MODE_NAMES = {"Team Number", "Address"};

    private static final int DEFAULT_VERSION = VERSION_NT3;
    private static final int DEFAULT_CONN_MODE = CONN_MODE_TEAM_NUMBER;
    private static final String DEFAULT_HOST = "localhost";
    private static final int[] DEFAULT_PORT_PER_VERSION = {
        NetworkTableInstance.kDefaultPort3, NetworkTableInstance.kDefaultPort4
    };
    private static final int DEFAULT_TEAM_NUMBER = 2129;

    private static final int BOOL_MODE_TOGGLE = 0;
    private static final int BOOL_MODE_MOMENTARY = 1;
    private static final int BOOL_MODE_INV_MOMENTARY = 2;
    private static final int BOOL_MODE_INDICATOR = 3;
    private static final float[] BOOL_INDICATOR_COL_TRUE = {0, 1, 0, 1};
    private static final float[] BOOL_INDICATOR_COL_FALSE = {1, 0, 0, 1};

    private final ImInt version;
    private final ImInt connectionMode;
    private final ImString host;
    private final ImInt portOrTeamNumber;

    private final NetworkTablesConnection connection;

    private final ImBoolean tempBool = new ImBoolean();
    private final ImInt tempInt = new ImInt();
    private final ImDouble tempDouble = new ImDouble();
    private final ImString tempString = new ImString(1024);

    public NetworkTablesTool(ExecutorService threadPool, SmartDashboard smartDashboard) {
        version = new ImInt(DEFAULT_VERSION);
        connectionMode = new ImInt(DEFAULT_CONN_MODE);
        host = new ImString(64);
        host.set(DEFAULT_HOST);
        portOrTeamNumber = new ImInt(getDefaultPortOrTeamNumber());

        connection = new NetworkTablesConnection(threadPool, smartDashboard);
    }

    // --- Server connection ---

    private int getDefaultPortOrTeamNumber() {
        if (connectionMode.get() == CONN_MODE_TEAM_NUMBER) return DEFAULT_TEAM_NUMBER;

        return DEFAULT_PORT_PER_VERSION[version.get()];
    }

    private void updateConnectionServer() {
        NetworkTablesConnection.Params params;
        if (connectionMode.get() == CONN_MODE_TEAM_NUMBER)
            params = new NetworkTablesConnection.Params(portOrTeamNumber.get());
        else params = new NetworkTablesConnection.Params(host.get(), portOrTeamNumber.get());

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
            if (connModeChanged) portOrTeamNumber.set(getDefaultPortOrTeamNumber());

            if (connectionMode.get() == CONN_MODE_TEAM_NUMBER) {
                label("Team Number:");
                ImGui.inputInt("##team_num", portOrTeamNumber);
            } else {
                label("Host:");
                ImGui.inputText("##host", host);
                label("Port:");
                ImGui.inputInt("##port", portOrTeamNumber);
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

    // --- Value editors ---

    private void editBoolean(ValueAccessor<Boolean> val, int mode) {
        switch (mode) {
            case BOOL_MODE_TOGGLE:
                tempBool.set(val.get());
                if (ImGui.checkbox("##bool", tempBool)) val.set(tempBool.get());
                break;
            case BOOL_MODE_MOMENTARY:
            case BOOL_MODE_INV_MOMENTARY:
                val.set(
                        ImGui.button((val.get() ? "True" : "False") + "##bool")
                                ^ (mode == BOOL_MODE_INV_MOMENTARY));
                break;
            case BOOL_MODE_INDICATOR:
                float textSz = ImGui.getTextLineHeight();
                ImGui.colorButton(
                        "##bool",
                        val.get() ? BOOL_INDICATOR_COL_TRUE : BOOL_INDICATOR_COL_FALSE,
                        ImGuiColorEditFlags.NoTooltip,
                        textSz,
                        textSz);
                break;
        }
    }

    private void editInt(ValueAccessor<Integer> val) {
        int[] tempVal = {val.get()};
        if (ImGui.dragInt("##int", tempVal)) val.set(tempVal[0]);
    }

    private void editFloat(ValueAccessor<Float> val) {
        float[] tempVal = {val.get()};
        if (ImGui.dragFloat("##float", tempVal)) val.set(tempVal[0]);
    }

    private void editDouble(ValueAccessor<Double> val) {
        tempDouble.set(val.get());
        if (ImGui.dragScalar("##double", ImGuiDataType.Double, tempDouble, 1))
            val.set(tempDouble.get());
    }

    private void editString(ValueAccessor<String> val) {
        tempString.set(val.get());
        if (ImGui.inputText("##string", tempString)) val.set(tempString.get());
    }

    private void editStringEnum(ValueAccessor<String> val, String[] options) {
        boolean foundCurrent = false;
        String current = val.get();
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(current)) {
                foundCurrent = true;
                tempInt.set(i);
            }
        }
        if (!foundCurrent) {
            if (options.length == 0) {
                ImGui.textDisabled("No options");
                return;
            } else {
                tempInt.set(0);
            }
        }

        if (ImGui.combo("##enum", tempInt, options)) val.set(options[tempInt.get()]);
    }

    // --- Array editors ---
    // We need separate edit functions for each primitive array type because
    // primitive types cannot be used as type parameters

    private void editBooleanArray(String path, ValueAccessor<boolean[]> val) {
        for (int i = 0; i < val.get().length; i++) {
            int idx = i;
            ValueAccessor<Boolean> acc =
                    new ValueAccessor<>(
                            NetworkTableType.kBoolean,
                            () -> {
                                boolean[] data = val.get();
                                return idx < data.length ? data[idx] : false;
                            },
                            (v) -> {
                                boolean[] data = val.get();
                                data[idx] = v;
                                val.set(data);
                            });

            String iStr = String.valueOf(i);
            editRow(
                    iStr,
                    path + NetworkTable.PATH_SEPARATOR + iStr,
                    acc,
                    () -> editBoolean(acc, BOOL_MODE_TOGGLE));
        }
    }

    private void editIntArray(String path, ValueAccessor<int[]> val) {
        for (int i = 0; i < val.get().length; i++) {
            int idx = i;
            ValueAccessor<Integer> acc =
                    new ValueAccessor<>(
                            NetworkTableType.kInteger,
                            () -> {
                                int[] data = val.get();
                                return idx < data.length ? data[idx] : 0;
                            },
                            (v) -> {
                                int[] data = val.get();
                                data[idx] = v;
                                val.set(data);
                            });

            String iStr = String.valueOf(i);
            editRow(iStr, path + NetworkTable.PATH_SEPARATOR + iStr, acc, () -> editInt(acc));
        }
    }

    private void editFloatArray(String path, ValueAccessor<float[]> val) {
        for (int i = 0; i < val.get().length; i++) {
            int idx = i;
            ValueAccessor<Float> acc =
                    new ValueAccessor<>(
                            NetworkTableType.kFloat,
                            () -> {
                                float[] data = val.get();
                                return idx < data.length ? data[idx] : 0;
                            },
                            (v) -> {
                                float[] data = val.get();
                                data[idx] = v;
                                val.set(data);
                            });

            String iStr = String.valueOf(i);
            editRow(iStr, path + NetworkTable.PATH_SEPARATOR + iStr, acc, () -> editFloat(acc));
        }
    }

    private void editDoubleArray(String path, ValueAccessor<double[]> val) {
        for (int i = 0; i < val.get().length; i++) {
            int idx = i;
            ValueAccessor<Double> acc =
                    new ValueAccessor<>(
                            NetworkTableType.kDouble,
                            () -> {
                                double[] data = val.get();
                                return idx < data.length ? data[idx] : 0;
                            },
                            (v) -> {
                                double[] data = val.get();
                                data[idx] = v;
                                val.set(data);
                            });

            String iStr = String.valueOf(i);
            editRow(iStr, path + NetworkTable.PATH_SEPARATOR + iStr, acc, () -> editDouble(acc));
        }
    }

    private void editStringArray(String path, ValueAccessor<String[]> val) {
        for (int i = 0; i < val.get().length; i++) {
            int idx = i;
            ValueAccessor<String> acc =
                    new ValueAccessor<>(
                            NetworkTableType.kString,
                            () -> {
                                String[] data = val.get();
                                return idx < data.length ? data[idx] : "";
                            },
                            (v) -> {
                                String[] data = val.get();
                                data[idx] = v;
                                val.set(data);
                            });

            String iStr = String.valueOf(i);
            editRow(iStr, path + NetworkTable.PATH_SEPARATOR + iStr, acc, () -> editString(acc));
        }
    }

    // --- Data view ---

    private void editRow(String name, String path, ValueAccessor<?> valAcc, Runnable editFn) {
        ImGui.pushID(name);
        ImGui.tableNextColumn();
        ImGui.treeNodeEx(name, ImGuiTreeNodeFlags.Leaf | ImGuiTreeNodeFlags.NoTreePushOnOpen);
        if (DataLogTool.canPlot(valAcc.getType()) && ImGui.beginDragDropSource()) {
            ImGui.text("NT: " + path);
            ImGui.setDragDropPayload(
                    DataLogTool.DRAG_DROP_IN_PAYLOAD, new PlotDef(name, path, valAcc));
            ImGui.endDragDropSource();
        }
        ImGui.tableNextColumn();
        ImGui.setNextItemWidth(-1);
        editFn.run();
        ImGui.tableNextColumn();
        ImGui.text(valAcc.getType().getValueStr());
        ImGui.popID();
    }

    private boolean editArrayRow(String name, NetworkTableType type) {
        ImGui.tableNextColumn();
        boolean open = ImGui.treeNodeEx(name);
        ImGui.tableNextColumn();
        ImGui.textDisabled("--");
        ImGui.tableNextColumn();
        ImGui.text(type.getValueStr());

        return open;
    }

    private int[] downcast(long[] longs) {
        int[] ints = new int[longs.length];
        for (int i = 0; i < ints.length; i++) ints[i] = (int) longs[i];
        return ints;
    }

    private long[] upcast(int[] ints) {
        long[] longs = new long[ints.length];
        for (int i = 0; i < ints.length; i++) longs[i] = ints[i];
        return longs;
    }

    private boolean isArray(NetworkTableType type) {
        switch (type) {
            case kBooleanArray:
            case kIntegerArray:
            case kFloatArray:
            case kDoubleArray:
            case kStringArray:
                return true;
            default:
                return false;
        }
    }

    private void showValue(NetworkTableValueRepr valueRepr, NetworkTableValueRepr metadata) {
        if (isArray(valueRepr.getType())) {
            if (!editArrayRow(valueRepr.getName(), valueRepr.getType())) return;

            switch (valueRepr.getType()) {
                case kBooleanArray:
                    editBooleanArray(
                            valueRepr.getPath(),
                            new ValueAccessor<>(
                                    NetworkTableType.kBooleanArray,
                                    () -> valueRepr.sub.getBooleanArray(new boolean[0]),
                                    (arr) -> valueRepr.getPub().setBooleanArray(arr)));
                    break;
                case kIntegerArray:
                    editIntArray(
                            valueRepr.getPath(),
                            new ValueAccessor<>(
                                    NetworkTableType.kIntegerArray,
                                    () -> downcast(valueRepr.sub.getIntegerArray(new long[0])),
                                    (longs) -> valueRepr.getPub().setIntegerArray(upcast(longs))));
                    break;
                case kFloatArray:
                    editFloatArray(
                            valueRepr.getPath(),
                            new ValueAccessor<>(
                                    NetworkTableType.kFloatArray,
                                    () -> valueRepr.sub.getFloatArray(new float[0]),
                                    (arr) -> valueRepr.getPub().setFloatArray(arr)));
                    break;
                case kDoubleArray:
                    editDoubleArray(
                            valueRepr.getPath(),
                            new ValueAccessor<>(
                                    NetworkTableType.kDoubleArray,
                                    () -> valueRepr.sub.getDoubleArray(new double[0]),
                                    (arr) -> valueRepr.getPub().setDoubleArray(arr)));
                    break;
                case kStringArray:
                    editStringArray(
                            valueRepr.getPath(),
                            new ValueAccessor<>(
                                    NetworkTableType.kStringArray,
                                    () -> valueRepr.sub.getStringArray(new String[0]),
                                    (arr) -> valueRepr.getPub().setStringArray(arr)));
                    break;
            }

            ImGui.treePop();
        } else {
            ValueAccessor<?> valAcc;
            Runnable fn;
            switch (valueRepr.getType()) {
                case kBoolean:
                    ValueAccessor<Boolean> boolAcc =
                            new ValueAccessor<>(
                                    NetworkTableType.kBoolean,
                                    () -> valueRepr.sub.getBoolean(false),
                                    (b) -> valueRepr.getPub().setBoolean(b));
                    valAcc = boolAcc;
                    fn =
                            () ->
                                    editBoolean(
                                            boolAcc,
                                            metadata == null
                                                    ? BOOL_MODE_TOGGLE
                                                    : (int)
                                                            metadata.sub.getInteger(
                                                                    BOOL_MODE_TOGGLE));
                    break;
                case kInteger:
                    ValueAccessor<Integer> intAcc =
                            new ValueAccessor<>(
                                    NetworkTableType.kInteger,
                                    () -> (int) valueRepr.sub.getInteger(0),
                                    (i) -> valueRepr.getPub().setInteger(i));
                    valAcc = intAcc;
                    fn = () -> editInt(intAcc);
                    break;
                case kFloat:
                    ValueAccessor<Float> fltAcc =
                            new ValueAccessor<>(
                                    NetworkTableType.kFloat,
                                    () -> valueRepr.sub.getFloat(0),
                                    (f) -> valueRepr.getPub().setFloat(f));
                    valAcc = fltAcc;
                    fn = () -> editFloat(fltAcc);
                    break;
                case kDouble:
                    ValueAccessor<Double> dblAcc =
                            new ValueAccessor<>(
                                    NetworkTableType.kDouble,
                                    () -> valueRepr.sub.getDouble(0),
                                    (d) -> valueRepr.getPub().setDouble(d));
                    valAcc = dblAcc;
                    fn = () -> editDouble(dblAcc);
                    break;
                case kString:
                    ValueAccessor<String> strAcc =
                            new ValueAccessor<>(
                                    NetworkTableType.kString,
                                    () -> valueRepr.sub.getString(""),
                                    (s) -> valueRepr.getPub().setString(s));
                    valAcc = strAcc;
                    if (metadata != null) {
                        String[] options = metadata.sub.getStringArray(new String[0]);
                        fn = () -> editStringEnum(strAcc, options);
                    } else {
                        fn = () -> editString(strAcc);
                    }
                    break;
                default:
                    return;
            }

            editRow(valueRepr.getName(), valueRepr.getPath(), valAcc, fn);
        }
    }

    private <T> List<T> sortAlphabetically(Set<T> values, Function<T, String> nameGetter) {
        List<T> list = new ArrayList<>(values);
        list.sort(Comparator.comparing(nameGetter, String.CASE_INSENSITIVE_ORDER));
        return list;
    }

    private void showTable(NetworkTableRepr table, NetworkTableRepr metadataTable, boolean root) {
        if (table.getPath().equals(NetworkTable.PATH_SEPARATOR + METADATA_TABLE_NAME)) return;

        boolean open = root;
        if (!root) {
            ImGui.tableNextColumn();
            open = ImGui.treeNodeEx(table.getName());
            ImGui.tableNextColumn();
            ImGui.textDisabled("--"); // Value
            ImGui.tableNextColumn();
            ImGui.textDisabled("--"); // Type
        }

        if (open) {
            for (NetworkTableRepr subtable :
                    sortAlphabetically(table.getSubtables(), NetworkTableRepr::getName)) {
                NetworkTableRepr subMeta = null;
                if (metadataTable != null) {
                    subMeta = metadataTable.getSubtable(subtable.getName());
                }

                showTable(subtable, subMeta, false);
            }
            for (NetworkTableValueRepr value :
                    sortAlphabetically(table.getValues(), NetworkTableValueRepr::getName)) {
                NetworkTableValueRepr subValue = null;
                if (metadataTable != null) {
                    subValue = metadataTable.getValue(value.getName());
                }

                showValue(value, subValue);
            }

            if (!root) ImGui.treePop();
        }
    }

    private void showData() {
        int tableFlags =
                ImGuiTableFlags.BordersOuter
                        | ImGuiTableFlags.BordersInnerV
                        | ImGuiTableFlags.Resizable;

        NetworkTableRepr rootTable = connection.getRootTable();
        if (rootTable == null) {
            ImGui.textDisabled("Not connected");
        } else if (ImGui.beginTable("data", 3, tableFlags)) {
            NetworkTableRepr metadataTable = rootTable.getSubtable(METADATA_TABLE_NAME);
            ImGui.tableSetupColumn("Name", ImGuiTableColumnFlags.WidthStretch, 3);
            ImGui.tableSetupColumn("Value", ImGuiTableColumnFlags.WidthStretch, 2);
            ImGui.tableSetupColumn("Type", ImGuiTableColumnFlags.WidthFixed, 60);
            ImGui.tableHeadersRow();
            showTable(rootTable, metadataTable, true);
            ImGui.endTable();
        }
    }

    @Override
    public void process() {
        if (ImGui.begin(TITLE)) {
            ImGui.text("Instances: " + connection.getActiveInstances());
            showConnectionInfo();
            ImGui.separator();
            showData();
        }
        ImGui.end();
    }
}
