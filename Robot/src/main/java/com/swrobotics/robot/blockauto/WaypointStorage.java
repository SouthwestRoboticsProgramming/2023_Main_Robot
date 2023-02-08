package com.swrobotics.robot.blockauto;

import com.swrobotics.mathlib.Vec2d;
import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.messenger.client.MessengerClient;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Filesystem;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class WaypointStorage {
    private static final File STORAGE_FILE = new File(Filesystem.getOperatingDirectory(), "waypoints.tsv");

    private static final String MSG_GET_WAYPOINTS = "Waypoints:Get";
    private static final String MSG_ADD_WAYPOINT = "Waypoints:Add";
    private static final String MSG_REMOVE_WAYPOINT = "Waypoints:Remove";
    private static final String MSG_WAYPOINTS = "Waypoints:List";
    private static MessengerClient msg;

    private static final class Waypoint {
        private final Vec2d position;
        private final boolean editable;

        public Waypoint(Vec2d position, boolean editable) {
            this.position = position;
            this.editable = editable;
        }
    }

    private static final Map<String, Waypoint> waypoints = new HashMap<>();

    public static void init(MessengerClient msg) {
        WaypointStorage.msg = msg;
        msg.addHandler(MSG_GET_WAYPOINTS, WaypointStorage::onGetWaypoints);
        msg.addHandler(MSG_ADD_WAYPOINT, WaypointStorage::onAddWaypoint);
        msg.addHandler(MSG_REMOVE_WAYPOINT, WaypointStorage::onRemoveWaypoint);

        try {
            if (!STORAGE_FILE.exists())
                return;

            FileReader fr = new FileReader(STORAGE_FILE);
            BufferedReader br = new BufferedReader(fr);

            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split("\t");
                waypoints.put(
                        tokens[0],
                        new Waypoint(new Vec2d(Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2])), true)
                );
            }

            br.close();
        } catch (IOException e) {
            System.err.println("Failed to load waypoints:");
            e.printStackTrace();
        }
    }

    public static Vec2d getWaypointLocation(String name) {
        return waypoints.get(name).position;
    }

    public static void registerStaticWaypoint(String name, Vec2d position) {
        if (waypoints.containsKey(name))
            DriverStation.reportWarning("Static waypoint overwriting existing waypoint: " + name, true);

        waypoints.put(name, new Waypoint(position, false));
    }

    private static void save() {
        try {
            FileWriter fw = new FileWriter(STORAGE_FILE);
            PrintWriter pw = new PrintWriter(fw);

            for (Map.Entry<String, Waypoint> entry : waypoints.entrySet()) {
                String name = entry.getKey();
                Waypoint waypoint = entry.getValue();

                // Don't need to save static waypoints
                if (!waypoint.editable)
                    continue;

                Vec2d pos = waypoint.position;
                pw.println(name + "\t" + pos.x + "\t" + pos.y);
            }

            pw.close();
        } catch (IOException e) {
            System.err.println("Failed to save waypoints:");
            e.printStackTrace();
        }
    }

    private static void onGetWaypoints(String type, MessageReader reader) {
        MessageBuilder builder = msg.prepare(MSG_WAYPOINTS);

        List<String> names = new ArrayList<>(waypoints.keySet());
        names.sort(String.CASE_INSENSITIVE_ORDER);

        builder.addInt(names.size());
        for (String name : names) {
            builder.addString(name);
            Waypoint wp = waypoints.get(name);
            builder.addDouble(wp.position.x);
            builder.addDouble(wp.position.y);
            builder.addBoolean(wp.editable);
        }
        builder.send();
    }

    private static void onAddWaypoint(String type, MessageReader reader) {
        String name = reader.readString();
        double x = reader.readDouble();
        double y = reader.readDouble();

        // Don't overwrite static waypoints
        Waypoint existing = waypoints.get(name);
        if (existing != null && !existing.editable)
            return;

        waypoints.put(name, new Waypoint(new Vec2d(x, y), true));
        save();
    }

    private static void onRemoveWaypoint(String type, MessageReader reader) {
        String name = reader.readString();

        // Don't remove static waypoints
        if (waypoints.containsKey(name) && !waypoints.get(name).editable)
            return;

        waypoints.remove(name);
        save();
    }
}
