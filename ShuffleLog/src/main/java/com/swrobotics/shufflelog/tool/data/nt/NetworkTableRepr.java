package com.swrobotics.shufflelog.tool.data.nt;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.Topic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class NetworkTableRepr implements AutoCloseable {
    private final NetworkTable table;
    private final Map<String, NetworkTableRepr> subtableCache;
    private final Map<String, NetworkTableValueRepr> valueCache;

    public NetworkTableRepr(NetworkTable table) {
        this.table = table;
        subtableCache = new HashMap<>();
        valueCache = new HashMap<>();
    }

    public String getPath() {
        return table.getPath();
    }

    /**
     * Gets the name of this specific table within its parent, without a leading or trailing path
     * separator.
     *
     * @return name
     */
    public String getName() {
        String path = getPath();
        int lastSeparatorIdx = path.lastIndexOf(NetworkTable.PATH_SEPARATOR);
        if (lastSeparatorIdx < 0) return path;
        else return path.substring(lastSeparatorIdx + 1);
    }

    public NetworkTableRepr getSubtable(String name) {
        if (!table.containsSubTable(name)) return null;

        return getOrCreateSubtable(name);
    }

    public NetworkTableValueRepr getValue(String name) {
        if (!table.containsKey(name)) return null;

        return getOrCreateValue(name);
    }

    private NetworkTableRepr getOrCreateSubtable(String name) {
        NetworkTableRepr existing = subtableCache.get(name);
        if (existing != null) {
            return existing;
        } else {
            NetworkTableRepr repr = new NetworkTableRepr(table.getSubTable(name));
            subtableCache.put(name, repr);
            return repr;
        }
    }

    private NetworkTableValueRepr getOrCreateValue(String name) {
        NetworkTableValueRepr existing = valueCache.get(name);
        if (existing != null) {
            return existing;
        } else {
            NetworkTableValueRepr repr = new NetworkTableValueRepr(table.getTopic(name));
            valueCache.put(name, repr);
            return repr;
        }
    }

    public Set<NetworkTableRepr> getSubtables() {
        Set<NetworkTableRepr> out = new HashSet<>();
        Set<String> subtableNames = table.getSubTables();

        Map<String, NetworkTableRepr> toRemove = new HashMap<>(subtableCache);
        for (String key : subtableNames) {
            out.add(getOrCreateSubtable(key));
            toRemove.remove(key);
        }

        for (NetworkTableRepr repr : toRemove.values()) repr.close();

        return out;
    }

    public Set<NetworkTableValueRepr> getValues() {
        Set<NetworkTableValueRepr> out = new HashSet<>();

        Map<String, NetworkTableValueRepr> toRemove = new HashMap<>(valueCache);
        for (Topic topic : table.getTopics()) {
            NetworkTableValueRepr existing = valueCache.get(topic.getName());
            if (existing != null) {
                out.add(existing);
            } else {
                NetworkTableValueRepr repr = new NetworkTableValueRepr(topic);
                out.add(repr);
                valueCache.put(topic.getName(), repr);
            }
            toRemove.remove(topic.getName());
        }

        for (NetworkTableValueRepr repr : toRemove.values()) repr.close();

        return out;
    }

    @Override
    public void close() {
        for (NetworkTableValueRepr value : valueCache.values()) {
            value.close();
        }
    }
}
