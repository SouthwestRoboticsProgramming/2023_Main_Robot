package com.swrobotics.robot.blockauto;

import com.swrobotics.mathlib.Vec2d;
import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.messenger.client.MessengerClient;
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

    private static final Map<String, Vec2d> waypoints = new HashMap<>();

    public static void init(MessengerClient msg) {
        WaypointStorage.msg = msg;
        msg.addHandler(MSG_GET_WAYPOINTS, WaypointStorage::onGetWaypoints);
        msg.addHandler(MSG_ADD_WAYPOINT, WaypointStorage::onAddWaypoint);
        msg.addHandler(MSG_REMOVE_WAYPOINT, WaypointStorage::onRemoveWaypoint);

        try {
            FileReader fr = new FileReader(STORAGE_FILE);
            BufferedReader br = new BufferedReader(fr);

            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split("\t");
                waypoints.put(tokens[0], new Vec2d(Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2])));
            }

            br.close();
        } catch (IOException e) {
            System.err.println("Failed to load waypoints:");
            e.printStackTrace();
        }
    }

    private static void save() {
        try {
            FileWriter fw = new FileWriter(STORAGE_FILE);
            PrintWriter pw = new PrintWriter(fw);

            for (Map.Entry<String, Vec2d> waypoint : waypoints.entrySet()) {
                String name = waypoint.getKey();
                Vec2d pos = waypoint.getValue();
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
            Vec2d pos = waypoints.get(name);
            builder.addDouble(pos.x);
            builder.addDouble(pos.y);
        }
        builder.send();
    }

    private static void onAddWaypoint(String type, MessageReader reader) {
        String name = reader.readString();
        double x = reader.readDouble();
        double y = reader.readDouble();

        waypoints.put(name, new Vec2d(x, y));
        save();
    }

    private static void onRemoveWaypoint(String type, MessageReader reader) {
        String name = reader.readString();
        waypoints.remove(name);
        save();
    }
}
