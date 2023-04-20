package com.swrobotics.shufflelog.tool.smartdashboard;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import imgui.ImGui;
import imgui.type.ImBoolean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class SmartDashboard {
    public static final String WINDOW_PREFIX = "SD: ";
    public static final SmartDashboard INSTANCE = new SmartDashboard();

    private NetworkTable table;
    private final Map<String, SmartDashboardTool> tools;
    private final Set<String> openTools;
    private final ImBoolean pOpen;

    private SmartDashboard() {
        tools = new HashMap<>();
        openTools = new HashSet<>();
        pOpen = new ImBoolean(false);
    }

    public void init(NetworkTableInstance inst) {
        table = inst.getTable("SmartDashboard");
    }

    public void close() {
        table = null;
    }

    public void showMenuItems() {
        // Update tools
        Set<String> removedTools = new HashSet<>(tools.keySet());
        for (String childName : table.getSubTables()) {
            removedTools.remove(childName);
            if (tools.containsKey(childName))
                continue; // Already exists

            NetworkTable child = table.getSubTable(childName);

            String type = child.getEntry(".type").getString("NULL");
            if (type.equals("NULL"))
                continue;

            String name = child.getEntry(".name").getString("");
            switch (type) {
                case "Mechanism2d":
                    tools.put(name, new Mechanism2dTool(name, child));
                    break;
            }
        }
        for (String removed : removedTools) {
            tools.remove(removed);
        }

        ArrayList<String> keys = new ArrayList<>(tools.keySet());
        keys.sort(String.CASE_INSENSITIVE_ORDER);
        for (String key : keys) {
            pOpen.set(openTools.contains(key));
            ImGui.menuItem(key, null, pOpen);
            if (pOpen.get())
                openTools.add(key);
            else
                openTools.remove(key);
        }
    }

    public void process() {
        for (String key : openTools) {
            tools.get(key).process();
        }
    }
}
