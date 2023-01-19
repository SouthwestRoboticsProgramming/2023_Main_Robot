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

    public Set<NetworkTableRepr> getSubtables() {
        Set<NetworkTableRepr> out = new HashSet<>();
        Set<String> subtableNames = table.getSubTables();

        Map<String, NetworkTableRepr> toRemove = new HashMap<>(subtableCache);
        for (String key : subtableNames) {
            NetworkTableRepr existing = subtableCache.get(key);
            if (existing != null) {
                out.add(existing);
            } else {
                NetworkTableRepr repr = new NetworkTableRepr(table.getSubTable(key));
                out.add(repr);
                subtableCache.put(key, repr);
            }
            toRemove.remove(key);
        }

        for (NetworkTableRepr repr : toRemove.values())
            repr.close();

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

        for (NetworkTableValueRepr repr : toRemove.values())
            repr.close();

        return out;
    }

    @Override
    public void close() {

    }
}
