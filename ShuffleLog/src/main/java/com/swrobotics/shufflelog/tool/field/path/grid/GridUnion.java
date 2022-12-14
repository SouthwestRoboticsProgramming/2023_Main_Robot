package com.swrobotics.shufflelog.tool.field.path.grid;

import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.shufflelog.tool.field.path.PathfindingLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class GridUnion extends Grid {
    private final List<Grid> children;

    public GridUnion(UUID id) {
        super(id);
        children = new ArrayList<>();
    }

    public List<Grid> getChildren() {
        return children;
    }

    @Override
    public void readContent(MessageReader reader) {
        int count = reader.readInt();
        for (int i = 0; i < count; i++) {
            children.add(Grid.read(reader));
        }
    }

    @Override
    public void register(PathfindingLayer layer) {
        super.register(layer);
        for (Grid child : children) {
            child.register(layer);
        }
    }
}
